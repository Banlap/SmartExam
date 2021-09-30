package com.banlap.smartexam.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;

import com.banlap.smartexam.R;
import com.banlap.smartexam.VcomData;
import com.banlap.smartexam.base.BaseActivity;
import com.banlap.smartexam.bean.Device;
import com.banlap.smartexam.bean.Function;
import com.banlap.smartexam.bean.Way;
import com.banlap.smartexam.data.FixedData;
import com.banlap.smartexam.databinding.ActivityMainBinding;
import com.banlap.smartexam.fragment.DeviceFragment;
import com.banlap.smartexam.fragment.DeviceStatusFragment;
import com.banlap.smartexam.fragment.MainFragment;
import com.banlap.smartexam.fragment.UserFragment;
import com.banlap.smartexam.request.MessageEvent;
import com.banlap.smartexam.uivm.MainVM;
import com.banlap.smartexam.utils.SPUtil;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity<MainVM, ActivityMainBinding>
        implements MainVM.MainVMCallBack  {

    private final String[] tab_name = {"Main", "Function", "Status", "Help Center"};

    private List<Device> mDeviceList;       //设备列表

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        FragmentTransaction cleanTransaction = getSupportFragmentManager().beginTransaction();
        for(Fragment fragment : getSupportFragmentManager().getFragments()) {
            cleanTransaction.remove(fragment);
        }
        cleanTransaction.commitAllowingStateLoss();

        Fragment mainFragment = new MainFragment();
        Fragment deviceFragment = new DeviceFragment();
        Fragment deviceStatusFragment = new DeviceStatusFragment();
        Fragment userFragment = new UserFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fl_main_fragment, mainFragment, "0");
        fragmentTransaction.add(R.id.fl_main_fragment, deviceFragment, "1");
        fragmentTransaction.add(R.id.fl_main_fragment, deviceStatusFragment, "2");
        fragmentTransaction.add(R.id.fl_main_fragment, userFragment, "3");

        fragmentTransaction.commitAllowingStateLoss();

        new Handler().postDelayed(() -> showOrHideFragmentByTag("0"), 300);

        //初始化设备列表
        mDeviceList = new ArrayList<>();
        mDeviceList = SPUtil.getListValue(this, "DeviceList", Device.class);
        if(mDeviceList.size() == 0) {
            SPUtil.setListValue(this, "DeviceList", FixedData.getInstance().getListData());
        }
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
                for(Fragment fragment : getSupportFragmentManager().getFragments()) {
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for(Fragment fragment : getSupportFragmentManager().getFragments()) {
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
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}