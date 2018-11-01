package com.inspur.emmcloud.util.common.romadaptation;

import com.inspur.emmcloud.util.common.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 目前支持四种手机ROM的判断
 * 还没有完全对四种手机的各种版本做测试，现有手机测试是可以的
 * Created by yufuchang on 2018/10/31.
 */

public class RomInfoUtils {
    public static final String MIUI = "miui";
    public static final String EMUI = "emotionui";
    public static final String FLYME = "flyme";
    public static final String COLOROS = "coloros";
    public static final String UNKNOW = "unknow";

    public static final String RUNTIME_SYS_NAME_EMUI = "ro.build.version.emui";
    public static final String RUNTIME_SYS_NAME_MIUI = "ro.miui.ui.version.name";
    public static final String RUNTIME_SYS_NAME_OPPO = "ro.build.version.opporom";
    public static final String RUNTIME_SYS_NAME_DEFAULT = "ro.build.display.id";

    public static final String RUNTIME_SYS_VERSION_MIUI = "ro.miui.ui.version.name";
    public static final String RUNTIME_SYS_VERSION_EMUI = "ro.build.version.emui";
    public static final String RUNTIME_SYS_VERSION_OPPO = "ro.build.version.opporom";
    public static final String RUNTIME_SYS_VERSION_DEFAULT = "ro.comp.system_version";

    public static String getRomNameInfo() {
        String romNameInfo = "";
        if (!StringUtils.isBlank(getRomProperty(RUNTIME_SYS_NAME_MIUI))) {
            romNameInfo = MIUI;
        } else if (!StringUtils.isBlank(getRomProperty(RUNTIME_SYS_NAME_OPPO))) {
            romNameInfo = COLOROS;
        } else if (getRomProperty(RUNTIME_SYS_NAME_EMUI).toLowerCase().contains(EMUI)) {
            romNameInfo = EMUI;
        } else if (getRomProperty(RUNTIME_SYS_NAME_DEFAULT).toLowerCase().contains(FLYME)) {
            romNameInfo = FLYME;
        } else {
            romNameInfo = UNKNOW;
        }
        return romNameInfo;
    }

    public static String getRomVersionInfo() {
        String romNameInfo = getRomNameInfo();
        String romVersionInfo = "";
        switch (romNameInfo) {
            case MIUI:
                romVersionInfo = getRomProperty(RUNTIME_SYS_VERSION_MIUI);
                break;
            case EMUI:
                romVersionInfo = getRomProperty(RUNTIME_SYS_VERSION_EMUI);
                break;
            case COLOROS:
                romVersionInfo = getRomProperty(RUNTIME_SYS_VERSION_OPPO);
                break;
            case FLYME:
            case UNKNOW:
                romVersionInfo = getRomProperty(RUNTIME_SYS_VERSION_DEFAULT);
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
