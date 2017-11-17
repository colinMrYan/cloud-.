package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TimeUtils;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2017/11/10.
 */

public class GetMyAppWidgetResult {
    private String response = "";
    private long expiredDate = 0L;
    private List<RecommendAppWidgetBean> recommendAppWidgetBeanList = new ArrayList<>();

    public GetMyAppWidgetResult(String response) {
        try {
            this.response = response;
            expiredDate = getExpiredDate(JSONUtils.getString(response, "ExpiredDate", ""));
            LogUtils.YfcDebug("转换时间戳：" + expiredDate);
            JSONArray jsonArray = JSONUtils.getJSONArray(response, "Recommends", new JSONArray());
            for (int i = 0; i < jsonArray.length(); i++) {
                recommendAppWidgetBeanList.add(new RecommendAppWidgetBean(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把失效时间转换为时间戳
     *
     * @param expiredDate
     * @return
     */
    private long getExpiredDate(String expiredDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy/MM/dd HH:mm:ss");
        long expireDate = StringUtils.isBlank(expiredDate)? 0 : TimeUtils.timeString2Calendar(expiredDate, simpleDateFormat).getTimeInMillis();
        return expireDate;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public long getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(long expiredDate) {
        this.expiredDate = expiredDate;
    }

    public List<RecommendAppWidgetBean> getRecommendAppWidgetBeanList() {
        return recommendAppWidgetBeanList;
    }

    public void setRecommendAppWidgetBeanList(List<RecommendAppWidgetBean> recommendAppWidgetBeanList) {
        this.recommendAppWidgetBeanList = recommendAppWidgetBeanList;
    }
}
