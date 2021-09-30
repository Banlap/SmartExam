package com.banlap.smartexam.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.banlap.smartexam.R;
import com.banlap.smartexam.VcomData;
import com.banlap.smartexam.base.BaseActivity;
import com.banlap.smartexam.bean.Device;
import com.banlap.smartexam.bean.Function;
import com.banlap.smartexam.bean.GroupAddress;
import com.banlap.smartexam.bean.Scene;
import com.banlap.smartexam.bean.Way;
import com.banlap.smartexam.data.FixedData;
import com.banlap.smartexam.databinding.ActivityDeviceSettingBinding;
import com.banlap.smartexam.databinding.DialogEnableFunctionBinding;
import com.banlap.smartexam.databinding.DialogGroupAddressListBinding;
import com.banlap.smartexam.databinding.ItemFunctionListBinding;
import com.banlap.smartexam.databinding.ItemGroupIpListBinding;
import com.banlap.smartexam.databinding.ItemWayListBinding;
import com.banlap.smartexam.request.MessageEvent;
import com.banlap.smartexam.request.MqttManagement;
import com.banlap.smartexam.uivm.DeviceSettingVM;
import com.banlap.smartexam.utils.GsonUtil;
import com.banlap.smartexam.utils.SPUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Banlap on 2021/9/2
 */
