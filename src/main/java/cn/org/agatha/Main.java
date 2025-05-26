package cn.org.agatha;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String ip = null;
        String account = null;
        Integer port = null;
        String pass = null;

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
                default:
                    System.out.println("Unknown argument: " + args[i]);
            }
        }

        // 输出参数值（不包括密码）
        System.out.println("IP: " + ip);
        System.out.println("Account: " + account);
        System.out.println("Port: " + port);

        // 创建 FTP 管理器并连接
        FtpManager ftpManager = new FtpManager(ip, port, account, pass);
        if (ftpManager.connect()) {
            System.out.println("FTP connection established.");

            // 监听用户输入
            Scanner scanner = new Scanner(System.in);
            System.out.println("Type 'stop' to exit the program.");
            while (true) {
                String input = scanner.nextLine();
                if ("stop".equalsIgnoreCase(input)) {
                    break;
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