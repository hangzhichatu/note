09_数据库深度剖析（从SQL到底层）.md
标签: #数据库 #MySQL #InnoDB #SQL优化 #索引 #事务
最后复习: 2026-06-16
掌握程度: ⭐⭐⭐ (刚学第一轮，需要大量练习)

---

## 1. 🎯 一句话本质

数据库优化的核心就一件事：**减少回表次数，让排序和数据都在索引里搞定 (覆盖索引)，实在不行再走 filesort 但尽量在内存里排完，别让 MySQL 写磁盘临时文件。**

---

## 2. 💥 灾难现场 (The Disaster Scenario)

### 🅰️ DELETE 没索引导致全表锁

```sql
DELETE FROM orders WHERE status = 'CANCELLED';
-- 没有 status 索引！
```

**底层发生了什么：**
- 全表扫描，InnoDB 逐行判断 status
- RR 级别下每一行都加 Next-Key Lock
- 几百万行全部被锁住
- 其他事务的 INSERT/UPDATE/DELETE 全部阻塞

**后果：** 生产事故，业务直接堵死。

**修复：** 先确认有索引，再分批删除：
```sql
DELETE FROM orders WHERE status = 'CANCELLED' LIMIT 1000;
-- 循环执行，每次 COMMIT，避免大事务
```

---

### 🅱️ `INSERT ... ON DUPLICATE KEY UPDATE` 死锁

```sql
-- 事务 A 和事务 B 同时执行
INSERT INTO t (k, v) VALUES ('a', 2)
ON DUPLICATE KEY UPDATE v = 2;
```

**底层发生了什么：**
1. A 检测到冲突 → 在 uk_k 行上 S 锁 ✅
2. B 检测到冲突 → 在 uk_k 行上 S 锁 ✅（S 锁不互斥）
3. A 要执行 UPDATE → 需要升级 X 锁 → 等 B 释放 S 锁 ❌
4. B 要执行 UPDATE → 需要升级 X 锁 → 等 A 释放 S 锁 ❌
5. InnoDB 检测到死锁，选一个回滚

**后果：** 并发高时频繁死锁。

**修复：** 拆成 SELECT + 业务判断 + INSERT/UPDATE，或用分布式锁。

---

### 🅲 ORDER BY 列不在索引中导致大文件排序

```sql
-- 有索引 (status, created_at)
SELECT * FROM orders
WHERE status = 'PAID'
  AND created_at >= '2023-06-01'
ORDER BY amount DESC
LIMIT 20;
```

**底层发生了什么：**
1. 走 (status, created_at) 索引找到 3 万行
2. **每行回表**拿 amount（3 万次回表！）
3. sort_buffer 不够 → 写磁盘归并排序

**后果：** 慢查询，磁盘 IO 飙高。

**修复：**
```sql
-- 方案1：把 amount 加入索引（覆盖索引）
ALTER TABLE orders ADD INDEX idx_all(status, created_at, amount);

-- 方案2：延迟关联
SELECT o.*
FROM (
    SELECT id FROM orders
    WHERE status = 'PAID' AND created_at >= '2023-06-01'
    ORDER BY amount DESC LIMIT 20
) tmp
JOIN orders o ON tmp.id = o.id
ORDER BY o.amount DESC;
```

---

## 3. 🧠 费曼复述 (The Feynman Test)

### 👶 给实习生的比喻

**MVCC 是什么？**
> 你在图书馆写读书报告（事务 A），馆员给你一张"现在在馆内的人的名单"（ReadView），名单上有另一个同学（事务 B）的名字。
> B 还书的时候在书后写了个纸条改了内容（UPDATE + COMMIT），但你的名单上还有他名字，所以你**假装没看到他的纸条**，继续读旧内容。
> 这就叫 RR（可重复读）——你开始读书时的那张名单就是你的 ReadView，整场不变。
> 对应项目代码：`DomainObject.findObjects` 在不同事务中返回不同结果的问题。

**索引的 B+Tree 是什么？**
> 想象一本字典的索引页（非叶子节点），上面只写着"单词首字母 → 在几页"。
> 翻到对应页后（叶子节点），才看到完整的单词解释。
> 二级索引就像字典后面的"拼音索引"——先查到字，再翻到正文页码（回表）。

**为什么覆盖索引快？**
> 假如你在拼音索引里不光能看到页码，还能直接看到"这个词什么意思"，那你就不用翻正文了。
> 这就是 `Using index`。

