package com.banlap.smartexam.fragment;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.banlap.smartexam.R;
import com.banlap.smartexam.VcomData;
import com.banlap.smartexam.base.BaseFragment;
import com.banlap.smartexam.databinding.FragmentUserBinding;
import com.banlap.smartexam.fvm.UserFVM;
import com.banlap.smartexam.request.MessageEvent;
import com.banlap.smartexam.request.MqttManagement;
import com.banlap.smartexam.ui.HelpDocActivity;
import com.banlap.smartexam.ui.LoginActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

/**
 * @author Banlap on 2021/8/7
 */
public class UserFragment extends BaseFragment<UserFVM, FragmentUserBinding>
    implements UserFVM.UserFVMCallBack {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_user;
    }

    @Override
    protected void initData() { }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);

        getViewDataBinding().setVm(getViewModel());
        getViewModel().setCallBack(this);

        getViewDataBinding().tvTcpValue.setText(VcomData.getInstance().getIp());
        getViewDataBinding().tvPortValue.setText(VcomData.getInstance().getPort());
        getViewDataBinding().tvClientIdValue.setText(VcomData.getInstance().getClientId());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            case MessageEvent.CONNECT_SUCCESS:
            case MessageEvent.CONNECT_ERROR:
            case MessageEvent.CONNECT_LOST:
                break;
            case MessageEvent.RESPONSE_SUCCESS:
                getViewDataBinding().tvLogMessage.append(event.msgData + "\n");
                break;
            case MessageEvent.RESPONSE_ERROR:
                break;
        }
    }

    @Override
    public void viewHelpDoc() {
        Intent intent = new Intent(getActivity(), HelpDocActivity.class);
        startActivity(intent);
    }

    @Override
    public void viewLogout() {
        VcomData.getInstance().getManagement().disconnect();
        VcomData.getInstance().setManagement(null);
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }


}
