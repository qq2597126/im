package com.lcy.common.utils;

import java.util.UUID;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/8.
 */
public class SessionUtils {

    public static String createSessionId(){
        UUID randomUUID = UUID.randomUUID();
        return randomUUID.toString().replaceAll("-","");
    }
}
