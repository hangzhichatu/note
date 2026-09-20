# Spring MVC 一次请求的完整流程（DispatcherServlet 全链路）

> 面试必考、且常跟"拦截器/过滤器/ThreadLocal"一起问。这篇讲清一次 HTTP 请求从进来到回去的每一步。

---

## 一、一句话本质

> **Spring MVC 的核心是前端控制器 DispatcherServlet——所有请求都先进它，由它统一调度"谁来处理、怎么处理、结果怎么返回"。** 它把"接收请求 → 找到处理器 → 执行 → 渲染响应"串成一条标准流水线。

---

## 二、完整流程（9 步，背诵版）

```
客户端请求
   │
   ▼
① 过滤器链 Filter（如登录校验、编码、CORS）— web层，DispatcherServlet之前
   │
   ▼
② DispatcherServlet 接收请求（前端控制器，总入口）
   │  获取 HandlerMapping
   ▼
③ HandlerMapping 根据 URL 找到对应的 Controller 方法（Handler，含拦截器列表）
   │
   ▼
④ HandlerAdapter 适配执行该 Handler
   │   先执行拦截器 preHandle
   │   通过反射调用 @Controller 方法，@RequestBody/@RequestParam 等参数绑定
   │   返回 ModelAndView / @ResponseBody 数据
   ▼
⑤ 拦截器 postHandle 执行
   │
   ▼
⑥ 判断：是返回视图 还是 JSON 数据？
   │   - 返回视图 → ViewResolver 解析视图名 → 渲染 HTML
   │   - @ResponseBody/@RestController → HttpMessageConverter 序列化成 JSON
   ▼
⑦ 拦截器 afterCompletion 执行（清理资源）
   │
   ▼
⑧ 写回 HTTP 响应
   │
   ▼
⑨ Filter 倒序收尾
```

**三步定位（面试口述版）：**
> 请求先经过 **Filter（web层）** → 到 **DispatcherServlet** → 通过 **HandlerMapping** 找到方法 → **HandlerAdapter** 执行 → 结果交给 **ViewResolver/HtmlMessageConverter** 返回 → 再走拦截器**清理**。

---

## 三、核心组件一览（大白话）

| 组件 | 大白话角色 |
|---|---|
| **DispatcherServlet** | 总前台/总导演。所有请求的第一站，负责调度 |
| **HandlerMapping** | 台账/导航。根据 URL 告诉我"这个请求该找谁处理" |
| **Handler / Controller** | 真正的干活的人（你的 @Controller 方法） |
| **HandlerAdapter** | 传话人。用合适的方式调用 Handler（反射），兼容各种 Handler |
| **HandlerInterceptor(pre/post/after)** | 安检+哨兵。执行前把关、执行后收尾 |
| **ViewResolver** | 视图翻译官。把逻辑视图名 → 真正的视图对象（JSP/Thymeleaf） |
| **HttpMessageConverter**（@ResponseBody 走这个）| JSON 转换器。Java对象 ↔ JSON 互转 |

---

## 四、Filter vs Interceptor vs AOP（面试高频三兄弟对比）

> 常被连问，一定要分清楚三层拦截的**顺序、作用范围、生命周期数据**。

| 维度 | **Filter（过滤器）** | **Interceptor（拦截器）** | **AOP（切面）** |
|---|---|---|---|
| 所属 | Servlet 规范（web层）| Spring MVC | Spring 容器 |
| 位置 | **最外层**，进 DispatcherServlet **之前** | 在 Handler 执行前后 | **最内层**，方法调用层面 |
| 执行顺序 | 先 Filter → 再 Interceptor → 再 AOP | 在 DispatcherServlet 之后、方法包裹 | 在方法真正执行处 |
| 能拿到什么 | 只能拿到 `HttpServletRequest/Response`，**拿不到 Handler 方法信息** | 能拿到 HandlerMethod、参数、返回值 | 能拿到方法签名、参数、注解、甚至修改目标对象 |
| 典型用途 | 编码、登录态过滤、CORS、防 XSS | 权限校验、日志、token解析（能拿到方法名）| 事务、缓存、日志、@Transactional、@Async |
| 能否改请求体 | ✅ 能 | ❌ 一般不改 | 视切点 |

**面试一句话分工：**
> 全局粗粒度用 **Filter**（编码/CORS/登录）；需要方法级信息用 **Interceptor**（权限/token/日志）；需要业务逻辑织入用 **AOP**（事务/缓存）。**Filter 在外 → Interceptor 居中 → AOP 最内**。

**⚠️ 经典坑**：想在拦截器里拿到方法上自定义注解（如 `@RequirePermission("admin")`）判断权限 —— 必须用 **Interceptor** 而不是 Filter（Filter 拿不到 HandlerMethod）。

---

## 五、@Controller vs @RestController

```java
@Controller        // 返回的是"视图名"，靠 ViewResolver 渲染 HTML；类里方法要加 @ResponseBody 才返回JSON
@RestController    // = @Controller + @ResponseBody，方法直接返回 Java对象→序列化成JSON
```

> @ResponseBody 走的是 **HttpMessageConverter**（MappingJackson2HttpMessageConverter 把对象转成 JSON），不走 ViewResolver。

---

## 六、面试答题公式

**Q：一次请求从发出到返回，Spring MVC 都经历了什么？**
> 1（原理）：DispatcherServlet 是前端控制器，全流程是 接收→HandlerMapping找到Controller→HandlerAdapter执行→返回模型/JSON。
> 2（取舍）：区分三层拦截——Filter做全局web层，Interceptor做方法级权限/日志，AOP做事务等业务织入，各司其职。
> 3（实战·若依）：若依后端是 RuoYi + Spring Boot，接口统一走 @RestController，登录用 Filter/JWT 拦截器校验 token，权限用 @Require注解+AOP/拦截器判断；接口返回统一 Result 对象，通过 HttpMessageConverter 序列化。

---

## 七、自测灵魂拷问

Q1: Filter 和 Interceptor 谁先执行？区别是什么？
A: Filter 先（web层，DispatcherServlet 之前），Interceptor 后（Handler 前后）。Filter 拿不到 HandlerMethod，Interceptor 能。

Q2: 想在拦截器里校验"用户是否有权限调用这个带有注解的方法"，用 Filter 还是 Interceptor？
A: Interceptor。因为 Filter 拿不到 Handler 方法信息，Interceptor 能拿到方法上的自定义注解。

Q3: @ResponseBody 返回 JSON 走的是 ViewResolver 吗？
A: 不是，走 HttpMessageConverter 序列化。

Q4: 三层拦截的顺序？
A: Filter（最外）→ Interceptor（居中）→ AOP（最内，方法处）。

🔗 关联知识
[[01Spring Boot 启动流程]]（DispatcherServlet 怎么被 Tomcat 启动的）
[[03Spring事务深度剖析]]（@Transactional 就是 AOP 的应用）
[[04_...]]（若依后端如何用 Filter/Interceptor/JWT）
