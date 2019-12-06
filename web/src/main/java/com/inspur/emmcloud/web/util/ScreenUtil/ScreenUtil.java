package com.inspur.emmcloud.web.util.ScreenUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by libaochao on 2019/12/5.
 */

public class ScreenUtil {
    Context context;

    public ScreenUtil(Context context) {
        this.context = context;
    }

    public void screenShot(String path, String name) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            screenShotContentStateBar(path, name);
        } else {
            ScreenShotWithoutStateBar(path, name);
        }
    }

    public void screenShotContentStateBar(String path, String name) {
        String absolutePath = path + "/" + name;
        Intent intent = new Intent(context, ScreenShotActivity.class);
        intent.putExtra("path", absolutePath);
        context.startActivity(intent);
    }

    public void ScreenShotWithoutStateBar(String path, String name) {
        View view = ((Activity) context).getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        try {
            File file = new File(path, name);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ScreenShotWithoutStateBarByView(View view, String path, String name) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        try {
            File file = new File(path, name);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
