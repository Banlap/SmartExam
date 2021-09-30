package com.banlap.smartexam.uivm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * @author Banlap on 2021/8/7
 */
public class MainVM extends AndroidViewModel {

    private MainVMCallBack callBack;

    public MainVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(MainVMCallBack callBack) { this.callBack = callBack; }

    public interface MainVMCallBack {

    }
}
