package com.banlap.smartexam.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.banlap.smartexam.R;
import com.banlap.smartexam.VcomData;
import com.banlap.smartexam.base.BaseFragment;
import com.banlap.smartexam.bean.Device;
import com.banlap.smartexam.bean.Function;
import com.banlap.smartexam.bean.GroupAddress;
import com.banlap.smartexam.bean.Scene;
import com.banlap.smartexam.bean.Way;
import com.banlap.smartexam.databinding.DialogGroupAddressListBinding;
import com.banlap.smartexam.databinding.FragmentActuatorsStatusBinding;
import com.banlap.smartexam.databinding.ItemGroupIpListBinding;
import com.banlap.smartexam.databinding.ItemStatusActuatorsBinding;
import com.banlap.smartexam.databinding.ItemStatusFunctionListBinding;
import com.banlap.smartexam.databinding.ItemStatusWayListBinding;
import com.banlap.smartexam.fvm.ActuatorsStatusFVM;
import com.banlap.smartexam.request.MessageEvent;
import com.banlap.smartexam.request.MqttManagement;
import com.banlap.smartexam.ui.DeviceSettingActivity;
import com.banlap.smartexam.utils.AnimationUtil;
import com.banlap.smartexam.utils.GsonUtil;
import com.banlap.smartexam.utils.PxUtil;
import com.banlap.smartexam.utils.SPUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Banlap on 2021/9/23
 */
