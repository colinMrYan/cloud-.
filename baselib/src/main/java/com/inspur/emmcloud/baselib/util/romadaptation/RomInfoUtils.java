package com.inspur.emmcloud.baselib.util.romadaptation;

import com.inspur.emmcloud.baselib.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 变更获取getRomNameInfo的实现改为通过厂商确定系统181101
 * Created by yufuchang on 2018/10/31.
 */

public class RomInfoUtils {
    private static final String MIUI = "miui";//小米
    private static final String EMUI = "emotionui";//华为
    private static final String FLYME = "flyme";//魅族
    private static final String COLOROS = "coloros";//OPPO
    private static final String FUNTOUCH = "funtouch";//VIVO
    private static final String EXPERIENCE = "experience";//三星
    private static final String UNKNOW = "unknow";//未知

    private static final String RUNTIME_SYS_VERSION_MIUI = "ro.miui.ui.version.name";
    private static final String RUNTIME_SYS_VERSION_EMUI = "ro.build.version.emui";
    private static final String RUNTIME_SYS_VERSION_OPPO = "ro.build.version.opporom";
    private static final String RUNTIME_SYS_VERSION_VIVO = "ro.vivo.os.build.display.id";
    private static final String RUNTIME_SYS_VERSION_SAMSUNG = "ro.build.version.incremental";
    private static final String RUNTIME_SYS_VERSION_DEFAULT = "ro.comp.system_version";

    /**
     * 获取RomName的信息如emotionui
     *
     * @return
     */
    public static String getRomNameInfo() {
        String romNameInfo = "";
        String manufacturer = android.os.Build.MANUFACTURER;
        switch (manufacturer) {
            case "huawei":
                romNameInfo = EMUI;
                break;
            case "xiaomi":
                romNameInfo = MIUI;
                break;
            case "meizu":
                romNameInfo = FLYME;
                break;
            case "vivo":
                romNameInfo = FUNTOUCH;
                break;
            case "oppo":
                romNameInfo = COLOROS;
                break;
            case "samsung":
                romNameInfo = EXPERIENCE;
                break;
            default:
                romNameInfo = StringUtils.isBlank(manufacturer) ? UNKNOW : manufacturer;
                break;
        }
        return romNameInfo;
    }

    /**
     * 获取rom版本信息如EmotionUI_8.0.0
     *
     * @return
     */
    public static String getRomVersionInfo() {
        String romVersionInfo = "";
        switch (getRomNameInfo()) {
            case MIUI:
                romVersionInfo = getRomProperty(RUNTIME_SYS_VERSION_MIUI);
                break;
            case EMUI:
                romVersionInfo = getRomProperty(RUNTIME_SYS_VERSION_EMUI);
                break;
            case COLOROS:
                romVersionInfo = getRomProperty(RUNTIME_SYS_VERSION_OPPO);
                break;
            case FUNTOUCH:
                romVersionInfo = getRomProperty(RUNTIME_SYS_VERSION_VIVO);
                break;
            case EXPERIENCE:
                romVersionInfo = getRomProperty(RUNTIME_SYS_VERSION_SAMSUNG);
                break;
            default:
                romVersionInfo = getRomProperty(RUNTIME_SYS_VERSION_DEFAULT);
                break;
        }
        return romVersionInfo;
    }

    private static String getRomProperty(String prop) {
        String line = "";
        BufferedReader reader = null;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("getprop " + prop);
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (p != null) {
                p.destroy();
            }
        }
        return line;
    }

}
