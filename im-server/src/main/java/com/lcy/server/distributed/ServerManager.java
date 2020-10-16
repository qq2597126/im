package com.lcy.server.distributed;

import java.util.concurrent.ConcurrentMap;

public interface ServerManager<T> {
    /**
     * 获取管理器对象
     * @return
     */
    public ConcurrentMap<Long, T> getWorkerReSenderMap();

}
