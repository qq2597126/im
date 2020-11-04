package com.lcy.server.distributed;

public  abstract class AbstractServerWorker implements ServerWorker{

    public ImServerNode serverNode = null;

    public AbstractServerWorker() {
        //初始化
        serverNode = new ImServerNode();
    }

    public void setServerNode(String host, int port){
        serverNode.setHost(host);
        serverNode.setPort(port);
    }

    public ImServerNode getLocalNodeInfo() {
        return serverNode;
    }

    public ImServerNode getServerNode() {
        return serverNode;
    }

    public void setServerNode(ImServerNode serverNode) {
        this.serverNode = serverNode;
    }
}
