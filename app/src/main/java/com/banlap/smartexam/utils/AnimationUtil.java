package com.banlap.smartexam.utils;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

/**
 * @author Banlap on 2021/8/26
 */
public class AnimationUtil {

    /*
     *  旋转动画 指定旋转角度
     * */
    public static Animation animationRotate(int startAngle, int endAngle, int duration) {
        Animation animation = new RotateAnimation(startAngle, endAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);   //设置动画速度
        animation.setRepeatCount(0);   //设置重复动画次数
        animation.setFillAfter(true); //完成动画后不再恢复原来状态
        return animation;
    }
}
