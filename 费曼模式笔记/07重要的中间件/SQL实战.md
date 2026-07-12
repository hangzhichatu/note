# SQL 瀹炴垬锛氫粠涓氬姟闇€姹傚埌璇彞缈昏瘧

## 瀛︿範鐩爣

- 鎺屾彙銆屼笟鍔￠渶姹?鈫?SQL 璇彞銆嶇殑鎷嗚В鏂规硶
- DML锛堝鍒犳敼鏌ワ級+ DDL锛堣〃缁撴瀯绠＄悊锛? DCL锛堟潈闄愭帶鍒讹級鍏ㄨ鐩?- 瀹炴垬锛氱敤**鐢靛晢璁㈠崟鍦烘櫙**鍜?*鍛樺伐鑰冨嫟鍦烘櫙**缁冩墜

---

## 涓€銆佹牳蹇冩€濈淮锛氫簲姝ョ炕璇戞硶

鍐?SQL 涔嬪墠鍏堥棶鑷繁 5 涓棶棰橈細

```
鈶?瀵瑰摢涓〃鎿嶄綔锛?       鈫?FROM / INTO / UPDATE table
鈶?閫夊摢浜涘垪/瀛楁锛?      鈫?SELECT / SET / VALUES 閲屽啓浠€涔?鈶?瑕佽繃婊ゅ摢浜涜锛?      鈫?WHERE 鏉′欢鏄粈涔?鈶?瑕佷笉瑕佸垎缁?鑱氬悎锛?   鈫?GROUP BY + HAVING
鈶?缁撴灉瑕佹€庝箞鎺掑簭/闄愬埗锛?鈫?ORDER BY + LIMIT
```

**涓句緥锛?* 涓氬姟闇€姹傘€屾煡涓€涓?2026 骞?7 鏈堥攢鍞瓒呰繃 10000 鐨勯儴闂ㄣ€?
```
鈶?琛細orders锛堣鍗曡〃锛?鈶?鍒楋細department, SUM(amount) 浣滀负閿€鍞
鈶?杩囨护锛歰rder_date 鍦?2026-07锛岀姸鎬佹槸宸蹭粯娆?鈶?鍒嗙粍锛氭寜閮ㄩ棬 GROUP BY锛屼笖 HAVING SUM(amount) > 10000
鈶?鎺掑簭锛氭寜閿€鍞闄嶅簭
```

---

## 浜屻€佸缓琛ㄧ瘒锛圖DL锛?
### 鍦烘櫙锛氱數鍟嗚鍗曠郴缁?
涓氬姟闇€姹傦細涓哄叕鍙歌璁¤鍗曡〃鍜屽晢鍝佽〃

**缈昏瘧杩囩▼锛?*

| 琛ㄧ殑鐜板疄姒傚康 | SQL 鏈 |
|-------------|---------|
| 姣忎釜璁㈠崟鏈変釜缂栧彿 | 涓婚敭 id |
| 璁㈠崟鏈夊鎴枫€佹棩鏈熴€侀噾棰濄€佺姸鎬?| 瀛楁 |
| 涓€涓鍗曞彲鑳藉寘鍚涓晢鍝?| 闇€瑕佹媶鎴?order + t_order_item 涓ゅ紶琛?|
| 鍟嗗搧淇℃伅鐙珛缁存姢 | 鍗曠嫭寤?product 琛?|
| 璁㈠崟鍜屽晢鍝佺殑鍏崇郴 | 澶栭敭 |

```sql
-- 鍟嗗搧琛?CREATE TABLE product (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '鍟嗗搧鍚嶇О',
    category    VARCHAR(50)  NOT NULL COMMENT '鍟嗗搧鍒嗙被',
    price       DECIMAL(10,2) NOT NULL COMMENT '鍗曚环',
    stock       INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '搴撳瓨',
    status      TINYINT NOT NULL DEFAULT 1 COMMENT '1涓婃灦 0涓嬫灦',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '鍟嗗搧琛?;

-- 璁㈠崟涓昏〃
CREATE TABLE t_order (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no    VARCHAR(32) NOT NULL UNIQUE COMMENT '璁㈠崟鍙?,
    customer    VARCHAR(50) NOT NULL COMMENT '瀹㈡埛鍚嶇О',
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '鎬婚噾棰?,
    status      TINYINT NOT NULL DEFAULT 0 COMMENT '0寰呬粯娆?1宸蹭粯娆?2宸插彂璐?3宸插畬鎴?4宸插彇娑?,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    paid_at     DATETIME NULL COMMENT '浠樻鏃堕棿',
    INDEX idx_customer (customer),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) COMMENT '璁㈠崟涓昏〃';

-- 璁㈠崟鏄庣粏琛?CREATE TABLE t_order_item (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT NOT NULL COMMENT '璁㈠崟ID',
    product_id  BIGINT NOT NULL COMMENT '鍟嗗搧ID',
    quantity    INT NOT NULL COMMENT '鏁伴噺',
    unit_price  DECIMAL(10,2) NOT NULL COMMENT '涓嬪崟鏃跺崟浠?,
    FOREIGN KEY (order_id) REFERENCES t_order(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id)
) COMMENT '璁㈠崟鏄庣粏琛?;
```

