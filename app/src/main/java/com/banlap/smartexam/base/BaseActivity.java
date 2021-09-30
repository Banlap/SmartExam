package com.banlap.smartexam.base;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

/**
 * @author Banlap on 2021/6/7
 */
public abstract class BaseActivity<VM extends ViewModel, VDB extends ViewDataBinding> extends AppCompatActivity {

    protected VDB mViewDataBinding;
    protected VM mViewModel;

    public VDB getViewDataBinding() {
        return mViewDataBinding;
    }
    public VM getViewModel() {
        return mViewModel;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        //banlap：使用DataBinding 解决建立大量的findViewById
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId());
        mViewDataBinding.setLifecycleOwner(this);
        init();
        initData();
        initView();
    }

    @SuppressWarnings("unchecked")
    protected void init() {
        //banlap: 通过class反射获取 参数化类型即泛型(比如model文件夹里面的各种实体类)
        Class<VM> vmClass = (Class<VM>) ((ParameterizedType) Objects.requireNonNull(this.getClass().getGenericSuperclass())).getActualTypeArguments()[0];
        mViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(vmClass);
    }

    protected abstract void initData();
    protected abstract void initView();

}
