## 前调用后端服务
* 核心问题，跨域问题 ，可以通过配置的方式，在后端设置运行请求服务的端口号，在前端设置请求转发的，路径和转发的目标，核心目的是把请求发送给后端。

* 前端配置：在vite.config.ts 配置文件中配置
```typeScript
    import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '') // 如果后端不需要 /api 前缀
      }
    }
  }
  ,
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})

```
这里我们将所有 以/api开头的请求都转发给了http://localhost:8081这里，比如我们有个请求  /api/service/s 等效于访问 http://localhost:8081/service/s （这里把api给去除掉了）

* 后端配置接收服务
    * 方法一： 在对应的控制类中添加注解，允许指定端口的请求调用`@CrossOrigin(origins = "http://localhost:5173")` // ← 允许这个前端地址请求服务
    * 方法二: config类实现  `WebMvcConfigurer` 实现管控 
    示范代码:
    ```java
    @Configuration
    public class CorsConfig implements WebMvcConfigurer {

        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**") // 拦截所有路径
                    .allowedOriginPatterns("*") // Spring Boot 2.4+ 推荐用 allowedOriginPatterns 代替 allowedOrigins
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true) // 是否允许携带 Cookie
                    .maxAge(3600); // 预检请求缓存时间（秒）
        }
    }
    ```
    * 方法三： 注册CorsConfigurationSource Bean，原理：这种方法会注册一个 `CorsFilter` 在请求进入 DispatcherServlet 之前就处理 CORS，优先级更高，适合需要在安全框架（如 Spring Security）之前处理跨域的场景。
    示范代码:
    ```java
    // config/CorsConfig.java
    package com.example.springbootproject.config;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
    import org.springframework.web.cors.CorsConfigurationSource;

    @Configuration
    public class CorsConfig {

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration config = new CorsConfiguration();
            config.addAllowedOrigin("http://localhost:5173"); // 允许你的前端地址
            config.addAllowedHeader("*");
            config.addAllowedMethod("*");
            config.setAllowCredentials(true); // 如果需要携带 cookie

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config); // 所有接口都允许跨域
            return source;
        }
    }
    ``` 
## 前端请求后端全过程

* 以vue3前端项目和SpringBoot后端以及Mysql数据库的项目为例
* 前端源码:
```vue
<template>
  <div style="padding: 20px; font-family: Arial">
    <h2>前后端联调测试</h2>

    <p v-if="loading">加载中...</p>
    <p v-else-if="message">✅ 后端返回: {{ message }}</p>
    <p v-else>❌ 未收到数据</p>

    <button @click="fetchData" :disabled="loading">点击请求后端</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const loading = ref(false)
const message = ref('')

const fetchData = async () => {
  loading.value = true
  message.value = ''
  try {
    // 注意：这里直接写后端完整 URL（开发阶段）
    const res = await axios.get('http://localhost:8081/api/users/get')
    message.value = res.data.message || JSON.stringify(res.data)
  } catch (error) {
    console.error('请求失败:', error)
    message.value = '请求失败: ' + (error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}
</script>
```

前端同axios异步请求服务

