# Spring 事务（@Transactional 深度剖析）

> 面试必考、日常必踩的点。这篇从"怎么用"到"为什么失效"到"AOP原理"一次讲透，最后给出面试答题公式。

---

## 一、一句话本质

> **Spring 事务 = AOP 代理 + 数据库事务的统一封装**。它用"代理对象"在调用方法**前后**自动帮你 `begin / commit / rollback`，让你不用手写 `Connection` 的事务代码。而"事务是否真的生效"完全取决于**你调用的是不是被代理的对象**。

**两个关键前提（先记住，后面展开）：**

- 前提1：底层用的是**数据库事务**（InnoDB），Spring 只是"调度员"，数据库才是"执行者"。
- 前提2：**只有走代理的方法 @Transactional 才生效**——自己调自己（this.method()）不走代理 → 失效。

---

## 二、传播行为（PROPAGATION）— 高频面试

> 当"一个事务方法调用另一个事务方法"时，怎么决定嵌套关系。默认 **REQUIRED**。

| 传播行为                   | 含义                                                                      | 场景                             |
| -------------------------- | ------------------------------------------------------------------------- | -------------------------------- |
| **REQUIRED（默认）** | 外层有事务就用外层，没有就新建                                            | 绝大多数业务                     |
| **REQUIRES_NEW**     | 无论如何都**新开一个独立事务**，与外部互不影响                      | 记账、外呼日志——必须独立成不混 |
| **NESTED**           | 外层有就用外层，但**内层是个保存点(savepoint)**，内层失败只回滚自己 | 批量导入，一条失败不影响整体     |
| SUPPORTS                   | 有就用，没有就非事务                                                      | 只读辅助                         |
| NOT_SUPPORTED              | 非事务执行                                                                | 同步调远程/发消息（阻塞耗时）    |
| MANDATORY                  | 必须有外层事务，否则异常                                                  | 强制事务内                       |
| NEVER                      | 必须无事务，否则异常                                                      | 绝对不能在事务里做的             |

**面试必问的区分（REQUIRES_NEW vs NESTED）：**

```
外层A → 调内层B
REQUIRES_NEW：B 独立事务。B失败 → B回滚，A可继续（或看整体）；B成功但A后面失败 → B不回滚！
NESTED：B是A的保存点。B失败 → 只回滚B；A后面失败 → 连B一起回滚（B是A的一部分）

一句话总结：
  REQUIRES_NEW = 完全独立（同生不共死，A死B活）
  NESTED      = 部分回滚（同生共死，但B可单独回滚）
```

**你572/若依项目里最常见的误用：** 事务内做**远程调用**（推MES/ERP）。远程调用要等数据库事务提交后再发才安全，否则对方回调查库查不到，或事务回滚了消息却发出去（**分布式事务脏数据**）。
正确姿势：事务内只发 `transaction after commit`（事务提交后回调）或走 MQ 异步。

---

## 三、隔离级别（ISOLATION）

> 和数据库一致，解决并发读的问题。默认 **ISOLATION_DEFAULT**（跟随数据库，MySQL 默认 RR）。

| 隔离级别                                 | 脏读   | 不可重复读 | 幻读                                 |
| ---------------------------------------- | ------ | ---------- | ------------------------------------ |
| READ UNCOMMITTED                         | ❌会   | 会         | 会                                   |
| READ COMMITTED (RC)                      | ✅不会 | 会         | 会                                   |
| **REPEATABLE READ (RR)** MySQL默认 | ✅不会 | ✅不会     | ⚠️(MySQL的InnoDB用MVCC+间隙锁解决) |
| SERIALIZABLE                             | ✅不会 | ✅不会     | ✅不会                               |

**背口诀：** RC解决脏读，RR解决不可重复读，Serializable解决一切但性能最差。

---

## 四、@Transactional 到底做了什么（AOP 原理）⭐核心

```java
@Service
public class OrderService {
    @Transactional
    public void createOrder(Order order) {
        // 业务代码
    }
}
```

