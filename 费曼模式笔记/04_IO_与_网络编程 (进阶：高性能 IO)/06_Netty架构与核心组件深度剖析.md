# 06 Netty 架构与核心组件深度剖析

标签: #Netty #NIO #网络编程 #Reactor #IO多路复用
最后复习:
掌握程度: ⭐⭐⭐⭐⭐ (需定期回顾 EventLoop 线程模型与零拷贝细节)

> 本文承接 `Channel、Buffer、Selector组件交互.md`，把 NIO 那套"水管/水桶/大堂经理"升级成 Netty 的"饭店/流水线"思维。学了这篇，你看 Netty 源码才不会发怵。
> 依赖前置：先搞懂 NIO（Channel / Buffer / Selector），否则下面很多"Netty 帮你做了啥"看不懂。

---

## 0. 🎯 一句话本质

**Netty 是对 JDK NIO 的封装 + 思想的升华**：它把 NIO 里那个需要反复写的笨重循环（`while(selector.select())` + `if(key.isXxx())`）收敛成两件事——

1. **EventLoop 线程模型**（Reactor 模式的落地）：一个线程 + 一个 Selector + 一个永远在跑的循环，专心伺候任务队列里的 IO 事件；
2. **Pipeline 责任链**：把"收到数据 → 拆包 → 转对象 → 走业务 → 编码 → 写回"这条流水线的每一环，拆成独立的 Handler 插件，可增删、可复用、可热插拔。

一句话总结：**Netty 用"线程 + 流水线"这两个抽象，把 NIO 的手动档换成了自动档，同时把线程安全、粘包拆包、缓冲扩容这些坑替你填平了。**

---

## 1. 💥 灾难现场 (The Disaster Scenario)

### 场景 A：手写 NIO 循环的三大坑（Netty 解决的痛点）

**错误/痛苦的写法**——你刚在上一篇笔记里用 NIO 写了个服务器，很快会发现这些崩溃现场：

```java
// 坑 1：自己维护每个连接的 ByteBuffer 容量
ByteBuffer buffer = ByteBuffer.allocate(1024);
channel.read(buffer);
// 客户端一次发了 5KB -> 数据被截断，剩下的丢了！
// 客户端发了 500 字节 -> 你又得判断这次读到的是不是完整一条消息

// 坑 2：粘包/拆包全靠自己拼
// 客户端连续发了 "hello" + "world"（网络层可能合并成一次到达）
// 你 read 到的可能是 "helloworld"，得自己想办法按分隔符切开
// 分片：一条消息 "hello world" 可能被拆成 "hel" + "lo wor" + "ld" 三次到达，你得自己攒够再处理

// 坑 3：线程安全地狱
// 你在 NIO 主循环里处理业务；但业务线程想主动 push 数据给某个连接
// 你得自己维护 Map<Channel, ByteBuffer> + 锁，还要防止 Buffer 并发读写
// 稍不留神就是数据错乱或 OOM
```

**Netty 对应的救法（下面代码案例会展开）：**

- 坑 1 → `ByteBuf` 自动扩容，`readIndex/writerIndex` 双指针，不用自己算容量；
- 坑 2 → 一行 `LineBasedFrameDecoder(1024)` 或 `LengthFieldBasedFrameDecoder` 搞定粘包拆包；
- 坑 3 → `writeAndFlush` 线程安全，Netty 内部帮你把跨线程的写操作封装成任务投递到对应 EventLoop。

### 场景 B：背压/内存失控（Netty 帮你挡住的灾难）

**问题**：客户端疯狂发包，你的服务器 read 得超快，但业务处理（比如写数据库）很慢。数据全堆在内存里 → OOM。
**NIO 里**：你得自己实现"读到的先入队列、队列满就暂停读"的流控。
**Netty 里**：`Channel` 有 `isWritable()` + `ChannelOption.WRITE_BUFFER_WATER_MARK`（高水位/低水位），配合 `channelWritabilityChanged()`，自动帮你做背压。后面有代码。

---

## 2. 🧠 费曼复述 (The Feynman Test)

👶 **给实习生的比喻：Netty 是一家五星级大饭店**

