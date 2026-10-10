package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** TCP 案例：客户端发送多行消息，服务端逐行回复。 */
public class TcpEchoDemo {
    private static final int TIMEOUT_MS = 5000;

    public static void main(String[] args) throws Exception {
        runDemo();
    }

    public static void runDemo() throws Exception {
        System.out.println("\n=== 2. TCP 请求与响应 ===");
        InetAddress address = InetAddress.getByName("127.0.0.1");
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 端口 0 表示让系统分配空闲端口；只绑定回环地址，客户端和服务端均在本机运行。
        try (ServerSocket server = new ServerSocket(0, 1, address)) {
            server.setSoTimeout(TIMEOUT_MS); // 限制 accept() 等待连接的时间。
            System.out.println("TCP 服务端端口：" + server.getLocalPort());

            Future<Void> serverTask = executor.submit(() -> {
                // accept() 阻塞直到客户端连接；该教学服务仅接受一个连接。
                try (Socket connection = server.accept();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(
                             connection.getInputStream(), StandardCharsets.UTF_8));
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                             connection.getOutputStream(), StandardCharsets.UTF_8))) {
                    connection.setSoTimeout(TIMEOUT_MS); // 限制 socket 读取等待时间。
                    String message;
                    // TCP 是字节流，没有消息边界；双方约定每行是一条完整消息。
                    // readLine() 返回 null 表示对方已结束输出，空行仍然是一条消息。
                    while ((message = reader.readLine()) != null) {
                        writer.write("服务端收到：" + message);
                        writer.newLine();
                        writer.flush(); // 立即发送缓冲数据，避免客户端一直等待读取。
                    }
                }
                return null;
            });

            try (Socket client = new Socket()) {
                // connect 超时与读取超时是两种不同设置，都应根据业务需求配置。
                client.connect(new InetSocketAddress(address, server.getLocalPort()), TIMEOUT_MS);
                client.setSoTimeout(TIMEOUT_MS);
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                             client.getOutputStream(), StandardCharsets.UTF_8));
                     BufferedReader reader = new BufferedReader(new InputStreamReader(
                             client.getInputStream(), StandardCharsets.UTF_8))) {
                    for (String message : List.of("你好，TCP", "订单编号：1001", "22222222222")) {
                        writer.write(message);
                        writer.newLine();
                        writer.flush();
                        String response = reader.readLine();
                        if (!("服务端收到：" + message).equals(response)) {
                            throw new IllegalStateException("TCP 响应不符合预期：" + response);
                        }
                        System.out.println("客户端收到：" + response);
                    }
                    // 半关闭：告诉服务端发送完毕，同时保留客户端的读取能力。
                    client.shutdownOutput();
                    if (reader.readLine() != null) {
                        throw new IllegalStateException("TCP 服务端返回了额外消息");
                    }
                }
            }
            // 等待服务端结束，并让后台线程中的异常传播到主线程。
            serverTask.get(TIMEOUT_MS * 2L, TimeUnit.MILLISECONDS);
        } finally {
            // 先由 try-with-resources 关闭 socket，再关闭线程池，异常时也不会遗留监听端口。
            executor.shutdownNow();
            executor.awaitTermination(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }
        // 本例使用可信的小消息；面向不可信输入时，需要限制消息长度及并发连接数量。
    }
}
