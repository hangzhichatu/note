# @Value 与 @ConfigurationProperties 深度辨析

> 2026-10-19 学习产出（以练代学：s5 prototype 环 + s3 源码对照后接 Spring 配置主线）
> 关联：[[bean全生命周期流程（专注Bean的灵活性来源接口）]]、[[05Spring代理体系全景与生命周期包装节点]]、[[00_IoC与AOP精讲]]
> 实验田：`D:\git\spring-demo-lab`
> Spring 源码副本：`D:\git\spring-src\beans`（spring-beans 6.1.6）

---

## 〇、先定位：它们在生命周期的哪一站

```
实例化(Instantiation) → 属性赋值(Population) → 初始化(Initialization) → 使用 → 销毁
                          ↑                        ↑
                    @Value / @ConfigurationProperties
                    都在这站完成，但走两条不同的路
```

> ⭐ **共同点**：都在 `populateBean`（属性赋值）阶段完成
> ⭐ **不同点**：完成它的 BPP 不同、机制不同、能力边界不同

---

## 一、本质差异：逐字段解析 vs 整对象绑定

### @Value 做的事

```java
@Value("${app.name}")
private String name;
```

```
读到注解 → 字符串交给 Environment 解析 → 得到 "茶荼" → 反射 set 到字段 → 结束
```

**一次只处理一个字段，是逐字段解析。**

处理者：`AutowiredAnnotationBeanPostProcessor`
> ⚠️ 注意：`@Value` 和 `@Autowired` 用的是**同一个 BPP**。这是个容易忽略的细节。

### @ConfigurationProperties 做的事

```java
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String name;
    private int port;
    private List<String> servers;
    private Nested nested;
}
```

```
拿到前缀 "app" → 扫描类里所有字段 → 按名去找 app.name / app.port / app.servers / app.nested.x
→ 类型转换（String → Integer / List / Duration / 嵌套对象）
→ 构造出整个对象 → 整体塞进去
```

**一次性绑定整个对象。**

处理者：`ConfigurationPropertiesBindingPostProcessor`

> ⭐ **一句话记牢：`@Value` 是「逐字段解析」，`@ConfigurationProperties` 是「整对象绑定」。**

---

## 二、六维差异表（面试可直接背这张）

| 维度 | `@Value` | `@ConfigurationProperties` |
|---|---|---|
| **粒度** | 单字段，一个字段一个注解 | 整个类，一个前缀全收 |
| **松散绑定**（Relaxed Binding） | ❌ `app.user-name` 绑不到 `userName` | ✅ `user-name` / `userName` / `user_name` / `USER_NAME` 都行 |
| **复杂类型** | ❌ List/Map/嵌套对象很难搞（只能靠 SpEL 硬拼） | ✅ List / Map / 嵌套对象 / `Duration` / `DataSize` 原生支持 |
| **类型安全** | ❌ 类型错误运行时才炸 | ✅ 启动时校验，可配 `@Validated` 做 JSR-303 |
| **IDE 元数据** | ❌ 没有 | ✅ 生成 `spring-configuration-metadata.json`，IDEA 自动补全 |
| **配置校验** | ❌ 不支持 | ✅ `@Validated` + `@NotNull` / `@Min` / `@Pattern` 等 |

### 松散绑定的含义（容易被追问）

```yaml
app:
  user-name: 茶荼      # kebab-case
  # 或 userName: 茶荼  # camelCase
  # 或 user_name: 茶荼 # snake_case
  # 或 USER_NAME: 茶荼 # 大写下划线（环境变量风格）
```
```java
@ConfigurationProperties(prefix = "app")
public class AppProps { private String userName; }   // ✅ 四种写法全都能绑上
```
```java
@Value("${app.userName}")   // ❌ app.user-name 绑不上
private String userName;
```

**为什么重要**：环境变量天然是大写+下划线（`APP_USER_NAME`），Docker/K8s 注入配置靠的就是松散绑定。**这是 `@ConfigurationProperties` 在云原生场景的硬优势。**

