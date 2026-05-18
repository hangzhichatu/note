# 面试技术问答准备

## 使用说明

这份文档整理了三个项目涉及的核心技术知识点，按"技术 → 常见面试题 → 参考回答"的格式组织。面试前通读一遍，重点记住**黑体关键词**和**3-5句话的核心表述**即可。

---

# 一、达索 3DEXPERIENCE 平台

## 1.1 什么是 3DE？和传统 PLM 有什么区别？

**3DEXPERIENCE（3DE）** 是达索系统的新一代 PLM 平台，在传统 PLM（产品生命周期管理）基础上增加了 **3D 数据协同、社交协作、实时分析** 能力。

| 传统 PLM | 3DE |
|----------|-----|
| 主要是文档/数据管理 | 3D 模型为核心的数据协同 |
| 独立模块 | 统一的体验式平台 |
| 服务端/客户端分离 | 浏览器 + 3D 客户端混合架构 |

## 1.2 3DE 的架构是什么样的？

3DE 采用 **三层架构**：

- **表示层**：浏览器（JSP/Widget）+ CATIA 客户端
- **业务逻辑层**：JPO（Java Program Object）—— 服务端 Java 类
- **数据层**：Oracle 数据库 + 文件存储

**核心组件：**
- `3dspace` — Web 应用服务
- `3ddashboard` — Dashboard 门户
- `3dpassport` — 统一认证
- `3dsearch` — 搜索引擎
- `3dspace-cas` — CAS 客户端接入

## 1.3 什么是 Context？

Context 是 **3DE 的会话上下文**，包含用户身份、权限、语言等信息。所有 JPO 方法都带 `Context context` 参数。

**关键方法：**
```java
ContextUtil.pushContext(context);   // 推入上下文（获取权限）
ContextUtil.popContext(context);    // 弹出上下文（恢复权限）
```

**注意：** pushContext 和 popContext 必须成对调用，否则会导致 **权限泄露** 或 **NullPointerException**。

---

# 二、MQL（Matrix Query Language）

## 2.1 MQL 是什么？

MQL 是 3DE 的 **类 SQL 查询语言**，用于操作数据库中的对象。不是标准 SQL，而是达索专有的语法。

```mql
# 创建对象
add bus "TypeName" "Name" "A" policy "PolicyName" vault "eService Production";

# 查询属性
print bus <id> select attribute[AttrName] dump;

# 修改
mod bus <id> attrName newValue;

# 升级生命周期
mod bus <id> promote StateName;

# 连接关系
connect bus "fromId" rel "RelName" to "toId";
```

## 2.2 MQL 和 SQL 有什么区别？

| MQL | SQL |
|-----|-----|
| 操作 3DE 业务对象 | 操作数据库表 |
| `add bus` / `print bus` | `INSERT` / `SELECT` |
| 自动处理权限 | 需要手动处理权限 |
| 业务语义（如 promote 生命周期） | 无业务语义 |

**面试话术：** "MQL 是 3DE 的对象操作语言，不是 SQL。它直接操作业务对象而非数据库表，3DE 底层会自动处理对象的安全索引和权限。"

---

# 三、JPO（Java Program Object）

## 3.1 什么是 JPO？运行原理是什么？

JPO 是 3DE 的 **服务端 Java 类**，类似于 Servlet 中的 Service 层。

**运行流程：**
1. Java 源码 → 写入数据库 code 字段（CLOB 类型）
2. 运行时 `javac` 自动编译 → class 文件放入 `ENOVIA_INSTALL/java/custom/`
3. 通过 `JPO.invoke()` 或 Trigger 调用

**误解纠正：** JPO 源码存储在**数据库中**（`insert program` 命令写入），**不是** WEB-INF/classes。热部署时只需重新执行 MQL 注册，不需要改 WEB-INF。

## 3.2 JPO 方法签名规则

```java
public int/Map/String methodName(Context context, String[] args) throws Exception
```

- `context` — 3DE 上下文（必带）
- `args` — 参数数组（Trigger 传入 `${OBJECTID}` 等）
- `throws Exception` — 异常抛出给调用方处理

## 3.3 JPO 的调用方式有哪些？

| 调用方式 | 说明 |
|---------|------|
| **JPO.invoke()** | Java 代码中调用另一个 JPO |
| **Trigger** | Spinner 配置，生命周期事件触发 |
| **exec prog** | MQL 命令直接调用 |
| **postmethod 路由** | 前端 Widget 通过 HTTP 调用 |

## 3.4 如何在一个 JPO 中调用另一个 JPO？

```java
String result = (String) JPO.invoke(context, "ClassName", null, "methodName", args);
```

## 3.5 JPO 优势在哪？

