package com.inspur.emmcloud.web.util;

import java.text.DecimalFormat;

public class WebFormatUtil {
    private static double MBDATA = 1048576.0;
    private static double KBDATA = 1024.0;

    /**
     * 格式化数据
     **/
    public static String setFormat(long data) {
        // TODO Auto-generated method stub
        if (data < KBDATA) {
            return data + "B";
        } else if (data < MBDATA) {
            return new DecimalFormat(("####0.00")).format(data / KBDATA) + "KB";
        } else {
            return new DecimalFormat(("####0.00")).format(data / MBDATA) + "MB";
        }
    }
}
