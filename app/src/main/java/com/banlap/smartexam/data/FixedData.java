package com.banlap.smartexam.data;

import com.banlap.smartexam.bean.Device;
import com.banlap.smartexam.bean.Function;
import com.banlap.smartexam.bean.Way;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Banlap on 2021/9/18
 */
public class FixedData {

    public List<Device> mDeviceList;
    public List<String> mRelativeValueList;

    private static FixedData fixedData = new FixedData();
    public static FixedData getInstance() { return fixedData; }

    public List<Device> getListData() {
        mDeviceList = new ArrayList<>();

        Device d1 = new Device();
        d1.setDeviceId("1");
        d1.setDeviceMac("");
        d1.setDeviceType("AS");
        d1.setDeviceName("1-10V Dimming Actuators");
        d1.setWayList(way1());
        d1.setFunctionList(functionNull());
        d1.setSelect(false);

        Device d2 = new Device();
        d2.setDeviceId("2");
        d2.setDeviceMac("");
        d2.setDeviceType("AS");
        d2.setDeviceName("4-Way Dimming Actuators");
        d2.setWayList(way2());
        d2.setFunctionList(functionNull());
        d2.setSelect(false);

        Device d3 = new Device();
        d3.setDeviceId("3");
        d3.setDeviceMac("");
        d3.setDeviceType("S");
        d3.setDeviceName("Presence Sensor With Constant Lighting");
        d3.setWayList(wayNull());
        d3.setFunctionList(function9());
        d3.setSelect(false);

        Device d4 = new Device();
        d4.setDeviceId("4");
        d4.setDeviceMac("");
        d4.setDeviceType("S");
        d4.setDeviceName("Push Button Sensor");
        d4.setWayList(wayNull());
        d4.setFunctionList(function10());
        d4.setSelect(false);

        mDeviceList.add(d1);
        mDeviceList.add(d2);
        mDeviceList.add(d3);
        mDeviceList.add(d4);

        return mDeviceList;
    }

    private List<Way> wayNull() { return new ArrayList<>(); }
    private List<Function> functionNull() { return new ArrayList<>(); }

    //通道列表
    private List<Way> way1() {
        List<Way> list = new ArrayList<>();

        Way way1 = new Way();
        way1.setWayId("1");
        way1.setWayName("CH A");
        way1.setFunctionList(function1());
        way1.isSelect = true;

        Way way2 = new Way();
        way2.setWayId("2");
        way2.setWayName("CH B");
        way2.setFunctionList(function2());

        Way way3 = new Way();
        way3.setWayId("3");
        way3.setWayName("CH C");
        way3.setFunctionList(function3());

        Way way4 = new Way();
        way4.setWayId("4");
        way4.setWayName("CH D");
        way4.setFunctionList(function4());

        list.add(way1);
        list.add(way2);
        list.add(way3);
        list.add(way4);
        return list;
    }
    private List<Way> way2() {
        List<Way> list = new ArrayList<>();

        Way way5 = new Way();
        way5.setWayId("5");
        way5.setWayName("CH A");
        way5.setFunctionList(function5());
        way5.isSelect = true;

        Way way6 = new Way();
        way6.setWayId("6");
        way6.setWayName("CH B");
        way6.setFunctionList(function6());

        Way way7 = new Way();
        way7.setWayId("7");
        way7.setWayName("CH C");
        way7.setFunctionList(function7());

        Way way8 = new Way();
        way8.setWayId("8");
        way8.setWayName("CH D");
        way8.setFunctionList(function8());

        list.add(way5);
        list.add(way6);
        list.add(way7);
        list.add(way8);

        return list;
    }

