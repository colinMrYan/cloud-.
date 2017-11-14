package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.Context;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.GetMyAppWidgetResult;
import com.inspur.emmcloud.bean.RecommendAppWidgetBean;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by yufuchang on 2017/11/10.
 */

public class MyAppWidgetUtils {
    private Context context;
    private LoadingDialog loadingDlg;
    private static MyAppWidgetUtils myAppWidgetUtils;

    /**
     * 我的应用推荐应用小部件单例模式
     * @param activity
     * @param needDialog
     * @return
     */
    public  static MyAppWidgetUtils getInstance(Activity activity,boolean needDialog){
        if(myAppWidgetUtils == null){
            synchronized (MyAppWidgetUtils.class){
                if(myAppWidgetUtils == null){
                    myAppWidgetUtils = new MyAppWidgetUtils(activity,needDialog);
                }
            }
        }
        return myAppWidgetUtils;
    }

    /**
     * 需要展示Dialog的
     * @param activity
     * @param needDialog
     */
    private MyAppWidgetUtils(Activity activity,boolean needDialog){
        this.context = activity;
        loadingDlg = new LoadingDialog(context);
        getMyAppWidgetsFromNet(needDialog);
    }

    /**
     * 获取我的应用推荐小部件
     * @param needDialog
     */
    private void getMyAppWidgetsFromNet(boolean needDialog){
        if(NetUtils.isNetworkConnected(context)){
            if(needDialog){
                loadingDlg.show();
            }
            MyAppAPIService appAPIService = new MyAppAPIService(context);
            appAPIService.setAPIInterface(new WebService());
            appAPIService.getMyAppWidgets();
        }
    }

    /**
     * 获取需要显示的appId列表
     * @param recommendAppWidgetBeanList
     * @return
     */
    public static List<String> getShouldShowAppList(List<RecommendAppWidgetBean> recommendAppWidgetBeanList){
        List<String> appIdList = new ArrayList<>();
        for(int i = 0; i < recommendAppWidgetBeanList.size(); i++){
            if(Integer.parseInt(recommendAppWidgetBeanList.get(i).getPeriod()) == getNowHour()){
                appIdList.addAll(recommendAppWidgetBeanList.get(i).getAppIdList());
                break;
            }
        }
        return appIdList;
    }

    /**
     * 获取当前小时数
     * @return
     */
    public static int getNowHour(){
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 判断推荐应用小部件是否在有效期内
     * @param expiredDate
     * @return
     */
    public static boolean isEffective(long expiredDate){
        return expiredDate > System.currentTimeMillis();
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnMyAppWidgetsSuccess(GetMyAppWidgetResult getMyAppWidgetResult) {
            if(loadingDlg.isShowing()){
                loadingDlg.dismiss();
            }
            PreferencesByUserAndTanentUtils.putString(context,getMyAppWidgetResult.getResponse(),"");
        }

        @Override
        public void returnMyAppWidgetsFail(String error, int errorCode) {
            if(loadingDlg.isShowing()){
                loadingDlg.dismiss();
            }
        }
    }
}