**filesort 在干什么？**
> 你把朋友（行数据）按身高（ORDER BY amount）排队，但朋友要一个个从房间里叫出来（回表拿 amount），然后你拿小本子记名字和身高，再排序。
> 如果小本子写满了（sort_buffer 不够），就需要在地上多铺几张纸（磁盘临时文件），再把纸拼起来排序——超级慢。

---

### ⚖️ 核心对比

| 特性 | 聚簇索引 (PRIMARY) | 二级索引 (普通INDEX) |
|------|-------------------|-------------------|
| 叶子节点存什么 | **整行数据** | 主键值（和索引列） |
| 查询方式 | 一次 B+Tree | 两次 B+Tree（先二级→再回聚簇） |
| 回表 | 无 | 需要回表 |
| 覆盖索引 | — | 如果 SELECT 列全在二级索引里 | ...则不需要回表 |

| 隔离级别 | 脏读 | 不可重复读 | 幻读 | MVCC ReadView 生成时机 |
|----------|------|-----------|------|----------------------|
| RC | ❌ | ✅ | ✅ | **每条语句**重新生成 |
| RR (InnoDB) | ❌ | ❌ | ❌ | **事务开始时**生成，复用 |
| 幻读防谁防 | — | — | 快照读靠 ReadView；当前读靠 Next-Key Lock | |

---

## 4. ⚡ 性能与边界 (Performance & Boundaries)

### ✅ 适用场景 (Safe Zones)

- **等值查询走唯一索引** → type=const，最快的查询
- **覆盖索引查询** → Extra: Using index，不回表
- **联合索引等值匹配全部列** → 精确定位，不回表或者极少回表
- **延迟关联** → 子查询先在索引里排好序，再回表取 100 行（而不是 3 万行）
- **INSERT 正常执行（无冲突）** → 插入意向锁，不阻塞
- **ORDER BY 和索引顺序一致** → 不走 filesort

### 🚫 绝对禁区 (Danger Zones)

- **DELETE/UPDATE 全表扫（WHERE 条件没索引）** → 行级锁全部锁住 → 生产事故
- **`ON DUPLICATE KEY UPDATE` 高并发冲突同一行** → S 锁升级 X 锁死锁
- **大表 COUNT(*)** → InnoDB 要逐行数，MyISAM 才快
- **函数包裹索引列** → `WHERE DATE(create_time) = '2024-01-01'` → 不走索引
- **隐式类型转换** → `WHERE card_no = 123456`（card_no 是 VARCHAR） → 不走索引
- **大表分页深偏移** → `LIMIT 10 OFFSET 1000000` → 扫描 100 万行再丢弃
- **被驱动表 JOIN 无索引** → type=ALL + Using join buffer → 极慢

### 📉 性能杀手

| 现象 | Extra 列标记 | 根因 |
|------|------------|------|
| 大表排序 | `Using filesort` | sort_buffer 不够时写磁盘归并 |
| 大量回表 | `Using where` (无 Using index) | 二级索引查到主键后，每行随机读聚簇索引 |
| 临时表写磁盘 | `Using temporary` | GROUP BY 组数太多超过 tmp_table_size |
| 优化器选错索引 | `type=ALL` | 统计信息不准确或区分度判断失误 |
| S→X 锁升级死锁 | — | ON DUPLICATE KEY UPDATE 冲突时两个事务各持 S 锁，都想升级 X |

---

## 5. ❓ 自测灵魂拷问 (Self-Quiz)

(复习时遮住答案，口头回答)

**Q: MySQL InnoDB 的 RR 级别为什么能防幻读？**
A: (关键点：快照读靠 MVCC ReadView 事务期间不变 → 新行不可见；当前读 (SELECT ... FOR UPDATE) 靠 Next-Key Lock → 间隙锁阻止插入)

**Q: MVCC 的 ReadView 在 RC 和 RR 下有什么区别？**
A: (关键点：RC 每条语句重新生成 → 读到最新提交；RR 事务开始时生成且不变 → 整个事务看到一致快照)

**Q: 什么是覆盖索引？Extra 列怎么看？**
A: (关键点：SELECT 的列全在索引里 → 不用回表 → Extra 显示 Using index)

**Q: 范围查询后为什么右边的列走不了索引？**
A: (关键点：联合索引 (a, b)，WHERE a > 10 AND b = 5。a 范围后 b 不是全局有序的，不能再索引过滤 → 只有 a 用到索引，b 需要回表后 Server 层过滤)

