<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*,com.jspsmart.upload.*,java.io.*,java.text.*" %>
<%@ page
        import="matrix.util.UUID,matrix.db.RelationshipType,com.matrixone.servlet.Framework,matrix.db.Context,com.matrixone.apps.domain.DomainObject,com.matrixone.apps.domain.util.PersonUtil,matrix.util.MatrixException,com.matrixone.apps.domain.util.ContextUtil,com.matrixone.apps.domain.util.FrameworkException,com.matrixone.apps.domain.util.MqlUtil,com.matrixone.apps.domain.util.ContextUtil,com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="jxl.*" %>
<%@ page import="java.io.File" %>
<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../emxTagLibInclude.inc" %>
<%@include file="../common/emxUIConstantsInclude.inc" %>

<html>
<head>
    <title>文件上传处理页面</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<body>
<%
    context = Framework.getFrameContext(session);
    String relFilePath = "";
    String strPathFile = request.getSession().getServletContext().getRealPath("/") + "upload";
    String strfile = "";
    try {
        java.io.File dirFile = new java.io.File(strPathFile);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        com.matrixone.servlet.MultipartRequest fileUpload = new MultipartRequest(request, strPathFile);
        Enumeration oFiles = fileUpload.getFileNames();
        String sFileName = "";
        if (oFiles != null) {
            while (oFiles.hasMoreElements()) {
                // query the multipart request for HTTP files
                String sName = (String) oFiles.nextElement();
                sFileName = fileUpload.getFilesystemName(sName);
                if ("file".equals(sName)) {
                    strfile = sFileName;
                    break;
                }
            }
        }
        String filename = sFileName;
        String prefix = filename.substring(filename.lastIndexOf(".") + 1);
        if (!"xlsx".equals(prefix)) {
%>
<script type="text/javascript">
    window.opener.location.reload();
    alert("请导入正确的类型，只支持.xlsx文件类型。");
    window.close();
</script>
<%
        return;
    }
    //改名开始
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");
    Date myDate = new Date();
    String tempname = formatter.format(myDate);
    String temp_NewFileName = "UpdateCustoAttr_" + tempname + ".xlsx"; // 这儿在日期字符串加上了一个标识符
    relFilePath = strPathFile + "/" + filename;
    File file = new File(relFilePath);
    file.renameTo(new File(strPathFile + "/" + temp_NewFileName));
    relFilePath = strPathFile + "/" + temp_NewFileName;
    HashMap<String, String> paramMap = new HashMap<String, String>(2);
    paramMap.put("relFilePath", relFilePath);
    System.out.println("invoke");
    String result = (String) JPO.invoke(context, "ImportUpdateCustoAttribute", new String[]{}, "ImportExcel", JPO.packArgs(paramMap), String.class);
    if ("OK".equals(result)) {
%>
<script type="text/javascript">
    window.opener.location.reload();
    alert("汉化导入完成，请关闭。");
    window.close();

</script>
<%
} else if ("length".equals(result)) {
%>
<script type="text/javascript">
    window.opener.location.reload();
    alert("名称不符合，请限制名称长度在40汉字以内！");
    window.close();
</script>
<%
} else {
%>
<script type="text/javascript">
    window.opener.location.reload();
    alert("导入出现问题，请重新导入。");
    window.close();
</script>
<%
    }
} catch (Exception e) {
%>
<script type="text/javascript">
    window.opener.location.reload();
    alert("导入出现问题，请重新导入。");
    window.close();
</script>
<%
    }

%>


</body>
</html>