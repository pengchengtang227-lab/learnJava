package javabasicPractice.network;

import java.net.InetAddress;

/** 地址解析案例：获取 IP、主机名，并识别回环地址。 */
public class InetAddressDemo {

    public static void main(String[] args) throws Exception {
        runDemo();
    }

    public static void runDemo() throws Exception {
        System.out.println("\n=== 1. InetAddress 地址解析 ===");
        // localhost 通常由系统主机配置解析，不需要访问外部 DNS 服务。
        // 换成域名时可能触发 DNS 查询，解析失败会抛出 UnknownHostException。
        for (InetAddress address : InetAddress.getAllByName("localhost")) {
            System.out.println("localhost -> " + address.getHostAddress());
        }

        // 数字形式的 IP 地址无需查询 DNS；127.0.0.1 是 IPv4 回环地址。
        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        System.out.println("IP 地址：" + loopback.getHostAddress());
        System.out.println("是否为回环地址：" + loopback.isLoopbackAddress());
        // getHostName() 在某些情况下会尝试反向解析；只需要 IP 时使用 getHostAddress()。
        System.out.println("主机名：" + loopback.getHostName());
        // 地址解析成功不代表服务可用；应连接目标端口或发起实际请求来验证服务。
    }
}
