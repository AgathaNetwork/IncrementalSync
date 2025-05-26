package cn.org.agatha;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    if (i + 1 < args.length) local  = args[++i];
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

            // 监听用户输入
            Scanner scanner = new Scanner(System.in);
            System.out.println("Type 'stop' to exit the program.");
            System.out.println("Type 'fetch' to retrieve file information.");
            System.out.println("Type 'local' to list local files.");
            while (true) {
                String input = scanner.nextLine();
                if ("stop".equalsIgnoreCase(input)) {
                    break;
                } else if ("fetch".equalsIgnoreCase(input)) {
                    // 获取 FTP 文件信息
                    List<String> ftpFiles = ftpManager.fetchFileInfo();
                    System.out.println("FTP File Information:");
                    ftpFiles.forEach(System.out::println);
                } else if ("local".equalsIgnoreCase(input)) {
                    // 获取本地文件信息
                    if (local != null) {
                        List<String> localFiles = ftpManager.getLocalFileInfo(local);
                        System.out.println("Local File Information:");
                        localFiles.forEach(System.out::println);
                    } else {
                        System.out.println("Local directory not specified.");
                    }
                } else if ("sync".equalsIgnoreCase(input)) {
                    // 同步本地和 FTP 文件
                    if (local != null) {
                        List<String> localFiles = ftpManager.getLocalFileInfo(local);
                        List<String> ftpFiles = ftpManager.fetchFileInfo();

                        // 构建本地文件路径和时间戳的映射
                        Map<String, Long> localFileMap = new HashMap<>();
                        for (String fileInfo : localFiles) {
                            String[] parts = fileInfo.split(",");
                            String localPath = parts[0];
                            // 去掉本地路径的前缀，使其与远程路径格式一致
                            String normalizedLocalPath = localPath.replace(local, "").replace("\\", "/");
                            if (normalizedLocalPath.startsWith("/")) {
                                normalizedLocalPath = normalizedLocalPath.substring(1);
                            }
                            localFileMap.put(normalizedLocalPath, Long.parseLong(parts[1]));
                        }

                        // 遍历 FTP 文件，筛选需要同步的文件
                        for (String fileInfo : ftpFiles) {
                            String[] parts = fileInfo.split(",");
                            String remotePath = parts[0];
                            long remoteTimestamp = Long.parseLong(parts[1]);

                            // 检查本地是否存在对应的文件
                            Long localTimestamp = localFileMap.get(remotePath);

                            // 如果本地文件不存在或修改时间较早，则下载
                            if (localTimestamp == null || localTimestamp < remoteTimestamp) {
                                System.out.println("Syncing file: " + remotePath);
                                String localDownloadPath = local + remotePath;
                                ftpManager.downloadFile(remotePath, localDownloadPath);
                            }
                        }
                    } else {
                        System.out.println("Local directory not specified.");
                    }
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