### 馃 璁捐鏃剁殑涓氬姟缈昏瘧鎶€宸?
| 涓氬姟姒傚康 | SQL 瀹炵幇鏂瑰紡 |
|---------|-------------|
| 鍞竴缂栧彿銆佽鍗曞彿 | UNIQUE 绾︽潫 |
| 閲戦鐢?decimal 涓嶇敤 float | 绮剧‘璁＄畻 |
| 鐘舵€佹灇涓?| TINYINT + 娉ㄩ噴 |
| 璁板綍鍒涘缓/鏇存柊鏃堕棿 | DEFAULT CURRENT_TIMESTAMP |
| 鏁版嵁涓嶈兘涓虹┖ | NOT NULL |
| 楂橀鏌ヨ瀛楁 | 寤?INDEX |
| 涓よ〃鍏宠仈鍏崇郴 | FOREIGN KEY |

---

## 涓夈€佹彃鍏ユ暟鎹瘒锛圛NSERT锛?
### 涓氬姟锛氭柊澧炰竴鏉¤鍗?
**闇€姹傛弿杩帮細** 瀹㈡埛"寮犱笁"涓嬪崟涔颁簡涓や欢鍟嗗搧锛歩Phone锛? 鍙帮紝鍗曚环 6999锛夈€佸厖鐢靛櫒锛? 涓紝鍗曚环 99锛?
**缈昏瘧杩囩▼锛?*

```
姝ラ1锛氬厛鎻掑叆 order 涓昏〃 鈫?鎷垮埌璁㈠崟 ID
姝ラ2锛氱敤璁㈠崟 ID 鎻掑叆 t_order_item 鏄庣粏
姝ラ3锛氫袱寮犺〃鐨?total_amount 鍜?quantity 瀵瑰簲璧锋潵
```

```sql
-- 鎻掕鍗曚富琛?INSERT INTO t_order (order_no, customer, total_amount, status)
VALUES ('ORD202607120001', '寮犱笁', 6999*2 + 99*1, 1);

-- 鎻掓槑缁嗭紙order_id 瑕佺敤涓婇潰鎻掑叆鐨?id锛?INSERT INTO t_order_item (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 2, 6999.00),
(1, 2, 1, 99.00);
```

### INSERT 鍙樹綋

```sql
-- 鎻掑叆閮ㄥ垎瀛楁锛堝叾浠栫敤榛樿鍊硷級
INSERT INTO product (name, category, price, stock) 
VALUES ('鏃犵嚎钃濈墮鑰虫満', '鏁扮爜', 299.00, 500);

-- 鎵归噺鎻掑叆锛堜竴娆℃彃澶氭潯锛?INSERT INTO product (name, category, price, stock) VALUES
('鏈烘閿洏', '澶栬', 399.00, 200),
('榧犳爣鍨?, '澶栬', 29.90, 1000),
('鏄剧ず鍣ㄦ敮鏋?, '澶栬', 199.00, 150);

-- 浠庡彟涓€寮犺〃鏌ヨ缁撴灉鎻掑叆锛圗TL 鍦烘櫙锛?INSERT INTO product_backup (name, category, price, stock)
SELECT name, category, price, stock FROM product WHERE status = 0;
```

---

## 鍥涖€佹煡璇㈢瘒锛圫ELECT锛夆€斺€?閲嶇偣涓殑閲嶇偣

### 鍦烘櫙 1锛氱畝鍗曟煡璇?
**闇€姹傦細** 鏌ョ湅鎵€鏈変笂鏋跺晢鍝侊紝鎸変环鏍间粠浣庡埌楂樻帓鍒?
**缈昏瘧锛?* product 琛?鈫?閫夋墍鏈夊垪 鈫?status=1 杩囨护 鈫?ORDER BY price 鍗囧簭

```sql
SELECT * FROM product 
WHERE status = 1 
ORDER BY price ASC;
```

### 鍦烘櫙 2锛氭潯浠惰仛鍚?
**闇€姹傦細** 姣忎釜鍒嗙被鏈夊灏戜釜涓婃灦鍟嗗搧锛屼互鍙婂钩鍧囦环鏍?
**缈昏瘧锛?* product 琛?鈫?鎸?category 鍒嗙粍 鈫?COUNT + AVG 鑱氬悎 鈫?status=1 杩囨护

```sql
SELECT 
    category,
    COUNT(*)   AS product_count,
    ROUND(AVG(price), 2) AS avg_price
FROM product
WHERE status = 1
GROUP BY category
ORDER BY product_count DESC;
```

### 鍦烘櫙 3锛氬琛?JOIN

