package com.banlap.smartexam.request;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.banlap.smartexam.VcomData;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;

/**
 * @author Banlap on 2021/8/7
 */
public class MqttManagement{

    private MqttClient mqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions options;

    //private String host = "tcp://192.168.3.233:61613";
    //private String host = "tcp://192.168.166.3:1883";
    private String host = "tcp://api1.vcom.eu.org:1883";
    private String userName = "Banlap";
    private String passWord = "Banlap";
    private String mqttId = "Vcom_" + Build.MODEL;
    public static String PUBLISH_TOPIC_SYS  = "/cmd/s/sys/";//发布主题 系统
    public static String RESPONSE_TOPIC_SYS   = "/cmd/p/sys/";//响应主题 系统
    public static String PUBLISH_TOPIC  = "/cmd/s/app/";//发布主题 app
    public static String RESPONSE_TOPIC = "/cmd/p/app/";//响应主题 app

    public static final int CONNECT_SUCCESS = 100;
    public static final int CONNECT_ERROR = 200;

    private Handler mHandler = new Handler(msg -> {
        switch (msg.what) {
            case CONNECT_SUCCESS:
                subscribeTopic();
                EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_SUCCESS));
                break;
            case CONNECT_ERROR:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_ERROR));
                break;
        }
        return false;
    });

    private String mConnectIP="";  //连接ip

  /*  @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return super.onStartCommand(intent, flags, startId);
    }*/

    public MqttManagement(String connectIP) {
        try {
            mConnectIP = connectIP;
            mqttClient = new MqttClient(host, mqttId, new MemoryPersistence());
            //mqttAndroidClient = new MqttAndroidClient(this, ip, mqttId);

            options = new MqttConnectOptions();
            options.setCleanSession(true);      //设置是否清除缓存
            options.setUserName(userName);
            options.setPassword(passWord.toCharArray());
            options.setConnectionTimeout(10);    // 设置超时时间 单位为秒
            options.setKeepAliveInterval(20);    // 设置会话心跳时间

            mqttClient.setCallback(mqttCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        try {
            //mqttAndroidClient = new MqttAndroidClient(this, host, mqttId);

            options = new MqttConnectOptions();
            options.setCleanSession(true);      //设置是否清除缓存
            options.setUserName(userName);
            options.setPassword(passWord.toCharArray());
            options.setConnectionTimeout(10);    // 设置超时时间 单位为秒
            options.setKeepAliveInterval(20);    // 设置会话心跳时间
            mqttAndroidClient.setCallback(mqttCallback);

            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * 连接mqtt
    * */
    public void connect() {
        try {
            mqttClient.connect(options);
            Message msg = new Message();
            msg.what = CONNECT_SUCCESS;
            mHandler.sendMessage(msg);
        } catch (Exception e) {
            EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_ERROR));
            e.printStackTrace();
        };
    }

    public boolean isConnect(){
        return mqttClient.isConnected();
    }

    /*
    * 订阅接收topic
    * */
    public void subscribeTopic() {
            try {
                if(mqttClient.isConnected()) {
                    mqttClient.subscribe(RESPONSE_TOPIC_SYS + mConnectIP, 0);
                    mqttClient.subscribe(RESPONSE_TOPIC + mConnectIP, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    /*
    *  断开mqtt连接
    * */
    public void disconnect() {
        if (mqttClient == null || !mqttClient.isConnected()) {
            return;
        }
        try {
            mqttClient.disconnect();
            EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_LOST));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //MQTT是否连接成功的监听
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            new Handler().postDelayed(() -> {
                try {
                    // 订阅myTopic话题
                    mqttAndroidClient.subscribe(RESPONSE_TOPIC_SYS + VcomData.getInstance().getClientId(),0);
                    mqttAndroidClient.subscribe(RESPONSE_TOPIC + VcomData.getInstance().getClientId(), 0);
                    //EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_SUCCESS));
                } catch (MqttException e) {
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_ERROR));
                    e.printStackTrace();
                }
            }, 500);

        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_ERROR));
        }
    };
    //MQTT 监听
    private static IMqttActionListener publishCallback = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.e("Debug -----------","mqttAndroidClient publishCallback Suc");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.e("Debug -----------","mqttAndroidClient publishCallback Fail");
            EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_ERROR));
        }
    };


    /*
     * 发送消息
     * */
    public void publish(String topic, String message) {
        if (mqttClient == null || !mqttClient.isConnected() ) {
            return;
        }
        MqttMessage msg = new MqttMessage();
        msg.setPayload(message.getBytes());
        try {
            mqttClient.publish(topic, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    /*
    * 订阅主题的回调
    * */
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void connectionLost(Throwable cause) {
            EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_LOST));
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.e("MessageArrived:", "topic: " + topic + " -- message: " + message);
            String msg = new String(message.getPayload(), StandardCharsets.UTF_8);
            EventBus.getDefault().post(new MessageEvent(MessageEvent.RESPONSE_SUCCESS, msg));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };


   /* @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }*/
}
