import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

public class ApiClient {

    private String url;
    private String jsonData;
    private HttpURLConnection connection;

    // 构造函数，设置接口URL
    public ApiClient(String url) {
        this.url = url;
    }

    // 设置要发送的JSON数据
    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    // 建立连接
    public void connect() throws IOException {
        // 创建URL对象
        URL urlObj = new URL(url);
        // 打开连接
        connection = (HttpURLConnection) urlObj.openConnection();

        // 设置请求方法为POST
        connection.setRequestMethod("POST");

        // 设置请求头
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        // 启用输入输出流
        connection.setDoOutput(true);
    }

    // 发送POST请求并返回响应
    public String post() throws IOException {
        if (connection == null) {
            throw new IllegalStateException("Connection has not been established. Call connect() first.");
        }

        // 写入JSON数据到请求体
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 获取响应状态码
        int statusCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                statusCode < HttpURLConnection.HTTP_BAD_REQUEST ? connection.getInputStream() : connection.getErrorStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return response.toString();
    }

    // 关闭连接
    public void disconnect() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}
