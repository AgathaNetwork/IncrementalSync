package cn.org.agatha;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;

public class HttpCallback {

    /**
     * 发送 HTTP POST 请求到指定的 URL
     *
     * @param url    目标 URL
     * @param ts     时间戳
     * @param commit 文件路径组成的字符串，用 | 分隔
     * @return HTTP 响应内容
     */
    public static String sendPost(String url, long ts, String commit) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // 设置请求方法为 POST
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 构建请求参数，使用 URL 编码
            String urlParameters = "ts=" + ts + "&commit=" + URLEncoder.encode(commit, StandardCharsets.UTF_8.toString());

            // 发送 POST 请求
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = urlParameters.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应
            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // 读取响应内容
            try (java.util.Scanner scanner = new java.util.Scanner(con.getInputStream(), StandardCharsets.UTF_8.name())) {
                String responseBody = scanner.useDelimiter("\\A").next();
                return responseBody;
            }
        } catch (Exception e) {
            System.out.println("Error sending HTTP POST request: " + e.getMessage());
            return null;
        }
    }
}