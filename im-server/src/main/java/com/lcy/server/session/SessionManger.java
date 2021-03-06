package com.lcy.server.session;

import com.alibaba.fastjson.JSON;
import com.lcy.common.bean.bo.User;
import com.lcy.common.bean.msg.Notification;
import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.session.ServerSession;
import com.lcy.server.builder.NotificationBuilder;
import com.lcy.server.distributed.ImServerNode;
import com.lcy.server.distributed.OnlineCounter;
import com.lcy.server.distributed.WorkerRouter;
import com.lcy.server.distributed.zk.ImZkServerWorker;
import com.lcy.server.redis.UserSessionsDAO;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j
@Component
public class SessionManger {



    private static SessionManger singleInstance = null;

    /*本地会话集合 sessionId*/
    private ConcurrentHashMap<String, LocalSession> localSessionMap = new ConcurrentHashMap<>();

    /*本地用户集合 uid*/
    private ConcurrentHashMap<String, UserSessions> sessionsLocalCache = new ConcurrentHashMap();

    /*远程会话集合 session*/
    private ConcurrentHashMap<String, RemoteSession> remoteSessionMap = new ConcurrentHashMap();



    public static SessionManger inst() {
        return singleInstance;
    }

    @Autowired
    public UserSessionsDAO userSessionsDAO;
    @Autowired
    public OnlineCounter onlineCounter;

    public static void setSingleInstance(SessionManger singleInstance) {
        SessionManger.singleInstance = singleInstance;
    }

    /**
     * 登录成功之后， 增加session对象
     */
    public void addLocalSession(LocalSession session) {

        String sessionId = session.getSessionId();
        localSessionMap.put(sessionId, session);
        String uid = session.getUser().getUid();

        //增加用户数
        ImZkServerWorker.getInst().incBalance();


        log.info("本地session增加：{},  当前节点在线总数:{} ",
                JSON.toJSONString(session.getUser()),
                ImZkServerWorker.getInst().getLocalNodeInfo().getBalance().intValue()
                );



        //增加用户的session 信息到缓存
        userSessionsDAO.cacheUser(uid, sessionId);


        /**
         * 通知其他节点
         */
        notifyOtherImNode(session, Notification.SESSION_ON);

    }


    /**
     * 删除session
     */
    public void removeLocalSession(String sessionId) {
        if (!localSessionMap.containsKey(sessionId)) {
            return;
        }
        LocalSession session = localSessionMap.get(sessionId);
        String uid = session.getUser().getUid();
        localSessionMap.remove(sessionId);



        //减少用户数
        ImZkServerWorker.getInst().decrBalance();

        log.info("本地session减少：{},  在线总数:{} ",
                JSON.toJSONString(session.getUser()),
                ImZkServerWorker.getInst().getLocalNodeInfo().getBalance().intValue());

        //分布式保存user和所有session
        userSessionsDAO.removeUserSession(uid, sessionId);

        /**
         * 通知其他节点
         */
        notifyOtherImNode(session, Notification.SESSION_OFF);

    }

    /**
     * 通知其他节点
     *
     * @param session session
     * @param type    类型
     */
    private void notifyOtherImNode(LocalSession session, int type) {

        User user = session.getUser();

        RemoteSession remoteSession = new RemoteSession(user.getUid(),session.getSessionId(),ImZkServerWorker.getInst().getServerNode());

        Notification<RemoteSession> notification = new Notification<RemoteSession>(remoteSession);
        notification.setType(type);


        ProtoMsg.Message message = NotificationBuilder.notification(notification);

        WorkerRouter.getInst().sendNotification(message);
    }


    /**
     * 根据用户id，获取session对象
     */
    public List<ServerSession> getSessionsBy(String userId) {

        List<ServerSession> sessions = new LinkedList<>();


        UserSessions userSessions = loadFromCache(userId);

        if (null == userSessions) {
            return null;
        }

        Map<String, ImServerNode> allSession = userSessions.getMap();

        allSession.keySet().stream().forEach(key -> {

            //首先取得本地的session
            ServerSession session = localSessionMap.get(key);

            //没有命中，取得远程的session
            if (session == null) {
                session = remoteSessionMap.get(key);

            }
            sessions.add(session);
        });


        return sessions;

    }

    /**
     * 从二级缓存加载
     *
     * @param userId 用户的id
     * @return 用户的集合
     */
    private UserSessions loadFromRedis(String userId) {


        UserSessions userSessions = userSessionsDAO.getAllSession(userId);

        if (null == userSessions) {
            return null;
        }
        Map<String, ImServerNode> map = userSessions.getMap();
        map.keySet().stream().forEach(key -> {
            ImServerNode node = map.get(key);
            //当前节点直接忽略
            if (!node.equals(ImZkServerWorker.getInst().getLocalNodeInfo())) {

                remoteSessionMap.put(key, new RemoteSession(key, userId, node));

            }
        });


        return userSessions;
    }


    /**
     * 从二级缓存加载
     *
     * @param userId 用户的id
     * @return 用户的集合
     */
    private UserSessions loadFromCache(String userId) {

        //本地缓存
        UserSessions userSessions = sessionsLocalCache.get(userId);

        if (null != userSessions
                && null != userSessions.getMap()
                && userSessions.getMap().keySet().size() > 0) {
            return userSessions;
        }


        UserSessions finalUserSessions = new UserSessions(userId);

        localSessionMap.values().stream().forEach(session -> {

            if (userId.equals(session.getUser().getUid())) {
                finalUserSessions.addLocalSession(session);
            }
        });

        remoteSessionMap.values().stream().forEach(session -> {

            if (userId.equals(session.getUserId())) {
                finalUserSessions.addSession(session.getSessionId(), session.getImServerNode());
            }
        });

        sessionsLocalCache.put(userId, finalUserSessions);


        return finalUserSessions;
    }


    /**
     * 增加 远程的 session
     */
    public void addRemoteSession(RemoteSession remoteSession) {
        String sessionId = remoteSession.getSessionId();
        if (localSessionMap.containsKey(sessionId)) {
            log.error("通知有误，通知到了会话所在的节点");
            return;
        }

        remoteSessionMap.put(sessionId, remoteSession);
        //删除本地保存的 远程session
        String uid = remoteSession.getUserId();
        UserSessions sessions = sessionsLocalCache.get(uid);
        if (null == sessions) {
            sessions = new UserSessions(uid);
            sessionsLocalCache.put(uid, sessions);
        }

        sessions.addSession(sessionId, remoteSession.getImServerNode());
    }

    /**
     * 删除 远程的 session
     */
    public void removeRemoteSession(String sessionId) {
        if (localSessionMap.containsKey(sessionId)) {
            log.error("通知有误，通知到了会话所在的节点");
            return;
        }

        RemoteSession s = remoteSessionMap.get(sessionId);
        remoteSessionMap.remove(sessionId);

        //删除本地保存的 远程session
        String uid = s.getUserId();
        UserSessions sessions = sessionsLocalCache.get(uid);
        sessions.removeSession(sessionId);

    }


    //关闭连接
    public void closeSession(ChannelHandlerContext ctx) {

        LocalSession  session =
                (LocalSession) ctx.channel().attr(LocalSession.SESSION_KEY).get();
        if (null != session && session.isValid()) {
            session.close();
            this.removeLocalSession(session.getSessionId());
        }
    }

}
