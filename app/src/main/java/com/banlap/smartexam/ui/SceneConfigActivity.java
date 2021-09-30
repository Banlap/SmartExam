package com.banlap.smartexam.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.banlap.smartexam.R;
import com.banlap.smartexam.base.BaseActivity;
import com.banlap.smartexam.bean.Device;
import com.banlap.smartexam.bean.Function;
import com.banlap.smartexam.bean.GroupAddress;
import com.banlap.smartexam.bean.Scene;
import com.banlap.smartexam.bean.Way;
import com.banlap.smartexam.data.FixedData;
import com.banlap.smartexam.databinding.ActivitySceneConfigBinding;
import com.banlap.smartexam.databinding.DialogDeleteAddressBinding;
import com.banlap.smartexam.databinding.DialogGroupAddressListBinding;
import com.banlap.smartexam.databinding.DialogSelectFunctionBinding;
import com.banlap.smartexam.databinding.ItemDeviceListBinding;
import com.banlap.smartexam.databinding.ItemFunctionListBinding;
import com.banlap.smartexam.databinding.ItemGroupIpListBinding;
import com.banlap.smartexam.databinding.ItemSceneGroupIpListBinding;
import com.banlap.smartexam.databinding.ItemWayListBinding;
import com.banlap.smartexam.request.MessageEvent;
import com.banlap.smartexam.uivm.SceneConfigVM;
import com.banlap.smartexam.utils.SPUtil;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Banlap on 2021/8/30
 */