- **`EventLoopGroup` = 饭店的两个班组**

  - **bossGroup（迎宾队）**：岗位只有一个——站在大门口，只要有客人来（`accept` 新连接），就喊"这边请"，把客人领进来。迎宾自己不去端菜（不处理业务 IO）。通常就 1 个线程就够。
  - **workerGroup（服务生班）**：专门伺候每位入座的客人。每位服务生（EventLoop）脚下踩着一个踏板（Selector），耳朵一直听着"哪位客人有声音了？（有数据可读）"，一旦听到就立刻过去端盘子（读数据）、上菜（写数据）。通常默认 CPU 核数 × 2 人。
- **`Channel` = 每位客人的专属餐桌**

  - 一张桌子对应一个连接。服务生只认自己的桌子。
- **`handler（ChannelPipeline）` = 后厨流水线 + 传菜口**

  - `pipeline.addLast(...)` 就像在流水线上装传送带，一盘菜要按顺序过几个关口：
    - **`FrameDecoder`（拆包）**：验菜——把混在一起(粘包)的菜按盘子分好。
    - **`StringDecoder/Encoder`（解码/编码）**：把 byte 变成你认识的"菜名"（String/对象），出餐前再变回 byte。
    - **`SimpleChannelInboundHandler`（业务处理）**：真正的大厨——你只在这里写"这道菜怎么炒"（业务逻辑）。
  - 整条流水线是**双向**的：入站的菜从 `tail` 端传到你，出站的菜从你传到 `head` 端发出去。
- **`ByteBuf` = 流水线上的托盘**

  - 盘子会自动变大（自动扩容），还自带"吃到哪了"（readerIndex）和"放到哪了"（writerIndex）两个刻度，不用你手动 `flip()`。

⚖️ **一张图记住 Netty 线程模型（Reactor 主从多线程模型）**

```
   bossGroup (1线程, 只 accept)
        │  accept 新连接
        ▼
   workerGroup (N线程, 每个 EventLoop 一个 Selector + 一个任务队列)
   ├── EventLoop-1 → selector → 负责 连接A、连接B 的所有IO
   ├── EventLoop-2 → selector → 负责 连接C
   └── EventLoop-N → selector → 负责 ...
   （一个连接的一生只由一个 EventLoop 管，天然线程安全，无需加锁）
```

> 这是**主从 Reactor 多线程模型**：mainReactor(boss) 只管接收连接并分发给 subReactor(worker)，subReactor 管连接的所有读写。面试必问！Netty 默认就是这个模型。

---

## 3. 🧩 核心组件逐个拆解（带代码）

### 3.1 EventLoopGroup / EventLoop —— 线程模型的载体

```java
// boss 组：负责 accept 新连接，线程数 1 就够了
EventLoopGroup bossGroup = new NioEventLoopGroup(1);
// worker 组：负责该连接的所有读写 IO，默认线程数 = CPU核数 × 2（可自定义）
EventLoopGroup workerGroup = new NioEventLoopGroup(2); // 这里显式给 2 个演示
```

**要点（面试）：**

- 一个 `EventLoop` 内部 = 一个线程 + 一个 `Selector` + 一个 `TaskQueue`（任务队列）。
- EventLoop 是一个**串行化执行器**：所有注册到它上面的 Channel 的 IO 事件，都在**同一个线程**里按顺序执行 → **不需要加锁**，天然线程安全。
- 关键 API：`eventLoop.execute(runnable)` 可以在别的线程把自己的任务丢给该连接所在的 EventLoop 执行（这也是跨线程 push 数据安全的原因）。

### 3.2 ServerBootstrap / Bootstrap —— 启动器

```java
ServerBootstrap bootstrap = new ServerBootstrap();
bootstrap.group(bossGroup, workerGroup)          // 绑定两班组
         .channel(NioServerSocketChannel.class) // 指定用 NIO 通道（还有 Epoll/KQueue 可选）
         .option(ChannelOption.SO_BACKLOG, 128)          // 服务端：连接队列最大长度
         .childOption(ChannelOption.SO_KEEPALIVE, true)  // 客户端连接：TCP keepalive
         .childOption(ChannelOption.TCP_NODELAY, true)   // 关闭 Nagle 算法，降低延迟（交互型必开）
         .childHandler(new ChannelInitializer<SocketChannel>() { // 每个新连接的初始化器
             @Override
             protected void initChannel(SocketChannel ch) {
                 ch.pipeline().addLast(new MyServerHandler()); // 给每个连接装流水线
             }
         });

// 阻塞直到绑定成功，拿到 ChannelFuture
ChannelFuture f = bootstrap.bind(8080).sync();
f.channel().closeFuture().sync(); // 阻塞等待服务端关闭
```

