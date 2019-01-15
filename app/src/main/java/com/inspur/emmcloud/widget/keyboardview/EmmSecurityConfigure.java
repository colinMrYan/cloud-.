package com.inspur.emmcloud.widget.keyboardview;

import android.graphics.Color;

/**
 * 安全键盘相关配置
 *
 * @author yidong (onlyloveyd@gmaol.com)
 * @date 2018/6/22 07:45
 */
public class EmmSecurityConfigure {

    /**
     * 键盘类型选中颜色
     */
    private int selectedColor = 0xff66aeff;

    /**
     * 键盘类型未选中颜色
     */
    private int unselectedColor = Color.BLACK;

    /**
     * 数字键盘使能
     */
    private boolean isNumberEnabled = true;

    /**
     * 字母键盘使能
     */
    private boolean isLetterEnabled = true;

    /**
     * 符号键盘使能
     */
    private boolean isSymbolEnabled = true;

    /**
     * 默认选中键盘
     */
    private EmmKeyboardType defaultKeyboardType = EmmKeyboardType.LETTER;

    public EmmSecurityConfigure() {
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public EmmSecurityConfigure setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
        return this;
    }

    public int getUnselectedColor() {
        return unselectedColor;
    }

    public EmmSecurityConfigure setUnselectedColor(int unselectedColor) {
        this.unselectedColor = unselectedColor;
        return this;
    }

    public boolean isNumberEnabled() {
        return isNumberEnabled;
    }

    public EmmSecurityConfigure setNumberEnabled(boolean numberEnabled) {
        this.isNumberEnabled = numberEnabled;
        return this;
    }

    public boolean isLetterEnabled() {
        return isLetterEnabled;
    }

    public EmmSecurityConfigure setLetterEnabled(boolean letterEnabled) {
        this.isLetterEnabled = letterEnabled;
        return this;
    }

    public boolean isSymbolEnabled() {
        return isSymbolEnabled;
    }

    public EmmSecurityConfigure setSymbolEnabled(boolean symbolEnabled) {
        this.isSymbolEnabled = symbolEnabled;
        return this;
    }

    public EmmKeyboardType getDefaultKeyboardType() {
        return defaultKeyboardType;
    }

    public EmmSecurityConfigure setDefaultKeyboardType(EmmKeyboardType defaultKeyboardType) {
        this.defaultKeyboardType = defaultKeyboardType;
        return this;
    }
}
