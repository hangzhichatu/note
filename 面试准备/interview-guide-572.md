# 面试项目介绍 — 572 项目

## 一句话介绍

达索 3DEXPERIENCE 平台上的 PLM 二次开发项目，为航空航天制造企业实现工程变更管理、文档管理和制造数据管理的全流程数字化。

---

## 项目信息

| 项目 | 内容 |
|------|------|
| **项目名称** | 572 厂 PLM 项目 |
| **客户** | 某航空航天制造企业 |
| **平台** | 达索 3DEXPERIENCE |
| **技术栈** | 3DE JPO + Spinner + Vue3 Widget + Aspose + 若依 |
| **我的角色** | 3DE 二次开发工程师（全栈） |
| **团队规模** | 10+ 人 |
| **项目周期** | 长期 |

---

## 技术栈

| 技术 | 用途 |
|------|------|
| 3DE MQL / JPO | 服务端业务逻辑（185 个 JPO 文件） |
| Spinner | 系统配置管理（13 种对象类型） |
| Vue 3 + ElementPlus | 前端 Widget |
| Apache HttpClient | JPO 中代理 HTTP 请求 |
| Aspose.Words / Cells | Word/Excel 文档生成和 PDF 输出 |
| 若依 (RuoYi) | 流程引擎和数据管理 |
| Oracle | 数据库 |

---

## 我负责的工作

### 1. JPO 后端开发
- 开发和维护了 20+ 个核心 JPO（工程变更文档管理、MES 接口、PDF 生成等）
- 实现 Trigger 驱动的自动 PDF 生成（EO/ECR/ECN 变更单提交时自动生成签章 PDF）
- 实现 JPO 代理模式，解决 3DE 前端跨域问题

### 2. Spinner 系统配置
- 负责 Type/Table/Form/Command/Trigger 的 Spinner 配置
- 独立开发 SpinnerConfigTool（配置可视化工具，支持 13 种对象类型的导入编辑导出）
- 维护 10+ 个 Trigger 配置（生命周期 promote/demote 触发业务逻辑）

### 3. 前端 Widget 开发
- 基于 Vue 3 + ElementPlus 开发标准 Widget 模板
- 开发 LAMC_ApiDebugger（Postman 调试工具，在 3DE 内直接测试外部接口）
- 开发 LAMC_SpinnerConfigTool（Spinner 配置在线管理工具）

### 4. 外部系统集成
- 若依系统接口对接（流程引擎、数据同步）
- MES 接口对接（制造数据同步）
- PDF 电子签名系统集成

---

## 项目亮点

### 亮点 1：统一的 JPO 代理网关

设计了标准化的 JPO 调用网关（`LAMC_ApiGateway`），所有外部接口通过 `POST /postmethod?JPO=XXX&Method=xxx` 统一路由，新增接口只需写 JPO，无需新增 REST 路由，无需重启服务。

### 亮点 2：文档 PDF 全自动生成

通过 Trigger + Job 异步任务，在 EO/ECR/ECN 变更单提交时自动生成带签章的 PDF 文件，使用 Aspose 进行模板填充，整个过程无人值守。

### 亮点 3：Spinner 配置可视化

将 13 种 Spinner 对象类型（Type/Attribute/Table/Form/Command/Trigger 等）的 XLS 配置管理做成在线工具，不用手动编辑 Excel，直接在 3DE 中可视化编辑。

### 亮点 4：Widget 标准化模板

沉淀了一套标准的 AMD + Vue 3 + ElementPlus Widget 模板，包含认证请求封装、通知组件、字符串工具、部署检查清单等，新 Widget 开发时间从 3 天缩短到 1 天。
