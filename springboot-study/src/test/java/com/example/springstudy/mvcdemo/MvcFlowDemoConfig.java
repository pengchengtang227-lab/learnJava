package com.example.springstudy.mvcdemo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.method.HandlerMethod;

/**
 * 给 Spring MVC 的每个核心组件“装一个录音笔”，把一次请求的流转顺序记到 {@link FlowTrace}。
 * 只在 mvc-flow-demo 环境下生效，不影响项目里其它测试和正式代码。
 */
@Configuration
@Profile("mvc-flow-demo")
public class MvcFlowDemoConfig implements WebMvcConfigurer {

    private final FlowTrace trace;

    public MvcFlowDemoConfig(FlowTrace trace) {
        this.trace = trace;
    }

    /** DispatcherServlet：前端控制器。bean 名必须叫 dispatcherServlet，Boot 就不再创建默认的。 */
    @Bean(name = "dispatcherServlet")
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet() {
            @Override
            protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
                trace.add("① DispatcherServlet  拦截请求 " + request.getMethod() + " " + request.getRequestURI());
                super.doDispatch(request, response);
                trace.add("⑧ DispatcherServlet  处理完毕，HTTP 状态码 " + response.getStatus());
            }
        };
    }

    /** HandlerMapping 与 HandlerAdapter：Boot 允许通过 WebMvcRegistrations 替换默认实现。 */
    @Bean
    public WebMvcRegistrations tracingRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new RequestMappingHandlerMapping() {
                    @Override
                    protected @Nullable HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
                        HandlerMethod handler = super.getHandlerInternal(request);
                        if (handler != null) {
                            trace.add("② HandlerMapping     按 URL 匹配到 Handler -> " + handler.getShortLogMessage());
                        }
                        return handler;
                    }
                };
            }

            @Override
            public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
                return new RequestMappingHandlerAdapter() {
                    @Override
                    protected @Nullable ModelAndView handleInternal(HttpServletRequest request,
                            HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
                        trace.add("③ HandlerAdapter     适配并调用 Handler");
                        ModelAndView mav = super.handleInternal(request, response, handlerMethod);
                        trace.add("⑤ HandlerAdapter     Handler 返回 -> "
                                + (mav == null ? "null（已直接写出响应体，例如 JSON）"
                                        : "ModelAndView(视图名=" + mav.getViewName() + ", 数据=" + mav.getModel() + ")"));
                        return mav;
                    }
                };
            }
        };
    }

    /** 拦截器：HandlerMapping 会把它和 Handler 一起封装成“执行链”。 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                trace.add("②' 拦截器 preHandle   Handler 执行前");
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                    @Nullable ModelAndView modelAndView) {
                trace.add("⑤' 拦截器 postHandle  Handler 执行后、视图渲染前");
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                    @Nullable Exception ex) {
                trace.add("⑦' 拦截器 afterCompletion  整个请求完成后");
            }
        }).addPathPatterns("/mvc-demo/**");
    }

    /** ViewResolver：把逻辑视图名 demoPage 解析成真正的 View。 */
    @Bean
    public ViewResolver demoViewResolver() {
        return new DemoViewResolver();
    }

    class DemoViewResolver implements ViewResolver, Ordered {

        @Override
        public @Nullable View resolveViewName(String viewName, Locale locale) {
            if (!"demoPage".equals(viewName)) {
                return null;
            }
            trace.add("⑥ ViewResolver       逻辑视图名 \"" + viewName + "\" -> 解析出真实 View");
            return new View() {
                @Override
                public String getContentType() {
                    return "text/plain;charset=UTF-8";
                }

                @Override
                public void render(@Nullable Map<String, ?> model, HttpServletRequest request,
                        HttpServletResponse response) throws Exception {
                    trace.add("⑦ View               拿到 Model " + model + "，渲染页面");
                    response.setContentType(getContentType());
                    response.getOutputStream().write(("页面内容：" + model.get("message"))
                            .getBytes(StandardCharsets.UTF_8));
                }
            };
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }
}
