package com.lcy.server.distributed.zk;

import com.alibaba.fastjson.JSON;
import com.lcy.common.constant.Constant;
import com.lcy.server.distributed.ImServerNode;
import com.lcy.server.distributed.ServerManager;
import com.lcy.server.distributed.WorkerReSender;
import com.lcy.common.zk.ZKClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ImZkServerManager implements ServerManager<WorkerReSender> {

    private static ImZkServerManager singleInstance = null;

    private ConcurrentHashMap<Long,WorkerReSender> workerReSenderMap = new ConcurrentHashMap<>();

    private ImZkServerManager(){

    }

    public static ImZkServerManager getInst(){
        if(singleInstance == null){
            singleInstance = new ImZkServerManager();
            singleInstance.init();
        }
        return singleInstance;
    }

    @Override
    public ConcurrentMap getWorkerReSenderMap() {
        return workerReSenderMap;
    }

    //初始化轮训信息
    public void init(){
        try {
            //监听节点的新增和删除
            TreeCache treeCache = new TreeCache(ZKClient.instance.getClient(), Constant.ImServerConstants.MANAGE_PATH);
            TreeCacheListener treeCacheListener = new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                    TreeCacheEvent.Type type = treeCacheEvent.getType();
                    ChildData eventData = treeCacheEvent.getData();
                    log.info("im 节点 相关操作："+type.name());
                    if(eventData != null){
                        switch (type){
                            case NODE_ADDED:
                                //添加相关操作
                                processNodeAdded(eventData);
                                break;
                            case NODE_REMOVED:
                                //删除相关操作
                                processNodeRemoved(eventData);
                                break;
                            default:
                                break;
                        }
                    }else {
                        log.info("节点数据为空");
                    }
                }
            };
            treeCache.getListenable().addListener(treeCacheListener);
            treeCache.start();
        } catch (Exception e) {
            log.error("初始化 节点监听器失败");
            e.printStackTrace();
        }
    }

    /**
     * 数据处理
     * @param data
     */
    public void processNodeAdded(ChildData data) {
        log.info("[TreeCache]节点更新端口, path={}, data={}", data.getPath(), data.getData());
        byte[] serverNodeByte = data.getData();
        String path = data.getPath();
        ImServerNode imServerNode = JSON.parseObject(serverNodeByte,ImServerNode.class);
        long nodeId = imServerNode.getId();
        WorkerReSender workerReSender = workerReSenderMap.get(nodeId);
        if(workerReSender != null && workerReSender.getRemoteNode().equals(imServerNode)){
            return;
        }

        WorkerReSender reSender = new WorkerReSender(imServerNode);
        //建立连接
        reSender.doConnect();
        workerReSenderMap.put(nodeId,workerReSender);
    }

    public void processNodeRemoved(ChildData data) {

        byte[] serverNodeByte = data.getData();
        String path = data.getPath();
        ImServerNode imServerNode = JSON.parseObject(serverNodeByte,ImServerNode.class);
        long nodeId = imServerNode.getId();
        WorkerReSender workerReSender = workerReSenderMap.get(nodeId);

        log.info("[TreeCache]节点删除, path={}, data={}",
                data.getPath(),imServerNode.toString());


        if (null != workerReSender) {
            workerReSender.stopConnecting();
            workerReSenderMap.remove(nodeId);
        }
    }

}
