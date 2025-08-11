<%-- Common Includes --%>
<%@page import="com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil"%>
<%@page import="com.dassault_systemes.enovia.tskv2.ProjectSequence"%>
<%@page import="com.matrixone.apps.common.TaskDateRollup"%>
<%@page import="javax.json.JsonObjectBuilder"%>
<%@page import="javax.json.Json"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="javax.json.JsonObjectBuilder"%>
<%@page import="javax.json.JsonArrayBuilder"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="com.matrixone.apps.program.Currency"%>
<%@page import="com.matrixone.apps.common.WorkCalendar"%>
<%@page import="com.matrixone.apps.program.fiscal.Helper"%>
<%@page import="com.matrixone.apps.common.Search"%>
<%@page import="java.util.Set"%>
<%@page import=" com.dassault_systemes.enovia.e6wv2.foundation.db.ObjectUtil"%>

<%@ page import="java.util.HashMap,java.util.Date"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.matrixone.apps.domain.util.MessageUtil"%>


<%@include file="../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file="../emxUICommonAppInclude.inc"%>
<%@ include file = "../emxUICommonHeaderBeginInclude.inc" %>
<%@include file = "../emxUICommonHeaderEndInclude.inc" %>
<%@include file = "../common/emxUIConstantsInclude.inc"%>

<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@page import="matrix.db.MQLCommand"%>
<%@page import="com.matrixone.apps.common.Company,matrix.util.StringList" %>
<%@page import="com.matrixone.apps.program.ProgramCentralUtil"%>
<%@page import="com.matrixone.apps.domain.util.MapList"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="com.matrixone.apps.common.ProjectManagement"%>

<%@page import="java.util.Enumeration"%>
<%@page import="com.matrixone.apps.program.ProgramCentralConstants"%>
<%@page import="com.matrixone.apps.domain.util.XSSUtil"%>
<%@page import="com.matrixone.apps.domain.util.FrameworkUtil"%>
<%@page import="com.matrixone.apps.domain.util.EnoviaResourceBundle"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.matrixone.apps.program.FTE"%>
<%@page import="com.matrixone.apps.program.ResourceRequest"%>
<%@page import="com.matrixone.apps.program.Question"%>

<%@page import="com.matrixone.apps.program.fiscal.CalendarType"%>
<%@page import="com.matrixone.apps.program.fiscal.Interval"%>
<%@page import="com.matrixone.apps.program.fiscal.Helper"%>
<%@page import="com.dassault_systemes.enovia.dpm.ProjectService"%>
<%@page import="com.matrixone.apps.program.ProgramCentralUtil"%>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>


<%@ page import="java.io.IOException" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>

<%@include file = "../common/emxNavigatorBottomErrorInclude.inc" %>


<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<jsp:useBean id="formBean" scope="session" class="com.matrixone.apps.common.util.FormBean"/>
<SCRIPT language="javascript" src="../common/scripts/emxUICore.js"></SCRIPT>
<script language="javascript" src="../common/scripts/emxUIConstants.js"></script>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script src="../programcentral/emxProgramCentralUIFormValidation.js" type="text/javascript"></script>


<%	
	
	
	String errormessange="";
	try{
	String selectRoteNoteId=request.getParameter("stringListParam");//传入所根节点的id（文档对象）
	String redirectToURL = "../teamcentral/BP_CopyWorkspaceVaultFromTemplatePre.jsp?action=refresh";
	//String selectRoteNoteId=(String) session.getAttribute("BPDocumentRowsId");
	
    String[] emxTableRowIds=request.getParameterValues("emxTableRowId");
	if(selectRoteNoteId.equals("")){
		selectRoteNoteId=(String) session.getAttribute("BPDocumentRowsId");
	}
	String[] sourceObjects = FrameworkUtil.getSplitTableRowIds(emxTableRowIds);//该字符串的0位是所选复制的书签的id
	String selectclassId ="";
	
	
	if(sourceObjects.length>0){
	 selectclassId = sourceObjects[0];
	}
	//设置传入JPO的参数
	HashMap<String, String> programMap = new HashMap<String, String>(3);
	
	programMap.put("selectRoteNoteId",selectRoteNoteId);
	programMap.put("CopyWorkspaceVaultId",selectclassId);
	
	System.out.println("SelectClassId"+selectclassId+"request"+request);
	
	if(selectclassId.equals("")||selectclassId.equals(null))
	{
		%>
		<script>
		alert("请选择复制的书签!");
		getTopWindow().closeWindow();
		</script>
		
		<%
		return;
	}
	Map retMap=JPO.invoke(context, "BP_CopyWorkspace",null, "CopyWorkspaceVaultStructFromTemplate", JPO.packArgs(programMap),Map.class);
	errormessange=(String)retMap.get("returnMessange"); 
	
	
	
	%>	
		<html>
		<body>
		<script language="javascript">
		var messange = ""+'<%=errormessange%>'
		var redirectToURL = '<%=redirectToURL%>';
		alert(messange); 
		getTopWindow().closeWindow();		
		window.opener.location.reload(); // 刷新父页面
		
		</script>
		</body>
		</html>
		
		<%
	}catch(Exception e) {
                e.printStackTrace();
%>
		<html>
		<body>
		<script language="javascript">
		getTopWindow().closeWindow();
		</script>
		</body>
		</html>
<%
        }
%>
       

 
		