package cn.org.agatha;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String ip = null;
        String account = null;
        Integer port = null;
        String pass = null;
        String local = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--ip":
                    if (i + 1 < args.length) ip = args[++i];
                    break;
                case "--account":
                    if (i + 1 < args.length) account = args[++i];
                    break;
                case "--port":
                    if (i + 1 < args.length) port = Integer.parseInt(args[++i]);
                    break;
                case "--pass":
                    if (i + 1 < args.length) pass = args[++i];
                    break;
                case "--local":
                    if (i + 1 < args.length) local = args[++i];
                    break;
                default:
                    System.out.println("Unknown argument: " + args[i]);
            }
        }

        // 输出参数值（不包括密码）
        System.out.println("IP: " + ip);
        System.out.println("Account: " + account);
        System.out.println("Port: " + port);
        System.out.println("Local: " + local);

        // 创建 FTP 管理器并连接
        FtpManager ftpManager = new FtpManager(ip, port, account, pass);
        if (ftpManager.connect()) {
            System.out.println("FTP connection established.");

            // 创建 AutoEngine 实例
            AutoEngine autoEngine = new AutoEngine(ftpManager, local);

            // 监听用户输入
            Scanner scanner = new Scanner(System.in);
            System.out.println("Type 'stop' to exit the program.");
            System.out.println("Type 'fetch' to retrieve file information.");
            System.out.println("Type 'local' to list local files.");
            System.out.println("Type 'sync' to synchronize files.");
            System.out.println("Type 'auto start <interval>' to start auto sync.");
            System.out.println("Type 'auto stop' to stop auto sync.");

            while (true) {
                String input = scanner.nextLine();
                if ("stop".equalsIgnoreCase(input)) {
                    break;
                } else if ("fetch".equalsIgnoreCase(input)) {
                    // 获取 FTP 文件信息
                    List<FileInfo> ftpFiles = ftpManager.fetchFileInfo();
                    System.out.println("FTP File Information:");
                    ftpFiles.forEach(System.out::println);
                } else if ("local".equalsIgnoreCase(input)) {
                    // 获取本地文件信息
                    if (local != null) {
                        List<FileInfo> localFiles = ftpManager.getLocalFileInfo(local);
                        System.out.println("Local File Information:");
                        localFiles.forEach(System.out::println);
                    } else {
                        System.out.println("Local directory not specified.");
                    }
                } else if ("sync".equalsIgnoreCase(input)) {
                    // 同步本地和 FTP 文件
                    if (local != null) {
                        autoEngine.syncFiles(); // 调用 AutoEngine 的 syncFiles 方法
                    } else {
                        System.out.println("Local directory not specified.");
                    }
                } else if (input.toLowerCase().startsWith("auto start")) {
                    // 解析同步间隔并启动自动同步
                    String[] parts = input.split("\\s+");
                    if (parts.length == 3) {
                        try {
                            long interval = Long.parseLong(parts[2]);
                            autoEngine.startAutoSync(interval); // 调用 AutoEngine 的 startAutoSync 方法
                            System.out.println("Auto sync started with interval: " + interval + " seconds");
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid interval. Please provide a valid number.");
                        }
                    } else {
                        System.out.println("Usage: auto start <interval>");
                    }
                } else if ("auto stop".equalsIgnoreCase(input)) {
                    // 停止自动同步
                    autoEngine.stopAutoSync(); // 调用 AutoEngine 的 stopAutoSync 方法
                    System.out.println("Auto sync stopped.");
                } else {
                    System.out.println("Unknown command: " + input);
                }
            }

            // 关闭 FTP 连接
            ftpManager.disconnect();
            System.out.println("FTP connection closed.");
        } else {
            System.out.println("Failed to establish FTP connection.");
        }

        // 关闭程序
        System.out.println("Program exited.");
    }
}