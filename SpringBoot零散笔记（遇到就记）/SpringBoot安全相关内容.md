## 关键词 : Security 、JWT、SecurityConfig、Filter链

* 一、整体架构目标
    * 实现无状态的用户认证与授权
    * 基于JWT （JSON Web Token）传递身份和权限信息
    * 使用Spring Security 构建安全防护网络
* 二、核心组件与职责
    * JwtUtil--JWT工具类 
        作用生成、解析、验证JWT Token
    * JwtUtil 代码源码
    ```java
        // src/main/java/com/example/cahblogbackend/security/JwtUtil.java
        package com.example.cahblogbackend.security;

        import io.jsonwebtoken.Claims;
        import io.jsonwebtoken.Jwts;
        import io.jsonwebtoken.SignatureAlgorithm;
        import io.jsonwebtoken.io.Decoders;
        import io.jsonwebtoken.security.Keys;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.stereotype.Component;

        import javax.crypto.SecretKey;
        import java.security.Key;
        import java.util.Date;
        import java.util.function.Function;
        //这个 JwtUtil 类是用于 处理 JWT（JSON Web Token） 的工具类，主要功能包括：
        //
        //        生成 JWT Token
        //        解析 JWT Token 中的信息（如用户名、过期时间等）
        //        验证 Token 是否有效（是否属于指定用户且未过期）
        @Component
        public class JwtUtil {

            @Value("${jwt.secret}")
            private String jwtSecret;

            @Value("${jwt.expiration}")
            private Long jwtExpiration;

            private Key getSigningKey() {
                byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
                return Keys.hmacShaKeyFor(keyBytes);
            }

            public String generateToken(String username) {
                return Jwts.builder()
                        .setSubject(username)//这里往Jwt里面放了username，其他程序可以通过token获取到username
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))//这里设置了过期时间
                        .signWith(getSigningKey(), SignatureAlgorithm.HS256)//这里设置加密算法和密文（jwtSecret是配置文件配置的）
                        .compact();//打包
            }

            public String extractUsername(String token) {
                return extractClaim(token, Claims::getSubject);
            }

            public String extractEmail(String token) {
                return extractClaim(token, Claims::getSubject);
            }

            public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
                final Claims claims = extractAllClaims(token);
                return claimsResolver.apply(claims);
            }

            private Claims extractAllClaims(String token) {
                return Jwts.parser()
                        .verifyWith((SecretKey) getSigningKey()) // 注意：这里用 verifyWith
                        .build()
                        .parseSignedClaims(token)                // 注意：这里用 parseSignedClaims
                        .getPayload();                           // 注意：新版叫 getPayload() 而不是 getBody()
            }
            //这里验证token是否有效的方法是通过传入的usename和token里面解码得到的name是否相同
            public boolean isTokenValid(String token, String username) {
                final String extractedUsername = extractUsername(token);
                return (extractedUsername.equals(username)) && !isTokenExpired(token);
            }

            private boolean isTokenExpired(String token) {
                return extractExpiration(token).before(new Date());
            }

            private Date extractExpiration(String token) {
                return extractClaim(token, Claims::getExpiration);
            }
        }
    ```

    * JwtAuthenticationFilter —— JWT 认证过滤器
    作用：拦截请求，自动完成用户认证。
        执行流程：
        从 Authorization: Bearer <token> 头中提取 Token；
        调用 JwtUtil 解析并验证 Token；
        若有效，通过 UserDetailsService 加载用户详情；
        构造 UsernamePasswordAuthenticationToken；
        设置到 SecurityContextHolder；
        调用 filterChain.doFilter() 放行请求。
        关键依赖：
        JwtUtil：验证 Token；
        UserDetailsService：加载用户权限（如角色、权限列表）；
        OncePerRequestFilter：确保每个请求只处理一次。
    * JwtAuthenticationFilter 代买
    ```java
            @Component
            public class JwtAuthenticationFilter extends OncePerRequestFilter {
            @Autowired
            private JwtUtil jwtUtil;
            @Autowired
            private CustomUserDetailsService userDetailsService;

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException,IOException{
                String authHeader = request.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")){
                    String token = authHeader.substring(7); // 去掉 "Bearer "

                    try {
                        String name = jwtUtil.extractEmail(token); // 你已有的方法

                        if (name != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(name);

                            if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {
                                UsernamePasswordAuthenticationToken authToken =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities()
                                        );
                                SecurityContextHolder.getContext().setAuthentication(authToken);
                            }
                        }
                    } catch (Exception e) {
                        // Token 无效或过期，不设置认证，后续由 Security 拦截
                        logger.warn("JWT 验证失败: " + e.getMessage());
                    }
                }
                filterChain.doFilter(request, response);//这里会去执行其他配置的JWT认证器，如果有配置的话
            }
        }
    ```


