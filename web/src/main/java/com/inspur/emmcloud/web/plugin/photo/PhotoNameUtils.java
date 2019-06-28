package com.inspur.emmcloud.web.plugin.photo;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;

public class PhotoNameUtils {
    public static String getFileName(Context context, int parm_encodingType) {
        return getFileName(context, 0, parm_encodingType);
    }

    public static String getFileName(Context context, int index, int parm_encodingType) {
        index = index + 1;
        String userName = PreferencesUtils.getString(context,
                "userName", "");
        String fileName = userName + "_" + System.currentTimeMillis() + "_" + index + (parm_encodingType == 0 ? ".jpg" : ".png");
        return fileName;
    }

    public static String getThumbnailFileName(Context context, int index, int parm_encodingType) {
        index = index + 1;
        String userName = PreferencesUtils.getString(context,
                "userName", "");
        String fileName = userName + "_" + System.currentTimeMillis() + "_" + "thumb_" + index + (parm_encodingType == 0 ? ".jpg" : ".png");
        return fileName;
    }

    public static String getListFileName(Context context, long time, int index, int parm_encodingType) {
        index = index + 1;
        String userName = PreferencesUtils.getString(context,
                "userName", "");
        String fileName = userName + "_" + time + "_" + index + (parm_encodingType == 0 ? ".jpg" : ".png");
        return fileName;
    }
}
