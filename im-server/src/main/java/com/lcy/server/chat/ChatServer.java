package com.lcy.server.chat;


import com.lcy.common.codec.ProtobufDecoder;
import com.lcy.common.codec.ProtobufEncoder;
import com.lcy.common.concurrent.ExecuteTask;
import com.lcy.common.concurrent.FutureTaskScheduler;
import com.lcy.server.distributed.WorkerRouter;
import com.lcy.server.distributed.zk.ImZkServerWorker;
import com.lcy.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Data
@Slf4j
@Service("ChatServer")
public class ChatServer {
    // 服务器端口
    @Value("${netty.server.port}")
    private int port;
    // 通过nio方式来接收连接和处理连接
    private EventLoopGroup bg =
            new NioEventLoopGroup();
    private EventLoopGroup wg =
            new NioEventLoopGroup();

    // 启动引导器
    private ServerBootstrap b =
            new ServerBootstrap();
    @Autowired
    private LoginRequestHandler loginRequestHandler;

    @Autowired
    private ServerExceptionHandler serverExceptionHandler;

    @Autowired
    private RemoteNotificationHandler remoteNotificationHandler;

    @Autowired
    private ChatRedirectHandler chatRedirectHandler;

    public void run() {
        //1 设置reactor 线程
        b.group(bg, wg);
        //2 设置nio类型的channel
        b.channel(NioServerSocketChannel.class);
        //3 设置监听端口
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();;
        }
        b.localAddress(new InetSocketAddress(ip, port));

        log.info("InetSocketAddress  : IP {},  Port{}"+ip,port);

        //4 设置通道选项
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.ALLOCATOR,
                PooledByteBufAllocator.DEFAULT);

        //5 装配流水线
        b.childHandler(new ChannelInitializer<SocketChannel>() {
            //有连接到达时会创建一个channel
            protected void initChannel(SocketChannel ch) throws Exception {
                // 管理pipeline中的Handler
                ch.pipeline().addLast("deCoder",new ProtobufDecoder());
                ch.pipeline().addLast("enCoder",new ProtobufEncoder());
                ch.pipeline().addLast("heartBeat",new HeartBeatServerHandler());
                // 在流水线中添加handler来处理登录,登录后删除
                ch.pipeline().addLast("loginRequest",loginRequestHandler);
                ch.pipeline().addLast("remoteNotificationHandler",remoteNotificationHandler);
                ch.pipeline().addLast("chatRedirect",chatRedirectHandler);
                ch.pipeline().addLast("serverException",serverExceptionHandler);
            }
        });
        // 6 开始绑定server
        // 通过调用sync同步方法阻塞直到绑定成功

        ChannelFuture channelFuture = null;
        boolean isStart = false;
        while (!isStart) {
            try {

                channelFuture = b.bind().sync();
                log.info(" IM 启动, 端口为： " +
                        channelFuture.channel().localAddress());
                isStart = true;
            } catch (Exception e) {
                log.error("发生启动异常", e);
                port++;
                log.info("尝试一个新的端口：" + port);
                b.localAddress(new InetSocketAddress(port));
            }
        }


        ImZkServerWorker.getInst().setServerNode(ip, port);

        FutureTaskScheduler.add(new ExecuteTask() {
            @Override
            public void execute() {
                /**
                 * 启动节点
                 */
                ImZkServerWorker.getInst().init();

                /**
                 * 启动节点的管理
                 */
                WorkerRouter.getInst();
            }

            @Override
            public void onException(Throwable t) {
                log.error("节点启动失败");
            }
        });
        try {
            // 7 监听通道关闭事件
            // 应用程序会一直等待，直到channel关闭
            ChannelFuture closeFuture =
                    channelFuture.channel().closeFuture();
            closeFuture.sync();
        } catch (
                Exception e) {
            log.error("发生其他异常", e);
        } finally {
            // 8 优雅关闭EventLoopGroup，
            // 释放掉所有资源包括创建的线程
            wg.shutdownGracefully();
            bg.shutdownGracefully();
        }

    }

}
