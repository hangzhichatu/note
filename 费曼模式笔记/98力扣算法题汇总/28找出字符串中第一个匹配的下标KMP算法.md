## 题目 经典算法题解析：找出字符串中第一个匹配项的下标 -KMP算法思路梳理和代码详细解释

* 题目描述 : 给定两个字符串 haystack（主串）和 needle（模式串），要求找出 needle 在 haystack 中第一次出现的位置（下标从 0 开始）。如果找不到，则返回 -1。

### 暴力解法
* 思路双指针遍历，遍历 主串的每一个字符开始，利用双指针同时 在主串和模式串直接同步前进，如果匹配则同步前进，如果不匹配则则将模式串的指针下标移动回原点，主串指针往后移动一位。

```java
    class Solution {
        public int strStr(String haystack, String needle) {
            int hLen = haystack.length();
            int nLen = needle.length();

            // 边界情况：如果 needle 为空，通常认为在索引 0 处匹配
            if (nLen == 0) return 0;

            // 如果 needle 比 haystack 长，不可能匹配
            if (nLen > hLen) return -1;

            // 遍历 haystack，只需要遍历到 hLen - nLen 即可
            for (int i = 0; i <= hLen - nLen; i++) {
                boolean isMatch = true;
                // 检查从 i 开始的子串是否与 needle 相同
                for (int j = 0; j < nLen; j++) {
                    if (haystack.charAt(i + j) != needle.charAt(j)) {
                        isMatch = false;
                        break; // 发现不匹配，立即停止当前比较
                    }
                }
                // 如果整个 needle 都匹配上了
                if (isMatch) {
                    return i;
                }
            }

            return -1;
        }
    }
```

### 工程解法 使用内置API快速解决
```java
class Solution {
    public int strStr(String haystack, String needle) {
        // 直接调用 JDK 内置方法
        return haystack.indexOf(needle);
    }
}
```

### KMP算法详解部分 关于 next 数组
* 核心思想是分治， 考虑这样一个模式串 abcdabce 如果主串在对比最后一个字面 e的时候不匹配了将指针移动回原点在匹配浪费了，将指针移动到d对比可以节约时间
* 这里需要 声明一个核心概念 核心概念：**最长相等前后缀**   
* 要理解 next 数组，首先要理解什么是前缀和后缀。
        假设有一个字符串 S = "ABABC"：
        真前缀（不包含最后一个字符的前缀集合）："A", "AB", "ABA", "ABAB"
        真后缀（不包含第一个字符的后缀集合）："C", "BC", "ABC", "BABC"
| 下标 `i` | 子串 `needle[0...i]` | 最长相等前后缀 | 长度 (`next[i]`) | 解释 |
| :--- | :--- | :--- | :--- | :--- |
| 0 | `a` | 无 | 0 | 单个字符没有真前后缀 |
| 1 | `aa` | `a` | 1 | 前缀`a` == 后缀`a` |
| 2 | `aab` | 无 | 0 | 前缀`a`,`aa` != 后缀`b`,`ab` |
| 3 | `aaba` | `a` | 1 | 前缀`a` == 后缀`a` |
| 4 | `aabaa` | `aa` | 2 | 前缀`aa` == 后缀`aa` |
| 5 | `aabaaf` | 无 | 0 | 无相等前后缀 |


* 关于工程上的一些实践想法，如果用暴力算法去匹配复杂度是N平方级别的， 考虑每一个前缀的最长相等前后缀都是在 前面的基础上累加的，所以当长度K 不匹配的时候考虑分治思路，查找长度为 K-1的最长相等后缀继续进行对比，这样做指针能够跳跃过很多内容，优化了效率

* 全体代码实现
```java
class Solution {
    public int strStr(String haystack, String needle) {
        int n = haystack.length(), m = needle.length();
        if(m == 0)return 0;

        //构建next数组
        int[] next = new int[m];//注释 java初始化的时候是0 但是使用C++等语言的时候需要额外进行初始化操作
        for(int i = 1,j = 0;i<m;i++){
            //i 从1开始是因为 单字符没有真后缀前缀
            while(j> 0 && needle.charAt(i) != needle.charAt(j)){
                j = next[j-1];//这里的语义是尝试如果不匹配的话尝试对比长度为 j的字串最大前缀是否对比成功
            }
            if(needle.charAt(j) == needle.charAt(i)){
                j++;
            }
            next[i] = j;
        }

        //开始KMP匹配过程
        for(int i=0,j=0;i<n;i++){
            //处理不匹配情况needle指针回退到next数组指向的上一个可能的对比的位置
            while (j > 0 && haystack.charAt(i) != needle.charAt(j)) {
                j = next[j - 1];
            }
            //成功匹配上 j自增一对比下一个
            if (haystack.charAt(i) == needle.charAt(j)) {
                j++;
            }
            //完成匹配的结束分支，当j = 时候
            if (j == m) {
                return i - m + 1; // 找到匹配项，返回起始下标
            }
        }
        return -1;//边界条件处理
    }
}
```