# 572项目 达索 API 使用文档

> 基于 183 个 JPO Java 源文件提取，覆盖 23 万行代码中的 API 调用模式

---

## 目录

1. [DomainObject — 对象CRUD](#1-domainobject--对象crud)
2. [MqlUtil — MQL 命令执行](#2-mqlutil--mql-命令执行)
3. [BusinessObject — Business Object 操作](#3-businessobject--business-object-操作)
4. [JPO — JPO 跨类调用](#4-jpo--jpo-跨类调用)
5. [DomainRelationship — 关系操作](#5-domainrelationship--关系操作)
6. [Context — 上下文管理](#6-context--上下文管理)
7. [PersonUtil — 人员操作](#7-personutil--人员操作)
8. [工程变更 API (ECM/ChangeAction)](#8-工程变更-api-ecmchangeaction)
9. [生命周期 API](#9-生命周期-api)
10. [Framework API (UI、权限、属性)](#10-framework-api-ui权限属性)
11. [文件与文档 API](#11-文件与文档-api)
12. [3DE 标准 REST API](#12-3de-标准-rest-api)
13. [矩阵底层 API (matrix.db.*)](#13-矩阵底层-api-matrixdb)
14. [报表与导出 API](#14-报表与导出-api)
15. [通知与邮件 API](#15-通知与邮件-api)

---

## 1. DomainObject — 对象CRUD

### 基础操作

```java
import com.matrixone.apps.domain.DomainObject;

// 创建对象
DomainObject obj = new DomainObject();
obj.createObject(context, "TypeName", "name", "revision", "policyName", "vaultName");

// 根据ID获取对象
DomainObject obj = new DomainObject(objectId);

// 设置属性
Map<String, String> attrMap = new HashMap<>();
attrMap.put("attribute_BP_ItemCoding", "PRJ-2024-001");
obj.setAttributeValues(context, attrMap);

// 获取属性值
String value = obj.getAttributeValues(context, "attributeName").getValue();

// 获取ID
String id = obj.getId(context);

// 判断对象是否存在
obj.open(context);
boolean exists = obj.isExists(context);
obj.close(context);

// 删除对象
obj.deleteObject(context);

// 修改（更新）对象
obj.setAttributeValues(context, attrMap);
```

### 属性操作（AttributeUtil）

```java
import com.matrixone.apps.common.util.AttributeUtil;

// 获取属性值
String val = AttributeUtil.getAttributeValue(context, objectId, "attributeName");

// 设置属性值
AttributeUtil.setAttributeValue(context, objectId, "attributeName", "value");
```

### 属性映射模式

实际项目中大量使用属性常量：

```java
// 定义常量
public static final String ATTR_ITEM_CODING = "attribute_BP_ItemCoding";

// 构造属性Map
Map attributeMap = new HashMap();
attributeMap.put(ATTR_AtozOperationType, operationType);
attributeMap.put(ATTR_AtozLogType, logType);
obj.setAttributeValues(context, attributeMap);
```

---

## 2. MqlUtil — MQL 命令执行

### 基础用法

```java
import com.matrixone.apps.domain.util.MqlUtil;

// 执行MQL查询
String result = MqlUtil.mqlCommand(context, "print bus $1 $2 select id dump |", objectId);

// 带参数
String result = MqlUtil.mqlCommand(context, 
    "temp query bus Person * * where \"attribute[Last Login Date] != ''\" " +
    "orderby -attribute[Last Login Date] select attribute[Last Login Date] dump |");

// 执行MQL修改
MqlUtil.mqlCommand(context, "mod bus " + objectId + " attributeName value");
```

### 常见 MQL 模式

```java
// 查询对象信息
String info = MqlUtil.mqlCommand(context, 
    "print bus $1 select attribute[***].value dump |", objectId);

// 获取关系数据
String relData = MqlUtil.mqlCommand(context,
    "expand bus $1 from rel $2 recurse to all select bus attribute[***].value dump |",
    objectId, "relationshipName");

// 创建对象（MQL方式）
MqlUtil.mqlCommand(context,
    "add bus \"TypeName\" \"Name\" \"Revision\" policy \"PolicyName\" vault \"eService Production\"");

// 建立关系
MqlUtil.mqlCommand(context,
    "connect bus $1 rel \"RelationshipName\" to bus $2", fromId, toId);

// 获取类型详情
String typeInfo = MqlUtil.mqlCommand(context, "print type \"TypeName\" select attribute dump |");
```

---

## 3. BusinessObject — Business Object 操作

### 基础用法

```java
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;

// 根据ID打开
BusinessObject bo = new BusinessObject(objectId);
bo.open(context);

// 获取属性
String value = bo.getAttributeValues(context, "attributeName").getValue();

// 设置属性
bo.setAttributeValues(context, attributeList);

// 修改
bo.modify(context, 0);

// 获取关联对象
BusinessObjectList related = bo.getRelatedObjects(context, "relationshipName", 
    "fromType", "toType", true, true, (short)0, stringList, stringList);

// 关闭
bo.close(context);

// 创建
BusinessObject newBO = BusinessObject.create(context, "TypeName", "Name", "Revision",
    "PolicyName", "VaultName");
```

### BusinessObjectWithSelect

```java
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;

// 带select查询
BusinessObjectWithSelectList bosList = 
    BusinessObjectWithSelect.getSelectableObjects(context, sqlQuery, selectList);
```

---

## 4. JPO — JPO 跨类调用

### 调用其他JPO的方法

```java
import matrix.db.JPO;

// 基本调用（无参数）
String result = (String) JPO.invoke(context, "TargetJPOName", null, 
    "methodName", null, Object.class);

// 带Map参数
Map args = new HashMap();
args.put("key1", "value1");
args.put("key2", "value2");
String result = (String) JPO.invoke(context, "TargetJPOName", null,
    "methodName", JPO.packArgs(args), String.class);

// 获取返回Map
Map resultMap = (Map) JPO.invoke(context, "TargetJPOName", null,
    "methodName", JPO.packArgs(args), Map.class);
```

### 实例：回调JPO获取值

```java
// 在表格列定义中调用JPO
// setting "function" "getTaskAssignedPerson"
// setting "program" "BPColumnValueJPO"
// setting "Column Type" "program"

// 在trigger中调用JPO
// mod policy "Route" state "Define" add trigger promote check "emxTriggerManager"
// input "BP_RouteCreatePromoteCheck BP_ElectronicSignature";
```

### 实际项目中的调用链

```java
// EL_IntegrationJPO 调用其他JPO
String result = (String) JPO.invoke(context, 
    "LAMC_ChangeManagementJPO", null,
    "createEngineeringChange", 
    JPO.packArgs(paramMap), 
    String.class);
```

---

## 5. DomainRelationship — 关系操作

### 连接和断开关系

```java
import com.matrixone.apps.domain.DomainRelationship;

// 建立关系
DomainRelationship.connect(context, fromObjectId, "relationshipName", toObjectId);

// 断开关系
DomainRelationship.disconnect(context, relationshipId);

// 获取关系ID
String relId = DomainRelationship.getRelationshipId(context, fromObjectId, 
    "relationshipName", toObjectId);
```

### 查询关系

```java
// 通过mql获取关系数据
String relInfo = MqlUtil.mqlCommand(context, 
    "expand bus " + objectId + " from rel \"RelationshipName\" " +
    "recurse to all select bus id dump |");
```

---

## 6. Context — 上下文管理

```java
import matrix.db.Context;
import com.matrixone.apps.domain.util.ContextUtil;

// 获取当前用户
String userName = context.getUser();

// 获取用户ID
String personId = ContextUtil.getCurrentPersonId(context);

// 创建匿名上下文
Context anonymousCtx = ContextUtil.getAnonymousContext();

// 系统属性读取
String host = PropertyUtil.getProperty("ematrix.server.host");

// 创建指定用户上下文
Context newContext = new Context(userName, password, host);
newContext.connect();
```

---

## 7. PersonUtil — 人员操作

```java
import com.matrixone.apps.domain.util.PersonUtil;

// 获取人员对象ID
String personId = PersonUtil.getPersonObjectID(context, "userName");

// 获取当前Context人员ID
String personId = PersonUtil.getPersonObjectId(context);

// 获取全名
String fullName = PersonUtil.getFullName(context, "userName");

// 获取人员详细信息
String email = PersonUtil.getEmail(context, personObjectId);
```

### 角色操作

```java
import com.matrixone.apps.domain.util.RoleUtil;

// 获取角色下的所有人员
StringList members = RoleUtil.getPeopleInRole(context, "roleName");

// 分配角色给人员
// 通过MQL
MqlUtil.mqlCommand(context, "mod person " + userName + " assign role " + roleName);
```

---

## 8. 工程变更 API (ECM/ChangeAction)

### ECM Core API

```java
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrder;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeRequest;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeManagement;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil;

// 创建变更单
ChangeOrder co = new ChangeOrder(objectId);
co.open(context);

// 创建变更活动
ChangeAction ca = new ChangeAction(objectId);
ca.open(context);

// 获取变更相关对象
BusinessObjectList affectedItems = ca.getRelatedObjects(context, 
    "Change Action Affected Item", "*", "*", true, true, (short)0, null, null);
```

### ChangeAction Services (高级API)

```java
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeAction;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeActionServices;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedActivity;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedChanges;
import com.dassault_systemes.enovia.changeaction.servicesimpl.ChangeActionServices;
import com.dassault_systemes.enovia.changeaction.servicesimpl.ChangeConstants;

// 获取变更服务
IChangeActionServices changeService = new ChangeActionServices();

// 获取变更对象
IChangeAction changeAction = changeService.getChangeAction(context, actionId);

// 获取提议的活动
List<IProposedActivity> activities = changeAction.getProposedActivities();

// 获取影响分析
IProposedChanges proposedChanges = changeAction.getProposedChanges();

// 设置状态
changeService.setChangeActionStatus(context, actionId, newStatus);

// 获取受影响的对象
List<IBusinessObjectOrRelationshipObject> affectedItems = 
    proposedChanges.getAffectedItems();
```

### Change Dependencies

```java
import com.dassault_systemes.enovia.changedependencies.factory.ChangeDependenciesFactory;
import com.dassault_systemes.enovia.changedependencies.interfaces.IChangeDependenciesServices;

IChangeDependenciesServices depService = 
    ChangeDependenciesFactory.getChangeDependenciesServices();
```

### ECM Util

```java
import com.matrixone.apps.change.util.ECMUtil;

// 通用的ECM工具方法
String result = ECMUtil.someMethod(context, args);
```

### 实际项目中的变更操作流程

```java
// 创建工程变更单
public String createEngineeringChange(Context context, Map args) throws Exception {
    // 1. 创建Change Order对象
    BusinessObject bo = BusinessObject.create(context, "Change Order", "ECO-001", "1",
        "ECMChangeOrderPolicy", "eService Production");
    
    // 2. 创建Change Action
    BusinessObject ca = BusinessObject.create(context, "Change Action", "ECA-001", "1",
        "ECMChangeActionPolicy", "eService Production");
    
    // 3. 关联CA到CO
    DomainRelationship.connect(context, bo.getObjectId(), 
        "Change Order Change Action", ca.getObjectId());
    
    // 4. 添加受影响对象
    DomainRelationship.connect(context, ca.getObjectId(),
        "Change Action Affected Item", affectedItemId);
    
    return bo.getObjectId();
}
```

---

## 9. 生命周期 API

```java
import com.matrixone.apps.framework.lifecycle.LifeCyclePolicyDetails;

// 获取策略详情
LifeCyclePolicyDetails policy = new LifeCyclePolicyDetails();
policy.open(context, objectId);

// 获取当前状态
String currentState = policy.getCurrentState(context);

// 判断是否可以提升
boolean canPromote = policy.canPromote(context);

// 执行提升
policy.promote(context, "checkAccess");
```

### 版本管理

```java
import com.dassault_systemes.enovia.versioning.factory.VersioningFactory;
import com.dassault_systemes.enovia.versioning.interfaces.IVersioningServices;
import com.dassault_systemes.enovia.versioning.interfaces.IRequestLastVersion;

IVersioningServices versionService = VersioningFactory.getVersioningServices(context);

// 获取最新版本
IRequestLastVersion lastVersion = versionService.getLastVersion(context, objectId);

// 大版本修订
import com.dassault_systemes.lifecycle.implementations.LifecycleServices_NewMajorRevision_NLR;
LifecycleServices_NewMajorRevision_NLR revisionService = 
    new LifecycleServices_NewMajorRevision_NLR();
```

### 成熟度提升检查

```java
// 通过MQL执行生命周期操作
MqlUtil.mqlCommand(context, "mod bus " + objectId + " promote");
```

---

## 10. Framework API (UI、权限、属性)

### 权限校验

```java
import com.matrixone.apps.domain.util.FrameworkUtil;

// 检查操作权限
boolean hasAccess = FrameworkUtil.hasAccess(context, 
    new BusinessObject(objectId), "promote");

// 获取角色权限掩码
String masks = DomainAccess.getPhysicalAccessMasks(context, objectId, "roleName");

// 创建所有权
DomainAccess.createObjectOwnership(context, objectId, org, project, access, comment, true);
```

### 框架属性

```java
import com.matrixone.apps.domain.util.FrameworkProperties;

// 读取系统属性
String host = FrameworkProperties.getProperty("ematrix.server.host");
String contextRoot = FrameworkProperties.getProperty("ematrix.contextroot");
```

### Cache管理

```java
import com.matrixone.apps.cache.CacheManager;
import com.matrixone.apps.domain.util.CacheUtil;

// 清除缓存
CacheUtil.clearCache(context, "tableName");
CacheManager.clearCache();
```

### UI组件操作

```java
import com.matrixone.apps.framework.ui.UITable;
import com.matrixone.apps.framework.ui.UIForm;
import com.matrixone.apps.framework.ui.UIUtil;

// 获取表格对象
UITable table = new UITable();
table.load(context, "tableName", "suiteKey");
String tableXml = table.getTable(context, request);

// 获取表单对象
UIForm form = new UIForm();
form.load(context, "formName", "suiteKey");
```

### 国际化

```java
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import static com.matrixone.apps.domain.util.EnoviaResourceBundle.*;

// 获取资源文件中的字符串
String label = EnoviaResourceBundle.getProperty(context, 
    "emxFramework.Attribute.BP_DegreeImportance", 
    context.getLocale());

// 对象 i18n
String name = i18nNow.getI18nString(context, "key", bundleName);
```

---

## 11. 文件与文档 API

### 文档操作

```java
import com.matrixone.apps.common.Document;
import com.matrixone.apps.common.CommonDocument;
import matrix.db.File;
import matrix.db.FileList;

// 获取文档对象
Document doc = new Document(objectId);
doc.open(context);

// 获取文件列表
FileList files = doc.getFiles(context, "generic");

// 添加文件
File file = doc.addFile(context, "fileName", filePath);

// 签入/签出
doc.checkin(context, "comment");
doc.checkout(context, "comment");
```

### 文档触发器和生命周期

```java
// 利用 DocumentTriggerJPO 实现文档操作监听
// LAMC_DocumentTrigger_mxJPO.java - 文档相关触发器

// 文档变更时自动触发
public void onDocumentPromote(Context context, String[] args) throws Exception {
    String objectId = args[0];
    Document doc = new Document(objectId);
    doc.open(context);
    // 自定义业务逻辑
}
```

---

## 12. 3DE 标准 REST API

### JSonUtilities — 导航服务

```java
import com.dassault_systemes.plm.config.webservices.JSonUtilities;
import com.dassault_systemes.plm.config.webservices.navigation_services.args.GetMultipleFilterableObjectInfoRequiredFacets;

// 获取对象详细信息（XML格式）
JsonObjectBuilder builder = Json.createObjectBuilder();
String requestJson = "{\"version\":\"1.0\",\"output\":{\"targetFormat\":\"XML\",\"withDescription\":\"YES\",\"view\":\"ALL\",\"domains\":\"ALL\"},\"pidList\":\"objectPhysicalId\"}";

GetMultipleFilterableObjectInfoRequiredFacets facets = 
    new GetMultipleFilterableObjectInfoRequiredFacets(requestJson);
builder = JSonUtilities.getMultipleFilterableObjectInfoAsJsonBuilder(context, facets);
JsonObject jsonObject = builder.build();

// 解析返回结果
JsonObject expressions = jsonObject.getJsonObject("expressions");
JsonObject expression = expressions.getJsonObject(physicalId);
String xmlContent = expression.getJsonObject("content")
    .getJsonObject("Evolution").getString("Current");
```

### PPR Rest Services

```java
import com.dassault_systemes.pprRestServices.utils.RelationNavUtilLWC;
import com.dassault_systemes.pprRestServices.utils.WebServiceContext;
import com.dassault_systemes.pprRestServices.WorkInstruction.SignOffUtil;

// 关系导航
RelationNavUtilLWC navUtil = new RelationNavUtilLWC(context);
```

### Service Base (E6W)

```java
import com.dassault_systemes.enovia.e6w.foundation.ServiceBase;
import com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil;

// E6W框架服务基类
public class LAMC_Something_mxJPO extends ServiceBase {
    public void doSomething(Context ctx) throws Exception {
        // 使用E6W上下文工具
        Context e6wContext = ContextUtil.e6wContext(ctx);
    }
}
```

### REST 服务集成

```java
import com.dassault_systemes.rest.service.lifecycle.NewBranchResource;
import com.dassault_systemes.restWebServicesBase.LogContext;
import com.dassault_systemes.restWebServicesBase.LogUtil;

// 日志工具
LogUtil.log(context, "message");
```

---

## 13. 矩阵底层 API (matrix.db.*)

### BusinessObject 底层操作

```java
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;

// 带Select查询
BusinessObjectWithSelectList list = BusinessObjectWithSelect.
    getSelectableObjects(context, "type 'TypeName'", selects);
BusinessObjectWithSelectItr itr = new BusinessObjectWithSelectItr(list);
while (itr.next()) {
    BusinessObjectWithSelect bow = itr.obj();
    String id = bow.getSelectData("id");
    String name = bow.getSelectData("name");
}
```

### 属性操作底层

```java
import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;

// 构建属性列表
AttributeList attrs = new AttributeList();
attrs.add(new Attribute("attributeName", "value"));

// 获取属性类型
AttributeType attrType = new AttributeType("attributeName");
attrType.open(context);
```

### 查询API

```java
import matrix.db.Query;
import matrix.db.QueryIterator;

// 执行查询
Query query = new Query();
query.open(context);
query.query(context, "type 'TypeName'");
QueryIterator itr = new QueryIterator(query);
while (itr.next()) {
    BusinessObject bo = itr.getObject();
}
```

### 关系底层

```java
import matrix.db.RelationshipType;
import matrix.db.RelationshipWithSelect;
import matrix.db.RelationshipWithSelectList;

// 获取关系类型信息
RelationshipType relType = new RelationshipType("relationshipName");
relType.open(context);
```

### 用户与角色

```java
import matrix.db.Person;
import matrix.db.Role;
import matrix.db.RoleList;
import matrix.db.User;
import matrix.db.UserList;

// 人员操作
Person person = new Person(personId);
person.open(context);

// 角色操作
Role role = new Role(roleId);
role.open(context);
RoleList members = role.getAssociates(context);
```

### 访问控制

```java
import matrix.db.Access;
import matrix.db.AccessList;

// 获取对象上的访问控制
AccessList accessList = new AccessList(objectId);
accessList.open(context);
AccessItr itr = new AccessItr(accessList);
while (itr.next()) {
    Access access = itr.obj();
    String grantee = access.getGrantee();
    String accessType = access.getAccessType();
}
```

### 枚举迭代器

```java
import matrix.db.ExpansionWithSelect;
import matrix.db.ExpansionIterator;

// 展开关系
ExpansionWithSelect expansion = ExpansionWithSelect.
    expandObject(context, objectId, "relationshipName", 
    "*", "*", true, true, (short)0, selectList, false, false, null);
ExpansionIterator itr = new ExpansionIterator(expansion);
while (itr.next()) {
    BusinessObjectWithSelect bo = itr.getObject();
    String relatedId = bo.getSelectData("id");
}
```

### 文件操作

```java
import matrix.db.File;
import matrix.db.FileList;

FileList files = new FileList();
BusinessObject bo = new BusinessObject(objectId);
bo.open(context);
FileList existingFiles = bo.getFiles(context, "generic");
```

---

## 14. 报表与导出 API

### Excel 导出

```java
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

// 创建Excel工作簿
Workbook workbook = new XSSFWorkbook();
Sheet sheet = workbook.createSheet("SheetName");

// 创建表头行
Row headerRow = sheet.createRow(0);
Cell cell = headerRow.createCell(0);
cell.setCellValue("Column Name");

// 样式设置
CellStyle style = workbook.createCellStyle();
style.setBorderBottom(BorderStyle.THIN);
Font font = workbook.createFont();
font.setBold(true);
style.setFont(font);

// 写入数据行
Row dataRow = sheet.createRow(rowNum);
dataRow.createCell(colNum).setCellValue("data");
```

### PDF 导出与签章

```java
// LAMC_ExportGYPDF_mxJPO - 导出PDF
// LAMC_PDFSign_mxJPO - PDF签章
// LAMC_ElectronicSignPDF_mxJPO - 电子签章
```

### XML 报表生成

```java
// 从3DE导航服务获取XML数据
String xmlData = getDerivativeXMLData(context, physicalId);
// 解析XML并生成排序结果
List<Interval> intervals = parseIntervals(xmlData);
```

---

## 15. 通知与邮件 API

```java
import com.matrixone.apps.domain.util.MailUtil;
import com.dassault_systemes.enovia.dpm.notification.NotificationBase;
import com.dassault_systemes.enovia.dpm.notification.NotificationUtil;
import com.dassault_systemes.i3dx.appsservices.notifications.nomatrix.NotifConf;
import com.dassault_systemes.i3dx.appsservices.notifications.nomatrix.NotificationBasicUtil;
import com.matrixone.enovia.bps.notifications.NotificationData;
import com.matrixone.enovia.bps.notifications.NotificationService;
import com.matrixone.enovia.bps.notifications.NotificationUtil;

// 发送邮件
MailUtil.sendMail(context, toAddress, ccAddress, subject, message);

// 发送Notification
NotificationData notifData = new NotificationData();
notifData.setToRole("roleName");
NotificationService notifService = new NotificationService();
notifService.sendNotification(context, notifData);

// 通知配置
NotifConf conf = new NotifConf();
NotificationBasicUtil.sendNotification(context, conf);
```

---

## 其他3DE标准API

### 知识工程 API (KWE)

```java
import com.dassault_systemes.knowledge_itfs.*;

IKweDictionary dict = KweInterfacesServices.getDictionary(context, "dictionaryName");
IKweList list = dict.getList("listName");
```

### 参数 API

```java
import com.dassault_systemes.parameter_interfaces.*;

IPlmParameter parameter = ParameterInterfacesServices.getParameter(context, paramId);
```

### 需求 API

```java
import com.dassault_systemes.requirements.*;

ReqServices reqService = new ReqServices(context);
```

### 分类 API

```java
import com.matrixone.apps.classification.*;

Classification classification = new Classification(objectId);
classification.open(context);
AttributeGroup group = classification.getAttributeGroup(groupName);
```

### 库管理 API

```java
import com.matrixone.apps.library.*;

Libraries library = new Libraries(objectId);
library.open(context);
LibraryCentralCommon libCommon = new LibraryCentralCommon();
```

### 产品线 API

```java
import com.matrixone.apps.productline.*;

Model model = new Model(objectId);
Model.open(context);
Product product = new Product(objectId);
```

---

> 编写说明：以上 API 模式提取自 572 项目 183 个 JPO 源文件。项目中实际的用法可能因版本差异有所不同，但调用模式保持一致。
