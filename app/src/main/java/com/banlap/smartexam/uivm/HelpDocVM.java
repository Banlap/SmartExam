package com.banlap.smartexam.uivm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * @author Banlap on 2021/9/16
 */
public class HelpDocVM extends AndroidViewModel {

    private HelpDocCallBack callBack;

    public HelpDocVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(HelpDocCallBack callBack) {
        this.callBack = callBack;
    }

    public void viewBack() {
        callBack.viewBack();
    }

    public interface HelpDocCallBack {
        void viewBack();
    }
}
