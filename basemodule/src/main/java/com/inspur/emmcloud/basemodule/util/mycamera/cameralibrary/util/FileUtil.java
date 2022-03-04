package com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.util;

import java.io.File;

public class FileUtil {

    public static boolean deleteFile(String url) {
        boolean result = false;
        File file = new File(url);
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }

}