public class DeviceSettingActivity extends BaseActivity<DeviceSettingVM, ActivityDeviceSettingBinding>
    implements DeviceSettingVM.DeviceSettingCallBack {

    private int deviceListPos =-1;
    private List<Scene> mSceneList;
    private List<Device> mDeviceList;
    private List<Way> wayList;
    private List<Function> functionList;
    private List<GroupAddress> defaultGroupAddressList;   //已添加的群组地址
    private List<String> strList;          //增加、减少亮度%

    private boolean isType = false;        //标记: 设备类型

    private String mDeviceName="";
    private String mDeviceHint="";
    private String mDeviceId ="";

    private AlertDialog alertDialog;
    private WayListAdapter wayListAdapter;
    private FunctionListAdapter functionListAdapter;
    private FunctionListAdapter otherFunctionListAdapter;
    private GroupAddressAdapter groupAddressAdapter;

    @Override
    protected int getLayoutId() { return R.layout.activity_device_setting; }

    @Override
    protected void initData() {

        mDeviceName = checkIntentData("deviceName")!= null ?  checkIntentData("deviceName") : "";
        mDeviceHint = checkIntentData("deviceHint")!= null ?  checkIntentData("deviceHint") : "";
        mDeviceId = checkIntentData("deviceId")!= null ?  checkIntentData("deviceId") : "";

        mSceneList = new ArrayList<>();
        mSceneList = SPUtil.getListValue(this, "SceneList", Scene.class);

        //显示设备对应的功能
        wayList = new ArrayList<>();
        functionList = new ArrayList<>();
        mDeviceList = new ArrayList<>();
        mDeviceList = SPUtil.getListValue(this, "DeviceList", Device.class);
        int listSize = mDeviceList.size();
        for(int i=0; i<listSize; i++) {
            if(mDeviceList.get(i).getDeviceId().equals(mDeviceId)) {
                deviceListPos = i;
                if(mDeviceList.get(i).getWayList().size()>0){
                    wayList = mDeviceList.get(i).getWayList();
                    isType = true;
                } else {
                    functionList = mDeviceList.get(i).getFunctionList();
                }
                break;
            }
        }
        //显示已新建的群组地址
        defaultGroupAddressList = new ArrayList<>();
        defaultGroupAddressList.addAll(SPUtil.getListValue(this, "GroupIpList", GroupAddress.class));
        //获取相对亮度调整值列表
        strList = new ArrayList<>();
        strList = FixedData.getInstance().getRelativeData();
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);

        getViewDataBinding().setVm(getViewModel());
        getViewModel().setCallBack(this);

        //获取已配置的设备mac地址
        if(mDeviceName!=null && !mDeviceName.equals("")) {
            String[] deviceMac = mDeviceName.split("/");
            getViewDataBinding().etMainGroup.setText(deviceMac[0]);
            getViewDataBinding().etMiddleIp.setText(deviceMac[1]);
            getViewDataBinding().etGroupIp.setText(deviceMac[2]);
        }

        functionListAdapter = new FunctionListAdapter(this, functionList);
        getViewDataBinding().rvFunctionList.setLayoutManager(new LinearLayoutManager(this));
        getViewDataBinding().rvFunctionList.setAdapter(functionListAdapter);
        functionListAdapter.notifyDataSetChanged();

        wayListAdapter = new WayListAdapter(this, wayList);
        getViewDataBinding().rvWayList.setLayoutManager(new LinearLayoutManager(this));
        getViewDataBinding().rvWayList.setAdapter(wayListAdapter);
        wayListAdapter.notifyDataSetChanged();

        getViewDataBinding().rvFunctionList.setVisibility(isType? View.GONE : View.VISIBLE);
        getViewDataBinding().rvWayList.setVisibility(isType? View.VISIBLE : View.GONE);


    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            case MessageEvent.RESPONSE_SUCCESS:
                //Toast.makeText(this, ">>>" + event.msgData, Toast.LENGTH_SHORT).show();
                //String data = GsonUtil.getInstance().getValue(event.msgData, "data");
                //String gAddress = GsonUtil.getInstance().getValue(data, "g_addr");
                //String address = gAddress.substring(gAddress.indexOf("[")+1, gAddress.indexOf("]"));
                //String argv = GsonUtil.getInstance().getValue(data, "argv");
                //String value = argv.substring(argv.indexOf("[")+1, argv.indexOf(","));

                //checkFunctionStatus(address, value);
                //Log.e("SSSSSSSSSSSSSSSSSSS", "data: " + event.msgData +  " address: " + address);
                break;
            case MessageEvent.RESPONSE_ERROR:

                break;
        }
    }

    /*
    * 检查intent传递过来的值
    * */
    private String checkIntentData(String key) { return getIntent().getStringExtra(key); }

    /*
    * 检查设备功能状态
    * */
    private void checkFunctionStatus(String groupAddress, String value) {
        int size = functionList.size();
        for(int i=0; i<size; i++) {
            if(functionList.get(i).getGroupAddress().equals(groupAddress)) {
                //以256为最大值 计算 1:100 = value:256
                int vv = Integer.parseInt(value)*100;
                BigDecimal bigDecimal =  new BigDecimal(vv);
                BigDecimal divisor =  new  BigDecimal(256);
                BigDecimal mix = bigDecimal.divide(divisor,0, RoundingMode.HALF_UP);

                Log.e("FFFFFFFFFFFFFF", "origin: " + bigDecimal.divide(divisor) + " new: " + mix);
                if(functionList.get(i).getFunctionType().equals("Switch")) {
                    functionList.get(i).setFunctionValue(Integer.parseInt(value) == 0 ? "OFF" : "ON");
                    functionList.get(i).setFunctionCode("[" + Integer.parseInt(value) + ",0]");
                } else {
                    functionList.get(i).setFunctionValue(String.valueOf(mix));
                    functionList.get(i).setFunctionCode("[" + mix + ",0]");
                }
                functionListAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    /*
    * 点击返回处理并保存地址
    * */
    @Override
    public void viewBack() {
        if(mDeviceList.size()>0) {
            if(getViewDataBinding().etMainGroup.getText().toString().equals("")
                && getViewDataBinding().etMiddleIp.getText().toString().equals("")
                && getViewDataBinding().etGroupIp.getText().toString().equals("") ) {

                getViewDataBinding().etMainGroup.setBackgroundResource(R.drawable.shape_button_gray_f6);
                getViewDataBinding().etMiddleIp.setBackgroundResource(R.drawable.shape_button_gray_f6);
                getViewDataBinding().etGroupIp.setBackgroundResource(R.drawable.shape_button_gray_f6);

                mDeviceList.get(deviceListPos).setDeviceMac("");
                SPUtil.setListValue(this, "DeviceList", mDeviceList);
                finish();
            } else if (!getViewDataBinding().etMainGroup.getText().toString().equals("")
                    && !getViewDataBinding().etMiddleIp.getText().toString().equals("")
                    && !getViewDataBinding().etGroupIp.getText().toString().equals("")) {

                int mg = Integer.parseInt(getViewDataBinding().etMainGroup.getText().toString());
                int mi = Integer.parseInt(getViewDataBinding().etMiddleIp.getText().toString());
                int gi = Integer.parseInt(getViewDataBinding().etGroupIp.getText().toString());

                if (mg < 1 || mg > 31) {
                    Toast.makeText(this, "Please enter 1-31 range", Toast.LENGTH_SHORT).show();
                    getViewDataBinding().etMainGroup.setBackgroundResource(R.drawable.shape_item_error);
                }
                if (mi < 1 || mi > 7) {
                    Toast.makeText(this, "Please enter 1-7 range", Toast.LENGTH_SHORT).show();
                    getViewDataBinding().etMiddleIp.setBackgroundResource(R.drawable.shape_item_error);
                }
                if (gi < 1 || gi > 255) {
                    Toast.makeText(this, "Please enter 1-255 range", Toast.LENGTH_SHORT).show();
                    getViewDataBinding().etGroupIp.setBackgroundResource(R.drawable.shape_item_error);
                }
                if(mg>=1 && mg <=31 && mi >=1 && mi<=7 && gi >=1 && gi <=255) {
                    getViewDataBinding().etMainGroup.setBackgroundResource(R.drawable.shape_button_gray_f6);
                    getViewDataBinding().etMiddleIp.setBackgroundResource(R.drawable.shape_button_gray_f6);
                    getViewDataBinding().etGroupIp.setBackgroundResource(R.drawable.shape_button_gray_f6);

                    String mac = getViewDataBinding().etMainGroup.getText() + "/" + getViewDataBinding().etMiddleIp.getText() + "/" + getViewDataBinding().etGroupIp.getText();
                    mDeviceList.get(deviceListPos).setDeviceMac(mac);
                    SPUtil.setListValue(this, "DeviceList", mDeviceList);
                    finish();
                }
            } else {
                if (getViewDataBinding().etMainGroup.getText().toString().equals("")) {
                    getViewDataBinding().etMainGroup.setBackgroundResource(R.drawable.shape_item_error);
                }
                if (getViewDataBinding().etMiddleIp.getText().toString().equals("")) {
                    getViewDataBinding().etMiddleIp.setBackgroundResource(R.drawable.shape_item_error);
                }
                if (getViewDataBinding().etGroupIp.getText().toString().equals("")) {
                    getViewDataBinding().etGroupIp.setBackgroundResource(R.drawable.shape_item_error);
                }
                Toast.makeText(this, "Please enter complete information", Toast.LENGTH_SHORT).show();
            }
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    /*
    * 返回 保存当前设备名称
    * */
    @Override
    public void onBackPressed(){
        if(mDeviceList.size()>0) {
            if(getViewDataBinding().etMainGroup.getText().toString().equals("")
                    && getViewDataBinding().etMiddleIp.getText().toString().equals("")
                    && getViewDataBinding().etGroupIp.getText().toString().equals("") ) {

                getViewDataBinding().etMainGroup.setBackgroundResource(R.drawable.shape_button_gray_f6);
                getViewDataBinding().etMiddleIp.setBackgroundResource(R.drawable.shape_button_gray_f6);
                getViewDataBinding().etGroupIp.setBackgroundResource(R.drawable.shape_button_gray_f6);

                mDeviceList.get(deviceListPos).setDeviceMac("");
                SPUtil.setListValue(this, "DeviceList", mDeviceList);
                super.onBackPressed();
            } else if (!getViewDataBinding().etMainGroup.getText().toString().equals("")
                    && !getViewDataBinding().etMiddleIp.getText().toString().equals("")
                    && !getViewDataBinding().etGroupIp.getText().toString().equals("")) {

                int mg = Integer.parseInt(getViewDataBinding().etMainGroup.getText().toString());
                int mi = Integer.parseInt(getViewDataBinding().etMiddleIp.getText().toString());
                int gi = Integer.parseInt(getViewDataBinding().etGroupIp.getText().toString());

                if (mg < 1 || mg > 31) {
                    Toast.makeText(this, "Please enter 1-31 range", Toast.LENGTH_SHORT).show();
                    getViewDataBinding().etMainGroup.setBackgroundResource(R.drawable.shape_item_error);
                }
                if (mi < 1 || mi > 7) {
                    Toast.makeText(this, "Please enter 1-7 range", Toast.LENGTH_SHORT).show();
                    getViewDataBinding().etMiddleIp.setBackgroundResource(R.drawable.shape_item_error);
                }
                if (gi < 1 || gi > 255) {
                    Toast.makeText(this, "Please enter 1-255 range", Toast.LENGTH_SHORT).show();
                    getViewDataBinding().etGroupIp.setBackgroundResource(R.drawable.shape_item_error);
                }
                if(mg>=1 && mg <=31 && mi >=1 && mi<=7 && gi >=1 && gi <=255) {
                    getViewDataBinding().etMainGroup.setBackgroundResource(R.drawable.shape_button_gray_f6);
                    getViewDataBinding().etMiddleIp.setBackgroundResource(R.drawable.shape_button_gray_f6);
                    getViewDataBinding().etGroupIp.setBackgroundResource(R.drawable.shape_button_gray_f6);

                    String mac = getViewDataBinding().etMainGroup.getText() + "/" + getViewDataBinding().etMiddleIp.getText() + "/" + getViewDataBinding().etGroupIp.getText();
                    mDeviceList.get(deviceListPos).setDeviceMac(mac);
                    SPUtil.setListValue(this, "DeviceList", mDeviceList);
                    super.onBackPressed();
                }
            } else {
                if (getViewDataBinding().etMainGroup.getText().toString().equals("")) {
                    getViewDataBinding().etMainGroup.setBackgroundResource(R.drawable.shape_item_error);
                }
                if (getViewDataBinding().etMiddleIp.getText().toString().equals("")) {
                    getViewDataBinding().etMiddleIp.setBackgroundResource(R.drawable.shape_item_error);
                }
                if (getViewDataBinding().etGroupIp.getText().toString().equals("")) {
                    getViewDataBinding().etGroupIp.setBackgroundResource(R.drawable.shape_item_error);
                }
                Toast.makeText(this, "Please enter complete information", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
    *  四路通道 列表
    * */
    private static class WayListViewHolder extends RecyclerView.ViewHolder {
        public WayListViewHolder(@NonNull View itemView) { super(itemView); }
    }
    private class WayListAdapter extends RecyclerView.Adapter<WayListViewHolder> {

        private Context context;
        private List<Way> list;

        public WayListAdapter(Context context, List<Way> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public WayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemWayListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_way_list, parent, false);
            return new WayListViewHolder(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull WayListViewHolder holder, int position) {
            ItemWayListBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if(binding!=null) {
                binding.tvWay.setText(list.get(position).getWayName());

                binding.getRoot().setOnClickListener(v -> {
                    DialogEnableFunctionBinding enableFunctionBinding = DataBindingUtil.inflate(LayoutInflater.from(context)
                        , R.layout.dialog_enable_function, null, false);
                    alertDialog = new AlertDialog.Builder(context)
                        .setView(enableFunctionBinding.getRoot())
                        .create();

                    Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_item_online);
                    enableFunctionBinding.tvCancel.setOnClickListener(view ->  alertDialog.dismiss());

                    otherFunctionListAdapter = new FunctionListAdapter(context, list.get(position).getFunctionList(), position);
                    enableFunctionBinding.rvFunctionList.setLayoutManager(new LinearLayoutManager(context));
                    enableFunctionBinding.rvFunctionList.setAdapter(otherFunctionListAdapter);
                    otherFunctionListAdapter.notifyDataSetChanged();

                    alertDialog.show();
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }


    /*
    *  功能列表
    * */
    private static class FunctionListViewHolder extends RecyclerView.ViewHolder {
        public FunctionListViewHolder(@NonNull View itemView) { super(itemView); }
    }
    private class FunctionListAdapter extends RecyclerView.Adapter<FunctionListViewHolder> {

        private Context context;
        private List<Function> list;
        private int pos=-1;

        public FunctionListAdapter(Context context, List<Function> list) {
            this.context = context;
            this.list = list;
        }

        public FunctionListAdapter(Context context, List<Function> list, int pos) {
            this.context = context;
            this.list = list;
            this.pos = pos;
        }

        @NonNull
        @Override
        public FunctionListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemFunctionListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_function_list, parent, false);
            return new FunctionListViewHolder(binding.getRoot());
        }

        @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
        @Override
        public void onBindViewHolder(@NonNull FunctionListViewHolder holder, int position) {
            ItemFunctionListBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if(binding != null) {
                binding.rlFunction.setVisibility(View.GONE);
                //是否显示
                binding.ivSelect.setBackgroundResource(list.get(position).isEnable() ? R.drawable.ic_item_show : R.drawable.ic_item_hide);
                //显示对应的功能
                if(list.get(position).getOperationType().equals("Write")) {
                    binding.llActionFunction.setVisibility(View.VISIBLE);
                    binding.llStateFunction.setVisibility(View.GONE);
                    binding.tvActionFunction.setText(list.get(position).getFunctionName());

                    if(list.get(position).getFunctionType().equals("Switch")) {
                        binding.swSwitch.setVisibility(View.VISIBLE);
                        binding.sbBrightness.setVisibility(View.GONE);
                        binding.spControl.setVisibility(View.GONE);
                        binding.tvValue.setVisibility(View.VISIBLE);
                        binding.tvValue.setText(list.get(position).getFunctionValue());
                        binding.swSwitch.setChecked(list.get(position).getFunctionValue().equals("ON"));
                    } else if(list.get(position).getFunctionType().equals("Brightness")) {
                        binding.swSwitch.setVisibility(View.GONE);
                        binding.sbBrightness.setVisibility(View.VISIBLE);
                        binding.spControl.setVisibility(View.GONE);
                        binding.tvValue.setVisibility(View.VISIBLE);
                        int v = Integer.parseInt(list.get(position).getFunctionValue());
                        binding.tvValue.setText(v + "%");
                        binding.sbBrightness.setProgress(v);
                    } else {
                        binding.swSwitch.setVisibility(View.GONE);
                        binding.sbBrightness.setVisibility(View.GONE);
                        binding.spControl.setVisibility(View.VISIBLE);
                        binding.tvValue.setVisibility(View.GONE);
                        binding.tvValue.setText(list.get(position).getFunctionValue());

                        //显示调节亮度列表
                        ArrayAdapter aAdapter = new ArrayAdapter<String>(context, R.layout.spinner_default, strList);
                        aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spControl.setAdapter(aAdapter);
                        aAdapter.notifyDataSetChanged();

                        binding.spControl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {
                                pos = p;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                } else {
                    binding.llActionFunction.setVisibility(View.GONE);
                    binding.llStateFunction.setVisibility(View.VISIBLE);
                    binding.tvStateFunction.setText(list.get(position).getFunctionName());
                }

                //设置无法点击修改
                binding.swSwitch.setClickable(false);
                binding.sbBrightness.setOnTouchListener((v, event) -> true);
                binding.spControl.setClickable(false);


                //是否显示功能对应的群组地址
                /*if(list.get(position).getGroupAddress().equals("")) {
                    binding.llFunction.setVisibility(View.GONE);
                    binding.tvGroupIp.setText("");
                    binding.rlDisableFunction.setVisibility(View.VISIBLE);
                } else {
                    binding.llFunction.setVisibility(View.VISIBLE);
                    String[] groupAddress = list.get(position).getGroupAddress().split(",");
                    binding.tvGroupIp.setText(groupAddress[0] + "/" + groupAddress[1] + "/" + groupAddress[2]);
                    binding.rlDisableFunction.setVisibility(View.GONE);
                }*/

                //点击禁用该功能
                binding.llSelect.setOnClickListener(v -> {
                    list.get(position).setEnable(!list.get(position).isEnable());
                    if(mDeviceList.get(deviceListPos).getWayList().size()>0) {
                        if(pos != -1) {
                            mDeviceList.get(deviceListPos).getWayList().get(pos).getFunctionList().get(position).setEnable(list.get(position).isEnable());
                        }
                    } else {
                        mDeviceList.get(deviceListPos).getFunctionList().get(position).setEnable(list.get(position).isEnable());
                    }
                    SPUtil.setListValue(context, "DeviceList", mDeviceList);
                    notifyDataSetChanged();
                });


                /*//点击删除功能对应的群组地址
                binding.ivDelete.setOnClickListener(v -> {
                    list.get(position).setGroupAddress("");
                    mDeviceList.get(deviceListPos).getFunctionList().get(position).setGroupAddress("");
                    SPUtil.setListValue(context, "DeviceList", mDeviceList);
                    notifyDataSetChanged();
                });*/
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private static class GroupAddressViewHolder extends RecyclerView.ViewHolder {
        public GroupAddressViewHolder(@NonNull View itemView) { super(itemView); }
    }
    private class GroupAddressAdapter extends RecyclerView.Adapter<GroupAddressViewHolder> {

        private Context context;
        private List<GroupAddress> list;
        public GroupAddressAdapter(Context context,  List<GroupAddress> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public GroupAddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemGroupIpListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_group_ip_list, parent, false);
            return new GroupAddressViewHolder(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull GroupAddressViewHolder holder, int position) {
            ItemGroupIpListBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if(binding != null) {
                String ip = list.get(position).getMainGroup() + "/" + list.get(position).getMiddleIp() + "/" + list.get(position).getGroupIp();
                binding.tvGroupIp.setText(ip);
                binding.llDelete.setVisibility(View.GONE);

                if(list.get(position).isEnable()) {
                    binding.rlGroupIp.setBackgroundResource(list.get(position).isSelect? R.drawable.shape_item_offline : R.drawable.shape_button_white);
                } else {
                    binding.rlGroupIp.setBackgroundResource(R.drawable.shape_button_gray);
                }

                //点击选中群组地址
                binding.getRoot().setOnClickListener(v -> {
                    if(list.get(position).isEnable()) {
                        selectInvert();
                        list.get(position).isSelect = !list.get(position).isSelect;
                        if(list.get(position).isSelect) {
                            binding.rlGroupIp.setBackgroundResource(R.drawable.shape_item_offline);
                        } else {
                            binding.rlGroupIp.setBackgroundResource(R.drawable.shape_button_white);
                        }
                        notifyDataSetChanged();
                    }
                });

            }
        }

        //反选
        private void selectInvert() {
            for(GroupAddress groupAddress : list) {
                groupAddress.isSelect = false;
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    /**
     *  输入框点击空白处收回键盘 处理触摸事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点）
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = { 0, 0 };
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 多种隐藏软件盘方法的其中一种
     *
     * @param token
     */
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
