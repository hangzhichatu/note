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

## 注解笔记
* SpringBootApplication  
**SpringBootApplication**   说明了这个类是一个springboot应用，如果缺少该注解那么不会被识别到，它是一个组合注解，其下还有很多其他的注解。
* `@SpringBootConfiguration` 是 `@SpringBootApplication` 的注解之一 —— Spring Boot 的配置
    * `@Configuration`：Spring 配置类
    * `@Component`：说明这也是一个 Spring 组件

* `@EnableAutoConfiguration` 是 `@SpringBootApplication` 的注解之一 —— 自动配置
    * `@AutoConfigurationPackage`：自动配置包
    * `@Import(AutoConfigurationPackages.Registrar.class)`：导入选择器

* *SpringApplication.run（）* 该方法是一个静态方法，通过 **反射** 的方式加载DemoApplication类，并且传递了参数。



