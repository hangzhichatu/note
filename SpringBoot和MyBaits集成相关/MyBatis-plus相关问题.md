## 说明

* 问题 报错信息:

  ```plaintext
  Invalid bound statement (not found): com.example.cahblogbackend.mapper.PostMapper.insert
  ```

  这说明了MyBatis 或者 (或MyBatis-plus)没有正确加载 `PostMapper` 对应的映射关系,结合前面那句报错信息,意思就是没有找到 insert方法对应的映射关系.如果遇到了错误可以更具以下过程来排查错误.

  * 第一步确定是否根本没有XML文件  注释   **如果你没有写Mapper对应的xml文件的话,MyBatis-plus会自动实现常用的insert方法,但是如果写了的话就不会有默认的insert方法**
  * 第二步,检查对应的实体类 是否有注释 `@TableName("posts")`,这个注解的作用是告诉MyBatis-plus要对应那张表,(不过如果写了xml文件的话这个就是不是必须的了)
  * 第三步,检查Mapper类是否继承 `BaseMapper<PostEntity>`   extend BaseMapper
  * 第四步 ,  主启动类 是否有添加 MapperScan注解,并且该注解指定的扫描的包包括你写的Mapper类 类似这样的

    ```java
    @MapperScan("com.example.cahblogbackend.mapper")
    ```

    或者直在对应的Mapper类上增加注解 @Mapper (只不过每个Mapper类都必须增加@Mapper注解)
  * 第五步,检查是否正确添加依赖 `mybatis-plus-boot-starter`   **注意Mybatis-plus依赖不能同时mybatis-spring-boot-starter
    一起添加***
  * 
  * 第六步,检查 `application.yml`  文件   这里给下 Mabatis的参考

    ```yaml
    mybatis-plus:
      mapper-locations: classpath*:mapper/**/*.xml  # 如果你不用 XML，这行可删
      type-aliases-package: com.example.cahblogbackend.entity
      configuration:
        map-underscore-to-camel-case: true
    ```
  * 第七步 检查 `PostMapper` 接口写法

    ```java
    package com.example.cahblogbackend.mapper;

    import com.baomidou.mybatisplus.core.mapper.BaseMapper;
    import com.example.cahblogbackend.entity.PostEntity;
    import org.apache.ibatis.annotations.Mapper;

    // 方式1：用 @Mapper（不推荐，每个都要加）
    // @Mapper
    // public interface PostMapper extends BaseMapper<PostEntity> {}

    // 方式2：靠 @MapperScan（推荐）
    public interface PostMapper extends BaseMapper<PostEntity> {}
    ```
