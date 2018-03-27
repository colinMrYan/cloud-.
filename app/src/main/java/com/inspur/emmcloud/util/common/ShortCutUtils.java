package com.inspur.emmcloud.util.common;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.inspur.emmcloud.R;

import java.util.List;

/**
 * Created by yufuchang on 2017/3/30.
 */

public class ShortCutUtils {
    /**
     * 创建图标
     * @param contxt
     * @param clz
     */
    public static void createShortCut(Context contxt,Class clz,String shortCutName,String appPathOrUri,String type,int icon) {
        //String applicationName=getApplicationName(contxt);
        if(icon == 0){
            icon = R.drawable.ic_launcher;
        }
        String applicationName = shortCutName;//程序名称，不是packageName
        if (isShortCutExist(contxt,applicationName)) {// 如果已经创建了一次就不会再创建了
            return;
        }
        Intent sIntent = new Intent(Intent.ACTION_MAIN);
        sIntent.addCategory(Intent.CATEGORY_LAUNCHER);// 加入action,和category之后，程序卸载的时候才会主动将该快捷方式也卸载
        sIntent.setClass(contxt, clz);//点击后进入的Activity

        Intent installer = new Intent();
        installer.putExtra("duplicate", false);//false标示不重复创建
        Intent intentTodo = new Intent();
        if(type.equals("ecc-app-react-native")){
//            intentTodo.putExtra("ecc-app-react-native",appPathOrUri);
//            intentTodo.setClass(contxt, ReactNativeAppActivity.class);
            sIntent.putExtra("ecc-app-react-native",appPathOrUri);
        }else if(type.equals("ecc-app-web-hcm")){
            sIntent.putExtra("uri",appPathOrUri);
        }else if(type.equals("ecc-app-native")){
//            sIntent.setClass(contxt, clz);
        }else if(type.equals("ecc-app-web-gs")){
            sIntent.putExtra("uri",appPathOrUri);
//            sIntent.setClass(contxt, clz);
        }
        installer.putExtra(Intent.EXTRA_SHORTCUT_INTENT,intentTodo);
        installer.putExtra("android.intent.extra.shortcut.INTENT", sIntent);
        //设置应用的名称
        installer.putExtra("android.intent.extra.shortcut.NAME", applicationName);
        //设置图标
        installer.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.fromContext(contxt, icon));
        installer.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        LocalBroadcastManager.getInstance(contxt).sendBroadcast(installer);//发送安装桌面图标的通知
    }


    /**
     * 创建快捷方式，以bitmap指定图标的方式
     * @param contxt
     * @param clz
     * @param shortCutName
     * @param appPathOrUri
     * @param type
     * @param iconBitmap
     */
    public static void createShortCut(Context contxt,Class clz,String shortCutName,String appPathOrUri,String type,Bitmap iconBitmap) {
        String applicationName = shortCutName;//程序名称，不是packageName
        if (isShortCutExist(contxt,applicationName)) {// 如果已经创建了一次就不会再创建了
            return;
        }
        Intent sIntent = new Intent(Intent.ACTION_MAIN);
        sIntent.addCategory(Intent.CATEGORY_LAUNCHER);// 加入action,和category之后，程序卸载的时候才会主动将该快捷方式也卸载
        sIntent.setClass(contxt, clz);//点击后进入的Activity
        Intent installer = new Intent();
        installer.putExtra("duplicate", false);//false标示不重复创建
        Intent intentTodo = new Intent();
        if(type.equals("ecc-app-react-native")){
            sIntent.putExtra("ecc-app-react-native",appPathOrUri);
        }else if(type.equals("ecc-app-web-hcm")){
            sIntent.putExtra("uri",appPathOrUri);
        }else if(type.equals("ecc-app-native")){
//            sIntent.setClass(contxt, clz);
        }else if(type.equals("ecc-app-web-gs")){
            sIntent.putExtra("uri",appPathOrUri);
//            sIntent.setClass(contxt, clz);
        }
        installer.putExtra(Intent.EXTRA_SHORTCUT_INTENT,intentTodo);
        installer.putExtra("android.intent.extra.shortcut.INTENT", sIntent);
        //设置应用的名称
        installer.putExtra("android.intent.extra.shortcut.NAME", applicationName);
        //设置图标
        installer.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconBitmap);