1. **热部署** — 修改源码后重新注册即可，无需重启服务
2. **无跨域问题** — JPO 在服务端执行 HTTP 请求，不受浏览器跨域限制
3. **权限内置** — Context 自动处理权限
4. **轻量** — 不需要部署到 WEB-INF，一个 JPO 就是一个类

---

# 四、Spinner 配置体系

## 4.1 Spinner 是什么？

Spinner 是 **3DE 的配置管理工具**，通过结构化的 Excel 文件定义 Type、Attribute、Table、Form、Command、Trigger、Menu 等配置，然后一键导入到数据库。

**一句话总结：** "Spinner 就是用 Excel 管理 3DE 系统配置，不需要写代码。"

## 4.2 Spinner 支持哪些对象类型？

13 种：Type、Attribute、Relationship、Policy、**Trigger**、Table、Form、Command、Menu、Channel、Portal、Program、Expression

## 4.3 Trigger 是怎么工作的？

Trigger 分为**两层定义**：

1. **SpinnerTriggerData_ALL.xls** — 定义绑定关系：哪个 Policy 的哪个 State 的哪个事件要触发什么 Action
2. **bo_eService Trigger Program Parameters_ALL.xls** — 定义 Action 的具体参数：调哪个 JPO 的哪个方法，传什么参数

**执行流程：** 对象状态变更 → Trigger 引擎查找绑定 → 读取 Trigger 参数 → 调用 JPO 方法 → 执行业务逻辑

**Trigger 类型：**
- `promote action` — 提升时执行业务逻辑（如生成 PDF）
- `promote check` — 提升时校验（返回 1 阻止提升）
- `demote action` — 降级时执行业务逻辑
- `demote check` — 降级时校验
- `create action` — 创建时执行
- `delete action` — 删除时执行
- `modify attribute` — 属性修改时触发

## 4.4 Trigger 中为什么不能用同步的耗时操作？

**因为 Trigger 是在业务线程中同步执行的**，如果执行耗时操作（如 PDF 生成、文件上传），会阻塞业务线程，导致前端请求超时。

**解决方法：** 通过 `Job.createAndSubmit()` 提交后台异步任务。

```java
// Trigger 中：快速完成，提交 Job 后立即返回
Job job = new Job("ClassName", "methodName", args, false);
job.createAndSubmit(context);
return 0;

// Job 方法中：实际执行耗时操作
public int methodName(Context context, String[] args) {
    // PDF 生成、文件上传等耗时操作
}
```

---

# 五、Widget （3DE 前端开发）

## 5.1 3DE Widget 的开发模式是什么？

标准 Widget 采用 **AMD + Vue 3 + ElementPlus + JPO 代理** 模式。

```
模块加载: RequireJS (AMD)
前端框架: Vue 3 + ElementPlus
后端通信: WAFData.authenticatedRequest → postmethod 路由 → JPO
跨域方案: JPO 在服务端代理 HTTP 请求，前端永远只请求 3DE 同域 API
模板加载: RequireJS text! 插件
状态管理: localStorage (简单场景)
```

## 5.2 什么是 postmethod 路由？

postmethod 是 3DE 的 **统一 JPO 调用网关**，前端通过该路由动态指定要调用的 JPO 类和方法。

```javascript
// URL 格式
/3dspace/resources/v1/LAMC/common/postmethod?JPO=类名&Method=方法名

// 示例：前端调用
var url = widget.plmHost + "/3dspace/resources/v1/LAMC/common/postmethod"
    + "?JPO=LAMC_ApiDebugger&Method=sendRequest";
```

**优势：** 新增接口只需写 JPO，**不需要新增 REST 路由，不需要重启服务**。

## 5.3 前端如何做认证请求？

使用 **WAFData.authenticatedRequest()**，3DE 内置的认证请求工具。

```javascript
WAFData.authenticatedRequest(url, {
    method: "POST",
    type: "json",
    headers: {
        "Content-Type": "application/json",
        SecurityContext: widget.getValue("credential")
    },
    data: JSON.stringify(params),
    onComplete: function(response) { /* 成功 */ },
    onFailure: function(e) { /* 失败 */ },
    onPassportError: function(e) { /* 认证错误 */ },
    onTimeout: function() { /* 超时 */ }
});
```

## 5.4 遇到过哪些 Widget 开发坑？

| 坑 | 原因 | 解决 |
|----|------|------|
| template 502 | 3DE 拦截 .html 请求 | 改为 .tpl 后缀 |
| ElementPlus locale 报错 | replaceAll 不兼容 | 不传 locale 参数 |
| 闭包变量 undefined | Vue 模板不能访问闭包 | 放入 data() 中 |
| widget.plmHost undefined | 框架未自动注入 | i3DXCompassServices 手动初始化 |
| widget.getElement 返回 null | DOM 未就绪 | 用 document.getElementById 兜底 |

