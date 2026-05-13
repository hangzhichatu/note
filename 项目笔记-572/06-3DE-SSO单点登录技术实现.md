# 3DE 对外提供 SSO 单点登录 —— 技术实现笔记

> 场景：外站平台（企业门户/第三方系统）需要用户用 3DE 账号登录，免去重复注册和密码记忆。
> 本质：3DE 的 3DPassport 作为 OAuth2.0 授权服务器，外部系统作为客户端接入。

---

## 目录

1. [架构概览](#1-架构概览)
2. [OAuth2.0 授权码流程详解](#2-oauth20-授权码流程详解)
3. [3DE 端配置](#3-3de-端配置)
4. [服务端接口实现（JAX-RS）](#4-服务端接口实现jax-rs)
5. [外站平台客户端实现示例](#5-外站平台客户端实现示例)
6. [CAS 协议备选方案](#6-cas-协议备选方案)
7. [常见问题排查](#7-常见问题排查)
8. [参考源码（572 项目）](#8-参考源码572-项目)

---

## 1. 架构概览

### 1.1 角色说明

| 角色 | 在 3DE SSO 中对应 | 说明 |
|------|-------------------|------|
| **资源拥有者（Resource Owner）** | 3DE 用户 | 拥有 3DE 账号的人 |
| **客户端（Client）** | 外站平台 | 需要验证用户身份的第三方系统 |
| **授权服务器（Authorization Server）** | **3DPassport** | 负责用户认证、颁发授权码和令牌 |
| **资源服务器（Resource Server）** | **3DSpace** | 持有用户数据和业务资源 |

### 1.2 数据流图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        OAuth2.0 Authorization Code                    │
│                                                                       │
│  外站平台                  3DPassport                   3DSpace       │
│  (Client)                 (Auth Server)               (Resource)     │
│     │                         │                          │           │
│     │  ──① 用户访问外站 ──▶   │                          │           │
│     │    未登录，跳转          │                          │           │
│     │                         │                          │           │
│     │  ◀──② 重定向到登录页 ──  │                          │           │
│     │    ?client_id=xxx       │                          │           │
│     │    &redirect_uri=xxx    │                          │           │
│     │    &response_type=code  │                          │           │
│     │                         │                          │           │
│     │    [用户输入账号密码]     │                          │           │
│     │                         │                          │           │
│     │  ◀──③ 授权码 (code) ──  │                          │           │
│     │    redirect_uri?code=   │                          │           │
│     │                         │                          │           │
│     │  ──④ POST 换 token ──▶  │                          │           │
│     │    code + client_id     │                          │           │
│     │    + client_secret      │                          │           │
│     │                         │                          │           │
│     │  ◀──⑤ access_token ────  │                          │           │
│     │                         │                          │           │
│     │  ──⑥ 查用户信息 ──────▶  │                          │           │
│     │    Authorization:       │                          │           │
│     │    Bearer {token}       │                          │           │
│     │                         │                          │           │
│     │  ◀──⑦ 用户身份信息 ────  │                          │           │
│     │    {id, name, email}    │                          │           │
│     │                         │                          │           │
│     │  ──⑧ 凭证访问 3DE ─────────────────────────────────▶│           │
│     │    带上 token 调 API    │                          │           │
│     │                         │                          │           │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. OAuth2.0 授权码流程详解

### 步骤说明

#### ① 用户访问外站平台

用户打开外站平台的页面，会话过期或未登录。

#### ② 重定向到 3DPassport 登录页

外站平台将用户浏览器重定向到：

```
https://{3DPassport_HOST}/oauth2.0/authorize?
    client_id={CLIENT_ID}&
    redirect_uri={CALLBACK_URL}&
    response_type=code
```

参数说明：

| 参数 | 说明 | 示例 |
|------|------|------|
| `client_id` | 在 3DPassport 注册的客户端 ID | `portal_app` |
| `redirect_uri` | 登录成功后回调地址（需提前注册） | `https://portal.xxx.com/callback` |
| `response_type` | 固定为 `code` | `code` |
| `scope` | （可选）权限范围 | `openid profile` |

#### ③ 用户登录，获取授权码

用户在 3DPassport 登录页输入 3DE 账号密码，认证通过后，3DPassport 将用户浏览器重定向回外站平台的 `redirect_uri`，并在 URL 上附带授权码：

```
https://portal.xxx.com/callback?code={AUTHORIZATION_CODE}
```

#### ④ 外站平台用授权码换令牌

外站平台后端发起服务端请求（**注意：这一步不能在前端做，client_secret 不能暴露**）：

```
POST https://{3DPassport_HOST}/oauth2.0/accessToken
Content-Type: application/x-www-form-urlencoded

client_id={CLIENT_ID}
&client_secret={CLIENT_SECRET}
&grant_type=authorization_code
&code={AUTHORIZATION_CODE}
&redirect_uri={CALLBACK_URL}
```

#### ⑤ 获取 access_token

3DPassport 返回：

```json
{
  "access_token": "eyJhbGciOi...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "..." 
}
```

#### ⑥ 用 token 查询用户信息

```
GET https://{3DPassport_HOST}/oauth2.0/profile
Authorization: Bearer {access_token}
```

#### ⑦ 获取用户身份

```json
{
  "id": "zhangsan",
  "name": "张三",
  "email": "zhangsan@company.com",
  "preferred_username": "zhangsan_PRJ"
}
```

#### ⑧ 建立本地会话，访问 3DSpace

外站平台验证用户身份后，建立自己的会话。后续可通过这个用户的上下文调 3DSpace 的 REST API。

---

## 3. 3DE 端配置

### 3.1 外站平台的注册

在 3DPassport 管理后台注册外部应用，获取 `client_id` 和 `client_secret`。注册时需提供 `redirect_uri`（外站平台回调地址）。

### 3.2 enovia.properties 配置

SSO 相关的配置通常放在自定义 properties 文件中（如 `LAMIntegrateCustom.properties` 或 `emxFrameworkCustom.properties`）：

```properties
# SSO 服务地址（3DPassport 的域名）
gateway.sso_url=https://sso.company.com

# 外站平台的客户端 ID
gateway.client_id=portal_app_001

# 客户端密钥
gateway.client_secret=aBcDeFgHiJkLmNoPqRsT

# 获取授权码的回调地址（外站平台接收 code 的 URL）
gateway.getOAuthCodeUrl=https://portal.company.com/callback

# 登录成功后跳转到 3DE 的链接模板（可选）
gateway.loginUrl=https://3de.company.com/3dspace?user=

# 错误提示前缀和后缀（用于登录失败时显示）
gateway.logErrorPrefixMsg=用户
gateway.logErrorSuffixMsg=在3DE中不存在
```

### 3.3 enovia.ini 中 3DPassport URL 确认

确保 `enovia.ini` 中的 `PASSPORT_URL` 配置正确：

```ini
PASSPORT_URL=https://3de.company.com/3dpassport
```

---

## 4. 服务端接口实现（JAX-RS）

### 4.1 完整实现（参考 572 项目源码）

```java
package com.lamc.web.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.dassault_systemes.platform.restServices.RestService;
import com.lamc.webservice.util.Auto_PageCommon;
import com.lamc.webservice.util.PropertiesUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import matrix.db.Context;
import matrix.db.JPO;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Path("/Integration")
public class LAMC_IntegrationController extends RestService {

    /**
     * OAuth2.0 授权码流程入口
     * 
     * 第一步（无 ticket）：重定向到 3DPassport 登录页
     * 第二步（有 ticket）：换 token → 查用户 → 跳转 3DSpace
     */
    @GET
    @Path(value = "/getOAuthCode")
    public Response getOAuthCode(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            String dataJson) throws Exception {
        
        // 读取 SSO 配置
        String sso_url = PropertiesUtil.getPropertyValue(
            Auto_PageCommon.pageFile, "gateway.sso_url");
        String client_id = PropertiesUtil.getPropertyValue(
            Auto_PageCommon.pageFile, "gateway.client_id");
        String app_url = PropertiesUtil.getPropertyValue(
            Auto_PageCommon.pageFile, "gateway.getOAuthCodeUrl");
        String client_secret = PropertiesUtil.getPropertyValue(
            Auto_PageCommon.pageFile, "gateway.client_secret");
        
        String ticket = request.getParameter("ticket");
        
        if (ticket != null) {
            // 【第二步】有授权码 → 换 access_token → 查用户 → 登录
            try {
                // 4.1 用授权码换 access_token
                String accessTokenUrl = sso_url + "/oauth2.0/accessToken"
                    + "?client_id=" + client_id
                    + "&client_secret=" + client_secret
                    + "&grant_type=authorization_code"
                    + "&code=" + ticket
                    + "&redirect_uri=" + URLEncoder.encode(app_url, "UTF-8");
                
                String body = makeHttpRequest(accessTokenUrl, "GET");
                JSONObject object = JSON.parseObject(body);
                
                if (object != null && object.containsKey("access_token")) {
                    String access_token = object.getString("access_token");
                    
                    // 4.2 用 token 查用户信息
                    String userInfoUrl = sso_url + "/oauth2.0/profile"
                        + "?access_token=" + access_token;
                    String userInfoBody = makeHttpRequest(userInfoUrl, "GET");
                    JSONObject personInfo = JSON.parseObject(userInfoBody);
                    
                    if (personInfo != null && personInfo.containsKey("id")) {
                        String personName = personInfo.getString("id");
                        
                        // 4.3 验证用户在 3DE 中是否存在
                        Context context = ContextUtil.getAnonymousContext();
                        String personId = MqlUtil.mqlCommand(context,
                            "temp query bus Person " + personName
                            + " * select id dump", true);
                        
                        if (StringUtils.isEmpty(personId)) {
                            // 用户不存在 → 返回错误
                            String logErrorPrefixMsg = PropertiesUtil
                                .getPropertyValue(Auto_PageCommon.pageFile,
                                    "gateway.logErrorPrefixMsg");
                            String logErrorSuffixMsg = PropertiesUtil
                                .getPropertyValue(Auto_PageCommon.pageFile,
                                    "gateway.logErrorSuffixMsg");
                            String result = logErrorPrefixMsg
                                + personName + logErrorSuffixMsg;
                            return Response.ok(result)
                                .type(MediaType.TEXT_PLAIN_TYPE
                                    .withCharset("UTF-8"))
                                .build();
                        }
                        
                        // 4.4 跳转到 3DE
                        String logUrl = PropertiesUtil
                            .getPropertyValue(Auto_PageCommon.pageFile,
                                "gateway.loginUrl") + personName;
                        response.sendRedirect(logUrl);
                    }
                }
            } catch (Exception e) {
                response.sendError(500, e.getMessage());
            }
        } else {
            // 【第一步】无授权码 → 跳转到 3DPassport 登录页
            String authUrl = sso_url + "/oauth2.0/authorize"
                + "?client_id=" + client_id
                + "&redirect_uri=" + URLEncoder.encode(app_url, "UTF-8")
                + "&response_type=code";
            response.sendRedirect(authUrl);
        }
        return Response.ok().build();
    }

    /**
     * 通用 HTTP GET 请求工具
     */
    public static String makeHttpRequest(String requestUrl, String method)
            throws Exception {
        URL url = new URL(requestUrl);
        HttpURLConnection connection =
            (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException(
                "Failed: HTTP error code: " + responseCode);
        }

        BufferedReader in = new BufferedReader(
            new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}
```

### 4.2 ModelerBase 注册

```java
package com.lamc.web.api;

import com.dassault_systemes.platform.restServices.ModelerBase;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/resources/LAMC/3DE")
public class LAMC_IntegrationModeler extends ModelerBase {
    @Override
    public Class<?>[] getServices() {
        return new Class<?>[]{
            LAMC_IntegrationController.class
        };
    }
}
```

部署后接口路径：

```
GET {BASE}/resources/LAMC/3DE/Integration/getOAuthCode?ticket=xxx
```

---

## 5. 外站平台客户端实现示例

### 5.1 前端触发 SSO 登录

```javascript
// 外站前端：点击"3DE 账号登录"按钮
function loginWith3DE() {
    // 重定向到 3DE SSO 入口
    window.location.href =
        'https://3de.company.com/3dspace/resources/LAMC/3DE/Integration/getOAuthCode';
}
```

### 5.2 后端处理回调（Java 示例）

```java
// 外站后端：处理 3DPassport 的回调
@GetMapping("/callback")
public String handleCallback(@RequestParam("code") String code) {
    // 1. 用 code 换 token（服务端通信，client_secret 安全）
    String tokenUrl = "https://3de.company.com/3dpassport/oauth2.0/accessToken";
    String response = httpPost(tokenUrl, 
        "client_id=" + CLIENT_ID +
        "&client_secret=" + CLIENT_SECRET +
        "&grant_type=authorization_code" +
        "&code=" + code +
        "&redirect_uri=" + CALLBACK_URL);
    
    JSONObject tokenResp = JSON.parseObject(response);
    String accessToken = tokenResp.getString("access_token");
    
    // 2. 用 token 查用户
    String userUrl = "https://3de.company.com/3dpassport/oauth2.0/profile";
    String userResp = httpGet(userUrl, 
        "Authorization: Bearer " + accessToken);
    JSONObject userInfo = JSON.parseObject(userResp);
    
    String username = userInfo.getString("id");
    
    // 3. 建立外站平台自己的会话
    session.setAttribute("currentUser", username);
    session.setAttribute("token", accessToken);
    
    return "redirect:/dashboard";
}
```

### 5.3 外站通过 token 调 3DSpace API

```java
// 外站服务端：带着 token 调 3DSpace REST 接口
String apiUrl = "https://3de.company.com/3dspace/resources/v1/LAMC/common/getmethod"
    + "?JPO=XXX&Method=YYY";

HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
conn.setRequestProperty("Authorization", "Bearer " + accessToken);
// 或者使用 SecurityContext header
conn.setRequestProperty("SecurityContext", "VPLMAdmin.Company Name.Default");
```

---

## 6. CAS 协议备选方案

如果外站平台不支持 OAuth2.0，3DPassport 也支持 CAS 协议。

### CAS 流程

```
1. 外站检测未登录 → 302 跳转到 3DPassport CAS 登录页
   https://{PASSPORT_URL}/cas/login?service={SERVICE_URL}

2. 用户登录 → 3DPassport 验证凭证
   → 302 回 service?ticket={ST-xxx}

3. 外站用 ticket 验证
   GET https://{PASSPORT_URL}/cas/serviceValidate
       ?service={SERVICE_URL}
       &ticket={ST-xxx}
   
4. 返回 XML 格式的用户信息
   <cas:serviceResponse>
     <cas:authenticationSuccess>
       <cas:user>zhangsan</cas:user>
     </cas:authenticationSuccess>
   </cas:serviceResponse>
```

---

## 7. 常见问题排查

### 7.1 跳转到 3DPassport 后 404

```
原因：enovia.ini 中 PASSPORT_URL 配置错误
排查：确认 https://{PASSPORT_URL}/oauth2.0/authorize 能否直接访问
```

### 7.2 授权码换 token 失败

```
原因：client_secret 不匹配 或 redirect_uri 未注册
排查：重新在 3DPassport 管理后台注册外站应用
```

### 7.3 用户信息查询失败

```
原因：access_token 过期 或 用户无权限
排查：检查 token 有效期（默认 3600s），确认用户在 3DE 中存在
```

### 7.4 回调地址不能是 HTTP

```
3DPassport 要求回调地址必须是 HTTPS，否则会拒绝请求
```

### 7.5 3DE 中查不到用户

```
通过 MQL 确认用户是否在 Person 类型中：
  temp query bus Person {username} * select id dump

若用户不存在，需要先在 3DE 中创建用户或走用户同步
```

---

## 8. 参考源码（572 项目）

| 文件 | 路径 |
|------|------|
| SSO REST 接口 | `G:\工作笔记\572导出\20260331\03.Prod\07.java\3dsapce\src\main\java\com\lamc\web\api\LAMC_IntegrationController.java` |
| ModelerBase 注册 | `G:\工作笔记\572导出\20260331\03.Prod\07.java\3dsapce\src\main\java\com\lamc\web\api\LAMC_IntegrationModeler.java` |
| Properties 工具 | `G:\工作笔记\572导出\20260331\03.Prod\07.java\3dsapce\src\main\java\com\lamc\webservice\util\PropertiesUtil.java` |
| SSO 配置文件 | `emxFrameworkCustom.properties`（gateway.* 配置项）|
| enovia.ini | `G:\工作笔记\3DE系统配置相关\配置jar包基础路径\enovia.ini` |
