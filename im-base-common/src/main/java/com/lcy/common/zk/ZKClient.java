package com.lcy.common.zk;

import com.lcy.common.config.ZkConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.io.UnsupportedEncodingException;

@Slf4j
@Data
public class ZKClient {

    private CuratorFramework client;


    private static  ZKClient instance = null;

    public static ZKClient getIns(){
        if(instance == null){
            instance = new ZKClient();
            instance.init();
        }
        return instance;
    }

    private void  init(){
        if (null != client) {
            return;
        }
        //创建客户端
        ExponentialBackoffRetry retryPolicy =
                new ExponentialBackoffRetry(ZkConfig.baseSleepTimeMs, ZkConfig.maxRetries);
        client = ClientFactory.createWithOptions(ZkConfig.zkAddress,retryPolicy,ZkConfig.connectionTimeoutMs,ZkConfig.sessionTimeoutMs);
        //启动客户端实例,连接服务器
        client.start();
    }

    public void destroy() {
        CloseableUtils.closeQuietly(client);
    }

    /**
     * 创建节点
     * @param zkPath
     * return path
     */
    public String createNode(String zkPath){
        try {
            Stat stat = client.checkExists().forPath(zkPath);
            if (null == stat) {
                return client.create()
                        .creatingParentsIfNeeded()
                        .withProtection()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(zkPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return zkPath;
    }
    /**
     * 创建节点
     * return path
     */
    public String  createNode(String zkPath, String data) {
        return createNode(zkPath,data,CreateMode.PERSISTENT);
    }

    public String createNode(String zkPath, String data,CreateMode createMode) {
        if(data != null){
            try {
                return createNode(zkPath,data.getBytes("utf-8"),createMode);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else{
            return createNode(zkPath,new byte[]{},createMode);
        }
        return null;

    }

    public String createNode(String zkPath, byte[] data,CreateMode createMode) {
        try {
            // 创建一个 ZNode 节点
            if(data == null || data.length <= 0){
                data = "null content".getBytes("UTF-8");
            }
            String forPath = client.create()

                    .creatingParentsIfNeeded()

                    /** 节点模式
                     * （1）PERSISTENT 持久化节点
                     * （2）PERSISTENT_SEQUENTIAL 持久化顺序节点
                     * （3）PHEMERAL 临时节
                     * （4）EPHEMERAL_SEQUENTIAL 临时顺序节点
                     */
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(zkPath, data);
            return forPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 删除节点
     */
    public void deleteNode(String zkPath) {
        try {
            if (!isNodeExist(zkPath)) {
                return;
            }
            client.delete()
                    .deletingChildrenIfNeeded().forPath(zkPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 检查节点
     */
    public boolean isNodeExist(String zkPath) {
        try {

            Stat stat = client.checkExists().forPath(zkPath);
            if (null == stat) {
                log.info("节点不存在:", zkPath);
                return false;
            } else {

                log.info("节点存在 stat is:", stat.toString());
                return true;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建 临时 顺序 节点
     */
    public String createEphemeralSeqNode(String srcpath,byte[] data) {
        try {
            String path = null;
            // 创建一个 ZNode 节点
            ACLBackgroundPathAndBytesable<String> mode = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL);
            if(data != null && data.length > 0 ){
                path = mode.forPath(srcpath,data);
            }else{
                path = mode.forPath(srcpath);
            }
            return path;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
