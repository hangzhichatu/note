## Buffer：不仅仅是个数组
在 BIO 里，我们读写数据通常是直接读到一个 byte[] 数组里。但在 NIO 里，数据必须经过 Buffer。  
* 费曼理解：如果说 Channel 是水管，那 Buffer 就是水桶。你不能直接喝管子里流出来的水，得先接到桶里  
* 核心属性（面试必问）  
      * `Buffer` 本质上就是一个数组，但它有三个“指针”属性来控制读写：  
      * `Capacity`（容量）： 桶有多大。一旦创建，不可改变。  
      * `Position`（位置）： 当前读写到了哪里。  
          * 写模式： 指向下一个要写入的位置（初始为 0）。  
          * 读模式： 指向下一个要读取的位置。  
      * `Limit`（界限）： 读写到哪里停止。  
          * 写模式： 通常等于 Capacity（最多写到桶满）。  
          * 读模式： 指向有效数据的末尾 + 1（即刚才写入的数据总量）。  
* 灵魂操作：`Flip`、`Clear`、`Compact`  
      * 写 -> 读：`flip()`  
          * 场景： 你往 Buffer 里写入了数据，现在想把它读出来（或者通过 Channel 发送出去）  
          * 动作： 把 Position 重置为 0，把 Limit 设为刚才 Position 的位置（即刚才写了多少，现在就读多少）。  
          * 口诀： “写完翻转，从头读起”。  
      * 读 -> 写：`clear()`    
          * 场景： 读完了，或者要把 Buffer 腾空，准备接下一波数据。  
          * 动作： `Position` 归 0，`Limit` 归 `Capacity`。  
          * 注意： `clear()` 不会清空内存里的数据（那是浪费性能），它只是把指针重置了。旧数据会被新数据覆盖  
      * 读 -> 写（保留未读）：`compact()`  
          * 场景： 读了一半，不想全清空，想把剩下的数据挪到前面，后面接着写。  
          * 动作： 把未读的数据移到数组头部。  
* 堆内 vs 堆外（`DirectBuffer`）
      * `HeapBuffer`： 在 JVM 堆内存里。GC 会管，但读写文件/网络时，需要从堆拷贝到内核空间（多一次拷贝）。  
      * `DirectBuffer`： 在操作系统本地内存（堆外）。GC 管不到（容易内存泄漏），但读写文件/网络时，可以直接传输（零拷贝的基础），性能更高。  

## Channel：双向的高速公路
在 BIO 里，流是单向的（InputStream 只能读，OutputStream 只能写）。
* 费曼理解： Channel 就是全双工的高速公路。数据既可以顺着跑进来，也可以顺着跑出去。  
* 核心特点
      * 双向性： 一个 Channel 既可以读也可以写。
      * 异步非阻塞： Channel 可以设置为非阻塞模式（configureBlocking(false)）。
      * 必须经过 Buffer： 你不能直接从 Channel 读数据到变量里，必须经过 Buffer。
* 常见类型
      * `FileChannel`： 操作本地文件。注意：`FileChannel` 只能工作在阻塞模式（不能注册到 `Selector`），但它可以利用 `transferTo` 实现零拷贝文件传输。  
      * `SocketChannel`： 客户端 TCP 连接。  
      * `ServerSocketChannel`： 服务端监听端口，负责 accept 新连接。  

## Selector：高并发的指挥官
* 费曼理解： Selector 就是银行大堂经理 它手里有个小本本（注册表），记录着每个连接的状态（感兴趣的事件）。
它不停地问操作系统：“有没有人准备好了？”（select() 方法）。
一旦有人说“我数据到了”或者“我连上了”，Selector 就拿到一张就绪名单（SelectionKey），然后安排人去处理。
* 四种事件（OP_XXX）Channel 注册到 Selector 时，需要告诉它你关心什么：
      * OP_ACCEPT： 有新连接进来了（服务端用）。
      * OP_CONNECT： 连接建立成功了（客户端用）。
      * OP_CONNECT： 连接建立成功了（客户端用）。
      * OP_WRITE： 可以写数据了（通常只有在发送缓冲区满时才需要关心这个）。
