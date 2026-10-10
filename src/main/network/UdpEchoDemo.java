package network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** UDP 案例：发送一个数据报，服务端向发送方回复一个数据报。 */
public class UdpEchoDemo {
    private static final int TIMEOUT_MS = 5000;

    public static void main(String[] args) throws Exception {
        runDemo();
    }

    public static void runDemo() throws Exception {
        System.out.println("\n=== 3. UDP 数据报通信 ===");
        InetAddress address = InetAddress.getByName("127.0.0.1");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (DatagramSocket server = new DatagramSocket(new InetSocketAddress(address, 0));
             DatagramSocket client = new DatagramSocket(new InetSocketAddress(address, 0))) {
            server.setSoTimeout(TIMEOUT_MS);
            client.setSoTimeout(TIMEOUT_MS);
            System.out.println("UDP 服务端端口：" + server.getLocalPort());

            Future<Void> serverTask = executor.submit(() -> {
                byte[] buffer = new byte[1024];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                server.receive(request); // 阻塞等待一个数据报，超时会抛出 SocketTimeoutException。
                // 只解码实际收到的字节；不能把整个缓冲区都当成消息内容。
                String message = new String(request.getData(), request.getOffset(),
                        request.getLength(), StandardCharsets.UTF_8);
                byte[] reply = ("服务端收到：" + message).getBytes(StandardCharsets.UTF_8);
                // 请求中携带发送方的地址与端口，可直接用来构造回复。
                server.send(new DatagramPacket(reply, reply.length, request.getSocketAddress()));
                return null;
            });

            // UDP connect() 不进行 TCP 握手，只设置默认目标，并过滤其他地址发来的数据报。
            client.connect(address, server.getLocalPort());
            String message = "你好，UDP";
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            client.send(new DatagramPacket(data, data.length));

            byte[] buffer = new byte[1024];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            client.receive(reply);
            String response = new String(reply.getData(), reply.getOffset(),
                    reply.getLength(), StandardCharsets.UTF_8);
            if (!("服务端收到：" + message).equals(response)) {
                throw new IllegalStateException("UDP 响应不符合预期：" + response);
            }
            System.out.println("客户端收到：" + response);
            serverTask.get(TIMEOUT_MS * 2L, TimeUnit.MILLISECONDS);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }
        // UDP 保留数据报边界，但不保证送达、顺序或不重复；本例超时后直接报错。
        // 接收缓冲区过小会截断数据报；复用 DatagramPacket 接收时需先 setLength(buffer.length)。
        // 实际业务应按需求增加确认、去重或重试，并避免发送需要 IP 分片的大数据报。
    }
}
