package cn.org.agatha;

/**
 * 文件信息类，用于存储文件路径和时间戳
 */
public class FileInfo {
    private String path;
    private long timestamp;

    /**
     * 构造函数
     *
     * @param path     文件路径
     * @param timestamp 时间戳（秒）
     */
    public FileInfo(String path, long timestamp) {
        this.path = path;
        this.timestamp = timestamp;
    }

    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 获取时间戳
     *
     * @return 时间戳（秒）
     */
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "FileInfo{path='" + path + "', timestamp=" + timestamp + "}";
    }
}