package com.banlap.smartexam.fvm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.banlap.smartexam.VcomData;
import com.banlap.smartexam.request.MqttManagement;

/**
 * @author Banlap on 2021/8/7
 */
public class DeviceFVM extends AndroidViewModel {

    private DeviceFVMCallBack callBack;
    private MqttManagement management;

    public DeviceFVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(DeviceFVMCallBack callBack) { this.callBack = callBack; }


    public interface DeviceFVMCallBack {

    }
}
