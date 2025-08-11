<html>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%@include file="../common/emxNavigatorTopErrorInclude.inc"%>
<%@page import="com.matrixone.apps.program.ProgramCentralUtil"%>
<%@page import="java.util.Map"%>

<%@ include file="../emxUICommonHeaderBeginInclude.inc"%>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<head></head>
<%
	String objectId = request.getParameter("objectId");
	if (null == objectId || "".equals(objectId))
		objectId = request.getParameter("parentOID");
	DomainObject personObj = new DomainObject(objectId);
	String sep = System.getProperty("file.separator");
	String outPath = application.getRealPath("")+ sep +"SignaturePic";
	
	FileList fl = personObj.getFiles(context);
	String filePath = "";
	String fileName = "";
	for(matrix.db.File f :fl) {
		String format = f.getFormat();
		fileName = f.getName();
		if(fileName.endsWith(".png")) {
			personObj.open(context);
			personObj.checkoutFile(context, false, format, fileName, outPath);
			personObj.close(context);
			filePath = "../SignaturePic/"+fileName+"?version="+System.currentTimeMillis();
		}
	}
%>

<body>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="javascript" type="text/javascript" src="../components/emxComponentsJSFunctions.js"></script>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUICalendar.js"></script>
<script language="javascript" src="../common/scripts/emxUIModal.js"></script>

<script language="javascript" src="../plugins/bootstrap-3.3.7-dist/js/jquery-3.4.1.min.js"></script>
<script language="javascript" src="../plugins/bootstrap-3.3.7-dist/js/bootstrap.js"></script>
<script language="javascript" src="../plugins/bootstrap-3.3.7-dist/js/bootstrap.min.js"></script>
<script language="javascript" src="../plugins/bootstrap/js/bootstrap.min.js"></script>
<script language="javascript" src="../plugins/bootstrap-fileinput/js/fileinput.min.js"></script>
<script language="javascript" src="../plugins/bootstrap-fileinput/js/locales/zh.js"></script>
<script type="text/javascript" src="../plugins/bootstrap-select-1.13.10/js/bootstrap-select.js"></script>

<link  rel="stylesheet" href="../plugins/bootstrap-3.3.7-dist/css/bootstrap.min.css" type="text/css">
<link rel="stylesheet" href="../plugins/bootstrap-3.3.7-dist/css/bootstrap.css" type="text/css">
<link rel="stylesheet" href="../plugins/bootstrap-fileinput/css/fileinput.css" type="text/css">
<link rel="stylesheet" href="../plugins/bootstrap-select-1.13.10/dist/css/bootstrap-select.css" type="text/css">    
<form name="IssueCreate" method="post" action="DN_CreateCheckItemForAllUserProcess.jsp" onsubmit="submitForm(); return false">
<table>
	<tr id="calc_IMP_originalImage">
	<td width="150" nowrap="nowrap" class="createLabel">
	<label for="IMP_originalImage">原始签名图</label>
	</td>
      <td nowrap="nowrap" class="inputField">
		<img class="img-circle" src="<%=filePath%>" alt=""/>
      </td>
    </tr>
	<tr id="calc_IMP_PersonImage">
	<td width="150" nowrap="nowrap" class="createLabel">
	<label for="IMP_PersonImage">选择新图片</label>
	</td>
      <td nowrap="nowrap" class="inputField">
		<input name="IMP_PersonImage_file" id="IMP_PersonImage_file" multiple type="file" class="file" accept="image/*"/>
      </td>
    </tr>
  </table>
  </form>
  <script language="javascript" type="text/javaScript">
	//初始化fileinput
	initFileInput("IMP_PersonImage_file");
	function initFileInput(ctrlName) {
		var control = $('#' + ctrlName); 
		control.fileinput({
			language: 'zh', //设置语言
			dropZoneTitle: '可以将图片拖放到这里 …仅允许上传一张图片',
			uploadUrl: 'IMP_FileUpload.jsp?objectId=<%=objectId%>', //默认异步上传
			uploadAsync: false, //默认异步上传
			autoReplace: true, //是否自动替换当前图片，设置为true时，再次选择文件， 会将当前的文件替换掉。
			overwriteInitial: true, //自动覆盖
			showUpload: true, //是否显示上传按钮
			showRemove: true, //显示移除按钮
			showPreview: true, //是否显示预览
			showCancel:true,   //是否显示文件上传取消按钮。默认为true。只有在AJAX上传过程中，才会启用和显示
			showCaption: true,//是否显示文件标题，默认为true
			browseClass: "btn btn-primary", //文件选择器/浏览按钮的CSS类。默认为btn btn-primary
			dropZoneEnabled: true,//是否显示拖拽区域
			allowedFileExtensions: ["png"],
			minImageWidth: 50, //图片的最小宽度
			minImageHeight: 50,//图片的最小高度
			maxImageWidth: 1000,//图片的最大宽度
			maxImageHeight: 1000,//图片的最大高度
			maxFileSize: 10240,//单位为kb，如果为0表示不限制文件大小
			minFileCount: 1, //每次上传允许的最少文件数。如果设置为0，则表示文件数是可选的。默认为0
			maxFileCount: 1, //每次上传允许的最大文件数。如果设置为0，则表示允许的文件数是无限制的。默认为0
			previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",//当检测到用于预览的不可读文件类型时，将在每个预览文件缩略图中显示的图标。默认为<i class="glyphicon glyphicon-file"></i>  
			layoutTemplates :{                  
					  　　　　actionUpload:'',//去除上传预览缩略图中的上传图片；                 
					  　　　　//actionZoom:'',//去除上传预览缩略图中的上传图片；                 
					  　　　　actionDelete:'',//去除上传预览缩略图中的删除图片；                 
					  },
		}).on("fileremoved",function(event, data, msg){ 
			alert("123");
		}).on("fileuploaded", function (event, data, previewId, index) {
			alert('11');
		});
	}
  </script>
</body>
</html>
