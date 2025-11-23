## yaml 语法和配置起作用的笔记

```yml
    #语法结构 key: 空格 value 如下
server:
  port: 8081
#yaml 还可以存储对象，application.properties不行
#
#另 yaml的优先级更高
#可以注入到配置（可以直接给实体类赋值）
Student:
  name: 阿贝多
  age: 3
#行内写法
Student2: {name: sd , age: 4}


#数组
pets:
  -cat 
  -void
  -dog
pets2: [cat,dog,panda]
#yaml 对空格和缩进敏感，类似python

Dog:
  name: 小小狗
  age: 8

person:
  name: hang
  age: ${random.int(0,100)}
  happy: false
  birth: 2001/10/17
  maps: {k1: se,k2: pom}
  lists:
    - code
    - music
  dog:
    name: aha
    age: 96
#可以动$符号来实现一些随机的内容，甚至能够生成UUID
```

* yaml配置自动注解导入实体类
    ```java
        package com.example.springbootproject.pojo;

    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.springframework.boot.context.properties.ConfigurationProperties;
    import org.springframework.stereotype.Component;

    import java.util.Date;
    import java.util.List;
    import java.util.Map;

    @NoArgsConstructor//lombok无参构造器
    @Data//lombok属性自动get、Setting方法
    @AllArgsConstructor//lombok全参构造器
    @Component//注册位Componet组件，这样才能能够在初始化的时候被扫描到然后才能导入bean
    @ConfigurationProperties(prefix = "person") //指定配置文件里面导入的对象的前缀
        public class Person {
        private String name;
        private Integer age;
        private Boolean happy;
        private Date birth;
        private Map<String,Object> maps;
        private Dog dog;
        private List<Object> lists;
    }

    ```

* 测试类调用说明 
```java
    import com.example.springbootproject.pojo.Person;
    import lombok.Data;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;

    @SpringBootTest
    class SpringBootProjectApplicationTests {
        @Autowired//@Autowired 是 Spring 框架中最核心的依赖注入（Dependency Injection, DI）注解之一，它的主要作用是：自动将 Spring 容器中已管理的 Bean 注入到需要它的类中，从而实现对象之间的解耦和自动装配。
        private Dog dog;
        @Autowired
        private Person person;
        @Test
        void contextLoads() {
    //        System.out.println("Dog"+dog);
            System.out.println("person"+person);
        }

    }

```

* 正常项目中自动注入实现一般不会像测试类这样使用  @Autowired 注解，这里补充下其他情况

    * 方式 A：使用 @EnableConfigurationProperties（推荐）
    ```java
        // Person.java（去掉 @Component）
    import lombok.*;
    import org.springframework.boot.context.properties.ConfigurationProperties;
    import java.time.LocalDate;
    import java.util.List;
    import java.util.Map;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ConfigurationProperties(prefix = "person")
    public class Person {
        private String name;
        private Integer age;
        private Boolean happy;
        private LocalDate birth;
        private Map<String, Object> maps;
        private Dog dog;
        private List<Object> lists;
    }
    ```
    在主启动类上启用：
    ```java
        // SpringBootProjectApplication.java
        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;
        import org.springframework.boot.context.properties.EnableConfigurationProperties;

        @SpringBootApplication
        @EnableConfigurationProperties(Person.class) // 👈 关键：告诉 Spring 要管理 Person 这个 ConfigurationProperties 类
        public class SpringBootProjectApplication {
            public static void main(String[] args) {
                SpringApplication.run(SpringBootProjectApplication.class, args);
            }
        }
    ```

    * 方式 B：保留 @Component（也可行，但稍显冗余）
    ```java
            @Component // 让 Spring 扫描到它
            @ConfigurationProperties(prefix = "person")
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public class Person {
                // ...
            }
    ```
    注意：这种方式要求 Person 类在 @SpringBootApplication 所在包或其子包下（否则需额外配置 @ComponentScan）。
    ✅ 步骤 2：在其他组件中“使用”这个自动配置好的实例
    你不能自己 new，而要通过 Spring 的依赖注入获取：

    示例 1：在 Controller 中使用
    ```java 
        @RestController
        public class PersonController {

            // Spring 会自动注入那个已经绑定了配置的 Person 实例
            private final Person person;

            public PersonController(Person person) {
                this.person = person;
            }

            @GetMapping("/person")
            public Person getPerson() {
                return person; // 返回的内容就是 application.yml 中配置的值
            }
        }
    ```

    示例 2：在 Service 中使用
```java
    @Service
    public class SomeService {

        @Autowired
        private Person person; // 字段注入（不推荐，仅作演示）

        public void doSomething() {
            System.out.println("Name: " + person.getName());
            System.out.println("Dog: " + person.getDog().getName());
        }
    }
```
重点：无论你在哪里用 Person，只要它是通过 @Autowired、构造器注入、方法参数等方式从 Spring 获取的，就一定是已经加载了配置文件内容的实例。