**闇€姹傦細** 鏌?2026 骞?7 鏈堢殑鎵€鏈夎鍗曪細鏄剧ず璁㈠崟鍙枫€佸鎴峰悕銆佸晢鍝佹竻鍗曘€佹€婚噾棰?
**缈昏瘧锛?* order + t_order_item + product 涓夎〃鍏宠仈 鈫?閫夐渶瑕佺殑鍒?鈫?鏃堕棿杩囨护

```sql
SELECT 
    o.order_no,
    o.customer,
    p.name      AS product_name,
    oi.quantity,
    oi.unit_price,
    o.total_amount,
    o.created_at
FROM t_order o
JOIN t_order_item oi ON o.id = oi.order_id
JOIN product p ON oi.product_id = p.id
WHERE o.created_at >= '2026-07-01' 
  AND o.created_at < '2026-08-01'
ORDER BY o.created_at DESC, o.order_no;
```

### 鍦烘櫙 4锛氭潯浠剁瓫閫?+ 鑱氬悎

**闇€姹傦細** 鏌?2026 骞?7 鏈堟秷璐规€婚 TOP 5 鐨勫鎴?
**缈昏瘧锛?* order 琛?鈫?鎸?customer 鍒嗙粍 鈫?姹傞噾棰?SUM 鈫?杩囨护宸蹭粯娆俱€?鏈堣鍗?鈫?鎺掑簭鍙?TOP 5

```sql
SELECT 
    customer,
    COUNT(*)          AS order_count,
    SUM(total_amount) AS total_spent
FROM t_order
WHERE status IN (1, 2, 3)       -- 宸蹭粯娆剧殑璁㈠崟
  AND created_at >= '2026-07-01'
  AND created_at < '2026-08-01'
GROUP BY customer
ORDER BY total_spent DESC
LIMIT 5;
```

### 鍦烘櫙 5锛欻AVING 杩囨护鍒嗙粍鍚庣粨鏋?
**闇€姹傦細** 鏌ユ湀娑堣垂瓒呰繃 5000 鐨勫鎴凤紙鎺掗櫎寰堝皬鐨勬暎瀹級

**缈昏瘧锛?* 鍜屽満鏅?4 涓€鏍峰垎缁?鈫?澶氫竴姝?HAVING 杩囨护鑱氬悎鍚庣殑缁撴灉

```sql
SELECT 
    customer,
    SUM(total_amount) AS total_spent
FROM t_order
WHERE status IN (1, 2, 3)
  AND created_at >= '2026-07-01'
  AND created_at < '2026-08-01'
GROUP BY customer
HAVING SUM(total_amount) > 5000
ORDER BY total_spent DESC;
```

### 鈿狅笍 WHERE vs HAVING 鍙ｈ瘈

```
WHERE  鉃?鍒嗙粍鍓嶈繃婊わ紙鍘熷鏁版嵁琛岋級
HAVING 鉃?鍒嗙粍鍚庤繃婊わ紙鑱氬悎鍚庣殑缁撴灉锛?
WHERE 涓嶈兘鍐欒仛鍚堝嚱鏁帮紙SUM銆丆OUNT...锛?HAVING 涓撻棬鍐欒仛鍚堟潯浠?```

### 鍦烘櫙 6锛氬瓙鏌ヨ

**闇€姹傦細** 鏌ャ€岃喘涔拌繃 iPhone銆嶇殑瀹㈡埛鏈夊摢浜?
```sql
-- 鍐欐硶1锛欽OIN 鏇寸洿瑙?SELECT DISTINCT o.customer
FROM t_order o
JOIN t_order_item oi ON o.id = oi.order_id
JOIN product p ON oi.product_id = p.id
WHERE p.name LIKE '%iPhone%';

-- 鍐欐硶2锛氬瓙鏌ヨ锛堥€傚悎闈㈣瘯/澶嶆潅宓屽锛?SELECT DISTINCT customer
FROM t_order
WHERE id IN (
    SELECT order_id 
    FROM t_order_item 
    WHERE product_id = (SELECT id FROM product WHERE name = 'iPhone 16')
);
```

**閫変腑 JOIN 杩樻槸瀛愭煡璇㈢殑鍐崇瓥锛?*

| 鍦烘櫙 | 鎺ㄨ崘鏂瑰紡 |
|------|---------|
| 鍙渶瑕佷粠涓昏〃鍙栨暟鎹紝瀛愭煡璇㈠彧鏄釜杩囨护鏉′欢 | 瀛愭煡璇紙IN / EXISTS锛墊
| 闇€瑕佸悓鏃跺睍绀轰富琛ㄥ拰瀛愯〃鐨勬暟鎹?| JOIN |
| 闇€瑕佸垽鏂€屼笉瀛樺湪銆?| NOT IN / NOT EXISTS |
| 澶ф暟鎹噺 + 绱㈠紩涓嶅尮閰?| JOIN锛堝瓙鏌ヨ鍦ㄥぇ鏁版嵁閲忎笅鍙兘鎱級|

---

