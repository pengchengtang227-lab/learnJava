package com.example.springstudy.mvcdemo;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/** 流程演示用的 Handler（就是平常写的 Controller），一个走传统视图，一个走前后端分离的 JSON。 */
@Controller
@Profile("mvc-flow-demo")
@RequestMapping("/mvc-demo")
public class MvcDemoController {

    private final FlowTrace trace;

    public MvcDemoController(FlowTrace trace) {
        this.trace = trace;
    }

    /** 传统模式：返回 ModelAndView，后面会经过 ViewResolver 和 View。 */
    @GetMapping("/page")
    public ModelAndView page() {
        trace.add("④ Handler(Controller)  执行业务方法 page()");
        return new ModelAndView("demoPage", "message", "Hello Spring MVC");
    }

    /** 前后端分离：@ResponseBody 直接返回数据，由 HttpMessageConverter 转 JSON，不走 ViewResolver。 */
    @GetMapping("/json")
    @ResponseBody
    public Map<String, String> json() {
        trace.add("④ Handler(Controller)  执行业务方法 json()");
        return Map.of("message", "Hello Spring MVC");
    }
}