---

## 三、`${}` 与 `#{}` 的区别（高频考点，易错）

| 写法 | 谁解析 | 语义 |
|---|---|---|
| `${app.name}` | `PropertySourcesPropertyResolver` | **占位符查找**（从 Environment 取值） |
| `#{...}` | `SpelExpressionParser` | **SpEL 表达式求值** |

### 🔴 常见误解：「`#{'${app.name}'}` 是拿到值后再拿着这个值去配置文件找」

**不存在二次查找。** 正确拆解：

```java
@Value("#{'${app.name}'}")
```

1. **内层 `${app.name}` 先解析** → 得到字符串 `"茶荼"`
2. `${...}` 被包在**单引号里** → SpEL 把 `'茶荼'` 当作**字符串字面量**
3. SpEL 求值 → 结果就是 `"茶荼"`

**两个写法最终值完全一样**，区别只在**解析器不同**（一个走占位符解析，一个走 SpEL 求值）。

**为什么这么写？** 因为需要在 SpEL 里加工这个值：
```java
@Value("#{'${app.name}'.toUpperCase()}")          // 转大写
@Value("#{'${app.roles}'.split(',')}")            // 拆列表
@Value("#{'${app.name}' + '-' + '${app.port}'}")  // 拼接
```

**单写 `@Value("#{'${app.name}'}")` 是无意义的多余包装**，纯语法演示。

### 其他 @Value 语法

```java
@Value("${app.name}")             // 占位符
@Value("${app.name:默认值}")      // 带默认值（冒号后）
@Value("${app.name:#{null}}")     // 默认 null
@Value("#{2 + 3}")                // 纯 SpEL 计算
@Value("#{'${app.name}'.toUpperCase()}")  // 嵌套
```

> ⭐ **`${}` = 占位符查找，`#{}` = SpEL 运算。分工不同，不互相查找。**

---

## 四、@Value 的两个坑

### 坑 1：构造函数里拿不到 `@Value` 字段（值是 null）⭐⭐

```java
@Component
public class Bad {
    @Value("${app.name}")
    private String name;

    public Bad() {
        System.out.println(name);   // ← null！
    }
}
```

**原因**：`@Value` 注入在**属性赋值阶段（populateBean）**，构造函数执行在**实例化阶段**——**更早**。构造函数跑的那一刻，`@Value` 还没生效。

```
实例化（构造函数）  ← 这里 name 还是 null
     ↓
属性赋值（@Value 生效）  ← 这里才填值
     ↓
初始化
```

**解法**：用构造器注入 + `@Value` 参数

```java
public Good(@Value("${app.name}") String name) {
    this.name = name;   // ✅ 参数由 Spring 先解析好再传入
}
```

> 💡 这条跟循环依赖知识是**同一条时间轴**：「构造 → 属性注入 → 初始化」的顺序永远是关键。
> 也解释了为什么**构造器注入的循环依赖更难解**（依赖要在实例化阶段就给，但代理在初始化之后才生成）。

### 坑 2：复杂类型要用 SpEL 硬拼

```java
@Value("${app.servers}")                        // app.servers=a,b,c
private List<String> servers;                   // ❌ 需要自己 split

@Value("#{'${app.servers}'.split(',')}")        // ✅ 能跑但丑
private List<String> servers;

@Value("#{${app.map}}")                         // Map 更麻烦，容易类型转换异常
private Map<String, String> map;
```

对比 `@ConfigurationProperties`：
```yaml
app:
  servers: [a, b, c]
  map:
    k1: v1
    k2: v2
```
```java
@ConfigurationProperties(prefix = "app")
public class AppProps {
    private List<String> servers;              // ✅ 自动
    private Map<String, String> map;           // ✅ 自动
}
```

---

## 五、@ConfigurationProperties 的四种注册方式

