# Spring 代理体系全景 + 代理在 Bean 生命周期中的包装节点

> 2026-10-15 学习产出（以练代学：循环依赖实验室 s3/s4/s7 + 多拦截器链实操）。
> 本文回答两个问题：**① Spring 里代理体系常用的有哪些？② 这些代理分别在 Bean 生命周期的哪个节点完成"包装"？**
> 关联：[[00_IoC与AOP精讲]]（IoC/AOP 地基、三级缓存）、[[bean全生命周期流程（专注Bean的灵活性来源接口）]]、[[03Spring事务深度剖析]]
> 配套实验田：`D:\git\spring-demo-lab`（profile 隔离场景 s1~s7）

---

## 一、先记住一个模型：贴纸 → 扫贴纸的人 → 代理

面试/排查代理问题，脑子先跑这个三段式：

```
   贴纸(注解)   →   扫贴纸的人(BPP)   →   代理(拦截器链)
  @Transactional   BeanPostProcessor     CGLIB/JDK 代理
   @Async ...
```

- **贴纸本身不干活** —— 注解只是"声明我想要这个能力"
- **必须有 BPP 扫到它** —— 没有"扫贴纸的人"，注解 = 死贴纸，静默失效（不报错）
- **产物是代理** —— 外部调用先到代理，代理按链条插入逻辑再放行

> **⭐ 金句：注解是声明，BPP 是机制，代理是产物。三者缺一，增强失效。**

---

## 二、代理的两大"血统"（先分家，一切从这里开始）

Spring 的代理**来源只有两个家族**：

| 家族 | 核心类 | 代表注解 | 特点 |
|------|--------|---------|------|
| **AOP 家族**（AspectJ 风格） | `AnnotationAwareAspectJAutoProxyCreator`（继承 `AbstractAutoProxyCreator`） | `@Transactional`/`@Cacheable`/`@PreAuthorize`/`@Aspect`/`@Retryable`/`@Validated` | **所有 Advisor 汇总到同一个代理**，形成一条拦截器链 |
| **专用 BPP 家族** | 各自的 `BeanPostProcessor` | `@Async` | **各自为政**，自己管自己的代理 |

**⚠️ 这个"分家"是理解一切的关键**：AOP 家族的增强能"叠加成一个代理"；专用 BPP 各有各的脾气。

---

## 三、常用代理注解全景

### 🟦 AOP 家族（`AnnotationAwareAspectJAutoProxyCreator`）

**共性：这些注解本质都是"往容器里注册一个 Advisor"，最终汇入同一个代理的拦截器链。**

| 注解 | 作用 | 背后的拦截器 | 默认顺序 |
|------|------|-------------|---------|
| **`@Transactional`** | 声明式事务 | `TransactionInterceptor` | `LOWEST_PRECEDENCE`（最内层） |
| **`@Cacheable` / `@CacheEvict` / `@CachePut`** | 声明式缓存 | `CacheInterceptor` | `LOWEST_PRECEDENCE` |
| **`@PreAuthorize` / `@PostAuthorize` / `@Secured`** | 方法级安全 | `MethodSecurityInterceptor` | —— |
| **`@Aspect`（自定义切面）** | 你写的 `@Around`/`@Before`... | 你的通知 → Advisor | 按 `@Order` |
| **`@Retryable`**（Spring Retry） | 方法重试 | `RetryOperationsInterceptor` | —— |
| **`@Validated`（方法级）** | 方法参数校验 | `MethodValidationInterceptor` | —— |

**⭐ 叠加规则**：一个方法同时有 `@Transactional + @Cacheable + 自定义@Aspect` → **仍然是 1 个代理**，链条里挂 3 个拦截器。顺序由 `@Order` 决定（值越小越外层）。

### 🟩 专用 BPP 家族

