## smybatis 技术实现笔记

### 简单使用技术介绍
 
 * smybatis技术实现了通过简单的注解配置方式，实现了entity实体类和数据库指定表做一一映射的功能，它简化了普通增删改查的过程复杂
 * 通过实现mapper接口类并且通过注解的方式注入实体类相关信息来使其能够调用增删改等等常用方法

### 代码实现

 * 实体类定义
 ```java
 package com.example.springbootproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private String username;
    private String userPwd;
}

 ```
 代码说明，指令有关键注解TableName 这里对应的是真实的数据库表的名字，以下属性也是对应了真实的数据库字段的名字，其中 uid是自增主键。

 * 实现mapper接口
 ```java
    package com.example.springbootproject.mapper;

    import com.baomidou.mybatisplus.core.mapper.BaseMapper;
    import com.example.springbootproject.entity.SysUser;
    import org.apache.ibatis.annotations.Mapper;
    import org.mybatis.spring.annotation.MapperScan;

    @Mapper//该注解能够让其被正确扫描
    public interface SysUserMapper extends BaseMapper<SysUser> {
    }
 ```
关键代码 BaseMapper<SysUser>,这里是注入了SysUser这个实体类

 * 实现服务类代码

```java
    package com.example.springbootproject.service;

import com.example.springbootproject.entity.SysUser;
import com.example.springbootproject.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserService {
    @Autowired
    private SysUserMapper userMapper;

    /**
     * 查询服务
     * @return
     */
    public List<SysUser> ListAll(){
        return userMapper.selectList(null);
    }

    /**
     * 更新和保存服务
     * @param user
     */
    public void save(SysUser user){
        if(user.getUid() == null){
            userMapper.insert(user);
        }else{
            userMapper.updateById(user);
        }
    }

    public SysUser getById(Long id){
        return userMapper.selectById(id);
    }

    public void deleteById(Long id){
        userMapper.deleteById(id);
    }
}

```
注意这里的 userMapper对象就是我们前面mapper接口的实例化对象，它封装了操作数据库的基本操作，会返回一个 SysUser对象。

* 通过控制类调用服务类服务示范代码:
```java
    package com.example.springbootproject.controller;

import com.example.springbootproject.entity.SysUser;
import com.example.springbootproject.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // ← 允许这个前端地址调用服务，防止跨域问题
public class GetDatetoFormApi {
    @Autowired
    private SysUserService userService;

    @GetMapping("/get")
    public List<SysUser> list() {
        return userService.ListAll();
    }

    @PostMapping("/save")
    public SysUser save(@RequestBody SysUser user) {
        userService.save(user);
        return user;
    }

    @PutMapping("/update/{id}")
    public SysUser update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setUid(id);
        userService.save(user);
        return user;
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteById(id);
    }
}

```


### 配置文件配置相关

* POM.xml文件配置mybatis启动项目和数据库相关依赖
```xml
    <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>3.5.13</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>
```
ps : springBoot3 版本用的mybatis依赖和springBoot2版本用的依赖不一样注意分辨

* application.yaml文件配置数据库的路径和地址
```yaml
pring:
  datasource:
    url: jdbc:mysql://localhost:3306/schedule_system?useSSL=false&serverTimezone=UTC
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 自动驼峰转下划线
```
mysql-connector-java该依赖会自动抓取配置文件里面的数据库信息并且注入到bean对象中，注意不支持多源数据库的配置，如果需要实现这样的功能就需要使用其他的依赖

