package com.banlap.smartexam.ui;

import com.banlap.smartexam.R;
import com.banlap.smartexam.base.BaseActivity;
import com.banlap.smartexam.databinding.ActivityHelpDocBinding;
import com.banlap.smartexam.uivm.HelpDocVM;

/**
 * @author Banlap on 2021/9/16
 */
public class HelpDocActivity extends BaseActivity<HelpDocVM, ActivityHelpDocBinding>
        implements HelpDocVM.HelpDocCallBack {

    @Override
    protected int getLayoutId() { return R.layout.activity_help_doc; }

    @Override
    protected void initData() {
        getViewDataBinding().setVm(getViewModel());
        getViewModel().setCallBack(this);
    }

    @Override
    protected void initView() {

    }

    @Override
    public void viewBack() { finish(); }
}
