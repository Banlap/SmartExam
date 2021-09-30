package com.banlap.smartexam.fvm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * @author Banlap on 2021/8/7
 */
public class UserFVM extends AndroidViewModel {

    private UserFVMCallBack callBack;

    public UserFVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(UserFVMCallBack callBack) { this.callBack = callBack; }

    public void viewHelpDoc() {
        callBack.viewHelpDoc();
    }

    public void viewLogout() { callBack.viewLogout(); }

    public interface UserFVMCallBack {
        void viewHelpDoc();
        void viewLogout();
    }
}
