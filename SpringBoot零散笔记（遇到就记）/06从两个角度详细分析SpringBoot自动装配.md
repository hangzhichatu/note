## 从项目启动的角度从源码分析

### 一、 核心概念与关系

* 1.自动装配类（XXXAutoConfiguration）
  作用:根据 classpath 上是否存在某些类、是否配置了某些属性等条件 **自动创建并且注册spingBean,通常位于 `spring-boot-autoconfigure` 模块中**
* 2.默认配置属性类(xxxXXXProperties) 使用 `@ConfigurationProperties(prefix = "prexxx")` 注解 用于将 `application.properties` 或 `application.yml` 中以 `xxx.` 开头的配置项绑定到 Java 对象。通常被自动装配类注入使用
* 3. 用户自定义配置文件 `application.yml `用户在 `application.properties `/`application.yml `中写入的配置（如 `myapp.name=hello`）Spring Boot 会自动将这些值绑定到对应的 `XXXProperties` 类中。

### 二、底层作用机制详细解释

* 启动流程入口 ：启动类的注解 ``@SpringBootApplication`` 其中包含子注解 ``@EnableAutoConfiguration``， 子注解 ``@EnableAutoConfiguration``又会导入注解 ``AutoConfigurationImportSelector``  该注解会加载自动装配类
* 在上一步的继承上，会读取配置文件 ``META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`` 获取所有的预定义的 ``XXXAutoConfiguration``自动配置类列表
* 但是这些自动配置类并不一定都会自动配置，在这些类上通过注解进行了条件筛选如（如 `@ConditionalOnClass`, `@ConditionalOnMissingBean` 等，第一个注解要calss下又有指定类才会自动配置，第二个如果已经有同类的bean对象情况下也不会注解）
* 属性绑定 `XXXProperties` 被声明为 `@ConfigurationProperties`，并通过 `@EnableConfigurationProperties(XXXProperties.class)` 或直接作为 `@Bean` 注册，Spring Boot自动将配置文件中绑定的值绑定到该对象（注意这里有默认值也会被application的配置文件属性给覆盖）
* **用户覆盖默认值** ：如果用户在 `application.yml` 中写了同名配置，会覆盖 `XXXProperties` 中的默认值，自动装配类使用的是最终绑定后的 `XXXProperties` 实例

### 三、简单自定义例子

* 定义properties类

  ```java
  java @ConfigurationProperties(prefix = "myredis")
  public class MyRedisProperties {
      private String host = "localhost"; // 默认值
      private int port = 6379;

      // getter/setter
      public String getHost() { return host; }
      public void setHost(String host) { this.host = host; }
      public int getPort() { return port; }
      public void setPort(int port) { this.port = port; }
  }


  ```
* 定义自动装配类

  ```java
  @Configuration
  @ConditionalOnClass(RedisTemplate.class) // 只有 classpath 有 RedisTemplate 才生效
  @EnableConfigurationProperties(MyRedisProperties.class) // 启用属性绑定
  public class MyRedisAutoConfiguration {

      @Bean
      @ConditionalOnMissingBean // 如果用户没自己定义 RedisConnection，就用这个
      public RedisConnection redisConnection(MyRedisProperties properties) {
          return new RedisConnection(properties.getHost(), properties.getPort());
      }
  }
  ```
* 用户配置

  ```yaml
  myredis:
    host: 192.168.1.100
    port: 6380
  ```
