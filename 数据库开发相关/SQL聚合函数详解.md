# SQL 聚合函数详解

## 什么是聚合函数？

将多行数据压缩成一个值返回的函数。常用于统计分析和报表场景。

---

## 五大标准聚合函数

> 假设有如下 `employees` 表作为示例：

```sql
+----+-------+--------+------------+
| id | name  | salary | department |
+----+-------+--------+------------+
| 1  | 张三  | 10000  | 技术部     |
| 2  | 李四  | 15000  | 技术部     |
| 3  | 王五  | 8000   | 市场部     |
| 4  | 赵六  | 12000  | 市场部     |
| 5  | 陈七  | NULL   | 技术部     |
+----+-------+--------+------------+
```

### 1. COUNT — 计数

```sql
-- 统计表总行数（含 NULL）
SELECT COUNT(*) FROM employees;        -- 5

-- 效果同 COUNT(*)
SELECT COUNT(1) FROM employees;        -- 5

-- 统计某列非 NULL 的行数
SELECT COUNT(salary) FROM employees;   -- 4（排除了陈七的 NULL）

-- 去重计数
SELECT COUNT(DISTINCT department) FROM employees;  -- 2
```

> **核心区别：`COUNT(*)` 计所有行，`COUNT(列名)` 只计非 NULL**

### 2. SUM — 求和

```sql
SELECT SUM(salary) FROM employees;     -- 45000（NULL 被忽略）
```

### 3. AVG — 平均值

```sql
SELECT AVG(salary) FROM employees;     -- 11250（45000 / 4, NULL 不计入分母）
```

> **AVG 自动忽略 NULL。** 若需将 NULL 当作 0 计算：
```sql
SELECT AVG(COALESCE(salary, 0)) FROM employees;  -- 9000（45000 / 5）
```

### 4. MAX / MIN — 最大值 / 最小值

```sql
SELECT MAX(salary), MIN(salary) FROM employees;  -- 15000, 8000
```

---

## GROUP BY — 分组聚合

不分组 = 全表汇总；分组 = 按组汇总。

```sql
-- 每个部门的平均工资
SELECT department, AVG(salary) AS avg_salary
FROM employees
GROUP BY department;

-- 结果：
-- 技术部 | 12500（(10000+15000)/2）
-- 市场部 | 10000（(8000+12000)/2）
```

**执行顺序：** `FROM → WHERE → GROUP BY → 聚合计算 → HAVING → SELECT → ORDER BY`

### 常见面试坑：SELECT 的字段必须在 GROUP BY 中

```sql
-- ❌ 错误：name 不在 GROUP BY 中
SELECT name, department, AVG(salary)
FROM employees
GROUP BY department;

-- ✅ 正确
SELECT department, AVG(salary)
FROM employees
GROUP BY department;
```

多字段分组：

```sql
SELECT department, job_title, COUNT(*)
FROM employees
GROUP BY department, job_title;
```

---

## WHERE vs HAVING

这是面试最高频考点之一。

| 比较项 | WHERE | HAVING |
|--------|-------|--------|
| 执行时机 | **聚合之前**过滤行 | **聚合之后**过滤组 |
| 能否用聚合函数 | ❌ 不能 | ✅ 可以 |
| 对应关系 | 过滤"行" | 过滤"组" |
| 出现位置 | GROUP BY 之前 | GROUP BY 之后 |

```sql
-- 执行顺序演示
SELECT department, AVG(salary) AS avg_sal
FROM employees
WHERE salary > 0                    -- ① 先过滤行
GROUP BY department                 -- ② 再分组
HAVING AVG(salary) > 10000;         -- ③ 后过滤组
```

### 经典面试场景题

**场景 1：统计每个部门工资 > 10000 的员工数**

```sql
SELECT department, COUNT(*) AS high_salary_count
FROM employees
WHERE salary > 10000
GROUP BY department;
```

**场景 2：找出平均工资 > 10000 的部门（只统计有实际工资的员工）**

```sql
SELECT department, AVG(salary) AS avg_salary
FROM employees
WHERE salary IS NOT NULL
GROUP BY department
HAVING AVG(salary) > 10000;
```

**场景 3：某账户当月交易次数超过 5 次**

```sql
SELECT account_id, COUNT(*) AS tx_count
FROM transactions
WHERE tx_date BETWEEN '2026-06-01' AND '2026-06-30'
GROUP BY account_id
HAVING COUNT(*) > 5;
```

---

## 综合测试

假设 `orders` 表：

```sql
order_id | customer_id | amount  | status
---------+-------------+---------+-----------
1        | 101         | 100     | completed
2        | 102         | 200     | completed
3        | 101         | NULL    | refunded
4        | 103         | 150     | completed
5        | 101         | 300     | completed
```

1. `SELECT AVG(amount) FROM orders` → **结果：183.33**（(100+200+150+300)/4，NULL 被排除，refunded 不影响）

2. 每个客户 completed 状态的总消费金额：
   ```sql
   SELECT customer_id, SUM(amount) AS total
   FROM orders
   WHERE status = 'completed'
   GROUP BY customer_id;
   ```

3. 总消费金额超过 200 的客户：
   ```sql
   SELECT customer_id, SUM(amount) AS total
   FROM orders
   WHERE status = 'completed'
   GROUP BY customer_id
   HAVING SUM(amount) > 200;
   ```

4. `SELECT customer_id, COUNT(*) FROM orders GROUP BY customer_id HAVING COUNT(*) > 1`
   → **结果：只有 customer_id=101，count=3**（101 有三条订单）

---

## 一句话记忆

> **WHERE 是在分组前过滤行，HAVING 是在分组后过滤组。**