**区别：**

- `ServerBootstrap`：服务端用，多一个 `bossGroup` 专门 accept，有 `.child...` 系列（作用于每个子连接）。
- `Bootstrap`：客户端用，单 group，没有 `.child...`。

### 3.3 ChannelPipeline / ChannelHandler / ChannelHandlerContext —— 责任链（灵魂）

```java
public class ServerPipelineInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        // 入站方向（数据进来）：从头(head)往尾(tail)传 —— addLast 追加到尾部
        p.addLast(new LengthFieldBasedFrameDecoder(
                1024,          // 帧最大长度
                0, 4,          // 长度字段从第0字节起，占4字节
                0, 4));        // 长度值=长度字段+4(补上4字节长度本身)  —— 通用TLV拆包
        p.addLast(new StringDecoder(CharsetUtil.UTF_8)); // byte[] -> String
        p.addLast(new ServerHandler());                  // 业务处理（入站）
        // 出站方向（数据出去）：从尾(tail)往头(head)传
        p.addLast(new StringEncoder(CharsetUtil.UTF_8)); // String -> byte[]
    }
}
```

**要点（面试）：**

- `ChannelPipeline` 是**双向链表**：入站事件按 `head → ... → tail` 顺序传播；出站事件按 `tail → ... → head` 顺序传播（想象出站是从你手里往外传）。
- `ChannelHandler` 三种典型：
  - `ChannelInboundHandler`：入站，重写 `channelRead` / `channelActive` / `exceptionCaught`；
  - `ChannelOutboundHandler`：出站，重写 `write`；
  - `ChannelDuplexHandler`：双向。
- `SimpleChannelInboundHandler<T>`：自动帮你 `release()` 引用计数，**消息读完自动回收内存**，推荐业务用这个。
- `ChannelHandlerContext`：就是一个"指针"，指向流水线上的当前位置。通过它 `ctx.writeAndFlush(...)`、`ctx.fireChannelRead(...)`（把事件传给下一个 Handler）、`ctx.pipeline()` 等。它能绕过 `tail` 直接从指定节点往下传。

### 3.4 ByteBuf —— 比 NIO ByteBuffer 好用一万倍的缓冲

```java
// 池化 + 自动扩容 + 双指针
ByteBuf buf = ctx.alloc().buffer();       // 用 Channel 分配器，走池化(性能更高)
buf.writeInt(123);        // 写 int；writerIndex 自动 +4，自动扩容
buf.writeBytes("abc".getBytes()); // 再写 3 字节
int len = buf.readableBytes();  // = 7（writerIndex - readerIndex）
int first = buf.readInt();      // 读 4 字节，readerIndex +4
byte[] rest = new byte[3];
buf.readBytes(rest);            // 读完，readerIndex == writerIndex

// 常用 API
buf.readByte() / buf.readBoolean() / buf.readUTF();  // 读
buf.writeByte() / buf.writeBoolean() / buf.writeCharSequence(); // 写
buf.isReadable() / buf.isWritable();  // 判断
buf.retain() / buf.release();         // 引用计数，防止内存泄漏
```

**对比（面试加分）：**

| 维度   | NIO`ByteBuffer`                           | Netty`ByteBuf`                                                                                 |
| ------ | ------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| 容量   | 固定，超了要`allocate` 新的再 copy        | **自动扩容**                                                                               |
| 指针   | position/limit + 手动`flip()`/`clear()` | **readIndex/writerIndex** 双指针，读写互不干扰                                             |
| 零拷贝 | 依赖`transferTo`                          | **`CompositeByteBuf`**（逻辑拼接不物理拷贝）、**`wrap`/`slice`**（视图不拷贝） |
| 内存   | 堆内存 / 堆外                               | 堆 + 堆外 +**池化复用**（`PooledByteBufAllocator` 默认）                                 |
| 泄漏   | 无概念                                      | **引用计数 + `LeakDetector` 泄漏检测**                                                   |

