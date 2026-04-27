## Spring Boot启动流程
* 核心概念一句话: 本质上就是 **先筹备，再开张** 。先通过 `SpringApplication` 把环境、**监听器**、**初始化器** 等基础资源准备然后创建并刷新Spring 容器。把所有的Bean和Tomcat都启动起来（开张）。

* 宏观流程（两大阶段）
      * 筹备阶段：**实例化SpringApplicaiton**在main方法里面new出来的那个对象。它会做以下事情。
          * 1.推断应用类型，看看classpath下有没有Servlet类，判断普通应用还是Web应用
          * 2寻找加载拓展，去`spring.factories`里面把能帮忙的“初始化器”和“监听器”都找出来存好（缓存到一个Set对象里面）
          * 3.找到包含 `main`方法的那个启动类，作为后续扫描配置的出发点和大本营
      * 开张阶段: **执行run()方法阶段**
          * 1.发信号，高速所有监听器，“我要开始启动了”（发布`ApplicationStartingEvent`）
          * 2.准备环境：根据应用类型，通过反射创建Spring容器(ApplicationContext)
          * 3 刷容器**最核心**：调用 `refreshContext`。这一步会让Spring扫描并创建所有的Bean同时启动内嵌的Tomcat服务
          * 收尾工作:打印启动成功的Banner和耗时日志，并且依次执行开发者自己定义的`Runner` （比如启动后预热缓存）

## 关键组件大白话解释
* SpringApplication：整个启动流程的总导演，负责安排什么时候该干什么事。
* ApplicationContextInitializer（初始化器）：装修队。在Spring容器刚建好、但是还没有refresh之前做最好的配置修改。
* ApplicationListener（监听器）：情报员。分布在启动的各个节点，一旦导演发出某个阶段的事件（比如环境准备好了），它们里面执行对应的逻辑
* spring.factories:人才市场名录。SpringBoot通过它，不需要硬编码就能发现比加载各种框架自带的扩展组件。

## 常见避坑误解
* 避坑：启动类一定要放在项目的根包路径下，因为Spring Boot默认只扫描启动类所在的包及其子包，放错了位置 `@Controller`和 `@Service` 就全失效了

* 关键词: 在 `SpringApplication` 构造时，加载初始化器和监听器
* 在自动装配时，加载了所有的自动配置类，底层其实用来静态缓存，物理上的文件IO扫描只会发生一次，不会重复消耗性能


## 自测题目与参考回答
* Q1:Spring Boot 启动时，内嵌的Tomcat是在哪个阶段被启动的？
* A1: Tomcat是在 `run` 方法中的`refreshContext(context)` 阶段被启动的。 `refreshContext` 调用了Spring容器最核心的`refresh()`方法，在容器刷新、所有的Bean示例化完毕后，Spring Boot会识别出当前是Web应用，进而创建并启动内嵌的Tomcat服务器。
* Q2如果我想在 Spring Boot 启动完成后，立刻执行一段自定义的代码（比如预热缓存），应该怎么做？
* A2：可以实现Spring Boot提供的 `ApplicationRunner` 或 `CommandLinerRunner` 接口并且将实现类提交给Spring 管理(加上 `@Component`) 在启动流程的最好一步，Spring Boot会自动找到这些Runner并执行它们的`run`方法
* Q3 为什么 Spring Boot 启动时能自动扫描到我的 @Controller 和 @Service？
* A3 因为启动类上的 `@SpringBootApplication` 注解包含了一个 `@ComponentScan`注解。Spring Boot会以启动类所在包位基准，自动向下扫描所有的子包，识别并注册带有 `@Component`、`@Service`、`@Controller` 等注解的类到IOC容器中

## Spring Boot的启动流程

*  pring Boot 的启动流程可以宏观地分为‘筹备’和‘开张’两大阶段。
    首先是筹备阶段，也就是 SpringApplication 对象的实例化过程。这一步主要做了三件事：
       * 第一，根据类路径推断当前是普通的 Java 应用还是 Web 应用；
       * 第二，利用 SPI 机制从 spring.factories 中加载所有的初始化器（Initializer）和监听器（Listener）；
       * 第三，锁定包含 main 方法的启动类作为配置源。
    紧接着进入核心的开张阶段，也就是执行 run 方法。这个过程像一条流水线：
       * 先向监听器发布‘启动开始’的事件，并注册一个 JVM 关闭钩子以保证优雅停机。
       * 准备环境，加载 application.yml 和命令行参数，并确立配置优先级。
       * 根据应用类型创建对应的 Spring 容器（ApplicationContext）。执行最关键的 refreshContext 刷新容器。这一步会触发 Spring 框架底层的 refresh()，完成所有 Bean 的扫描、实例化、依赖注入，同时启动内嵌的 Tomcat。
        * 容器刷新完毕后，发布启动完成事件，并依次调用所有 ApplicationRunner 和 CommandLineRunner 的 run 方法，方便我们执行启动后的自定义逻辑。