| 注解 | 处理器 | 是否产生代理 | 关键特性 |
|------|--------|-------------|---------|
| **`@Async`** | `AsyncAnnotationBeanPostProcessor` | ✅ 产生代理 | **不实现 `getEarlyBeanReference`** → 循环依赖会炸 |
| `@Scheduled` | `ScheduledAnnotationBeanPostProcessor` | ❌ 不产生代理 | 只做"任务注册"，不涉及代理 |

**⚠️ 精确点**：`@Async` 是**唯一常见的"专用 BPP 代理"** —— 它和 AOP 家族井水不犯河水。

---

## 四、⭐ 分水岭：哪类代理「走提前暴露」（能否解循环依赖）

**这是本文最值钱的一张表**（由 s3/s4 实验得出，可推广到全局）：

| 代理来源 | 处理器 | 实现 `getEarlyBeanReference`？ | 循环依赖时 |
|---------|--------|---------------------------|-----------|
| **AOP 家族**（`@Aspect`/`@Transactional`/`@Cacheable`/`@PreAuthorize`...） | `AbstractAutoProxyCreator` | ✅ **实现了** | **能解** ✅ |
| **`@Async`** | `AsyncAnnotationBeanPostProcessor` | ❌ **没实现** | **抛 `BeanCurrentlyInCreationException`** ❌ |

**⭐ 一句话结论：**
> **"advisor-based 的代理（AOP 家族）能解循环依赖；`@Async` 不能。"**

### 为什么？—— 回到"包装时间节点"

`getEarlyBeanReference` 定义在 `SmartInstantiationAwareBeanPostProcessor`（**可选接口**）。**谁实现了它，谁就能在"循环依赖提前暴露"的那一刻现场生成代理。**

- **AOP 家族**：实现了 → 早期暴露的就是**代理**（终态）→ 前后一致 → 能解
- **`@Async`**：没实现 → 早期暴露的是**裸对象** → 之后才变代理 → 前后不一致 → 炸

---

## 五、⭐⭐ 核心：两类代理在 Bean 生命周期中的"包装时间节点"

### 5.1 先把 Bean 生命周期（带代理位点）铺开

```
【1】实例化         new A()                          ← 原生对象诞生
      │
【2】属性填充       @Autowired 注入                   ← 循环依赖的"暴露窗口"
      │                 ↑
      │          ★ 实例化后【立刻】塞三级缓存的工厂
      │
【3】初始化前       BPP.before → @PostConstruct 等
      │
【4】初始化         InitializingBean.afterPropertiesSet / init-method
      │
【5】初始化后       BPP.after → ⭐ 代理【标准/默认】生成点！
      │                （AOP 家族 + @Async 默认都在这）
      │
【6】进单例池       singletonObjects 存【代理】
```

**两个关键位点：**
- **【2】后**：塞三级缓存工厂 → **"提前暴露窗口"开启**
- **【5】`postProcessAfterInitialization`**：代理的**标准生成点**

### 5.2 🟦 AOP 家族：**双时间点，且"提前"优先**

```
【1】new A()
      ↓
【2】塞三级缓存（工厂）        ← 提前暴露窗口开启
      ↓
   👉 B 来要 A → 调工厂 → getEarlyBeanReference()
                          └─ AbstractAutoProxyCreator 在【这里】就生成代理！⭐
      ↓
【3-4】初始化推进
      ↓
【5】postProcessAfterInitialization
      └─ 检查 earlyProxyReferences："代理是否已生成？"
            ├─ 是 → 跳过（复用早期那个）✅
            └─ 否 → 在这里生成
      ↓
【6】进单例池：代理（= B 手里那个，同一个）
```

**⭐ 双保险机制：**
- 正常路径：在【5】生成
- 循环依赖时：在【2】后的工厂调用里**提前生成**（因为实现了 `getEarlyBeanReference`）
- **结果：无论哪条路，早期暴露的和最终的，都是同一个代理** ✅ → 这就是能解环的根本原因

### 5.3 🟩 `@Async`：**只有【5】一个时间点**

