package com.inspur.emmcloud.web.plugin.screenshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;

import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.web.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by libaochao on 2019/12/5.
 */

public class ScreenshotUtil {
    Context context;

    public ScreenshotUtil(Context context) {
        this.context = context;
    }

    public static String screenshot(Activity activity, String path, String name) {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            screenShotContentStateBar(path, name);
//        } else {
//            ScreenShotWithoutStateBar(path, name);
//        }
        return ScreenShotWithoutStateBar(activity, path, name);
    }

    public static String screenshot(Activity activity) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            ToastUtils.show(R.string.baselib_sd_not_exist);
            return null;
        }
        StringBuilder nameBuilder = new StringBuilder();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmssFFF");
        String time = format.format(Calendar.getInstance().getTime());
        nameBuilder.append("Screenshot_").append(time).append("_")
                .append(AppUtils.getPackageName(BaseApplication.getInstance())).append(".jpg");
        File dir = new File(MyAppConfig.LOCAL_CACHE_SCREENSHOTS_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return screenshot(activity, MyAppConfig.LOCAL_CACHE_SCREENSHOTS_PATH, nameBuilder.toString());
    }

    private static void screenShotContentStateBar(Activity activity, String path, String name) {
        String absolutePath = path + "/" + name;
        Intent intent = new Intent(activity, ScreenShotActivity.class);
        intent.putExtra("path", absolutePath);
        activity.startActivity(intent);
    }

    private static String ScreenShotWithoutStateBar(Activity activity, String path, String name) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        try {
            File file = new File(path, name);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return path + name;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
