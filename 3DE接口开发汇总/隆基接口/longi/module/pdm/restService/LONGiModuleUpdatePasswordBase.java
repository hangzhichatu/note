package longi.module.pdm.restService;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import sun.misc.BASE64Decoder;

import com.dassault_systemes.platform.restServices.RestService;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/** 
* @ClassName: LONGiModuleUpdatePasswordBase
* @Description: TODO IDM-ESB更新密码接口实现类
* @author: Longi.suwei
* @date: Apr 26, 2021 7:33:08 PM
*/
@Path("/updatePassword")
public class LONGiModuleUpdatePasswordBase extends RestService {
	private final static Logger logger = LoggerFactory.getLogger("module_interface");
	/*
	@GET
	public Response getUpdate(@Context HttpServletRequest request, @QueryParam("username") String userName,
			@QueryParam("password") String password) throws Exception {
		return updatePwd(request, userName, password);
	}
	*/
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response postUpdate(@Context HttpServletRequest request, @RequestBody String requestJson) throws Exception {
		return updatePwd(request, requestJson);
	}

	//@SuppressWarnings("unchecked")
	@SuppressWarnings("unchecked")
	public Response updatePwd(HttpServletRequest request, String requestJson) throws Exception {
		logger.info("收到IDM-ESB更新人员密码接口请求：" + requestJson);
		// Test sample :
		// https://3dexp.16xfd04.ds/3DSpace/UM5Tools/Find?type=Task*&selects=name,revision,current&where=name%20~=%20Ph*

		JsonObjectBuilder output = Json.createObjectBuilder();
		JsonObjectBuilder esbInfo = Json.createObjectBuilder();
		JsonObjectBuilder resultInfo = Json.createObjectBuilder();

		matrix.db.Context context = null;

		String instId = "";
		String requestTime = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

		String DES = "DES";
		String ENCODE = "GBK";
		String defaultKey = "hN/xQHkXJTg1QO844/Mur1nbuUKzhIDzQqI1imVJR98=";

		try {
			Properties property = new Properties();
			String path = this.getClass().getResource("/").getPath();
			File propertyFile = new File(path+"WebserviceParam.properties");
			FileInputStream fis = new FileInputStream(propertyFile);
			property.load(fis);

			defaultKey = property.getProperty("defaultKey");
			String internalURL = property.getProperty("internalURL");
			String internalUser = property.getProperty("internalUser");
			String internalPwd = property.getProperty("internalPwd");
			String passportURL = property.getProperty("passportURL");

			fis.close();

			// context = authenticate(request); //Deprecated in 17x
			//boolean isSCMandatory = false;
			//context = getAuthenticatedContext(request, isSCMandatory); // New way to get context in 17x
			//Connect With 3DE
			context = new matrix.db.Context(internalURL);
			context.setUser(internalUser);
			context.setPassword(internalPwd);
			context.connect();

			ContextUtil.startTransaction(context, false);

			//Parse JSON String Get Info
			JSONObject input = JSONObject.fromObject(requestJson);
			JSONObject inputESBInfo = input.getJSONObject("esbInfo");
			JSONObject inputRequestInfo = input.getJSONObject("requestInfo");

			instId = inputESBInfo.getString("instId");
			requestTime = inputESBInfo.getString("requestTime");

			String personId = inputRequestInfo.getString("EMPLID");
			String pwd = inputRequestInfo.getString("NewPassword");

			
			String userName = "";
			//check if person exist
			String personObjectId = MqlUtil.mqlCommand(context, "temp query bus Person * * where \"attribute[LONGiModulePersonId]=='"+personId+"'\" select name dump");
			if(UIUtil.isNotNullAndNotEmpty(personObjectId)){
				userName = personObjectId.substring(personObjectId.lastIndexOf(",")+1, personObjectId.length());

				//Decrypt pwd by default key
				BASE64Decoder decoder = new BASE64Decoder();
				byte[] buf = decoder.decodeBuffer(pwd);
				byte[] key = defaultKey.getBytes(ENCODE);

				SecureRandom sr = new SecureRandom();


				DESKeySpec dks = new DESKeySpec(key);


				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
				SecretKey securekey = keyFactory.generateSecret(dks);
				Cipher cipher = Cipher.getInstance(DES);

				cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
				byte[] bt = cipher.doFinal(buf);
				pwd = new String(bt, ENCODE);


				//Update Password

				URL localURL = new URL(new StringBuilder().append(passportURL).append("/api/public/skey").toString());
				HttpURLConnection localHttpURLConnection = (HttpURLConnection)localURL.openConnection();
				localHttpURLConnection.setDoOutput(true);
				localHttpURLConnection.setRequestMethod("POST");
				localHttpURLConnection.addRequestProperty("Content-Type", "text/plain;charset=UTF-8");
				localHttpURLConnection.addRequestProperty("Accept", "*/*");
				OutputStreamWriter localOutputStreamWriter = new OutputStreamWriter(localHttpURLConnection.getOutputStream());
				localOutputStreamWriter.write("admin_platform");
				localOutputStreamWriter.flush();
				BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localHttpURLConnection.getInputStream()));
				String str1 = localBufferedReader.readLine();
				str1 = str1.replace("\"", "");
				System.out.println("str1-------->"+str1);



				//Connect 3DPassport Oracle TO Get email lastName firstName
				Class.forName("oracle.jdbc.driver.OracleDriver");

				String url = property.getProperty("oracleConnURL");

				String username = property.getProperty("oracleUser");

				String password = property.getProperty("oraclePwd");

				Connection conn = DriverManager.getConnection(url,username,password);

				Statement statement = conn.createStatement();


				String firstName = "";
				String lastName = "";
				String email = "";

				String sql = "select ds_firstname,ds_lastname,ds_email from USERS where ds_username='"+userName+"'";


				ResultSet rs = ((java.sql.Statement) statement).executeQuery(sql);


				while(rs.next()) {
					firstName = rs.getString(1);
					lastName = rs.getString(2);
					email = rs.getString(3);
					logger.info("IDM-ESB更新人员密码接口请求，firstName-lastName-email：" + firstName + lastName + email);
				}

				//String jsonStr = "{\"active\":true,\"lastSynchronized\":0,\"sourceRepositories\":[],\"fields\":{"+"\"email\":\"test1@longigroup.com\","+"\"lastName\":\"hahahha\","+"\"firstName\":\"\u6D4B\","+"\"country\":\"CN\","+"\"username\":\"test1\""+",\"password\":\"LONGi123456\""+"},\"acls\":{\"3dexperience\":\"ACCEPTED\",\"passport\":\"ACCEPTED\"},\"tenant\":null}";
				//URI localURI = new URI(new StringBuilder().append("https://dsplm.jonhon.com/3dpassport").append("/api/private/tenant1/user/register").toString());

				String jsonStr = "{\"active\":true,\"lastSynchronized\":0,\"sourceRepositories\":[],\"fields\":{"+"\"email\":\""+email+"\","+"\"lastName\":\""+lastName+"\","+"\"firstName\":\""+firstName+"\","+"\"country\":\"CN\","+"\"username\":\""+userName+"\""+",\"password\":\""+pwd+"\""+"},\"acls\":{\"3dexperience\":\"ACCEPTED\",\"passport\":\"ACCEPTED\"},\"tenant\":null}";

				URI localURI = new URI(new StringBuilder().append(passportURL).append("/api/private/user/update").toString());
				//URI localURI = new URI(new StringBuilder().append("https://plmdev.longi-silicon.com/3dpassport").append("/api/private/user/update").toString());
				Object localObject = localURI.toURL();
				HttpURLConnection localHttpURLConnection1 = (HttpURLConnection)((URL)localObject).openConnection();
				localHttpURLConnection1.setDoOutput(true);
				localHttpURLConnection1.setRequestMethod("POST");
				localHttpURLConnection1.addRequestProperty("skey", str1);
				localHttpURLConnection1.addRequestProperty("Content-Type", "application/json;charset=UTF-8");
				localHttpURLConnection1.addRequestProperty("Accept", "application/json");
				OutputStreamWriter localOutputStreamWriter1 = new OutputStreamWriter(localHttpURLConnection1.getOutputStream(), StandardCharsets.UTF_8);
				localOutputStreamWriter1.write(jsonStr);
				localOutputStreamWriter1.flush();
				BufferedReader localBufferedReader1 = new BufferedReader(new InputStreamReader(localHttpURLConnection1.getInputStream()));
				for (String str2 = localBufferedReader1.readLine(); str2 != null; str2 = localBufferedReader1.readLine()){
					System.out.println(str2);
				}


				//Set Return Json
				esbInfo.add("instId", instId);
				esbInfo.add("returnStatus","S");
				esbInfo.add("returnCode","000");
				esbInfo.add("returnMsg","[PDM]\u4FEE\u6539\u6210\u529F");
				esbInfo.add("requestTime",requestTime);
				esbInfo.add("responseTime",sdf.format(new Date()));
				esbInfo.add("attr1","");
				esbInfo.add("attr2","");
				esbInfo.add("attr3","");

				resultInfo.add("returnStatus","S");
				resultInfo.add("returnCode","000");
				resultInfo.add("returnMsg","[PDM]\u4FEE\u6539\u6210\u529F");

				output.add("esbInfo", esbInfo);
				output.add("resultInfo", resultInfo);

			}else{
				esbInfo.add("instId", instId);
				esbInfo.add("returnStatus","E");
				esbInfo.add("returnCode","000");
				esbInfo.add("returnMsg","[PDM]\u65E0\u6B64\u8D26\u53F7");
				esbInfo.add("requestTime",requestTime);
				esbInfo.add("responseTime",sdf.format(new Date()));
				esbInfo.add("attr1","");
				esbInfo.add("attr2","");
				esbInfo.add("attr3","");

				resultInfo.add("returnStatus","E");
				resultInfo.add("returnCode","000");
				resultInfo.add("returnMsg","[PDM]\u65E0\u6B64\u8D26\u53F7");

				output.add("esbInfo", esbInfo);
				output.add("resultInfo", resultInfo);
				logger.info("IDM-ESB更新人员密码接口请求，查询该人员不存在");
			}


			ContextUtil.commitTransaction(context);

		} catch (Exception e) {
			try {
				if (ContextUtil.isTransactionActive(context)) {
					ContextUtil.abortTransaction(context);
				}
			} catch (FrameworkException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			logger.info("IDM-ESB更新人员密码接口请求，执行程序异常："+e.toString());
			esbInfo.add("instId", instId);
			esbInfo.add("returnStatus","E");
			esbInfo.add("returnCode","000");
			esbInfo.add("returnMsg","[PDM]\u4FEE\u6539\u5931\u8D25");
			esbInfo.add("requestTime",requestTime);
			esbInfo.add("responseTime",sdf.format(new Date()));
			esbInfo.add("attr1","");
			esbInfo.add("attr2","");
			esbInfo.add("attr3","");

			resultInfo.add("returnStatus","E");
			resultInfo.add("returnCode","000");
			resultInfo.add("returnMsg","[PDM]"+e.toString());

			output.add("esbInfo", esbInfo);
			output.add("resultInfo", resultInfo);

			
		}
		logger.info("IDM-ESB更新人员密码接口成功");
		return Response.status(HttpServletResponse.SC_OK).entity(output.build().toString()).build();

	}
}
