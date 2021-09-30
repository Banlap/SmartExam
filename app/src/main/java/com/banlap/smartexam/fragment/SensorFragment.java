package com.banlap.smartexam.fragment;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.banlap.smartexam.R;
import com.banlap.smartexam.base.BaseFragment;
import com.banlap.smartexam.bean.Device;
import com.banlap.smartexam.databinding.FragmentSensorBinding;
import com.banlap.smartexam.databinding.ItemDeviceBinding;
import com.banlap.smartexam.fvm.SensorFVM;
import com.banlap.smartexam.ui.DeviceSettingActivity;
import com.banlap.smartexam.utils.SPUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Banlap on 2021/9/1
 */
public class SensorFragment extends BaseFragment<SensorFVM, FragmentSensorBinding>
    implements SensorFVM.SensorCallBack {

    private List<Device> mList;
    private SensorAdapter sensorAdapter;

    @Override
    protected int getLayoutId() { return R.layout.fragment_sensor; }

    @Override
    protected void initData() {
        mList = new ArrayList<>();
        List<Device> tempList = SPUtil.getListValue(Objects.requireNonNull(getContext()), "DeviceList", Device.class);
        if(tempList.size()>0) {
            for(int i=0; i<tempList.size(); i++) {
                if(tempList.get(i).getDeviceType().equals("S")) {
                    mList.add(tempList.get(i));
                }
            }
        }
    }

    @Override
    protected void initView() {
        getViewDataBinding().setVm(getViewModel());
        getViewModel().setCallBack(this);

        sensorAdapter = new SensorAdapter(getContext(), mList);
        getViewDataBinding().rvSensorList.setLayoutManager(new LinearLayoutManager(getContext()));
        getViewDataBinding().rvSensorList.setAdapter(sensorAdapter);
        sensorAdapter.notifyDataSetChanged();

    }

    @Override
    public void onResume() {
        super.onResume();
        getViewDataBinding().rlLoading.setVisibility(View.GONE);

        mList.clear();
        List<Device> tempList = SPUtil.getListValue(Objects.requireNonNull(getContext()), "DeviceList", Device.class);
        if(tempList.size()>0) {
            for(int i=0; i<tempList.size(); i++) {
                if(tempList.get(i).getDeviceType().equals("S")) {
                    mList.add(tempList.get(i));
                }
            }
        }
        sensorAdapter.notifyDataSetChanged();
    }


    private static class SensorViewHolder extends RecyclerView.ViewHolder{
        public SensorViewHolder(@NonNull View itemView) { super(itemView); }
    }
    private class SensorAdapter extends RecyclerView.Adapter<SensorFragment.SensorViewHolder>{

        private Context context;
        private List<Device> list;

        public SensorAdapter(Context context, List<Device> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public SensorFragment.SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDeviceBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_device, parent, false);
            return new SensorFragment.SensorViewHolder(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull SensorFragment.SensorViewHolder holder, int position) {
            ItemDeviceBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if(binding !=null) {
                binding.tvDeviceName.setText(list.get(position).getDeviceName());
                binding.tvMacValue.setText(list.get(position).getDeviceMac());
                //点击进入设备功能设置
                binding.getRoot().setOnClickListener(v -> {
                    getViewDataBinding().rlLoading.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(context, DeviceSettingActivity.class);
                    intent.putExtra("deviceName", binding.tvMacValue.getText());
                    intent.putExtra("deviceHint", binding.tvDeviceName.getText());
                    intent.putExtra("deviceId", list.get(position).getDeviceId());
                    intent.putExtra("functionList", (Serializable) list.get(position).getFunctionList());
                    context.startActivity(intent);
                    //getViewDataBinding().rlLoading.setVisibility(View.GONE);
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
