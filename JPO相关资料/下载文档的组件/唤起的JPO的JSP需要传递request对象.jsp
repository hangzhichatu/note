<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@page import="java.util.HashMap"%>
<%@page import="matrix.db.*"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>

<script language="javascript" src="../common/scripts/emxUICore.js"></script>
<script language="javascript" src="../common/scripts/emxUIConstants.js"></script>
<script language="javascript" src="../common/scripts/emxUISearch.js"></script>

<scrip src="">
<%
try{
	String objectid = emxGetParameter(request,"objectId");
	DomainObject docObje =new DomainObject(objectid);
	
	FileList files = docObje.getFiles(context);
	for(matrix.db.File file : files){
		String fileName = file.toString();
		docObje.checkOutFile(context,false,"generic",fileName,"路径")；
		
		
	}
	
	argsMaps.put("docName",DocName);//文档名称
	argsMaps.put("path","路径");//
	argsMaps.put("response",response);//和请求对象reques对于response对象，用于后续唤起JPO返回文档或其他服务器资源时候操作
}
%>