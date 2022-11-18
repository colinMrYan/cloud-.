package com.inspur.emmcloud.web.plugin.filetransfer;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * Imp插件文件路径统一处理类
 * 最终返回可操作的
 */
public class FilePathUtils {


    public static String BASE_PATH = MyAppConfig.LOCAL_IMP_USER_OPERATE_INTERNAL_FILE_DIC +
            BaseApplication.getInstance().getTanent() + "/"
            + BaseApplication.getInstance().getUid();
    public static String SDCARD_PREFIX = "sdcard:";

    public static String getRealPath(String filePath) {
        if (filePath.startsWith(SDCARD_PREFIX)) {
            String path = filePath.replace(SDCARD_PREFIX, "");
            return path;
        } else if (filePath.startsWith("http")) {
            return filePath;
        } else {
            File file = new File(BASE_PATH);
            if (!file.exists()) file.mkdirs();
            return filePath.startsWith("/") ? (BASE_PATH + filePath) : (BASE_PATH + "/" + filePath);
        }
    }

    /**
     * 判断是否是安全目录
     *
     * @param filePath
     * @return
     */
    public static boolean isSafePath(String filePath) {
        return !StringUtils.isBlank(filePath) && filePath.startsWith(BASE_PATH);
    }
}