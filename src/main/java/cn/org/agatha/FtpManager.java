package cn.org.agatha;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

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
}