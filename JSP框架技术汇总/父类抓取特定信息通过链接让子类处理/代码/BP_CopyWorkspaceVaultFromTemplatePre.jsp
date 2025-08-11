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



<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<jsp:useBean id="formBean" scope="session" class="com.matrixone.apps.common.util.FormBean"/>
<SCRIPT language="javascript" src="../common/scripts/emxUICore.js"></SCRIPT>
<script language="javascript" src="../common/scripts/emxUIConstants.js"></script>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script src="../programcentral/emxProgramCentralUIFormValidation.js" type="text/javascript"></script>



<%
	String sObjId = "";
	String isNotDocumentNote = "Yes";
	try{
	System.out.println("YOU HAVE INTO TEST JSP !!!!!!");
	String Action=request.getParameter("action");//传入操作
	System.out.println("redirectAction=request.getParameter(action)="+Action);
    String[] emxTableRowIds=request.getParameterValues("emxTableRowId");	
	String[] projects = emxGetParameterValues(request,"emxTableRowId");
    		//String sObjId = "";
    		String sTempRowId = "";
    		
    		String[] strObjectIDArr    = new String[projects.length];
    		for(int i=0; i<projects.length; i++)
    		{
    			String sTempObj = projects[i];
				Map mParsedObject = ProgramCentralUtil.parseTableRowId(context,sTempObj);
				sObjId = (String)mParsedObject.get("objectId");
				strObjectIDArr[i] = sObjId;
				sTempRowId = (String)mParsedObject.get("rowId");
				
    		}
	
	
	List<String> stringList = Arrays.asList(strObjectIDArr);
	System.out.println("nianiania===stringList"+stringList);
	String stringListParam = String.join(",", stringList);
	System.out.println("stringListParam===stringListParam"+stringListParam);
	

	String redirectToURL = "../common/emxIndentedTable.jsp?program=BP_CopyWorkspace:getAllProjectTemplate&table=TMCSelectFolder&expandProgram=BP_CopyWorkspace:BPgetSubWorkspace&header=\u9009\u62e9\u5e93&cancelLabel=\u53d6\u6d88&submitURL=../teamcentral/BP_CopyWorkspaceVaultFromTemplate.jsp&submitLabel=\u63d0\u4ea4&stringListParam=" + stringListParam+"&targetLocation=slidein";
	
	
    String encodedRedirectToURL = URLEncoder.encode(redirectToURL, "UTF-8");
	
	//校验所选根节点类型
	String sType=MqlUtil.mqlCommand(context,"print bus "+stringListParam+" select type dump |;");
	if(sType.equals("Document")||sType.equals("BP_RequireDocument")){
		isNotDocumentNote = "No";
		%>
		<script>
		alert("请选择书签，或者项目作为复制的根节点!");
		
		</script>
		
		<%  
	}
	
	if(sType.equals("")){
		isNotDocumentNote = "No";
		%>
		<script>
		alert("请选择书签，或者项目作为复制的根节点!");
		
		</script>
		
		<%  
	}
	
	
  if(stringList.size()==1 && isNotDocumentNote.equals("Yes")){
	
%>
<html>
<head>
    <title>Redirecting...</title>
    <script type="text/javascript">
        window.onload = function() {
            // 设置要跳转的URL
            var redirectToURL = '<%=redirectToURL%>';  // 替换为你想要跳转的URL
             //emxShowModalDialog(redirectToURL, 500, 500);修改测试(测试可行)
			 getTopWindow().showModalDialog('<%=redirectToURL%>');
		
			//window.opener.closeWindow(); 

			 //window.location.href = redirectToURL; 
        };
    </script>
</head>

</html>
<%
  }else{
	  
	%>
		<script>
		alert("请只选择一个节点!");
		getTopWindow().closeWindow();
		</script>
		
		<%  
	  
  }
  
	
  
}catch(Exception e) {
                e.printStackTrace();
        }
%>