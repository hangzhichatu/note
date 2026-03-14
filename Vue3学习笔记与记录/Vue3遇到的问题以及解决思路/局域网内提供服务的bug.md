## 问题描述
    前端页面以及打开，后端请求api服务  失效 Vite已经配置了 `host:'0.0.0.0'` 并且防火墙放行了端口5173  
## 原因 
    前端写请求后端服务的代码部分，后端的地址是硬编码的，BaseUrl 写死了是 'http://localhost：8082' 这导致了在局域网内其他机器上 访问的时候，后端的请求路径变成了本机自己，导致所有涉及到请求后端服务的部分失效。
## 解决 

* 前端改造
    改造旧方法，将所有硬编码方式的后端请求修改为  相对路径+Vite代理的方式实现
    request.ts需要修改axios request对象的定义 baseURL的路径修改为相对路径
    ```typeScript
    // utils/request.ts
        import axios from 'axios'

        const request = axios.create({
        baseURL: '/api', // ← 只写路径，不写域名/端口
        timeout: 10000,
        withCredentials: true
        })

        export default request
    ```

    其次Vite代理需要修改对应的配置文件vite.config.ts
    ```typeScript
    export default defineConfig({
    server: {
        host: '0.0.0.0',
        port: 5173,
        proxy: {
        '/api': {
            target: 'http://127.0.0.1:8082', // Vite 代理到本机后端
            changeOrigin: true,
            secure: false
        }
        }
    }
    })
    ```
* 后端改造
    配置全局core方法，方向指定网段下面的访问。