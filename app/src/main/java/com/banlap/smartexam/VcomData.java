package com.banlap.smartexam;

import com.banlap.smartexam.bean.Scene;
import com.banlap.smartexam.request.MqttManagement;

import java.util.List;

/**
 * @author Banlap on 2021/8/9
 */
public class VcomData {

    //public static final String BASE_URL = "http://192.168.166.3:18083/api/v4/";
    public static final String BASE_URL = "http://api1.vcom.eu.org:18083/api/v4/";

    public static final String USERNAME = "admin";
    public static final String PASSWORD = "public";

    private static VcomData vcomData = new VcomData();
    public static VcomData getInstance() { return vcomData; }
    public VcomData() {}

    public String ip;
    public String port;
    public String clientId;
    public MqttManagement management;
    public boolean connectStatus;

    public List<Scene> sceneList;

    public MqttManagement getManagement() {
        return management;
    }
    public void setManagement(MqttManagement management) {
        this.management = management;
    }

    public List<Scene> getSceneList() {
        return sceneList;
    }
    public void setSceneList(List<Scene> sceneList) {
        this.sceneList = sceneList;
    }

    public boolean getConnectStatus() { return connectStatus; }
    public void setConnectStatus(boolean connectStatus) { this.connectStatus = connectStatus; }

    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }
    public void setPort(String port) {
        this.port = port;
    }

    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
