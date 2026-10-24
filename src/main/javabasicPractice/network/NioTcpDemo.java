package javabasicPractice.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** 使用 Selector 实现的最小 NIO TCP 回显示例：单线程服务端同时处理多个客户端。 */
public class NioTcpDemo {
    private static final int TIMEOUT_MS = 5000;
    private static final int CLIENT_COUNT = 5;

    public static void main(String[] args) throws Exception {
        CompletableFuture<Integer> portFuture = new CompletableFuture<>();
        Thread serverThread = new Thread(() -> runServer(portFuture), "nio-tcp-server");
        serverThread.start();

        int port = portFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);

        // 多个客户端各占一个线程同时连接，服务端仍然只用一个线程、一个 Selector 处理。
        ExecutorService pool = Executors.newFixedThreadPool(CLIENT_COUNT);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 1; i <= CLIENT_COUNT; i++) {
            int id = i;
            futures.add(pool.submit(() -> {
                runClient(port, id);
                return null;
            }));
        }
        for (Future<?> future : futures) {
            future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }
        pool.shutdown();

        // 等待服务端退出，避免遗留后台线程。
        serverThread.join(TIMEOUT_MS);
    }

    /** NIO 服务端：Selector 监听连接和读取事件，处理完 CLIENT_COUNT 个客户端后退出。 */
    private static void runServer(CompletableFuture<Integer> portFuture) {
        try (ServerSocketChannel server = ServerSocketChannel.open();
             Selector selector = Selector.open()) {
            // 127.0.0.1 是回环地址，只允许本机连接；端口 0 表示自动分配空闲端口。
            server.bind(new InetSocketAddress("127.0.0.1", 0));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            portFuture.complete(((InetSocketAddress) server.getLocalAddress()).getPort());

            int remaining = CLIENT_COUNT;
            while (remaining > 0) {
                selector.select(TIMEOUT_MS); // 等待事件，不会一直占用 CPU。
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        // accept() 返回的连接已经完成 TCP 三次握手。
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        // 新连接注册到同一个 Selector 上，这就是多路复用。
                        client.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                        System.out.println("服务端接受连接：" + client.getRemoteAddress()
                                + "，处理线程：" + Thread.currentThread().getName());
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer request = (ByteBuffer) key.attachment();
                        int count = client.read(request);

                        if (count == -1) {
                            client.close();
                            remaining--;
                        } else if (count > 0) {
                            request.flip();
                            String message = StandardCharsets.UTF_8.decode(request).toString();
                            ByteBuffer response = StandardCharsets.UTF_8.encode("服务端收到：" + message);

                            // 本示例消息很短，直接写完响应。
                            while (response.hasRemaining()) {
                                client.write(response);
                            }
                            client.close();
                            remaining--;
                        }
                    }
                }
            }
        } catch (Exception e) {
            portFuture.completeExceptionally(e);
            throw new RuntimeException("NIO TCP 服务端运行失败", e);
        }
    }

    /** NIO 客户端：连接、写入、读取都由 Selector 事件驱动。 */
    private static void runClient(int port, int id) throws IOException {
        try (SocketChannel client = SocketChannel.open();
             Selector selector = Selector.open()) {
            client.configureBlocking(false);
            // 本机连接可能立即成功，此时不会再触发 OP_CONNECT，直接关注写事件。
            int ops = client.connect(new InetSocketAddress("127.0.0.1", port))
                    ? SelectionKey.OP_WRITE : SelectionKey.OP_CONNECT;
            client.register(selector, ops, StandardCharsets.UTF_8.encode("你好，NIO TCP-" + id));

            ByteBuffer response = ByteBuffer.allocate(1024);
            boolean finished = false;
            while (!finished) {
                if (selector.select(TIMEOUT_MS) == 0) {
                    throw new IOException("等待 NIO 事件超时");
                }

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isConnectable()) {
                        // 非阻塞 connect 需要调用 finishConnect() 确认连接完成。
                        if (client.finishConnect()) {
                            key.interestOps(SelectionKey.OP_WRITE);
                        }
                    } else if (key.isWritable()) {
                        ByteBuffer request = (ByteBuffer) key.attachment();
                        client.write(request);
                        if (!request.hasRemaining()) {
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    } else if (key.isReadable()) {
                        if (!response.hasRemaining()) {
                            throw new IOException("响应超过缓冲区大小：" + response.capacity());
                        }
                        if (client.read(response) == -1) {
                            response.flip();
                            System.out.println("客户端" + id + "收到：" + StandardCharsets.UTF_8.decode(response));
                            finished = true;
                        }
                    }
                }
            }
        }
    }
}
