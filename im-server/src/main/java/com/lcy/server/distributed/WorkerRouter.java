package com.lcy.server.distributed;

import com.lcy.server.distributed.zk.ImZkServerManager;
import com.lcy.server.distributed.zk.ImZkServerWorker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkerRouter {

    //链接轮询

    private static WorkerRouter singleInstance = null;

    private ServerManager<WorkerReSender> serverManager;

    private ServerWorker serverWorker;



    public static WorkerRouter getInst(){
        if(singleInstance == null){
            singleInstance = new WorkerRouter();
            //设置默认值
            singleInstance.serverManager = ImZkServerManager.getInst();
            singleInstance.serverWorker = ImZkServerWorker.getInst();
        }
        return singleInstance;
    }
    //设置节点管理器
    public static WorkerRouter setServerManager(ServerManager serverManager) {
        if(singleInstance == null){
            getInst();
        }
        singleInstance.serverManager = serverManager;
        return singleInstance;
    }
    //设置当前节点操作
    public static WorkerRouter setServerWorker(ServerWorker serverWorker) {
        if(singleInstance == null){
            getInst();
        }
        singleInstance.serverWorker = serverWorker;
        return singleInstance;
    }

    private WorkerRouter(){

    }

    public WorkerReSender getWorkerReSender(Long id) {
        WorkerReSender workerReSender = serverManager.getWorkerReSenderMap().get(id);
        if (null != workerReSender) {
            return workerReSender;
        }
        return null;
    }


    //根据消息类型 进行 消息转发

    public void sendNotification(String json) {
        serverManager.getWorkerReSenderMap().forEach((key, workerReSender) -> {
            if (!key.equals(serverWorker.getServerId())) {
                workerReSender.writeAndFlush(json);
            }
        });

    }

}