**Q: `COUNT(*)` 和 `COUNT(1)` 和 `COUNT(col)` 性能一样吗？**
A: (关键点：`COUNT(*)` = `COUNT(1)` = `COUNT(NOT NULL col)`，InnoDB 选最小的索引遍历；`COUNT(nullable col)` 稍慢，需判空)

**Q: 自增 ID 回滚后会复用吗？**
A: (关键点：不会。自增计数器在 INSERT 开始时就分配了，不参与事务。原因：保证并发，无需管理空洞)

**Q: JOIN 小表驱动大表是什么道理？**
A: (关键点：驱动表扫 N 行 → 被驱动表每行走索引查 M 次。总复杂度：N × log₂(被驱动表行数)，所以 N 越小越好)

**Q: `ON DUPLICATE KEY UPDATE` 死锁的根因是什么？**
A: (关键点：两个事务同时冲突同一唯一键 → 各持 S 锁不互斥 → 都想升级 X 锁 → X 和 S 互斥 → 互相等待)

**Q: 什么情况下子查询不会减少回表次数？**
A: (关键点：子查询中 ORDER BY 的列不在覆盖索引里 → 子查询内部也要回表拿排序列的值 → 回表次数没少)

**Q: 一条 UPDATE 的完整执行链路是什么？**
A: (关键点：行定位(Buffer Pool/磁盘) → Record Lock → undo log → 改 Buffer Pool 脏页 → 更新 DB_TRX_ID/DB_ROLL_PTR → redo log prepare → binlog → redo log commit)

---

## 6. 💡 今日练习总结：犯错记录

### 练习 1 的错误

题目：`SELECT * FROM t WHERE a = 5 AND b = 100;`，有索引 `idx_a(a)`、`idx_b(b)`、`idx_ab(a, b)`，a 只有 10 种值，b 有 10000 种。

**我的回答：** 优化器选 idx_b，因为 b 的区分度更高。

**正确答案：** 应该选 idx_ab。因为 `a=5 AND b=100` 是等值匹配，联合索引能精确 B+Tree 定位到单行（或几行），扫描行数极少。虽然 a 的"单独区分度"低，但加上 b 后联合区分度最高。

**教训：** 等值匹配场景下，联合索引的前缀列区分度低没关系，因为后面列能进一步过滤。区分度影响的是"范围匹配"场景，不是等值匹配场景。

---

### 练习 2 的错误

题目：延迟关联写法：

```sql
SELECT o.*
FROM (
    SELECT id FROM orders
    WHERE status = 'PAID'
      AND created_at >= '2023-06-01'
      AND created_at < '2023-07-01'
    ORDER BY amount DESC
    LIMIT 100
) tmp
JOIN orders o ON tmp.id = o.id
ORDER BY o.amount DESC;
```

问：有没有减少回表次数？

**我的回答：** 有（当时以为子查询和直接查有区别）

**正确答案：**
- **没有覆盖索引时：** 子查询内部要回表拿 amount 来排序 → 回表 3 万次，和直接查一样，还多了外层 100 次回表和两次 filesort
- **有覆盖索引 (status, created_at, amount) 时：** 子查询排序不用回表 → 0 次回表，外层 100 次回表

**教训：** 子查询不是银弹。核心是要覆盖索引让排序字段在索引里。没有覆盖索引的延迟关联反而更慢。

---

### 练习 3（自我补充）

题目：`SELECT * FROM t WHERE a = 5 ORDER BY c LIMIT 10;`

思考路径：
1. `ORDER BY c` 能走索引避免 filesort 吗？
   - 不能。c 没有任何索引，必须 filesort。
2. 优化器选哪个索引？
   - 如果走 idx_ab → `a=5` 定位到 10 万行，回表 10 万次 → filesort 10 万行
   - 如果走 idx_a → 同上
   - 如果全表扫 → 顺序读可能比 10 万次随机 IO 快
   - 实际可能全表扫。
3. 怎么改？
   - 建联合索引 `(a, c)`：a 等值、c 排序都在索引里，不需要 filesort，只需要回表 10 行取完整数据

---

## 🔗 关联知识

- [[03_Java_集合框架]] (B+Tree 和 HashMap/红黑树的对比)
- [[07重要的中间件]] (MySQL 集群、分库分表)
- `G:\工作笔记\git\note-repo\数据库开发相关\数据库优化相关.md` (更多慢查询优化案例)
- `Project: 572` `documentHandle docId null bug` (TODO: 参数传递错误待业务确认)
