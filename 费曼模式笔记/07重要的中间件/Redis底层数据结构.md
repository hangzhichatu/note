# Redis 底层数据结构（5 大类型 + 底层实现）

> 面试高频。不要只背"String=字符串"，要能说出**每种 key 的 value 底层用了什么结构**，以及"为什么这么设计、什么时候切换"。

---

## 一、一句话本质

> Redis 的所有 value 底层都不是"一个简单的类型"，而是**根据不同数据规模/特性，动态选择最优的底层数据结构**（这是 Redis 内存高效的核心）。面试从"Redis 的 value 有几种类型"问到"底层用什么结构、为什么"。

---

## 二、5 大类型 → 底层结构总览（先背这张表）

| Redis 类型（对外） | 底层数据结构 | 说明 |
|---|---|---|
| **String** | SDS（简单动态字符串）＋ int/embstr/raw 编码 | 整数用 int，短串用 embstr，长串用 raw |
| **List** | 早期 quicklist（ziplist 压缩列表 + linkedlist）｜3.2+ 纯 quicklist | 本质是"压缩列表组成的链表" |
| **Hash** | **ziplist**（元素少时）→ 切换 → **hashtable**（元素多时）| 小 --> 大 动态切换 |
| **Set** | **intset**（全整数且少时）→ 切换 → **hashtable** | 整数集合 → 哈希表 |
| **ZSet(有序集合)** | **ziplist**（少时）→ 切换 → **skiplist（跳表）+ hashtable** | 跳表保证有序 |

> ⚠️ 注意：Redis 7.0 把 listpack（更优的压缩结构）逐步替代 ziplist。但面试按主流答 ziplist/quicklist/skiplist 即可，提一句 listpack 是加分。

---

## 三、逐个底层结构拆解

### 1. SDS（简单动态字符串）— String 的底层

> 为解决 C 语言 `char*` 的问题而设计。

**C 的 char\* 有哪些坑（为什么 Redis 自己造 SDS）：**
1. 取长度要 O(n) 遍历到 `\0`
2. 二进制不安全：内容含 `\0` 会截断
3. 拼接要手动管理扩容（易溢出）

**SDS 结构（含头和变长数据）：**
```
+--------+--------+--------+------------+
| len(长度) | alloc(容量) | flags | buf[]  |   → 还带 flags 记录类型
+--------+--------+--------+------------+
```

**SDS 解决的 4 个问题：**
- **O(1) 拿长度**：直接读 len 字段
- **杜绝缓冲溢出**：拼接前检查 alloc，不够自动扩容（还有空间预分配减少扩容次数）
- **二进制安全**：以 len 而非 `\0` 判结束，能存二进制内容
- **惰性空间释放**：缩短不立刻回收，留 alloc 备用

**String 的 3 种编码（int/embstr/raw）：**
| 编码 | 条件 | 说明 |
|---|---|---|
| `int` | value 是纯整数 | 用 long 存储，省内存 |
| `embstr` | 短字符串（≤44字节）| SDS 和 RedisObject 连续分配，一次 malloc |
| `raw` | 长字符串（>44字节）| 分开分配两次 |

---

### 2. ziplist（压缩列表）— 小数据时的通用选择

> 一个"连续内存 + 紧凑存储"的数组结构，**内存占用极小**，但元素多/大时插入删除 O(n) 且会连锁更新 → 触发切换成 hashtable/skiplist。

```
[zlbytes][zltail][zllen][entry1][entry2]...[entryN][zlend]
                每个 entry: [prevlen][encoding][data]
```

**特点：**
- 连续内存，**省内存**（内存碎片少）
- 每个 entry 记录前一个的长度(prevlen) → **支持反向遍历**
- **缺点**：元素多时插入/删除要移动后续所有 entry（O(n)），且 prevlen 会连锁更新 → **不适合大列表**
- 所以：**小数据用它，大数据换 hashtable/skiplist**

---

### 3. hashtable — 大数据的通用选择（Hash/Set/ZSet 的键）

> Redis 的字典，用**哈希表 + 渐进式 rehash**。

```c
typedef struct dict {
    dictht ht[2];     // 两个哈希表：ht[0] 正式用，ht[1] 扩容时过渡
    int rehashidx;    // -1 表示没在 rehash
} dict;
```

**渐进式 rehash（面试重点）：**
- 扩容时**不是一次性搬所有数据**（会卡顿），而是把 rehashidx 记录进度
- **每次增删改查顺带搬一小部分**（rehash 一步），直到搬完
- 好处：把 O(n) 的大迁移**分摊到每次操作里**，避免阻塞
- **期间**：新增只进 ht[1]，查询先查 ht[0] 再查 ht[1]

**为什么用双表**：rehash 时新旧并存，查完旧的查新的，平滑扩容。

---

### 4. intset（整数集合）— Set 的"全整数小集合"形态

> Set 里**全是整数且数量少**时，用 intset（有序整数数组），省内存且支持二分查找。