    private List<Function> function1() {
        List<Function> list = new ArrayList<>();
        //开关功能
        Function f1 = new Function();
        f1.setFunctionId("1");
        f1.setOperationType("Write");
        f1.setFunctionType("Switch");
        f1.setFunctionName("Switch");
        f1.setFunctionValue("ON");
        f1.setLength(1);
        f1.setFunctionCode("[1,0]");
        f1.setGroupAddress("");
        f1.setEnable(true);
        //绝对调光功能
        Function f2 = new Function();
        f2.setFunctionId("2");
        f2.setOperationType("Write");
        f2.setFunctionType("Brightness");
        f2.setFunctionName("Absolute Dimming");
        f2.setFunctionValue("20");
        f2.setLength(2);
        f2.setFunctionCode("[20,0]");
        f2.setGroupAddress("");
        f2.setEnable(true);
        //相对调光功能
        Function f3 = new Function();
        f3.setFunctionId("3");
        f3.setOperationType("Write");
        f3.setFunctionType("Control");
        f3.setFunctionName("Relative Dimming");
        f3.setFunctionValue("+100%");
        f3.setLength(1);
        f3.setFunctionCode("[9,0]");
        f3.setGroupAddress("");
        f3.setEnable(true);

        list.add(f1);
        list.add(f2);
        list.add(f3);
        return list;
    }
    private List<Function> function2() {
        List<Function> list = new ArrayList<>();
        //开关功能
        Function f4 = new Function();
        f4.setFunctionId("4");
        f4.setOperationType("Write");
        f4.setFunctionType("Switch");
        f4.setFunctionName("Switch");
        f4.setFunctionValue("ON");
        f4.setLength(1);
        f4.setFunctionCode("[1,0]");
        f4.setGroupAddress("");
        f4.setEnable(true);
        //绝对调光功能
        Function f5 = new Function();
        f5.setFunctionId("5");
        f5.setOperationType("Write");
        f5.setFunctionType("Brightness");
        f5.setFunctionName("Absolute Dimming");
        f5.setFunctionValue("20");
        f5.setLength(2);
        f5.setFunctionCode("[20,0]");
        f5.setGroupAddress("");
        f5.setEnable(true);
        //相对调光功能
        Function f6 = new Function();
        f6.setFunctionId("6");
        f6.setOperationType("Write");
        f6.setFunctionType("Control");
        f6.setFunctionName("Relative Dimming");
        f6.setFunctionValue("+100%");
        f6.setLength(1);
        f6.setFunctionCode("[9,0]");
        f6.setGroupAddress("");
        f6.setEnable(true);

        list.add(f4);
        list.add(f5);
        list.add(f6);
        return list;
    }
    private List<Function> function3() {
        List<Function> list = new ArrayList<>();
        //开关功能
        Function f7 = new Function();
        f7.setFunctionId("7");
        f7.setOperationType("Write");
        f7.setFunctionType("Switch");
        f7.setFunctionName("Switch");
        f7.setFunctionValue("ON");
        f7.setLength(1);
        f7.setFunctionCode("[1,0]");
        f7.setGroupAddress("");
        f7.setEnable(true);
        //绝对调光功能
        Function f8 = new Function();
        f8.setFunctionId("8");
        f8.setOperationType("Write");
        f8.setFunctionType("Brightness");
        f8.setFunctionName("Absolute Dimming");
        f8.setFunctionValue("20");
        f8.setLength(2);
        f8.setFunctionCode("[20,0]");
        f8.setGroupAddress("");
        f8.setEnable(true);
        //相对调光功能
        Function f9 = new Function();
        f9.setFunctionId("9");
        f9.setOperationType("Write");
        f9.setFunctionType("Control");
        f9.setFunctionName("Relative Dimming");
        f9.setFunctionValue("+100%");
        f9.setLength(1);
        f9.setFunctionCode("[9,0]");
        f9.setGroupAddress("");
        f9.setEnable(true);

        list.add(f7);
        list.add(f8);
        list.add(f9);
        return list;
    }
    private List<Function> function4() {
        List<Function> list = new ArrayList<>();
        //开关功能
        Function f10 = new Function();
        f10.setFunctionId("10");
        f10.setOperationType("Write");
        f10.setFunctionType("Switch");
        f10.setFunctionName("Switch");
        f10.setFunctionValue("ON");
        f10.setLength(1);
        f10.setFunctionCode("[1,0]");
        f10.setGroupAddress("");
        f10.setEnable(true);
        //绝对调光功能
        Function f11 = new Function();
        f11.setFunctionId("11");
        f11.setOperationType("Write");
        f11.setFunctionType("Brightness");
        f11.setFunctionName("Absolute Dimming");
        f11.setFunctionValue("20");
        f11.setLength(2);
        f11.setFunctionCode("[20,0]");
        f11.setGroupAddress("");
        f11.setEnable(true);
        //相对调光功能
        Function f12 = new Function();
        f12.setFunctionId("12");
        f12.setOperationType("Write");
        f12.setFunctionType("Control");
        f12.setFunctionName("Relative Dimming");
        f12.setFunctionValue("+100%");
        f12.setLength(1);
        f12.setFunctionCode("[9,0]");
        f12.setGroupAddress("");
        f12.setEnable(true);

        list.add(f10);
        list.add(f11);
        list.add(f12);
        return list;
    }

