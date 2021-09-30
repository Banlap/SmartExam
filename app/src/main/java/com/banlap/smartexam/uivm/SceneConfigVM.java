package com.banlap.smartexam.uivm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * @author Banlap on 2021/8/30
 */
public class SceneConfigVM extends AndroidViewModel {

    private SceneConfigCallBack callBack;

    public SceneConfigVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(SceneConfigCallBack callBack) {
        this.callBack = callBack;
    }

    public void viewConfirm() {
        callBack.viewConfirm();
    }

    public void viewBack() {
        callBack.viewBack();
    }

    public void viewAddIP() {
        callBack.viewAddIP();
    }

    public interface SceneConfigCallBack{
        void viewBack();
        void viewAddIP();
        void viewConfirm();
    }
}
