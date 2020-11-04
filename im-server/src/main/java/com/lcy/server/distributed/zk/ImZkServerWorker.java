package com.lcy.server.distributed.zk;

import com.alibaba.fastjson.JSON;
import com.lcy.common.constant.Constant;
import com.lcy.common.utils.StringUtils;
import com.lcy.common.zk.ZKClient;
import com.lcy.server.distributed.AbstractServerWorker;

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
        ZKClient.getIns().createNode(Constant.ImServerConstants.MANAGE_PATH);
        //创建节点
        try {

            //数据写入
            pathRegistered =  ZKClient.getIns().createEphemeralSeqNode(Constant.ImServerConstants.PATH_PREFIX,JSON.toJSONBytes(serverNode));

            //获取ID
            long nodeId = StringUtils.getIdByPath(pathRegistered);

            //更新节点数据
            serverNode.setId(nodeId);

            //更新节点数据
            ZKClient.getIns().getClient().setData().forPath(pathRegistered,JSON.toJSONBytes(serverNode));

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
        serverNode.incBalance();
        while (true) {
            try {
                byte[] payload = JSON.toJSONBytes(serverNode);
                ZKClient.getIns().getClient().setData().forPath(pathRegistered,payload);
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
                ZKClient.getIns().getClient().setData().forPath(pathRegistered, payload);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

    }


    @Override
    public Long getServerId() {
        return StringUtils.getIdByPath(pathRegistered);
    }

    /**
     *  节点编号
     *
     * @return 编号
     * @param path  路径
     */

}
