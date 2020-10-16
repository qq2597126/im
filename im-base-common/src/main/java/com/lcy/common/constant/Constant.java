package com.lcy.common.constant;

import io.netty.util.AttributeKey;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/4.
 */
public final class Constant {

    /**
     * 编解码
     */
    public final static class MessageCodecConstants{
        /**
         * 魔数，可以通过配置获取
         */
        public static final short MAGIC_CODE = 0x86;

        /**
         * 版本号
         */
        public static final short VERSION_CODE = 0x01;
    }

    public final static class ImServerConstants{

        public static final AttributeKey<String> CHANNEL_NAME=
                AttributeKey.valueOf("CHANNEL_NAME");



        // 服务器节点的相关信息
        public static final String MANAGE_PATH = "/im/nodes";

        //服务器节点
        public static final String PATH_PREFIX =  MANAGE_PATH + "/seq-";

        //统计用户数的znode
        public static final String COUNTER_PATH = "/im/OnlineCounter";

        public static final String WEB_URL = "http://localhost:8080";




    }
}