```
【1】new A()
      ↓
【2】塞三级缓存（工厂）
      ↓
   👉 B 来要 A → 调工厂 → getEarlyBeanReference()
                          └─ AsyncAnnotationBeanPostProcessor 没实现 → 返回【裸对象】❗
      ↓
【3-4】初始化
      ↓
【5】postProcessAfterInitialization
      └─ @Async 在【这里】才生成代理  ⚠️ 太晚！
      ↓
【6】进单例池：代理  ← 但 B 手里还是【裸对象】→ 分叉 → 炸
```

**⭐ `@Async` 的致命点：包装只发生在【5】，而【2】的窗口期它给不出代理 → 循环依赖必然分叉。**

### 5.4 对比表（背这个）

| 维度 | AOP 家族 | `@Async` |
|------|---------|----------|
| **标准生成点** | 【5】`postProcessAfterInitialization` | 【5】`postProcessAfterInitialization` |
| **提前生成点** | ✅ 【2】后（`getEarlyBeanReference`） | ❌ 无 |
| **循环依赖时暴露的** | **代理**（终态） | **裸对象** |
| **循环依赖结果** | ✅ 能解 | ❌ 炸 |
| **本质差异** | 实现了 `SmartInstantiationAwareBeanPostProcessor.getEarlyBeanReference` | 只实现了 `postProcessAfterInitialization` |

> **⭐ 一句话：两者"标准包装点"都在【5】；但 AOP 家族多了一个【2】的"提前包装"能力，`@Async` 没有。这个差异就是"能否解循环依赖"的分水岭。**

### 5.5 完整时间轴图（建议保存）

```
       【1】实例化      【2】暴露窗口        【5】初始化后
          │                  │                   │
  AOP家族 │ new A()      ✅可提前包装代理    检查(已包→跳过)
          │                  │                   │
          │            → B 拿到【代理】✅      进池:代理

  @Async  │ new A()      ❌只能给裸对象       在此才包装代理
          │                  │                   │
          │            → B 拿到【裸对象】❌     进池:代理 → 分叉炸
```

### 5.6 追问：AOP 家族在【2】包了，到【5】怎么不重复包？

**靠 `earlyProxyReferences` 缓存：**
- `AbstractAutoProxyCreator` 内部有个 `Map<Object, Object> earlyProxyReferences`
- 【2】生成代理时记录"这个 bean 已代理过"
- 【5】检查该 map → **已代理过 → 直接返回，不再包一层**

> **⭐ 这就是 s3 里"早期暴露的代理 == 最终代理"（不会套两层代理）的底层原因。**

---

## 六、代理生成"三要素"（抽象层，缺一不可）

```
① 有增强逻辑的存在
     AOP家族：容器里有 Advisor（切面/事务/缓存...）
     专用BPP：处理器匹配到注解（@Async）
② 有触发点
     都是 BeanPostProcessor（初始化后判断要不要包）
③ 走代理创建流程
     JDK 动态代理（有接口）or CGLIB（无接口；Spring Boot 2.0+ 默认 CGLIB）
```

对应口语版：**贴纸（①）+ 扫贴纸的人（②）+ 代理（③）**。

---

## 七、还有哪些「非注解」的代理？

| 场景 | 说明 |
|------|------|
| **`@EnableAspectJAutoProxy`** | AOP 家族的总开关（Boot 自动开） |
| **`@EnableTransactionManagement`** | 事务代理开关（Boot 自动开，靠 `TransactionAutoConfiguration`） |
| **`@EnableMethodSecurity`** | 安全代理开关 |
| **`ProxyFactoryBean` / 显式 Advisor** | 手工编程式 AOP、老项目 XML |
| **MyBatis Mapper 接口** | JDK 动态代理生成实现（`MapperProxy`）⭐ 不是 AOP |
| **`@FeignClient`** | 声明式 HTTP 客户端（动态代理）⭐ 不是 AOP |
| **`@Repository` 异常转换** | `PersistenceExceptionTranslationPostProcessor` |

> **⚠️ 注意区分**：MyBatis Mapper、Feign Client 是"**用动态代理生成接口实现**"，**不算 AOP**，别混。

