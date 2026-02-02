# 该记录用于梳理SpringBoot框架的

## 起点我为什么要学习SpringBoot框架

* 技术栈的迭代，国内javaWeb项目主流的技术栈，学习它能够让我把项目经验转换成可以迁移的资本（核心驱动力）
* 期望能够了解主流框架的设计思路

## 知识图景1 关于SpringBoot的核心设计思路

* 约定大于配置，早期javaweb项目有许多配置文件和配置项，原本的目的是为了方便实现细粒度的控制，但是在实际项目中90%的项目
  都是标准的配置，这造成了需要写很多重复的模板代码，SpringBoot中，有许多配置都是默认的比如: 最重要的项目的解构。在我了解 的其他技术栈中，例如vue是可以配置源文件文件夹的路径的，SpringBoot则有一个默认的文件夹解构，好处就是如果开发都遵循相同的原则进行开发的话，协作者可以很方法的找到对应代码的位置
* 灵活且丰富的配置项体现，支持 xml/yml/properties 配置文件 ，在多环境情况下，可以配置类似 application.yml  application-dev.yml application-prod.yml 这样的多配置文件（在application.yml文件中配置具体激活哪个环境的配置文件）
  在代码中可以直接使用 @value 方式直接读取配置文件给变量赋值 或者 @ConfigurationProperties 给对象配置
* 提供了端点，可以通过端点了解服务上的各种信息（需要配置放开端点）
* 和常用的技术有很好的整合 （这下面涉及的技术栈都需要去了解）
  * 数据库 MyBatis /JPA /JDBC
  * 缓存 Redis,Caffeine
  * 消息队列 RabbitMQ,kafka
  * 安全:Spring Security +JWT/OAuth2 (我记得Vue3也有JWT的包，这个需要注意)
  * 异步 `@Async` ,`@Scheduled`
* 部署打包方便 ， 既可以通过 `mvn package` ->生成可执行JAR ，也可以支持Docker容器化  ，支持云原生
* 起步依赖  比如 `Spring-boot-starter-web` 不是一个库，而是一个依赖集合：
  * 内嵌tomcat
  * Spring MVC
  * JackSon
  * 日志
* 注解驱动&控制反转(IoC)/依赖注入(DI) : Spring Framework的能力，Spring Boot在其基础上做了增强
  核心注解(你已经记录了很多遍了，再记忆一次):
  * `@Componennt`, `@service`,`@Repository` , `@Controller` ->声明式bean
  * `@Autowired` -->自动依赖注入
  * `@Configuration` + `@Bean` -->手动定义Bean
  * `@SpringBootApplication` **=** `@Configuration` **+** `@EnableAutoConfiguration` **+** `@ComponentScan` 注解SpringBootApplication 是这三个注解的合并