## 浜斻€佹洿鏂扮瘒锛圲PDATE锛?
### 涓氬姟 1锛氫慨鏀瑰晢鍝佷环鏍?
**闇€姹傦細** 鑻规灉鎵嬫満鍏ㄧ郴鍒楅檷浠?500 鍏?
```sql
-- 缈昏瘧锛歎PDATE product 鈫?SET price = price - 500 鈫?WHERE 鏉′欢
UPDATE product 
SET price = price - 500 
WHERE name LIKE '%iPhone%';
```

### 涓氬姟 2锛氳鍗曠姸鎬佹祦杞?
**闇€姹傦細** 灏嗘槰澶╀箣鍓嶅垱寤轰笖宸蹭粯娆句絾鏈彂璐х殑璁㈠崟锛屾爣璁颁负"宸插彇娑?

```sql
UPDATE t_order
SET status = 4, -- 宸插彇娑?    updated_at = NOW()
WHERE status = 1 -- 宸蹭粯娆?  AND created_at < DATE_SUB(CURDATE(), INTERVAL 1 DAY)
  AND created_at >= '2026-01-01';
```

### 鈿狅笍 UPDATE 鐨勪繚鍛芥妧宸?
```sql
-- 鍔″繀鍏堟煡鍐嶆敼锛佸厛 SELECT 鐪嬩竴涓嬪懡涓摢浜涜
SELECT id, order_no, status, created_at 
FROM t_order
WHERE status = 1 AND created_at < DATE_SUB(CURDATE(), INTERVAL 30 DAY);

-- 纭鏃犺鍚庡啀 UPDATE锛堟妸 WHERE 鏉′欢鍘熷皝涓嶅姩鎼繃鍘伙級
UPDATE t_order
SET status = 4
WHERE status = 1 AND created_at < DATE_SUB(CURDATE(), INTERVAL 30 DAY);

-- UPDATE 涓嶅甫 WHERE = 鍏ㄨ〃鏇存柊锛堢伨闅剧骇鎿嶄綔锛?UPDATE product SET price = 0; -- 鉂?鎵炬鐨勫啓娉?```

---

## 鍏€佸垹闄ょ瘒锛圖ELETE锛?
### 涓氬姟 1锛氱墿鐞嗗垹闄わ紙鎱庣敤锛?
**闇€姹傦細** 鍒犻櫎涓€骞村墠宸插彇娑堢殑璁㈠崟

```sql
-- 鍏堟煡
SELECT id, order_no, status, created_at 
FROM t_order
WHERE status = 4 
  AND created_at < DATE_SUB(CURDATE(), INTERVAL 1 YEAR);

-- 纭鍐嶅垹锛堟敞鎰?t_order_item 鏈?CASCADE锛屼富琛ㄥ垹浜嗘槑缁嗚嚜鍔ㄥ垹锛?DELETE FROM t_order
WHERE status = 4 
  AND created_at < DATE_SUB(CURDATE(), INTERVAL 1 YEAR);
```

### 涓氬姟 2锛氶€昏緫鍒犻櫎锛堟帹鑽愶級

瀹為檯鐢熶骇鐜涓€鑸?*涓嶅垹鏁版嵁**锛岀敤閫昏緫鍒犻櫎锛?
```sql
-- 缁欎骇鍝佽〃鍔犱竴涓?is_deleted 瀛楁
ALTER TABLE product ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0姝ｅ父 1宸插垹闄?;

-- "鍒犻櫎"鍏跺疄鏄洿鏂?UPDATE product SET is_deleted = 1, status = 0 WHERE id = 10;

-- 鏌ヨ鏃惰繃婊ゅ凡鍒犻櫎鐨?SELECT * FROM product WHERE is_deleted = 0;
```

### DELETE 淇濆懡娓呭崟

```
鈽?WHERE 鍐欎簡鍚楋紵锛堟病鍐?WHERE = 鍒犲叏琛級
鈽?鍏堢敤 SELECT 楠岃瘉 WHERE 鍛戒腑鍝簺琛?鈽?鏈夊閿叧鑱斿悧锛烵N DELETE CASCADE 浼氫笉浼氳繛甯﹀垹鏁版嵁锛?鈽?纭畾涓嶈兘/涓嶉渶瑕侀€昏緫鍒犻櫎锛?```

---

## 涓冦€佹潈闄愭帶鍒剁瘒锛圖CL锛?
### 鍦烘櫙锛氬叕鍙哥粰鏂版潵鐨勫疄涔犵敓鍒嗛厤鏁版嵁搴撴潈闄?
**涓氬姟闇€姹傚垎鏋愶細**

```
瑙掕壊锛氬疄涔犵敓锛堝彧璇伙級
鏉冮檺锛氬彧鑳?SELECT 鐢靛晢鏁版嵁搴撶殑琛?杩炴帴锛氬彧鑳藉湪鍏徃鍐呯綉锛?92.168.1.%锛?```

