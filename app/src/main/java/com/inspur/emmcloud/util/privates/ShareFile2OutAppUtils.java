package com.inspur.emmcloud.util.privates;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ToastUtils;

import java.io.File;
import java.util.List;

/**
 * Created by libaochao on 2019/9/26.
 */

public class ShareFile2OutAppUtils {

    public static final String PACKAGE_WECHAT = "com.tencent.mm";
    public static final String PACKAGE_MOBILE_QQ = "com.tencent.mobileqq";
    public static final String PACKAGE_QZONE = "com.qzone";
    public static final String PACKAGE_SINA = "com.sina.weibo";

    // 判断是否安装指定app
    public static boolean isInstallApp(Context context, String app_package) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);
        if (pInfo != null) {
            for (int i = 0; i < pInfo.size(); i++) {
                String pn = pInfo.get(i).packageName;
                if (app_package.equals(pn)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 直接分享纯文本内容至QQ好友
     *
     * @param mContext
     * @param content
     */
    public static void shareText2QQ(Context mContext, String content) {
        if (isInstallApp(mContext, PACKAGE_MOBILE_QQ)) {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
            intent.putExtra(Intent.EXTRA_TEXT, content);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity"));
            mContext.startActivity(intent);
        } else {
            ToastUtils.show(mContext.getString(R.string.volume_please_install_qq));
        }
    }

    /**
     * 分享图片给QQ好友
     */
    public static void shareFileToQQ(Context mContext, String filePath) {
        if (isInstallApp(mContext, PACKAGE_MOBILE_QQ)) {
            try {
                Uri uriToImage = getFileUri(mContext, filePath);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.setType("*/*");
                // 遍历所有支持发送图片的应用。找到需要的应用
                ComponentName componentName = new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");
                shareIntent.setComponent(componentName);
                mContext.startActivity(Intent.createChooser(shareIntent, "Share"));
            } catch (Exception e) {
                ToastUtils.show(mContext.getString(R.string.volume_please_install_qq));
            }
        }
    }


    /**
     * 直接分享图片到微信好友
     *
     * @param context
     */
    public static void shareFile2WeChat(Context context, String filePath) {
        if (isInstallApp(context, PACKAGE_WECHAT)) {
            try {
                Uri uri = getFileUri(context, filePath);
                Intent intent = new Intent();
                ComponentName cop = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
                intent.setComponent(cop);
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(Intent.createChooser(intent, "Share"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ToastUtils.show(context.getString(R.string.volume_please_install_wechat));
        }
    }

    private static Uri getFileUri(Context context, String filePath) {
        File file = new File(filePath);
        final Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        } else {
            contentUri = Uri.fromFile(new File(filePath));
        }
        return contentUri;
    }


}