* 示例代码:
```java  
// 1. 打开选择器（大堂经理上岗）
Selector selector = Selector.open();

// 2. 打开通道（开门）
ServerSocketChannel serverChannel = ServerSocketChannel.open();
serverChannel.bind(new InetSocketAddress(8080));

// 3. 设置为非阻塞！这是必须的！
// 如果阻塞，selector.select() 就没意义了
serverChannel.configureBlocking(false);

// 4. 注册兴趣
// 告诉经理：“我关心有没有新客户（OP_ACCEPT）”
// 注册后会返回一个 SelectionKey，代表这个连接在经理小本本上的记录
SelectionKey acceptKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);

while (true) {
    // 5. 轮询就绪事件
    // 这行代码会阻塞，直到有注册的事件发生（有人连上了，或者有数据来了）
    // 底层调用的是操作系统的 epoll (Linux) 或 select/poll
    int readyChannels = selector.select(); 

    if (readyChannels == 0) continue;

    // 6. 获取就绪的 Key 集合
    Set<SelectionKey> selectedKeys = selector.selectedKeys();
    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

    while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();

        // 7. 处理事件
        if (key.isAcceptable()) {
            // 有新连接！
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            // 把这个新连接也注册到经理那里，这次关心“读事件”
            clientChannel.register(selector, SelectionKey.OP_READ);
        } else if (key.isReadable()) {
            // 有数据可读！
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int len = clientChannel.read(buffer);
            // ...处理数据...
        }

        // 8. 必须移除！
        // 因为 selectedKeys 不会自动清空，如果不手动移除，下次循环还会处理，导致逻辑错误
        keyIterator.remove();
    }
}
```

* Netty 版服务端代码
```java
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder; // 解决粘包
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class NettyServer {

    public static void main(String[] args) throws Exception {
        // 1. 创建两个线程组（对应 Reactor 模型）
        // bossGroup：只负责“接客”（接受连接），通常只需 1 个线程
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // workerGroup：负责“干活”（处理 IO 和业务），默认 CPU 核数 * 2
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 2. 辅助启动类（相当于之前的 ServerSocketChannel 配置）
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 指定使用 NIO 通道
                    // 3. 配置连接参数（可选）
                    .option(ChannelOption.SO_BACKLOG, 128) // 连接队列大小
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 保持长连接

                    // 4. 配置处理器（核心！）
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 这里的 ch.pipeline() 就是 Netty 的“流水线”
                            ChannelPipeline pipeline = ch.pipeline();

                            // --- 步骤 A：解决粘包/拆包（NIO 需要手写，Netty 直接加组件） ---
                            // 以换行符为分隔符，最大长度 1024
                            pipeline.addLast(new LineBasedFrameDecoder(1024));
                            
                            // --- 步骤 B：编解码（NIO 需要手动转 byte[] 和 String，Netty 自动转） ---
                            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8)); // 入站：bytes -> String
                            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8)); // 出站：String -> bytes

                            // --- 步骤 C：自定义业务逻辑（这是你唯一需要写的代码） ---
                            pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                    // 业务逻辑：收到消息，打印并回复
                                    System.out.println("服务端收到: " + msg);
                                    
                                    // 回复客户端
                                    ctx.writeAndFlush("服务端已收到：" + msg + "\n");
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    cause.printStackTrace();
                                    ctx.close(); // 发生异常，关闭连接
                                }
                            });
                        }
                    });

            // 5. 绑定端口并启动
            ChannelFuture future = bootstrap.bind(8080).sync();
            System.out.println("Netty 服务端启动成功...");

            // 6. 监听关闭
            future.channel().closeFuture().sync();

        } finally {
            // 优雅停机
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

```

## 核心对比：NIO vs Netty
为了让你更直观地感受 Netty 的“等价”之处，我做了一个映射表：  


| NIO 原生概念 | Netty 对应组件 | 区别与优势 |  
| :--- | :--- | :--- |  
| Selector | EventLoop | Netty 把 Selector 封装在 EventLoop 里，你不需要自己写 `while` 循环去轮询，Netty 帮你跑好了。 |  
| Channel | Channel | 名字没变，但 Netty 的 Channel 接口功能更强，比如可以直接获取 IP、属性等。 |  
| ByteBuffer | ByteBuf | 最大亮点！ Netty 的 ByteBuf 是动态扩容的（不用像 NIO 那样计算 capacity），而且自带 `readIndex` 和 `writerIndex`，不用手动 `flip()` 了！ |  
| Handler 逻辑 | ChannelHandler | NIO 里所有逻辑都堆在 `if (key.isReadable())` 里，Netty 把它拆分成一个个 Handler，通过 Pipeline（责任链） 串联，代码极其清晰。 |  
| 粘包处理 | FrameDecoder | NIO 需要自己写逻辑判断包尾，Netty 内置了 `LineBasedFrameDecoder`（按行）、`LengthFieldBasedFrameDecoder`（按长度）等几十种解码器。 |  

