package longi.common.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** 
* @ClassName: YZFCSUtil
* @Description: TODO 永中fcs应用工具类
* @author: Longi.suwei
* @date: May 27, 2021 8:55:09 PM
*/
public class YZFCSUtil {
	private final static Logger logger = LoggerFactory.getLogger(YZFCSUtil.class);
	public static String doPost(String url, Map<String, Object> params) {
		CloseableHttpResponse response = null;
		String result = "";
		try {
			HttpPost httpPost = new HttpPost(url);
			// 判断map是否为空，不为空则进行遍历，封装from表单对象
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> list = new ArrayList<>();
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					if (entry.getValue() != null) {
						if (entry.getValue() instanceof ArrayList) {
							for (Object obj : ((ArrayList) entry.getValue())) {
								list.add(new BasicNameValuePair(entry.getKey(), obj.toString()));
							}
						} else {
							list.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
						}
					}
				}
				UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(list, "UTF-8");
				httpPost.setEntity(urlEncodedFormEntity);
			}
			CloseableHttpClient httpclient = HttpClients.createDefault();
			response = httpclient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity);
				logger.info("请求成功" + result);
			} else {
				logger.info("请求失败，状态码：" + response.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("postByJson请求失败,请求URL为:" + url);
			logger.error("postByJson请求失败,请求URL为:" + url);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
			}
		}
		return result;
	}

