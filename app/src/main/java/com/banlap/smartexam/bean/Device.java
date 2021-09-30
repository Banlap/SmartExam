package com.banlap.smartexam.bean;

import java.util.List;

/**
 * @author Banlap on 2021/8/25
 */
public class Device {
    private String deviceId;
    private String deviceName;
    private String deviceMac;
    private String deviceType;
    private List<Way> wayList;
    private List<Function> functionList;
    public boolean isSelect = false;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<Way> getWayList() {
        return wayList;
    }

    public void setWayList(List<Way> wayList) {
        this.wayList = wayList;
    }

    public List<Function> getFunctionList() {
        return functionList;
    }

    public void setFunctionList(List<Function> functionList) {
        this.functionList = functionList;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
