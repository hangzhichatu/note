# Spring IoC 与 AOP 精讲（原理 + 底层 + 为什么这么设计）

> 2026-09-21 学习补充。IoC 和 AOP 是 Spring 的两块地基，本文从"为什么需要它们"出发，讲到"底层怎么实现"，最后给出面试话术与自测。
> 关联：[[03Spring事务深度剖析]]（事务 = AOP 代理的应用）、[[01Spring Boot 启动流程]]、[[02自动装配原理]]

---

## 一、IoC（控制反转）

### 1.1 先问"为什么"——没有 IoC 会怎样

```java
public class OrderService {
    private UserDao userDao = new UserDaoImpl();   // 硬编码依赖
    private PayService payService = new PayService();
}
```

**三个问题：**
1. **强耦合**：编译期就跟 `UserDaoImpl` 绑死，换实现要改源码重编译
2. **测试地狱**：单元测试想 mock 一个假 `PayService`？做不到，它自己 new 的
3. **生命周期没人管**：谁创建、谁析构、谁来保证单例？

> 根因：**对象自己决定依赖谁、自己创建依赖 → "变化"会沿依赖链传染。**

### 1.2 反转了什么

> **IoC = 把"创建和管理对象"的控制权，从业务代码交给容器。**

```
之前：OrderService 说「我要 new UserDaoImpl」    ← 主动方是业务代码
之后：容器说「我给你注入一个 UserDao」            ← 主动方是容器
```

- **DI（依赖注入）是 IoC 的实现手段**：IoC 是思想，DI 是做法（构造器/setter/字段注入）
- **一句话**：IoC 解决的是**"依赖的创建权归属"**，本质是**解耦**
- 关键词是**"被动接收"**：对象不再主动拿依赖，而是被动等容器注入

### 1.3 底层：容器到底是什么

> **IoC 容器本质就是一个 `Map<String, Object>`**（`DefaultSingletonBeanRegistry` 里的 `singletonObjects`，底层 `ConcurrentHashMap`）。

流程：`BeanDefinition`（Bean 的"身份证"，由 `@Component`/`@Bean`/XML 扫描得到）→ 容器按它**反射实例化** → 塞进 `singletonObjects` → 谁需要就从 Map 取（取不到就按依赖链递归创建再注回去）。

**🎯 结论：注入的技术本质 = Map 取值 + 反射赋值。没有魔法。**

### 1.4 ⭐ 底层精讲：三级缓存解决循环依赖

```java
class A { @Autowired B b; }
class B { @Autowired A a; }
```

朴素流程会死锁：创建 A 需要 B → 创建 B 需要 A → A 还没创建完 💀

**Spring 解法：不等对象"完成"，先"提前暴露一个半成品引用"。**

| 级别 | 名字 | 存什么 |
|---|---|---|
| 一级 | `singletonObjects` | **成品** Bean |
| 二级 | `earlySingletonObjects` | **半成品**（提前暴露的早期引用） |
| 三级 | `singletonFactories` | **工厂**（`ObjectFactory`，用来生成早期引用） |

**流程（A、B 互依赖）：**
```
1. 创建 A → 实例化(未注入属性) → A 的 ObjectFactory 放进【三级】
2. A 注入 B → 去创建 B
3. 创建 B → 实例化 → B 注入 A
4. B 找 A：三级缓存拿到 A 的【早期引用】→ 放进【二级】，返回给 B
5. B 注入 A 成功 → B 完成 → 升到【一级】
6. 回 A → 注入 B 成功 → A 完成 → 升到【一级】
```

**🎯 为什么必须三级，不能只二级？**（高频追问）
> 因为**第三级的 `ObjectFactory` 是为了"延迟生成代理对象"**。
> 如果需要 AOP 代理，暴露给 B 的**必须最终是代理对象**。如果直接把原始对象放进二级，等 A 后面被 AOP 包成代理时，B 手里拿的还是原始对象 → 不一致。
> **工厂（三级）的作用：真正用到时才决定"给原始对象还是给代理对象"。**
> ⚠️ 措辞关键：理由**不是"代理生成晚"，而是"代理决策要延迟"** → 所以存**工厂**而非死对象。二级只能存"已定型的对象"，三级存"能延迟决定生成什么的工厂"。
> ⚠️ 边界：如果 Bean **不需要 AOP 代理**，二级其实就够，三级只是统一留的扩展口。