//    public static void main(String[] args) {
//    	YZFCSUtil util = new YZFCSUtil();
//    	try {
//			util.uploadAndComposite(0, "D:\\suwei3\\Desktop\\稳定就业证明.docx");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
	/**
	* @Author: Longi.suwei
	* @Title: uploadAndComposite 向指定 URL 上传文件POST方法的请求
	* @Description: TODO
	* @param: @param filepath 文件路径
	* @param: @param type 转换类型
	* @param: @return
	* @param: @throws IOException
	* @date: 2021 Jul 15 16:45:54
	*/
	public static String uploadAndComposite(int convertType, String filePath) throws IOException {
		String url = LONGiPropertiesUtil.readYZProperties("YZ_UploadANDComposite");
		File pdfFile = new File(filePath);
		FileInputStream fileInputStream = new FileInputStream(pdfFile);
		MultipartFile multipartFile = new MockMultipartFile(pdfFile.getName(), pdfFile.getName(),
				ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
		Map<String, Object> params = new HashMap<>();
		params.put("convertType", convertType);
		params.put("file", multipartFile);
		doPost(url, params);
		return "";
	}



	/**
	* @Author: Longi.suwei
	* @Title: uploadAndComposite 向指定 URL 上传文件POST方法的请求
	* @Description: TODO
	* @param: @param filepath 文件路径
	* @param: @param type 转换类型
	* @param: @param wmContent 签名
	* @param: @return 所代表远程资源的响应结果, json数据
	* @param: @throws Exception
	* @date: 2021 Jul 15 16:45:20
	*/
	public static String uploadAndComposite(String filepath, String type, String wmContent) throws Exception {
		logger.info("开始YZ上传和转换功能\n filepath:"+filepath+"\ntype:"+type+"\nwmContent"+wmContent);
//		String url = "https://yzfcs.longi.com:8443/fcscloud/composite/upload";
		String url = LONGiPropertiesUtil.readYZProperties("YZ_UploadANDComposite");
		String requestJson = "";
		HttpClient httpclient = new DefaultHttpClient();
		try {
			HttpPost httppost = new HttpPost(url);
			FileBody file = new FileBody(new File(filepath));
//			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null,
//					Charset.forName("UTF-8"));
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setCharset(Charset.forName("UTF-8"));
			builder.addPart("file", file); // file为请求后台的File upload;
			builder.addPart("convertType", new StringBody(type, ContentType.DEFAULT_TEXT));
			builder.addPart("num", new StringBody("1", ContentType.DEFAULT_TEXT));
			builder.addPart("showFooter", new StringBody("0", ContentType.DEFAULT_TEXT));
			builder.addPart("wmContent", new StringBody(wmContent, ContentType.DEFAULT_TEXT));
			builder.addPart("wmMargin", new StringBody("250", ContentType.DEFAULT_TEXT));
			builder.addPart("wmMargin", new StringBody("250", ContentType.DEFAULT_TEXT));
			builder.addPart("wmSize", new StringBody("20", ContentType.DEFAULT_BINARY));
			builder.addPart("wmTransparency", new StringBody("0.2", ContentType.DEFAULT_BINARY));
			builder.addPart("wmRotate", new StringBody("45", ContentType.DEFAULT_BINARY));
			HttpEntity entity = builder.build();
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity resEntity = response.getEntity();
				requestJson = EntityUtils.toString(resEntity);
				EntityUtils.consume(resEntity);
			}
			logger.info("YZ上传和转换成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("YZ转换失败"+e.toString());
			//e.printStackTrace();
			throw new Exception(e);
			// requestJson = e.toString();
		} finally {
			try {
				if(httpclient != null){
					httpclient.getConnectionManager().shutdown();
				}
			} catch (Exception ignore) {
				logger.error("httpclient.getConnectionManager().shutdown() 失败");
				ignore.printStackTrace();

			}
		}

		return requestJson;
	}

	/**
	 * @Author: Longi.suwei
	 * @Title: uploadAndComposite 向指定 URL 上传文件POST方法的请求
	 * @Description: TODO
	 * @param: @param filepath 文件路径
	 * @param: @param type 转换类型
	 * @param: @param wmContent 签名
	 * @param: @return 所代表远程资源的响应结果, json数据
	 * @param: @throws Exception
	 * @date: 2021 Jul 15 16:45:20
	 */
	public static String compositeConvert(String filepath, String type, String wmContent) throws Exception {
		logger.info("开始YZ上传和转换功能\n filepath:"+filepath+"\ntype:"+type+"\nwmContent"+wmContent);
//		String url = "https://yzfcs.longi.com:8443/fcscloud/composite/convert";
		String url = LONGiPropertiesUtil.readYZProperties("YZ_CompositeConvert");
		System.out.println("url="+url);
		String requestJson = "";
		HttpClient httpclient = new DefaultHttpClient();
		try {
			HttpPost httppost = new HttpPost(url);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setCharset(Charset.forName("UTF-8"));
			builder.addPart("srcRelativePath", new StringBody(filepath,Charset.forName("UTF-8"))); // file为请求后台的File upload;
			builder.addPart("convertType", new StringBody(type, ContentType.DEFAULT_TEXT));
			builder.addPart("wmContent", new StringBody(wmContent, ContentType.DEFAULT_TEXT));
			builder.addPart("num", new StringBody("1", ContentType.DEFAULT_TEXT));
			builder.addPart("showFooter", new StringBody("0", ContentType.DEFAULT_TEXT));
			builder.addPart("wmMargin", new StringBody("250", ContentType.DEFAULT_TEXT));
			builder.addPart("wmMargin", new StringBody("250", ContentType.DEFAULT_TEXT));
			builder.addPart("wmSize", new StringBody("20", ContentType.DEFAULT_BINARY));
			builder.addPart("wmTransparency", new StringBody("0.2", ContentType.DEFAULT_BINARY));
			builder.addPart("wmRotate", new StringBody("45", ContentType.DEFAULT_BINARY));
			HttpEntity entity = builder.build();
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity resEntity = response.getEntity();
				requestJson = EntityUtils.toString(resEntity);
				EntityUtils.consume(resEntity);
			}
			logger.info("YZ服务器文件转换成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("YZ服务器文件转换失败"+e.toString());
			//e.printStackTrace();
			throw new Exception(e);
			// requestJson = e.toString();
		} finally {
			try {
				if(httpclient != null){
					httpclient.getConnectionManager().shutdown();
				}
			} catch (Exception ignore) {
				logger.error("httpclient.getConnectionManager().shutdown() 失败");
				ignore.printStackTrace();

			}
		}

		return requestJson;
	}
	public static void main(String[] args) {

		// TODO Auto-generated method stub

		// 文件上传转换

		try {
			//String convertByFile = uploadAndComposite("D:\\suwei3\\Desktop\\快速技术方案管理规范 V02_解.doc", "0","lihao30");
			String convertByFile2 = compositeConvert("f1d826852f2372dacef5d6f5a67c5e1d5/隆基股份-SSO平台使用培训.pdf", "14","plmcodeuser");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 网络地址转换

		// String convertByUrl = sendPost("http://dcs.yozosoft.com:80/onlinefile",
		// "downloadUrl=http://img.iyocloud.com:8000/doctest.docx&convertType=1");


		// System.out.println(convertByUrl);

	}

}