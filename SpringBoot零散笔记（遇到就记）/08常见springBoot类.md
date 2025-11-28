## SpringBoot 类详解

### 控制器类Controller

* 职责 ：接收前端请求，调用Service层，返回响应
* 注解 ： `@RestController` 或者 `@Controller` (这两个注解下会写请求路径的注解)
* 示例:

  ```java
  @RestController
  @RequestMapping("/api/users")
  public class UserController {
      @Autowired
      private UserService userService;

      @GetMapping("/{id}")
      public User getUser(@PathVariable Long id) {
          return userService.findById(id);
      }
  }
  ```

### 业务逻辑类Service

* 职责：实现具体的业务规则（比如注册用户时候要发邮件写数据库记录日志）
* 注解: `@Service`
* 特点：可以被多个Controller复用
* 示例:

```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User register(User user) {
        // 校验邮箱唯一性
        // 发送欢迎邮件
        // 保存到数据库
        return userRepository.save(user);
    }
}
```

### 数据访问类 Repository / DAO

* 职责： 执行CRUD操作（增删改查）
* 技术栈 ： Spring Data JPA → `@Repository` + 继承 `JpaRepository   `MyBatis → Mapper 接口（通常也叫 Repository 或 DAO）
* 示例（JPA）:

  ```java
  @Repository
  public interface UserRepository extends JpaRepository<User, Long> {
      Optional<User> findByEmail(String email);
  }
  ```

  DAO（Data Access Object）是传统叫法，Spring Data 中更常用  **Repository** 。

### Entity/Model(实体类)

* 职责：映射数据库表字段（ORM）
* 注解：（JPA）：`@Entity`,` @Table` `@Id`,`@Column`
* 
* 
* 示例:

  ```java
  java@Entity
  @Table(name = "users")
  public class User {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
      private String email;
      private String name;
      // getter/setter
  }
  ```

### 数据传输对象 DTO （Data Transfer Object）

* 职责：避免Entity类直接暴露
* 特点：无业务逻辑，只有字段和getter，setter方法
* 常见类型：

  * UserCreateDTO（前端传入）
  * UserResponseDTO（返回给前端）
* 示例:

  ```java
  public class UserResponseDTO {
      private Long id;
      private String name;
      // 不包含 password 字段！
  }
  ```

### 配置类 Configuration

* 职责：定义Bean、配置拦截器、跨域、第三方客户端
* 注解: `@Configuration`
* 示例：

  ```java
  @Configuration
  public class WebConfig {
      @Bean
      public RestTemplate restTemplate() {
          return new RestTemplate();
      }
  }

  }
  ```

### 工具类 Util/Helper

* 职责：日期格式化、字符串处理、加密等
* 注解：通常无注解 用 `public class XxxUtils { private XxxUtils() {} }`防止实例化

### 过滤器/拦截器(Filter/Interptor)

* Filter: Servlet 层，处理所有请求（如日志、编码）
* Interceptor：Spring MVC，可以访问Controller信息（如权限校验）


### 分层图

```plaintext
前端请求
   ↓
[Controller] → 接收请求，参数校验
   ↓
[Service]    → 执行业务逻辑
   ↓
[Repository] → 操作数据库
   ↓
[Entity]     ↔ 数据库表
```
