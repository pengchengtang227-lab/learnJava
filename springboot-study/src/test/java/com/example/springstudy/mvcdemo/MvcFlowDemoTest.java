package com.example.springstudy.mvcdemo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * 真实启动内嵌 Tomcat 发 HTTP 请求（不用 MockMvc），打印并断言 Spring MVC 一次请求的完整流转顺序。
 * 运行后看控制台，对照“背诵版”的 8 个步骤即可。
 */
// 独立的内存库名，避免与其它测试共用的 H2 库重复执行 schema.sql。
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:mvc-flow-demo;DB_CLOSE_DELAY=-1")
@ActiveProfiles("mvc-flow-demo")
class MvcFlowDemoTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FlowTrace trace;

    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void cleanTraceFile() throws IOException {
        Files.deleteIfExists(Path.of("target", "mvc-flow-trace.txt"));
    }

    @BeforeEach
    void setUp() {
        trace.clear();
    }

    @Test
    void traditionalFlow_returnsModelAndView_goesThroughViewResolver() throws Exception {
        HttpResponse<String> response = get("/mvc-demo/page");

        print("传统模式：返回 ModelAndView", trace.steps());
        assertThat(response.body()).isEqualTo("页面内容：Hello Spring MVC");
        assertThat(trace.labels()).containsExactly(
                "① DispatcherServlet", "② HandlerMapping", "②' 拦截器 preHandle", "③ HandlerAdapter",
                "④ Handler(Controller)", "⑤ HandlerAdapter", "⑤' 拦截器 postHandle", "⑥ ViewResolver",
                "⑦ View", "⑦' 拦截器 afterCompletion", "⑧ DispatcherServlet");
    }

    @Test
    void restFlow_returnsJson_skipsViewResolver() throws Exception {
        HttpResponse<String> response = get("/mvc-demo/json");

        print("前后端分离：@ResponseBody 返回 JSON", trace.steps());
        assertThat(response.body()).isEqualTo("{\"message\":\"Hello Spring MVC\"}");
        assertThat(trace.labels()).containsExactly(
                "① DispatcherServlet", "② HandlerMapping", "②' 拦截器 preHandle", "③ HandlerAdapter",
                "④ Handler(Controller)", "⑤ HandlerAdapter", "⑤' 拦截器 postHandle",
                "⑦' 拦截器 afterCompletion", "⑧ DispatcherServlet");
        assertThat(trace.steps()).noneMatch(step -> step.startsWith("⑥") || step.startsWith("⑦ View"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void print(String title, List<String> steps) {
        System.out.println("\n===== " + title + " =====");
        steps.forEach(System.out::println);
        // Windows 默认 GBK 控制台可能把中文和 ①② 序号显示成乱码，所以同时写一份 UTF-8 文件，用编辑器打开即可。
        try {
            Path file = Path.of("target", "mvc-flow-trace.txt");
            String text = "===== " + title + " =====\n" + String.join("\n", steps) + "\n\n";
            Files.writeString(file, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
