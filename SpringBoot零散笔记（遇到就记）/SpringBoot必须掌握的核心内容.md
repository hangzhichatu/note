## 必须掌握的核心内容层

* 核心骨架:目标必须能够脱口而出，必须熟练，必须了解原理
    * 项目结构   `@SpringBootApplication`、自动配置原理(`@EnableAutoConfiguration`)、Starter机制   掌握标准：解释为什么加一个stater就能使用redis
    * Bean管理  `@Component`/`@Service`/`@Repository`、`@Autowired`、`@Configuration` + `@Bean`  掌握标准:能自己手写一个自定义的Bean并注入
    * web开发   `@RestController`、`@RequestMapping`、参数绑定（`@PathVariable`, `@RequestBody`）、统一异常处理（`@ControllerAdvice`） 掌握标准:能独立开发一个CRUD接口
    * 配置管理 `application.yml`、`@ConfigurationProperties`、`Profile` 多环境配置  掌握标准:能配置dev/test/prod  三套数据库
    * 日志与测试  SLF4J 日志使用、`@SpringBootTest` 单元测试  能写集成测试验证接口

* 常用拓展:这部分提供许多常见的业务场景的解决问题的方案不需要知道细节，但是需要知道去哪里找官方文档和典型场景
    * 数据访问  MyBatis-Plus 的 QueryWrapper、JPA 的 @Entity
    * 异步任务  @Async、@Scheduled
    * 缓存      @Cacheable、Redis 集成
    * 安全     Spring Security 基础配置
    * 监控     Actuator 端点（/actuator/health） （这个确实好用，能查系统执行时候资源使用情况）