---

## 八、面试话术（背这段）

> **问：Spring 的代理体系是怎样的？**
>
> 「Spring 的代理主要分两类：
> 1. **AOP 家族**：以 `AnnotationAwareAspectJAutoProxyCreator` 为核心，`@Transactional`、`@Cacheable`、`@PreAuthorize`、自定义 `@Aspect` 都是注册 Advisor，**最终汇入同一个代理的拦截器链**。它们实现了 `getEarlyBeanReference`，**能解循环依赖**。
> 2. **专用 BPP 家族**：比如 `@Async`，由 `AsyncAnnotationBeanPostProcessor` 处理，**不实现 `getEarlyBeanReference`**，所以在循环依赖里会抛 `BeanCurrentlyInCreationException`。
>
> 代理生成的三要素是：**有 Advisor/增强、BPP 触发、JDK/CGLIB 创建**。包装时间点默认在 **`postProcessAfterInitialization`**（初始化后），AOP 家族额外支持在**三级缓存提前暴露窗口**（实例化后、属性填充时）提前包装。
>
> 另外还有一类"代理别用"：MyBatis Mapper、Feign Client，是靠动态代理生成接口实现，不算 AOP。」

> **问：Bean 生命周期里代理在哪一步生成？**
>
> 「默认在**初始化后**的 `postProcessAfterInitialization`。但 AOP 家族的 `AbstractAutoProxyCreator` 额外实现了 `getEarlyBeanReference`，所以**如果这个 bean 陷入循环依赖，代理会在"实例化后、属性填充时"的提前暴露窗口就生成**，并记进 `earlyProxyReferences`；到初始化后检查发现已代理，就跳过不重复包。
> 而 `@Async` 只实现了 `postProcessAfterInitialization`，没有提前包装能力，所以它参与循环依赖时，别人拿到的是裸对象，最终却被包成代理 → 前后不一致 → 抛 `BeanCurrentlyInCreationException`。」

---

## 九、自测（合上笔记回答）

1. Spring 代理的两大血统分别是什么？各自的核心类？
2. `@Transactional` / `@Cacheable` / `@Aspect` 同时作用在一个方法上，会有几个代理对象？
3. `@Async` 和 `@Transactional` 在"循环依赖"上的表现有什么本质区别？为什么？
4. 代理的"标准包装时间点"是生命周期的哪一步？AOP 家族多出的那一步在哪？
5. AOP 家族在提前暴露时包了代理，到初始化后为什么不重复包？
6. MyBatis Mapper 接口是 AOP 吗？为什么？

---

## 附：本文结论的实验来源（spring-demo-lab）

| 场景 | 验证内容 | 结论 |
|------|---------|------|
| s3 | `@Aspect` 代理环 | `getEarlyBeanReference` 在 B 索取那刻返回**代理**（早期=终态）→ 能解 |
| s4 | `@Async` 代理环 | 没实现 `getEarlyBeanReference` → 早期给**裸对象** → `BeanCurrentlyInCreationException` |
| s4-实验B | 绕过校验后 | `B4` 拿裸 A4 → `doAsync()` 的 `thread=main`（异步**静默失效**）；从容器调 `thread=task-1` |
| s7 | 多拦截器链 | 洋葱顺序（`@Order` 小=外层）/ ThreadLocal 通信 / 不 `proceed()` 则内层全不执行 |

---

## 补充实验：s5 / s5b —— 作用域边界（2026-10-19 追加）

> 前面 s1/s3/s4/s7 都在「单例↔单例」范围内。本节回答：**换成 prototype 会怎样？**
> 唯一变量：只改 `@Scope`，其他不动（MVP 原则）

### 三态对照表（核心结论）

| 组合 | 结果 | 谁救的 |
|------|------|--------|
| s1：单例 ↔ 单例 | 启动成功 | 三级缓存，**两端**都能用 |
| s5a：prototype ↔ 单例 | 启动成功 | **单例那一端**从三级缓存获救 |
| s5b：prototype ↔ prototype | **启动失败** `BeanCurrentlyInCreationException` | **无人可救** |

