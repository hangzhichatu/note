<%@include file = "../engineeringcentral/emxDesignTopInclude.inc"%>
<%@include file = "../emxUICommonHeaderBeginInclude.inc"%>
<%@include file = "../engineeringcentral/emxEngrVisiblePageInclude.inc"%>

<script type="text/javascript" language="JavaScript" src="../common/scripts/emxUIBottomPageJavaScriptInclude.js"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil,
                 com.matrixone.apps.common.util.JSPUtil,
                 com.matrixone.apps.domain.DomainObject,
                 com.matrixone.apps.domain.DomainRelationship,
                 com.matrixone.apps.framework.taglib.*,
                 com.matrixone.apps.domain.util.MapList,
                 java.util.*" %>
<%
	String templete = "";
	String templateTools = "";
	String checkinFileURL = null;
	String language=context.getSession().getLanguage();
	
	//String objectId	= emxGetParameter(request,"objectId");
    try {
        checkinFileURL = "IMPImportDataProcess.jsp";
		String objectId = emxGetParameter(request, "objectId");
		String JPOName = emxGetParameter(request, "JPOName");
		String MethodName = emxGetParameter(request, "MethodName");
		String IsMultiple = emxGetParameter(request, "IsMultiple");
		String multiple = "";
		if("true".equals(IsMultiple)){
			multiple="multiple";
		}
		
		templete = emxGetParameter(request, "templeteFile");
		templete = i18nNow.getI18nString(templete,"emxEngineeringCentralStringResource","zh_cn");
		
		
		checkinFileURL+="?objectId="+objectId;
		checkinFileURL+="&JPOName="+JPOName;
		checkinFileURL+="&MethodName="+MethodName;
%>
<script language="JavaScript">
	var IsRunnning = false;
	var res="";
    function nextMethod() {
		var  importFiles = document.getElementById('file').files;
		if(importFiles.length==0){
			alert("\u8bf7\u9009\u62e9\u6587\u4ef6");
			return;
		}
		if(IsRunnning == true){
			alert("\u5df2\u6709\u5bfc\u5165\u4efb\u52a1\u6b63\u5728\u6267\u884c\uff0c\u8bf7\u7b49\u5f85\u5bfc\u5165\u7ed3\u679c!");
			return;
		}
		
		document.getElementById("importResult").innerHTML = "\u6b63\u5728\u5bfc\u5165\uff0c\u8bf7\u7b49\u5f85\u5bfc\u5165\u7ed3\u679c.......";
		IsRunnning = true;
		var formData = new FormData($("#checkinForm")[0]);
		$.ajax({
				url: "<%=checkinFileURL%>",
				type: "post",
				data: formData,
				cache: false,
				processData: false,
				contentType: false,
				success: function (data) {
					IsRunnning = false;
                    data = data.toString().trim();
                    var fontColor = "red";
					if(data==''){
						fontColor = 'green';
						data = "\u5bfc\u5165\u6210\u529f";
						document.getElementById('file').value="";
					}
					document.getElementById("importResult").innerHTML = "\u5bfc\u5165\u7ed3\u679c\uff1a<font color='"+fontColor+"'>"+data+"</font>";					
					alert(data);
					parent.opener.location.href = parent.opener.location.href;
					
					window.parent.opener=null;window.parent.open('','_self','');window.parent.close();
					
				},
				
				error: function (error) {
					IsRunnning = false;
					error = error.toString().trim();
					var fontColor = "red";
					document.getElementById("importResult").innerHTML = "\u5bfc\u5165\u7ed3\u679c\uff1a<font color='"+fontColor+"'>"+error+"</font>";	
					alert(error);
					window.parent.opener=null;window.parent.open('','_self','');window.parent.close();
				}
			});
		
    }
	
	
			
</script>
<%@include file = "../emxUICommonHeaderEndInclude.inc" %>

<form name="checkinForm" id="checkinForm" method="post"  enctype="multipart/form-data" action="<%=checkinFileURL%>">
    <table border="0" cellpadding="5" cellspacing="2" width="100%">
        <tr>
            <td class="labelRequired" nowrap="nowrap" width="20%"> 导入
            </td>
            <td class="inputField" width="40%"><input size="20" id="file" type="file" <%=multiple%> name="file" >
            </td>
		</tr>
<%
	if(templete!=null&&!"".equals(templete)){
%>	
		<tr>
			<td class="label" nowrap="nowrap" width="20%"> 模板下载
            </td>
			<td class="label">
				<a href="../templates/<%=templete %>">下载</a>
			</td>
		</tr>
<%
	}
%>
    <tr>
		<td colspan="2" >
                <div id="importResult"></div>
            </td>
	</tr>
</form>
<%
    } catch(Exception exc) {
		out.println(exc);
	}
%>
<%@include file = "../engineeringcentral/emxDesignBottomInclude.inc"%>
<%@include file = "../emxUICommonEndOfPageInclude.inc" %>

