<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>

<%
    String objectId = request.getParameter("objectId");
    String JPOName = request.getParameter("JPOName");
    String MethodName = request.getParameter("MethodName");
    String templeteFile = request.getParameter("templeteFile");
    String templateTools = request.getParameter("templateTools");
    String IsMultiple = request.getParameter("IsMultiple");

    String contentURL = "IMPImportDataBrowse.jsp?objectId=" + objectId +
                        "&JPOName=" + JPOName +
                        "&MethodName=" + MethodName +
                        "&templeteFile=" + templeteFile +
                        "&templateTools=" + templateTools +
                        "&IsMultiple=" + IsMultiple;
%>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>数据导入</title>
    <script>
        function nextMethod() {
            // 执行下一步操作的 JavaScript 代码
            alert("执行下一步操作");
            // 这里可以添加具体的逻辑
        }
    </script>
</head>
<body>
    <h1>数据导入</h1>
    <iframe src="<%= contentURL %>" width="100%" height="600px"></iframe>
    <div>
        <button onclick="nextMethod()">
            <img src="common/images/buttonDialogDone.gif" alt="完成"> 完成
        </button>
        <button onclick="window.close();">
            <img src="common/images/buttonDialogCancel.gif" alt="取消"> 取消
        </button>
    </div>
</body>
</html>
