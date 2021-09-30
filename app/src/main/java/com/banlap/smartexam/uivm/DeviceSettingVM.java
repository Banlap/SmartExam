package com.banlap.smartexam.uivm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * @author Banlap on 2021/9/2
 */
public class DeviceSettingVM extends AndroidViewModel {

    private DeviceSettingCallBack callBack;

    public DeviceSettingVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(DeviceSettingCallBack callBack) {
        this.callBack = callBack;
    }

    public void viewBack() {
        callBack.viewBack();
    }

    public interface DeviceSettingCallBack{
        void viewBack();
    }
}
