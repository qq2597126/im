package com.lcy.server.distributed.zk;

import com.alibaba.fastjson.JSON;
import com.lcy.common.constant.Constant;
import com.lcy.common.utils.StringUtils;
import com.lcy.common.zk.ZKClient;
import com.lcy.server.distributed.ImServerNode;
import com.lcy.server.distributed.ServerManager;
import com.lcy.server.distributed.WorkerReSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;

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
    public ConcurrentMap<Long,WorkerReSender> getWorkerReSenderMap() {
        return workerReSenderMap;
    }

    //初始化轮训信息
    public void init(){
        try {
            //监听节点的新增和删除
            PathChildrenCache childrenCache  = new PathChildrenCache(ZKClient.getIns().getClient(), Constant.ImServerConstants.MANAGE_PATH,true);

            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {

                @Override
                public void childEvent(CuratorFramework client,
                                       PathChildrenCacheEvent event) throws Exception {

                    log.info("开始监听其他的ImWorker子节点:-----");
                    ChildData data = event.getData();
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            log.info("CHILD_ADDED : " + data.getPath() + "  数据:" + new String(data.getData()));
                            processNodeAdded(data);
                            break;
                        case CHILD_REMOVED:
                            log.info("CHILD_REMOVED : " + data.getPath() + "  数据:" + new String(data.getData()));
                            processNodeRemoved(data);
                            break;
                        case CHILD_UPDATED:
                            log.info("CHILD_UPDATED : " + data.getPath() + "  数据:" + new String(data.getData()));
                            break;
                        default:
                            log.debug("[PathChildrenCache]节点数据为空, path={}", data == null ? "null" : data.getPath());
                            break;
                    }

                }
            };
            childrenCache.getListenable().addListener(childrenCacheListener);
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
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
        byte[] payload = data.getData();
        String path = data.getPath();

        ImServerNode imServerNode = JSON.parseObject(payload,ImServerNode.class);
        imServerNode.setId(StringUtils.getIdByPath(path));


        log.info("[TreeCache]节点更新端口, path={}, data={}",
                data.getPath(), JSON.toJSONString(imServerNode));

        if(imServerNode.equals(ImZkServerWorker.getInst().getServerNode()))
        {
            log.info("[TreeCache]本地节点, path={}, data={}",
                    data.getPath(), JSON.toJSONString(imServerNode));
            return;
        }
        WorkerReSender workerReSender = workerReSenderMap.get(imServerNode.getId());
        //重复收到注册的事件
        if (null != workerReSender && workerReSender.getRemoteNode().equals(imServerNode)) {

            log.info("[TreeCache]节点重复增加, path={}, data={}",
                    data.getPath(), JSON.toJSONString(imServerNode));
            return;
        }
        if (null != workerReSender) {
            //关闭老的连接
            workerReSender.stopConnecting();
        }
        //创建一个消息转发器
        workerReSender = new WorkerReSender(imServerNode);
        //建立转发的连接
        workerReSender.doConnect();

        workerReSenderMap.put(imServerNode.getId(), workerReSender);
    }

    public void processNodeRemoved(ChildData data) {

        byte[] serverNodeByte = data.getData();
        String path = data.getPath();
        ImServerNode imServerNode = JSON.parseObject(serverNodeByte,ImServerNode.class);
        imServerNode.setId(StringUtils.getIdByPath(path));

        WorkerReSender workerReSender = workerReSenderMap.get(imServerNode.getId());

        log.info("[TreeCache]节点删除, path={}, data={}",
                data.getPath(),imServerNode.toString());


        if (null != workerReSender) {
            workerReSender.stopConnecting();
            workerReSenderMap.remove(imServerNode.getId());
        }
    }

    public void addWorkerReSender(WorkerReSender workerReSender){

        WorkerReSender sender = workerReSenderMap.get(workerReSender.getRemoteNode().getId());

        if(sender == null || sender.getRemoteNode().equals(workerReSender.getRemoteNode())){
            workerReSenderMap.put(workerReSender.getRemoteNode().getId(), workerReSender);
        }else {
            log.info("重复添加服务转发器");
        }
    }

}
