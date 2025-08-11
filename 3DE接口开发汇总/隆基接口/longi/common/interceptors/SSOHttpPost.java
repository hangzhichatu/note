package longi.common.interceptors;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import longi.common.util.LONGiPropertiesUtil;

import com.alibaba.fastjson.JSON;


import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSOHttpPost {
    /**
     * 定义所需的变量
     */
	private final static String client_id = LONGiPropertiesUtil.readInterfaceProperties("CODE_SYS_SSO_ID");
	private final static String client_secret = LONGiPropertiesUtil.readInterfaceProperties("CODE_SYS_SSO_SECRET");
	private final static String client_token = LONGiPropertiesUtil.readInterfaceProperties("SSO_TOKEN_URL");
	private final static String client_UID = LONGiPropertiesUtil.readInterfaceProperties("SSO_UID_URL");
	
	
	private final static Logger logger = LoggerFactory.getLogger(SSOHttpPost.class);
    public static String getTokenPost(String code, String redirectUri) {
       // String tokenURL = "https://esctest.longi.com/esc-sso/oauth2.0/accessToken?grant_type=authorization_code&oauth_timestamp="+new Date().getTime()+"&client_id=wlbmtest&client_secret=64482a80-b86d-4052-8524-6fe3a1e58d32&code="+code+"&redirect_uri="+redirectUri;
        String tokenURL = client_token + "&oauth_timestamp="+new Date().getTime()+"&client_id="+client_id+"&client_secret="+client_secret+"&code="+code+"&redirect_uri="+redirectUri;
        
        logger.info("请求token - tokenUrl："+tokenURL);
        System.out.println("请求token - tokenUrl："+tokenURL);
        String res = "";
		try {
			res = sendPost(tokenURL);
			System.out.println("请求token - 返回结果："+res);
			logger.info("请求token - 返回结果："+res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("请求token - 异常"+e.getMessage());
        	logger.info("请求token - 异常"+e.getMessage());
			e.printStackTrace();
		}
        return res;
    }
    
    //https://esctest.longi.com/escsso/oauth2.0/profile?access_token=b1d508f3-32e7-4d6d-ac62-

    public static String getUIDsGet(String accessToken){
        String uidUrl = client_UID + accessToken;
        logger.info("获取UID URL:"+uidUrl);
        String res = "";
        try {
            res = sendGet(uidUrl);
            logger.info("res:"+res);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return res;
    }
    private static String sendPost(String postUrl) throws Exception {
//    	Unirest.shutdown();
//    	System.out.println("设置超时时间：2s");
//        Unirest.setTimeouts(2000, 3000);
        System.out.println("开始请求");
        HttpResponse<String> response = Unirest.post(postUrl)
                .header("Cookie", "")
                .asString();
        System.out.println("请求结束："+response.getBody());
//        Unirest.shutdown();
        return response.getBody();
    }
    private static String  sendGet(String postUrl) throws Exception {
    	Unirest.setTimeouts(30000, 31000);
        HttpResponse<String> response = Unirest.get(postUrl)
                .header("Cookie", "")
                .asString();
        return response.getBody();
    }

    public static void main(String[] args) {
        String tokenJson = getTokenPost("OC-8994-nTw6q4d9iDBfnSx3nC3eVhvUzleCdAsZAp6","https://plmdev2.longi.com/internal");
        System.out.println("tokenJson="+tokenJson);
        Map tokenMap = (Map)JSON.parse(tokenJson);
        System.out.println(tokenMap);
        //{"access_token":"AT-126-jw2eZaSkFpRcKlQmX9jQxkspT96oDt5qj6e","token_type":"bearer","expires_in":86400,"refresh_token":"RT-113-dahxUSIjMreqGJVa6kbpORg6I7gN9gYD4bA"}
//        
        if(tokenMap.containsKey("access_token")){
            String access_token = tokenMap.get("access_token").toString();
            String uidJson = getUIDsGet(access_token);
            System.out.println(uidJson);
        }
    	
    	
        
    }
}