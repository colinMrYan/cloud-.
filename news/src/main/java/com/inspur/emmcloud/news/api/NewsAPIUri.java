package com.inspur.emmcloud.news.api;

import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;

public class NewsAPIUri {

    /**
     * 获取新闻
     *
     * @param url
     * @return
     */
    public static String getGroupNewsUrl(String url) {
        return WebServiceRouterManager.getInstance().getClusterNews() + url;
    }

    /**
     * 获取网页地址
     *
     * @param url
     * @return
     */
    public static String getGroupNewsHtmlUrl(String url) {
        return WebServiceRouterManager.getInstance().getClusterStorageLegacy() + url;
    }

    /**
     * 预览图片或视频
     **/
    public static String getPreviewUrl(String fileName) {
        return WebServiceRouterManager.getInstance().getClusterStorageLegacy() + "/res/stream/" + fileName;
    }

    /**
     * 得到集团新闻的Path
     *
     * @return
     */
    public static String getGroupNewsArticleUrl() {
        return "/res" + "/article" + "/";
    }

    /**
     * 获取新闻批示
     *
     * @param newsId
     * @return
     */
    public static String getNewsInstruction(String newsId) {
        return WebServiceRouterManager.getInstance().getClusterNews() + "/content/news/" + newsId + "/editor-comment";
    }

}
