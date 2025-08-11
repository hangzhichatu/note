<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="com.matrixone.apps.domain.util.*,com.matrixone.apps.common.util.*,com.matrixone.apps.domain.*" %>
<%@page import="java.io.*,java.util.*"%>
<%@page import="com.matrixone.json.JSONObject"%>
<%@page import="com.matrixone.apps.common.util.FormBean"%>
<%@page import="com.matrixone.apps.framework.ui.UINavigatorUtil"%>
<%@page import="matrix.db.JPO,com.matrixone.apps.domain.DomainObject,java.util.Map,com.matrixone.servlet.Framework,com.matrixone.servlet.MultipartRequest"%>
<%@page import="matrix.util.StringList,org.apache.commons.fileupload.FileItem"%>
<%@page import="org.apache.commons.fileupload.disk.DiskFileItemFactory,org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<jsp:useBean id="tableBean" class="com.matrixone.apps.framework.ui.UITable" scope="session"/>
<%!
	private Map getParamMap(HttpServletRequest request,String path,String ctxUser) throws Exception{
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload diskFileUpload = new ServletFileUpload(factory);
		diskFileUpload.setHeaderEncoding("UTF-8");
		List fileItems = diskFileUpload.parseRequest(request);
		
		Iterator i = fileItems.iterator();
		Map paramMap = new HashMap();
		while (i.hasNext()) {
				FileItem fi = (FileItem) i.next();
				String fieldName = fi.getFieldName();
				
				if (!fi.isFormField()) {
					String fileName = fi.getName();
					//fileName = chinaToUnicode(fileName);
					//System.out.println("--->>>[liurui] : fileName1 = " + fileName);
					//fileName = getUpEname(fileName);
					//System.out.println("--->>>[liurui] : fileName2 = " + fileName);
					if("".equals(fileName)) continue;
					String filePath = writeFileToLocal(path, fileName, fi.getInputStream(),ctxUser);
					
					if(paramMap.containsKey(fieldName)){
						String existFilePath = (String)paramMap.get(fieldName);
						
						paramMap.put(fieldName,existFilePath + "," + filePath);
					}else{
						paramMap.put(fieldName,filePath);
					}
				}else{
					paramMap.put(fieldName,fi.getString("UTF-8"));
				}
		}
		return paramMap;
	}
	private String writeFileToLocal(String dir, String fileName, InputStream is,String ctxUser) throws Exception{
		java.io.File file = new java.io.File(dir+System.getProperty("file.separator")+ fileName);
		if(!file.getParentFile().exists() && !file.getParentFile().isDirectory()) {
			file.getParentFile().mkdirs();
		}
		if(!fileName.contains(ctxUser)){
			//获取文件的原始名称
			String originalFilename = file.getName();//timg (1).jpg
			//获取文件的后缀名 .jpg
			String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
			fileName = ctxUser+suffix;
		}
		File newFile = new File(dir+System.getProperty("file.separator")+fileName);
		
		OutputStream os = null;
		try{
			os = new FileOutputStream(newFile);
			int b = 0;
			while((b = is.read()) != -1){
				os.write(b);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		finally{
			if(os != null)
				os.close();
		}
		return newFile.getAbsolutePath();
	}
%>
<%
	matrix.db.Context context = Framework.getFrameContext(session);
	String objectId = request.getParameter("objectId");
	
    String strfile = "";
	String sep = System.getProperty("file.separator");
	String sWorkspace = context.createWorkspace();
	JSONObject retJson = new JSONObject();
	String uploadPath =  "";
	String elementName =  "";
	String ctxUser = MqlUtil.mqlCommand(context,"print bus "+objectId+" select name dump",true);
    try {
		Map paramMap = getParamMap(request,sWorkspace,ctxUser);
		String filePath = (String)paramMap.get("IMP_PersonImage_file");
		MqlUtil.mqlCommand(context,"checkin bus "+objectId+" unlock server format 'Image' store 'STORE' append '"+filePath+"'",true);
		retJson.put("status","success");
    }catch (Exception e){
		e.printStackTrace();
		retJson.put("status","failed");
		retJson.put("message",e.getMessage());
    }
	out.print(retJson.toString());
%>
