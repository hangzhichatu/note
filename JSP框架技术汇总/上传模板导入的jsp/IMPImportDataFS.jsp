<%@include file = "../engineeringcentral/emxDesignTopInclude.inc"%>
<%
  	framesetObject fs = new framesetObject();
  	fs.setDirectory(appDirectory);
  	String contentURL = "IMPImportDataBrowse.jsp";
	String objectId = emxGetParameter(request, "objectId");
  	String JPOName = emxGetParameter(request, "JPOName");
	String MethodName = emxGetParameter(request, "MethodName");
	String templeteFile = emxGetParameter(request, "templeteFile");
	String templateTools = emxGetParameter(request, "templateTools");
	String IsMultiple = emxGetParameter(request, "IsMultiple");
	
	contentURL+="?objectId="+objectId;
	contentURL+="&JPOName="+JPOName;
	contentURL+="&MethodName="+MethodName;
	contentURL+="&templeteFile="+templeteFile;
	contentURL+="&templateTools="+templateTools;
	contentURL+="&IsMultiple="+IsMultiple;
	
  	String language=context.getSession().getLanguage();
  	String PageHeading = "";
  	String HelpMarker = "";
  	fs.initFrameset(
		PageHeading,
  		HelpMarker,
  		contentURL,
  		false,
  		false,
  		false,
  		false
  	);
  	fs.setStringResourceFile("emxFrameworkStringResource");
  	fs.setObjectId("");
	fs.createCommonLink(
		"emxFramework.Lifecycle.Done",
		"nextMethod()",
		"",
		false,
		true,
	   "common/images/buttonDialogDone.gif",
		false,
		4
	);
	fs.createCommonLink(
		"emxFramework.Lifecycle.Cancel",
		"parent.window.close()",
		"",
		false,
		true,
		"common/images/buttonDialogCancel.gif",
		false,
		0
	);  
  	fs.writePage(out);  
%>
