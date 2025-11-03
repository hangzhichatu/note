<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<html>
<head>
    <title>Session 信息查看器</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            background-color: #f4f6f9;
        }
        h2 {
            color: #333;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        th, td {
            border: 1px solid #ccc;
            padding: 10px;
            text-align: left;
        }
        th {
            background-color: #007bff;
            color: white;
        }
        tr:nth-child(even) {
            background-color: #f2f2f2;
        }
        .info {
            background-color: #e7f3ff;
            padding: 15px;
            border-left: 5px solid #007bff;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>

<h2>📌 当前 Session 信息</h2>

<div class="info">
    <strong>Session ID:</strong> <%= session.getId() %><br>
    <strong>是否为新会话:</strong> <%= session.isNew() %><br>
    <strong>创建时间:</strong> <%= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(session.getCreationTime())) %><br>
    <strong>最后访问时间:</strong> <%= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(session.getLastAccessedTime())) %><br>
    <strong>最大不活动间隔:</strong> <%= session.getMaxInactiveInterval() %> 秒<br>
    <strong>应用名称:</strong> <%= session.getServletContext().getServletContextName() != null ? 
        session.getServletContext().getServletContextName() : "未命名应用" %>
</div>

<%
    Enumeration<String> attrNames = session.getAttributeNames();
    boolean hasAttrs = attrNames.hasMoreElements();
%>

<% if (hasAttrs) { %>
    <h3>📋 Session 属性列表</h3>
    <table>
        <tr>
            <th>属性名 (Attribute Name)</th>
            <th>属性值 (Attribute Value)</th>
            <th>值的类型 (Class)</th>
        </tr>
        <% while (attrNames.hasMoreElements()) {
            String name = attrNames.nextElement();
            Object value = session.getAttribute(name);
            String valueStr = (value == null) ? "<i>null</i>" : value.toString();
        %>
        <tr>
            <td><strong><%= name %></strong></td>
            <td><%= valueStr %></td>
            <td><%= value == null ? "null" : value.getClass().getName() %></td>
        </tr>
        <% } %>
    </table>
<% } else { %>
    <p><em>🔍 当前 Session 中没有任何属性。</em></p>
<% } %>

</body>
</html>