```java
// ① @Component 直接标在类上（最简单，但耦合 Spring）
@Component
@ConfigurationProperties(prefix = "app")
public class AppProps {}

// ② ⭐ @EnableConfigurationProperties（官方推荐，Props 保持纯 POJO）
@Configuration
@EnableConfigurationProperties(AppProps.class)
public class AppConfig {}

// ③ @Bean + @ConfigurationProperties（适合需要控制创建过程）
@Bean
@ConfigurationProperties(prefix = "app")
public AppProps appProps() { return new AppProps(); }

// ④ @ConfigurationPropertiesScan（Boot 3 免逐个注册）
@SpringBootApplication
@ConfigurationPropertiesScan
public class App {}
```

---

## 六、🔴 为什么 starter 场景必须用 `@EnableConfigurationProperties`

> 这是本文最重要的一节。核心不是语法，是 **starter 的设计哲学**。

### 6.1 两种写法的实际差别

**写法 A：`@Component` 标在 Props 上**
```java
@Component
@ConfigurationProperties(prefix = "mql")
public class MqlProperties {
    private String endpoint;
    private int timeout;
    private List<String> retryOn;
    // getter/setter
}
```

**写法 B：`@EnableConfigurationProperties` + 纯 POJO**
```java
// ① Props 类是纯 POJO，一个 Spring 注解都没有
@ConfigurationProperties(prefix = "mql")
public class MqlProperties {
    private String endpoint;
    private int timeout;
    private List<String> retryOn;
    // getter/setter
}

// ② 在一个配置类里「登记」它
@Configuration
@EnableConfigurationProperties(MqlProperties.class)   // ← 关键
public class MqlAutoConfiguration {
    @Bean
    public MqlClient mqlClient(MqlProperties props) {
        return new MqlClient(props);
    }
}
```

**运行时效果完全一样**——容器里都会有一个填好值的 `MqlProperties` 单例。差异不在运行期，在**编译期和设计约束**。

### 6.2 澄清一个高频困惑：`@Bean` 方法返回的是 `MqlClient`，那 `MqlProperties` 谁创建？

**答：容器里有「两个」bean，不要混着看。**

| bean | 类型 | 谁创建的 |
|---|---|---|
| `mql-com.tea.MqlProperties` | `MqlProperties` | **`@EnableConfigurationProperties` 创建的**（不是 `@Bean` 方法返回的） |
| `mqlClient` | `MqlClient` | `@Bean` 方法返回的 |

**完整链条**：

```
@EnableConfigurationProperties(MqlProperties.class)
  → 注册 MqlProperties 的 BeanDefinition（我是谁、怎么造）
  → 实例化 new MqlProperties()（空壳，字段全 null）
  → ConfigurationPropertiesBindingPostProcessor 介入
  → 读 mql.* 前缀 → 反射 set 到字段（填值）
  → 完整的 MqlProperties bean 就绪 ✅
          ↓
mqlClient(MqlProperties props)   ← 参数从容器里取（依赖注入）
  → return new MqlClient(props)
```

**`@Bean` 方法的参数 = 从容器取 bean。** 它取到的 `MqlProperties` 是**别人早已创建好、填好值**的。

> ⭐ `@EnableConfigurationProperties` **管创建**；`@Bean public MqlClient mqlClient(...)` **管使用**。两件事，两个 bean，互不替代。

### 6.3 两种写法对照：谁负责「发起创建」

| 步骤 | 写法 A | 写法 B |
|---|---|---|
| **创建 bean** | **组件扫描**（`@ComponentScan` 扫到 `@Component` → 注册 BeanDefinition） | **`@EnableConfigurationProperties`**（显式注册 BeanDefinition，不走扫描） |
| 实例化 | Spring 容器 | Spring 容器（一样） |
| 填值 | `ConfigurationPropertiesBindingPostProcessor` | 同左（一样） |

**创建的是同一个 bean，区别只在「创建动作由谁发起」。**

### 6.4 `@EnableConfigurationProperties` 到底做了什么