**池化 vs 非池化**：生产默认用池化（`PooledByteBufAllocator`），申请/释放走内存池，大幅减少 GC 压力。阿里规范里也建议 Netty 用池化。

---

## 4. 🔌 完整代码案例（可运行，覆盖服务端/客户端/编解码/粘包拆包）

> 依赖：`io.netty:netty-all`。下面案例实现一个"按 4 字节长度前缀 + UTF-8 字符串"的协议（通用 TLV），解决粘包拆包，并演示背压水印。

### 4.1 自定义协议编解码器（粘包拆包 + 对象转换）

```java
// ========= 协议：4字节长度(length) + 载荷(payload) =========
public class Msg {
    private int type;
    private String content;
    // getter/setter/constructor 省略
}

// —— 编码器（出站）：Msg -> ByteBuf ——
public class MsgEncoder extends MessageToByteEncoder<Msg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Msg msg, ByteBuf out) {
        byte[] body = msg.getContent().getBytes(CharsetUtil.UTF_8);
        // 长度字段 = 4(int type) + 4(payload长度) + payload
        out.writeInt(4 + body.length);
        out.writeInt(msg.getType());
        out.writeBytes(body);
    }
}

// —— 解码器（入站）：ByteBuf -> Msg，内部处理粘包/拆包 ——
public class MsgDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 拆包：先攒够 4 字节读长度
        if (in.readableBytes() < 4) return;          // 数据不够，等下一批
        in.markReaderIndex();                        // 先标记（因为可能读一半发现不够要回退）
        int total = in.readInt();                    // 读总长度
        if (in.readableBytes() < total) {            // 粘包/半包：有效数据还没到齐
            in.resetReaderIndex();                   // 回退到标记，等下一批数据
            return;
        }
        // 此时够一条完整消息了
        Msg msg = new Msg();
        msg.setType(in.readInt());
        byte[] body = new byte[total - 4];
        in.readBytes(body);
        msg.setContent(new String(body, CharsetUtil.UTF_8));
        out.add(msg);                                // 交给下一个入站 Handler
    }
}
```

> 费曼理解：解码器就是"拆包裹员"。先看一眼运单（4字节长度），如果信封里还没凑够那个长度（半包），就把针脚留个记号（markReaderIndex）等下一批到了再拆；一次来了好几封信（粘包），就 `for` 循环一封信一封信拆给上下游。

### 4.2 服务端 + 业务 Handler（含背压水印）

```java
public class NettyServer {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_BACKLOG, 1024)
             .childOption(ChannelOption.SO_KEEPALIVE, true)
             .childOption(ChannelOption.TCP_NODELAY, true)
             .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                          new WriteBufferWaterMark(8 * 1024, 32 * 1024)); // 低水位8K/高水位32K
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline()
                       .addLast(new MsgDecoder())   // 拆包 -> Msg
                       .addLast(new MsgEncoder())   // Msg -> ByteBuf（出站）
                       .addLast(new BusinessHandler()); // 业务
                 }
             });

            ChannelFuture f = b.bind(8080).sync();
            System.out.println("Server started on 8080");
            f.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}

// 业务 Handler：用 SimpleChannelInboundHandler 自动释放内存
class BusinessHandler extends SimpleChannelInboundHandler<Msg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) {
        System.out.println("收到 type=" + msg.getType() + ", content=" + msg.getContent());

        // 跨线程安全地给客户端回消息（这里就是当前线程，直接 writeAndFlush）
        Msg resp = new Msg(1, "已收到：" + msg.getContent());
        ctx.writeAndFlush(resp);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        // 背压回调：当写出缓冲区在高/低水位之间变化时触发
        if (ctx.channel().isWritable()) {
            System.out.println("连接恢复可写，可继续发送");
        } else {
            System.out.println("连接不可写！暂停发送，防止内存溢出");
            // 实际开发：停掉生产者，或改用服务端 push 队列限流
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```

### 4.3 客户端（连接 + 发送 + 收回复）

