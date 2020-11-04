package com.lcy.server.session;

import com.lcy.server.distributed.ImServerNode;
import com.lcy.server.distributed.zk.ImZkServerWorker;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class UserSessions {

    private String userId;
    private Map<String, ImServerNode> map = new LinkedHashMap<>(10);

    public UserSessions(String userId) {
        this.userId = userId;
    }


    public void addSession(String sessionId, ImServerNode node) {

        map.put(sessionId, node);
    }

    public void removeSession(String sessionId) {
        map.remove(sessionId);
    }


    public void addLocalSession(LocalSession session) {
        map.put(session.getSessionId(), ImZkServerWorker.getInst().getLocalNodeInfo());
    }
}