```java
@Import(EnableConfigurationPropertiesRegistrar.class)   // ← 真正的动作
public @interface EnableConfigurationProperties { ... }
```

`EnableConfigurationPropertiesRegistrar` 干两件事：
1. 把你指定的类**注册成 BeanDefinition**（绕过包扫描）
2. 顺便把 `ConfigurationPropertiesBindingPostProcessor` 注册进来（**这才是真正做绑定的 BPP**）

**这就是它「更可靠」的原因——它绕过了包扫描这层不确定性，直接和容器对话。**

### 6.5 为什么 starter 不能用 `@Component`：三个问题

**问题 1：不能直接 new，单测必须起容器**

```java
// ❌ Props 标了 @Component —— 单测必须起容器
@SpringBootTest                          // 慢，几秒起步
class MqlPropertiesTest {
    @Autowired MqlProperties props;
    @Test void test() { ... }
}

// ✅ 纯 POJO —— 直接 new，毫秒级
class MqlPropertiesTest {
    @Test void test() {
        MqlProperties p = new MqlProperties();
        p.setTimeout(3000);
        // 直接测逻辑，不需要任何 Spring 环境
    }
}
```

**「可被单元测试直接 new 出来」不是理论洁癖，是每天都要用的效率。**

**问题 2：⭐ 包扫描边界陷阱（最隐蔽）**

```
你的 starter 包结构：
  com.tea.mql.starter
      └── MqlProperties      ← @Component 标在这

使用方的启动类：
  com.othercompany.app
      └── Application        ← @SpringBootApplication 扫 com.othercompany.app
```

**结果**：`@Component` **不生效**——组件扫描默认只扫启动类所在包及其子包，`com.tea.*` 根本没被扫到。

**更阴的是：它不报错。** 配置值全是 null，排查半天才发现是包扫描问题。

**问题 3：使用者被污染 / 无法控制**
使用者拿到你的库，类里带着 `@Component`，想当普通 POJO 用就尴尬了。

### 6.6 为什么 `@EnableConfigurationProperties` 不怕包扫描

