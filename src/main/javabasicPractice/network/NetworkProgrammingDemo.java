package javabasicPractice.network;

/**
 * Java 网络编程示例总入口，所有通信都在本机完成，无需联网或启动其他程序。
 * 可传入 address、tcp、udp、http 运行单个案例，不传参数则运行全部案例。
 */
public class NetworkProgrammingDemo {

    public static void main(String[] args) throws Exception {
        String mode = args.length == 0 ? "all" : args[0];
        switch (mode) {
            case "all" -> {
                InetAddressDemo.runDemo();
                TcpEchoDemo.runDemo();
                UdpEchoDemo.runDemo();
                HttpClientDemo.runDemo();
            }
            case "address" -> InetAddressDemo.runDemo();
            case "tcp" -> TcpEchoDemo.runDemo();
            case "udp" -> UdpEchoDemo.runDemo();
            case "http" -> HttpClientDemo.runDemo();
            default -> throw new IllegalArgumentException("参数应为 all、address、tcp、udp 或 http");
        }
        System.out.println("\n网络编程案例运行完成。");
        // 教学入口向上传递异常，便于观察问题；业务代码应根据需求记录日志、重试或返回错误。
    }
}
