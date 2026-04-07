## Nacos - 微服务的大管家 （待补充）
* 一句话说明白:Nacos 就像是微服务架构里的“电话簿” + “广播站”。  
    注册中心（电话簿）：服务启动时，去 Nacos 登记“我上线了，我的 IP 是 xxx”。其他服务想调用它，就去 Nacos 查 IP
    配置中心（广播站）：把配置文件（如数据库连接、开关）放在 Nacos 上。服务启动时去拉取，Nacos 一广播（配置变更），所有服务立马更新，不用重启。

* 关键概念: 临时实例 vs 持久化实例：
      * 临时：像“外卖骑手”，心跳断了（网断了）就剔除，适合动态扩缩容。  
      * 持久：像“固定店铺”，心跳断了只是标记不健康，不会剔除。
* 关键概念Namespace（命名空间）: 用来做环境隔离（开发、测试、生产互不干扰）。
* Group（分组）：用来做项目隔离（比如订单组和用户组）


* 关于配置的生效优先级:
    命令行参数 > 系统环境变量 > Nacos 远程配置 > 本地 application.yml > 本地 bootstrap.yml。
    注意：Nacos 内部也有优先级，越具体的配置（带环境名的）优先级越高。

* Nacos也能实现热更新的效果，不过热更新的效果客户端通过长轮询机制，一直问服务端“配置变了吗？”。如果变了，服务端立马返回新配置，Spring 监听到后刷新 Bean（需加 @RefreshScope）。
* 青铜段位：@Value + @RefreshScope
    ```java
    @RestController
    @RefreshScope // 必须加！告诉 Spring 这个 Bean 是可以刷新的
    public class UserController {

        @Value("${user.timeout:5000}") // 注入配置，5000 是默认值
        private int timeout;

        @GetMapping("/test")
        public String test() {
            return "超时时间: " + timeout;
        }
    }
    ```
    原理：当 Nacos 配置变更，Spring 会销毁这个 Bean，下次调用时重新创建一个，从而注入新值。
    缺点：如果不加 @RefreshScope，值永远不会变。
* 黄金段位：@ConfigurationProperties（推荐）
    ```java
    @Component
    @ConfigurationProperties(prefix = "user") // 绑定前缀
    @Data
    public class UserConfigProperties {
        private int timeout;
        private String featureSwitch;
    }
    ```
    用法：在 Service 里直接注入 UserConfigProperties 对象。
    优势：
    自动刷新：通常不需要加 @RefreshScope（Spring Cloud Alibaba 内部做了处理），配置一变，对象里的值自动更新。
    类型安全：有代码提示，不会写错配置名。
    结构清晰：把散落的配置聚合成一个类。
* 钻石段位：编程式监听  如果你需要在配置变更时做复杂的逻辑（比如：配置变了，不仅要更新变量，还要手动关闭旧的数据库连接池，开启新的），那就用监听器。
    ```java
    @Component
    public class CustomListener {

        @Autowired
        private NacosConfigManager nacosConfigManager;

        @PostConstruct
        public void init() {
            // 监听特定的 Data ID
            nacosConfigManager.getConfigService().addListener(
                "user-service-dev.yaml", 
                "DEFAULT_GROUP", 
                new Listener() {
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 收到变更后的完整配置内容（通常是 YAML 字符串）
                        System.out.println("配置变更了！新内容: " + configInfo);
                        // 在这里写你的自定义逻辑，比如刷新缓存、重建连接池
                    }
                }
            );
        }
    }
    ```

## NACOS 拉取配置问题快速自测
* Q1: 我在 Nacos 改了配置，为什么不生效？  
    检查点 1：代码里有没有加 @RefreshScope？  
    检查点 2：是不是用了 static 变量？静态变量属于类，不受 Spring 容器管理，无法热更新。  
    检查点 3：Data ID、Group、Namespace 是否完全匹配？哪怕差一个字符都拉取不到。  
* Q2: 开发环境想用自己的配置，不想用 Nacos 的怎么办？  
    技巧：利用优先级。在本地 application.yml 里写配置，如果 Nacos 优先级太高覆盖了怎么办？  
    解决：在启动命令里加 -Dspring.cloud.nacos.config.enabled=false。这样启动时直接禁用 Nacos 配置，完全回退到本地，非常适合断网开发。  
* Q3: 为什么要分 bootstrap 和 application？  
    形象比喻：
    bootstrap.yml 是“钥匙”：它先加载，用来打开 Nacos 的门。  
    application.yml 是“房子”：门打开了，才能把房子（业务配置）搬进来。  
    如果把 Nacos 地址写在 application.yml 里，Spring Boot 启动时还没读到这个文件，就不知道去哪里拉取配置，导致死循环。  

