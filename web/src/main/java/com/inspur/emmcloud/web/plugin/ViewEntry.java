package com.inspur.emmcloud.web.plugin;

/**
 * 传递参数的实体类用于实现窗口和webview相关参数的传递
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class ViewEntry {
    // 网页url
    public String url;
    // webview的名称
    public String viewName;
    // 窗口的名称
    public String windName;
    // 动画ID
    public int animId = -1;
    // 动画延时
    public long animDuration;
    // 字体大小
    public int fontsize;
    // 页面宽度
    public int width;
    // 页面高度
    public int height;
    // 页面X坐标
    public int x;
    // 页面Y坐标
    public int y;
    // 窗口弹动的位置（顶部，底部）
    public int flag = -1;
    // 实体类
    public Object obj;
    // 侧边栏显示比例
    public float scale;
    //指示执行的操作
    public int execution = -1;

}
