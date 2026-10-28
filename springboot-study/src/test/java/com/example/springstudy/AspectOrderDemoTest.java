package com.example.springstudy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 激活 aop-order-demo 后会多出一个 @Order(1) 的切面，
 * 通过日志出现的先后位置，验证“进入时数字小的在前，退出时数字小的在后”。
 */
// 独立的内存库名：application.yml 的库带 DB_CLOSE_DELAY=-1，同一 JVM 里第二个容器会重复执行 schema.sql 而报错。
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:aop-order-demo;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("aop-order-demo")
@ExtendWith(OutputCaptureExtension.class)
class AspectOrderDemoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void lowerOrderAspectWrapsOutsideOfHigherOrderAspect(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/api/accounts")).andExpect(status().isOk());

        String log = output.getOut();
        int outerEnter = log.indexOf("进入外层切面");
        int innerEnter = log.indexOf("接口开始");
        int controllerBody = log.indexOf("查询账户列表");
        int innerExit = log.indexOf("接口结束");
        int outerExit = log.indexOf("退出外层切面");

        assertThat(outerEnter).isNotNegative();
        assertThat(outerEnter).isLessThan(innerEnter);
        assertThat(innerEnter).isLessThan(controllerBody);
        assertThat(controllerBody).isLessThan(innerExit);
        assertThat(innerExit).isLessThan(outerExit);
    }
}