- 自动配置类通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` **保证被加载**
- 挂在上面的 `@EnableConfigurationProperties` **一定执行**，不管包在哪
- 挂在业务类上则**依赖用户是否照做**，不保证

> ⭐ **这解决了 starter 最核心的诉求：引入依赖即可用（零配置）。**

### 6.7 追问：能不能把 `@EnableConfigurationProperties` 标在业务类上？

**技术上可以**——`@Import` 在任何 `@Component` 上都能生效，不限 `@Configuration`。

**但不推荐，三个理由**：

1. **`@Configuration` 被 CGLIB 增强，普通 `@Component` 不会**

| 类类型 | 是否 CGLIB | `@Bean` 方法调用行为 |
|---|---|---|
| `@Configuration` | ✅ 是 | 从容器取 bean（**单例保证**） |
| `@Component` | ❌ 否 | **普通 Java 方法调用**（每次 new 新的） |

2. **职责错位**——业务类不该负责「注册别人的 bean」，code review 会被打回
3. **starter 场景不能这么做**——用户必须手动加注解，破坏「引入依赖即用」

### 6.8 场景对照表

| 场景 | 挂在哪 | 原因 |
|---|---|---|
| **日常业务** | `@Component` 直接标 Props 类 | 省事，包扫描必然覆盖 |
| **业务里想分离** | `@Configuration` + `@EnableConfigurationProperties` | 语义清晰，Props 可单测 |
| **⭐ starter / 公共库** | **自动配置类** + `@EnableConfigurationProperties` | **必须**：import 文件保证加载，用户零配置 |
| 业务类 + `@Enable` | 业务类上 | **能跑但不推荐**：职责错位 + 无 CGLIB + 用户需手动做 |

**补充**：`@EnableConfigurationProperties` 可重复标注，内部 `Set` 去重，不会重复注册。

### 6.9 为什么 Boot 官方 starter 都这么写（面试加分）

| Starter | Props 类 | 注册方式 |
|---|---|---|
| `spring-boot-starter-jdbc` | `DataSourceProperties` | `@EnableConfigurationProperties` / `@Bean` |
| `spring-boot-starter-data-redis` | `RedisProperties` | 自动配置里注册 |
| `spring-boot-starter-web` | `ServerProperties` | 同上 |
| `mybatis-spring-boot-starter` | `MybatisProperties` | `@EnableConfigurationProperties` |

**去翻 `DataSourceProperties` 源码——类上只有 `@ConfigurationProperties`，没有 `@Component`。**

> 💡 面试可以这样说：**「Spring Boot 官方 starter 的 xxxProperties 都是纯 POJO，我在自研 starter 里也遵循这个约定。」**——这句话说明你读过源码，不是背的。

### 6.10 附带好处：IDE 元数据

```json
// META-INF/spring-configuration-metadata.json（由 spring-boot-configuration-processor 生成）
{
  "properties": [
    { "name": "mql.endpoint", "type": "java.lang.String",
      "description": "MQL 服务地址", "defaultValue": "localhost:8080" }
  ]
}
```

使用方在 `application.yml` 里**敲 `mql.` 就自动补全**，有中文说明和默认值提示。

> ⚠️ **此功能只对 `@ConfigurationProperties` 生效，`@Value` 生成不了。**
> **starter 的体验，一半靠这个文件。**

---

## 七、什么时候用哪个（选型决策）

| 场景 | 推荐 | 原因 |
|---|---|---|
| 只有 1-2 个简单的值 | `@Value` | 一个注解搞定，不必为一行配置开一个类 |
| 一组相关配置（数据库、第三方、业务参数） | `@ConfigurationProperties` | 聚合、类型安全、可校验 |
| 需要 List / Map / 嵌套结构 | `@ConfigurationProperties` | `@Value` 要 SpEL 硬拼 |
| 需要启动时校验配置合法性 | `@ConfigurationProperties` + `@Validated` | 快速失败，不等到运行期 |
| 需要 SpEL 表达式、条件逻辑（`#{...}`） | `@Value` | 只有它支持 SpEL |
| 写 starter / 公共组件 | `@ConfigurationProperties` + `@EnableConfigurationProperties` | 纯 POJO、不怕扫描、零配置 |

> ⭐ **一句话总结：能用 `@ConfigurationProperties` 就别用 `@Value`，除非你就只要一个值。**

---

## 八、加载时机差异（进阶，面试偶考）

- `@Value` 走 `AutowiredAnnotationBeanPostProcessor`（属性赋值阶段）
- `@ConfigurationProperties` 走 `ConfigurationPropertiesBindingPostProcessor`

**实际后果**：`@ConfigurationProperties` 的绑定**比 `@Value` 稍晚**。

在 `@PostConstruct` 中依赖 `@ConfigurationProperties` 的值通常没问题；但如果是在 `@ConfigurationProperties` 类的 setter 里读 `@Value` 注入的字段，**可能拿到 null**。

---

## 九、starter 剖析：角色、作用、适用边界

> 2026-10-19 追加（Boss 追问：starter 里有哪些角色？有什么作用？哪些功能适合做成 starter？）

### 9.1 为什么 starter 要拆两个模块

```
xxx-spring-boot-starter          ← 空壳（只有 pom，引入下面那个）
xxx-spring-boot-autoconfigure    ← 干活（代码全在这）
```

Boot 官方就是这么分的（`spring-boot-starter-web` + `spring-boot-autoconfigure`）。
**好处**：用户只引 starter，`autoconfigure` 作为传递依赖进来，用户看不到也不必关心内部结构。

### 9.2 一个 starter 里的 8 个角色

