# Java 网络编程案例

适用于本项目的 Java 17，仅使用 JDK API，无需额外依赖。入口类是 `javabasicPractice.network.NetworkProgrammingDemo`。

| 文件 | 案例 | 重点 |
| --- | --- | --- |
| `InetAddressDemo.java` | 地址解析 | 主机名、IP 地址、回环地址 |
| `TcpEchoDemo.java` | TCP 多行消息通信 | 连接、消息边界、UTF-8、刷新缓冲、半关闭 |
| `UdpEchoDemo.java` | UDP 请求与回复 | 数据报、发送方地址、有效长度、接收超时 |
| `HttpClientDemo.java` | HTTP GET、POST、404 | 请求头、请求体、状态码、响应头、本地 HTTP 服务 |
| `NetworkProgrammingDemo.java` | 总入口 | 一次运行全部案例，或通过参数选择案例 |

## 运行方式

在 IDEA 中直接运行 `NetworkProgrammingDemo.main()`，也可以分别运行四个案例的 `main()`。
TCP、UDP、HTTP 案例会自行启动本地服务、发送请求并关闭资源，不需要开两个终端。
所有服务仅监听 `127.0.0.1`，使用系统分配的空闲端口，重复运行也无需修改端口。

在项目根目录使用 PowerShell：

```powershell
javac -encoding UTF-8 -d target/javabasicPractice.network-classes src/main/javabasicPractice.network/*.java
java '-Dfile.encoding=UTF-8' -cp target/javabasicPractice.network-classes javabasicPractice.network.NetworkProgrammingDemo
```

按参数选择案例：

```powershell
java '-Dfile.encoding=UTF-8' -cp target/javabasicPractice.network-classes javabasicPractice.network.NetworkProgrammingDemo tcp
java '-Dfile.encoding=UTF-8' -cp target/javabasicPractice.network-classes javabasicPractice.network.NetworkProgrammingDemo udp
java '-Dfile.encoding=UTF-8' -cp target/javabasicPractice.network-classes javabasicPractice.network.NetworkProgrammingDemo http
java '-Dfile.encoding=UTF-8' -cp target/javabasicPractice.network-classes javabasicPractice.network.NetworkProgrammingDemo address
```

也可以独立运行，例如：

```powershell
java '-Dfile.encoding=UTF-8' -cp target/javabasicPractice.network-classes javabasicPractice.network.TcpEchoDemo
```

## 使用要点

| 要点 | 说明 |
| --- | --- |
| TCP 与 UDP | TCP 提供可靠、有序的字节流；UDP 保留数据报边界，但不保证送达、顺序或不重复 |
| 消息协议 | TCP 本例使用换行符分隔消息，因此一条消息不能包含换行；实际业务可使用长度前缀协议 |
| 字符编码 | 发送和接收双方显式使用 UTF-8；数据包大小与 HTTP 响应长度按字节计算 |
| 超时 | 示例设置 5 秒连接、接收或请求超时，超时会抛出异常；阻塞 Socket 的读取超时不限制写入时间 |
| HTTP 状态码 | HTTP 请求成功传输不等于业务成功；404、500 等仍需检查 `statusCode()` |
| 资源释放 | Socket 使用 try-with-resources；HTTP 服务和线程池在 finally 中关闭 |
| 并发模型 | 为了便于学习，TCP 只处理一个连接，UDP 只回复一个数据报，HTTP 使用单线程处理请求 |
| 输入限制 | HTTP 回显限制请求体为 1024 字节；TCP 示例处理可信的小消息，扩展时需限制消息长度 |
| 学习范围 | 本地 HTTP 使用明文；访问真实服务时优先 HTTPS，并按业务需要处理鉴权、重试与错误 |

每个案例均有中文类注释和关键步骤注释。通信案例会检查响应内容，不符合预期时抛出异常。
