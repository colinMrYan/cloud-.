package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.AppGroupBean;
import com.inspur.emmcloud.bean.appcenter.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.bean.appcenter.RecommendAppWidgetBean;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by yufuchang on 2017/11/10.
 */

public class MyAppWidgetUtils {
    private static MyAppWidgetUtils myAppWidgetUtils;
    private Context context;

    /**
     * 需要展示Dialog的
     *
     * @param context
     */
    private MyAppWidgetUtils(Context context) {
        this.context = context;
    }

    /**
     * 我的应用推荐应用小部件单例模式
     *
     * @param context
     * @return
     */
    public static MyAppWidgetUtils getInstance(Context context) {
        if (myAppWidgetUtils == null) {
            synchronized (MyAppWidgetUtils.class) {
                if (myAppWidgetUtils == null) {
                    myAppWidgetUtils = new MyAppWidgetUtils(context);
                }
            }
        }
        return myAppWidgetUtils;
    }

    /**
     * 保存不要显示的日期时间
     *
     * @param context
     * @param notShowDate
     */
    public static void saveNotShowDate(Context context, long notShowDate) {
        PreferencesByUserAndTanentUtils.putLong(context, Constant.PREF_HAS_MY_APP_RECOMMEND, notShowDate);
    }

    /**
     * 判断是否显示推荐应用小部件
     *
     * @param context
     * @return
     */
    public static boolean isNeedShowMyAppRecommendWidgets(Context context) {
        long notShowTime = PreferencesByUserAndTanentUtils.getLong(context, Constant.PREF_HAS_MY_APP_RECOMMEND, 0);
        return System.currentTimeMillis() > notShowTime;
    }

    /**
     * 检查是否需要发起更新请求
     *
     * @param context
     * @return
     */
    public static boolean checkNeedUpdateMyAppWidget(Context context) {
        return StringUtils.isBlank(PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_MY_APP_RECOMMEND_DATA, "")) || Integer.parseInt(TimeUtils.getFormatYearMonthDay()) >
                Integer.parseInt(PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_MY_APP_RECOMMEND_DATE, "0"));
    }

    /**
     * 获取需要显示的appId列表
     *
     * @return
     */
    public static List<App> getShouldShowAppList(List<RecommendAppWidgetBean> recommendAppWidgetBeanList, List<AppGroupBean> appGroupBeanList) {
        List<String> appIdList = new ArrayList<>();
        for (int i = 0; i < recommendAppWidgetBeanList.size(); i++) {
            if (Integer.parseInt(recommendAppWidgetBeanList.get(i).getPeriod()) == (getNowHour() + 1)) {
                appIdList.addAll(recommendAppWidgetBeanList.get(i).getAppIdList());
                break;
            }
        }

        List<App> recommendAppWidgetList = new ArrayList<>();
        App app = new App();
        for (int i = 0; i < appIdList.size(); i++) {
            app.setAppID(appIdList.get(i));
            for (int j = 0; j < appGroupBeanList.size(); j++) {
                int index = appGroupBeanList.get(j).getAppItemList().indexOf(app);
                if (index != -1) {
                    recommendAppWidgetList.add(appGroupBeanList.get(j).getAppItemList().get(index));
                    break;
                }
            }
        }
        return recommendAppWidgetList;
    }

    /**
     * 获取当前小时数
     *
     * @return
     */
    public static int getNowHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 判断推荐应用小部件是否在有效期内
     *
     * @param expiredDate
     * @return
     */
    public static boolean isEffective(long expiredDate) {
        return expiredDate > System.currentTimeMillis();
    }

    /**
     * 获取我的应用推荐小部件
     */
    public void getMyAppWidgetsFromNet() {
        if (NetUtils.isNetworkConnected(context, false)) {
            MyAppAPIService appAPIService = new MyAppAPIService(context);
            appAPIService.setAPIInterface(new WebService());
            appAPIService.getRecommendAppWidgetList();
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnRecommendAppWidgetListSuccess(GetRecommendAppWidgetListResult getRecommendAppWidgetListResult) {
            //发送到MyAPPFragment updateMyAppWidegts方法
            EventBus.getDefault().post(getRecommendAppWidgetListResult);
            PreferencesByUserAndTanentUtils.putString(context, Constant.PREF_MY_APP_RECOMMEND_DATA, getRecommendAppWidgetListResult.getResponse());
            PreferencesByUserAndTanentUtils.putString(context, Constant.PREF_MY_APP_RECOMMEND_DATE, TimeUtils.getFormatYearMonthDay());
            PreferencesByUserAndTanentUtils.putLong(context, Constant.PREF_MY_APP_RECOMMEND_EXPIREDDATE, getRecommendAppWidgetListResult.getExpiredDate());
        }

        @Override
        public void returnRecommendAppWidgetListFail(String error, int errorCode) {
        }
    }
}
