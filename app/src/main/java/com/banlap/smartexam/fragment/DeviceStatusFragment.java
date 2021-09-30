package com.banlap.smartexam.fragment;

import android.os.Handler;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.banlap.smartexam.R;
import com.banlap.smartexam.base.BaseActivity;
import com.banlap.smartexam.base.BaseFragment;
import com.banlap.smartexam.databinding.FragmentDeviceStatusBinding;
import com.banlap.smartexam.fvm.DeviceStatusFVM;
import com.google.android.material.tabs.TabLayout;

/**
 * @author Banlap on 2021/9/18
 */
public class DeviceStatusFragment extends BaseFragment<DeviceStatusFVM, FragmentDeviceStatusBinding>
    implements DeviceStatusFVM.DeviceStatusCallBack {

    private final String[] tab_name = {"Actuators series", "Sensor"};
    private String currentTag="";

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_device_status;
    }

    @Override
    protected void initData() {
        FragmentTransaction cleanTransaction = getChildFragmentManager().beginTransaction();
        for(Fragment fragment : getChildFragmentManager().getFragments()) {
            cleanTransaction.remove(fragment);
        }
        cleanTransaction.commitAllowingStateLoss();

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        Fragment actuatorsStatusFragment = new ActuatorsStatusFragment();
        Fragment sensorStatusFragment = new SensorStatusFragment();

        fragmentTransaction.add(R.id.fl_device_list, actuatorsStatusFragment, "0");
        fragmentTransaction.add(R.id.fl_device_list, sensorStatusFragment, "1");

        fragmentTransaction.commitAllowingStateLoss();

        new Handler().postDelayed(() -> showOrHideFragmentByTag("0"), 300);
    }

    @Override
    protected void initView() {
        getViewDataBinding().setVm(getViewModel());
        getViewModel().setCallBack(this);

        initTabs();
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
                currentTag = fragment.getTag();
            } else {
                transaction.hide(fragment);
            }
        }
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onHiddenChanged(boolean isHidden) {
        super.onHiddenChanged(isHidden);
        if(!isHidden) {
            if (!currentTag.equals("")) {
                if (currentTag.equals("0")) {
                    new Handler().postDelayed(() -> showOrHideFragmentByTag("1"), 250);
                    new Handler().postDelayed(() -> showOrHideFragmentByTag("0"), 250);
                } else {
                    new Handler().postDelayed(() -> showOrHideFragmentByTag("0"), 250);
                    new Handler().postDelayed(() -> showOrHideFragmentByTag("1"), 250);
                }
            }
        }
    }

}
