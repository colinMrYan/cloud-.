package com.inspur.emmcloud.baselib.util;

/**
 * 动态设置 点击事件 selector 的工具类  可以从本地添加  也可以从网络添加
 * Created by yufuchang on 2019/1/29.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SelectorUtils {
    private final static int NORMAL_STATE = 1;//正常状态
    private final static int PRESSED_STATE = 2;//选中状态

    /**
     * 从 drawable 获取图片 id 给 Imageview 添加 selector
     *
     * @param context  调用方法的 Activity
     * @param idNormal 默认图片的 id
     * @param idPress  点击图片的 id
     * @param iv       点击的 view
     */
    public static void addSelectorFromDrawable(Context context, int idNormal, int idPress, ImageView iv) {
        StateListDrawable drawable = new StateListDrawable();
        Drawable normal = context.getResources().getDrawable(idNormal);
        Drawable press = context.getResources().getDrawable(idPress);
        drawable.addState(new int[]{android.R.attr.state_pressed}, press);
        drawable.addState(new int[]{-android.R.attr.state_pressed}, normal);
        iv.setBackground(drawable);
    }

    /**
     * 从 drawable 获取图片 id 给 Button 添加 selector
     *
     * @param context  调用方法的 Activity
     * @param idNormal 默认图片的 id
     * @param idPress  点击图片的 id
     * @param button   点击的 view
     */
    public static void addSelectorFromDrawable(Context context, int idNormal, int idPress, Button button) {
        StateListDrawable drawable = new StateListDrawable();
        Drawable normal = context.getResources().getDrawable(idNormal);
        Drawable press = context.getResources().getDrawable(idPress);
        drawable.addState(new int[]{android.R.attr.state_pressed}, press);
        drawable.addState(new int[]{-android.R.attr.state_pressed}, normal);
        button.setBackground(drawable);
    }

    /**
     * 从网络获取图片 给 ImageView 设置 selector
     *
     * @param iconUrl   获取默认图片的链接
     * @param imageView 点击的 view
     */
    public static void addSelectorFromNet(final Context context, final String iconUrl, final ImageView imageView, final int normalDefaultResId, final int pressDefaultResId) {
        new AsyncTask<Void, Void, Drawable>() {

            @Override
            protected Drawable doInBackground(Void... params) {
                StateListDrawable drawable = new StateListDrawable();
                Drawable normal = loadImageFromNet(context, getIconByState(iconUrl, NORMAL_STATE));
                if (normal == null) {
                    normal = context.getResources().getDrawable(normalDefaultResId);
                }
                Drawable press = loadImageFromNet(context, getIconByState(iconUrl, PRESSED_STATE));
                if (press == null) {
                    press = context.getResources().getDrawable(pressDefaultResId);
                }
                drawable.addState(new int[]{android.R.attr.state_selected}, press);
                drawable.addState(new int[]{-android.R.attr.state_selected}, normal);
                return drawable;
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                super.onPostExecute(drawable);
                imageView.setImageDrawable(drawable);
            }
        }.execute();
    }

    /**
     * 通过状态获取url地址
     *
     * @param iconUrl
     * @param normalState
     * @return
     */
    private static String getIconByState(String iconUrl, int normalState) {
        String iconUrlWithState = "";
        switch (normalState) {
            case NORMAL_STATE:
                iconUrlWithState = iconUrl + "@3x.png";
                break;
            case PRESSED_STATE:
                iconUrlWithState = iconUrl + "_selected@3x.png";
                break;
            default:
                break;
        }
        return iconUrlWithState;
    }

    /**
     * 从网络获取图片 给 Button 设置 selector
     *
     * @param clazz     调用方法的类
     * @param normalUrl 获取默认图片的链接
     * @param pressUrl  获取点击图片的链接
     * @param button    点击的 view
     */
    public static void addSeletorFromNet(final Context context, final Class clazz, final String normalUrl, final String pressUrl, final Button button) {
        new AsyncTask<Void, Void, Drawable>() {
            @Override
            protected Drawable doInBackground(Void... params) {
                StateListDrawable drawable = new StateListDrawable();
                Drawable normal = loadImageFromNet(context, normalUrl);
                Drawable press = loadImageFromNet(context, pressUrl);
                drawable.addState(new int[]{android.R.attr.state_pressed}, press);
                drawable.addState(new int[]{-android.R.attr.state_pressed}, normal);
                return drawable;
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                super.onPostExecute(drawable);
                button.setBackground(drawable);
            }
        }.execute();

    }

    /**
     * 从网络获取图片
     *
     * @param context 调用方法的类
     * @param netUrl  获取图片的链接
     * @return 返回一个 drawable 类型的图片
     */
    private static Drawable loadImageFromNet(Context context, String netUrl) {
        Drawable drawable = null;
        try {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    // 设置图片的解码类型
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(netUrl, options);
            if (bitmap != null) {
                drawable = new BitmapDrawable(context.getResources(), bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }
}

