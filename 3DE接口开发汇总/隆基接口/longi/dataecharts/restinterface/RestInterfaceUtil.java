package longi.dataecharts.restinterface;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RestInterfaceUtil {

    public static String doPost(String url, Map<String, String> params, String username, String password) {
        HttpClient httpClient;
        HttpPost httpPost;
        String result = null;
        try {
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(JSON.toJSONString(params));
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            String value2 = "Basic " + Base64.getUrlEncoder().encodeToString((username + ":" + password).getBytes());
            //设置头
            httpPost.addHeader("Authorization", value2);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, "UTF-8");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(doPost("https://10.0.88.100/internal/pdmservice/module/bd/select", new HashMap<String, String>() {{
            put("Menu", "ProductCode");
        }}, "bpm_basic", "bpm_basic"));
    }
}