| # | 角色 | 是什么 | 干什么 |
|---|---|---|---|
| **1** | **AutoConfiguration 类** | `@AutoConfiguration` 标在配置类上 | **总装配图纸**：决定「什么条件下创建什么 bean」 |
| **2** | **Properties 类** | `@ConfigurationProperties(prefix="xxx")` | **配置载体**：把 yml 的值变成对象 |
| **3** | **业务 Bean** | `XxxClient` / `XxxTemplate` / `XxxManager` | **真正干活的东西**，用户最终用的就是它 |
| **4** | **注册文件** | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` | **登记表**：告诉 Boot「我有哪些自动配置类要加载」 |
| **5** | **条件注解** | `@ConditionalOnClass` / `@ConditionalOnMissingBean` / `@ConditionalOnProperty` | **门禁**：什么条件下才生效 |
| **6** | **configuration-processor** | `spring-boot-configuration-processor` 依赖 | 编译期生成 metadata.json，让用户 yml 有自动补全 |
| **7** | **imports 文件** | Boot 2.7+ 新位置（旧版是 `spring.factories`） | 见角色 4 |
| **8** | **配套类** | 自定义异常 / 常量 / 枚举 | 可选，给用户用的 |

### 9.3 角色关系图

```
用户项目
  └── 引入 xxx-spring-boot-starter（空壳）
        └── 传递依赖 → xxx-spring-boot-autoconfigure
              ├── META-INF/spring/....AutoConfiguration.imports   ← 角色4：登记
              │         └─ 声明 → XxxAutoConfiguration
              │
              ├── XxxAutoConfiguration (@AutoConfiguration)       ← 角色1：装配
              │     ├─ @EnableConfigurationProperties(XxxProps.class)
              │     ├─ @ConditionalOnClass(XxxClient.class)       ← 角色5：门禁
              │     └─ @Bean @ConditionalOnMissingBean
              │           → new XxxClient(props)                 ← 角色3：业务 bean
              │
              ├── XxxProperties (@ConfigurationProperties)        ← 角色2：配置
              │
              └── spring-configuration-metadata.json             ← 角色6：IDE 提示
```

### 9.4 典型代码骨架

```java
// 角色4：META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
// 内容一行： com.tea.mql.starter.MqlAutoConfiguration

// 角色1 + 角色5
@AutoConfiguration                      // Boot 3 / 2.7+ 专用注解
@ConditionalOnClass(MqlClient.class)    // classpath 有 MqlClient 才生效
@EnableConfigurationProperties(MqlProperties.class)
public class MqlAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean           // ★ 用户自己定义了就不用我的
    public MqlClient mqlClient(MqlProperties props) {
        return new MqlClient(props);
    }
}

