package com.banlap.smartexam.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.banlap.smartexam.R;
import com.banlap.smartexam.VcomData;
import com.banlap.smartexam.base.BaseActivity;
import com.banlap.smartexam.bean.ClientEMQ;
import com.banlap.smartexam.databinding.ActivityLoginBinding;
import com.banlap.smartexam.request.HttpLoader;
import com.banlap.smartexam.request.MessageEvent;
import com.banlap.smartexam.request.MqttManagement;
import com.banlap.smartexam.uivm.LoginVM;
import com.banlap.smartexam.utils.GsonUtil;
import com.banlap.smartexam.utils.SPUtil;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;


/**
 * @author Banlap on 2021/8/17
 */
public class LoginActivity extends BaseActivity<LoginVM, ActivityLoginBinding>
    implements LoginVM.LoginVMCallBack {

    private String configIp ="";
    private int configPort =0;
    private int connectTimeOutNum =0;   //响应时value返回连接状态次数

    private Handler mHandler = new Handler();;          //连接超时 执行
    private Runnable runEvent;                          //连接超时 事件
    private static final int connectTimeOut = 10000;    //连接超时 秒数

    private boolean isConnect = false;
    private boolean isConfigIp = false;

    @Override
    protected int getLayoutId() { return R.layout.activity_login; }

    @Override
    protected void initData() {}

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);

        getViewDataBinding().setVm(getViewModel());
        getViewModel().setCallBack(this);

        String cClientID = SPUtil.getStrValue(this, "SaveCurrentClientID");
        if(!cClientID.equals("")) {
            getViewDataBinding().etClientId.setText(cClientID);
        }
        getViewModel().isConnect();

        Log.e("FFFFFFFFFFFFFFF", "phone: " + Build.MODEL);

    }


    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            case MessageEvent.CONNECT_READY:
                getViewDataBinding().tvLoading.setText("loading...");
                getViewDataBinding().rlLoading.setVisibility(View.VISIBLE);
                getViewDataBinding().tvConnect.setBackgroundResource(R.drawable.selector_button_gray);
                getViewDataBinding().etTcpConnect.setInputType(InputType.TYPE_NULL);
                getViewDataBinding().etPort.setInputType(InputType.TYPE_NULL);
                getViewDataBinding().etClientId.setInputType(InputType.TYPE_NULL);
                break;
            case MessageEvent.CONNECT_SUCCESS:
                checkStatus();
                //intoMainActivity();
                //configIp();
                break;
            case MessageEvent.CONNECT_ERROR:
            case MessageEvent.CONNECT_LOST:
                mHandler.removeCallbacks(runEvent);
                Toast.makeText(this, "Connect Loss", Toast.LENGTH_SHORT).show();
                getViewModel().disconnect();
                getViewDataBinding().rlLoading.setVisibility(View.GONE);
                getViewDataBinding().tvConnect.setBackgroundResource(R.drawable.selector_button_green);
                getViewDataBinding().etTcpConnect.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
                getViewDataBinding().etPort.setInputType(InputType.TYPE_CLASS_NUMBER);
                getViewDataBinding().etClientId.setInputType(InputType.TYPE_CLASS_TEXT);
                //延时初始化isConnect值
                new Handler().postDelayed(() -> isConnect = false, 500);
                break;
            case MessageEvent.CHECK_ID_SUCCESS:
                isConnect = false;
                intoMainActivity();
                VcomData.getInstance().setConnectStatus(true);
                getViewDataBinding().rlLoading.setVisibility(View.GONE);
                getViewDataBinding().tvConnect.setBackgroundResource(R.drawable.selector_button_green);
                break;
            case MessageEvent.CHECK_ID_ERROR:
                isConnect = false;
                Toast.makeText(this, "Check ID Error", Toast.LENGTH_SHORT).show();
                VcomData.getInstance().setConnectStatus(false);
                getViewDataBinding().tvConnect.setBackgroundResource(R.drawable.selector_button_green);
                getViewDataBinding().etTcpConnect.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
                getViewDataBinding().etPort.setInputType(InputType.TYPE_CLASS_NUMBER);
                getViewDataBinding().etClientId.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case MessageEvent.RESPONSE_SUCCESS:
                mHandler.removeCallbacks(runEvent);
                //当返回value为ip以及端口时 即配置ip成功，value为3时则还没配置成功
                String data = GsonUtil.getInstance().getValue(event.msgData, "data");
                String value = GsonUtil.getInstance().getValue(data, "value");
                //int value = Integer.parseInt(GsonUtil.getInstance().getValue(data, "value"));
                if(value.length() !=1) {
                    String[] knxIPInfo = value.split(":");
                    if(knxIPInfo[0].equals( getViewDataBinding().etTcpConnect.getText().toString()) &&
                            knxIPInfo[1].equals( getViewDataBinding().etPort.getText().toString())) {
                            checkClientId();
                    } else {
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.RESPONSE_ERROR));
                    }
                } else {
                    if(!isConfigIp) {
                        configIp();
                        isConfigIp = !isConfigIp;
                        break;
                    }
                }
                connectTimeOutNum++;
                //返回value值三次以上，则执行连接失败
                if(connectTimeOutNum >3){
                    VcomData.getInstance().getManagement().disconnect();
                    break;
                }
                getViewDataBinding().tvLoading.setText("In Response..." + connectTimeOutNum);
                break;
            case MessageEvent.RESPONSE_ERROR:
                isConnect = false;
                mHandler.removeCallbacks(runEvent);

                Toast.makeText(this, "Response Error", Toast.LENGTH_SHORT).show();
                getViewModel().disconnect();
                getViewDataBinding().rlLoading.setVisibility(View.GONE);
                getViewDataBinding().tvConnect.setBackgroundResource(R.drawable.selector_button_green);
                getViewDataBinding().tvConnect.setBackgroundResource(R.drawable.selector_button_green);
                getViewDataBinding().etTcpConnect.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
                getViewDataBinding().etPort.setInputType(InputType.TYPE_CLASS_NUMBER);
                getViewDataBinding().etClientId.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }

    }

    /*
    * 配置连接knx系统的ip
    * */
    private void configIp() {
        VcomData.getInstance().getManagement().publish(MqttManagement.PUBLISH_TOPIC_SYS +  getViewDataBinding().etClientId.getText().toString(),
                "{\"data\": {\"host\": \"" + configIp +"\",\"port\": \"" + configPort +"\"},\"id\": 0,\"type\": 1}");
        mHandler = new Handler();
        //设置连接超时执行的内容
        runEvent = () -> { EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_ERROR)); };
        mHandler.postDelayed(runEvent, connectTimeOut);
    }

    /*
     * 检查当前knxIP是否在线
     * */
    private void checkStatus(){
        VcomData.getInstance().getManagement().publish(MqttManagement.PUBLISH_TOPIC_SYS +  getViewDataBinding().etClientId.getText().toString(),
                "{\"data\":{\"code\":0},\"id\":0,\"type\":3}");
        mHandler = new Handler();
        //设置连接超时执行的内容
        runEvent = () -> { EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_ERROR)); };
        mHandler.postDelayed(runEvent, connectTimeOut);
    }

    /*
    * 判断是否存在设备mac地址 以clientId值判断
    * */
    private void checkClientId() {
        String clientId = getViewDataBinding().etClientId.getText().toString();
        HttpLoader.getClientId(clientId, new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onNext(ResponseBody body) {
                try {
                    String json = body.string();
                    String json2Map = GsonUtil.getInstance().getValue(json, "data");
                    List<ClientEMQ> list = GsonUtil.getInstance().json2List(json2Map, new TypeToken<List<ClientEMQ>>(){}.getType());
                    if(list.size()>0) {
                        VcomData.getInstance().setClientId(list.get(0).getClientid());
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.CHECK_ID_SUCCESS));
                    } else {
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.CHECK_ID_ERROR));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.CHECK_ID_ERROR));
                }
            }

            @Override
            public void onError(Throwable e) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.RESPONSE_ERROR));
            }

            @Override
            public void onComplete() {}
        });
    }

    /**
    *  进入主界面
    * */
    private void intoMainActivity() {
        Toast.makeText(this, "Connect Success.", Toast.LENGTH_SHORT).show();
        getViewDataBinding().tvConnect.setBackgroundResource(R.drawable.selector_button_green);
        getViewDataBinding().etTcpConnect.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        getViewDataBinding().etPort.setInputType(InputType.TYPE_CLASS_NUMBER);
        getViewDataBinding().etClientId.setInputType(InputType.TYPE_CLASS_TEXT);
        //保存连接信息
        VcomData.getInstance().setIp(getViewDataBinding().etTcpConnect.getText().toString());
        VcomData.getInstance().setPort(getViewDataBinding().etPort.getText().toString());
        //保存已存在的clientID
        SPUtil.setStrValue(this, "SaveCurrentClientID", getViewDataBinding().etClientId.getText().toString());
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /*
    * 点击tcp连接
    * */
    @Override
    public void viewConnect() {
        if(!getViewDataBinding().etTcpConnect.getText().toString().equals("")
                && !getViewDataBinding().etPort.getText().toString().equals("")
                    && !getViewDataBinding().etClientId.getText().toString().equals("")) {
            configIp = getViewDataBinding().etTcpConnect.getText().toString();
            configPort = Integer.parseInt(getViewDataBinding().etPort.getText().toString());

            /*EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_READY));
            Intent intent = new Intent(this, MqttManagement.class);
            startService(intent);*/
            if(!isConnect) {
                isConnect = true;
                connectTimeOutNum = 0;

                EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_READY));
                String clientID = VcomData.getInstance().getClientId();
                //先处理ui线程，弹出loading框，再执行子线程操作
                /* new Thread(() -> {
                    Looper.prepare();
                    getViewModel().connect(clientID == null ? getViewDataBinding().etClientId.getText().toString() : clientID);
                    Looper.loop();
                 }).start();*/
                new Handler().postDelayed(() ->
                     getViewModel().connect(clientID == null ? getViewDataBinding().etClientId.getText().toString() : clientID)
                     , 500);
            }
        } else {
            Toast.makeText(this, "Please enter ip, port and clientID", Toast.LENGTH_SHORT).show();
        }
    }



    /*
    * 如果已连接到服务器 则自动跳转到主界面
    * */
    @Override
    public void viewAutoIntoActivity() {
        checkClientId();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
