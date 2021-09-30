package com.banlap.smartexam.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Banlap on 2021/6/11
 */
public abstract class BaseBindingAdapter<M, VDB extends ViewDataBinding> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected Context mContext;
    protected List<M> list;

    public BaseBindingAdapter(Context context) {
        this.mContext = context;
        this.list = new ArrayList<>();
    }

    public void setList(List<M> list) {
        this.list = list;
    }

    @LayoutRes
    protected abstract int getLayoutId(int layoutId);

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public static class BaseBindViewHolder extends RecyclerView.ViewHolder{
        public BaseBindViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        VDB vdb = DataBindingUtil.inflate(LayoutInflater.from(mContext), getLayoutId(viewType), parent, false);
        return new BaseBindViewHolder(vdb.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position){
        VDB vdb = DataBindingUtil.getBinding(viewHolder.itemView);
        this.onBindItem(vdb, this.list.get(position), position);
    }

    protected abstract void onBindItem(VDB vdb, M m, int i);

}