---

# 六、CAA C++ 二次开发

## 6.1 CAA 是什么？

CAA（Component Application Architecture）是达索提供的 **CATIA 二次开发框架**，使用 C++ 语言。它允许开发者：

- 创建自定义 CATIA 命令和工具条
- 操作 3D 模型结构树
- 读写模型属性
- 创建和修改几何特征
- 实现自动化流程

**开发环境：** CAA RADE（基于 Visual Studio 的集成开发环境）

## 6.2 CAA 和 JPO 有什么区别？

| CAA | JPO |
|-----|-----|
| CATIA **客户端侧**开发 | 3DE **服务端侧**开发 |
| C++ 语言 | Java 语言 |
| 操作 3D 几何和特征 | 操作业务数据和流程 |
| 需要安装 CATIA + RADE | 无需客户端环境 |
| 编译为 DLL | 运行时动态编译 |

## 6.3 CAA 一般用来做什么？

- **3D 结构树操作**：自动创建/修改/删除树节点
- **属性同步**：3D 模型属性 ↔ PLM 业务属性自动同步
- **自动化流程**：翻模、BOM 展开、电缆表生成
- **交互工具**：自定义命令、对话框、工具条

---

# 七、PDF 生成（Aspose）

## 7.1 Aspose 怎么用？

项目中用 Aspose.Words 做 Word 模板填充，再转成 PDF。

```java
// 1. 加载模板
Document doc = new Document(templatePath);
DocumentBuilder builder = new DocumentBuilder(doc);

// 2. 填充字段
if (builder.moveToMergeField("tag1", false, true)) {
    builder.write("填充内容");
}

// 3. 输出 PDF
doc.save(pdfFilePath, SaveFormat.PDF);
```

## 7.2 Aspose 需要 License 吗？

需要。项目中使用 `Aspose.Words.lic` 和 `Aspose.Cells.lic` 文件。没有 License 时输出的文件会有水印。

```java
if (LAMC_XXX.class.getClassLoader().getResourceAsStream("/Aspose.Words.lic") != null) {
    License license = new License();
    license.setLicense("/Aspose.Words.lic");
}
```

---

# 八、外部系统集成

## 8.1 3DE 如何与外部系统通信？

**通过 JPO 代理模式：**

```
前端 Widget → 3DE postmethod 路由 → JPO (HttpClient) → 外部系统
```

前端永远只请求 3DE 同域，没有跨域问题。JPO 在服务端用 HttpClient 调用外部系统。

## 8.2 统一的数据响应格式是什么？

```json
{
    "code": "200",
    "status": "success",
    "data": { /* 业务数据 */ },
    "Exception": null
}
```

---

# 九、通用概念

## 9.1 什么是生命周期（Policy）？

Policy 定义业务对象的**状态和状态间转换规则**。典型的生命周期：

```
Create → In Work → Review → Approved → Released → Obsolete
```

每个状态转换（promote/demote）可以通过 Trigger 绑定业务逻辑。

## 9.2 什么是 VPMReference？

VPMReference 是 3DE 中 **物理产品**的表示，对应 CAD 模型在 PLM 中的管理对象。

**创建方式（3DE REST API）：**
```java
POST /resources/v1/collabServices/authoring/createContent/Create
Body: { "create": [{ "type": "VPMReference", "interfaces": [...], "attributes": {...} }] }
```

## 9.3 什么是 Spinner tsvector？有什么用？

在 572 项目的 MQL 部署脚本中看到类似配置。🔄 如有面试问到可如实说明"这个具体场景下未直接接触，但了解它是 3DE 全文索引的功能。"

---

# 十、面试常见问题应变

## 遇到不会的问题怎么办？

**面试话术：** "这个具体场景我没有直接遇到过，但根据我的理解，它的原理大概是……（按你的知识框架推演）。在 572 项目中，我们是通过另一种方式解决的……"

**核心原则：** 不懂的不要硬编，但可以展示你的**分析能力和技术广度**。

## 举例：被问到没接触过的 3DE 功能

"这个功能我项目中没有用到，但我理解 3DE 的平台设计思路是……（讲你会的相关概念）。如果需要实现，我会先从以下几个方面入手：查 MQL 手册、看 Spinner 配置是否支持、参考 572 项目已有的类似 Pattern。"

---

> **总结：** 面试官通常不会问特别底层的细节，更多是问**架构思路**和**解决问题的方法**。
> 重点记住：JPO 热部署原理、Trigger 双层绑定机制、JPO 代理跨域方案、Widget 模板架构——这四个能讲清楚，基本可以应对 80% 的 3DE 相关问题。
