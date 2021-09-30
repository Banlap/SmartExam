package com.banlap.smartexam.fvm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * @author Banlap on 2021/9/1
 */
public class ActuatorsFVM extends AndroidViewModel {

    private ActuatorsCallBack callBack;

    public ActuatorsFVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(ActuatorsCallBack callBack) {
        this.callBack = callBack;
    }

    public interface ActuatorsCallBack {
        void viewDimming10V();
    }
}
