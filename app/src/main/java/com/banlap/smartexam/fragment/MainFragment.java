package com.banlap.smartexam.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
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
import com.banlap.smartexam.databinding.DialogAddGroupAddressBinding;
import com.banlap.smartexam.databinding.DialogAddSceneBinding;
import com.banlap.smartexam.databinding.DialogDeleteAddressBinding;
import com.banlap.smartexam.databinding.DialogDeleteSceneBinding;
import com.banlap.smartexam.databinding.DialogGroupAddressListBinding;
import com.banlap.smartexam.databinding.DialogSceneConfigBinding;
import com.banlap.smartexam.databinding.DialogSelectFunctionBinding;
import com.banlap.smartexam.databinding.FragmentMainBinding;
import com.banlap.smartexam.databinding.ItemDeviceListBinding;
import com.banlap.smartexam.databinding.ItemFunctionListBinding;
import com.banlap.smartexam.databinding.ItemGroupIpListBinding;
import com.banlap.smartexam.databinding.ItemSceneListBinding;
import com.banlap.smartexam.fvm.MainFVM;
import com.banlap.smartexam.request.MessageEvent;
import com.banlap.smartexam.request.MqttManagement;
import com.banlap.smartexam.ui.SceneConfigActivity;
import com.banlap.smartexam.utils.AnimationUtil;
import com.banlap.smartexam.utils.GsonUtil;
import com.banlap.smartexam.utils.SPUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Banlap on 2021/8/7
 */
