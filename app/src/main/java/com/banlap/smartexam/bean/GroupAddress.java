package com.banlap.smartexam.bean;

import android.util.Log;

import java.io.Serializable;
import java.util.List;

/**
 * @author Banlap on 2021/8/19
 */
public class GroupAddress implements Serializable {
    private int mainGroup;
    private int middleIp;
    private int groupIp;
    private boolean isEnable;          //Is the function enabled
    private Function function;
    public boolean isSelect = false;

    public int getMainGroup() {
        return mainGroup;
    }

    public void setMainGroup(int mainGroup) {
        this.mainGroup = mainGroup;
    }

    public int getMiddleIp() {
        return middleIp;
    }

    public void setMiddleIp(int middleIp) {
        this.middleIp = middleIp;
    }

    public int getGroupIp() {
        return groupIp;
    }

    public void setGroupIp(int groupIp) {
        this.groupIp = groupIp;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public Function getFunction() { return function; }

    public void setFunction(Function function) {
        this.function = function;
    }
}