// 角色2
@ConfigurationProperties(prefix = "mql")
public class MqlProperties { ... }
```

### 9.5 starter 的四大作用

> **一句话：starter = 把「引依赖 → 加配置 → 搜包扫描 → 手动 new」这串手动操作，压成「引依赖就行」一件事。**

| # | 作用 | 说明 |
|---|---|---|
| **1** | **依赖聚合** | 一次性打包好所有 jar，版本对齐好，用户不用管版本。这是 starter **最初的目的**（名字就是这个意思） |
| **2** | **自动配置** ⭐ | 引依赖后 bean 自动创建好，用户不需要写任何 `@Bean` |
| **3** | **零配置可用 + 可覆盖** | 有合理 DefaultValue，用户改 yml 即可；想自定义就自己写 `@Bean`（靠 `@ConditionalOnMissingBean` 让位） |
| **4** | **封装复杂度** | 内部实现（连接池/重试/线程池/序列化）全封装，用户只看到 `mqlClient.send()` |

> ⭐ **设计灵魂：「约定优于配置」+「显式覆盖隐式」。**

> 💡 **面试加分句：「starter 的本质是把『变化的部分（配置）』暴露给用户，把『不变的部分（装配逻辑）』封装起来。」**

### 9.6 什么样的功能适合做成 starter —— 4 个必须满足的标准

| 标准 | 说明 | 反例 |
|---|---|---|
| **1. 跨项目复用** | 至少 2 个项目要用 | 只在一个项目用的工具类 |
| **2. 有「装配逻辑」** | 创建过程有套路（配连接池/设参数/初始化） | 纯静态工具类 |
| **3. 依赖第三方/外部系统** | 需要固定版本、统一客户端 | 项目内纯业务代码 |
| **4. 有配置项** | 不同环境/项目要改参数 | 完全无配置的常量 |

> ⚠️ **反例很重要：纯工具类不该做 starter。**
> 写个 `DateUtils` 打包成 starter 是灾难——用户得引一整个自动配置就为了一行 `format()`。**工具类做普通 jar 就行。**

### 9.7 适合做成 starter 的六大类场景

| 类别 | 例子 | 为什么适合 |
|---|---|---|
| **① 中间件/外部系统客户端** ⭐最常见 | `mql-spring-boot-starter`、自研 RPC/MQ | 连接管理、重试、序列化都是套路，项目间完全一致 |
| **② 云厂商/第三方服务 SDK** | aliyun-oss、wechat-pay、sms | AppKey/Secret/Endpoint 是配置，HTTP 客户端创建是套路 |
| **③ 公司内部公共能力** ⭐最有优势 | audit-log（审计日志）、permission、trace（全链路）、idempotent（幂等）、file-storage | 每个业务项目都要，但实现完全一样 |
| **④ 技术基座/基础设施** | mybatis、redis、xxl-job、dynamic-datasource | 装配复杂度极高，不封装没法用 |
| **⑤ 监控/运维** | prometheus 增强、统一异常上报、统一日志格式 | 团队标准，必须统一 |
| **⑥ 统一规范/脚手架** | unified-response（统一返回+全局异常）、web-common（跨域/时间格式/拦截器）、swagger 聚合 | 团队规范统一的前提——不靠人遵守，靠依赖强制 |

### 9.8 做 starter 的 5 个坑

| 坑 | 说明 |
|---|---|
| **1. 包扫描边界** | 别用 `@Component`，用 `@AutoConfiguration` + imports 文件（见 6.5） |
| **2. 别用 `@ComponentScan`** | starter 里写 `@ComponentScan` 会扫到用户包，**污染用户容器** |
| **3. 必须给 `@ConditionalOnMissingBean`** | 否则用户无法覆盖你的默认 bean |
| **4. 自动配置类别加 `@EnableAspectJAutoProxy` 等** | 可能和用户已有配置冲突 |
| **5. 版本对齐** | starter 的 Boot 版本要尽量宽松（`<scope>provided</scope>` 或 `<optional>true</scope>`） |

### 9.9 建议的第一个 starter（结合 C919 真实素材）

**最值得做的第一个：`audit-log-spring-boot-starter`（审计日志）**

理由：
1. **有真实素材** —— C919 的接口日志体系就是做过的
2. **技术含量够** —— 涉及 `@Aspect`、`ThreadLocal`、异步落库、`@ConditionalOnMissingBean` 多实现（可插拔存储）
3. **面试能讲** —— 「我封装了一个审计日志 starter，注解即可接入」是**作品**，不是「学过」
4. **规模适中** —— 250 行左右能跑通

---

## 十、一分钟速查

```
本质        @Value = 逐字段解析    |  @CP = 整对象绑定
BPP         AutowiredAnnotationBPP |  ConfigurationPropertiesBindingBPP
松散绑定     ❌                     |  ✅ 四种命名都行
复杂类型     ❌ 靠 SpEL              |  ✅ List/Map/嵌套/Duration
校验         ❌                     |  ✅ @Validated
IDE 提示     ❌                     |  ✅ metadata.json
${} vs #{}  占位符查找 vs SpEL 求值（不是二次查找！）
@Value 坑   构造函数里是 null（构造 < 属性注入）
starter     纯 POJO + @EnableConfigurationProperties（不用 @Component）
```
