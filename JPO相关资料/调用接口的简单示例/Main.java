public class Main {
    public static void main(String[] args) {
        try {
            String apiUrl = "https://api.example.com/endpoint";
            String jsonInput = "{\"key\":\"value\"}";

            // 创建 ApiClient 实例
            ApiClient client = new ApiClient(apiUrl);

            // 设置要发送的 JSON 数据
            client.setJsonData(jsonInput);

            // 建立连接
            client.connect();

            // 发送 POST 请求并获取响应
            String response = client.post();
            System.out.println("Response: " + response);

            // 关闭连接
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
