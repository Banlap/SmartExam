package com.banlap.smartexam.fvm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.banlap.smartexam.VcomData;
import com.banlap.smartexam.bean.Device;
import com.banlap.smartexam.request.MessageEvent;
import com.banlap.smartexam.request.MqttManagement;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @author Banlap on 2021/9/23
 */
public class SensorStatusFVM extends AndroidViewModel {

    private SensorStatusCallBack callBack;

    public SensorStatusFVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(SensorStatusCallBack callBack) {
        this.callBack = callBack;
    }

    /*
     * 查询设备状态
     * */
    public void queryStatus(List<Device> list) {
        MqttManagement management = VcomData.getInstance().getManagement();
        Thread thread = new Thread(() -> {
            //执行查询功能状态
            boolean isPublish = false;
            int mListSize = list.size();
            for (int i = 0; i < mListSize; i++) {
                if (list.get(i).getWayList().size() == 0) {
                    int funListSize = list.get(i).getFunctionList().size();
                    for (int j = 0; j < funListSize; j++) {
                        if (!list.get(i).getFunctionList().get(j).getGroupAddress().equals("")) {
                            isPublish = true;
                            try {
                                String code = "{\"id\":0,\"type\":1,\"data\":{\"addr\":[" + list.get(i).getFunctionList().get(j).getGroupAddress() + "],\"rw\":\"r\"}}";
                                String topic = MqttManagement.PUBLISH_TOPIC + VcomData.getInstance().getClientId();
                                //根据对应的topic值 发送数据 (同时发送会出现只返回一条记录，需要线程休眠几毫秒)
                                management.publish(topic, code);
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }


                    }
                }
            }
            //判断是否向订阅发送数据
            if (!isPublish) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.NOT_OPS));
            } else {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.SEND_SUCCESS));
            }
        });
        thread.start();
    }

    public interface SensorStatusCallBack {

    }
}