* SecurityConfig 类 前面提到的 JWT认证器类，必须要在SecurityConfig类里面注册才会被使用
```java
        package com.example.cahblogbackend.config;

        import com.example.cahblogbackend.security.JwtAuthenticationFilter;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
        import org.springframework.security.config.http.SessionCreationPolicy;
        import org.springframework.security.web.SecurityFilterChain;
        import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
        @Configuration
        @EnableWebSecurity
        public class SecurityConfig {
        @Autowired
        private JwtAuthenticationFilter jwtAuthFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable()) // JWT 无状态，禁用 CSRF
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authz -> authz
                            .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()//这里配置了哪些请求会通过这个过滤器
                            .requestMatchers("/api/posts/**").permitAll() // 公开文章列表/详情
                            .anyRequest().authenticated() // 其他所有请求必须登录
                    )
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);//这里添加了前面我们写的过滤器

            return http.build();
        }
    }

```


## 整体执行过程 场景说明
用户已登录，持有有效 JWT Token。
客户端发起请求：
``html
    GET /api/profile
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xxxxx
``
目标：服务器识别用户身份（Authentication），并允许请求到达 Controller。（这里不涉及权限判断）
涉及的核心类及其职责

类	                  职责	                               依赖
`SecurityConfig`	配置 `Spring Security`，注册过滤器链	依赖 `JwtAuthenticationFilter`
`JwtAuthenticationFilter`	拦截请求，解析 JWT，完成认证	依赖 `JwtUtil`、`UserDetailsService`
`JwtUtil`	生成/解析/验证 JWT Token	无外部依赖（仅配置）
`CustomUserDetailsService`	根据用户名（或邮箱）加载用户详情	依赖 `UserMapper`（MyBatis）
`UserMapper`	数据库操作（查用户）	依赖数据库连接

* 步骤 1️⃣：请求进入 Spring Security 过滤器链
客户端发送 GET `/api/profile` + `Authorization: Bearer <token>`
Spring Security 的过滤器链开始执行
* 步骤 2️⃣：JwtAuthenticationFilter 被触发（因已注册）
因在 SecurityConfig 中调用了
```java
    http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```
所以该 Filter 在链中被调用。 （依赖注入部分 依赖注入：Spring 自动将 JwtUtil 和 CustomUserDetailsService 注入此 Filter）

* 步骤 3️⃣：JwtAuthenticationFilter.doFilterInternal() 执行
    * 3.1 从请求头提取 Token
    ```java 
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
    ```
    * 3.2 调用 JwtUtil.extractEmail(token)
        委托给 JwtUtil 解析 Token 的 subject 字段（此处为邮箱）。
        JwtUtil 使用密钥验签 + 解码 → 返回 user@example.com
        🔁 依赖：JwtAuthenticationFilter → JwtUtil
    * 3.3 检查 SecurityContext 是否已认证
        ```java
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null)
        ```
    初次请求，未认证 → 继续。
    * 3.4 调用 UserDetailsService.loadUserByUsername(email)
        委托给 CustomUserDetailsService 加载用户。
        该服务内部调用 UserMapper.selectByEmail(email) 查询数据库。
        返回 UserDetails 对象（含用户名、密码（可忽略）、权限等）。
    * 3.5 调用 JwtUtil.isTokenValid(token, email)
        再次验证 Token 是否属于该用户且未过期。
        （防止 Token 被盗用后修改 subject）
    * 3.6 构建认证对象并存入上下文
        ```java
            UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        ```
    ✅ 此时，当前线程的 SecurityContext 已包含认证信息。

* 步骤 4️⃣：放行请求到后续过滤器和 Controller
    请求继续经过其他 Security Filters（如 CSRF、Session 管理等，但 STATELESS 下基本跳过）；
    最终到达你的 @RestController：

🔄 整体依赖与数据流图（文字版）

```textare
    客户端
    ↓ (HTTP + Bearer Token)
    Spring Security Filter Chain
    ↓
    JwtAuthenticationFilter
    ├──→ JwtUtil.extractEmail(token) → 验证签名，返回 email
    └──→ CustomUserDetailsService.loadUserByUsername(email)
                ↓
            UserMapper (MyBatis)
                ↓
            Database (MySQL)
    ←── 返回 UserDetails
    ├──→ JwtUtil.isTokenValid(...) → 二次校验
    └──→ 构造 Authentication → 存入 SecurityContextHolder
    ↓
    filterChain.doFilter()
    ↓
    Controller (可安全获取当前用户)
```

一个携带 JWT 的请求，通过 自定义过滤器 触发 Token 解析 → 用户加载 → 认证上下文建立 的链路，最终让整个请求生命周期都能“知道当前是谁”，而这一切发生在到达业务代码之前，且完全无状态。