```java
public class NettyClient {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline()
                       .addLast(new MsgDecoder())
                       .addLast(new MsgEncoder())
                       .addLast(new SimpleChannelInboundHandler<Msg>() {
                           @Override
                           public void channelActive(ChannelHandlerContext ctx) {
                               // 连上就发 3 条消息（故意连续发，演示服务端拆包）
                               for (int i = 0; i < 3; i++) {
                                   ctx.writeAndFlush(new Msg(0, "hello-" + i));
                               }
                           }
                           @Override
                           protected void channelRead0(ChannelHandlerContext ctx, Msg msg) {
                               System.out.println("收到服务端: type=" + msg.getType()
                                       + ", content=" + msg.getContent());
                           }
                       });
                 }
             });

            ChannelFuture f = b.connect("127.0.0.1", 8080).sync();
            f.channel().closeFuture().sync(); // 这里会一直阻塞(演示用)
        } finally {
            group.shutdownGracefully();
        }
    }
}
```

### 4.4 常用内置解码器速查（粘包问题一键解决）

```java
// 按换行符 \n 拆包（常用于文本协议，如 Redis、Memcached）
p.addLast(new LineBasedFrameDecoder(1024));

// 按 4 字节长度字段拆包（二分法详解）
// 参数1=maxFrameLength 参数2=lengthFieldOffset 参数3=lengthFieldLength
// 参数4=lengthAdjustment(长度字段后还需跳过的字节) 参数5=initialBytesToStrip(拆包时剥离的前缀字节)
p.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));

// 分隔符拆包（自定义分隔符，如 ";;"）
p.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(";;".getBytes())));

// 定长拆包（每 100 字节一条）
p.addLast(new FixedLengthFrameDecoder(100));
```

---

## 5. ⚡ 性能与边界 (Performance & Boundaries)

### ✅ 高性能的四大底座（面试常考）

1. **IO 多路复用**：底层默认用 `epoll`（Linux），一个线程监听海量连接的读写就绪事件，而非 B/N 个阻塞线程。
2. **主从 Reactor 线程模型**：boss 只接客分发给 worker，worker 一个线程串行服务多个连接，无锁线程安全。
3. **零拷贝技术**（Netty 的零拷贝是"用户态"层面的，注意区分）：
   - `CompositeByteBuf`：多个 ByteBuf 逻辑拼接，不物理复制；
   - `slice`/`duplicate`/`wrap`：返回视图/包装，共享底层数组不复制；
   - 配合操作系统 `sendfile`/`FileRegion` 可实现网络文件传输零拷贝。
4. **池化内存 + 引用计数**：`PooledByteBufAllocator` 复用内存对象，`retain()/release()` + `LeakDetector` 防泄漏。

### 🚫 常见坑 / 边界

- **不要在 `channelRead0` 里做耗时操作**（比如远程调用、大批量 DB 写）——会阻塞该 EventLoop 上的所有连接！
  对策：`ctx.executor().execute()`? 不，应该**把耗时任务提交到独立的业务线程池**，处理完再 `channel.writeAndFlush` 回写（Netty 保证 write 线程安全）。
- **`ByteBuf` 用完要 `release()`**，否则内存泄漏。用 `SimpleChannelInboundHandler` 可自动释放；自己继承 `ChannelInboundHandler` 时要手动 `ReferenceCountUtil.release(msg)`。
- **`write` 但不 `flush`**：`write` 只是写进缓冲区，`writeAndFlush` 才真正发出去。大批量写可用 `write` 攒批 + 最后 `flush`。
- **客户端不要忘 `.childOption` 和 connect 后 `.sync()`**，否则可能还没建立就操作。
- **Handler 是"有状态"还是"无状态"**：业务 Handler 若带共享字段，注意同一实例会被多条连接复用（`@Sharable` 注解才允许多连接共享实例，否则每个连接 new 一个）。

### 📉 性能对照直觉

- 连接数 < 1000，BIO + 线程池可能够用；**海量长连接（万级以上）**，Netty 才是正确选择。
- Netty 号称"百万并发"的核心支撑：epoll + 主从 Reactor（单线程+IO多路复用）+ 无锁串行化 + 池化零拷贝 GC 友好。

---

## 6. 🆚 进阶：Netty 各 IO 模型(JNI)对比

