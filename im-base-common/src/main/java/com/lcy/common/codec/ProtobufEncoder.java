package com.lcy.common.codec;


import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.exception.MessageTooLongException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProtobufEncoder extends MessageToByteEncoder<ProtoMsg.Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ProtoMsg.Message message, ByteBuf byteBuf) throws Exception {
        //消息数组
        byte[] messageBytes = message.toByteArray();
        int messageBytesLength = messageBytes.length;

        //根据消息大小判断消息长度占有的字节数 (现在长度) //建议不要过长 2个字节存储消息大小 ，最大为32767
        if (messageBytes.length < Short.MAX_VALUE){
            byteBuf.writeShort(messageBytesLength); //写入2个字节
        }else{
            throw  new MessageTooLongException("Message TooLong Short.MAX_VALUE  CURRENT："+messageBytesLength);
        }

        //2.写入魔数


        //3.写入版本号

        byteBuf.writeBytes(messageBytes);

    }
}
