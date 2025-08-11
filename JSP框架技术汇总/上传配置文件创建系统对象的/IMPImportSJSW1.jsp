<%@page contentType="text/html;charset=utf-8"%>
<%@page	import="com.matrixone.apps.domain.*,com.matrixone.apps.domain.util.*,matrix.util.*,matrix.db.Context,com.matrixone.servlet.Framework,java.io.PrintWriter"%>
<%@page	import="java.util.*,java.text.SimpleDateFormat,java.math.BigDecimal,java.util.Calendar,java.text.*"%>
<%@include file="../common/emxUIConstantsInclude.inc"%>
<%@include file="../emxRequestWrapperMethods.inc"%>
<%@include file="../emxStyleDefaultInclude.inc"%>

<%
	String csddcTitle = "实际事务导入";
	String loadPage = "";
	try{
		String objectId= emxGetParameter(request, "objectId");
		
		String mode = request.getParameter("mode");
		System.out.println("mode11__" + mode);
		System.out.println("objectId+++++++++++++"+ objectId);
		
		loadPage = "IMPImportSJSW2.jsp?objectId="+objectId;
	} catch (Exception ex) {
		ex.printStackTrace();
	} 

%>


<html><head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<title><%=csddcTitle%></title>
<script language="JavaScript" type="text/javascript" src="../common/scripts/emxUIConstantsJavaScriptInclude.jsp">
</script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js">
</script>
<script language="JavaScript" src="../common/scripts/emxUICore.js">
</script>
<script language="JavaScript" src="../common/scripts/emxUICoreMenu.js">
</script>
<script language="JavaScript" src="../common/scripts/emxUIToolbar.js">
</script>
<script language="JavaScript" src="../common/scripts/emxUIFilterUtility.js">
</script>
<script language="JavaScript" src="../common/scripts/emxUIActionbar.js">
</script>
<script language="JavaScript" src="../common/scripts/emxUIModal.js">
</script>
<script language="JavaScript" src="../common/scripts/emxNavigatorHelp.js">
</script>
<script language="JavaScript" src="../common/scripts/emxUIPageUtility.js">
</script><script language="JavaScript" src="../common/scripts/emxUIBottomPageJavaScriptInclude.js">
</script>
<script language="Javascript"> 
   addStyleSheet("emxUIDefault");
   addStyleSheet("emxUIToolbar"); 
   addStyleSheet("emxUIMenu");  
   addStyleSheet("emxUIDOMLayout"); 
   addStyleSheet("emxUIDialog"); 
   var linkArray = new Array;
   var pageNumber = 1;	 
   var pages = 1;
   var idArray = new Array();
   var busIdArray = new Array();	
   var contentArray = new Array();	
   var scrollLoc = 0;
   </script>
   <link rel="stylesheet" type="text/css" href="../common/styles/emxUIDefault.css">
   <link rel="stylesheet" type="text/css" href="../common/styles/emxUIToolbar.css">
   <link rel="stylesheet" type="text/css" href="../common/styles/emxUIMenu.css">
   <link rel="stylesheet" type="text/css" href="../common/styles/emxUIDOMLayout.css">
   <link rel="stylesheet" type="text/css" href="../common/styles/emxUIDialog.css">
   </head>
   <body class="dialog"">
   <div id="pageHeadDiv">
   <form name="mx_filterselect_hidden_form">
   <input name="pheader" id="pheader" value="emxComponents.CreateRouteWizardDialog.SpecifyDetailsRW" type="hidden">
   <table>
   <tbody>
   <tr>
   </br>
   <td class="page-title">
   <h2 id="ph"><%=csddcTitle%></h2>
<h3 id="sph"></h3>
	</td><td class="functions">
	<table>
	<tbody>
	<tr>
	<td class="progress-indicator">
	<div style="visibility: hidden;" id="imgProgressDiv">


	</div>
	</td>
	</tr>
	</tbody>
	</table>
	</td>
	</tr>
	</tbody>
	</table>
	<script language="JavaScript" src="../common/scripts/emxToolbarJavaScript.jsp" type="text/javascript">
	</script><div class="toolbar-container" id="divToolbarContainer">

	</div>
	</form>
	</div>
	<div style="top: 50px; position:relative ;height:1024px" id="divPageBody">
	
	<iframe name="pagecontent" id="pagecontent" src="<%=loadPage%>" border="0" frameborder="0" height="500px" width="400px%">
	</iframe>
	
	</div>
	<div id="divPageFoot">
		<form method="post" name="bottomCommonForm" id="bottomCommonForm">

<table>
<tbody>
<tr>
    	<td class="functions"></td>
		   <td class="buttons">
		   <table><tbody><tr>

		   <td><a href="javascript:this.frames['pagecontent'].submitValidate()"><img src="../common/images/buttonDialogDone.gif" align="absmiddle" border="0"></a></td>
		   <td><a href="javascript:this.frames['pagecontent'].submitValidate()">提交</a></td>

		<td> </td>

		   <td><a href="javascript:this.frames['pagecontent'].closeWindow()"><img src="../common/images/buttonDialogCancel.gif" align="absmiddle" border="0"></a></td>
		   <td><a href="javascript:this.frames['pagecontent'].closeWindow()">取消</a></td>

</tr></tbody></table>
</td>
</tr>
</tbody></table>



</form></div>

</body></html>