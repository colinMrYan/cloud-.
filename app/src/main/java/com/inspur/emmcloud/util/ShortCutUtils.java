package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.inspur.emmcloud.R;

/**
 * Created by yufuchang on 2017/3/30.
 */

public class ShortCutUtils {
    /**
     * 创建图标
     * @param contxt
     * @param clz
     */
    public static void createShortCut(Context contxt,Class clz,String shortCutName,String appPathOrUri,String type) {
        //String applicationName=getApplicationName(contxt);
        String applicationName = shortCutName;//程序名称，不是packageName
        if (isInstallShortcut(contxt,applicationName)) {// 如果已经创建了一次就不会再创建了
            return;
        }
        Intent sIntent = new Intent(Intent.ACTION_MAIN);
        sIntent.addCategory(Intent.CATEGORY_LAUNCHER);// 加入action,和category之后，程序卸载的时候才会主动将该快捷方式也卸载
        sIntent.setClass(contxt, clz);//点击后进入的Activity

        Intent installer = new Intent();
        installer.putExtra("duplicate", false);//false标示不重复创建
        LogUtils.YfcDebug("发出添加图标的广播");
        Intent intentTodo = new Intent();
        if(type.equals("ecc-app-react-native")){
            LogUtils.YfcDebug("添加react：ecc-app-react-native===="+appPathOrUri);
//            intentTodo.putExtra("ecc-app-react-native",appPathOrUri);
//            intentTodo.setClass(contxt, ReactNativeAppActivity.class);
            sIntent.putExtra("ecc-app-react-native",appPathOrUri);
        }
        installer.putExtra(Intent.EXTRA_SHORTCUT_INTENT,intentTodo);
        installer.putExtra("android.intent.extra.shortcut.INTENT", sIntent);
        //设置应用的名称
        installer.putExtra("android.intent.extra.shortcut.NAME", applicationName);
        //设置图标
        installer.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.fromContext(contxt, R.drawable.ic_launcher));
        installer.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        contxt.sendBroadcast(installer);//发送安装桌面图标的通知
    }

    /**
     * 检查是否已经存在
     * @param context
     * @param applicationName
     * @return
     */
    public static boolean isInstallShortcut(Context context,String applicationName) {
        boolean isInstallShortcut = false;
        ContentResolver cr = context.getContentResolver();
        //sdk大于8的时候,launcher2的设置查找
        String AUTHORITY = "com.android.launcher2.settings";
        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
        Cursor c = cr.query(CONTENT_URI, new String[] { "title", "iconResource" },
                "title=?", new String[] { applicationName }, null);
        if (c != null && c.getCount() > 0) {
            isInstallShortcut = true;
        }
        if (c != null) {
            c.close();
        }
        //如果存在先关闭cursor，再返回结果
        if (isInstallShortcut) {
            return isInstallShortcut;
        }
        //android.os.Build.VERSION.SDK_INT < 8时
        AUTHORITY = "com.android.launcher.settings";
        CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
        c = cr.query(CONTENT_URI, new String[] { "title", "iconResource" }, "title=?",
                new String[] {applicationName}, null);
        if (c != null && c.getCount() > 0) {
            isInstallShortcut = true;
        }
        if (c != null) {
            c.close();
        }
        return isInstallShortcut;
    }

    public static void delShortcut(Activity activity) {
        Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        //快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, activity.getString(R.string.app_name));
        String appClass = activity.getPackageName() + "." + activity.getLocalClassName();
        ComponentName comp = new ComponentName(activity.getPackageName(), appClass);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(comp));
        activity.sendBroadcast(shortcut);
    }
}
