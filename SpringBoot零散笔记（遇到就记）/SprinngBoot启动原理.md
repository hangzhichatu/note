这里我们记录下SpringBoot启动时候的自动装配原理 



考虑一个springBoot项目启动的时候，它是根据哪些条件来实现自动装配的呢？
答案是 其项目内的pom.xml 文件 它定义springBoot启动的时候的启动器

```xml
<!-- 示例：Spring Boot starter 片段 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter</artifactId>
  <version>2.7.5</version>
</dependency>
```

spring-boot-stater-web 会自动帮我们导入所有web环境的所有依赖

那么**启动**的时候，spring是怎么识别主程序的呢？ 
这依赖于注解
## 展示注解起作用的代码
```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @SpringBootApplication 是核心注解，
 * 等同于 @Configuration + @EnableAutoConfiguration + @ComponentScan
 * SpringBoot 在启动时会识别并触发自动装配与组件扫描。
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```
*  ps:*SpringApplication.run（）* 该方法是一个静态方法，通过 **反射** 的方式加载DemoApplication类，并且传递了参数。


## 注解笔记
* SpringBootApplication  
**SpringBootApplication**   说明了这个类是一个springboot应用，如果缺少该注解那么不会被识别到，它是一个组合注解，其下还有很多其他的注解。
* `@SpringBootConfiguration` 是 `@SpringBootApplication` 的注解之一 —— Spring Boot 的配置
    * `@Configuration`：Spring 配置类
    * `@Component`：说明这也是一个 Spring 组件

* `@EnableAutoConfiguration` 是 `@SpringBootApplication` 的注解之一 —— 自动配置
    * `@AutoConfigurationPackage`：自动配置包
    * `@Import(AutoConfigurationPackages.Registrar.class)`：导入选择器
    * 说明 该注解通过 `@Import(AutoConfigurationImportSelector.class)` 导入一个选择器类：`AutoConfigurationImportSelector` 这个类的作用是在 Spring 容器启动时，**动态加载所有符合条件的自动配置类**（xxxAutoConfiguration）。
* 自动装配类的来源：`spring.factories`  Spring Boot 在 META-INF/spring.factories 文件中定义了所有可能的自动配置类，例如：
    ```Properties
        # Auto Configure
    org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
    org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration,\
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
    ```
    注意：从 Spring Boot 2.7 开始，官方推荐使用 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 文件替代 `spring.factories`，但原理相同。
    `AutoConfigurationImportSelector` 会读取这些文件，将列出的自动配置类加入到 Spring 容器的候选 Bean 中。

* `@ComponentScan` 是`@SpringBootApplication` 的注解之一



* *二条件化装配*：`@Conditional` 注解说明  自动配置类虽然被加载，但不会无条件生效。它们通常配合         @ConditionalOn... 系列注解，只有满足特定条件时才会真正注册 Bean。常见注解体条件
    * `@ConditionalOnClass` 当 classpath 中存在指定类时生效
    * `@ConditionalOnMissingBean` 当容器中没有指定类型的 Bean 时生效
    * `@ConditionalOnProperty` 当配置文件中存在指定属性且值匹配时生效
    * `@ConditionalOnWebApplication` 当应用是 Web 应用时生效

```java
//示例：DataSourceAutoConfiguration  classpath 中有 DataSource 类（比如引入了 spring-boot-starter-jdbc）并且用户没有自己定义 DataSource Bean 这个自动配置类才会生效。
@Configuration
@ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class })
@ConditionalOnMissingBean(DataSource.class)
public class DataSourceAutoConfiguration {
// 自动配置数据源
}
```


## 三、自动装配的执行顺序

* 启动 `Spring Boot`应用 → 扫描 `@SpringBootApplication`
* 触发 `@EnableAutoConfiguration` → 加载 `AutoConfigurationImportSelector`
* 读取所有 `spring.factories` 或 `AutoConfiguration.imports` 中的自动配置类
* 对每个自动配置类，检查其上的 `Conditional`. 条件
* 满足条件的配置类被解析，其中的 `@Bean` 方法注册到 Spring 容器

## 四、如何自定义自动装配？

* 创建一个自动配置类（如 MyServiceAutoConfiguration）
* 使用 `@Configuration` + 条件注解
* 在 `resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中注册该类
* 打包为 starter（可选），供其他项目依赖使用

## 五、总结

* 入口 `@SpringBootApplication` → `@EnableAutoConfiguration`
* 配置类发现机制 通过 `spring.factories` 或 `AutoConfiguration.imports`
* 条件控制 `@ConditionalOn`... 系列注解实现按需装配



## 六、代码示范：如何写一个自动配置类的@Bean方法并且使用它

* 1. 自动配置类中的 @Bean 方法（定义 Bean）
    ```java
        // 文件：MyUtilAutoConfiguration.java

    import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(MyUtil.class)
    public class MyUtilAutoConfiguration {

        @Bean  // ←←← 这就是注册 Bean 的关键注解！
        public MyUtil myUtil() {
            return new MyUtil();  // ← 返回的对象会被 Spring 容器管理
        }
    }
    ```
    说明 ：
    * @Bean 注解标记在方法上；
    * 方法名 myUtil 就是这个 Bean 在 Spring 容器中的默认名称；
    * 返回值 new MyUtil() 就是实际被注册为 Bean 的对象；
    * 整个方法的作用等价于 XML 中的 <bean class="com.example.demo.MyUtil"/>。
    *  注意：@Bean 方法必须写在被 @Configuration 标记的类中（或 @Component 等能被组件扫描到的类中），否则不会生效。
* 2.主应用中“使用”这个 Bean（注入并调用）
    ```java
    // 文件：DemoApplication.java

        @RestController
        public class DemoApplication {

            private final MyUtil myUtil;  // ← 声明依赖

            // 构造函数注入（推荐方式）
            public DemoApplication(MyUtil myUtil) {  // ← Spring 自动传入上面 @Bean 创建的实例
                this.myUtil = myUtil;
            }

            @GetMapping("/hello")
            public String hello() {
                return myUtil.sayHello("Spring Boot");  // ← 调用 Bean 的方法
            }
        }
    ```
    * MyUtil myUtil 是通过 依赖注入（DI） 获取的；
    * Spring 在启动时发现容器里有一个类型为 MyUtil 的 Bean（由 myUtil() 方法创建），于是自动注入；
    * 这就是“使用 Bean”的典型方式。
* 3.逻辑导图
    定义 Bean	告诉 Spring “请把这个对象放进容器”	
    ```java
    @Bean
    public MyUtil myUtil() {    return new MyUtil();}
    ```
    使用 Bean	从容器中取出并使用	
    ```java
    public DemoApplication(MyUtil myUtil) { ... }
    ```
* 4.深入思考
    Q：为什么不用 new MyUtil() 而要用 @Bean？
    A：因为 @Bean 让对象由 Spring 容器管理，支持：

        依赖注入（DI）
        生命周期回调（如 @PostConstruct）
        AOP 代理（如事务、缓存）
        条件装配（如 @ConditionalOnProperty）
    Q：@Bean 方法可以有参数吗？
    A：可以！Spring 会自动从容器中查找匹配的 Bean 作为参数：

    ```java
        @Bean
    public MyService myService(MyUtil myUtil) {
        return new MyService(myUtil); // 自动注入 myUtil
    }
    ```