> ⭐ **一句话：三级缓存 = 「缓存」+「单例」两个前提，缺一崩。**

### s5a 实测（A5 prototype / B5 单例）

```
[构造] B5(singleton)  hash = 1315795813   ← 只有这一个 B5
[构造] A5(prototype)  hash = 1515116175   ← 老 A5（B5 创建时生出来的）
[构造] A5(prototype)  hash = 1919576980   ← 新 A5 #1
[构造] A5(prototype)  hash = 211649273    ← 新 A5 #2

探针2 两次 getBean(A5) 相同?          false  → prototype 语义正常
探针1 b5.getA5() vs getBean 相同?     false  → B5 手里是「老 A5」
探针3 新 A5 里的 B5 == 容器里的 B5?   true   → 环被「绕开」而非「解开」
```

### 真实创建路径（⚠️ 方向容易搞反）

```
1. 容器 refresh → 预实例化单例 B5（单例必须在启动期造完）
2. B5 构造中 @Autowired 需要 A5
   → A5 是 prototype，启动时不创建 → 此刻现造（老 A5）
3. 老 A5 内部 @Autowired 需要 B5
   → B5 正在创建中且是单例 → 从三级缓存 singletonFactories 拿到早期引用 ✅
   → 救场的是「B5 是单例」，不是 A5
4. 老 A5 完成 → 注入给 B5 → B5 完成进一级缓存
5. 之后每次 getBean(A5) → 全新 A5 → 其 B5 需求直接命中一级缓存
```

**两个易错点：**
1. **prototype 的 A5 根本不进三级缓存** —— `addSingletonFactory` 只对单例执行
2. **是单例先造、prototype 后造**，不是反过来。「谁先」不是运气，是**生命周期类型决定的**（单例启动期造完，prototype 延迟到 getBean）

### s5b 实测（双 prototype）

```
[构造] B5 → [构造] A5 → 炸
WARN: Error creating bean with name 'b5': Requested bean is currently in creation:
      Is there an unresolvable circular reference?
APPLICATION FAILED TO START
```

**原因**：`earlySingletonExposure = (mbd.isSingleton() && allowCircularReferences && isSingletonCurrentlyInCreation)`
两边都是 prototype → **双方都 false** → 没有一方能进三级缓存 → 死锁无解。

### 收口金句

> **单例环是被三级缓存【解开】的 —— 机制保证；**
> **prototype 环是「每次现造 + 单例那端已经在了」【绕开】的 —— 靠顺序，不是机制。**

> **三级缓存里存的不是对象，是「到需要时再决定要不要包装」的决策权。**
> 传 `() -> getEarlyBeanReference(...)` 而不是直接传 `bean`，是因为此刻还不该决定「暴露的是原生对象还是代理」。
> 好处 = **按需生成**：无环（绝大多数 bean）永不调用工厂，少生成一次代理。

### 源码坐标（spring-beans 6.1.6）

| 内容 | 文件 | 行 |
|------|------|-----|
| 三级缓存字段声明 | `DefaultSingletonBeanRegistry` | ~L90 |
| `addSingletonFactory` | `DefaultSingletonBeanRegistry` | **L154** |
| `getSingleton(beanName, allowEarlyReference)` | `DefaultSingletonBeanRegistry` | **L180** |
| `doCreateBean` 三条件 + earlySingletonExposure | `AbstractAutowireCapableBeanFactory` | **L553** |
| `getEarlyBeanReference` | `AbstractAutowireCapableBeanFactory` | **L950** |

### 复习方法

**不重读笔记，重跑实验**（3 分钟，现象触发回忆）：
```
cd D:\git\spring-demo-lab
mvn -o spring-boot:run "-Dspring-boot.run.profiles=s5"
```
复习 s5b：把 B5 的 `@Scope("prototype")` 加回去再跑。