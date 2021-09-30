package com.banlap.smartexam.bean;

/**
 * @author Banlap on 2021/9/1
 */
public class ClientEMQ {
    private String topic;
    private int qos;
    private String node;
    private String clientid;

    public void setTopic(String topic){
        this.topic = topic;
    }
    public String getTopic(){
        return this.topic;
    }
    public void setQos(int qos){
        this.qos = qos;
    }
    public int getQos(){
        return this.qos;
    }
    public void setNode(String node){
        this.node = node;
    }
    public String getNode(){
        return this.node;
    }
    public void setClientid(String clientid){
        this.clientid = clientid;
    }
    public String getClientid(){
        return this.clientid;
    }
}
