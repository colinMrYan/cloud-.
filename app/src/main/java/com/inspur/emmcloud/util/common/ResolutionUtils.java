package com.inspur.emmcloud.util.common;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowInsets;

import com.inspur.emmcloud.MyApplication;

import java.lang.reflect.Method;
import java.util.List;

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

    private static int getNotchHeight(Activity context) {
        int notchHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            notchHeight = ResolutionUtils.getStatusBarHeightAboutAndroidP(context);
        } else {
            //判断手机厂商，目前8.0只有华为、小米、oppo、vivo适配了刘海屏
            String phoneManufacturer = android.os.Build.BRAND.toLowerCase();
            if ("huawei".equals(phoneManufacturer)) {
                //huawei,长度为length,单位px
                boolean haveInScreenEMUI = hasNotchInScreenEMUI(context);
                if (haveInScreenEMUI) {
                    int[] screenSize = {0};
                    int length = 0;
                    screenSize = getNotchSizeEMUI(context);
                    notchHeight = screenSize[1];
                    //下面注释的是单独测试时的弹出消息
                    //Toast.makeText(context, "haveInScreen:" + haveInScreenEMUI + ",screenSize:" + length, Toast.LENGTH_LONG).show();
                }
            } else if ("xiaomi".equals(phoneManufacturer)) {
                //xiaomi,单位px
                boolean haveInScreenMIUI = getNotchMIUI("ro.miui.notch", 0) == 1;
                if (haveInScreenMIUI) {
                    int resourceId = context.getResources().getIdentifier("notch_height", "dimen", "android");
                    int result = 0;
                    if (resourceId > 0) {
                        result = context.getResources().getDimensionPixelSize(resourceId);
                    }
                    notchHeight = result;
                    //下面注释的是单独测试时的弹出消息
                    //Toast.makeText(context, "haveInScreen:" + haveInScreenMIUI + ",screenSize" + result, Toast.LENGTH_LONG).show();
                }
            } else if ("vivo".equals(phoneManufacturer)) {
                //vivo,单位dp，高度27dp
                boolean haveInScreenVIVO = hasNotchAtVivo(context);
                if (haveInScreenVIVO) {
                    //下面注释的是单独测试时的弹出消息
                    //Toast.makeText(context, "haveInScreenVIVO:" + haveInScreenVIVO, Toast.LENGTH_LONG).show();
                    notchHeight = DensityUtil.dip2px(context, 27);
                }
            } else if ("oppo".equals(phoneManufacturer)) {
                //oppo
                boolean haveInScreenOPPO = context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
                if (haveInScreenOPPO) {
                    //下面注释的是单独测试时的弹出消息
                    //Toast.makeText(context, "haveInScreenOPPO:" + haveInScreenOPPO, Toast.LENGTH_LONG).show();
                    notchHeight = 80;
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

    public static boolean hasNotchInScreenEMUI(Context context) {
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

    public static int[] getNotchSizeEMUI(Context context) {
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

    public static int getNotchMIUI(final String key, final int def) {
        Method getIntMethod = null;
        try {
            if (getIntMethod == null) {
                getIntMethod = Class.forName("android.os.SystemProperties")
                        .getMethod("getInt", String.class, int.class);
            }
            return ((Integer) getIntMethod.invoke(null, key, def)).intValue();
        } catch (Exception e) {
            Log.e("MainActivity", "Platform error: " + e.toString());
            return def;
        }
    }

    public static boolean hasNotchAtVivo(Context context) {
        boolean ret = false;
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class FtFeature = classLoader.loadClass("android.util.FtFeature");
            Method method = FtFeature.getMethod("isFeatureSupport", int.class);
            ret = (boolean) method.invoke(FtFeature, 0x00000020);
        } catch (ClassNotFoundException e) {
            Log.e("Notch", "hasNotchAtVivo ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("Notch", "hasNotchAtVivo NoSuchMethodException");
        } catch (Exception e) {
            Log.e("Notch", "hasNotchAtVivo Exception");
        } finally {
            return ret;
        }
    }


    @TargetApi(28)
    private static boolean hasNotchInScreenAboutAndroidP(Activity context) {
        boolean isNotchScreen = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets windowInsets = context.getWindow().getDecorView().getRootWindowInsets();
            if (windowInsets != null) {
                DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                if (displayCutout != null) {
                    List<Rect> rects = displayCutout.getBoundingRects();
                    //通过判断是否存在rects来确定是否刘海屏手机
                    if (rects != null && rects.size() > 0) {
                        isNotchScreen = true;
                    }
                }
            }
        }
        return isNotchScreen;
    }

    public static int getStatusBarHeightAboutAndroidP(Activity context) {
        int result = 0;
        if (hasNotchInScreenAboutAndroidP(context)) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }
}