**⚠️ 关键边界：构造器注入的循环依赖解决不了。**
> 因为构造器注入要求"依赖必须先就绪才能 new"，而循环依赖时 `new A` 需要 B、`new B` 需要 A → **连实例化都完不成，提不上"提前暴露"**。Spring 会直接启动报错。
> **严格说法**：不是"注入同阶段"，而是"**实例化前就要依赖**" → 连半成品都没有。
> 对比：字段/setter 注入能解决，是因为"先实例化（空壳），后注入"，有个半成品窗口。

### 1.5 三种注入方式

| 方式 | 评价 | 理由 |
|---|---|---|
| **构造器注入** | ✅ 官方推荐 | 依赖不可变(final)、便于测试、**启动即暴露循环依赖** |
| setter 注入 | 可选依赖 | — |
| 字段 `@Autowired` | ⚠️ 不推荐 | 无法 final、难测试、循环依赖运行时才暴露 |

---

## 二、AOP（面向切面）

### 2.1 先问"为什么"——横切关注点

20 个方法每个都要写日志/事务/权限：
```java
public void createOrder() {
    log.info("开始");                        // ← 横切
    checkPermission();                       // ← 横切
    try { /* 50 行真正的业务 */ }
    finally { log.info("耗时..."); }         // ← 横切
}
```
- 复制粘贴 20 遍 → 改一处要改 20 处
- 业务代码被噪音淹没

> **AOP 思想：把横切逻辑抽出来单独写，再"插回"到需要的地方。**
> 注意：横切逻辑**不是消失了**，只是**集中管理**了。

### 2.2 核心概念

| 概念 | 含义 | 类比 |
|---|---|---|
| **切面 Aspect** | 横切逻辑的封装 | 一张"贴纸" |
| **连接点 JoinPoint** | 可以插入切面的点 | 墙上能贴贴纸的**位置** |
| **切点 Pointcut** | **筛选**哪些连接点要贴 | 贴纸的**准入规则** |
| **通知 Advice** | 切点前后做什么 | 贴纸的**内容** |
| **织入 Weaving** | 把切面插进目标对象的**动作** | 贴贴纸的**动作** |

> 顺口溜：**连接点是"所有可能的位置"，切点是"我选了哪几个"，通知是"在那儿干什么"，织入是"怎么植进去的"。**
> 补充：切点决定"**在哪儿**"，通知决定"**什么时候 + 做什么**"（Before/After/Around）。

### 2.3 底层：动态代理（织入怎么实现）

Spring 不改源码，而是**运行时生成一个代理对象冒充原对象**。

| 方式 | 原理 | 前提 |
|---|---|---|
| **JDK 动态代理** | 基于**接口**（`Proxy.newProxyInstance`） | 目标类**必须有接口**，只能代理接口里定义的方法 |
| **CGLIB** | 基于**继承**，生成子类、重写方法 | 目标类**不能是 final** |
| **Spring Boot 2.x 起默认 CGLIB** | 不强制要求接口，更通用 | — |

**代理伪代码：**
```java
class OrderService$Proxy extends OrderService {
    @Override
    public void createOrder() {
        log.info("开始"); tx.begin();   // 前置
        super.createOrder();            // 真实业务
        tx.commit(); log.info("耗时");   // 后置
    }
}
```

**🎯 为什么用代理？** → **不侵入原代码**。代价：**调用必须经过代理对象** —— 这就是所有 AOP 失效问题的根源（`this` 直调绕过代理 = AOP 没加）。

### 2.4 ⭐ 底层：代理在哪个阶段生成

```
实例化 → 属性注入 → Aware → 前置处理 postProcessBeforeInitialization
  → @PostConstruct → InitializingBean.afterPropertiesSet → init-method
  → ★【postProcessAfterInitialization】← AOP 代理在这生成，替换容器里的原始对象
  → (之后才是能被别人注入使用的成品 Bean)
```

**答案：`BeanPostProcessor.postProcessAfterInitialization`（初始化【之后】的后置处理）。**
> `AbstractAutoProxyCreator` 干这个：Bean 初始化完成后检查"要不要被切"，需要就用 `ProxyFactory` 生成代理**替换**原始对象。
> ⚠️ 订正：不是 `postProcessBeforeInitialization`（那是前置），只在 **After**。

