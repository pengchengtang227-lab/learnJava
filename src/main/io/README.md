# Java I/O 流常用案例

入口类：`io.IOStreamDemo`，适用于项目当前使用的 Java 17，无需额外依赖。

| 案例 | 使用的 API | 常见场景 |
| --- | --- | --- |
| 缓冲字节流复制 | `BufferedInputStream`、`BufferedOutputStream` | 附件保存、图片备份、大文件分块复制 |
| 文本读写与追加 | `BufferedReader`、`BufferedWriter` | 文本报表、日志处理、逐行读取 |
| 编码转换 | `InputStreamReader`、`OutputStreamWriter` | HTTP 响应流、文本上传内容的解码和编码 |
| 内存字节流 | `ByteArrayInputStream`、`ByteArrayOutputStream` | 小型文件导出、接口适配、测试数据 |
| 配置读写 | `Properties` 配合 `Reader` / `Writer` | 加载和保存应用配置 |
| 二进制数据读写 | `DataInputStream`、`DataOutputStream` | 约定格式的本地缓存、简单数据协议 |
| 文件工具 API | `Path`、`Files` | 小文本读写、按行统计、复制和目录扫描 |

## 运行方式

在 IDEA 中直接运行 `IOStreamDemo.main()`。示例会准备所需数据，不需要提前创建输入文件。

也可以在项目根目录使用 PowerShell 运行：

```powershell
javac -encoding UTF-8 -d target/io-classes src/main/io/IOStreamDemo.java
java '-Dfile.encoding=UTF-8' -cp target/io-classes io.IOStreamDemo
```

默认输出到工作目录下的 `target/io-demo`，每次运行会覆盖本示例的同名文件。可以通过程序参数指定输出目录：

```powershell
java '-Dfile.encoding=UTF-8' -cp target/io-classes io.IOStreamDemo target/io-custom-demo
```

## 使用要点

| 要点 | 说明 |
| --- | --- |
| 字节流与字符流 | 任意二进制文件使用字节流；需要解析文字时使用字符流并明确编码 |
| 关闭资源 | 使用 `try-with-resources`；包装流关闭时会关闭底层流 |
| 读取结束条件 | 字节流的 `read()` 返回 `-1`；`readLine()` 返回 `null`；空行不是文件结束 |
| 缓冲区写入长度 | 使用 `write(buffer, 0, length)`，避免将最后一块的无效数据写入文件 |
| 刷新缓冲区 | `flush()` 将缓冲数据交给下游，但不代表数据已持久化到磁盘；关闭输出流也会刷新 |
| 大文件内存占用 | 分块复制或逐行处理；避免对大文件使用 `readAllBytes()`、`readString()` 或内存输出流 |
| Stream 资源 | `Files.lines()`、`Files.list()` 返回的流也必须关闭 |
| 二进制格式 | 字段顺序、类型及格式版本由读写双方约定；`writeUTF()` 不是普通 UTF-8 文本输出 |

每个案例的方法注释说明业务场景，关键语句的中文注释说明资源管理、编码和常见易错点。