**Spring 实际做的事：**

1. 容器里真实的对象是 `OrderService`（原始业务类）
2. Spring 用 AOP 给它**包了一层代理**（`$Proxy` / CGLIB 增强对象），代理持有原始 target
3. 代理在 `createOrder()` **方法执行前** 自动调 `事务管理器.begin()`；**正常返回** commit；**抛异常** rollback

```
调用方 → 代理对象(代理拦截) → 开启事务 → 调用真实方法 → 成功commit/异常rollback → 返回调用方
```

**关键点：事务的开启由"代理"决定，真实对象本身不开启事务。**

---

## 五、⚠️ 事务失效的 8 大场景（面试重灾区 + 日常排雷）

> 背着 8 条，面试"为什么我的事务没生效"直接锁死。

**① 自调用（this.method()）——最经典 ❌**

```java
public void outer() {
    this.inner();   // ❌ this 是真实对象，不是代理，@Transactional 失效！
    // 正确：注入自己 / 用代理调用 inner
}
@Transactional
public void inner() { ... }
```

**原因**：`this` 指向原始对象，没走代理，AOP 拦截不到，事务不开启。
**解决**：把 `inner` 拆到另一个 Bean，或 `@Autowired self; self.inner()`，或 `TransactionTemplate`。

**② 方法不是 public ❌**

```java
@Transactional
private void method() { ... }   // ❌ private 方法代理拦截不到
```

原因：AOP（CGLIB/JDK代理）只能增强 public 方法。private/protected 不生效。

**③ 异常被 catch 吞掉 ❌**

```java
@Transactional
public void method() {
    try {
        // 业务
    } catch (Exception e) {
        log.error(e);   // ❌ 异常被吞，没有抛出，事务以为成功 → commit 了
    }
}
```

原因：Spring 事务**只对被抛出的异常**回滚，你 catch 住不抛 = 事务正常提交。

**④ 抛的是受检异常（默认不回滚）❌**

```java
@Transactional
public void method() throws Exception {
    throw new IOException();   // ❌ 受检异常默认不回滚！
}
```

规则：**默认只对 RuntimeException（运行时异常）和 Error 回滚**，受检异常要 `rollbackFor = Exception.class` 指定。

```java
@Transactional(rollbackFor = Exception.class)
```

**⑤ 类没被 Spring 管理 ❌**：没加 `@Service/@Component`，根本不是 Bean，没有代理。

**⑥ 数据库引擎不支持事务 ❌**：`MyISAM` 表引擎不支持事务（要用 InnoDB）。你项目里如果表是 MyISAM，@Transactional 无效（若依默认 InnoDB，一般没事）。

**⑦ 多线程/不同事务管理器 ❌**：`new Thread` 里调用，或不同 DataSource 用了不同事务管理器。

**⑧ 配置错（@EnableTransactionManagement 缺失 / 事务管理器没配好）**：Spring Boot 默认配好，但手动配多数据源时容易踩。

**记忆口诀：**

> 私有方法、catch吞、this自调、受检异常、非Bean、引擎错、跨线程、管理器错——八大失效场景。

---

## 六、事务不回滚 vs 回滚的判定（补充 3 和 4）

```java
// 正确：显式指定回滚所有异常
@Transactional(rollbackFor = Exception.class)

// 正确：异常抛出给Spring（不要catch不抛）
// 正确：如需catch做点事再抛出去
@Transactional(rollbackFor = Exception.class)
public void method() {
    try {
        doSomething();
    } catch (Exception e) {
        log.error("出错", e);
        throw e;   // 重新抛出，让 Spring 回滚
    }
}
```

**注意回滚粒度**：Spring 事务失败会**回滚整个事务**，不是只回滚一行。要"部分成功"，用传播行为 NESTED（保存点）或编程式事务（TransactionTemplate）。

---

## 七、编程式事务 TransactionTemplate（兜底/精细控制）

代理方式搞不定（自调用、动态条件回滚）时用编程式：

