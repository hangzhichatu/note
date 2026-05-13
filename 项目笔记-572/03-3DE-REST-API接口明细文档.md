# 572 项目 3DE REST API 接口明细文档

> 基于源码 `G:\工作笔记\572导出\20260331\03.Prod\07.java` 提取
> 收录 572 项目部署在 3DSpace 服务端的全部 JAX-RS REST 接口
> 按功能模块分类，含请求/响应格式说明
> 注意：`{BASE}` = `https://<3DE服务器>/3dspace`

---

## 目录

1. [通用 JPO 调用接口](#1-通用-jpo-调用接口)
2. [文档业务接口（Wuqi）](#2-文档业务接口wuqi)
3. [若依集成接口（RuoYi）](#3-若依集成接口ruoyi)
4. [MES/五性一体化接口](#4-mes五性一体化接口)
5. [基础数据服务接口](#5-基础数据服务接口)
6. [SLM 推送接口](#6-slm-推送接口)
7. [任务与 OA 接口](#7-任务与-oa-接口)
8. [集成授权接口](#8-集成授权接口)
9. [Oracle 数据库操作接口](#9-oracle-数据库操作接口)
10. [MDM 主数据同步接口](#10-mdm-主数据同步接口)
11. [成品件查询接口](#11-成品件查询接口)
12. [ATOZ 集成接口](#12-atoz-集成接口)
13. [FTP 上传接口](#13-ftp-上传接口)
14. [达索原生产品结构复制接口](#14-达索原生产品结构复制接口)

---

## 1. 通用 JPO 调用接口

> 最常用的接口，Widget 开发、外部系统动态调用 JPO 都走此路由
> 注册路径：`/resources/v1/LAMC`（LAMCApiModeler）
> 注册类：`LAMCCommonServices`、`LAMCCAAServices`

### 1.1 postmethod — POST 方式调用 JPO

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/v1/LAMC/common/postmethod` |
| 方法 | `POST` |
| Content-Type | `application/json` |
| Query 参数 | `JPO` — JPO 类名（不带 `_mxJPO`）；`Method` — 方法名 |
| 请求体 | JSON 字符串，作为 JPO 方法的 `args[0]` 传入 |
| 响应 | JPO 返回的字符串（通常为 JSON） |

**请求示例：**
```
POST {BASE}/resources/v1/LAMC/common/postmethod?JPO=LAMC_RuoYiiInterface&Method=queryProductsData
Content-Type: application/json

{"model":"01"}
```

**认证方式：** 3DE `getAuthenticatedContext(req)` 自动从请求中获取认证上下文

### 1.2 getmethod — GET 方式调用 JPO

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/v1/LAMC/common/getmethod` |
| 方法 | `GET` |

其余同 postmethod。

---

## 2. 文档业务接口（Wuqi）

> 注册路径：`/resources/wuqi`（Wuqi_ServiceBase）
> 注册类：`Wuqi_IntegrationController`
> 响应格式：`AjaxResult` — `{code:200, msg:"操作成功", data:{...}}`

### 2.1 POST /custom/config — 获取文档配置信息

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/wuqi/custom/config` |
| 方法 | `POST` |
| 请求体 | `{"data":"型号名称"}` |
| 响应 | `{code:200, msg:"操作成功", data:{documentType1:[], common:[{attrEnName,attrZhName,controlType}], extend:[]}}` |
| 说明 | 获取指定型号的文档类型列表和通用属性配置，用于前端动态渲染表单 |

### 2.2 POST /custom/configExtendInfo — 获取扩展配置

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/wuqi/custom/configExtendInfo` |
| 方法 | `POST` |
| 请求体 | `{"model":"型号名","sheetName":"工作表名"}` |
| 响应 | `AjaxResult` |

### 2.3 POST /custom/createDocument — 创建文档（含文件上传）

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/wuqi/custom/createDocument` |
| 方法 | `POST` |
| Content-Type | `multipart/form-data` |
| 请求参数 | `formData`（JSON 字符串，含 Name/Title/Revision/LAMC_Model 等）；`userName`（用户名）；`plmHost`（3DE 地址）；`mainFile`（主文件，可多个）；`otherFile`（其他文件，可多个） |
| 响应 | `AjaxResult` |

### 2.4 POST /custom/getDocument — 查询文档

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/wuqi/custom/getDocument` |
| 方法 | `POST` |
| 请求体 | JSON 字符串 |
| 响应 | `AjaxResult` |

### 2.5 POST /custom/updateDocument — 更新文档

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/wuqi/custom/updateDocument` |
| 方法 | `POST` |
| 响应 | `AjaxResult`（flag>0 成功，否则失败） |

---

## 3. 若依集成接口（RuoYi）

> 注册路径：`/resources/ruoyi`（ruoyiApi）
> 注册类：domainObjectAction、domainObjectPolicyChange、synchUser3De、LAMC_sendDataToLK、LAMC_getPersonTypePersonName

### 3.1 文档操作

#### 3.1.1 POST /ruoyi/domainAction/create — 创建文档

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/domainAction/create` |
| 方法 | `POST` |
| 认证 | `admin` 用户提权 |
| 请求体 JSON 示例： | |
```json
{
  "Name": "DOC001",
  "Revision": "A",
  "Title": "文档标题",
  "owner": "admin",
  "LAMC_Model": "型号名称",
  "PBOMID": "关联PBOM ID",
  "LAMC_DocumentType3": "文档类型",
  "mainFileTitle": ["主文件1.pdf"],
  "fileSavePath": "/path/to/files/"
}
```
| 业务逻辑： | |
1. 校验文档编号是否重复
2. 创建 Document 对象（策略：Document Release）
3. 关联 PBOM（`LAMC_PBOMRelDocument` 关系）
4. 设置属性值（自动转换 Range 汉化为系统值）
5. 关联型号文件夹（Workspace 下的目录结构）
6. 签入主文件/其他文件
7. 自动发起若依审批流程
8. 生成 XML 上传 FTP（如有 PBOM 关联）

| 响应 | `{code:200, status:"success", objectId:"...", message:"创建成功！"}` |

#### 3.1.2 POST /ruoyi/domainAction/change — 修改属性

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/domainAction/change` |
| 方法 | `POST` |
| 请求体 | JSON：含对象 ID 和要修改的属性键值对 |

#### 3.1.3 GET /ruoyi/domainAction/getDomainObjectInfo — 查询对象信息

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/domainAction/getDomainObjectInfo` |
| 方法 | `GET` |
| 查询参数 | `param` — 对象 ID |
| 响应 | 对象类型、名称、版本、状态、创建信息等 |

### 3.2 生命周期策略操作

#### 3.2.1 POST /ruoyi/domainPolicy/promote — 提升

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/domainPolicy/promote` |
| 方法 | `POST` |
| 请求体 | `{"objectIds":["对象ID"], "requestId":"去重标识"}` |
| 说明 | 提升对象生命周期状态，支持幂等（requestId 去重） |

#### 3.2.2 POST /ruoyi/domainPolicy/fileSign — 非结构化文档签字

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/domainPolicy/fileSign` |
| 方法 | `POST` |
| 请求体 | `{"objectIds":["对象ID"], "signInfo":[{"name":"审批人","taskId":"taskId"}], "isSignNot":"false"}` |
| 说明 | 特殊文档类型（工装、图样等）走签审合并 PDF，其余走若依文档签字 |

#### 3.2.3 POST /ruoyi/domainPolicy/structureFileSign — 结构化文档签字

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/domainPolicy/structureFileSign` |
| 方法 | `POST` |
| 请求体 | `{"objectId":"对象ID", "signInfo":[...], "requestId":"..."}` |

#### 3.2.4 POST /ruoyi/domainPolicy/demote — 降级

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/domainPolicy/demote` |
| 方法 | `POST` |

#### 3.2.5 POST /ruoyi/domainPolicy/sendQMS — 发送 QMS

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/domainPolicy/sendQMS` |
| 方法 | `POST` |

### 3.3 数据同步接口

#### 3.3.1 GET /ruoyi/user/synchUser3DE — 用户同步

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/user/synchUser3DE` |
| 方法 | `GET` |
| 说明 | 全量同步 3DE 中所有 Active 状态的人员到若依系统 |

#### 3.3.2 GET /ruoyi/user/synchCompany3DE — 公司同步

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/user/synchCompany3DE` |
| 方法 | `GET` |
| 说明 | 同步 3DE 中的公司组织到若依 |

#### 3.3.3 GET /ruoyi/user/synchRole3DE — 角色同步

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/user/synchRole3DE` |
| 方法 | `GET` |

#### 3.3.4 GET /ruoyi/user/synchGroup3DE — 组同步

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/user/synchGroup3DE` |
| 方法 | `GET` |

### 3.4 文档归档（兰考）

#### 3.4.1 POST /DocSendData — 归档至兰考

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/DocSendData` |
| 方法 | `POST` |
| 请求体 | 解析为 Map, 含 key: `objectId` |
| 说明 | 审批完成后，调用 `LAMC_ArchiveRest` JPO 执行文件检查和归档 |

### 3.5 人员信息查询

#### 3.5.1 POST /getPersonTypePersonName — 获取人员类型和姓名

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ruoyi/getPersonTypePersonName` |
| 方法 | `POST` |
| 响应 | `{"personName":"xxx"}` |
| 说明 | 调 JPO `LAMC_DELLmiProductionSystemReference.getPersonTypePersonName` |

---

## 4. MES/五性一体化接口

> 注册路径：`/resources/LAMCRestFiveCharacter`（LAMC_WXTOPLMMODELER）
> 注册类：`LAMC_WXTOPLM`

### 4.1 GET /mesServices/processIssueReport — 获取型号分类信息

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMCRestFiveCharacter/mesServices/processIssueReport` |
| 方法 | `GET` |
| 响应 | `{"code":200, "msg":"...", "data":{...}}` |
| 说明 | 调 JPO `LAMC_DELLmiProductionSystemReference.getProduct` |

### 4.2 POST /mesServices/getProductEBOM — 查询型号 EBOM

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMCRestFiveCharacter/mesServices/getProductEBOM` |
| 方法 | `POST` |
| 说明 | 调 JPO 查询产品 EBOM 结构 |

### 4.3 POST /mesServices/sendProductDoc — 推送产品文档

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMCRestFiveCharacter/mesServices/sendProductDoc` |
| 方法 | `POST` |

### 4.4 POST /mesServices/receviceFileByFiveSystem — 接收五性文件

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMCRestFiveCharacter/mesServices/receviceFileByFiveSystem` |
| 方法 | `POST` |
| Content-Type | `multipart/form-data` |
| 请求参数 | `materialCode`（物料编码）；`materialVersion`（物料版本）；`fiveCharacter`（五性特性数据）；`file`（上传的文件） |
| 说明 | MES 系统上传五性文件到 3DE |

### 4.5 POST /mesServices/getProductInstance — 获取产品实例

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMCRestFiveCharacter/mesServices/getProductInstance` |
| 方法 | `POST` |

### 4.6 GET /mesServices/getMainInstance — 获取主界面数据

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMCRestFiveCharacter/mesServices/getMainInstance` |
| 方法 | `GET` |

---

## 5. 基础数据服务接口

> 注册路径：`/resources/Service`（BasicRestBase）
> 注册类：MBDRestService、LibraryRestService、CARestService、LAMC_ImplementInterface

### 5.1 MBD 数据服务

#### 5.1.1 GET /Service/MBDRestService/getMBDData — 获取 MBD 数据

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/Service/MBDRestService/getMBDData` |
| 方法 | `GET` |
| 说明 | 调 JPO `LAMC_InterfaceManager.getMBDData` |

#### 5.1.2 GET /Service/MBDRestService/getMBDAttrList — 获取 MBD 属性列表

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/Service/MBDRestService/getMBDAttrList` |
| 方法 | `GET` |
| 查询参数 | `OID` — 对象 ID |

### 5.2 库服务

#### 5.2.1 GET /Service/LibraryRestService/getCategory — 获取分类

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/Service/LibraryRestService/getCategory` |
| 方法 | `GET` |
| 查询参数 | `type` — 分类类型 |
| 说明 | 调 JPO `LAMC_InterfaceManager.getCategory` |

#### 5.2.2 POST /Service/LibraryRestService/getPartList — 获取零件列表

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/Service/LibraryRestService/getPartList` |
| 方法 | `POST` |
| Content-Type | `application/x-www-form-urlencoded` |
| 参数 | `OID`、`type` 及其他自定义参数 |
| 说明 | 调 JPO `LAMC_InterfaceManager.getPartList` |

### 5.3 CA 服务

#### 5.3.1 POST /Service/CARestService/getEOInfo — 获取 EO 信息

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/Service/CARestService/getEOInfo` |
| 方法 | `POST` |
| 请求体 | JSON 字符串 |
| 说明 | 调 JPO `LAMC_InterfaceManager.getEOInfo` |

#### 5.3.2 POST /Service/CARestService/getHandoverStatus — 获取交接状态

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/Service/CARestService/getHandoverStatus` |
| 方法 | `POST` |
| 说明 | 调 JPO `LAMC_InterfaceManager.getHandoverStatus` |

### 5.4 人员信息服务

#### 5.4.1 POST /getPersonInfo — 获取人员信息

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/getPersonInfo` |
| 方法 | `POST` |
| 说明 | 调 JPO `LAMC_RouYiInterface.getPersonInfo`。同一个类名 `LAMC_ImplementInterface` 在多个注册路径下都出现 |

---

## 6. SLM 推送接口

> 注册路径：`/resources/LAMC/3DEDOC`（LAMC_SLMToPLMDocModeler）
> 注册类：`LAMC_SLMToPLMDoc`

### 6.1 POST /sendToPLMDoc — SLM 推送文档到 PLM

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMC/3DEDOC/sendToPLMDoc` |
| 方法 | `POST` |
| 响应 | `{"code":200, "msg":"...", "data":{...}}` |
| 说明 | SLM 系统调用此接口将文档推送到 3DE PLM，调 JPO `LAMC_DELLmiProductionSystemReference.create3deDoc` |

---

## 7. 任务与 OA 接口

> 注册路径：`/resources/LAMC/Task`（LAMC_TaskModeler）
> 注册类：`LAMC_OnePartTaskManager`

### 7.1 POST /onePartTaskManage — 一键创建任务

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMC/Task/onePartTaskManage` |
| 方法 | `POST` |
| 说明 | 创建带部件的任务，调 JPO `LAMC_TaskCreateWithOnePart.onePartTaskManage` |

---

## 8. 集成授权接口

> 注册路径：`/resources/LAMC/3DE`（LAMC_IntegrationModeler）
> 注册类：`LAMC_IntegrationController`

### 8.1 GET /Integration/getOAuthCode — OAuth2.0 授权码流程

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMC/3DE/Integration/getOAuthCode` |
| 方法 | `GET` |
| 查询参数 | `ticket` — 授权码（首次调用时无需传，跳转 SSO 登录） |
| 说明 | 完整 OAuth2.0 Authorization Code 流程：<br>1. 无 ticket：重定向到 SSO 登录页<br>2. 有 ticket：获取 access_token → 获取用户信息 → 调用 3DE MQL 查询用户 → 跳转到 3DE 应用 |

**配置项（LAMIntegrateCustom.properties）：**
```
gateway.sso_url=SSO地址
gateway.client_id=客户端ID
gateway.client_secret=客户端密钥
gateway.getOAuthCodeUrl=本接口回调URL
gateway.loginUrl=登录后跳转URL
```

### 8.2 POST /Integration/giveAuthority — 授予权限

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMC/3DE/Integration/giveAuthority` |
| 方法 | `POST` |
| 说明 | 若依流程完成后授予 3DE 对象权限，调 JPO `LAMC_DELLmiProductionSystemReference.giveAuthority` |

---

## 9. Oracle 数据库操作接口

> 注册路径：`/resources/route3DEServices`（Auto_RouteInterfaceModeler）
> 注册类：`Auto_RouteService`
> 另外有 `ATOZ_OperateDataBase` 注册在 `/resources/ATOZ/3DE`

### 9.1 POST /route3DEServices/route/getSearchData — 路由搜索

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/route3DEServices/route/getSearchData` |
| 方法 | `POST` |
| 说明 | 若依前端搜索 3DE 业务对象数据 |

### 9.2 POST /resources/ATOZ/3DE/operateOracleDatae/operateDataBase — Oracle 操作

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ATOZ/3DE/operateOracleDatae/operateDataBase` |
| 方法 | `POST` |
| 说明 | 数据库操作 |

### 9.3 GET /resources/ATOZ/3DE/operateOracleDatae/connectOracle — Oracle 连接

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ATOZ/3DE/operateOracleDatae/connectOracle` |
| 方法 | `GET` |

### 9.4 GET /resources/ATOZ/3DE/operateOracleDatae/disConnectOracle — 断开连接

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/ATOZ/3DE/operateOracleDatae/disConnectOracle` |
| 方法 | `GET` |

---

## 10. MDM 主数据同步接口

> 注册路径：`/resources/LAMCRestWS`（LAMCRESTIntegrationApplication）
> 注册类：`LAMCRESTIntegrationMDMWS`

### 10.1 POST /mdmServices/SynchronizePersonData — 人员数据同步

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMCRestWS/mdmServices/SynchronizePersonData` |
| 方法 | `POST` |
| 说明 | MDM 同步人员信息到 3DE，调 JPO 处理 |

### 10.2 POST /mdmServices/SynchronizeOrganizationalData — 组织数据同步

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/LAMCRestWS/mdmServices/SynchronizeOrganizationalData` |
| 方法 | `POST` |

---

## 11. 成品件查询接口

> 注册路径：`/resources/FinshedPart/LAMC`（LAMCApiGetFinishedPartParent）
> 注册类：`LAMCGetFinishedPartParent`

### 11.1 GET /getFinishedPartParent — 获取成品件父节点

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/FinshedPart/LAMC/getFinishedPartParent` |
| 方法 | `GET` |
| 查询参数 | `objectId` — 成品件对象 ID |
| 说明 | 调 JPO `LAMC_MyFinishedPartJPO.getFinishedPartParent` |

---

## 12. ATOZ 集成接口

> 注册路径：`/resources/v1/LAMC2`（ATOZ_RESTIntegrationApplication）
> 注册类：`ATOZ_RESTIntegrationWS`

### 12.1 POST /Services/postmethod — 通用 JPO 调用

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/v1/LAMC2/Services/postmethod` |
| 方法 | `POST` |
| Query 参数 | `JPO`、`Method` |
| 请求体 | JSON 字符串 |

### 12.2 GET /Services/getmethod — 通用 JPO 调用

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/v1/LAMC2/Services/getmethod` |
| 方法 | `GET` |

### 12.3 CAA 库信息

**注册路径：** `/resources/CAA`（LAMC_libInformation）
**注册类：** `LAMC_libItem`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取库信息 | POST | `/getLibItem` | 调 JPO `LAMC_DELLmiProductionSystemReference.getLibItem` |

---

## 13. FTP 上传接口

> 注册路径：`/resources/lamcService`（LAMC_WebServiceModeler）
> 注册类：`LAMC_uploadFolder2Ftp`

### 13.1 POST /LAMC/upload — 上传文件夹到 FTP

| 项目 | 值 |
|------|----|
| 路径 | `{BASE}/resources/lamcService/LAMC/upload` |
| 方法 | `POST` |
| Content-Type | `multipart/form-data` |
| 说明 | 上传本地文件夹至 FTP 服务器并生成 XML |

---

## 14. 达索原生产品结构复制接口

> 注册路径：`/resources/duplicate`（达索原生 REST 扩展）
> 注册类：`DuplicateResource`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 复制结构 | POST | `/duplicate/structure` | 复制产品结构 |
| 获取选项 | POST | `/duplicate/options` | 复制选项配置 |
| 获取信息 | POST | `/duplicate/info` | 复制信息查询 |
| 能力查询 | POST | `/duplicate/capabilites` | 查询复制能力 |
| 相关对象 | POST | `/duplicate/relatedobjects` | 查询相关对象 |

---

## 附录：接口路径速查表

> 基础路径替换变量：`{BASE}` = `https://<3DE服务器>/3dspace`

| # | 功能模块 | 方法 | 完整路径 |
|---|---------|------|---------|
| 1 | 通用 JPO POST 调用 | POST | `{BASE}/resources/v1/LAMC/common/postmethod?JPO=xxx&Method=yyy` |
| 2 | 通用 JPO GET 调用 | GET | `{BASE}/resources/v1/LAMC/common/getmethod?JPO=xxx&Method=yyy` |
| 3 | 文档配置查询 | POST | `{BASE}/resources/wuqi/custom/config` |
| 4 | 文档创建（含文件） | POST | `{BASE}/resources/wuqi/custom/createDocument` |
| 5 | 若依创建文档 | POST | `{BASE}/resources/ruoyi/domainAction/create` |
| 6 | 若依修改属性 | POST | `{BASE}/resources/ruoyi/domainAction/change` |
| 7 | 若依查询对象 | GET | `{BASE}/resources/ruoyi/domainAction/getDomainObjectInfo` |
| 8 | 若依提升 | POST | `{BASE}/resources/ruoyi/domainPolicy/promote` |
| 9 | 若依签字 | POST | `{BASE}/resources/ruoyi/domainPolicy/fileSign` |
| 10 | 若依结构化签字 | POST | `{BASE}/resources/ruoyi/domainPolicy/structureFileSign` |
| 11 | 若依降级 | POST | `{BASE}/resources/ruoyi/domainPolicy/demote` |
| 12 | 若依发送 QMS | POST | `{BASE}/resources/ruoyi/domainPolicy/sendQMS` |
| 13 | 用户同步 | GET | `{BASE}/resources/ruoyi/user/synchUser3DE` |
| 14 | 公司同步 | GET | `{BASE}/resources/ruoyi/user/synchCompany3DE` |
| 15 | 角色同步 | GET | `{BASE}/resources/ruoyi/user/synchRole3DE` |
| 16 | 组同步 | GET | `{BASE}/resources/ruoyi/user/synchGroup3DE` |
| 17 | 文档归档兰考 | POST | `{BASE}/resources/ruoyi/DocSendData` |
| 18 | 人员类型查询 | POST | `{BASE}/resources/ruoyi/getPersonTypePersonName` |
| 19 | MES 产品查询 | GET | `{BASE}/resources/LAMCRestFiveCharacter/mesServices/processIssueReport` |
| 20 | MES EBOM 查询 | POST | `{BASE}/resources/LAMCRestFiveCharacter/mesServices/getProductEBOM` |
| 21 | MES 文档推送 | POST | `{BASE}/resources/LAMCRestFiveCharacter/mesServices/sendProductDoc` |
| 22 | MES 五性文件接收 | POST | `{BASE}/resources/LAMCRestFiveCharacter/mesServices/receviceFileByFiveSystem` |
| 23 | MES 实例查询 | POST | `{BASE}/resources/LAMCRestFiveCharacter/mesServices/getProductInstance` |
| 24 | MBD 数据 | GET | `{BASE}/resources/Service/MBDRestService/getMBDData` |
| 25 | MBD 属性列表 | GET | `{BASE}/resources/Service/MBDRestService/getMBDAttrList?OID=xxx` |
| 26 | 库分类 | GET | `{BASE}/resources/Service/LibraryRestService/getCategory?type=xxx` |
| 27 | 库零件列表 | POST | `{BASE}/resources/Service/LibraryRestService/getPartList` |
| 28 | EO 信息 | POST | `{BASE}/resources/Service/CARestService/getEOInfo` |
| 29 | 交接状态 | POST | `{BASE}/resources/Service/CARestService/getHandoverStatus` |
| 30 | 人员信息 | POST | `{BASE}/resources/getPersonInfo` |
| 31 | SLM 推送文档 | POST | `{BASE}/resources/LAMC/3DEDOC/sendToPLMDoc` |
| 32 | 一键创建任务 | POST | `{BASE}/resources/LAMC/Task/onePartTaskManage` |
| 33 | OAuth 授权 | GET | `{BASE}/resources/LAMC/3DE/Integration/getOAuthCode?ticket=xxx` |
| 34 | 权限授予 | POST | `{BASE}/resources/LAMC/3DE/Integration/giveAuthority` |
| 35 | 成品件父节点 | GET | `{BASE}/resources/FinshedPart/LAMC/getFinishedPartParent?objectId=xxx` |
| 36 | 路由搜索 | POST | `{BASE}/resources/route3DEServices/route/getSearchData` |
| 37 | ATOZ JPO POST | POST | `{BASE}/resources/v1/LAMC2/Services/postmethod?JPO=xxx&Method=yyy` |
| 38 | ATOZ JPO GET | GET | `{BASE}/resources/v1/LAMC2/Services/getmethod?JPO=xxx&Method=yyy` |
| 39 | 库信息查询 | POST | `{BASE}/resources/CAA/getLibItem` |
| 40 | FTP 上传 | POST | `{BASE}/resources/lamcService/LAMC/upload` |
| 41 | MDM 人员同步 | POST | `{BASE}/resources/LAMCRestWS/mdmServices/SynchronizePersonData` |
| 42 | MDM 组织同步 | POST | `{BASE}/resources/LAMCRestWS/mdmServices/SynchronizeOrganizationalData` |
| 43 | Oracle 操作 | POST | `{BASE}/resources/ATOZ/3DE/operateOracleDatae/operateDataBase` |
| 44 | Oracle 连接 | GET | `{BASE}/resources/ATOZ/3DE/operateOracleDatae/connectOracle` |
| 45 | 结构复制 | POST | `{BASE}/resources/duplicate/structure` |
