package com.inspur.emmcloud.widget.keyboardview;

public enum EmmKeyboardType {

    /**
     * 字母键盘
     */
    LETTER(0, "字母"),

    /**
     * 数字键盘
     */
    NUMBER(1, "数字"),

    /**
     * 符号键盘
     */
    SYMBOL(2, "符号");

    private int code;
    private String name;

    EmmKeyboardType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
