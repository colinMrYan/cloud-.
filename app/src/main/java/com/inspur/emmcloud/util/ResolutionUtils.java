package com.inspur.emmcloud.util;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;

import java.lang.reflect.Method;

public class ResolutionUtils {

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
		if (height == 0) {
			height = displayMetrics.heightPixels;
		}
		// 判断设备的分辨率，要求不小于800*480
		if (width * height >= 384000) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取屏幕分辨率
	 * @param context
	 * @return
	 */
	public static int getResolution(Activity context){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int width = displayMetrics.widthPixels;
		int height = getHeight(context);
		if (height == 0) {
			height = displayMetrics.heightPixels;
		}
		return width * height;
	}
	
	/**
	 * 支持带有虚拟按键手机屏幕高度的计算
	 * @param context
	 * @return
	 */
	private static int getHeight(Activity context)
    {   int dpi = 0;
            Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics(); 
                Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
                        Method method = c.getMethod("getRealMetrics",DisplayMetrics.class);
            method.invoke(display, dm);
            dpi=dm.heightPixels;
        }catch(Exception e){
            e.printStackTrace();
        }  
        return dpi;
    }


}
