package com.inspur.emmcloud.basemodule.ui;

import android.graphics.Color;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;

import static com.inspur.emmcloud.basemodule.ui.BaseActivity.THEME_DARK;

public class DarkUtil {


    /**
     * 兜底
     * 暗黑模式适配获取文字主色
     * @return 文字主色
     */
    public static int getTextColor(){
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        return Color.parseColor(currentThemeNo != THEME_DARK ? "#333333":"#FFFFFF");
    }

    /**
     * 兜底
     * 暗黑模式适配获取文字主色
     * @return 文字主色
     */
    public static int getSendButtonColor(){
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        return Color.parseColor(currentThemeNo != THEME_DARK ? "#000000":"#FFFFFF");
    }

    /**
     * 兜底
     * 暗黑模式适配获取背景色
     * @return 背景色
     */
    public static int getTextContainerColor(){
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        return Color.parseColor(currentThemeNo != THEME_DARK ? "#FFFFFF":"#1C1C1E");
    }

    /**
     * 兜底
     * 暗黑模式适配获取list下划线颜色
     * @return list下划线颜色
     */
    public static int getListDividerColor(){
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        return Color.parseColor(currentThemeNo != THEME_DARK ? "#dddddd":"#484951");
    }

    /**
     * 兜底
     * 暗黑模式适配获取文字容器二级背景颜色
     * @return 文字容器二级背景颜色
     */
    public static int getTextContainerLevelTwoColor(){
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        return Color.parseColor(currentThemeNo != THEME_DARK ? "#f6f6f6":"#292929");
    }

}
