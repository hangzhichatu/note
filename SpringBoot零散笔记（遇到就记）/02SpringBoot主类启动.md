## Springboot主启动类

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

* `SpringApplication.run` 分析 主要做了以下四部分
  * 1. 推断应用的类型是普通的项目还是Web项目
  * 2. 查找并加载所有可用的初始化器，设置到initializers属性中
       关于初始化器:**初始化器（Initializer**通常指的是实现了 ApplicationContextInitializer **接口的类**。它的主要作用是在 Spring 应用上下文（ConfigurableApplicationContext）被刷新（refresh）之前，对上下文进行一些自定义的初始化操作。
       ApplicationContextInitializer 是什么？：
       这是 Spring 框架提供的一个回调接口：
       ```java
           public interface ApplicationContextInitializer<C extends            ConfigurableApplicationContext> {
           void initialize(C applicationContext);
           }
       ```

    它允许你在 Spring 容器完全启动前，对 ApplicationContext 进行配置或修改。
    执行时机：在 refresh() 方法调用之前，但在环境（Environment）已经准备好的时候。
  * 3. 查找出所有的应用程序监听器，设置到listenrs属性中
  * 4. 推断并设置main方法的定义类，找到运行的主类
       这里指 run方法里面显示定义的启动类（这里还传递了一个参数args） `DemoApplication.class`
