package com.banlap.smartexam.bean;

import java.util.List;

/**
 * @author Banlap on 2021/9/18
 */
public class Way {
    private String wayId;
    private String wayName;
    private List<Function> functionList;
    public boolean isSelect = false;

    public String getWayId() {
        return wayId;
    }

    public void setWayId(String wayId) {
        this.wayId = wayId;
    }

    public String getWayName() {
        return wayName;
    }

    public void setWayName(String wayName) {
        this.wayName = wayName;
    }

    public List<Function> getFunctionList() {
        return functionList;
    }

    public void setFunctionList(List<Function> functionList) {
        this.functionList = functionList;
    }
}
