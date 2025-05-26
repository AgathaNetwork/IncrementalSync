package cn.org.agatha;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class AutoEngine {
    private final FtpManager ftpManager;
    private final String localDir;
    private final String incrementsDir;
    private final String callbackUrl; // 新增：回调地址
    private AtomicBoolean autoSyncRunning = new AtomicBoolean(false);
    private long autoSyncInterval = 0;

    public AutoEngine(FtpManager ftpManager, String localDir, String incrementsDir, String callbackUrl) {
        this.ftpManager = ftpManager;
        this.localDir = localDir;
        this.incrementsDir = incrementsDir;
        this.callbackUrl = callbackUrl; // 初始化回调地址
    }

    /**
     * 同步本地和 FTP 文件
     */
    public void syncFiles() {
        List<FileInfo> localFiles = ftpManager.getLocalFileInfo(localDir);
        List<FileInfo> ftpFiles = ftpManager.fetchFileInfo();

        // 构建本地文件路径和时间戳的映射
        Map<String, Long> localFileMap = new HashMap<>();
        for (FileInfo fileInfo : localFiles) {
            localFileMap.put(fileInfo.getPath(), fileInfo.getTimestamp());
        }

        // 遍历 FTP 文件，筛选需要同步的文件
        List<String> syncedFiles = new ArrayList<>(); // 新增：记录已同步的文件
        for (FileInfo fileInfo : ftpFiles) {
            String remotePath = fileInfo.getPath();
            long remoteTimestamp = fileInfo.getTimestamp();

            // 检查本地是否存在对应的文件
            Long localTimestamp = localFileMap.get(remotePath);

            // 如果本地文件不存在或修改时间较早，则下载
            if (localTimestamp == null || localTimestamp < remoteTimestamp) {
                System.out.println("Syncing file: " + remotePath);
                String localDownloadPath = localDir.endsWith("/") 
                    ? localDir + remotePath.substring(1) 
                    : localDir + remotePath;
                if (ftpManager.downloadFile(remotePath, localDownloadPath)) {
                    syncedFiles.add(localDownloadPath); // 记录已同步的文件
                }
            }
        }

        // 如果有文件被同步，则压缩为 ZIP 文件
        if (!syncedFiles.isEmpty()) {
            try {
                compressToZip(syncedFiles); // 调用压缩方法
            } catch (IOException e) {
                System.out.println("Error compressing files to ZIP: " + e.getMessage());
            }
        }
    }

    /**
     * 将同步的文件压缩为 ZIP 文件
     *
     * @param files 要压缩的文件列表
     * @throws IOException 压缩过程中发生异常
     */
    private void compressToZip(List<String> files) throws IOException {
        // 创建 ZIP 文件名，使用时间戳命名
        String zipFileName = incrementsDir + File.separator + new Date().getTime() + ".zip";
        File zipFile = new File(zipFileName);
        if (!zipFile.getParentFile().exists()) {
            zipFile.getParentFile().mkdirs();
        }

        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (String filePath : files) {
                File fileToZip = new File(filePath);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
        }
        System.out.println("Files compressed to ZIP: " + zipFileName);

        // 执行 HTTP 回调
        long ts = System.currentTimeMillis() / 1000;
        String commit = String.join("|", files);
        String response = HttpCallback.sendPost(callbackUrl, ts, commit); // 使用传入的回调地址
        if (response != null) {
            System.out.println("HTTP Callback Response: " + response);
        } else {
            System.out.println("HTTP Callback failed.");
        }
    }

    /**
     * 启动自动同步
     *
     * @param interval 同步间隔（秒）
     */
    public void startAutoSync(long interval) {
        if (autoSyncRunning.get()) {
            System.out.println("Auto sync is already running.");
            return;
        }

        autoSyncInterval = interval * 1000; // 转换为毫秒
        autoSyncRunning.set(true);
        System.out.println("Auto sync started with interval: " + interval + " seconds");

        Thread autoSyncThread = new Thread(() -> {
            while (autoSyncRunning.get()) {
                try {
                    syncFiles(); // 调用 AutoEngine 的 syncFiles 方法
                    Thread.sleep(autoSyncInterval);
                } catch (InterruptedException e) {
                    System.out.println("Auto sync thread interrupted.");
                }
            }
        });
        autoSyncThread.start();
    }

    /**
     * 停止自动同步
     */
    public void stopAutoSync() {
        if (!autoSyncRunning.get()) {
            System.out.println("Auto sync is not running.");
            return;
        }

        autoSyncRunning.set(false);
        System.out.println("Auto sync stopped.");
    }
}