    private List<Function> function5() {
        List<Function> list = new ArrayList<>();
        //开关功能
        Function f13 = new Function();
        f13.setFunctionId("13");
        f13.setOperationType("Write");
        f13.setFunctionType("Switch");
        f13.setFunctionName("Switch");
        f13.setFunctionValue("ON");
        f13.setLength(1);
        f13.setFunctionCode("[1,0]");
        f13.setGroupAddress("");
        f13.setEnable(true);
        //绝对调光功能
        Function f14 = new Function();
        f14.setFunctionId("14");
        f14.setOperationType("Write");
        f14.setFunctionType("Brightness");
        f14.setFunctionName("Absolute Dimming");
        f14.setFunctionValue("20");
        f14.setLength(2);
        f14.setFunctionCode("[20,0]");
        f14.setGroupAddress("");
        f14.setEnable(true);

        list.add(f13);
        list.add(f14);
        return list;
    }
    private List<Function> function6() {
        List<Function> list = new ArrayList<>();
        //开关功能
        Function f15 = new Function();
        f15.setFunctionId("15");
        f15.setOperationType("Write");
        f15.setFunctionType("Switch");
        f15.setFunctionName("Switch");
        f15.setFunctionValue("ON");
        f15.setLength(1);
        f15.setFunctionCode("[1,0]");
        f15.setGroupAddress("");
        f15.setEnable(true);
        //绝对调光功能
        Function f16 = new Function();
        f16.setFunctionId("8");
        f16.setOperationType("Write");
        f16.setFunctionType("Brightness");
        f16.setFunctionName("Absolute Dimming");
        f16.setFunctionValue("20");
        f16.setLength(2);
        f16.setFunctionCode("[20,0]");
        f16.setGroupAddress("");
        f16.setEnable(true);

        list.add(f15);
        list.add(f16);
        return list;
    }
    private List<Function> function7() {
        List<Function> list = new ArrayList<>();
        //开关功能
        Function f17 = new Function();
        f17.setFunctionId("17");
        f17.setOperationType("Write");
        f17.setFunctionType("Switch");
        f17.setFunctionName("Switch");
        f17.setFunctionValue("ON");
        f17.setLength(1);
        f17.setFunctionCode("[1,0]");
        f17.setGroupAddress("");
        f17.setEnable(true);
        //绝对调光功能
        Function f18 = new Function();
        f18.setFunctionId("18");
        f18.setOperationType("Write");
        f18.setFunctionType("Brightness");
        f18.setFunctionName("Absolute Dimming");
        f18.setFunctionValue("20");
        f18.setLength(2);
        f18.setFunctionCode("[20,0]");
        f18.setGroupAddress("");
        f18.setEnable(true);

        list.add(f17);
        list.add(f18);
        return list;
    }
    private List<Function> function8() {
        List<Function> list = new ArrayList<>();
        //开关功能
        Function f19 = new Function();
        f19.setFunctionId("19");
        f19.setOperationType("Write");
        f19.setFunctionType("Switch");
        f19.setFunctionName("Switch");
        f19.setFunctionValue("ON");
        f19.setLength(1);
        f19.setFunctionCode("[1,0]");
        f19.setGroupAddress("");
        f19.setEnable(true);
        //绝对调光功能
        Function f20 = new Function();
        f20.setFunctionId("20");
        f20.setOperationType("Write");
        f20.setFunctionType("Brightness");
        f20.setFunctionName("Absolute Dimming");
        f20.setFunctionValue("20");
        f20.setLength(2);
        f20.setFunctionCode("[20,0]");
        f20.setGroupAddress("");
        f20.setEnable(true);

        list.add(f19);
        list.add(f20);
        return list;
    }