```java
@Autowired
private TransactionTemplate transactionTemplate;

public void method() {
    transactionTemplate.execute(status -> {
        doA();      // 业务
        if (条件不满足) {
            status.setRollbackOnly();  // 手动回滚
            return null;
        }
        doB();
        return null;
    });
}
```

**什么时候用**：自调用失效、需要代码里动态决定 commit/rollback、事务里有多分叉逻辑。

---

## 八、面试答题公式（泛化）

> 照搬你 RocketMQ 笔记的 `1原理 → 2取舍 → 3实战(572怎么做+为什么)`：

**Q：你项目里 @Transactional 遇到过失效吗？**

> 1（原理）：Spring 事务本质是 AOP 代理，在方法前后 begin/commit/rollback，只对 public 且走代理的方法生效。
> 2（取舍）：别在事务里做远程调用/发消息（会分布式脏数据），要回滚的就让异常抛给 Spring，别 catch 吞掉。
> 3（实战·572）：572 的 JPO 里我一般不用 @Transactional 管跨对象事务（3DE 的 DomainObject 有自己的事务/apiContext），而是事务边界在 DB/中间件层控制；若依的 Service 推荐用 REQUIRED 默认 + rollbackFor=Exception，远程推送走 MQ 异步避免事务内发消息。

---

## 九、自测灵魂拷问

Q1: this.inner() 为什么会让 @Transactional 失效？
A: this 是原始对象不是代理，AOP 拦截不到，事务不开启。要走注入的代理 Bean 调用。

Q2: 受检异常为什么不回滚？怎么让它回滚？
A: 默认只对 RuntimeException/Error 回滚。受检异常要加 rollbackFor = Exception.class。

Q3: REQUIRES_NEW 和 NESTED 的区别？
A: REQUIRES_NEW 完全独立事务（A死B活）；NESTED 保存点（A失败B一起回滚，但B可单独回滚）。

Q4: @Transactional 作用在私有方法为什么不行？
A: AOP（JDK代理/CGLIB增强）只能增强 public 方法，private 无法拦截。

Q5: 事务里能不能调远程接口？
A: 尽量不。远程调用要等事务 commit 后再发，否则事务回滚了消息已发出=脏数据；或走事务提交后回调/MQ。

🔗 关联知识
[[01Spring Boot 启动流程]]（AOP/IOC 容器基础）
[[02自动装配原理]]（场景配置、代理生成）
[[09_数据库深度剖析]]（事务真正在 InnoDB 上的隔离级别/锁/MVCC 底层）

---

# 十、补充精讲（2026-09-21 学习追加）

> 本节补齐上面没讲到或太略的地方：**① 事务失效的数据库根因 ② MyBatis 如何接入 Spring 事务（机制展开）③ 多数据源事务 ④ 572 手动事务对比**。

## 10.1 事务失效的数据库根因（补充第五节⑥）

**核心认知：MySQL 的事务能力由【存储引擎】实现，不是 MySQL 本身。**

> 不严谨的说法是"MySQL 支持事务"；准确说是"**InnoDB 引擎支持事务**"。MySQL Server 层只负责解析 SQL、优化、管 binlog，ACID 由引擎实现。

| 引擎 | 事务 | 锁 | 适用 |
|---|---|---|---|
| **InnoDB** | ✅ 支持 ACID | 行锁 + MVCC | **生产默认** |
| **MyISAM** | ❌ 不支持 | 表锁 | 只读/日志/老系统 |
| Memory / Archive | ❌ 不支持 | 表锁/行锁 | 临时表/归档 |
| NDB | ✅ 支持 | 行锁 | 分布式集群 |

- MySQL 5.5 后默认引擎就是 InnoDB，所以新建表不指定引擎 = 能事务。**坑在老项目迁移过来的建表语句里可能写着 `ENGINE=MyISAM`。**
- 检查：`SHOW TABLE STATUS LIKE 'xxx'` 看 Engine 列；修复：`ALTER TABLE xxx ENGINE=InnoDB;`（会锁表重建，大表评估窗口）

