<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="com.matrixone.apps.domain.util.*,com.matrixone.apps.common.util.*,com.matrixone.apps.domain.*" %>
<%@page import="java.io.*,java.util.*"%>
<%@page import="matrix.db.JPO,com.matrixone.apps.domain.DomainObject,java.util.Map,com.matrixone.servlet.Framework,com.matrixone.servlet.MultipartRequest"%>
<%@page import="matrix.util.StringList,org.apache.commons.fileupload.FileItem"%>
<%@page import="org.apache.commons.fileupload.disk.DiskFileItemFactory,org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%!
	private StringList getFileList(HttpServletRequest request,String path) throws Exception{
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload diskFileUpload = new ServletFileUpload(factory);
		diskFileUpload.setHeaderEncoding("UTF-8");
		List fileItems = diskFileUpload.parseRequest(request);
		Iterator i = fileItems.iterator();
		StringList fileList = new StringList();
		while (i.hasNext()) {
				FileItem fi = (FileItem) i.next();
				if (!fi.isFormField()) {
					String fileName = fi.getName();
					if("".equals(fileName)) continue;
					String filePath = writeFileToLocal(path, fileName, fi.getInputStream());
					fileList.add(fileName);
				}
		}
		return fileList;
	}
	private String writeFileToLocal(String dir, String fileName, InputStream is) throws Exception{
		File file = new File(dir+System.getProperty("file.separator")+ fileName);
		if(!file.getParentFile().exists() && !file.getParentFile().isDirectory()) {
			file.getParentFile().mkdirs();
		}
		OutputStream os = null;
		try{
			os = new FileOutputStream(file);
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
		return file.getAbsolutePath();
	}
%>
<%
	matrix.db.Context context = Framework.getFrameContext(session);
    String strfile = "";
	String objectId = request.getParameter("objectId");
	String sWorkspace = context.createWorkspace();
	String sep = System.getProperty("file.separator");
	String errorMessage="";
    try {
		StringList fileList =getFileList(request,sWorkspace);
		if(fileList!=null&&fileList.size()>0){
			strfile = (String)fileList.get(0);
		}
		String JPOName = request.getParameter("JPOName");
		String MethodName =  request.getParameter("MethodName");
		Map paramMap=new HashMap();
		paramMap.put("objectId",objectId);
		paramMap.put("path",sWorkspace);
		paramMap.put("fileName",strfile);
		paramMap.put("fileList",fileList);
		String[] argsJPO = JPO.packArgs(paramMap);
		errorMessage=JPO.invoke(context, JPOName, null, MethodName, argsJPO, String.class);
		try{
			if(fileList!=null&&sWorkspace!=null){
				for(int i=0;i<fileList.size();i++){
					String filePath = sWorkspace+"/"+fileList.get(i);
					new File(filePath).delete();
				}
			}
		}catch(Exception e1){
			e1.printStackTrace();
		}
    }catch (Exception e){
		e.printStackTrace();
		errorMessage=e.getMessage();
    }
	out.print(errorMessage);
%>
