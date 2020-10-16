package com.lcy.server.balance;

import com.alibaba.fastjson.JSON;
import com.lcy.common.constant.Constant;
import com.lcy.common.zk.ZKClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.lcy.server.distributed.ImServerNode;

/**
 *
 */
public class ImServerLoadBalance implements LoadBalance<ImServerNode>{

    //获取服务器节点列表
    public List<ImServerNode> getImServerNodeList(String zkPath){
        List<ImServerNode> imServerNodes = new ArrayList<>();
        List<String> children = null;
        try {
            children = ZKClient.instance.getClient().getChildren().forPath(zkPath);
            for (String child : children) {
                //处理数据
                try {
                    byte[] imServerBytes = ZKClient.instance.getClient().getData().forPath(child);

                    if(imServerBytes != null && imServerBytes.length > 0){
                        imServerNodes.add(JSON.parseObject(imServerBytes,ImServerNode.class));
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imServerNodes;
    }


    @Override
    public ImServerNode balance() {
        List<ImServerNode> serverNodeList = getImServerNodeList(Constant.ImServerConstants.MANAGE_PATH);
        if(serverNodeList != null && serverNodeList.size() > 0){
            Collections.sort(serverNodeList);
            com.lcy.server.distributed.ImServerNode imServerNode = serverNodeList.get(0);
            return imServerNode;
        }
        return null;
    }
}