public class SceneConfigActivity extends BaseActivity<SceneConfigVM, ActivitySceneConfigBinding>
        implements SceneConfigVM.SceneConfigCallBack {

    private Scene mScene;
    private List<GroupAddress> defaultGroupAddressList;   //已添加的群组地址
    private List<GroupAddress> groupAddressList;          //场景的群组地址
    private List<GroupAddress> tempGroupAddressList;      //临时选择的群组地址

    private List<Device> devicesList;      //设备功能列表
    private List<Way> wayList;             //四路
    private List<Function> functionList;   //功能列表
    private List<String> strList;          //增加、减少亮度%

    private ListGroupIpAdapter listGroupIpAdapter;
    private GroupAddressAdapter groupAddressAdapter;
    private ChildFunctionAdapter childFunctionAdapter;
    private WayListAdapter wayListAdapter;

    private Way mWay;                   //确定保存的way列表
    private Function mFunction;         //确定保存的function列表

    private AlertDialog alertDialog;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_scene_config;
    }

    @Override
    protected void initData() {
        //获取默认新增的群组地址
        defaultGroupAddressList = new ArrayList<>();
        defaultGroupAddressList.addAll(SPUtil.getListValue(this, "GroupIpList", GroupAddress.class));
        //当前场景的群组地址
        groupAddressList = new ArrayList<>();
        if (getIntent().getSerializableExtra("SceneData") != null) {
            mScene = (Scene) getIntent().getSerializableExtra("SceneData");
            groupAddressList = Objects.requireNonNull(mScene).getGroupAddressList();
        }
        //
        tempGroupAddressList = new ArrayList<>();
        devicesList = new ArrayList<>();
        wayList = new ArrayList<>();
        functionList = new ArrayList<>();
        mWay = new Way();
        mFunction = new Function();
        //
        strList = new ArrayList<>();
        strList = FixedData.getInstance().getRelativeData();
    }

    @Override
    protected void initView() {
        getViewDataBinding().setVm(getViewModel());
        getViewModel().setCallBack(this);

        getViewDataBinding().etSceneName.setText(mScene.getSceneName());
        //
        listGroupIpAdapter = new ListGroupIpAdapter(this, groupAddressList);
        getViewDataBinding().rvFunctionList.setLayoutManager(new LinearLayoutManager(this));
        getViewDataBinding().rvFunctionList.setAdapter(listGroupIpAdapter);
        listGroupIpAdapter.notifyDataSetChanged();
    }

    @Override
    public void viewBack() {
        finish();
    }

    /*
     * 添加群组地址到场景里面
     * */
    @Override
    public void viewAddIP() {
        tempGroupAddressList = new ArrayList<>();

        DialogGroupAddressListBinding groupAddressListBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.dialog_group_address_list, null, false);
        alertDialog = new AlertDialog.Builder(this)
                .setView(groupAddressListBinding.getRoot())
                .create();
        alertDialog.show();

        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_item_online);
        groupAddressListBinding.tvAddGroupAddress.setText(getString(R.string.tv_select_address));

        //显示默认新增的群组地址
        groupAddressAdapter = new GroupAddressAdapter(this, defaultGroupAddressList);
        groupAddressListBinding.rvGroupList.setLayoutManager(new LinearLayoutManager(this));
        groupAddressListBinding.rvGroupList.setAdapter(groupAddressAdapter);
        groupAddressAdapter.notifyDataSetChanged();
        //点击确认添加群组地址
        groupAddressListBinding.tvAddGroupAddress.setOnClickListener(v -> {
            //剔除重复地址添加
            int size = tempGroupAddressList.size();
            if (groupAddressList.size() > 0) {
                for (int i = 0; i < size; i++) {
                    boolean isAdd = true;
                    for (int j = 0; j < groupAddressList.size(); j++) {
                        if (tempGroupAddressList.get(i).getMainGroup() == groupAddressList.get(j).getMainGroup()
                                && tempGroupAddressList.get(i).getMiddleIp() == groupAddressList.get(j).getMiddleIp()
                                && tempGroupAddressList.get(i).getGroupIp() == groupAddressList.get(j).getGroupIp()) {
                            isAdd = false;
                        }
                    }
                    if (isAdd) {
                        groupAddressList.add(tempGroupAddressList.get(i));
                    }
                }
            } else {
                groupAddressList.addAll(tempGroupAddressList);
            }
            listGroupIpAdapter.notifyDataSetChanged();

            alertDialog.dismiss();
            initNotSelectList();
        });
        //取消
        groupAddressListBinding.tvCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
            initNotSelectList();
        });
    }

    /*
     * 点击确认保存
     * */
    @Override
    public void viewConfirm() {
        EventBus.getDefault().post(new MessageEvent(MessageEvent.SAVE_SUCCESS, groupAddressList, getViewDataBinding().etSceneName.getText().toString()));
        finish();
    }

    /*
     * 初始化群组地址列表未选中状态
     * */
    private void initNotSelectList() {
        int listSize = defaultGroupAddressList.size();
        for (int i = 0; i < listSize; i++) {
            defaultGroupAddressList.get(i).isSelect = false;
        }
        groupAddressAdapter.notifyDataSetChanged();
    }

    /*
     * 场景里面的群组地址 适配器
     * */
    private static class ListGroupIpViewHolder extends RecyclerView.ViewHolder {
        public ListGroupIpViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private class ListGroupIpAdapter extends RecyclerView.Adapter<ListGroupIpViewHolder> {

        private Context context;
        private List<GroupAddress> list;
        private int strPos = 0; //设置当前选中的调节亮度position （功能：调节亮度）

        public ListGroupIpAdapter(Context context, List<GroupAddress> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ListGroupIpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSceneGroupIpListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_scene_group_ip_list, parent, false);
            return new ListGroupIpViewHolder(binding.getRoot());
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ListGroupIpViewHolder holder, int position) {
            ItemSceneGroupIpListBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if (binding != null) {
                String ip = list.get(position).getMainGroup() + "/" + list.get(position).getMiddleIp() + "/" + list.get(position).getGroupIp();
                binding.tvGroupIp.setText(ip);
                //是否显示功能
                binding.viewLine.setVisibility(View.GONE);
                binding.llFunction.setVisibility(View.GONE);
                if (list.get(position).getFunction() != null) {
                    if (list.get(position).getFunction().getFunctionName() != null) {
                        binding.viewLine.setVisibility(View.VISIBLE);
                        binding.llFunction.setVisibility(View.VISIBLE);
                        binding.tvFunctionName.setText(list.get(position).getFunction().getFunctionName());
                        //判断哪种功能显示对应的功能界面
                        if (list.get(position).getFunction().getOperationType().equals("Write")) {
                            binding.llFunctionType.setVisibility(View.VISIBLE);
                            if (list.get(position).getFunction().getFunctionType().equals("Switch")) {
                                binding.swSwitch.setVisibility(View.VISIBLE);
                                binding.sbBrightness.setVisibility(View.GONE);
                                binding.spControl.setVisibility(View.GONE);
                                binding.tvValue.setVisibility(View.VISIBLE);
                                Log.e("FFFFFFFFFFFFFFFF", "pos: " + position + " isClick: " + binding.swSwitch.isChecked() + " listSwitch: " + list.get(position).getFunction().getFunctionValue());
                                binding.swSwitch.setChecked(list.get(position).getFunction().getFunctionValue().equals("ON"));
                                binding.tvValue.setText(list.get(position).getFunction().getFunctionValue());
                            } else if (list.get(position).getFunction().getFunctionType().equals("Brightness")) {
                                binding.swSwitch.setVisibility(View.GONE);
                                binding.sbBrightness.setVisibility(View.VISIBLE);
                                binding.spControl.setVisibility(View.GONE);
                                binding.tvValue.setVisibility(View.VISIBLE);
                                Log.e("FFFFFFFFFFFFFFFF", "pos: " + position + " brightness: " + binding.sbBrightness.getProgress() + " listBrightness: " + Integer.parseInt(list.get(position).getFunction().getFunctionValue()));
                                binding.sbBrightness.setProgress(Integer.parseInt(list.get(position).getFunction().getFunctionValue()));
                                binding.tvValue.setText(Integer.parseInt(list.get(position).getFunction().getFunctionValue()) + "%");
                            } else {
                                binding.swSwitch.setVisibility(View.GONE);
                                binding.sbBrightness.setVisibility(View.GONE);
                                binding.spControl.setVisibility(View.VISIBLE);
                                binding.tvValue.setVisibility(View.GONE);

                                //显示调节亮度列表
                                ArrayAdapter aAdapter = new ArrayAdapter(context, R.layout.spinner_default, strList);
                                aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                binding.spControl.setAdapter(aAdapter);

                                switch (list.get(position).getFunction().getFunctionValue()) {
                                    case "+100%": strPos = 0; break;
                                    case "+50%": strPos = 1; break;
                                    case "+25%": strPos = 2; break;
                                    case "+12%": strPos = 3; break;
                                    case "+6%": strPos = 4; break;
                                    case "+3%": strPos = 5; break;
                                    case "+1%": strPos = 6; break;
                                    case "-1%": strPos = 7; break;
                                    case "-3%": strPos = 8; break;
                                    case "-6%": strPos = 9; break;
                                    case "-12%": strPos = 10; break;
                                    case "-25%": strPos = 11; break;
                                    case "-50%": strPos = 12; break;
                                    case "-100%": strPos = 13; break;
                                }
                                binding.spControl.setSelection(strPos);
                                aAdapter.notifyDataSetChanged();

                                binding.spControl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {
                                        strPos = p;
                                        groupAddressList.get(position).getFunction().setFunctionValue(strList.get(p));
                                        switch (strList.get(p)) {
                                            case "+100%": groupAddressList.get(position).getFunction().setFunctionCode("[9,0]"); break;
                                            case "+50%": groupAddressList.get(position).getFunction().setFunctionCode("[10,0]"); break;
                                            case "+25%": groupAddressList.get(position).getFunction().setFunctionCode("[11,0]"); break;
                                            case "+12%": groupAddressList.get(position).getFunction().setFunctionCode("[12,0]"); break;
                                            case "+6%": groupAddressList.get(position).getFunction().setFunctionCode("[13,0]"); break;
                                            case "+3%": groupAddressList.get(position).getFunction().setFunctionCode("[14,0]"); break;
                                            case "+1%": groupAddressList.get(position).getFunction().setFunctionCode("[15,0]"); break;
                                            case "-1%": groupAddressList.get(position).getFunction().setFunctionCode("[7,0]"); break;
                                            case "-3%": groupAddressList.get(position).getFunction().setFunctionCode("[6,0]"); break;
                                            case "-6%": groupAddressList.get(position).getFunction().setFunctionCode("[5,0]"); break;
                                            case "-12%": groupAddressList.get(position).getFunction().setFunctionCode("[4,0]"); break;
                                            case "-25%": groupAddressList.get(position).getFunction().setFunctionCode("[3,0]"); break;
                                            case "-50%": groupAddressList.get(position).getFunction().setFunctionCode("[2,0]"); break;
                                            case "-100%": groupAddressList.get(position).getFunction().setFunctionCode("[1,0]"); break;
                                        }

                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                            }
                        } else {
                            binding.llFunctionType.setVisibility(View.GONE);
                        }
                    }
                }

                //点击选择功能
                binding.llConfig.setOnClickListener(v -> {
                    devicesList = new ArrayList<>();
                    devicesList.addAll(SPUtil.getListValue(context, "DeviceList", Device.class));

                    DialogSelectFunctionBinding selectFunctionBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                            R.layout.dialog_select_function, null, false);
                    alertDialog = new AlertDialog.Builder(context)
                            .setView(selectFunctionBinding.getRoot())
                            .create();
                    alertDialog.show();

                    Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_item_online);

                    //显示设备对应的功能
                    childFunctionAdapter = new ChildFunctionAdapter(context, functionList);
                    selectFunctionBinding.rvFunctionList.setLayoutManager(new LinearLayoutManager(context));
                    selectFunctionBinding.rvFunctionList.setAdapter(childFunctionAdapter);
                    childFunctionAdapter.notifyDataSetChanged();

                    //四路列表 显示对应的功能
                    wayListAdapter = new WayListAdapter(context, wayList);
                    selectFunctionBinding.elvWayList.setAdapter(wayListAdapter);
                    //点击其中一组，关闭其他组
                    selectFunctionBinding.elvWayList.setOnGroupExpandListener(groupPosition -> {
                        int count = selectFunctionBinding.elvWayList.getExpandableListAdapter().getGroupCount();
                        for(int i=0; i<count; i++) {
                            if(i != groupPosition) {
                                selectFunctionBinding.elvWayList.collapseGroup(i);
                            }
                        }
                    });
                    wayListAdapter.notifyDataSetChanged();

                    //加载设备名称列表
                    List<String> list = new ArrayList<>();
                    int size = devicesList.size();
                    for (int i = 0; i < size; i++) {
                        list.add(devicesList.get(i).getDeviceName());
                    }

                    //显示设备的列表
                    ArrayAdapter arrayAdapter = new ArrayAdapter(context, R.layout.spinner_default_2, list);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    selectFunctionBinding.srDeviceList.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();

                    //切换设备选项
                    selectFunctionBinding.srDeviceList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            wayList.clear();
                            functionList.clear();
                            if (devicesList.get(position).getWayList().size() > 0) {
                                selectFunctionBinding.rvFunctionList.setVisibility(View.GONE);
                                selectFunctionBinding.elvWayList.setVisibility(View.VISIBLE);
                                wayList.addAll(devicesList.get(position).getWayList());
                            } else {
                                selectFunctionBinding.rvFunctionList.setVisibility(View.VISIBLE);
                                selectFunctionBinding.elvWayList.setVisibility(View.GONE);
                                functionList.addAll(devicesList.get(position).getFunctionList());
                            }
                            wayListAdapter.notifyDataSetChanged();
                            childFunctionAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    //点击确认选择功能
                    selectFunctionBinding.tvConfirm.setOnClickListener(view -> {
                        int waySize = wayList.size();
                        for(int x=0; x<waySize; x++) {
                            int funSize = wayList.get(x).getFunctionList().size();
                            if(funSize >0) {
                                for(int y=0; y<funSize; y++) {
                                    if(wayList.get(x).getFunctionList().get(y).isSelect) {
                                        mFunction = wayList.get(x).getFunctionList().get(y);
                                    }
                                }
                            }
                        }

                        int listSize = functionList.size();
                        for (int i=0; i<listSize; i++) {
                            if (functionList.get(i).isSelect) {
                                mFunction = functionList.get(i);
                            }
                        }
                        //Toast.makeText(context, "click: " + mFunction.getFunctionName() + mFunction.getFunctionValue(), Toast.LENGTH_SHORT).show();
                        groupAddressList.get(position).setFunction(mFunction);
                        mFunction = new Function();
                        alertDialog.dismiss();
                        notifyDataSetChanged();
                    });
                    //取消
                    selectFunctionBinding.tvCancel.setOnClickListener(view -> {
                        alertDialog.dismiss();
                    });
                });

                //点击打开或关闭 开关
                binding.swSwitch.setOnClickListener(v -> {
                    Log.e("FFFFFFFFFFFFFFFF", "pos: " + position + " isChecked: " + binding.swSwitch.isChecked() + " tvValue: " +  binding.tvValue.getText());
                    if (binding.swSwitch.isChecked()) {
                        groupAddressList.get(position).getFunction().setFunctionValue("ON");
                        groupAddressList.get(position).getFunction().setFunctionCode("[1,0]");
                        binding.tvValue.setText("ON");
                    } else {
                        groupAddressList.get(position).getFunction().setFunctionValue("OFF");
                        groupAddressList.get(position).getFunction().setFunctionCode("[0,0]");
                        binding.tvValue.setText("OFF");
                    }
                });

                //点击拖动滚动条
                binding.sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        //滑动的时候做改变
                        if (fromUser) {
                            int value = progress + 2;
                            binding.tvValue.setText(value + "%");
                            groupAddressList.get(position).getFunction().setFunctionValue("" + value);
                            groupAddressList.get(position).getFunction().setFunctionCode("[" + value * 256 / 100 + ",0]");
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                //点击删除场景下的群组地址对应功能
                binding.llDeleteItem.setOnClickListener(view -> {
                    DialogDeleteAddressBinding deleteAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                            R.layout.dialog_delete_address, null, false);
                    alertDialog = new AlertDialog.Builder(context)
                            .setView(deleteAddressBinding.getRoot())
                            .create();

                    Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_item_online);

                    deleteAddressBinding.tvConfirm.setOnClickListener(v -> {
                        list.remove(position);
                        notifyDataSetChanged();
                        alertDialog.dismiss();
                    });
                    deleteAddressBinding.tvCancel.setOnClickListener(v -> alertDialog.dismiss());

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
     * 选择群组地址 并 添加到场景 适配器
     * */
    private static class GroupAddressViewHolder extends RecyclerView.ViewHolder {
        public GroupAddressViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private class GroupAddressAdapter extends RecyclerView.Adapter<GroupAddressViewHolder> {

        private Context context;
        private List<GroupAddress> list;

        public GroupAddressAdapter(Context context, List<GroupAddress> list) {
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
            if (binding != null) {
                binding.llDelete.setVisibility(View.GONE);

                String ip = list.get(position).getMainGroup() + "/" + list.get(position).getMiddleIp() + "/" + list.get(position).getGroupIp();
                binding.tvGroupIp.setText(ip);

                //默认显示是否选中
                if (list.get(position).isSelect) {
                    binding.rlGroupIp.setBackgroundResource(R.drawable.shape_item_offline);
                } else {
                    binding.rlGroupIp.setBackgroundResource(R.drawable.shape_button_white);
                }

                //点击选中或反选
                binding.getRoot().setOnClickListener(v -> {
                    list.get(position).isSelect = !list.get(position).isSelect;
                    if (list.get(position).isSelect) {
                        tempGroupAddressList.add(list.get(position));
                        binding.rlGroupIp.setBackgroundResource(R.drawable.shape_item_offline);
                    } else {
                        if (tempGroupAddressList.size() > 0) {
                            int index = 0;
                            boolean isRemove = false;
                            for (int i = 0; i < tempGroupAddressList.size(); i++) {
                                if (tempGroupAddressList.get(i).getMainGroup() == list.get(position).getMainGroup()
                                        && tempGroupAddressList.get(i).getMiddleIp() == list.get(position).getMiddleIp()
                                        && tempGroupAddressList.get(i).getGroupIp() == list.get(position).getGroupIp()) {
                                    index = i;
                                    isRemove = true;
                                    break;
                                }
                            }
                            if (isRemove) {
                                tempGroupAddressList.remove(index);
                                binding.rlGroupIp.setBackgroundResource(R.drawable.shape_button_white);
                            }

                        }
                    }
                });
            }
        }


        @Override
        public int getItemCount() {
            return list.size();
        }
    }


    /*
     * 设备对应的功能列表 适配器
     * */
    private static class ChildFunctionViewHolder extends RecyclerView.ViewHolder {
        public ChildFunctionViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private class ChildFunctionAdapter extends RecyclerView.Adapter<ChildFunctionViewHolder> {

        private Context context;
        private List<Function> list;
        private int pos = 0; //设置当前选中的调节亮度position （功能：调节亮度）

        public ChildFunctionAdapter(Context context, List<Function> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ChildFunctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemFunctionListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_function_list, parent, false);
            return new ChildFunctionViewHolder(binding.getRoot());
        }

        @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
        @Override
        public void onBindViewHolder(@NonNull ChildFunctionViewHolder holder, int position) {
            ItemFunctionListBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if (binding != null) {
                //显示对应的功能
                if (list.get(position).getOperationType().equals("Write")) {
                    binding.llActionFunction.setVisibility(View.VISIBLE);
                    binding.llStateFunction.setVisibility(View.GONE);
                    binding.tvActionFunction.setText(list.get(position).getFunctionName());

                    if (list.get(position).getFunctionType().equals("Switch")) {
                        binding.swSwitch.setVisibility(View.VISIBLE);
                        binding.sbBrightness.setVisibility(View.GONE);
                        binding.spControl.setVisibility(View.GONE);
                        binding.tvValue.setVisibility(View.VISIBLE);
                        binding.tvValue.setText(list.get(position).getFunctionValue());
                        binding.swSwitch.setChecked(list.get(position).getFunctionValue().equals("ON"));
                    } else if (list.get(position).getFunctionType().equals("Brightness")) {
                        binding.swSwitch.setVisibility(View.GONE);
                        binding.sbBrightness.setVisibility(View.VISIBLE);
                        binding.spControl.setVisibility(View.GONE);
                        binding.tvValue.setVisibility(View.VISIBLE);

                        binding.tvValue.setText(Integer.parseInt(list.get(position).getFunctionValue()) + "%");
                        binding.sbBrightness.setProgress(Integer.parseInt(list.get(position).getFunctionValue()));
                        if (list.get(position).getFunctionName().equals("Long Press")) {
                            binding.sbBrightness.setMax(15);
                        }
                    } else {
                        binding.swSwitch.setVisibility(View.GONE);
                        binding.sbBrightness.setVisibility(View.GONE);
                        binding.spControl.setVisibility(View.VISIBLE);
                        binding.tvValue.setVisibility(View.GONE);

                        //显示调节亮度列表
                        ArrayAdapter aAdapter = new ArrayAdapter(context, R.layout.spinner_default, strList);
                        aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spControl.setAdapter(aAdapter);
                        binding.spControl.setSelection(pos);
                        aAdapter.notifyDataSetChanged();
                        //
                        binding.spControl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {
                                pos = p;
                                list.get(position).setFunctionValue(strList.get(p));
                                switch (strList.get(p)) {
                                    case "+100%": list.get(position).setFunctionCode("[9,0]"); break;
                                    case "+50%": list.get(position).setFunctionCode("[10,0]"); break;
                                    case "+25%": list.get(position).setFunctionCode("[11,0]"); break;
                                    case "+12%": list.get(position).setFunctionCode("[12,0]"); break;
                                    case "+6%": list.get(position).setFunctionCode("[13,0]"); break;
                                    case "+3%": list.get(position).setFunctionCode("[14,0]"); break;
                                    case "+1%": list.get(position).setFunctionCode("[15,0]"); break;
                                    case "-1%": list.get(position).setFunctionCode("[7,0]"); break;
                                    case "-3%": list.get(position).setFunctionCode("[6,0]"); break;
                                    case "-6%": list.get(position).setFunctionCode("[5,0]"); break;
                                    case "-12%": list.get(position).setFunctionCode("[4,0]"); break;
                                    case "-25%": list.get(position).setFunctionCode("[3,0]"); break;
                                    case "-50%": list.get(position).setFunctionCode("[2,0]"); break;
                                    case "-100%": list.get(position).setFunctionCode("[1,0]"); break;
                                }

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

                //设置当前功能是否禁用
                binding.rlDisableItem.setVisibility(list.get(position).isEnable() ? View.GONE : View.VISIBLE);
                binding.swSwitch.setClickable(list.get(position).isEnable());
                binding.sbBrightness.setOnTouchListener((v, event) -> !list.get(position).isEnable());


                //默认是否勾选状态
                if (list.get(position).isSelect) {
                    binding.ivSelect.setBackgroundResource(R.drawable.ic_check);
                } else {
                    binding.ivSelect.setBackgroundResource(R.drawable.ic_check_not);
                }

                //点击勾选选中功能
                binding.llSelect.setOnClickListener(v -> {
                    //判断是否允许使用该功能
                    if (list.get(position).isEnable()) {
                        selectInvert();
                        list.get(position).isSelect = !list.get(position).isSelect;
                        if (list.get(position).isSelect) {
                            binding.ivSelect.setBackgroundResource(R.drawable.ic_check);
                        } else {
                            binding.ivSelect.setBackgroundResource(R.drawable.ic_check_not);
                        }
                        notifyDataSetChanged();
                    }
                });

                //点击开关
                binding.swSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    binding.tvValue.setText(getString(isChecked ? R.string.tv_on : R.string.tv_off));
                    if (isChecked) {
                        list.get(position).setFunctionValue("ON");
                        list.get(position).setFunctionCode("[1,0]");
                    } else {
                        list.get(position).setFunctionValue("OFF");
                        list.get(position).setFunctionCode("[0,0]");
                    }
                });


                //点击拖动滚动条
                binding.sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        //滑动的时候做改变
                        if (fromUser) {
                            int value = progress + 2;
                            binding.tvValue.setText(value + "%");
                            list.get(position).setFunctionValue("" + value);
                            list.get(position).setFunctionCode("[" + value * 256 / 100 + ",0]");
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }
        }

        //反选
        private void selectInvert() {
            for (Function function : list) {
                function.isSelect = false;
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }


    private class WayListAdapter extends BaseExpandableListAdapter {

        private Context context;
        private List<Way> list;
        private int pos = 0; //设置当前选中的调节亮度position （功能：调节亮度）

        private WayListAdapter(Context context, List<Way> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getGroupCount() { return list.size(); }

        @Override
        public int getChildrenCount(int groupPosition) { return list.get(groupPosition).getFunctionList().size(); }

        @Override
        public Object getGroup(int groupPosition) { return list.get(groupPosition); }

        @Override
        public Object getChild(int groupPosition, int childPosition) { return list.get(groupPosition).getFunctionList().get(childPosition); }

        @Override
        public long getGroupId(int groupPosition) { return groupPosition; }

        @Override
        public long getChildId(int groupPosition, int childPosition) { return childPosition; }

        @Override
        public boolean hasStableIds() { return true; }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ItemWayListBinding wayListBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_way_list, parent, false);
            wayListBinding.tvWay.setText(list.get(groupPosition).getWayName());
            wayListBinding.tvWay.setTextColor(getResources().getColor(R.color.black));
            wayListBinding.ivArrow.setBackgroundResource(isExpanded ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right);
            return wayListBinding.getRoot();
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ItemFunctionListBinding functionListBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_function_list, parent, false);
            //显示对应的功能
            if (list.get(groupPosition).getFunctionList().get(childPosition).getOperationType().equals("Write")) {
                functionListBinding.llActionFunction.setVisibility(View.VISIBLE);
                functionListBinding.llStateFunction.setVisibility(View.GONE);
                functionListBinding.tvActionFunction.setText(list.get(groupPosition).getFunctionList().get(childPosition).getFunctionName());

                if (list.get(groupPosition).getFunctionList().get(childPosition).getFunctionType().equals("Switch")) {
                    functionListBinding.swSwitch.setVisibility(View.VISIBLE);
                    functionListBinding.sbBrightness.setVisibility(View.GONE);
                    functionListBinding.spControl.setVisibility(View.GONE);
                    functionListBinding.tvValue.setVisibility(View.VISIBLE);
                    functionListBinding.tvValue.setText(list.get(groupPosition).getFunctionList().get(childPosition).getFunctionValue());
                    functionListBinding.swSwitch.setChecked(list.get(groupPosition).getFunctionList().get(childPosition).getFunctionValue().equals("ON"));
                } else if (list.get(groupPosition).getFunctionList().get(childPosition).getFunctionType().equals("Brightness")) {
                    functionListBinding.swSwitch.setVisibility(View.GONE);
                    functionListBinding.sbBrightness.setVisibility(View.VISIBLE);
                    functionListBinding.spControl.setVisibility(View.GONE);
                    functionListBinding.tvValue.setVisibility(View.VISIBLE);

                    functionListBinding.tvValue.setText(Integer.parseInt(list.get(groupPosition).getFunctionList().get(childPosition).getFunctionValue()) + "%");
                    functionListBinding.sbBrightness.setProgress(Integer.parseInt(list.get(groupPosition).getFunctionList().get(childPosition).getFunctionValue()));
                    if (list.get(groupPosition).getFunctionList().get(childPosition).getFunctionName().equals("Long Press")) {
                        functionListBinding.sbBrightness.setMax(15);
                    }
                } else {
                    functionListBinding.swSwitch.setVisibility(View.GONE);
                    functionListBinding.sbBrightness.setVisibility(View.GONE);
                    functionListBinding.spControl.setVisibility(View.VISIBLE);
                    functionListBinding.tvValue.setVisibility(View.GONE);

                    //显示调节亮度列表
                    ArrayAdapter aAdapter = new ArrayAdapter(context, R.layout.spinner_default, strList);
                    aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    functionListBinding.spControl.setAdapter(aAdapter);
                    functionListBinding.spControl.setSelection(pos);
                    aAdapter.notifyDataSetChanged();
                    //
                    functionListBinding.spControl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {
                            pos = p;
                            list.get(groupPosition).getFunctionList().get(childPosition).setFunctionValue(strList.get(p));
                            switch (strList.get(p)) {
                                case "+100%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[9,0]");break;
                                case "+50%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[10,0]");break;
                                case "+25%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[11,0]");break;
                                case "+12%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[12,0]");break;
                                case "+6%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[13,0]");break;
                                case "+3%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[14,0]");break;
                                case "+1%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[15,0]");break;
                                case "-1%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[7,0]");break;
                                case "-3%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[6,0]");break;
                                case "-6%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[5,0]");break;
                                case "-12%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[4,0]");break;
                                case "-25%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[3,0]");break;
                                case "-50%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[2,0]");break;
                                case "-100%": list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[1,0]");break;
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
            } else {
                functionListBinding.llActionFunction.setVisibility(View.GONE);
                functionListBinding.llStateFunction.setVisibility(View.VISIBLE);
                functionListBinding.tvStateFunction.setText(list.get(groupPosition).getFunctionList().get(childPosition).getFunctionName());
            }

            //设置当前功能是否禁用
            functionListBinding.rlDisableItem.setVisibility(list.get(groupPosition).getFunctionList().get(childPosition).isEnable() ? View.GONE : View.VISIBLE);
            functionListBinding.swSwitch.setClickable(list.get(groupPosition).getFunctionList().get(childPosition).isEnable());
            functionListBinding.sbBrightness.setOnTouchListener((v, event) -> !list.get(groupPosition).getFunctionList().get(childPosition).isEnable());

            //默认是否勾选状态
            if (list.get(groupPosition).getFunctionList().get(childPosition).isSelect) {
                functionListBinding.ivSelect.setBackgroundResource(R.drawable.ic_check);
            } else {
                functionListBinding.ivSelect.setBackgroundResource(R.drawable.ic_check_not);
            }

            //点击勾选选中功能
            functionListBinding.llSelect.setOnClickListener(v -> {
                //判断是否允许使用该功能
                if (list.get(groupPosition).getFunctionList().get(childPosition).isEnable()) {
                    selectInvert();
                    list.get(groupPosition).getFunctionList().get(childPosition).isSelect = !list.get(groupPosition).getFunctionList().get(childPosition).isSelect;
                    if (list.get(groupPosition).getFunctionList().get(childPosition).isSelect) {
                        functionListBinding.ivSelect.setBackgroundResource(R.drawable.ic_check);
                    } else {
                        functionListBinding.ivSelect.setBackgroundResource(R.drawable.ic_check_not);
                    }
                    notifyDataSetChanged();
                }
            });

            //点击开关
            functionListBinding.swSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                functionListBinding.tvValue.setText(getString(isChecked ? R.string.tv_on : R.string.tv_off));
                if (isChecked) {
                    list.get(groupPosition).getFunctionList().get(childPosition).setFunctionValue("ON");
                    list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[1,0]");
                } else {
                    list.get(groupPosition).getFunctionList().get(childPosition).setFunctionValue("OFF");
                    list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[0,0]");
                }
            });


            //点击拖动滚动条
            functionListBinding.sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //滑动的时候做改变
                    if (fromUser) {
                        int value = progress + 2;
                        functionListBinding.tvValue.setText(value + "%");
                        list.get(groupPosition).getFunctionList().get(childPosition).setFunctionValue("" + value);
                        list.get(groupPosition).getFunctionList().get(childPosition).setFunctionCode("[" + value * 256 / 100 + ",0]");
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });


            return functionListBinding.getRoot();
        }

        //反选
        private void selectInvert() {
            for(Way way : list) {
                for(Function function : way.getFunctionList()) {
                    function.isSelect = false;
                }
            }
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
