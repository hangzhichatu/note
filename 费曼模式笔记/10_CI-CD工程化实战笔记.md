# CI/CD 工程化实战笔记

标签: #CI/CD #DevOps #GitHubActions #Docker #SpringBoot #Java面试
创建: 2026-07-01
最近更新: 2026-08-14
学习目标: 形成企业级 CI/CD 工程化系统思维，应对 Java 技术面试
前置基础: Git 会用，Docker 部署过若依微服务

---

## 学习计划（进度总表）

| 阶段 | 内容 | 状态 | 日期 |
|------|------|------|------|
| 1 | 准备 Spring Boot 项目 + Dockerfile | ✅ 完成 | 2026-07-01/08-14 |
| 2 | GitHub Actions 自动构建与测试 | ⏳ 待开始 | |
| 3 | Docker 镜像构建与推送 | ✅ 半完成（本地构建OK，推送待做）| 2026-08-14 |
| 4 | 多环境部署策略 | ⏳ 待开始 | |
| 5 | 简历包装与面试话术 | ⏳ 待开始 | |

> 主线推荐：**阶段2（GitHub Actions）** 是下一步，把"手动链路 → 自动流水线"补完。

---

## 项目路径 & 结构

**本地项目：** `D:\cicd-learn`（Spring Boot 3.2.5 + Java 17 + Web + /health接口）
**部署目标：** 虚拟机 `/root/cicd-learn/`
- `src/` — 源码
- `pom.xml` — Maven 配置（finalName=cicd-learn，spring-boot-maven-plugin）
- `Dockerfile` — 镜像构建脚本
- `target/cicd-learn.jar` — 本地 Maven 构建产物（fat jar）

### Dockerfile（最终有效版——VM 用）

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY cicd-learn.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> ⚠️ **关键坑**：`COPY` 路径是**相对构建上下文**的。
> - 本机 Maven 产物在 `target/cicd-learn.jar` → 本机 Dockerfile 写 `COPY target/...`
> - 但我只把 jar 和 Dockerfile 平级传到 VM → VM 上必须写 `COPY cicd-learn.jar`（不能带 target/）
> - **教训：Dockerfile 路径跟着"文件实际放哪"走，不是跟着"本机结构"走**

---

## 当前进度（2026-08-14 已跑通完整最小链路）

### ✅ 步骤1：Maven 构建（本机）
```bash
cd D:\cicd-learn
mvn clean package
```
结果：**BUILD SUCCESS**
- 跑 1 个测试，0 失败
- 产物 `target/cicd-learn.jar`（spring-boot repackage 成 fat jar，依赖打进 BOOT-INF）
- ⚠️ **PowerShell 不认 `&&`**，要用分号 `;` 或分两行

### ✅ 步骤2：上传 VM
- 通过 **FTP** 传 `cicd-learn.jar` + `Dockerfile` 到 VM `/root/cicd-learn/`（平级，无 target/ 层的目录结构）

### ✅ 步骤3：构建镜像（VM）
```bash
cd /root/cicd-learn
docker build -t cicd-learn:1.0.0 .
```
结果：`Successfully tagged cicd-learn:1.0.0`，镜像 **203MB**

### ✅ 步骤4：启动容器（VM）
```bash
docker run -d --name cicd-learn -p 8080:8080 cicd-learn:1.0.0
docker logs cicd-learn    # 看 Started CicdLearnApplication
curl http://localhost:8080/health   # 验证接口
```
结果：**全通** ✅

---

## 已跑通链路（最小可行版 · 全手动）

```
mvn clean package(本地编译+测试) → FTP上传 jar+Dockerfile
→ docker build(拉基础镜像+打镜像) → docker run(起容器) → /health 验证
```

**CI/CD 的全部意义 = 把这套手工链路变成"代码一提交就自动跑"。**
下一步（阶段2）就是用 GitHub Actions 替换其中的手动步骤。

---

## 核心知识：镜像分层 & 基础镜像（2026-08-14）

### Docker build 自动找 Dockerfile
- 末尾 `.` = 构建上下文（当前目录）
- 默认找该目录下 `Dockerfile` 文件
- 指定别的文件名用 `-f`：`docker build -f docker/Dockerfile.dev .`

### 基础镜像与 JDK（关键认知）
- `FROM eclipse-temurin:17-jre-alpine`：**基础镜像内已装好 JRE**，Dockerfile 不需要"下载 JDK"
- **不是每个容器下载一份 JDK，而是分层共享**：
  - 基础镜像本机拉过一次就缓存一份
  - 之后所有基于它的镜像/容器复用，不再下载
- **分层机制**：镜像 = 基础层 + 你的代码层（COPY）。改代码只重打代码层，基础层直接复用 → 重新 build 很快
- **类比**：基础镜像 ≈ 电脑装好的 JDK，10 个 Java 程序共用一份，不是各装各的

---

## 核心知识：JRE vs JDK & 线上排障（2026-08-14 · 面试金句）