**🎯 这解释了三个经典问题：**
1. **同类自调用 `@Transactional` 失效** → `this.createOrder()` 走原始对象，不是代理
2. **`@Transactional` 要加在 public 方法** → CGLIB 重写的是 public 方法
3. **单测 mock 接口而非实现类** → 代理本身就是"实现同接口/继承同类"的东西

### 2.5 ⭐ IoC 和 AOP 不是两件事

```
IoC 容器创建 Bean
  ↓ 在创建的后置阶段(postProcessAfterInitialization)，AOP 的 BeanPostProcessor 插手
  ↓ 把需要被切的 Bean 换成代理
  ↓ 下次谁注入这个 Bean，拿到的就是代理
```

> **所以：`@Transactional`/`@Async`/`@Cacheable` 本质都是 AOP；AOP 的代理又必须在 IoC 容器里才能生效。IoC 是地基，AOP 是地基上盖的楼。**

**面试金句（Boss 自述版 + 标准版）：**
> Boss 版："IoC 让 AOP 的代理可以无感实现，整个过程不需要人手动参与；切面本身 `@Aspect` 也是 Bean，依赖注入也统一了；所有用过原始 Bean 的地方最后拿到的都是增强后的代理，这过程不用手动接入。"
>
> **标准模板**："AOP 依赖 IoC，因为 AOP 的两端都活在 IoC 容器里：① 切面本身（`@Aspect`）是 Bean，由容器创建、注入它需要的依赖（如 `PlatformTransactionManager`）；② 被切的业务 Bean 也由容器创建；③ 最关键的是——容器在创建 Bean 的后期阶段（`postProcessAfterInitialization`）完成"代理替换"，之后所有人从容器里拿到的都是代理。**IoC 负责造零件和装配，AOP 只是在装配线上加了一道'贴膜'工序。** 没有 IoC 的集中管理，AOP 无处下手。"

---

## 三、572 / 若依 项目联结（面试加分）

| 概念 | 项目对应 |
|---|---|
| IoC | 若依 `ruoyi-common` 里各 starter 通过自动装配把 Bean 交给容器 |
| AOP | 若依**操作日志、数据权限、接口限流**都是 AOP 实现 |
| **AOP 思想类比** | 572 的 **3DE Trigger 机制** —— 挂在生命周期切点上、侵入性低，**本质也是一种 AOP 思想**（★ 面试很出彩） |
| 代理失效类比 | 572 `domainObjectPolicyChange` 是 JAX-RS 类**非 Spring Bean** → 用不了 `@Transactional` → 只能 `ContextUtil` 手动事务 |

---

## 四、面试前盯死的三个点（2026-09-21 判卷暴露）

| 点 | 正确版本 |
|---|---|
| **三级缓存的理由** | 不是"代理生成晚"，是"**代理决策要延迟**" → 存**工厂**而非死对象 |
| **构造器循环依赖** | 不是"注入同阶段"，是"**实例化前就要依赖**" → 连半成品都没有 |
| **代理生成时机** | **`postProcessAfterInitialization`**（初始化后置处理），记牢 |

---

## 五、自测

**IoC**
1. IoC 到底"反转"了什么？为什么说它解决的是"依赖创建权归属"？
2. IoC 容器本质是什么数据结构？"注入"的技术本质是什么？
3. 三级缓存每级存什么？为什么**必须三级不能只二级**？（关键：代理决策要延迟）
4. 构造器注入的循环依赖为什么解决不了？

**AOP**
5. 连接点、切点、通知三者的区别？
6. JDK 动态代理和 CGLIB 的区别？Spring Boot 默认哪个？
7. AOP 代理在**哪个回调哪一步**生成？为什么这解释了"自调用事务失效"？

**综合**
8. 为什么说"AOP 依赖 IoC"？用"装配线 + 贴膜工序"讲一遍。

---

🔗 关联知识
[[03Spring事务深度剖析]]（事务 = AOP 代理的典型应用）
[[01Spring Boot 启动流程]]（refreshContext 里扫描/实例化 Bean）
[[02自动装配原理]]（Bean 怎么被注册进容器）
[[06设计模式/05代理模式]]（动态代理的设计模式视角）
[[09_数据库深度剖析]]（事务真正在 InnoDB 上的实现）
