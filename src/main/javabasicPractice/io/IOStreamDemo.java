package javabasicPractice.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public class IOStreamDemo {

    public static void main(String[] args) throws IOException {
        // 相对路径以程序的工作目录为基准；在 IDEA 中可通过运行配置设置工作目录和程序参数。
        // 默认写入 target/javabasicPractice.io-demo；每次运行覆盖本示例的同名文件，请使用专门的示例目录。
        Path directory = args.length > 0 ? Path.of(args[0]) : Path.of("target", "javabasicPractice.io-demo");
        Files.createDirectories(directory);
        System.out.println("示例文件目录：" + directory.toAbsolutePath());

        copyBinaryFile(directory);
        readAndWriteText(directory);
        convertByteAndCharacterStreams(directory);
        useMemoryStreams();
        readAndWriteProperties(directory);
        readAndWriteBinaryData(directory);
        useFilesApi(directory);
        // 教学入口将 IOException 向上传递，便于观察错误；业务代码应按业务需求处理或继续抛出。
    }

    /**
     * 案例 1：使用缓冲字节流复制文件，适合附件保存、图片备份等场景。
     * 分块读取不会把整个文件加载到内存，适合大文件。
     */
    private static void copyBinaryFile(Path directory) throws IOException {
        System.out.println("\n=== 1. 缓冲字节流复制文件 ===");
        Path source = directory.resolve("source.bin");
        Path target = directory.resolve("source-javabasicPractice.copy.bin");
        // 准备演示数据；实际项目中 source 通常是已有附件或上传文件。
        Files.write(source, new byte[]{0, 1, 2, 3, 127, (byte) 128, (byte) 255});

        // 缓冲流包装底层文件流；关闭外层流时也会关闭被包装的底层流。
        try (InputStream input = new BufferedInputStream(Files.newInputStream(source));
             OutputStream output = new BufferedOutputStream(Files.newOutputStream(target))) {
            byte[] buffer = new byte[8192];
            int length;
            // read() 返回实际读取的字节数；-1 表示结束，不能用 available() 判断文件结束。
            while ((length = input.read(buffer)) != -1) {
                // 最后一块可能没有填满 buffer，因此只写入本次读取的有效部分。
                output.write(buffer, 0, length);
            }
            // close() 会刷新缓冲区；若需要在流关闭前让下游读到数据，则应显式 flush()。
            // flush() 只是把数据交给下游，不保证数据已经持久化到磁盘。
        }
        System.out.println("复制完成，文件大小：" + Files.size(target) + " 字节");
    }

    /**
     * 案例 2：逐行写入、追加和读取文本，适合生成报表、处理日志或文本清单。
     */
    private static void readAndWriteText(Path directory) throws IOException {
        System.out.println("\n=== 2. 文本读写与追加 ===");
        Path report = directory.resolve("report.txt");

        // 默认选项为 CREATE、TRUNCATE_EXISTING、WRITE：创建文件或覆盖已有内容。
        try (BufferedWriter writer = Files.newBufferedWriter(report, StandardCharsets.UTF_8)) {
            writer.write("订单编号,商品,数量");
            writer.newLine(); // 使用当前系统的换行符。
            writer.write("1001,键盘,2");
            writer.newLine();
        }

        // APPEND 用于追加；CREATE 确保文件不存在时也能创建。
        try (BufferedWriter writer = Files.newBufferedWriter(report, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write("1002,鼠标,1");
            writer.newLine();
        }

        try (BufferedReader reader = Files.newBufferedReader(report, StandardCharsets.UTF_8)) {
            String line;
            // readLine() 不包含换行符，返回 null 才表示结束；空字符串只是一个空行。
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        // 这里只展示无特殊字符的 CSV；含逗号、引号或换行的字段应交给 CSV 库处理。
    }

    /**
     * 案例 3：在字节流和字符流之间转换，适合读取 HTTP 响应流、上传的文本附件。
     */
    private static void convertByteAndCharacterStreams(Path directory) throws IOException {
        System.out.println("\n=== 3. 字节流与字符流的编码转换 ===");
        Path text = directory.resolve("message.txt");

        // OutputStreamWriter 将字符按指定编码转换为字节，再写入底层输出流。
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(text), StandardCharsets.UTF_8))) {
            writer.write("你好，Java I/O！");
            writer.newLine();
            writer.write("你好，Java I/O！11111");
        }

        // InputStreamReader 将字节按指定编码解码为字符；编码要与实际数据一致。
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Files.newInputStream(text), StandardCharsets.UTF_8))) {
            String line;
            // readLine() 不包含换行符，返回 null 才表示结束；空字符串只是一个空行。
            System.out.println("解码后的文本：");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        // 实际文件读写可直接使用 Files.newBufferedReader/Writer，这里展示转换流的原理。
    }

    /**
     * 案例 4：在内存中组装字节数据，适合小型导出文件、测试数据和需要 byte[] 的接口。
     */
    private static void useMemoryStreams() throws IOException {
        System.out.println("\n=== 4. 内存字节流 ===");
        byte[] content;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            output.write("用户：张三\n".getBytes(StandardCharsets.UTF_8));
            output.write("状态：已启用\n".getBytes(StandardCharsets.UTF_8));
            // toByteArray() 返回一份副本；大文件应直接流式传输，避免占用大量堆内存。
            content = output.toByteArray();
        }

        // ByteArrayInputStream 可以将 byte[] 适配为接收 InputStream 的接口所需参数。
        try (InputStream input = new ByteArrayInputStream(content);
             ByteArrayOutputStream received = new ByteArrayOutputStream()) {
            input.transferTo(received); // Java 9+，将输入流剩余内容复制到输出流。
            // transferTo() 不会替你关闭两个流，生命周期仍由调用方负责。
            System.out.print(received.toString(StandardCharsets.UTF_8));
        }
        // 内存流的 close() 无实际效果；此处保留统一的资源管理写法。
    }

    /**
     * 案例 5：保存和加载 properties 配置，适合简单的应用配置文件。
     */
    private static void readAndWriteProperties(Path directory) throws IOException {
        System.out.println("\n=== 5. properties 配置读写 ===");
        Path config = directory.resolve("app.properties");
        Properties properties = new Properties();
        properties.setProperty("app.name", "订单服务");
        properties.setProperty("app.port", "8080");

        // 使用 Writer/Reader 重载显式处理 UTF-8；load(InputStream) 使用 ISO-8859-1。
        try (BufferedWriter writer = Files.newBufferedWriter(config, StandardCharsets.UTF_8)) {
            properties.store(writer, "Application settings");
        }

        Properties loaded = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
            loaded.load(reader);
        }
        // 配置值都是字符串，可提供默认值；转成数字前应在业务代码中进行有效性校验。
        System.out.println("应用名称：" + loaded.getProperty("app.name"));
        System.out.println("服务端口：" + loaded.getProperty("app.port", "8080"));
    }

    /**
     * 案例 6：按类型读写二进制数据，适合双方约定格式的缓存文件或简单数据协议。
     */
    private static void readAndWriteBinaryData(Path directory) throws IOException {
        System.out.println("\n=== 6. DataInputStream / DataOutputStream ===");
        Path record = directory.resolve("record.dat");
        try (DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(record)))) {
            output.writeInt(1001);
            output.writeUTF("张三");
            output.writeDouble(99.5);
            output.writeBoolean(true);
        }

        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(record)))) {
            // 读取顺序和类型必须与写入完全一致，二进制文件不会自行告诉你字段类型。
            // 实际项目还应约定格式版本；新增或调整字段时，需要考虑旧文件的读取兼容性。
            int id = input.readInt();
            String name = input.readUTF();
            double score = input.readDouble();
            boolean enabled = input.readBoolean();
            System.out.printf("编号=%d，姓名=%s，分数=%.1f，启用=%s%n", id, name, score, enabled);
        }
        // writeUTF/readUTF 使用 Java 的 modified UTF-8；单个字符串编码后最多 65535 字节。
        // double 仅用于演示数值读写；金额应使用 javabasicPractice.BigDecimal 并约定保存格式。
    }

    /**
     * 案例 7：使用 Path / Files 简化常见文件操作，适合小型配置和目录扫描。
     */
    private static void useFilesApi(Path directory) throws IOException {
        System.out.println("\n=== 7. Path / Files 常用操作 ===");
        Path notes = directory.resolve("notes.txt");
        // writeString/readString 适合小文本；readString 会把全部内容读入内存。
        // 大文件应使用缓冲流分块或逐行处理；readAllBytes() 同样会读取整个文件。
        Files.writeString(notes, "第一行：初始化\n第二行：处理完成\n", StandardCharsets.UTF_8);
        System.out.print(Files.readString(notes, StandardCharsets.UTF_8));

        // Files.lines 按需读取；返回的 Stream 持有文件资源，必须显式关闭。
        try (Stream<String> lines = Files.lines(notes, StandardCharsets.UTF_8)) {
            long count = lines.filter(line -> !line.isBlank()).count();
            System.out.println("非空行数：" + count);
        }

        // 不需要自行控制复制过程时，可直接使用 Files.javabasicPractice.copy。
        Path backup = directory.resolve("notes-backup.txt");
        Files.copy(notes, backup, StandardCopyOption.REPLACE_EXISTING);

        // Files.list 只列出当前目录，不递归；它的 Stream 同样需要关闭。
        try (Stream<Path> files = Files.list(directory)) {
            List<String> names = files.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
            System.out.println("当前目录的文件：" + names);
        }
    }
}
