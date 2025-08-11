package longi.module.pdm.restService;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

import matrix.db.BusinessObject;
import matrix.util.LicenseUtil;
import matrix.util.StringList;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import com.dassault_systemes.platform.restServices.RestService;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MqlUtil;

import longi.module.pdm.util.LONGiModuleJacksonUtil;

/** 
* @ClassName: LONGiModuleCreateAccountBase
* @Description: TODO IDM-ESB新建用户接口
* @author: modify by Longi.suwei
* @date: Apr 26, 2021 7:20:52 PM
*/
@Path("/createAccount")
public class LONGiModuleCreateAccountBase extends RestService {
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
	public Response postCreateAccount(@Context HttpServletRequest request, @RequestBody String requestJson) throws Exception {
		return createAccount(request, requestJson);
	}

	//@SuppressWarnings("unchecked")
	@SuppressWarnings("unchecked")
	public Response createAccount(HttpServletRequest request, String requestJson) throws Exception {

		// Test sample :
		// https://3dexp.16xfd04.ds/3DSpace/UM5Tools/Find?type=Task*&selects=name,revision,current&where=name%20~=%20Ph*

		logger.info("接收到ESB新建用户，请求报文："+requestJson);
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
			String importSHPath = property.getProperty("importSHPath");
			
			fis.close();

			context = new matrix.db.Context(internalURL);
			context.setUser(internalUser);
			context.setPassword(internalPwd);
			context.connect();

			
			ContextUtil.startTransaction(context, true);
			
			//Parse JSON String Get Info
			JSONObject input = JSONObject.fromObject(requestJson);
			JSONObject inputESBInfo = input.getJSONObject("esbInfo");
			JSONObject inputRequestInfo = input.getJSONObject("requestInfo");
			
			instId = inputESBInfo.getString("instId");
			requestTime = inputESBInfo.getString("requestTime");
			
			JSONObject inputUsers = inputRequestInfo.getJSONObject("Users");
			
			String sysCode = inputUsers.getString("SysCode");
			//if("xaplm".equals(sysCode)){
				JSONArray inputUserArray = inputUsers.getJSONArray("User");
				for(int i=0;i<inputUserArray.size();i++){
					JSONObject inputUserInfo = inputUserArray.getJSONObject(i);
					String userName = inputUserInfo.getString("username");
					logger.info(userName+" IDM-ESB新建用户，处理用户数据："+LONGiModuleJacksonUtil.beanToJson(inputUserInfo));
					String firstName = inputUserInfo.getString("realname");
					String email = inputUserInfo.getString("email");
					String operation = inputUserInfo.getString("operator");
					String personId = inputUserInfo.getString("id");
					if("D".equals(operation) || "S".equals(operation)){
						DomainObject personObj = new DomainObject(new BusinessObject("Person",userName,"-","eService Production"));
						if(personObj.exists(context) && "Active".equals(personObj.getInfo(context, "current"))){
							StringList objectSelects= new StringList();
		            		objectSelects.addElement(DomainConstants.SELECT_NAME);
		            		objectSelects.addElement("attribute["+DomainObject.ATTRIBUTE_LICENSED_HOURS+"]");
		            		Map personInfo = personObj.getInfo(context, objectSelects);
		            		String strUserName = (String)personInfo.get(DomainConstants.SELECT_NAME);
		            		String sLicensedHours = (String)personInfo.get("attribute["+DomainObject.ATTRIBUTE_LICENSED_HOURS+"]");
		            		boolean isCasualUser = !"0".equals(sLicensedHours);
		                    
		                    String[] licenseList;
		                    String[] licenseListToRelease;
		                    try {
		                        ContextUtil.pushContext(context);
		                        ContextUtil.pushContext(context, strUserName, "", null);
		                       	if(isCasualUser){
		                       		Map<String, Integer> mapLicenses = LicenseUtil.getCasualLicenses(context, false);
		                            licenseList = (String[]) mapLicenses.keySet().toArray(new String[]{});	
		                       	}else{
		                        licenseList = LicenseUtil.getLicenses(context, false);//exportting all the web application licenses for the current user
		                       	}
		                    }
		                    catch (Exception e){
		                        throw new Exception (e);
		                    }
		                    finally {
		                        ContextUtil.popContext(context);
		                        ContextUtil.popContext(context);
		                    }
		                    
		                    if (licenseList !=null && licenseList.length != 0){
	
		       						try
		       						{
			       						java.util.List info = LicenseUtil.getUserLicenseUsage(context,strUserName,null);
			       						licenseListToRelease = new String[info.size()];
			       						for( int k=0, len=info.size(); k<len; k++ ) {
			       	                   	 HashMap rowmap = (HashMap)info.get(k);
			       	                   	 String licTrigram = (String)   rowmap.get(LicenseUtil.INFO_LICENSE_NAME);
			       	                     licenseListToRelease[k] = licTrigram;
			       						}
			       						LicenseUtil.releaseLicenses(context, strUserName, licenseListToRelease, null);
					       				
			       						
		       						}
		       						catch(Exception exp)
		       						{
		       						    String strMsg = exp.getMessage();
		       						    if(strMsg.indexOf("License not found") == -1)
		       						    {
		       						        throw new Exception(exp);
		       						    }
		       						}
									String strCommand;
		       						for (int j = 0; j< licenseList.length; j++){
		       							String strResult = "";
		       							strCommand = "print product $1 select $2 dump $3";
		       						    if(isCasualUser){
		       						    	strResult = MqlUtil.mqlCommand(context, strCommand, true, licenseList[j], "casualhour["+ sLicensedHours +"].person", "|");
		       						    }else{
		       						    	strResult = MqlUtil.mqlCommand(context, strCommand, true, licenseList[j], "person", "|");     						    	
		       						    }       						 	
		       						 	if (strResult != null && !"".equals(strResult) && strResult.indexOf(strUserName) != -1){
		       						 		strCommand = "modify product " + licenseList[j] + " remove person \"" + strUserName + "\""; 
		       						    	MqlUtil.mqlCommand(context, strCommand, true);
		       						 	}
		       						}       						
		       						
		                    }
		                    personObj.demote(context);
		                    logger.info(userName+" IDM-ESB新建用户，处理用户数据，用户冻结成功");
						}
					}else if("E".equals(operation)){
						DomainObject personObj = new DomainObject(new BusinessObject("Person",userName,"-","eService Production"));
						if(personObj.exists(context) && "Inactive".equals(personObj.getInfo(context, "current"))){
							personObj.promote(context);
							logger.info(userName+" IDM-ESB新建用户，处理用户数据，用户激活成功");
						}
					}else if("A".equals(operation)){
						String tempUserImportFilePath = "/tmp/Imp3DSpaceUser_"+userName+".txt";
//						String execPath="sh "+importSHPath+" -user \""+internalUser+"\" -password \""+internalPwd+"\" -context \"VPLMAdmin.LONGi.Default\" -server \""+internalURL+"\" -file /tmp/Imp3DSpaceUser.txt";
						String execPath="sh "+importSHPath+" -user \""+internalUser+"\" -password \""+internalPwd+"\" -context \"VPLMAdmin.LONGi.Default\" -server \""+internalURL+"\" -file "+tempUserImportFilePath;
						String defaultPwd = "Aa123456";
						//Create Import TXT
//						File importFile = new File("/tmp/Imp3DSpaceUser.txt");
						File importFile = new File(tempUserImportFilePath);
						if(importFile.exists()){
							importFile.delete();
						}
						String importStr = "*PERSON "+userName+";LONGi;$;0\r\n"+
											"+ATTRIBUTE Country;China\r\n"+
											"+CONTEXT Basic User.LONGi.GLOBAL;,;CSV,IFW\r\n"+
											"+MEMBER LONGi\r\n"+
											"+PASSWORD "+defaultPwd+"\r\n"+
											"+ATTRIBUTE Email Address;"+email+"\r\n"+
											"+ATTRIBUTE First Name;"+firstName+"\r\n"+
											"+ATTRIBUTE Last Name;LONGI\r\n"+
											"+ATTRIBUTE Host Meetings;Yes\r\n"+
											"+ATTRIBUTE Icon Mail;FALSE\r\n"+
											"+ATTRIBUTE JT Viewer Type;None\r\n"+
											"+ATTRIBUTE Login Type;Standard\r\n"+
											"+ATTRIBUTE Originator;admin_platform";
						FileOutputStream fos = new FileOutputStream(importFile);
						fos.write(importStr.getBytes());
						fos.flush();
						fos.close();
						
						
						//Create 3DSpace Account
						Runtime runtime = Runtime.getRuntime();
						Process pro = runtime.exec(execPath);
						int status = pro.waitFor();
						if (status != 0)
						{
							System.out.println("Failed to call shell's command");
							esbInfo.add("instId", instId);
							esbInfo.add("returnStatus","E");
							esbInfo.add("returnCode","000");
							esbInfo.add("returnMsg","[PDM]\u64CD\u4F5C\u5931\u8D25");
							esbInfo.add("requestTime",requestTime);
							esbInfo.add("responseTime",sdf.format(new Date()));
							esbInfo.add("attr1","");
							esbInfo.add("attr2","");
							esbInfo.add("attr3","");
							
							resultInfo.add("returnStatus","E");
							resultInfo.add("returnCode","000");
							resultInfo.add("returnMsg","[PDM]Failed to call shell's command");
	
							output.add("esbInfo", esbInfo);
							output.add("resultInfo", resultInfo);
							logger.info(userName+" IDM-ESB新建用户，处理用户数据，用户新建失败");
						}else{
	
							BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
							StringBuffer strbr = new StringBuffer();
							String line;
							while ((line = br.readLine())!= null)
							{
								strbr.append(line).append("\n");
							}
				 
							String result = strbr.toString();
							System.out.println(result);
							pro.destroy();
							pro = null;
							
							//Set Person Id 
							DomainObject personObj = new DomainObject(new BusinessObject("Person",userName,"-","eService Production"));
							personObj.setAttributeValue(context, "LONGiModulePersonId", personId);
							
							//Get Key
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
							
							//Create 3DPassport
							
							String jsonStr = "{\"active\":true,\"lastSynchronized\":0,\"sourceRepositories\":[],\"fields\":{"+"\"email\":\""+email+"\","+"\"lastName\":\"LONGI\","+"\"firstName\":\""+firstName+"\","+"\"country\":\"CN\","+"\"username\":\""+userName+"\""+",\"password\":\""+defaultPwd+"\""+"},\"acls\":{\"3dexperience\":\"ACCEPTED\",\"passport\":\"ACCEPTED\"},\"tenant\":null}";
							
							URI localURI = new URI(new StringBuilder().append(passportURL).append("/api/private/tenant1/user/register").toString());
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
							logger.info(userName+" IDM-ESB新建用户，处理用户数据，用户新建成功");
						}
					}
				}
			//}
			
			//Set Return Json
			esbInfo.add("instId", instId);
			esbInfo.add("returnStatus","S");
			esbInfo.add("returnCode","000");
			esbInfo.add("returnMsg","[PDM]\u64CD\u4F5C\u6210\u529F");
			esbInfo.add("requestTime",requestTime);
			esbInfo.add("responseTime",sdf.format(new Date()));
			esbInfo.add("attr1","");
			esbInfo.add("attr2","");
			esbInfo.add("attr3","");
			
			resultInfo.add("returnStatus","S");
			resultInfo.add("returnCode","000");
			resultInfo.add("returnMsg","[PDM]\u64CD\u4F5C\u6210\u529F");

			output.add("esbInfo", esbInfo);
			output.add("resultInfo", resultInfo);

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
			esbInfo.add("instId", instId);
			esbInfo.add("returnStatus","E");
			esbInfo.add("returnCode","000");
			esbInfo.add("returnMsg","[PDM]\u64CD\u4F5C\u5931\u8D25");
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
		return Response.status(HttpServletResponse.SC_OK).entity(output.build().toString()).build();
		
	}
}
