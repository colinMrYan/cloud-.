package com.inspur.emmcloud.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by yufuchang on 2017/6/15.
 */

public class ParseHtmlUtils {

    /**
     * 解析html内容获取指定标签
     * @param html
     * @param metaLable
     * @return
     */
    public static Elements getDataFromHtml(String html,String metaLable){
        Document doc = Jsoup.parse(html);
        Elements metas = doc.select(metaLable);
        for (int i = 0; i < metas.size(); i++) {
            LogUtils.YfcDebug( "content："+metas.get(i).attr("content"));
        }
        return metas;
    }
}