**隔离级别差异（面试高频）：**

| 数据库 | 默认隔离级别 | 备注 |
|---|---|---|
| MySQL (InnoDB) | **REPEATABLE READ** | 因早期 binlog=STATEMENT 的历史包袱，RR+间隙锁保证主从一致 |
| Oracle | **READ COMMITTED** | |
| PostgreSQL | READ COMMITTED | DDL 也支持事务 |

> 现在 `binlog_format=ROW` 后 RC 也安全，**互联网公司普遍主动改成 RC**（减少间隙锁、提升并发）。

**⚠️ DDL 隐式提交（MySQL 特有坑）：**

```java
@Transactional
public void doIt() {
    orderMapper.insert(o);
    jdbcTemplate.execute("ALTER TABLE orders ADD COLUMN x INT");  // 💥 隐式提交！
    throw new RuntimeException();   // 前面的 insert 也回滚不了，已被 DDL 提交
}
```
**规则：事务里别混 DDL。** （PG 支持 DDL 事务可回滚，MySQL 不支持——差异点）

## 10.2 ⭐ MyBatis 如何接入 Spring 事务（机制展开）

> 一句话：**事务的本质是一个 Connection。Spring 开事务时把这个 Connection 放进 ThreadLocal；MyBatis 执行 SQL 前先查 ThreadLocal，有就复用、没有就自己开。所以用注入的 Mapper 时，SQL 天然落在 Spring 事务里。**

**为什么需要 ThreadLocal？** 不传参又要让同线程多个方法共用同一个 Connection：
```
方法参数透传 Connection → 污染业务签名 ❌
全局变量存 Connection   → 线程不安全 ❌
ThreadLocal            → 按线程隔离，省参又安全 ✅
```

**Spring 侧（`DataSourceTransactionManager`）：**
```
① @Transactional 被代理拦截
② tm.getTransaction() 向 DataSource 要 Connection → setAutoCommit(false)
   → ★ 包装成 ConnectionHolder，放进 TransactionSynchronizationManager 的 ThreadLocal
③ 调用真实业务方法（Service → Mapper）
④ 正常返回 commit / 抛异常 rollback → 清理 ThreadLocal
```

**MyBatis 侧（`SqlSessionUtils.getSqlSession`）：**
```java
// 简化逻辑
ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager
        .getResource(factory.getConfiguration().getEnvironment().getDataSource());
if (holder != null) {
    // 有！在 Spring 事务里 → 复用这个 Connection（SpringManagedTransaction）
    return new SqlSessionTemplate(factory);
}
// 没有 → 自己新开一个（自动提交模式）
return factory.openSession();
```

**完整时序：**
```
OrderService.createOrder()  [@Transactional]
├─ 代理 → DataSourceTransactionManager.getTransaction()
│     ├─ ds.getConnection() → conn1
│     ├─ conn1.setAutoCommit(false)
│     └─ ThreadLocal.put(ds, ConnectionHolder(conn1))   ★
├─ orderMapper.insert()  → SqlSessionUtils 从 ThreadLocal 拿到 conn1 ✅ 复用
├─ stockMapper.decrease() → 还是 conn1 ✅ 复用
└─ 代理 commit() → conn1.commit()   ← 两条 SQL 一起提交
```

**⚠️ 反面例子（常见"加了注解却没生效"）：**
```java
@Transactional
public void doIt() {
    SqlSession session = factory.openSession();  // ❌ 新开 Connection，绕过 SqlSessionTemplate
    session.insert(...); session.commit();       // 用的是 conn2，脱离 Spring 事务
    throw new RuntimeException();                // conn1 回滚，conn2 已提交
}
```
**正确做法：永远用注入的 Mapper（背后是 SqlSessionTemplate），别自己 openSession()。**

**⚠️ 跨线程失效根因：** ThreadLocal 是线程隔离的 → 事务方法里 `new Thread` / 丢线程池 → 新线程拿不到 conn1 → 独立事务。

