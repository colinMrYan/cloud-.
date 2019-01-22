package com.inspur.emmcloud.util.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.util.privates.AppUtils;

import java.lang.reflect.Method;

public class ResolutionUtils {

    public static int getWidth(Activity context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    /**
     * 检查设备分辨率是否符合条件
     *
     * @return true：符合 false：不符合
     */
    public static Boolean isFitResolution(Activity context) {
        // TODO Auto-generated method stub
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = getHeight(context);
        // 判断设备的分辨率，要求不小于800*480
        return width * height >= 384000;
    }

    /**
     * 获取屏幕分辨率
     *
     * @param context
     * @return
     */
    public static int getResolution(Activity context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = getHeight(context);
        return width * height;
    }

    /**
     * 获取屏幕长宽比比例
     *
     * @param context
     * @return
     */
    public static float getResolutionRate(Activity context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = getHeight(context);
        //刘海的高度
        int NotchHeight = getNotchHeight(context);
        height = height - NotchHeight;
        return height * 1.0f / width;
    }

    private static int getNotchHeight(Context context) {
        int notchHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            notchHeight = ResolutionUtils.getStatusBarHeightAboutAndroidP(context);
        }else {
            if (AppUtils.getIsHuaWei()) {
                if (hasNotchInScreenHuaWei(context)) {
                    notchHeight =getNotchSizeHuaWei(context)[1];
                }
            }
        }

        return notchHeight;
    }

    /**
     * 支持带有虚拟按键手机屏幕高度的计算
     *
     * @param context
     * @return
     */
    public static int getHeight(Activity context) {
        int dpi = 0;
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            dpi = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dpi == 0) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dpi = displayMetrics.heightPixels;
        }
        return dpi;
    }

    /**
     * 获取navigationbar 的高度
     *
     * @return
     */
    public static int getNavigationBarHeight() {
        boolean hasMenuKey = ViewConfiguration.get(MyApplication.getInstance()).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        //判断是否有虚拟按钮
        if (!hasMenuKey) {
            Resources resources = MyApplication.getInstance().getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            //获取NavigationBar的高度
            int height = resources.getDimensionPixelSize(resourceId);
            return height;
        } else {
            return 0;
        }
    }

    public static boolean hasNotchInScreenHuaWei(Context context) {
        boolean ret = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            ret = (boolean) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            Log.e("test", "hasNotchInScreen ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("test", "hasNotchInScreen NoSuchMethodException");
        } catch (Exception e) {
            Log.e("test", "hasNotchInScreen Exception");
        } finally {
            return ret;
        }
    }

    public static int[] getNotchSizeHuaWei(Context context) {
        int[] ret = new int[]{0, 0};
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("getNotchSize");
            ret = (int[]) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            Log.e("test", "getNotchSize ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("test", "getNotchSize NoSuchMethodException");
        } catch (Exception e) {
            Log.e("test", "getNotchSize Exception");
        } finally {
            LogUtils.jasonDebug("ret[0]=" + ret[0]);
            LogUtils.jasonDebug("ret[1]=" + ret[1]);
            return ret;
        }
    }

    public static int getStatusBarHeightAboutAndroidP(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
