<%@ page contentType="text/html; charset=UTF-8"%>
<%@include file="../emxUICommonAppInclude.inc"%>
<%@include file="../emxUICommonHeaderBeginInclude.inc"%>


<%	
	//add by zhenghang 2024-10-18
	String strParentObjectId = emxGetParameter(request,"objectId");
	session.setAttribute("strParentObjectId", strParentObjectId);
	
	//end by zhenghang 2024-10-18
	String strSearPage = emxGetParameter(request, "SearchPage");
	String strShowData = "";
	if("ACCDocSearch.jsp".equals(strSearPage)){
		strShowData = "../common/emxTable.jsp?program=ACCDoc:getMyDevelopDocs,ACCDoc:getMyCompanyDevelopDocs,ACCDoc:getAllDevelopDocs&programLabel=emxComponentCentral.ACCDevelopDoc.getMyDevelopDocs,emxComponentCentral.ACCDevelopDoc.getMyCompanyDevelopDocs,emxComponentCentral.ACCDevelopDoc.getAllDevelopDocs&toolbar=ACCDevelopDocSummaryToolbar&mode=New&table=ACCDevelopDocSummary&header=emxComponentCentral.ACCDevelopDoc.MyDoc&clearLimitNotice=true&editLink=false&selection=multiple&suiteKey=Components&StringResourceFileId=emxComponentsStringResource";
	}
%>
<html>
<style type="text/css">
body {
	width : 100%;
	height : 100%;
    overflow-x : hidden;
	overflow-y : hidden;
} 
</style>

<body>
    <div style="height:10%">
    <iframe name="SearchPage" style="height: 100%;" scrolling="yes" frameborder=no src="<%=strSearPage%>" ></iframe>
    </div>
     <div style="height:90%">
    <iframe name="ShowData" style="height: 100%;" scrolling="no" frameborder=no  src="<%=strShowData%>"></iframe>
    </div>
</body>
</html>    
