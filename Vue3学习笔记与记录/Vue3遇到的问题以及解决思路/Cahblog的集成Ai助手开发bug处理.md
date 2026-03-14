# CahBlog集成本地Ai模型开发bug汇总

## 1 前端请求结构嵌套错误
    * 现象:
        后端收到的原始JSON如下
        ```Json
        {
            "prompt": {
                "messages": [...],
                "model": "qwen3:4b"
            }
        }
        ```
        但是后端的DTO类期望的收到的对象是平铺的（也就是没有promot那一个层级）
    * 原因:
        错误的核心原因是前端发送请求的时候，错误的多包了一层 promot
        ```javaScript
        // api/Ai.ts
        export const fetchAiChatResponse = (prompt: AichatContents) => {
        return request.post('/api/ai/chat', { prompt }) // ❌ 多包了一层 { prompt: ... }
        }
        ```
    * 修复方案：修改错误的前端请求方法
        ```javaScript
            export const fetchAiChatResponse = (prompt: AichatContents) => {
            return request.post('/api/ai/chat', prompt) // ✅ 正确：直接传 payload
        }
        ```
## 2 :后端DTO使用List<Map<String,String>> 有潜在风险
    * 风险 private List<Map<String, String>> messages; // 反序列化不可靠
        Jackson 在泛型擦除下可能无法正确反序列化
        无编译时类型安全，易出错
    * 处理方式，使用专门的POJO类
    ```java
    // Message.java
    @Data
    public class Message {
        private String role;
        private String content;
    }

    // ChatRequest.java
    @Data
    public class ChatRequest {
        private List<Message> messages; // ✅ 类型安全、可靠
        private String model = "qwen3:4b";
    }
    ```
## 3 前端Axios请求超时（由于本地ai模型的速率限制导致的）
    * 原因axios设置了较短的全局超时 （10s）
    * 修复方案，针对那些操作较慢的接口，单独设置更长的超时时间
    ```typeScript
    export const fetchAiChatResponse = (prompt: AichatContents): Promise<string> => {
    return request.post('/api/ai/chat', prompt, {
        timeout: 120000 // ⏱️ 单独为 AI 接口设置 120 秒超时
    }).then(res => res.data)
    }
    ```
