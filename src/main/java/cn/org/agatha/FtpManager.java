package cn.org.agatha;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FtpManager {
    private FTPClient ftpClient;
    private String server;
    private int port;
    private String username;
    private String password;

    /**
     * 构造函数，初始化 FTP 服务器信息
     *
     * @param server   FTP 服务器地址
     * @param port     FTP 服务器端口
     * @param username FTP 用户名
     * @param password FTP 密码
     */
    public FtpManager(String server, int port, String username, String password) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.ftpClient = new FTPClient();
    }

    /**
     * 建立 FTP 连接
     *
     * @return 是否成功连接
     */
    public boolean connect() {
        try {
            ftpClient.connect(server, port);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                System.out.println("FTP server refused connection.");
                return false;
            }
            boolean loginSuccess = ftpClient.login(username, password);
            if (!loginSuccess) {
                System.out.println("FTP login failed.");
                return false;
            }
            // 设置被动模式
            ftpClient.enterLocalPassiveMode();
            // 设置文件类型为二进制
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            System.out.println("Connected to FTP server successfully.");
            return true;
        } catch (Exception e) {
            System.out.println("Error connecting to FTP server: " + e.getMessage());
            return false;
        }
    }

    /**
     * 执行 FTP 命令
     *
     * @param command FTP 命令
     * @return 是否执行成功
     */
    public boolean executeCommand(String command) {
        try {
            if (!isConnected()) {
                System.out.println("Not connected to FTP server.");
                return false;
            }
            boolean success = ftpClient.sendSiteCommand(command);
            if (!success) {
                System.out.println("Failed to execute command: " + command);
                return false;
            }
            System.out.println("Command executed successfully: " + command);
            return true;
        } catch (Exception e) {
            System.out.println("Error executing command: " + e.getMessage());
            return false;
        }
    }

    /**
     * 断开 FTP 连接
     */
    public void disconnect() {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
                System.out.println("Disconnected from FTP server.");
            }
        } catch (Exception e) {
            System.out.println("Error disconnecting from FTP server: " + e.getMessage());
        }
    }

    /**
     * 查询当前是否已连接到 FTP 服务器
     *
     * @return 是否已连接
     */
    public boolean isConnected() {
        return ftpClient != null && ftpClient.isConnected();
    }

    /**
     * 递归列出目录下的所有文件
     *
     * @param directory 当前目录
     * @param fileList  文件信息列表
     * @param baseDir   基础目录路径
     */
    private void listFilesRecursively(File directory, List<FileInfo> fileList, String baseDir) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    listFilesRecursively(file, fileList, baseDir);
                } else {
                    // 构建 FileInfo 对象并添加到列表
                    String relativePath = file.getAbsolutePath().replace(baseDir, "").replace("\\", "/");
                    if (!relativePath.startsWith("/")) {
                        relativePath = "/" + relativePath; // 确保路径以 / 开头
                    }
                    fileList.add(new FileInfo(relativePath, file.lastModified() / 1000));
                }
            }
        }
    }

    /**
     * 遍历 FTP 服务器上的所有目录并获取文件信息
     *
     * @return 文件信息列表
     */
    public List<FileInfo> fetchFileInfo() {
        if (!isConnected()) {
            System.out.println("Not connected to FTP server.");
            return new ArrayList<>();
        }

        List<FileInfo> fileList = new ArrayList<>();
        try {
            List<String> directories = new ArrayList<>();
            directories.add("/");

            while (!directories.isEmpty()) {
                String currentDir = directories.remove(0);
                FTPFile[] files = ftpClient.listFiles(currentDir);

                for (FTPFile file : files) {
                    if (file.isDirectory()) {
                        directories.add(currentDir + file.getName() + "/");
                    } else {
                        // 构建 FileInfo 对象并添加到列表
                        String filePath = currentDir + file.getName();
                        if (!filePath.startsWith("/")) {
                            filePath = "/" + filePath; // 确保路径以 / 开头
                        }
                        fileList.add(new FileInfo(filePath, file.getTimestamp().getTimeInMillis() / 1000));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching file information: " + e.getMessage());
        }

        return fileList;
    }

    /**
     * 遍历本地指定目录下的文件
     *
     * @param localDir 本地目录路径
     * @return 文件信息列表
     */
    public List<FileInfo> getLocalFileInfo(String localDir) {
        File directory = new File(localDir);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory: " + localDir);
            return new ArrayList<>();
        }

        List<FileInfo> fileList = new ArrayList<>();
        listFilesRecursively(directory, fileList, localDir);

        return fileList;
    }

    /**
     * 下载 FTP 文件到本地
     *
     * @param remotePath FTP 文件路径
     * @param localPath  本地保存路径
     * @return 是否下载成功
     */
    public boolean downloadFile(String remotePath, String localPath) {
        if (!isConnected()) {
            System.out.println("Not connected to FTP server.");
            return false;
        }

        try {
            File localFile = new File(localPath);
            if (!localFile.getParentFile().exists()) {
                localFile.getParentFile().mkdirs();
            }

            // 使用 try-with-resources 确保 FileOutputStream 在下载完成后被正确关闭
            try (java.io.FileOutputStream outputStream = new java.io.FileOutputStream(localFile)) {
                boolean success = ftpClient.retrieveFile(remotePath, outputStream);
                if (!success) {
                    System.out.println("Failed to download file: " + remotePath);
                    return false;
                }
                System.out.println("File downloaded successfully: " + remotePath);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error downloading file: " + e.getMessage());
            return false;
        }
    }
}