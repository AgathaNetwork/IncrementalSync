package cn.org.agatha;

import java.net.URL;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String ip = null;
        String account = null;
        Integer port = null;
        String pass = null;
        String local = null;
        String increments = null;
        String callbackUrl = null; // 新增：回调地址

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
                case "--increments":
                    if (i + 1 < args.length) increments = args[++i];
                    break;
                case "--callback": // 新增：回调地址参数
                    if (i + 1 < args.length) {
                        String url = args[++i];
                        try {
                            new URL(url); // 校验 URL 格式
                            callbackUrl = url;
                        } catch (Exception e) {
                            System.out.println("Invalid callback URL: " + url);
                        }
                    }
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
        System.out.println("Increments: " + increments);
        System.out.println("Callback URL: " + callbackUrl); // 输出回调地址

        // 创建 FTP 管理器并连接
        FtpManager ftpManager = new FtpManager(ip, port, account, pass);
        if (ftpManager.connect()) {
            System.out.println("FTP connection established.");

            // 创建 AutoEngine 实例，传入回调地址
            AutoEngine autoEngine = new AutoEngine(ftpManager, local, increments, callbackUrl);

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