### 为什么运行用 JRE 不用 JDK
- 运行期只执行字节码，用不到编译工具 `javac`/`jar`
- JRE 镜像更小、更省磁盘、更安全（没有编译工具被攻击利用）
- "构建用 JDK、运行用 JRE"的分离哲学

### 镜像瘦身 ≠ 阉割诊断能力（OOM 的正确答案）
- **OOM 时容器内工具可能根本跑不起来**（内存已被吃爆，jmap 也 OOM）
- **正解：JVM 自动 dump 黑匣子，事后用本机大工具分析**
- 给 JVM 加参数，OOM 瞬间自动 dump：
```dockerfile
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m",
  "-XX:+HeapDumpOnOutOfMemoryError",
  "-XX:HeapDumpPath=/dumps/heap.hprof",
  "-jar", "app.jar"]
```
- dump 的 `.hprof` 用 **MAT/JProfiler（本机）** 分析，不在容器里

| 工具 | 在哪跑 | 作用 |
|---|---|---|
| jmap/jstack/jcmd | 容器内 | 现场看一眼 |
| MAT/JProfiler | 自己电脑 | 分析 hprof |
| JFR/async-profiler | 进程外采样 | 动态性能分析 |

### 生产瘦 / 调试胖 双镜像策略
- 平时跑 `17-jre-alpine` 瘦镜像
- 疑难杂症构建一个 `-debug` 镜像（完整 JDK，带 jmap/jstack/bash），临时替换采集，完事换回
- **代码一样，只是基础镜像换大** → 兼顾安全与可诊断性

### 面试金句
> "镜像瘦身和可诊断性不是对立的。生产用 JRE 镜像保证安全和小体积；配合 `HeapDumpOnOutOfMemoryError`、`JFR` 自动采集，OOM 现场自动 dump 到宿主机，事后用本机 MAT 分析，不需要容器内工具。疑难场景用同代码 debug 镜像（完整 JDK）临时替换采集。"

---

## 核心知识：AI 辅助分析 .hprof（2026-08-14）

### 现状：直接喂 .hprof 给 LLM 是伪命题
- hprof 是二进制大文件（几百MB~几GB），LLM 上下文装不下
- 正确姿势 = **AI + 传统工具分工**，AI 分析"人话报告"而非原始二进制

### 可落地工作流（现在就可用）
1. JVM 配好 `HeapDumpOnOutOfMemoryError` → 自动 dump `heap.hprof`
2. 本机用 **MAT** 打开，导出 `Leak Suspects Report`
3. 把报告关键段落 + 疑问丢给 AI → AI 给业务人话结论 + 修复建议
4. 需要动态看 → 用 **JFR** 采样（结构化输出更适合 AI）

### 结论
- 市面上工具"不好用"多半卡在 MAT/JProfiler 的专家 UI
- 有 AI 解读后，只需会 MAT "导出报告"一个动作，解读交给 AI
- 进阶方向：JFR 比 hprof 更适合 AI 分析（可捕捉静态快照看不到的动态泄漏）

---

## VM 环境备忘（顺手发现）

这台部署 VM 上已容器化跑着整套中间件（后续学 MQ/Nacos 可直接实操）：
- `rmqbroker` / `rmqnamesrv` — RocketMQ 5.3.2（端口 9876/10909/10911-10912）
- `nacos` — nacos-server v2.1.1（端口 8848/9848-9849）
- `mysql` — mysql:8.0.36（端口 3306）
- `redis` — redis:5.0.14（端口 6379）

---

## 待办 / 待续

- [ ] **阶段2：GitHub Actions** 自动 build + test（主线，下一步做）
- [ ] 阶段3 补全：镜像推送（推 Docker Hub / 私有仓库）
- [ ] 阶段4：多环境部署策略（dev/staging/prod）
- [ ] 阶段5：简历包装 + 面试话术
- [ ] 可选：加 `.dockerignore`（本次 build context 发了 19.77MB，太大，该排除 target/）
- [ ] 可选：把 OOM 诊断参数配置进 Dockerfile 验证 dump 机制
- [ ] 可选：docker-compose 编排 — 把 cicd-learn 加入 VM 现有 RMQ/Nacos/MySQL 编排

---

## 面试高频考点备忘录

- [ ] Docker 分层机制（基础层共享、改代码只重打代码层）
- [ ] Dockerfile COPY 路径是相对构建上下文的（本机 target/ vs VM 平级）
- [ ] FROM 基础镜像自带运行环境，不需要 Dockerfile 下载 JDK
- [ ] JRE vs JDK 为什么生产用 JRE（构建用 JDK / 运行用 JRE）
- [ ] OOM 黑匣子：HeapDumpOnOutOfMemoryError + 本机 MAT 分析
- [ ] 生产瘦/调试胖双镜像策略
- [ ] .dockerignore 减少 build context
- [ ] PowerShell 不认 &&，用分号