```sql
-- 1. 鍒涘缓鐢ㄦ埛锛岄檺瀹氭潵婧?IP
CREATE USER 'intern'@'192.168.1.%' IDENTIFIED BY 'ReadOnly@2026';

-- 2. 鍙粰 SELECT 鏉冮檺
GRANT SELECT ON ecommerce.* TO 'intern'@'192.168.1.%';

-- 3. 鍒锋柊
FLUSH PRIVILEGES;
```

**涓氬姟闇€姹傦細鐮斿彂鏂颁汉鏈夊啓琛ㄦ潈闄愪絾涓嶈兘鍒犺〃**

```
瑙掕壊锛氬紑鍙戜汉鍛?鏉冮檺锛歋ELECT INSERT UPDATE DELETE锛屼笉鑳?DDL锛堜笉鑳藉垹琛?鏀硅〃缁撴瀯锛?```

```sql
CREATE USER 'dev_lisi'@'%' IDENTIFIED BY 'DevP@ss123';
GRANT SELECT, INSERT, UPDATE, DELETE ON ecommerce.* TO 'dev_lisi'@'%';
FLUSH PRIVILEGES;
```

**涓氬姟闇€姹傦細绂佹鏌愪釜鐢ㄦ埛鍒犻櫎鏁版嵁**

```sql
-- 鎾ら攢 DELETE 鏉冮檺
REVOKE DELETE ON ecommerce.* FROM 'dev_lisi'@'%';
FLUSH PRIVILEGES;
```

### 鏉冮檺鐨勫喅绛栬〃

| 涓氬姟瑙掕壊 | 闇€瑕佺殑鏉冮檺 | 寤鸿 |
|---------|-----------|------|
| 鎶ヨ〃鏌ョ湅 | 鍙?SELECT | 鍙璐︽埛 |
| CRUD 寮€鍙?| SELECT INSERT UPDATE DELETE | 涓嶈兘缁?DROP/ALTER |
| DBA | ALL PRIVILEGES | 闄愬埗鏉ユ簮 IP锛堜粎鍫″瀿鏈猴級|
| 绗笁鏂规帴鍙?| SELECT + 鎸囧畾琛ㄧ殑 INSERT | 鍙紑鏀捐鐢ㄥ埌鐨勮〃 |
| 鏁版嵁鍒嗘瀽甯?| SELECT + CREATE TEMPORARY | 涓存椂琛ㄥ仛鍒嗘瀽鐢?|

---

## 鍏€佺患鍚堝疄鎴橈細鍛樺伐鑰冨嫟绯荤粺

鐜板湪缁欎綘涓€涓畬鏁寸殑**鏂板満鏅?*锛岃鎸変簲姝ョ炕璇戞硶鑷繁鎬濊€冿紝鐪嬬瓟妗堝墠鍏堣瘯鐫€鍐欏嚭 SQL銆?
### 寤鸿〃

```sql
CREATE TABLE employee (
    id      INT PRIMARY KEY AUTO_INCREMENT,
    name    VARCHAR(50) NOT NULL,
    dept    VARCHAR(50) NOT NULL COMMENT '閮ㄩ棬',
    hire_date DATE NOT NULL
);

CREATE TABLE attendance (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    emp_id      INT NOT NULL,
    check_in    DATETIME NOT NULL COMMENT '绛惧埌鏃堕棿',
    check_out   DATETIME COMMENT '绛鹃€€鏃堕棿',
    FOREIGN KEY (emp_id) REFERENCES employee(id)
);
```

### 璇蜂綘缈昏瘧浠ヤ笅闇€姹?
> 鈶?鏌ヨ 2026 骞?7 鏈堬紝鍝釜閮ㄩ棬鐨勫钩鍧囦笂鐝渶鏃╋紙鎸夌鍒版椂闂村钩鍧囷級銆?>
> 鈶?鏌ヨ杩熷埌鏈€澶氱殑鍓?3 鍚嶅憳宸ワ紙瀹氫箟锛氱鍒版椂闂存櫄浜?09:00:00 绠楄繜鍒帮級銆?>
> 鈶?灏?鎶€鏈儴"鎵€鏈夊憳宸ョ殑绛惧埌璁板綍涓殑閮ㄩ棬淇℃伅鏇存柊涓?鐮斿彂閮?锛堟媶鍒嗭細鍏跺疄璇ユ洿鏂?employee 琛ㄧ殑 dept 瀛楁锛夈€?
<details>
<summary>鐐瑰嚮鐪嬬瓟妗?/summary>

**鈶?鏈€鏃╅儴闂細**

```sql
SELECT 
    e.dept,
    TIME(AVG(TIME_TO_SEC(check_in))) AS avg_check_in
FROM attendance a
JOIN employee e ON a.emp_id = e.id
WHERE a.check_in >= '2026-07-01' AND a.check_in < '2026-08-01'
GROUP BY e.dept
ORDER BY avg_check_in ASC;
```

**鈶?杩熷埌鏈€澶氬憳宸ワ細**

