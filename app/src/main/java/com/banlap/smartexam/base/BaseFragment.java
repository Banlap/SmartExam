package com.banlap.smartexam.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

/**
 * @author Banlap on 2021/8/7
 */
public abstract class BaseFragment <VM extends ViewModel, VDB extends ViewDataBinding> extends Fragment {

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //banlap：使用DataBinding 解决建立大量的findViewById
        mViewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        mViewDataBinding.setLifecycleOwner(this);
        init();
        initData();
        initView();

        return mViewDataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressWarnings("unchecked")
    protected void init() {
        //banlap: 通过class反射获取 参数化类型即泛型(比如model文件夹里面的各种实体类)
        Class<VM> vmClass = (Class<VM>) ((ParameterizedType) Objects.requireNonNull(this.getClass().getGenericSuperclass())).getActualTypeArguments()[0];
        mViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(vmClass);
    }

    protected abstract void initData();
    protected abstract void initView();
}
