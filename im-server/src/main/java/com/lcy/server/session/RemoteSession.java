package com.lcy.server.session;

import com.lcy.common.bean.bo.User;
import com.lcy.common.session.ServerSession;
import com.lcy.server.distributed.ImServerNode;
import com.lcy.server.distributed.WorkerReSender;
import com.lcy.server.distributed.WorkerRouter;
import lombok.Data;

import java.io.Serializable;
@Data
public class RemoteSession implements ServerSession, Serializable {

    private String userId;
    private String sessionId;
    private ImServerNode imServerNode;

    private boolean valid= true;

    public RemoteSession() {

    }

    public RemoteSession(String userId, String sessionId, ImServerNode imServerNode) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.imServerNode = imServerNode;
    }


    @Override
    public void writeAndFlush(Object pkg) {
        long nodeId = imServerNode.getId();

        WorkerReSender workerReSender = WorkerRouter.getInst().getWorkerReSender(nodeId);

        workerReSender.writeAndFlush(pkg);
    }
    @Override
    public User getUser() {
        return null;
    }
}
