-- 使用某个数据库（比如你之前创建的 myapp_db）
USE myapp_db;

-- 创建一张新表：employees（员工表）
CREATE TABLE employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INT,
    department VARCHAR(50),
    salary DECIMAL(10, 2),
    hire_date DATE,
    is_active TINYINT DEFAULT 1
);