```sql
SELECT 
    e.id,
    e.name,
    COUNT(*) AS late_count
FROM attendance a
JOIN employee e ON a.emp_id = e.id
WHERE TIME(a.check_in) > '09:00:00'
GROUP BY e.id, e.name
ORDER BY late_count DESC
LIMIT 3;
```

**鈶?閮ㄩ棬鏀瑰悕锛?*

```sql
UPDATE employee 
SET dept = '鐮斿彂閮? 
WHERE dept = '鎶€鏈儴';
```

</details>

---

---

## 涔濄€丼QL 涓殑閫昏緫鍒ゆ柇

### 涓轰粈涔堣鍦?SQL 閲屽啓閫昏緫锛?
澶氫釜绯荤粺鍏辩敤鍚屼竴涓暟鎹簱鏃讹紝鎶婄畝鍗曠殑鏍煎紡鍖?鍒嗙被/鏍囪閫昏緫鍐欏湪 SQL 閲岋紝鑳戒繚璇佹墍鏈夋秷璐圭寰楀埌鐨勬暟鎹牸寮忎竴鑷达紝閬垮厤姣忎釜绯荤粺閲嶅瀹炵幇鐩稿悓鐨勯€昏緫鍒ゆ柇銆?
---

### 1. `IF()` 鈥?绠€鍗曚簩閫変竴