public class ActuatorsStatusFragment extends BaseFragment<ActuatorsStatusFVM, FragmentActuatorsStatusBinding>
        implements ActuatorsStatusFVM.ActuatorsStatusCallBack {

    private int wayPos =-1;       //选择Way的pos

    private List<Scene> mSceneList;      //场景列表
    private List<GroupAddress> defaultGroupAddressList;   //已添加的群组地址

    private List<Device> mList;
    private List<Function> mFunctionList;

    private ActuatorsStatusAdapter actuatorsStatusAdapter;
    private WayListAdapter wayListAdapter;
    private FunctionListAdapter functionListAdapter;
    private GroupAddressAdapter groupAddressAdapter;

    private AlertDialog alertDialog;

    private ItemStatusActuatorsBinding statusBinding;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_actuators_status;
    }

    @Override
    protected void initData() {
        mList = new ArrayList<>();
        List<Device> tempList = SPUtil.getListValue(Objects.requireNonNull(getContext()), "DeviceList", Device.class);
        if(tempList.size()>0) {
            for(int i=0; i<tempList.size(); i++) {
                if(tempList.get(i).getDeviceType().equals("AS")) {
                    mList.add(tempList.get(i));
                }
            }
        }

        mFunctionList = new ArrayList<>();
        mSceneList = new ArrayList<>();
        defaultGroupAddressList = new ArrayList<>();
        //获取本地存储内容 场景列表、已添加的群组地址
        mSceneList.addAll(SPUtil.getListValue(getContext(), "SceneList", Scene.class));
        defaultGroupAddressList.addAll(SPUtil.getListValue(getContext(), "GroupIpList", GroupAddress.class));
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);

        getViewDataBinding().setVm(getViewModel());
        getViewModel().setCallBack(this);

        actuatorsStatusAdapter = new ActuatorsStatusAdapter(getContext(),  mList);
        getViewDataBinding().rvActuatorsList.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        getViewDataBinding().rvActuatorsList.setAdapter(actuatorsStatusAdapter);
        actuatorsStatusAdapter.notifyDataSetChanged();
    }

    /*
    * 显示当前fragment时刷新数据
    * */
    @Override
    public void onHiddenChanged(boolean isHidden) {
        super.onHiddenChanged(isHidden);
        if(!isHidden) {
            getViewDataBinding().rlLoading.setVisibility(View.VISIBLE);

            mSceneList.clear();
            defaultGroupAddressList.clear();
            //获取本地存储内容 场景列表、已添加的群组地址
            mSceneList.addAll(SPUtil.getListValue(Objects.requireNonNull(getContext()), "SceneList", Scene.class));
            defaultGroupAddressList.addAll(SPUtil.getListValue(getContext(), "GroupIpList", GroupAddress.class));

            //更新设备的mac值
            List<Device> tempList = SPUtil.getListValue(Objects.requireNonNull(getContext()), "DeviceList", Device.class);
            int tempSize = tempList.size();
            if(tempSize >0) {
                for(int i=0; i<tempSize; i++) {
                    if(tempList.get(i).getDeviceType().equals("AS")) {
                        int size = mList.size();
                        for(int j=0; j<size; j++) {
                            if(tempList.get(i).getDeviceName().equals(mList.get(j).getDeviceName())) {
                                mList.get(j).setDeviceMac(tempList.get(i).getDeviceMac());
                                break;
                            }
                        }

                    }
                }
            }
            getViewModel().queryStatus(mList);
            actuatorsStatusAdapter.notifyDataSetChanged();
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            case MessageEvent.RESPONSE_SUCCESS:
                //Toast.makeText(this, ">>>" + event.msgData, Toast.LENGTH_SHORT).show();
                String data = GsonUtil.getInstance().getValue(event.msgData, "data");
                String gAddress = GsonUtil.getInstance().getValue(data, "g_addr");
                String address = gAddress.substring(gAddress.indexOf("[")+1, gAddress.indexOf("]"));
                String argv = GsonUtil.getInstance().getValue(data, "argv");
                String value = argv.substring(argv.indexOf("[")+1, argv.indexOf(","));

                checkFunctionStatus(address, value);
                //getViewDataBinding().rlLoading.setVisibility(View.GONE);
                break;
            case MessageEvent.RESPONSE_ERROR:
                break;
            case MessageEvent.SEND_SUCCESS:
            case MessageEvent.NOT_OPS:
                getViewDataBinding().rlLoading.setVisibility(View.GONE);
                break;
        }
    }

    /*
     * 检查设备功能状态
     * */
    private void checkFunctionStatus(String groupAddress, String value) {
        int mListSize = mList.size();
        for(int i=0; i<mListSize; i++) {
            if (mList.get(i).getWayList().size() > 0) {
                int wayListSize = mList.get(i).getWayList().size();
                for (int j = 0; j < wayListSize; j++) {
                    if (mList.get(i).getWayList().get(j).isSelect) {
                        int funListSize = mList.get(i).getWayList().get(j).getFunctionList().size();
                        for (int k = 0; k < funListSize; k++) {
                            if (mList.get(i).getWayList().get(j).getFunctionList().get(k).getGroupAddress().equals(groupAddress)) {
                                //以256为最大值 计算 1:100 = value:256
                                int vv = Integer.parseInt(value)*100;
                                BigDecimal bigDecimal =  new BigDecimal(vv);
                                BigDecimal divisor =  new  BigDecimal(256);
                                BigDecimal mix = bigDecimal.divide(divisor,0, RoundingMode.HALF_UP);

                                Log.e("FFFFFFFFFFFFFF", "origin: " + bigDecimal.divide(divisor) + " new: " + mix);
                                if(mList.get(i).getWayList().get(j).getFunctionList().get(k).getFunctionType().equals("Switch")) {
                                    mList.get(i).getWayList().get(j).getFunctionList().get(k).setFunctionValue(Integer.parseInt(value) == 0 ? "OFF" : "ON");
                                    mList.get(i).getWayList().get(j).getFunctionList().get(k).setFunctionCode("[" + Integer.parseInt(value) + ",0]");
                                } else {
                                    mList.get(i).getWayList().get(j).getFunctionList().get(k).setFunctionValue(String.valueOf(mix));
                                    mList.get(i).getWayList().get(j).getFunctionList().get(k).setFunctionCode("[" + mix + ",0]");
                                }
                                actuatorsStatusAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }
            }
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    private static class ActuatorsStatusViewHolder extends RecyclerView.ViewHolder{
        public ActuatorsStatusViewHolder(@NonNull View itemView) { super(itemView); }
    }
    private class ActuatorsStatusAdapter extends RecyclerView.Adapter<ActuatorsStatusFragment.ActuatorsStatusViewHolder> {

        private Context context;
        private List<Device> list;

        public ActuatorsStatusAdapter(Context context, List<Device> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ActuatorsStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemStatusActuatorsBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_status_actuators, parent, false);
            return new ActuatorsStatusViewHolder(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull ActuatorsStatusViewHolder holder, int position) {
            statusBinding = DataBindingUtil.getBinding(holder.itemView);
            if(statusBinding != null) {
                statusBinding.tvDeviceName.setText(list.get(position).getDeviceName());
                statusBinding.tvMacValue.setText(list.get(position).getDeviceMac());

                wayListAdapter = new WayListAdapter(getContext(), list.get(position).getWayList());
                //通道列表居中
                int x = PxUtil.getInstance().px2dp((Objects.requireNonNull(context.getDisplay()).getWidth() - (PxUtil.getInstance().dp2px(40, context) * 4)) / 2, context);
                //statusBinding.rvWayList.setPadding(x, 0, x, 0);
                //statusBinding.rvWayList.setLeftTopRightBottom(x, 0, x, 0);
                statusBinding.rvWayList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                statusBinding.rvWayList.setAdapter(wayListAdapter);
                wayListAdapter.notifyDataSetChanged();

                functionListAdapter = new FunctionListAdapter(context, mFunctionList, statusBinding.tvDeviceName.getText().toString());
                statusBinding.rvFunctionList.setLayoutManager(new LinearLayoutManager(context));
                statusBinding.rvFunctionList.setAdapter(functionListAdapter);
                functionListAdapter.notifyDataSetChanged();

                statusBinding.ivRefresh.clearAnimation();
                //手动点击刷新状态
                statusBinding.llRefreshStatus.setOnClickListener(v->{
                    statusBinding.ivRefresh.startAnimation(AnimationUtil.animationRotate(0, 360, 500));
                    getViewDataBinding().rlLoading.setVisibility(View.VISIBLE);
                    getViewModel().queryStatus(mList);
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private static class WayListViewHolder extends RecyclerView.ViewHolder{
        public WayListViewHolder(@NonNull View itemView) { super(itemView); }
    }
    private class WayListAdapter extends RecyclerView.Adapter<ActuatorsStatusFragment.WayListViewHolder> {

        private Context context;
        private List<Way> list;

        public WayListAdapter(Context context, List<Way> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public WayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemStatusWayListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_status_way_list, parent, false);
            return new WayListViewHolder(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull WayListViewHolder holder, int position) {
            ItemStatusWayListBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if(binding != null) {
                binding.tvWay.setText(list.get(position).getWayName());
                if(list.get(position).isSelect) {
                    wayPos=position;
                    binding.rlWay.setBackgroundResource(R.drawable.shape_button_black);
                    binding.tvWay.setTextColor(getResources().getColor(R.color.white));
                    mFunctionList.clear();
                    mFunctionList.addAll(list.get(position).getFunctionList());
                    functionListAdapter.notifyDataSetChanged();
                } else {
                    binding.rlWay.setBackgroundResource(R.drawable.shape_button_gray_f6);
                    binding.tvWay.setTextColor(getResources().getColor(R.color.black));
                }
                Log.e("FFFFFFFFFFFFFFFFFF",  "pos: " + position + " isSelect: " + list.get(position).isSelect + " size: " + list.get(position).getFunctionList().size() + " name: " + list.get(position).getFunctionList().get(list.get(position).getFunctionList().size() -1).getFunctionName());

                //切换way
                binding.getRoot().setOnClickListener(v -> {
                    selectInvert();
                    list.get(position).isSelect = true;
                    //notifyDataSetChanged();
                    actuatorsStatusAdapter.notifyDataSetChanged();
                });

            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        //反选
        private void selectInvert() {
            for(Way way : list) {
                way.isSelect = false;
            }
        }
    }

    private static class FunctionListViewHolder extends RecyclerView.ViewHolder{
        public FunctionListViewHolder(@NonNull View itemView) { super(itemView); }
    }
    private class FunctionListAdapter extends RecyclerView.Adapter<FunctionListViewHolder> {

        private Context context;
        private List<Function> list;
        private String cDeviceName;
        private int num=0;

        public FunctionListAdapter(Context context, List<Function> list, String cDeviceName) {
            this.context = context;
            this.list = list;
            this.cDeviceName = cDeviceName;
        }

        @NonNull
        @Override
        public FunctionListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemStatusFunctionListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_status_function_list, parent, false);
            return new FunctionListViewHolder(binding.getRoot());
        }

        @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
        @Override
        public void onBindViewHolder(@NonNull FunctionListViewHolder holder, int position) {
            ItemStatusFunctionListBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if(binding != null) {
                num++;
                binding.tvNum.setText(num+".");
                binding.tvFunctionName.setText(list.get(position).getFunctionName());

                //是否显示群组地址
                if(list.get(position).getGroupAddress().equals("")) {
                    binding.llAddress.setVisibility(View.GONE);
                    binding.llSwitchFunction.setVisibility(View.GONE);
                    binding.llBrightnessFunction.setVisibility(View.GONE);
                } else {
                    binding.llAddress.setVisibility(View.VISIBLE);
                    binding.tvGroupAddress.setText(list.get(position).getGroupAddress());

                    //显示对应的功能
                    if (list.get(position).getOperationType().equals("Write")){
                        if(list.get(position).getFunctionType().equals("Switch")) {
                            binding.llSwitchFunction.setVisibility(View.VISIBLE);
                            binding.llBrightnessFunction.setVisibility(View.GONE);
                            binding.tvValue.setText(list.get(position).getFunctionValue());
                            binding.swSwitch.setChecked(list.get(position).getFunctionValue().equals("ON"));
                        } else if(list.get(position).getFunctionType().equals("Brightness")) {
                            binding.llSwitchFunction.setVisibility(View.GONE);
                            binding.llBrightnessFunction.setVisibility(View.VISIBLE);
                            int v = Integer.parseInt(list.get(position).getFunctionValue());
                            binding.tvSbValue.setText(v + "%");
                            binding.sbBrightness.setProgress(v);
                        } else {
                            binding.llSwitchFunction.setVisibility(View.GONE);
                            binding.llBrightnessFunction.setVisibility(View.GONE);
                        }
                    }
                }

                binding.swSwitch.setClickable(false);
                binding.sbBrightness.setOnTouchListener((v, event) -> true);

                //点击选择群组地址
                binding.getRoot().setOnClickListener(v -> {
                    /*
                     * bug: recycleView里面再嵌套的recycleView会有这样的问题：因为
                     *      recycleView的机制是一开始显示屏幕内item数据，屏幕外的数据
                     *      会作为cache保留，当滚动列表后，cache的数据就显示,之后就整个
                     *      列表的数据都显示出来，不再进行onBindViewHolder的方法内容
                     */
                    for(int i=0; i<mList.size(); i++) {
                        if(mList.get(i).getDeviceName().equals(cDeviceName)) {
                            mFunctionList.clear();
                            mFunctionList.addAll(mList.get(i).getWayList().get(wayPos).getFunctionList());
                            actuatorsStatusAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    DialogGroupAddressListBinding groupAddressListBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                            R.layout.dialog_group_address_list, null, false);

                    alertDialog = new AlertDialog.Builder(context)
                            .setView(groupAddressListBinding.getRoot())
                            .create();
                    Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_item_online);

                    //初期化功能可选择的群组地址
                    int sceneSize = mSceneList.size();
                    int defaultSize = defaultGroupAddressList.size();
                    for(int x=0; x<defaultSize; x++) {
                        defaultGroupAddressList.get(x).setEnable(true);
                    }

                    //判断当前功能可选择的群组地址
                    for(int i=0; i<defaultSize; i++) {
                        for(int j=0; j<sceneSize; j++) {
                            if(mSceneList.get(j).getGroupAddressList().size()>0) {
                                boolean isBreak = false;
                                int addressSize =  mSceneList.get(j).getGroupAddressList().size();
                                for(int k=0; k<addressSize; k++) {
                                    if(mSceneList.get(j).getGroupAddressList().get(k).getMainGroup() == defaultGroupAddressList.get(i).getMainGroup()
                                            && mSceneList.get(j).getGroupAddressList().get(k).getMiddleIp() == defaultGroupAddressList.get(i).getMiddleIp()
                                            && mSceneList.get(j).getGroupAddressList().get(k).getGroupIp() == defaultGroupAddressList.get(i).getGroupIp()) {
                                        if(mSceneList.get(j).getGroupAddressList().get(k).getFunction().getFunctionType() !=null) {
                                            if(mSceneList.get(j).getGroupAddressList().get(k).getFunction().getFunctionType().equals(mFunctionList.get(position).getFunctionType())) {
                                                isBreak = true;
                                                defaultGroupAddressList.get(i).setEnable(true);
                                            } else {
                                                defaultGroupAddressList.get(i).setEnable(false);
                                            }
                                        } else {
                                            isBreak = true;
                                            defaultGroupAddressList.get(i).setEnable(true);
                                        }
                                        break;
                                    }
                                }
                                if(isBreak) {
                                    break;
                                }
                            }
                        }
                    }

                    groupAddressAdapter = new GroupAddressAdapter(context, defaultGroupAddressList);

                    groupAddressListBinding.rvGroupList.setLayoutManager(new LinearLayoutManager(context));
                    groupAddressListBinding.rvGroupList.setAdapter(groupAddressAdapter);
                    groupAddressAdapter.notifyDataSetChanged();

                    //点击选择群组地址赋予该功能
                    groupAddressListBinding.tvAddGroupAddress.setOnClickListener(view -> {
                        int mainGroup=0, middleIp=0, groupIp=0;
                        int size = defaultGroupAddressList.size();
                        for(int i=0; i<size; i++) {
                            if(defaultGroupAddressList.get(i).isSelect){
                                mainGroup = defaultGroupAddressList.get(i).getMainGroup();
                                middleIp = defaultGroupAddressList.get(i).getMiddleIp();
                                groupIp = defaultGroupAddressList.get(i).getGroupIp();
                                break;
                            }
                        }
                        //判断是否选择群组地址
                        if(mainGroup==0 && middleIp==0 && groupIp==0) {
                            Toast.makeText(context, "Please select at least one of them", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("FFFFFFFFFFFFFFFFFF", "size: " + list.size() + " position: " + position + " name: " + list.get(list.size()-1).getFunctionName());
                            mFunctionList.get(position).setGroupAddress(mainGroup + "," + middleIp + "," + groupIp);
                            //将已填入的群组地址保存 到 本地缓存
                            List<Device> saveList = SPUtil.getListValue(Objects.requireNonNull(getContext()), "DeviceList", Device.class);
                            int saveListSize = saveList.size();
                            for(int i=0; i<saveListSize; i++) {
                                if(saveList.get(i).getDeviceName().equals(cDeviceName)) {
                                    saveList.get(i).getWayList().get(wayPos).setFunctionList(mFunctionList);
                                }
                            }
                            SPUtil.setListValue(context, "DeviceList", saveList);
                            notifyDataSetChanged();
                            actuatorsStatusAdapter.notifyDataSetChanged();
                            alertDialog.dismiss();
                        }

                    });

                    groupAddressListBinding.tvCancel.setOnClickListener(view -> alertDialog.dismiss());
                    alertDialog.show();

                });

                //对当前的功能删除群组地址
                binding.ivDelete.setOnClickListener(v -> {
                    mFunctionList.get(position).setGroupAddress("");
                    //将删除的群组地址保存 到 本地缓存
                    List<Device> saveList = SPUtil.getListValue(Objects.requireNonNull(getContext()), "DeviceList", Device.class);
                    int saveListSize = saveList.size();
                    for(int i=0; i<saveListSize; i++) {
                        if(saveList.get(i).getDeviceName().equals(cDeviceName)) {
                            saveList.get(i).getWayList().get(wayPos).setFunctionList(mFunctionList);
                        }
                    }
                    SPUtil.setListValue(context, "DeviceList", saveList);
                    notifyDataSetChanged();
                    actuatorsStatusAdapter.notifyDataSetChanged();
                });
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


}
