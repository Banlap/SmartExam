package com.banlap.smartexam.fragment;



import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.banlap.smartexam.R;
import com.banlap.smartexam.base.BaseFragment;
import com.banlap.smartexam.databinding.FragmentDeviceBinding;
import com.banlap.smartexam.fvm.DeviceFVM;
import com.banlap.smartexam.request.MessageEvent;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

/**
 * @author Banlap on 2021/8/18
 */
public class DeviceFragment extends BaseFragment<DeviceFVM, FragmentDeviceBinding>
    implements DeviceFVM.DeviceFVMCallBack {

    private final String[] tab_name = {"Actuators series", "Sensor"};

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_device;
    }

    @Override
    protected void initData() {
        FragmentTransaction cleanTransaction = getChildFragmentManager().beginTransaction();
        for(Fragment fragment : getChildFragmentManager().getFragments()) {
            cleanTransaction.remove(fragment);
        }
        cleanTransaction.commitAllowingStateLoss();

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        Fragment actuatorsFragment = new ActuatorsFragment();
        Fragment sensorFragment = new SensorFragment();

        fragmentTransaction.add(R.id.fl_device_list, actuatorsFragment, "0");
        fragmentTransaction.add(R.id.fl_device_list, sensorFragment, "1");

        fragmentTransaction.commitAllowingStateLoss();

        new Handler().postDelayed(() -> showOrHideFragmentByTag("0"), 300);
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);

        getViewDataBinding().setVm(getViewModel());
        getViewModel().setCallBack(this);

        initTabs();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

    }

    private void initTabs() {
        for (String s : tab_name) {
            TabLayout.Tab tab = getViewDataBinding().tlMainTab.newTab();
            tab.setText(s);
            getViewDataBinding().tlMainTab.addTab(tab);
        }

        getViewDataBinding().tlMainTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                for(Fragment fragment : getChildFragmentManager().getFragments()) {
                    if (fragment.getTag() != null) {
                        if (Integer.parseInt(fragment.getTag()) == tab.getPosition()) {
                            showOrHideFragmentByTag(fragment.getTag());
                        }
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     *  显示或隐藏fragment
     * */
    private void showOrHideFragmentByTag(String tag) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        for(Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment.getTag() != null && fragment.getTag().equals(tag)) {
                transaction.show(fragment);
                TabLayout.Tab tab = getViewDataBinding().tlMainTab.getTabAt(Integer.parseInt(tag));
                getViewDataBinding().tlMainTab.selectTab(tab);
            } else {
                transaction.hide(fragment);
            }
        }
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
