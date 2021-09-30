package com.banlap.smartexam.request;

import com.banlap.smartexam.bean.GroupAddress;

import java.util.List;

/**
 * @author Banlap on 2021/8/7
 */
public class MessageEvent {
    public static final int CONNECT_READY =0x10;
    public static final int CONNECT_SUCCESS =0x11;
    public static final int CONNECT_ERROR =0x12;
    public static final int CONNECT_LOST =0x13;
    public static final int CONFIG_SUCCESS = 0x14;
    public static final int CONFIG_ERROR = 0x15;
    public static final int CHECK_ID_SUCCESS = 0x16;
    public static final int CHECK_ID_ERROR = 0x17;

    public static final int SEND_SUCCESS =0x20;
    public static final int SEND_ERROR =0x21;

    public static final int RESPONSE_SUCCESS =0x30;
    public static final int RESPONSE_ERROR =0x31;

    public static final int SAVE_SUCCESS = 0x40;

    public static final int NOT_OPS = 0x50;


    public int msgCode;
    public String msgData;
    public List<GroupAddress> list;
    public String sceneName;

    public MessageEvent(int msgCode) { this.msgCode = msgCode; }

    public MessageEvent(int msgCode, List<GroupAddress> list, String sceneName) { this.msgCode = msgCode; this.list = list; this.sceneName = sceneName; }

    public MessageEvent(int msgCode, String msgData) { this.msgCode = msgCode; this.msgData = msgData; }

}
