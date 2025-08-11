<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<%@ page import="java.util.*,com.jspsmart.upload.*,java.io.*,java.text.*"%> 
<%@ page import="matrix.util.UUID,matrix.db.RelationshipType,com.matrixone.servlet.Framework,matrix.db.Context,com.matrixone.apps.domain.DomainObject,com.matrixone.apps.domain.util.PersonUtil,matrix.util.MatrixException,com.matrixone.apps.domain.util.ContextUtil,com.matrixone.apps.domain.util.FrameworkException,com.matrixone.apps.domain.util.MqlUtil,com.matrixone.apps.domain.util.ContextUtil,com.matrixone.apps.domain.DomainRelationship"%>
<%@ page import="jxl.*"%>
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../emxTagLibInclude.inc" %>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<%@include file = "../common/model/CSICTreeUtilInclude.inc" %>
<html> 
<head> 
	<title>文件上传处理页面</title> 
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"> 
</head>
<body> 
<% 
	System.out.println("yyyyyyyyyyyyyyyyyyyyy");
	System.out.println("22222222222222");
	 context = Framework.getFrameContext(session);
	System.out.println("333333333333333");
	String objectId = "";
	String mode = "";
	String relFilePath = "";
	SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
	Date myDate = new Date();
	String tempname = format.format(myDate);
	System.out.println("444444444444");		 
	
	try {
		// 新建一个SmartUpload对象 
		SmartUpload su = new SmartUpload(); 
		System.out.println("555555555555");
		// 上传初始化 
		su.initialize(pageContext); 
		// 设定上传限制 
		// 1.限制每个上传文件的最大长度。 
		// su.setMaxFileSize(100000000); 
		// 2.限制总上传数据的长度。 
		// su.setTotalMaxFileSize(20000); 
		// 3.设定允许上传的文件（通过扩展名限制）,仅允许doc,txt文件。 
		   su.setAllowedFilesList("xls,xlsx"); 
		// 4.设定禁止上传的文件（通过扩展名限制）,禁止上传带有exe,bat,jsp,htm,html扩展名的文件和没有扩展名的文件。 
		// su.setDeniedFilesList("exe,bat,jsp,htm,html"); 
		su.upload();
		System.out.println("111111111111111111");
		//改名开始
		com.jspsmart.upload.File myfile =  su.getFiles().getFile(0);
		String filename = su.getFiles().getFile(0).getFileName();
		String prefix=filename.substring(filename.lastIndexOf(".")+1);
		System.out.println("filename++++++++++++++++" + filename);	
		System.out.println("prefix++++++++++++++++" + prefix);	
		if(!"xlsx".equals(prefix)&&!"xls".equals(prefix)){
		%>
			<script type="text/javascript">
				window.opener.location.reload();
				alert("请导入正确的类型，只支持.xlsx或.xls文件类型。");
				window.close();
			</script>
		<%
			return;
		} 
		System.out.println("111111111111111111111111111111");	
		objectId = request.getParameter("objectId");
		objectId = objectId.replace("'", "");
		System.out.println("objectId_++++++++++++++++" + objectId);			
		if (myfile.getFileExt().length()>0) {
			System.out.println("22222222222222222222222222222");	
			String temp_FileName = "tempname" + "."+prefix;//这儿在日期字符串加上了一个标识符
			myfile.saveAs("/upload/"+temp_FileName,su.SAVE_VIRTUAL);
			String strPathFile = request.getSession().getServletContext().getRealPath("/");
			relFilePath = strPathFile+"upload\\" + temp_FileName;	
			HashMap<String, String> paramMap = new HashMap<String, String>(2);
			paramMap.put("objectId", objectId);
			paramMap.put("relFilePath", relFilePath);
			System.out.println("555555555555555555555555555555");
			System.out.println("relFilePath++++++++++++++"+relFilePath);
			Map resultMap = (Map)JPO.invoke(context, "IMPImportSJSW", new String [] {}, "importSJSW", JPO.packArgs(paramMap),Map.class);
			System.out.println("666666666666666666666666666666666");
			String information = (String)resultMap.get("status");
			
			System.out.println("information_+++++++++++++++++" + information);
			
			System.out.println("information2111111122222+++++++++++++++++" + information);
			if("OK".equals(information)) {
				String sjswid = (String)resultMap.get("sjswid");
				HashMap<String, String> paramMap1 = new HashMap<String, String>(2);
				paramMap1.put("objectId", objectId);
				paramMap1.put("sjswid", sjswid);
				System.out.println("555555555555555555555555555555");
				String res = (String)JPO.invoke(context, "IMPImportSJSW", new String [] {}, "connectGS", JPO.packArgs(paramMap1),String.class);
				if(res.equals("ok")){
					%>
					<script type="text/javascript">
					//window.parent.top.left.location.href = window.parent.top.left.location.href;
					alert("导入完成，请关闭。");
					top.opener.location.href = top.opener.location.href;
					window.opener=null;window.top.open('','_self','');window.close(this);window.parent.close(this);
					//window.opener=null;window.top.open('','_self','');window.close(this);
					</script>
				<%
				}else{
					
					%>
					<script type="text/javascript">
						window.opener.location.reload();
						alert("导入出现问题，请重新导入。");
						window.close();
					</script>
							
					<%
				}
			
			} else {
				System.out.println("3333333333333333333333333");
				String mes = (String)resultMap.get("mes");
				String sjswid = (String)resultMap.get("sjswid");
				 if(UIUtil.isNotNullAndNotEmpty(sjswid)){
					String[] str=sjswid.split(",");
					for(int m=0;m>str.length;m++){
						String sid=(String)str[m];
						ContextUtil.pushContext(context);
						MqlUtil.mqlCommand(context,"delete bus "+sid,true);
						ContextUtil.popContext(context);	
					}
				}
				System.out.println("mes++++++"+mes);
				if(!mes.equals("ERROR")){
					%>	
						<script type="text/javascript">
							window.opener.location.reload();
							alert("<%=mes%>");
							window.close();
						</script>
			
					<%	
				}else{
					%>
					<script type="text/javascript">
						window.opener.location.reload();
						alert("导入出现问题，请重新导入。");
						window.close();
					</script>
							
					<%
				}
			
			}
		}
	} catch (Exception e) {
		System.out.println("4444444444444444444444444444444"+e);	
	%>
		<script type="text/javascript">
			window.opener.location.reload();
			alert("导入出现问题，请重新导入。");
			window.close();
		</script>
				
	<%
	//out.println(e.toString());  
	}
	%> 


</body> 
</html> 