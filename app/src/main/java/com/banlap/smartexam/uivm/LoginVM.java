package com.banlap.smartexam.uivm;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.banlap.smartexam.VcomData;
import com.banlap.smartexam.request.MessageEvent;
import com.banlap.smartexam.request.MqttManagement;

import org.greenrobot.eventbus.EventBus;

/**
 * @author Banlap on 2021/8/17
 */
public class LoginVM extends AndroidViewModel {

    private LoginVMCallBack callBack;
    private MqttManagement management;

    public LoginVM(@NonNull Application application) { super(application); }

    public void setCallBack(LoginVMCallBack callBack) { this.callBack = callBack; }

    public void viewConnect() {
        callBack.viewConnect();
    }

    /*判断是否已创建management*/
    public void isConnect() {
        if(VcomData.getInstance().getManagement() !=null) {
            callBack.viewAutoIntoActivity();
        }
    }

    public void connect(String id) {
        management = new MqttManagement(id);
        VcomData.getInstance().setManagement(management);
        if(!VcomData.getInstance().getConnectStatus()) {
            management.connect();
        } else {
            management.disconnect();
        }
    }


    public void disconnect(){
        management = VcomData.getInstance().getManagement();
        management.disconnect();
    }
    public interface LoginVMCallBack {
        void viewConnect();
        void viewAutoIntoActivity();
    }
}
