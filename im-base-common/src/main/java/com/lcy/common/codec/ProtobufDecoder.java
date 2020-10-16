package com.lcy.common.codec;


import com.lcy.common.bean.msg.ProtoMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ProtobufDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        // 标记一下当前的读指针readIndex的位置
        byteBuf.markReaderIndex();
        // 判断包头的长度
        if (byteBuf.readableBytes() < 2)
        {
            // 不够包头
            return;
        }
        //判断当前数据包
        // 读取传送过来的消息的长度。
        int length = byteBuf.readUnsignedShort();
        // 长度如果小于0
        if (length < 0) {// 非法数据，关闭连接
            channelHandlerContext.close();
        }
        //读取魔数


        //读取版本号


        if (length >byteBuf.readableBytes()) {// 读到的消息体长度如果小于传送过来的消息长度
            // 重置读取位置
            byteBuf.resetReaderIndex();
            return;
        }

        // 读取传送过来的消息的长度。
        byte[] array ;
        if (byteBuf.hasArray()) {
            //堆缓冲
            ByteBuf slice = byteBuf.slice();
            array = slice.array();
        }
        else{
            //直接缓冲(需要copy)
            array = new byte[length];
            byteBuf.readBytes( array, 0, length);
        }
        // 字节转成Protobuf的POJO对象
        ProtoMsg.Message outmsg = ProtoMsg.Message.parseFrom(array);

        if (outmsg != null) {
            // 获取业务消息
            out.add(outmsg);
        }
    }
}
