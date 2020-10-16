package com.lcy.server.session;

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

    @Override
    public void writeAndFlush(Object pkg) {
        long nodeId = imServerNode.getId();

        WorkerReSender workerReSender = WorkerRouter.getInst().getWorkerReSender(nodeId);

        workerReSender.writeAndFlush(pkg);
    }
    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public boolean isValid() {
        return valid;
    }
}
