package com.lcy.common.utils;

import com.lcy.common.constant.Constant;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
    public static long getIdByPath(String path) {
        String sid = null;
        if (null == path) {
            throw new RuntimeException("节点路径有误");
        }
        int index = path.lastIndexOf(Constant.ImServerConstants.PATH_PREFIX);
        if (index >= 0) {
            index += Constant.ImServerConstants.PATH_PREFIX.length();
            sid = index <= path.length() ? path.substring(index) : null;
        }

        if (null == sid) {
            throw new RuntimeException("节点ID获取失败");
        }

        return Long.parseLong(sid);

    }
}