public class MainFragment extends BaseFragment<MainFVM, FragmentMainBinding>
        implements MainFVM.MainFVMCallBack {

    private SceneAdapter sceneAdapter;
    private GroupAddressAdapter groupAddressAdapter;

    private AlertDialog alertDialog;
    private AlertDialog childAlertDialog;

    private List<Scene> mSceneList;
    private List<GroupAddress> mGroupAddressList;    //已建立的群组地址列表

    private int mPosition=0;  //点击的场景position

    private boolean isAllowClick = true;    //判断是否在运行场景 运行时不允许任何点击操作

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initData() {
        //初始化
        mSceneList = new ArrayList<>();
        mSceneList.addAll(SPUtil.getListValue(Objects.requireNonNull(getActivity()), "SceneList", Scene.class));

        mGroupAddressList = new ArrayList<>();
        mGroupAddressList.addAll(SPUtil.getListValue(Objects.requireNonNull(getActivity()), "GroupIpList", GroupAddress.class));
    }


    @Override
    protected void initView() {
        EventBus.getDefault().register(this);

        getViewDataBinding().setVm(getViewModel());
        getViewModel().setCallBack(this);

        getViewDataBinding().rlLoading.setVisibility(View.GONE);

        getViewDataBinding().ivIsConnect.setBackground(
            getResources().getDrawable(VcomData.getInstance().getConnectStatus() ? R.drawable.ic_connected : R.drawable.ic_disconnect
        ));

        sceneAdapter = new SceneAdapter(getActivity(), mSceneList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        getViewDataBinding().rvSceneList.setLayoutManager(layoutManager);
        getViewDataBinding().rvSceneList.setAdapter(sceneAdapter);
        sceneAdapter.notifyDataSetChanged();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            case MessageEvent.CONNECT_READY:
                Toast.makeText(getContext(), "Connecting", Toast.LENGTH_SHORT).show();
                break;
            case MessageEvent.CONNECT_SUCCESS:
                Toast.makeText(getContext(), "Connect Succeed", Toast.LENGTH_SHORT).show();
                getViewDataBinding().ivIsConnect.setBackground(getResources().getDrawable(R.drawable.ic_connected));
                VcomData.getInstance().setConnectStatus(true);
                break;
            case MessageEvent.CONNECT_ERROR:
            case MessageEvent.CONNECT_LOST:
                Toast.makeText(getContext(), "Connect Loss", Toast.LENGTH_SHORT).show();
                getViewDataBinding().ivIsConnect.setBackground(getResources().getDrawable(R.drawable.ic_disconnect));
                VcomData.getInstance().setConnectStatus(false);
                break;
            case MessageEvent.SAVE_SUCCESS:
                mSceneList.get(mPosition).setSceneName(event.sceneName);
                mSceneList.get(mPosition).setGroupAddressList(event.list);
                SPUtil.setListValue(Objects.requireNonNull(getActivity()), "SceneList", mSceneList);
                sceneAdapter.notifyDataSetChanged();
                break;
            case MessageEvent.RESPONSE_SUCCESS:
            case MessageEvent.RESPONSE_ERROR:
                //getViewDataBinding().rlLoading.setVisibility(View.GONE);
                break;
            case MessageEvent.SEND_SUCCESS:
                getViewDataBinding().rlLoading.setVisibility(View.GONE);
                isAllowClick = true;
                break;
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /*
     * 显示群组地址列表
     * */
    @Override
    public void viewGroupAddress() {
        DialogGroupAddressListBinding groupAddressListBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_group_address_list, null, false);

        alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setView(groupAddressListBinding.getRoot())
                .create();
        alertDialog.show();

        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_item_online);

        groupAddressAdapter = new GroupAddressAdapter(getActivity(), mGroupAddressList);
        groupAddressListBinding.rvGroupList.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupAddressListBinding.rvGroupList.setAdapter(groupAddressAdapter);
        groupAddressAdapter.notifyDataSetChanged();

        //显示添加群组地址框
        groupAddressListBinding.tvAddGroupAddress.setOnClickListener(v -> {
            DialogAddGroupAddressBinding addGroupAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                    R.layout.dialog_add_group_address, null, false);
            childAlertDialog = new AlertDialog.Builder(getActivity())
                    .setView(addGroupAddressBinding.getRoot())
                    .create();
            childAlertDialog.show();

            Objects.requireNonNull(childAlertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_item_online);

            addGroupAddressBinding.tvConfirm.setOnClickListener(view -> {
                addGroupAddressBinding.etMainGroup.setBackgroundResource(R.drawable.shape_button_gray_f6);
                addGroupAddressBinding.etMiddleIp.setBackgroundResource(R.drawable.shape_button_gray_f6);
                addGroupAddressBinding.etGroupIp.setBackgroundResource(R.drawable.shape_button_gray_f6);

                if (!addGroupAddressBinding.etMainGroup.getText().toString().equals("")
                        && !addGroupAddressBinding.etMiddleIp.getText().toString().equals("")
                        && !addGroupAddressBinding.etGroupIp.getText().toString().equals("")) {

                    //设定群主地址 最大范围 1-31/1-7/1-255
                    int checkMainGroup = Integer.parseInt(addGroupAddressBinding.etMainGroup.getText().toString());
                    int checkMiddleIp = Integer.parseInt(addGroupAddressBinding.etMiddleIp.getText().toString());
                    int checkGroupIp = Integer.parseInt(addGroupAddressBinding.etGroupIp.getText().toString());

                    if (checkMainGroup < 1 || checkMainGroup > 31) {
                        Toast.makeText(getActivity(), "Please enter 1-31 range", Toast.LENGTH_SHORT).show();
                        addGroupAddressBinding.etMainGroup.setBackgroundResource(R.drawable.shape_item_error);
                    }

                    if (checkMiddleIp < 1 || checkMiddleIp > 7) {
                        Toast.makeText(getActivity(), "Please enter 1-7 range", Toast.LENGTH_SHORT).show();
                        addGroupAddressBinding.etMiddleIp.setBackgroundResource(R.drawable.shape_item_error);
                    }

                    if (checkGroupIp < 1 || checkGroupIp > 255) {
                        Toast.makeText(getActivity(), "Please enter 1-255 range", Toast.LENGTH_SHORT).show();
                        addGroupAddressBinding.etGroupIp.setBackgroundResource(R.drawable.shape_item_error);
                    }

                    if (checkMainGroup > 0 && checkMainGroup <= 31 && checkMiddleIp > 0 && checkMiddleIp <= 7 && checkGroupIp > 0 && checkGroupIp <= 255) {
                        GroupAddress groupAddress = new GroupAddress();
                        Function function = new Function();
                        groupAddress.setMainGroup(checkMainGroup);
                        groupAddress.setMiddleIp(checkMiddleIp);
                        groupAddress.setGroupIp(checkGroupIp);
                        groupAddress.setFunction(function);
                        groupAddress.setEnable(true);
                        //剔除重复添加群组地址
                        int size = mGroupAddressList.size();
                        boolean isAdd = true;
                        for(int i=0; i<size; i++) {
                            if(mGroupAddressList.get(i).getMainGroup() == groupAddress.getMainGroup()
                                && mGroupAddressList.get(i).getMiddleIp() == groupAddress.getMiddleIp()
                                && mGroupAddressList.get(i).getGroupIp() == groupAddress.getGroupIp()) {
                                isAdd = false;
                            }
                        }
                        //是否允许添加群组地址
                        if(size<=100) {
                            if(isAdd) {
                                mGroupAddressList.add(groupAddress);
                                groupAddressAdapter.notifyDataSetChanged();
                                SPUtil.setListValue(getActivity(), "GroupIpList", mGroupAddressList);
                            } else {
                                Toast.makeText(getActivity(), "Duplicate group address", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Morn than 100 group address", Toast.LENGTH_SHORT).show();
                        }

                        hideSoftKeyBoard(addGroupAddressBinding.etMainGroup);
                        hideSoftKeyBoard(addGroupAddressBinding.etMiddleIp);
                        hideSoftKeyBoard(addGroupAddressBinding.etGroupIp);
                        childAlertDialog.dismiss();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please fill in the complete IP", Toast.LENGTH_SHORT).show();
                }
            });

            addGroupAddressBinding.tvCancel.setOnClickListener(view -> {
                hideSoftKeyBoard(addGroupAddressBinding.etMainGroup);
                hideSoftKeyBoard(addGroupAddressBinding.etMiddleIp);
                hideSoftKeyBoard(addGroupAddressBinding.etGroupIp);
                childAlertDialog.dismiss();
            });
        });

        groupAddressListBinding.tvCancel.setOnClickListener(v -> alertDialog.dismiss());
    }

    @Override
    public void viewAdd() {
        //判断是否在运行场景 运行时不允许点击操作
        if(isAllowClick) {
            DialogAddSceneBinding addSceneBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                    R.layout.dialog_add_scene, null, false);
            alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                    .setView(addSceneBinding.getRoot())
                    .create();
            alertDialog.show();

            Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_item_online);

            addSceneBinding.tvCancel.setOnClickListener(view -> alertDialog.dismiss());
            addSceneBinding.tvConfirm.setOnClickListener(view -> {
                //点击添加新场景
                if (!addSceneBinding.etSceneName.getText().toString().equals("")) {
                    addNewScene(mSceneList, addSceneBinding.etSceneName.getText().toString());
                    sceneAdapter.notifyDataSetChanged();
                    addSceneBinding.etSceneName.setText("");
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(getActivity(), "Please enter the scene name", Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
            });
        }
    }

    /*
     * 隐藏软键盘
     * */
    private void hideSoftKeyBoard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            // imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
            imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /*
     * 场景适配器
     * */
    private class SceneAdapter extends RecyclerView.Adapter {

        private Context context;
        private List<Scene> list;

        private class SceneViewHolder extends RecyclerView.ViewHolder {
            public SceneViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        public SceneAdapter(Context context, List<Scene> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSceneListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_scene_list, parent, false);
            return new SceneViewHolder(binding.getRoot());
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemSceneListBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if(binding!=null) {
                binding.llScene.setVisibility(list.get(position).getSceneId().equals("0") ? View.GONE : View.VISIBLE);
                binding.llDelete.setVisibility(list.get(position).getSceneId().equals("0") ? View.GONE : View.VISIBLE);
                binding.llNull.setVisibility(list.get(position).getSceneId().equals("0") ? View.VISIBLE : View.GONE);
                binding.rlItem.setBackgroundResource(list.get(position).getSceneId().equals("0") ? R.color.background_color_F2 : R.drawable.selector_button_click);

                binding.tvSceneName.setText(list.get(position).getSceneName());

                //执行场景
                binding.rlItem.setOnClickListener(v -> {
                    if(VcomData.getInstance().getConnectStatus()) {
                        //判断是否在运行场景 运行时不允许点击操作
                        if(isAllowClick) {
                            if(list.get(position).getGroupAddressList().size()>0) {
                                isAllowClick = false;
                                getViewDataBinding().rlLoading.setVisibility(View.VISIBLE);
                                getViewModel().runScene(list, position);
                                Toast.makeText(context, "Run scene:" + binding.tvSceneName.getText(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Empty scene.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                //点击config进入场景设置
                binding.llConfig.setOnClickListener(v -> {
                    if(isAllowClick) {
                        //记录当前点击场景的position
                        mPosition = position;
                        Intent intent = new Intent(getActivity(), SceneConfigActivity.class);
                        intent.putExtra("SceneData", mSceneList.get(position));
                        startActivity(intent);
                    }
                });

                //点击删除当前场景
                binding.llDelete.setOnClickListener(v -> {
                    if(isAllowClick) {
                        DialogDeleteSceneBinding deleteSceneBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                                R.layout.dialog_delete_scene, null, false);
                        alertDialog = new AlertDialog.Builder(context)
                                .setView(deleteSceneBinding.getRoot())
                                .create();

                        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_item_online);

                        deleteSceneBinding.tvConfirm.setOnClickListener(view -> {
                            mSceneList.remove(position);
                            notifyDataSetChanged();
                            SPUtil.setListValue(context, "SceneList", mSceneList);
                            alertDialog.dismiss();
                        });
                        deleteSceneBinding.tvCancel.setOnClickListener(view -> alertDialog.dismiss());

                        alertDialog.show();
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
     * 添加新场景
     * */
    private void addNewScene(List<Scene> list, String newSceneName) {

        for(int i=0; i<list.size(); i++) {
            if(list.get(i).getSceneId().equals("0")) {
                list.remove(i);
                break;
            }
        }

        int id = list.size();
        id++;
        Scene scene = new Scene();
        scene.setSceneId(String.valueOf(id));
        scene.setSceneName(newSceneName);
        List<GroupAddress> nullList = new ArrayList<>();
        scene.setGroupAddressList(nullList);
        list.add(scene);

        Scene sceneNull = new Scene();
        sceneNull.setSceneId("0");
        sceneNull.setSceneName("");
        sceneNull.setGroupAddressList(nullList);
        list.add(sceneNull);

        SPUtil.setListValue(Objects.requireNonNull(getActivity()), "SceneList", list);
    }


    /*
     * 群组地址适配器
     * */
    private class GroupAddressAdapter extends RecyclerView.Adapter {

        private Context context;
        private List<GroupAddress> list;
        private boolean isSelect;

        public GroupAddressAdapter(Context context, List<GroupAddress> list) {
            this.context = context;
            this.list = list;
        }

        private class GroupAddressViewHolder extends RecyclerView.ViewHolder {
            public GroupAddressViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemGroupIpListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.item_group_ip_list, parent, false);
            return new GroupAddressViewHolder(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemGroupIpListBinding binding = DataBindingUtil.getBinding(holder.itemView);
            String groupIp = list.get(position).getMainGroup()
                    + "/" + list.get(position).getMiddleIp()
                    + "/" + list.get(position).getGroupIp();
            binding.tvGroupIp.setText(groupIp);

            //删除指定的群组地址
            binding.llDelete.setOnClickListener(v ->{
                DialogDeleteAddressBinding deleteAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                        R.layout.dialog_delete_address, null, false);

                childAlertDialog = new AlertDialog.Builder(context)
                        .setView(deleteAddressBinding.getRoot())
                        .create();

                Objects.requireNonNull(childAlertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_item_online);

                deleteAddressBinding.tvConfirm.setOnClickListener(view -> {
                    mGroupAddressList.remove(position);
                    childAlertDialog.dismiss();

                    SPUtil.setListValue(context, "GroupIpList", mGroupAddressList);
                    notifyDataSetChanged();

                });

                deleteAddressBinding.tvCancel.setOnClickListener(view -> childAlertDialog.dismiss());
                childAlertDialog.show();

            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }




}