//        installer.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.fromContext(contxt, icon));
        installer.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        LocalBroadcastManager.getInstance(contxt).sendBroadcast(installer);//发送安装桌面图标的通知
    }

    private static String AUTHORITY = null;

    public static boolean isShortCutExist(Context context, String title) {


        boolean isInstallShortcut = false;

        if (null == context || TextUtils.isEmpty(title))
            return isInstallShortcut;

        if (TextUtils.isEmpty(AUTHORITY))
            AUTHORITY = getAuthorityFromPermission(context);

        final ContentResolver cr = context.getContentResolver();

        if (!TextUtils.isEmpty(AUTHORITY)) {
            try {
                final Uri CONTENT_URI = Uri.parse(AUTHORITY);

                Cursor c = cr.query(CONTENT_URI, new String[] { "title",
                                "iconResource" }, "title=?", new String[] { title },
                        null);

                // XXX表示应用名称。
                if (c != null && c.getCount() > 0) {
                    isInstallShortcut = true;
                }
                if (null != c && !c.isClosed())
                    c.close();
            } catch (Exception e) {
                // TODO: handle exception
                LogUtils.YfcDebug("isShortCutExist"+ e.getMessage());
            }

        }
        return isInstallShortcut;

    }

    public static String getCurrentLauncherPackageName(Context context) {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = context.getPackageManager()
                .resolveActivity(intent, 0);
        if (res == null || res.activityInfo == null) {
            // should not happen. A home is always installed, isn't it?
            return "";
        }
        if (res.activityInfo.packageName.equals("android")) {
            return "";
        } else {
            return res.activityInfo.packageName;
        }
    }

    public static String getAuthorityFromPermissionDefault(Context context) {

        return getThirdAuthorityFromPermission(context,
                "com.android.launcher.permission.READ_SETTINGS");
    }

    public static String getThirdAuthorityFromPermission(Context context,
                                                         String permission) {
        if (TextUtils.isEmpty(permission)) {
            return "";
        }

        try {
            List<PackageInfo> packs = context.getPackageManager()
                    .getInstalledPackages(PackageManager.GET_PROVIDERS);
            if (packs == null) {
                return "";
            }
            for (PackageInfo pack : packs) {
                ProviderInfo[] providers = pack.providers;
                if (providers != null) {
                    for (ProviderInfo provider : providers) {
                        if (permission.equals(provider.readPermission)
                                || permission.equals(provider.writePermission)) {
                            String authority = provider.authority;
                            if (!StringUtils.isBlank(authority)
                                    && (authority
                                    .contains(".launcher.settings")
                                    || authority
                                    .contains(".twlauncher.settings") || authority
                                    .contains(".launcher2.settings")))
                                return authority;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getAuthorityFromPermission(Context context) {
        // 获取默认
        String authority = getAuthorityFromPermissionDefault(context);
        // 获取特殊第三方
        if (authority == null || authority.trim().equals("")) {
            String packageName = getCurrentLauncherPackageName(context);
            packageName += ".permission.READ_SETTINGS";
            authority = getThirdAuthorityFromPermission(context, packageName);
        }
        // 还是获取不到，直接写死
        if (TextUtils.isEmpty(authority)) {
            int sdkInt = android.os.Build.VERSION.SDK_INT;
            if (sdkInt < 8) { // Android 2.1.x(API 7)以及以下的
                authority = "com.android.launcher.settings";
            } else if (sdkInt < 19) {// Android 4.4以下
                authority = "com.android.launcher2.settings";
            } else {// 4.4以及以上
                authority = "com.android.launcher3.settings";
            }
        }
        authority = "content://" + authority + "/favorites?notify=true";
        return authority;

    }

    public static void delShortcut(Activity activity) {
        Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        //快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, activity.getString(R.string.app_name));
        String appClass = activity.getPackageName() + "." + activity.getLocalClassName();
        ComponentName comp = new ComponentName(activity.getPackageName(), appClass);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(comp));
        LocalBroadcastManager.getInstance(activity).sendBroadcast(shortcut);
    }
}