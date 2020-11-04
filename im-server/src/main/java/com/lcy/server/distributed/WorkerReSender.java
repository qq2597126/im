package com.lcy.server.distributed;


import com.lcy.common.bean.msg.Notification;
import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.codec.ProtobufDecoder;
import com.lcy.common.codec.ProtobufEncoder;
import com.lcy.server.builder.NotificationBuilder;
import com.lcy.server.distributed.zk.ImZkServerWorker;
import com.lcy.server.handler.ImNodeExceptionHandler;
import com.lcy.server.handler.ImNodeHeartBeatClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 数据转发
 */
@Data
@Slf4j
public class WorkerReSender {
    /**
     * 连接通道
     */
    private Channel channel;
    /**
     * 节点信息
     */
    private ImServerNode remoteNode;

    /**
     * 连接标记
     */
    private boolean connectedFlag = false;


    private Bootstrap bootstrap;
    private EventLoopGroup loopGroup;




    private GenericFutureListener<ChannelFuture> closeListener = (ChannelFuture channelFuture)->{
        log.info("分布式连接已经被动断开……", remoteNode.toString());
        //关闭节点
        stopConnecting();
    };


    /**
     * 连接信息设置
     */
    private GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture channelFuture)->{
        if(channelFuture.isSuccess()){
            //连接成功
            connectedFlag = true;
            log.info("当前节点上线，成功连接其他节点： {}",remoteNode.toString());

            channel = channelFuture.channel();

            //连接成功添加关闭监听器
            channel.closeFuture().addListener(closeListener);

            //发送连接通知
            /**
             * 发送链接成功的通知
             */
            Notification<ImServerNode> notification = new Notification<>(ImZkServerWorker.getInst().getLocalNodeInfo());
            notification.setType(Notification.CONNECT_FINISHED);
            ProtoMsg.Message pkg = NotificationBuilder.notification(notification);
            writeAndFlush(pkg);

        }else{
            //连接失败
            loopGroup.schedule(() -> WorkerReSender.this.doConnect(), 10, TimeUnit.SECONDS);
        }
    };

    public WorkerReSender(ImServerNode remoteNode) {
        this.remoteNode = remoteNode;
        bootstrap  =  new Bootstrap();
        loopGroup  = new NioEventLoopGroup();
    }

    public WorkerReSender() {

    }

    public WorkerReSender(Channel channel, ImServerNode remoteNode,boolean connectedFlag) {
        this.channel = channel;
        this.remoteNode = remoteNode;
        this.connectedFlag = connectedFlag;
    }

    //连接
    public void doConnect(){
        if(bootstrap != null && bootstrap.group() == null){
            bootstrap.group(loopGroup);
            bootstrap.channel(NioSocketChannel.class);
            //开启TCP心跳机制
            bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
            bootstrap.option(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT); //默认使用重复缓存
            bootstrap.remoteAddress(remoteNode.getHost(),remoteNode.getPort()); //设置IP地址和端口
            //添加流水线
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast("decoder", new ProtobufDecoder());
                    socketChannel.pipeline().addLast("encoder", new ProtobufEncoder());
                    socketChannel.pipeline().addLast("imNodeHeartBeatClientHandler",new ImNodeHeartBeatClientHandler());
                    socketChannel.pipeline().addLast("exceptionHandler",new ImNodeExceptionHandler());
                }
            });
            log.info(new Date() +"开始连接分布式节点："+remoteNode.getHost());
            ChannelFuture channelFuture = bootstrap.connect();
            //添加连接监听器
            channelFuture.addListener(connectedListener);


        }else if (bootstrap.group() != null) {
            log.info(new Date() + "再一次开始连接分布式节点", remoteNode.toString());
            ChannelFuture channelFuture = bootstrap.connect();
            channelFuture.addListener(connectedListener);
        }
    }

    //关闭连接
    public void stopConnecting() {
        channel = null;
        if(loopGroup != null){
            loopGroup.shutdownGracefully();
        }
        connectedFlag = false;
    }
    /**
     * 消息转发的方法
     * @param pkg  聊天消息
     */
    public void writeAndFlush(Object pkg) {
        if (connectedFlag == false) {
            log.error("分布式节点未连接:", remoteNode.toString());
            return;
        }
        channel.writeAndFlush(pkg);
    }
}
