## 安全校验

* 主要使用技术栈
    * 前端：Vue 3 + Vite + Pinia + Axios
    * 后端：Spring Boot + MyBatis + JWT + Spring Security
* 主要代码 ：参考Cah 前后端代码，这里不多赘述了主要记录问题的解决思路和体会

* 已完成的功能
    * 🔐 用户认证	登录后返回 JWT Token，前端存入 `localStorage`
    * 📡 前后端通信	封装 `request.ts`，自动携带 `Authorization: Bearer <token>`
    * 👤 个人主页	`/profile` 页面展示用户名、邮箱等基本信息
    * 🔒 安全控制	启用 `Spring Security`，通过 `JwtAuthenticationFilter` 验证 Token
    * 🛡️ 权限管理	受保护接口（如 /api/user/me）仅允许已认证用户访问

* 遇到的问题
    * 模块缺失错误  ❌ Failed to resolve import `"@/utils/request"`
        * 解决：
            创建 src/utils/request.ts
            配置 baseURL、请求拦截器（自动加 token）、响应拦截器（401 跳登录）
            ✅ 成果：统一 API 调用入口，自动处理认证
        * 体会:
            这个还真的挺有用的，请求服务的时候就会自动把token给带上，也能处理后端token没校验通过的情况（自动跳转主页面）
    * 页面卡在“加载中”  ❌ Profile 页面一直显示“加载中...”
        * 排查路径：打开浏览器 DevTools → Network
            发现 没有发出 /user/me 请求 → 怀疑前端逻辑未触发
            加 console.log → 发现 authStore.user 初始为 {}（非 null），导致 v-if="user" 为真但无数据
            同时发现 实际发了请求但被 CORS 拦截 
        * 解决： 前端 初始化 store 的 user = null 后端 ：Spring Security 拦截了 OPTIONS 预检请求 ，修改Security的配置类
            ```java
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ← 关键！
            ```
        * 体会遇到加载数据不出来的问题记得及时在请求的各个环节打断点，打输出，这样才能及时判断问题的所在