    private List<Function> function9() {
        List<Function> list = new ArrayList<>();
        //Switching Start Of Presence A
        Function f21 = new Function();
        f21.setFunctionId("21");
        f21.setOperationType("Write");
        f21.setFunctionType("Switch");
        f21.setFunctionName("Switching Start Of Presence A");
        f21.setFunctionValue("ON");
        f21.setLength(1);
        f21.setFunctionCode("[1,0]");
        f21.setGroupAddress("");
        f21.setEnable(true);
        //Switching Start Of Presence C
        Function f22 = new Function();
        f22.setFunctionId("22");
        f22.setOperationType("Write");
        f22.setFunctionType("Switch");
        f22.setFunctionName("Switching Start Of Presence C");
        f22.setFunctionValue("OFF");
        f22.setLength(1);
        f22.setFunctionCode("[0,0]");
        f22.setGroupAddress("");
        f22.setEnable(true);
        //光照度
        Function f23 = new Function();
        f23.setFunctionId("23");
        f23.setOperationType("Write");
        f23.setFunctionType("Brightness");
        f23.setFunctionName("Illuminance");
        f23.setFunctionValue("20");
        f23.setLength(2);
        f23.setFunctionCode("[20,0]");
        f23.setGroupAddress("");
        f23.setEnable(true);

        list.add(f21);
        list.add(f22);
        list.add(f23);
        return list;
    }
    private List<Function> function10() {
        List<Function> list = new ArrayList<>();
        //长按调光
        Function f24 = new Function();
        f24.setFunctionId("24");
        f24.setOperationType("Write");
        f24.setFunctionType("Control");
        f24.setFunctionName("Long Press");
        f24.setFunctionValue("+100%");
        f24.setLength(1);
        f24.setFunctionCode("[9,0]");
        f24.setGroupAddress("");
        f24.setEnable(true);
        //短按
        Function f25 = new Function();
        f25.setFunctionId("25");
        f25.setOperationType("Write");
        f25.setFunctionType("Switch");
        f25.setFunctionName("Short Switch");
        f25.setFunctionValue("OFF");
        f25.setLength(1);
        f25.setFunctionCode("[0,0]");
        f25.setGroupAddress("");
        f25.setEnable(true);
        //背景灯
        Function f26 = new Function();
        f26.setFunctionId("26");
        f26.setOperationType("Write");
        f26.setFunctionType("Switch");
        f26.setFunctionName("BackLight");
        f26.setFunctionValue("OFF");
        f26.setLength(1);
        f26.setFunctionCode("[0,0]");
        f26.setGroupAddress("");
        f26.setEnable(true);

        list.add(f24);
        list.add(f25);
        list.add(f26);
        return list;
    }


    public List<String> getRelativeData() {
        mRelativeValueList = new ArrayList<>();
        mRelativeValueList.add("+100%");
        mRelativeValueList.add("+50%");
        mRelativeValueList.add("+25%");
        mRelativeValueList.add("+12%");
        mRelativeValueList.add("+6%");
        mRelativeValueList.add("+3%");
        mRelativeValueList.add("+1%");
        mRelativeValueList.add("-1%");
        mRelativeValueList.add("-3%");
        mRelativeValueList.add("-6%");
        mRelativeValueList.add("-12%");
        mRelativeValueList.add("-25%");
        mRelativeValueList.add("-50%");
        mRelativeValueList.add("-100%");
        return  mRelativeValueList;
    }
}
