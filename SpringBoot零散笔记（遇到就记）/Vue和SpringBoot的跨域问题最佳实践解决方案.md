## 跨域问题的后端解决方案

* 方案1 通过给控制类增加注解 `@CrossOring` 来给指定的IP及端口号的服务增加通过的许可
    * 方案1的优缺点：
    方便测试，在测试类上加上该注解能够很快测试，并且由于和业务代码写的很近也很容易就能够判断是什么地方发生问题，缺点就是这种方式不够优雅，遇到多环境配置的时候硬编码
    太难看了
    
* 方案2 全局CORS配置 
    代码实现1：通过实现WebMvcConfigurer接口重写addCorsMappings 方法即可 ，具体实现代码如下：
    ```java
        @Configuration
        public class CorsConfig implements WebMvcConfigurer {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 应用到所有接口
                        .allowedOriginPatterns("http://localhost:*", "https://yourdomain.com") // 支持通配符
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true) // 如果需要携带 cookie
                        .maxAge(3600); // 预检请求缓存时间
            }
        }
    ```

    代码实现2: 如果有security配置类的话也可以在该类里面增加，这种方法参考代码如下:
    ```java
        package com.example.cahblogbackend.config;

        import com.example.cahblogbackend.security.JwtAuthenticationFilter;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.http.HttpMethod;
        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
        import org.springframework.security.config.http.SessionCreationPolicy;
        import org.springframework.security.web.SecurityFilterChain;
        import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
        import org.springframework.web.cors.CorsConfiguration;
        import org.springframework.web.cors.CorsConfigurationSource;
        import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

        import java.util.Arrays;
        import java.util.List;

        @Configuration
        @EnableWebSecurity
        public class SecurityConfig {

            @Autowired
            private JwtAuthenticationFilter jwtAuthFilter;

            @Bean
            public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 👈 启用 CORS
                    .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/user/me").authenticated()
                        .anyRequest().authenticated()
                    )
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
            }

            // 👇 定义 CORS 配置源（可集中管理）
            @Bean
            public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // 允许的来源（开发阶段可宽松，生产环境应严格限制）
                configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true); // 如果前端需要携带 cookie（如 JWT 放在 cookie）

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
            }
        }
    ```
    ps：该方法里面的`CorsConfigurationSource` 方法里面的设置允许的访问路径可以通过改写为从配置文件获取，这样就能实现多环境部署了这里也同样提供一个配置类，通过调用该
    配置的方法，能够获取允许访问的路径

    ```java
        package com.example.cahblogbackend.configuration;
        import org.springframework.boot.context.properties.ConfigurationProperties;
        import org.springframework.stereotype.Component;
        import java.util.List;

        /**
        * 该类用于获取允许的前端的跨域访问的ip的配置信息
        */
        @Component
        @ConfigurationProperties(prefix = "app.cors")
        public class CorsProperties {
            private List<String> allowedOriginPatterns;

            // 必须有 getter/setter（或用 Lombok @Data）
            public List<String> getAllowedOriginPatterns() {
                return allowedOriginPatterns;
            }

            public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
                this.allowedOriginPatterns = allowedOriginPatterns;
            }

        }
    ```

    配置内容写入到yaml文件中即可
    ```yaml
        app:
            cors:
                allowed-origin-patterns:
                - "http://localhost:*"
                - "http://127.0.0.1:*"
    ```


    这样就能优雅的解决跨域问题了