**进阶：连接延迟获取** —— 事务方法**不执行任何 SQL 就不会真正建立连接**（`getTransaction()` 只登记 ConnectionHolder，真正 `getConnection()` 在第一次执行 SQL 时）。目的是省连接。

## 10.3 数据库数据源配置与多数据源事务

**标准写法（单数据源）：**
```java
@Transactional(rollbackFor = Exception.class)   // ★ 必加
public void createOrder(Order order) {
    orderMapper.insert(order);        // 自动在事务里
    stockMapper.decrease(...);
}
```
> Spring Boot 检测到 DataSource + MyBatis starter → 自动注入 `DataSourceTransactionManager`，无需手写。

**多数据源：必须显式指定 transactionManager**
```java
@Bean("orderTxManager")
public DataSourceTransactionManager orderTxManager(@Qualifier("orderDataSource") DataSource ds) {
    return new DataSourceTransactionManager(ds);
}

@Transactional(transactionManager = "orderTxManager", rollbackFor = Exception.class)
public void createOrder(...) { ... }
```
> 容器里有多个管理器 → 不指定报 `NoUniqueBeanDefinitionException` 或猜错。

**🎯 关键认知：一个 @Transactional 只能管一个数据源。** 一个方法要写两个库，声明式事务做不到（要用 XA / Seata），或拆成"本地消息表 + 最终一致性"。

**若依的坑：动态数据源(`@DataSource` 切面) + `@Transactional` 的顺序问题**
> 事务开启时 `DataSourceTransactionManager` 就要向 DataSource 要 Connection —— **这一刻就决定了用哪个库**。如果数据源切面在事务切面之后执行，Connection 已按默认库拿到，再切就晚了 → **SQL 打到错误的库**。所以若依给数据源切面加了 `@Order` 保证它先执行。

## 10.4 572 实战对比：为什么用手动事务

> **§八 面试公式里写的"572 不用 @Transactional"，这里给出根因和证据。**

**572 的两类"事务"：**
1. **3DE 侧**：走 **3DE 矩阵数据库**，JPO 用 `ContextUtil`（3DE 自己的事务 API），**不是 JDBC，谈不上 Spring 事务**。
2. **Oracle 中间表**（MES/ERP 对接）：`ConnectOracle` 用 **JDBC 手动管事务**（`setAutoCommit(false)` + `commit/rollback`）。

**根因**：`domainObjectPolicyChange` 这类类是 **JAX-RS 的 `extends RestService`，不是 Spring Bean** —— `@Transactional` 依赖 AOP 代理，非 Spring 托管的类上注解**根本不生效**，所以只能用 `ContextUtil.startTransaction/commitTransaction/abortTransaction` 手动事务。

**面试话术**：
> "572 的 `domainObjectPolicyChange`（若依提升接口）用的是 `ContextUtil` 手动事务，不是 `@Transactional`。那是个 JAX-RS 的 RestService 类，不是 Spring 管理的 Bean，而 `@Transactional` 依赖 AOP 代理，非 Spring 托管的类上注解不生效。这反过来证明我真懂'声明式事务依赖 AOP 代理'这个原理，不是背的。"

## 10.5 自调用失效的 5 种解法（补第五节①）

1. 把方法拆到**另一个 Bean** 里调
2. **注入自己**：`@Autowired private OrderService self;` → `self.b()`
3. **`AopContext.currentProxy()`**（需 `@EnableAspectJAutoProxy(exposeProxy = true)`）
4. **`ApplicationContext.getBean(OrderService.class).b()`**
5. 编程式事务 `TransactionTemplate`（最干净）

> 面试最爱听 ①②③。

## 10.6 事务日志自查技巧

```yaml
logging:
  level:
    org.springframework.jdbc.datasource.DataSourceTransactionManager: DEBUG   # 打印 begin/commit/rollback
    org.mybatis: DEBUG
```
> 日志里**没出现 `Creating new transaction`** → 事务没走代理（八成自调用/非 Bean）。**这是最快的自查手段。**
