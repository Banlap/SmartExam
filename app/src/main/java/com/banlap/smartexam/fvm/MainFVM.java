package com.banlap.smartexam.fvm;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.banlap.smartexam.VcomData;
import com.banlap.smartexam.bean.Scene;
import com.banlap.smartexam.request.MessageEvent;
import com.banlap.smartexam.request.MqttManagement;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @author Banlap on 2021/8/7
 */
public class MainFVM extends AndroidViewModel {

    private MainFVMCallBack callBack;
    private MqttManagement management;

    public MainFVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(MainFVMCallBack callBack) {
        this.callBack = callBack;
    }

    /*
     * 连接状态
     * */
    public void connect() {
        /*management = VcomData.getInstance().getManagement();
        if (!VcomData.getInstance().getConnectStatus()) {
            management.connect();
        } else {
            management.disconnect();
        }*/
    }

    public void runScene(List<Scene> list, int position) {
        MqttManagement management = VcomData.getInstance().getManagement();

        new Thread(() -> {
            int size = list.get(position).getGroupAddressList().size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    try {
                        String groupIp = "[" + list.get(position).getGroupAddressList().get(i).getMainGroup() +
                                "," + list.get(position).getGroupAddressList().get(i).getMiddleIp() +
                                "," + list.get(position).getGroupAddressList().get(i).getGroupIp() + "]";
                        //判断群组地址是否存在功能数据
                        if (list.get(position).getGroupAddressList().get(i).getFunction().getFunctionId() == null) {
                            break;
                        }
                        int length = list.get(position).getGroupAddressList().get(i).getFunction().getLength();
                        String value = list.get(position).getGroupAddressList().get(i).getFunction().getFunctionCode();
                        String type = list.get(position).getGroupAddressList().get(i).getFunction()
                                .getOperationType().equals("Write") ? "w" : "r";
                        String code = (type.equals("w") ?
                                "{\"id\":0,\"type\":1,\"data\":{\"addr\":" + groupIp + ",\"rw\":\"" + type + "\"," + "\"width\":" + length + ",\"argv\":" + value + "}}"
                                : "{\"id\":0,\"type\":1,\"data\":{\"addr\":" + groupIp + ",\"rw\":\"" + type + "\"}}");
                        String topic = MqttManagement.PUBLISH_TOPIC + VcomData.getInstance().getClientId();
                        Log.e("FFFFFFFFFFFFFFF", "code: " + code);

                        management.publish(topic, code);
                        //根据对应的topic值 发送数据
                        //new Handler().postDelayed(() ->  management.publish(topic, code), 700);
                        //MqttManagement.publish(topic, code);
                        Thread.sleep(700);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                EventBus.getDefault().post(new MessageEvent(MessageEvent.SEND_SUCCESS));
            }
        }).start();

    }

    public void viewGroupAddress() {
        callBack.viewGroupAddress();
    }

    public void viewAdd() {
        callBack.viewAdd();
    }

    public interface MainFVMCallBack {
        void viewGroupAddress();

        void viewAdd();
    }
}
