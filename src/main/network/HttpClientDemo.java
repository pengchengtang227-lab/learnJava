package network;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** HTTP 案例：使用 Java 11+ 的 HttpClient 发起 GET、POST，并处理非成功状态码。 */
public class HttpClientDemo {
    private static final int MAX_BODY_BYTES = 1024;

    public static void main(String[] args) throws Exception {
        runDemo();
    }

    public static void runDemo() throws Exception {
        System.out.println("\n=== 4. HTTP GET 与 POST ===");
        // JDK 自带的轻量 HTTP 服务，方便本地学习；生产服务通常使用成熟的 Web 框架。
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            server.setExecutor(executor);
            server.createContext("/", HttpClientDemo::handleRequest);
            server.start();
            URI baseUri = URI.create("http://127.0.0.1:" + server.getAddress().getPort());

            // HttpClient 可复用；连接超时限制建立连接，请求超时限制整个请求等待。
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpRequest getRequest = HttpRequest.newBuilder(baseUri.resolve("/hello"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "text/plain")
                    .GET()
                    .build();
            HttpResponse<String> getResponse = client.send(getRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            checkResponse(getResponse, 200, "你好，HTTP");
            System.out.println("GET 状态码：" + getResponse.statusCode());
            System.out.println("GET 响应类型：" + getResponse.headers()
                    .firstValue("Content-Type").orElse("未提供"));
            System.out.println("GET 响应内容：" + getResponse.body());

            String body = "订单编号：1001，商品：键盘";
            HttpRequest postRequest = HttpRequest.newBuilder(baseUri.resolve("/echo"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> postResponse = client.send(postRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            checkResponse(postResponse, 200, body);
            System.out.println("POST 状态码：" + postResponse.statusCode());
            System.out.println("POST 响应内容：" + postResponse.body());

            HttpRequest missingRequest = HttpRequest.newBuilder(baseUri.resolve("/missing"))
                    .timeout(Duration.ofSeconds(5)).GET().build();
            HttpResponse<String> missingResponse = client.send(missingRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            checkResponse(missingResponse, 404, "资源不存在");
            System.out.println("不存在的资源状态码：" + missingResponse.statusCode());
            // send() 不会因为 404/500 自动抛出异常，必须自己检查 statusCode()。
            // 网络故障可能抛出 IOException；线程中断会抛出 InterruptedException。
        } finally {
            server.stop(0);
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        // ofString() 将响应加载到内存，大文件可用 ofFile() 或 ofInputStream()。
        // 使用 ofInputStream() 时要自行关闭流；Java 17 的 HttpClient 本身无需手动 close()。
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if ("/hello".equals(path)) {
                if (!"GET".equals(method)) {
                    exchange.getResponseHeaders().set("Allow", "GET");
                    respond(exchange, 405, "请求方法不支持");
                    return;
                }
                respond(exchange, 200, "你好，HTTP");
            } else if ("/echo".equals(path)) {
                if (!"POST".equals(method)) {
                    exchange.getResponseHeaders().set("Allow", "POST");
                    respond(exchange, 405, "请求方法不支持");
                    return;
                }
                // 多读一个字节来判断是否超限，避免无上限读取请求体。
                byte[] requestBody = exchange.getRequestBody().readNBytes(MAX_BODY_BYTES + 1);
                if (requestBody.length > MAX_BODY_BYTES) {
                    respond(exchange, 413, "请求体过大");
                    return;
                }
                respond(exchange, 200, new String(requestBody, StandardCharsets.UTF_8));
            } else {
                respond(exchange, 404, "资源不存在");
            }
        }
    }

    private static void respond(HttpExchange exchange, int status, String text) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        // 响应长度按字节计算，中文字符串的字符数与 UTF-8 字节数不同。
        exchange.sendResponseHeaders(status, data.length);
        exchange.getResponseBody().write(data);
    }

    private static void checkResponse(HttpResponse<String> response, int status, String body) {
        if (response.statusCode() != status || !body.equals(response.body())) {
            throw new IllegalStateException("HTTP 响应不符合预期："
                    + response.statusCode() + "，" + response.body());
        }
    }
}
