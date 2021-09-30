package com.banlap.smartexam.fvm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * @author Banlap on 2021/9/18
 */
public class DeviceStatusFVM extends AndroidViewModel {

    private DeviceStatusCallBack callBack;

    public DeviceStatusFVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(DeviceStatusCallBack callBack) {
        this.callBack = callBack;
    }

    public interface DeviceStatusCallBack {

    }
}
