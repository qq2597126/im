package com.lcy.server.distributed;

public interface OnlineCounter {

    //设置某个值
    public void set(Long nodeId,Long value);

    //某个值+1
    public void increment(Long nodeId);
    //某个值-1
    public void decrement(Long nodeId);

    //节点下线操作

    public void remove(Long nodeId);

    // 获取某个值
    public Long get(Long nodeId);
}