璇硶锛歚IF(鏉′欢, 鍊间负鐪熸椂, 鍊间负鍋囨椂)`

```sql
SELECT 
    name,
    price,
    IF(price > 5000, '楂樹环', '鏅€?) AS price_tag
FROM product
WHERE status = 1;
```

```sql
-- 搴撳瓨绾㈢豢鐏?SELECT 
    name,
    stock,
    IF(stock < 100, '鈿狅笍 琛ヨ揣', '鉁?鍏呰冻') AS stock_status
FROM product
WHERE status = 1;
```

| 鍦烘櫙 | `IF()` | `CASE WHEN` |
|------|--------|-------------|
| 涓や釜鍒嗘敮 | 鉁?鎺ㄨ崘 | 涔熻 |
| 涓変釜鍙婁互涓婂垎鏀?| 鉂?涓嶉€傜敤 | 鉁?鎺ㄨ崘 |
| 鍦ㄨ仛鍚堝嚱鏁伴噷宓屽 | 鉂?涓嶅お琛?| 鉁?鎺ㄨ崘 |

---

### 2. `CASE WHEN` 鈥?涓囪兘鐨勬潯浠惰〃杈惧紡

璇硶锛?
```sql
CASE 
    WHEN 鏉′欢1 THEN 缁撴灉1
    WHEN 鏉′欢2 THEN 缁撴灉2
    ELSE 榛樿缁撴灉
END
```

#### 鍦烘櫙锛氱姸鎬佺爜杞腑鏂?
```sql
SELECT 
    order_no,
    customer,
    CASE 
        WHEN status = 0 THEN '寰呬粯娆?
        WHEN status = 1 THEN '宸蹭粯娆?
        WHEN status = 2 THEN '宸插彂璐?
        WHEN status = 3 THEN '宸插畬鎴?
        WHEN status = 4 THEN '宸插彇娑?
        ELSE '鏈煡'
    END AS status_desc
FROM t_order;
```

#### 鍦烘櫙锛氫环鏍煎垎妗?
```sql
SELECT 
    name,
    price,
    CASE 
        WHEN price > 7000      THEN '楂樼'
        WHEN price >= 3000     THEN '涓'
        ELSE '鍏ラ棬'
    END AS level
FROM product
WHERE status = 1;
```

---

### 3. `CASE WHEN` + 鑱氬悎鍑芥暟 = 琛岃浆鍒楃粺璁★紙鏍稿績鎶€宸э級

**鍘熺悊锛?* CASE WHEN 缁欐瘡涓€琛屾墦鏍囩锛堢鍚堟潯浠舵爣 1锛屼笉绗﹀悎鏍?0锛夛紝鐒跺悗 SUM 鎶?1 绱姞璧锋潵鈥斺€旀湰璐ㄤ笂灏辨槸"鏁版湁澶氬皯琛岀鍚堣繖涓潯浠?銆?
鎷嗗紑鐪嬩腑闂磋繃绋嬶細

```sql
-- 鍏堢湅 CASE WHEN 缁欐瘡琛屾墦浜嗕粈涔堟爣绛?SELECT 
    name,
    price,
    CASE WHEN price >= 3000 AND price <= 7000 THEN 1 ELSE 0 END AS is_mid
FROM product WHERE status = 1;
```

```
name              price    is_mid
iPhone 16 Pro Max 9448.95  0
灏忕背 15 Ultra     6298.95  1    鈫?鍙湁杩欒鏍囦簡 1
...                         0
```

鐒跺悗鐢?`SUM()` 鎶婅繖涓€鍒楀姞璧锋潵锛?
```sql
SELECT 
    SUM(CASE WHEN price > 7000 THEN 1 ELSE 0 END)     AS '楂樼鏁伴噺',
    SUM(CASE WHEN price >= 3000 AND price <= 7000 
             THEN 1 ELSE 0 END)                        AS '涓鏁伴噺',
    SUM(CASE WHEN price < 3000 THEN 1 ELSE 0 END)      AS '鍏ラ棬鏁伴噺'
FROM product
WHERE status = 1;
```

> **璁板繂娉曪細** CASE 缁欐瘡琛屾墦鍕惧弶 鈫?SUM 鏁颁竴鍏辨湁鍑犱釜鍕?
杩欎釜鎶€宸у湪涓€鏉?SQL 閲屽氨鑳藉畬鎴愬鏉傜殑澶氱淮缁熻锛屼笉鐢ㄥ湪涓氬姟浠ｇ爜閲屽啓寰幆銆?
---

### 4. NULL 澶勭悊涓変欢濂?
```sql
IFNULL(瀛楁, 榛樿鍊?     -- 濡傛灉瀛楁鏄?NULL锛岃繑鍥為粯璁ゅ€?COALESCE(鍊?, 鍊?, ...) -- 浠庡乏鍒板彸杩斿洖绗竴涓潪 NULL 鐨勫€?NULLIF(鍊?, 鍊?)        -- 濡傛灉鍊? = 鍊?锛岃繑鍥?NULL锛屽惁鍒欒繑鍥炲€?
```

```sql
SELECT 
    order_no,
    customer,
    IFNULL(paid_at, '灏氭湭浠樻') AS payment_time
FROM t_order;
```

**鍏充簬 NULL 鐨勪笁涓噸瑕佷簨瀹烇細**

| 瑙勫垯 | 璇存槑 | 涓句緥 |
|------|------|------|
| `COUNT(*)` 浼氭暟 NULL 琛?| 鏁拌鏁?| `COUNT(paid_at)` 璺宠繃 NULL 琛?|
| 鑱氬悎鍑芥暟璺宠繃 NULL | SUM/AVG 璁＄畻鏃舵棤瑙?NULL | AVG 搴曞眰 = SUM / COUNT(闈炵┖) |
| NULL 鍙備笌杩愮畻缁撴灉涓?NULL | 涓嶆槸鎶ラ敊锛屾槸闈欓粯寰?NULL | `price / NULL` 鈫?NULL |

**娉ㄦ剰锛?* 鍦ㄧ幆姣?鍚屾瘮璁＄畻鏃讹紝NULL 涓嶅弬涓庤繍绠楃殑鐗规€т笉浼氳浣犳姤閿欙紝浣嗕細闈欓粯鍦颁骇鐢?NULL 缁撴灉銆傞渶瑕佺敤 `COALESCE` 鎴?`IFNULL` 涓诲姩澶勭悊銆?
---

### 5. 缁煎悎瀹炴垬锛氫竴鏉?SQL 瀹屾垚澶氱淮搴︽暟鎹鐞?
```sql
SELECT 
    name,
    category,
    price                                   AS current_price,
    ROUND(price / 1.05, 2)                  AS original_price,
    CONCAT(ROUND(
        (price - price/1.05) / (price/1.05) * 100, 2
    ), '%')                                  AS increase_rate,
    CASE 
        WHEN price > 7000      THEN '楂樼'
        WHEN price >= 3000     THEN '涓'
        ELSE '鍏ラ棬'
    END                                      AS level,
    IF(stock < 100, '鈿狅笍 琛ヨ揣', '鉁?鍏呰冻')   AS stock_status
FROM product
WHERE status = 1
ORDER BY 
    CASE 
        WHEN price > 7000 THEN 1
        WHEN price >= 3000 THEN 2
        ELSE 3
    END,
    price DESC;
```

杩欐潯 SQL 浠ｆ浛浜嗕笟鍔″眰澶氭浠ｇ爜锛?- 鉁?鏁板杩愮畻锛氱畻鍑哄師浠?- 鉁?瀛楃鎷兼帴锛氱畻娑ㄥ箙姣斾緥
- 鉁?CASE WHEN锛氫环鏍煎垎妗?- 鉁?IF()锛氬簱瀛樼孩缁跨伅
- 鉁?ORDER BY CASE WHEN锛氳嚜瀹氫箟鎺掑簭瑙勫垯

---

### 6. `INSERT INTO ... SELECT` 鈥?鏌ヨ缁撴灉鍐欏叆鍙︿竴寮犺〃

**ETL/褰掓。甯哥敤锛?*

```sql
-- 鏂板缓褰掓。琛?CREATE TABLE t_order_archive LIKE t_order;

-- 鍏堟煡纭锛屽啀鎼暟鎹?INSERT INTO t_order_archive (id, order_no, customer, total_amount, status, created_at, paid_at)
SELECT id, order_no, customer, total_amount, status, created_at, paid_at
FROM t_order
WHERE status = 4;  -- 宸插彇娑堢殑璁㈠崟

-- 纭鎼畬鍐嶅垹
DELETE FROM t_order WHERE status = 4;
```

**涓€姝ュ缓琛?+ 鐏屾暟鎹紙蹇€熷浠斤級锛?*

```sql
CREATE TABLE t_order_bak AS SELECT * FROM t_order;
```

> 鈿狅笍 娉ㄦ剰锛氳繖绉嶆柟寮忎笉浼氬鍒剁储寮曞拰澶栭敭

---

## 鍗併€丼QL 涔﹀啓鐨勫父瑙佸潙

| 鍧?| 閿欒鍐欐硶 | 姝ｇ‘鍐欐硶 |
|----|---------|---------|
| NULL 鍒ゆ柇鐢?= | `WHERE name = NULL` | `WHERE name IS NULL` |
| 瀛楃涓蹭笉鐢ㄥ紩鍙?| `WHERE name = 寮犱笁` | `WHERE name = '寮犱笁'` |
| DELETE 娌?WHERE | `DELETE FROM product` | 鍏?SELECT 纭 |
| 鏃堕棿鐩存帴姣斿瓧绗︿覆 | `WHERE date = '2026-07-12'` | 鐢?DATE 鍑芥暟鎴?>= < 鑼冨洿 |
| GROUP BY 婕忓垪 | `SELECT id, name, COUNT(*) ... GROUP BY name` | SELECT 鐨勯潪鑱氬悎鍒楀繀椤诲湪 GROUP BY 閲?|
| 鑱氬悎鏉′欢鍐欏湪 WHERE | `WHERE COUNT(*) > 5` | `HAVING COUNT(*) > 5` |

---

## 涓€鍙ヨ瘽鎬荤粨

> **SQL 涓嶆槸鑳屽嚭鏉ョ殑锛屾槸缈昏瘧鍑烘潵鐨勩€?*
> 鎶婁笟鍔￠渶姹?-> 鎷嗘垚銆屽摢涓〃銆佸摢浜涘垪銆佷粈涔堟潯浠躲€佽涓嶈鍒嗙粍銆佹帓涓嶆帓搴忋€?> 姣忎釜 WHERE 鏉′欢閮藉搴斾竴鍙ヤ笟鍔＄害鏉燂紝姣忎釜 JOIN 閮藉搴斾竴涓幇瀹炲叧鑱斻€?
涓嬫閬囧埌浠讳綍 SQL 闇€姹傦紝鍏堝湪蹇冮噷杩囦竴閬嶄簲姝ョ炕璇戞硶锛屽啀鍔ㄦ墜鍐欍€?
---
*绗旇鏁寸悊浜?2026-07-12*



## 十一、窗口函数（Window Function）— 面试加分项

### 什么是窗口函数？

窗口函数在**不改变行数**的前提下对每一行计算一个聚合值。而 GROUP BY 会把多行压成一行。

**核心语法：** 函数() OVER (PARTITION BY 分组字段 ORDER BY 排序字段)

### ROW_NUMBER() — 分组排名

`sql
-- 需求：每个分类下价格最高的商品（带商品名）
SELECT category, name, price
FROM (
    SELECT category, name, price,
           ROW_NUMBER() OVER (PARTITION BY category ORDER BY price DESC) AS rk
    FROM product
) AS ranked
WHERE rk = 1;
`

| 写法 | 优点 | 缺点 |
|------|------|------|
| 子查询 + JOIN + MAX | 容易理解 | 查第2名要改条件 |
| **窗口函数** | 扩展性强，WHERE rk = 2 就行；性能更好 | 需要理解 OVER() 语法 |

### 其他窗口函数

`sql
-- RANK()：并列排名，有间隔（1,1,3）
-- DENSE_RANK()：并列排名，无间隔（1,1,2）
-- LAG()：取上一行的值
-- LEAD()：取下一行的值

SELECT name, category, price,
       ROW_NUMBER() OVER (ORDER BY price DESC) AS row_rk,
       RANK() OVER (ORDER BY price DESC) AS rank_rk,
       DENSE_RANK() OVER (ORDER BY price DESC) AS dense_rk
FROM product WHERE status = 1;
`


### EXISTS vs IN

**面试高频：** NOT IN 遇到结果集里有 NULL 时，整个查询返回 0 条数据！

`sql
-- ❌ 有坑
SELECT * FROM t_order WHERE customer NOT IN 
    (SELECT customer FROM t_order WHERE status = 4);

-- ✅ 安全
SELECT * FROM t_order WHERE NOT EXISTS 
    (SELECT 1 FROM t_order t2 WHERE t2.status = 4 AND t2.customer = t_order.customer);
`

**选择策略：**
| 场景 | 推荐 |
|------|------|
| 子查询结果集小 | IN |
| 外层表小，子查询大 | EXISTS |
| 判断不存在 | NOT EXISTS > NOT IN |
