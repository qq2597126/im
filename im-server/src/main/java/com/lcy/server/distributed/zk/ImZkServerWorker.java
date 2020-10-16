package com.lcy.server.distributed.zk;

import com.alibaba.fastjson.JSON;
import com.lcy.common.constant.Constant;
import com.lcy.server.distributed.AbstractServerWorker;
import com.lcy.common.zk.ZKClient;

public class ImZkServerWorker extends AbstractServerWorker {

    //保存当前ZNode节点的路径，创建后返回
    private String pathRegistered = null;

    private static ImZkServerWorker singleInstance = null;

    //取得单例
    public static ImZkServerWorker getInst() {

        if (null == singleInstance) {

            singleInstance = new ImZkServerWorker();
        }
        return singleInstance;
    }
    //初始化节点信息
    public void init(){
        //创建路径

        ZKClient.instance.createNode();
        //创建节点
        try {
            byte[] payload = JSON.toJSONBytes(serverNode);
            //数据写入
            pathRegistered = ZKClient.instance.createEphemeralSeqNode(Constant);
            //为node 设置id
            serverNode.setId(getServerId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 增加负载，表示有用户登录成功
     *
     * @return 成功状态
     */
    @Override
    public boolean incBalance() {
        if (null == serverNode) {
            throw new RuntimeException("还没有设置Node 节点");
        }
        // 增加负载：增加负载，并写回zookeeper
        while (true) {
            try {
                serverNode.incBalance();
                byte[] payload = JSON.toJSONBytes(serverNode);
                ZKClient.instance.getClient().setData().forPath(pathRegistered,payload);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

    }
    /**
     * 减少负载，表示有用户下线，写回zookeeper
     *
     * @return 成功状态
     */
    @Override
    public boolean decrBalance() {
        if (null == serverNode) {
            throw new RuntimeException("还没有设置Node 节点");
        }
        while (true) {
            try {

                serverNode.decrementBalance();

                byte[] payload = JSON.toJSONBytes(serverNode);
                ZKClient.instance.getClient().setData().forPath(pathRegistered, payload);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

    }


    @Override
    public Long getServerId() {
        return getIdByPath(pathRegistered);
    }

    /**
     *  节点编号
     *
     * @return 编号
     * @param path  路径
     */
    public long getIdByPath(String path) {
        String sid = null;
        if (null == path) {
            throw new RuntimeException("节点路径有误");
        }
        int index = path.lastIndexOf(ImServerConstants.PATH_PREFIX);
        if (index >= 0) {
            index += ImServerConstants.PATH_PREFIX.length();
            sid = index <= path.length() ? path.substring(index) : null;
        }

        if (null == sid) {
            throw new RuntimeException("节点ID获取失败");
        }

        return Long.parseLong(sid);

    }
}
