package com.example.springstudy.mvcdemo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** 按发生顺序记录 Spring MVC 各组件的调用，测试结束后可打印、可断言。 */
@Component
@Profile("mvc-flow-demo")
public class FlowTrace {

    private final List<String> steps = new CopyOnWriteArrayList<>();

    public void add(String step) {
        steps.add(step);
    }

    public List<String> steps() {
        return List.copyOf(steps);
    }

    /** 每一步的“编号 + 组件名”（描述前用两个以上空格分隔），便于断言顺序而不依赖描述文字。 */
    public List<String> labels() {
        return steps.stream().map(step -> step.split("\\s{2,}", 2)[0]).toList();
    }

    public void clear() {
        steps.clear();
    }
}