```c
typedef struct intset {
    uint32_t encoding;  // 编码（16/32/64位整数）
    uint32_t length;    // 元素个数
    int8_t contents[];  // 元素数组（有序）
} intset;
```

**触发切换（intset → hashtable）：**
- 添加了**非整数**元素
- 或元素个数超过配置阈值（`set-max-intset-entries` 默认 512）

---

### 5. skiplist（跳表）— ZSet 的排序神器

> 有序集合需要"按分数排序 + 快速范围查"，链表 O(n) 太慢，用**跳表**做到 O(log n)。

**跳表思想（费曼比喻）：**
> 普通链表像"一条只能步行的街"，要找一个节点得一家家走。跳表给链表**加了几层"高架桥"**——每一层跳过更多节点。查找时从最高层快速跳，不行再逐层降下来定位，O(log n)。

```
level3: head ──────────────────────► node50 ──► NULL
level2: head ──────► node20 ───────► node50 ──► NULL
level1: head ──► n10 ──► n20 ──► n30 ──► n50 ──► NULL
```

**为什么 ZSet 用跳表而不用树/平衡树？**
- 跳表**实现简单**（比红黑树好写，调整就是改指针层数）
- **范围查询高效**：跳表天然有序，区间遍历方便
- **Redis 里 ZSet = skiplist(排序) + hashtable(按键查分数O(1)) 组合**

**⚠️ 面试坑**：**为什么 Redis 用跳表不用 B+树/红黑树？**
> 答：① 内存数据库不需要像磁盘那样"大分块读"（B+树为磁盘设计），跳表内存友好；② 跳表实现简单、易维护、范围查询(O(log n)+顺序遍历)够用；③ 支持区间、排名等操作方便。

---

## 四、切换阈值汇总（背诵）

| 结构 | 小→大 切换条件 |
|---|---|
| Hash：ziplist → hashtable | 元素 > 512 或 单个value > 64字节（hash-max-ziplist-entries/value）|
| Set：intset → hashtable | 出现非整数 或 元素 > 512 |
| ZSet：ziplist → skiplist | 元素 > 128 或 单个value > 64字节 |
| List | 由 quicklist（ziplist链表）组成，天然适应大列表 |

> 记忆：**Hash/ZSet 的 ziplist→大结构 主要在元素数(512/128)和单元素大小(64B)两个维度触发；Set 只关心是不是整数。**

---

## 五、为什么 Redis 快（顺带把性能问题答了）

1. **纯内存操作**（主要）
2. **单线程 + IO多路复用（epoll）**：无锁、无上下文切换、无同步开销
3. **高效数据结构**（SDS/跳表/压缩列表，省内存省时间）
4. **可持久化 + 渐进式 rehash** 避免阻塞

---

## 六、面试答题公式

**Q：讲讲 Redis 的底层数据结构？**
> 1（原理）：Redis 每种 value 底层不是固定结构，而是按数据规模/特性选最优。String用SDS+编码、List用quicklist、Hash/Set/ZSet在数据少时用ziplist/intset、多了切hashtable/skiplist。
> 2（取舍）：小数据用压缩结构省内存(ziplist/intset)，大数据切哈希表保证O(1)、ZSet用跳表保证有序+范围查询。SDS解决C字符串长度O(n)/二进制不安全/扩容问题。
> 3（实战·若依）：若依后端用 Redis 做缓存(token/验证码/字典)，涉及 String 存值、Hash 存用户会话等；理解底层在做 Redis 容量规划和性能优化时能判断该用哪种类型。

**Q：ZSet 底层为什么用跳表而不用红黑树？**
> 跳表实现简单、内存友好、范围查询高效，适合内存型数据库的"有序+范围"需求；红黑树平衡操作复杂，B+树为磁盘设计，Redis 不需要。

---

## 七、自测

Q1: String 的 3 种编码是什么，怎么切？
A: int(整数)/embstr(≤44B短串，SDS和对象连续分配一次malloc)/raw(>44B分两次)。按 value 内容自动选。

Q2: Redis 为什么用 SDS 而不是 C 的 char*？
A: O(1)长度、二进制安全、防溢出自动扩容、惰性释放；C的char*取长度O(n)、遇\0截断、易溢出。

Q3: 渐进式 rehash 解决了什么问题？
A: 把一次 O(n) 大迁移分摊到每次操作逐步搬，避免扩容瞬间阻塞 Redis。

Q4: Set 什么时候从 intset 切到 hashtable？
A: 添加非整数 或 元素超过 512。

Q5: ZSet 用跳表+哈希表组合，各管什么？
A: 跳表管排序+范围查询，哈希表按键 O(1) 查分数。

🔗 关联知识
[[07重要的中间件/Nacos]]（配置中心也用到类似思想）
[[02_JUC_并发编程/JUC复习速查总纲]]（并发/一致性上下文）
[[09_数据库深度剖析]]（对比 MySQL B+树——为什么磁盘库用B+树而内存库用跳表）
