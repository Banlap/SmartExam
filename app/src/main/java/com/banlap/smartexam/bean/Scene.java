package com.banlap.smartexam.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author Banlap on 2021/8/18
 */
public class Scene implements Serializable {
    private String sceneId;
    private String sceneName;
    private List<GroupAddress> groupAddressList;

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public List<GroupAddress> getGroupAddressList() {
        return groupAddressList;
    }

    public void setGroupAddressList(List<GroupAddress> groupAddressList) {
        this.groupAddressList = groupAddressList;
    }
}
