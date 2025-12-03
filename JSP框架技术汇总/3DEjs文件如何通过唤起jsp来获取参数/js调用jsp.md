## 唤起服务

* js相关代码

  ```javascript
  var delUrl = "../common/LAMC_SupperJs.jsp";//设置请求服务的地址
  var delStr = "objectId="+objectid;//拼接参数
  var delText = emxUICore.getDataPost(delUrl,delStr);//请求服务
  var delObject = emxUICore.parseJSON(delText);
  var result = delObject["result"];
  var item = result.split("|");
  ```
* 请求的jsp相关代码

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8"%>
  <%@ page import="javax.json.JsonObjectBuilder" %>
  <%@ page import="javax.json.Json">
  <%@ page import="com.matrixone.apps.domain.DomainObject" %>
  <%@ page import="matrix.util.StringList" %>
  <%@include file = "emxNavigatorInclude.inc"%>
  <script language="javascript" type="text/javascript" src="scripts/emxUICore.js"></script>
  <%
  	JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
  	response.setHeader("Cache-Control","no-cache");
  	String strObject = emxGetParameter(request,"objectId");
  	....//其他业务代码目的是获取数据
  	StringBuffer sb = "xxx";//这里指代获取的结果
  	jsonObjectBuilder.add("result",sb.toString());
  	String str = jsonObjectBuilder.build().toString();
  	out.clear();
  	out.write(str);
  %>
  ```