* 启动时候执行的的操作:

  发现 `MyRedisAutoConfiguration` 在自动装配列表中（需注册到 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`）检查 classpath 是否有 `RedisTemplate`（假设你引入了 spring-boot-starter-data-redis）,创建 `MyRedisProperties` 实例，并将 `myredis.host` 和 `myredis.port` 绑定进去（用户配置覆盖默认值）调用 `redisConnection()` 方法，传入绑定后的 properties，创建 `RedisConnection` Bean


## 从bean对象声明周期角度去了解自动装配

### 一、前提：自动装配的本质

* Spring Boot 的自动装配类（如 `RedisAutoConfiguration`）本质上是一个  **带条件的 `@Configuration` 类** 。它定义了若干 `@Bean` 方法，这些方法在满足条件时会被 Spring 容器调用，从而 **创建并注册 Bean 实例** 。所以，自动装配只是  **Bean 创建的一种触发方式** ，而 Bean 本身的生命周期流程与普通 Bean 完全一致。

### 二、Bean生命周期关键阶段(以自动装配的StringRedisTemplate为例)

    我们假设你引入了`spring-boot-starter-data-redis`，Spring Boot 自动装配生效。

* 阶段 1️⃣：**发现与条件判断（Pre-Creation）**Spring Boot 启动时，通过 `@EnableAutoConfiguration` 扫描 `META-INF/spring/.../AutoConfiguration.imports`找到 RedisAutoConfiguration 类，检查注解上的条件是否满足。
* 阶段2️⃣：**BeanDefinition 注册（Registration）**
  Spring 解析 `RedisAutoConfiguration` 中的 `@Bean` 方法：
  ```java
  @Bean
  @ConditionalOnMissingBean
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) { ... }
  ```

  * 生成一个 `BeanDefinition` 对象，包含：
    * beanName: `"stringRedisTemplate"`
    * beanClass: `StringRedisTemplate`
    * factoryMethod: `stringRedisTemplate`
    * 依赖: `RedisConnectionFactory`
    * 条件: `@ConditionalOnMissingBean`
  * 将该 `BeanDefinition` 放入 `beanDefinitionMap`

    ps：此时仍**没有实例** ，只有“蓝图”。

* 阶段 3️⃣：**实例化前检查（Pre-Instantiation Check） Spring 开始初始化单例 Bean 时，会检查 `stringRedisTemplate` 是否满足 `@ConditionalOnMissingBean` ，以及其他容器是否有同类型的Bean对象**
* 阶段 4️⃣：**依赖解析与实例化（Instantiation）**

  * Spring 发现 `stringRedisTemplate()` 方法依赖 `RedisConnectionFactory`
  * 先去创建或查找 `RedisConnectionFactory` Bean（它可能也是自动装配的）
  * 调用 `stringRedisTemplate(connectionFactory)` 方法
  * 得到一个 `StringRedisTemplate` 实例
* 阶段 5️⃣：**属性填充与后置处理（Post-Processing）**

  虽然 `StringRedisTemplate` 是通过工厂方法创建的，但 Spring 仍会对其应用标准生命周期回调：

  #### a. `BeanNameAware` / `BeanFactoryAware` 等（如果实现）


  * 一般不会用到

  #### b. **`BeanPostProcessor` 处理**

  * 所有注册的 `BeanPostProcessor` 会依次处理该 Bean
  * 例如：
    * `ConfigurationPropertiesBindingPostProcessor`：绑定 `@ConfigurationProperties`
    * `AutowiredAnnotationBeanPostProcessor`：处理 `@Autowired` 字段（但 `StringRedisTemplate` 内部通常不需要）
  * 调用顺序
* 阶段 6️⃣：**放入单例池（Ready for Use）**
* 阶段 7️⃣：**使用阶段（In Use）**

  * 也可以通过
  * 你的 Service、Controller 等组件通过依赖注入获得该 Bean

  ```
  template.opsForValue().set("key","value");
  ```

  来调用
* 销毁阶段
* 流程图

```plaintext
                              ┌───────────────────────────────┐
                              │  Spring Boot 应用启动         │
                              └──────────────┬────────────────┘
                                             │
                                             ▼
                   ┌───────────────────────────────────────────────┐
                   │ 扫描自动装配类 (如 RedisAutoConfiguration)     │
                   │ 通过 META-INF/spring/.../AutoConfiguration.imports │
                   └───────────────────────┬───────────────────────┘
                                           │
                                           ▼
               ┌───────────────────────────────────────────────────────┐
               │ 条件评估 @ConditionalOnClass(RedisOperations.class)    │
               │ ┌─────────────┐   是   ┌───────────────────────────┐  │
               │ │ classpath   ├───────►│ 将配置类纳入候选           │  │
               │ │ 有该类？    │        └─────────────┬─────────────┘  │
               │ └─────────────┘                      │                │
               │        否                            ▼                │
               │ ┌─────────────────┐       ┌─────────────────────────┐│
               └─┤ 跳过该配置类     ◄───────┤ 不注册任何相关 Bean      ││
                 └─────────────────┘       └─────────────────────────┘│
                                                                     │
                                                                     ▼
                       ┌───────────────────────────────────────────────────────┐
                       │ 解析 @Bean 方法，注册 BeanDefinition                  │
                       │ 名称: "stringRedisTemplate"                           │
                       │ 类型: StringRedisTemplate                             │
                       │ 条件: @ConditionalOnMissingBean                       │
                       └───────────────────────┬───────────────────────────────┘
                                               │
                                               ▼
             ┌───────────────────────────────────────────────────────────────────┐
             │ 实例化前：检查 @ConditionalOnMissingBean                          │
             │ ┌──────────────────────┐   无同类型Bean   ┌─────────────────────┐ │
             │ │ 容器中已有            ├─────────────────►│ 继续创建此 Bean      │ │
             │ │ StringRedisTemplate? │                  └─────────┬───────────┘ │
             │ └──────────────────────┘                            │             │
             │          有                                         ▼             │
             │ ┌──────────────────────┐                ┌──────────────────────┐ │
             └─┤ 跳过此 @Bean 方法     ◄────────────────┤ 不创建自动装配 Bean   │ │
               └──────────────────────┘                └──────────────────────┘ │
                                                                               │
                                                                               ▼
                     ┌───────────────────────────────────────────────────────────────┐
                     │ 依赖解析：查找/创建 RedisConnectionFactory                    │
                     │ （可能也是自动装配的 Bean）                                   │
                     └─────────────────────────┬─────────────────────────────────────┘
                                               │
                                               ▼
                        ┌──────────────────────────────────────────────────────┐
                        │ 调用 @Bean 方法：                                     │
                        │ stringRedisTemplate(connectionFactory)               │
                        │ → 创建 StringRedisTemplate 实例                       │
                        └───────────────────────┬──────────────────────────────┘
                                                │
                                                ▼
              ┌───────────────────────────────────────────────────────────────────────┐
              │ 初始化后处理：                                                     │
              │ 1. BeanPostProcessor#postProcessBeforeInitialization()               │
              │ 2. @PostConstruct / InitializingBean#afterPropertiesSet()（如有）     │
              │ 3. BeanPostProcessor#postProcessAfterInitialization()                │
              └───────────────────────────────┬───────────────────────────────────────┘
                                              │
                                              ▼
                   ┌───────────────────────────────────────────────────────────────┐
                   │ 注册到单例池：                                                 │
                   │ singletonObjects.put("stringRedisTemplate", 实例)              │
                   └───────────────────────────────┬───────────────────────────────┘
                                                   │
                                                   ▼
                        ┌──────────────────────────────────────────────────────┐
                        │ 应用运行期：                                            │
                        │ 任何组件可通过 @Autowired / getBean() 获取并使用该 Bean │
                        └───────────────────────────────┬──────────────────────┘
                                                        │
                                                        ▼
                         ┌───────────────────────────────────────────────────────┐
                         │ 应用关闭（ApplicationContext.close()）                  │
                         └───────────────────────────────┬───────────────────────┘
                                                         │
                                                         ▼
               ┌───────────────────────────────────────────────────────────────────────┐
               │ 销毁回调：                                                           │
               │ - @PreDestroy 方法（如有）                                            │
               │ - DisposableBean#destroy()（如有）                                    │
               │ （StringRedisTemplate 通常无显式销毁逻辑）                            │
               └───────────────────────────────────────────────────────────────────────┘
```

流程图如上
