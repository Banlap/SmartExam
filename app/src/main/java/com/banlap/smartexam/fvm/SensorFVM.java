package com.banlap.smartexam.fvm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * @author Banlap on 2021/9/1
 */
public class SensorFVM extends AndroidViewModel {

    private SensorCallBack callBack;

    public SensorFVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(SensorCallBack callBack) {
        this.callBack = callBack;
    }
    public interface SensorCallBack {
    }
}