| 选项                          | 平台      | 说明                                                                    |
| ----------------------------- | --------- | ----------------------------------------------------------------------- |
| `NioServerSocketChannel`    | 跨平台    | 基于 JDK NIO，最通用，默认                                              |
| `EpollServerSocketChannel`  | Linux     | 原生 epoll，性能更高，需加`epoll` 依赖并 `Epoll.isAvailable()` 判断 |
| `KQueueServerSocketChannel` | macOS/BSD | 原生 kqueue                                                             |
| `OioServerSocketChannel`    | 跨平台    | 阻塞 IO，**已过时**，仅作兼容                                     |

```java
// Linux 生产常用：优先用原生 epoll（需引入 netty-transport-native-epoll）
if (Epoll.isAvailable()) {
    b.channel(EpollServerSocketChannel.class);
    // worker 用 EpollEventLoopGroup
} else {
    b.channel(NioServerSocketChannel.class);
}
```

---

## 7. 🔁 Reactor 模型三件套速记（面试串联）

| Reactor 变体                  | Netty 体现                                    | 一句话                                 |
| ----------------------------- | --------------------------------------------- | -------------------------------------- |
| 单 Reactor 单线程             | 单`EventLoop` + 一个线程处理 accept/io/业务 | 最简单，扛不住大并发，玩具级           |
| 单 Reactor 多线程             | boss 线程 accept + 业务丢线程池               | io 处理仍在单线程，有瓶颈              |
| **主从 Reactor 多线程** | `bossGroup` + `workerGroup`（Netty 默认） | ◀ 生产级：accept 与 io 分离，各司其职 |
| 多 Reactor 多线程             | 多 workerGroup 分工不同业务                   | 大型系统可扩展                         |

> 面试一串讲法：**Netty 用的是主从 Reactor 多线程模型（即 1 个 boss 线程组 accept + N 个 worker 线程组读写），本质是 IO 多路复用（epoll）+ 线程池复用 + 串行无锁 + 零拷贝内存池，所以能支撑高并发海量长连接。**

---

## 8. ❓ 自测灵魂拷问 (Self-Quiz)

(复习时遮住答案，口头回答)

Q1: 一个 EventLoop 为什么不需要加锁？它的线程模型是怎样的？
A: (关键点：一个 EventLoop = 一个线程 + 一个 Selector。它负责的所有 Channel 的 IO 事件都在同一个线程里按顺序执行，串行化天然避免并发竞争。跨线程 write 会被封装成 task 投递到该 EventLoop 的任务队列)

Q2: Netty 如何解决粘包/拆包问题？
A: (关键点：拆包——ByteToMessageDecoder/累积器，数据不够先攒着(markReaderIndex回退)；粘包——按分隔符/长度字段/定长把一条条完整消息解出来。常用 LengthFieldBasedFrameDecoder/LineBasedFrameDecoder/DelimiterBasedFrameDecoder)

Q3: 为什么 Netty 性能高？至少说出四点。
A: (关键点：①IO多路复用 epoll ②主从Reactor线程模型无锁串行 ③零拷贝 CompositeByteBuf/slice/sendfile ④池化内存+引用计数减少GC)

Q4: 业务处理很耗时（如写库、调第三方），应该怎么写？
A: (关键点：不能在 channelRead 里阻塞 EventLoop；把耗时任务提交到独立业务线程池，处理完再 writeAndFlush 回写——Netty 的 write 是线程安全的)

Q5: ByteBuf 和 NIO ByteBuffer 比，优势在哪？
A: (关键点：自动扩容、readIndex/writerIndex 双指针免 flip、组合/视图零拷贝、池化复用、引用计数防泄漏)

Q6: 什么是 Netty 的背压？如何实现？
A: (关键点：生产速度>消费速度时防止内存溢出；通过 WRITE_BUFFER_WATER_MARK 高低水位 + channelWritabilityChanged 回调暂停发送)

🔗 关联知识
[[Channel、Buffer、Selector组件交互]] (前置：NIO 三大组件)
[[IO流]] (BIO 与 NIO 的来龙去脉)
[[07... 高并发网络编程实战]] (Dubbo/gRPC 底层都依赖 Netty，可作为延伸实战)
