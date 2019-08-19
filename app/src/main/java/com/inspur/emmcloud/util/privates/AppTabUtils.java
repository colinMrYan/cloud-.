package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.res.Configuration;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.MainTabPayLoad;
import com.inspur.emmcloud.bean.system.MainTabProperty;
import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.bean.system.MainTabTitleResult;
import com.inspur.emmcloud.bean.system.navibar.NaviBarModel;
import com.inspur.emmcloud.bean.system.navibar.NaviBarScheme;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2017/4/27.
 */

public class AppTabUtils {
    public static String getTabTitle(Context context, String tabKey, String tabCompont) {
        String appTabs = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
//        ArrayList<MainTabResult> tabList = new GetAppMainTabResult(appTabs).getMainTabPayLoad().getMainTabResultList();
        ArrayList<MainTabResult> tabList = getMainTabList(context);
        String tabCompontText = !StringUtils.isBlank(tabCompont) ? tabCompont : getCompont(tabKey);
        MainTabResult tab = getTabByTabKey(tabList, tabCompontText);
        if (tab == null) {
            return "";
        }
        Configuration config = context.getResources().getConfiguration();
        String environmentLanguage = config.locale.getLanguage();
        if (environmentLanguage.toLowerCase().equals("zh") || environmentLanguage.toLowerCase().equals("zh-Hans".toLowerCase())) {
            return tab.getMainTabTitleResult().getZhHans();
        } else if (environmentLanguage.toLowerCase().equals("zh-Hant".toLowerCase())) {
            return tab.getMainTabTitleResult().getZhHant();
        } else if (environmentLanguage.toLowerCase().equals("en-US".toLowerCase()) ||
                environmentLanguage.toLowerCase().equals("en".toLowerCase())) {
            return tab.getMainTabTitleResult().getEnUS();
        } else {
            return tab.getMainTabTitleResult().getZhHans();
        }
    }

    private static ArrayList<MainTabResult> getMainTabList(Context context) {
        ArrayList<MainTabResult> mainTabResultList = null;
        String currentTabLayoutName = PreferencesByUserAndTanentUtils.getString(context,Constant.APP_TAB_LAYOUT_NAME,"");
        NaviBarModel naviBarModel = new NaviBarModel(PreferencesByUserAndTanentUtils.getString(context,Constant.APP_TAB_LAYOUT_DATA,""));
        List<NaviBarScheme> naviBarSchemeList = naviBarModel.getNaviBarPayload().getNaviBarSchemeList();
        for (int i = 0; i < naviBarSchemeList.size(); i++) {
            if(naviBarSchemeList.get(i).getName().equals(currentTabLayoutName)){
                mainTabResultList = naviBarSchemeList.get(i).getMainTabResultList();
            }
        }
        if(mainTabResultList == null){
            String appTabs = PreferencesByUserAndTanentUtils.getString(context,
                    Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
            GetAppMainTabResult getAppMainTabResult = new GetAppMainTabResult(appTabs);
            // 发送到MessageFragment
            EventBus.getDefault().post(getAppMainTabResult);
            mainTabResultList = getAppMainTabResult.getMainTabPayLoad().getMainTabResultList();
        }

        return mainTabResultList;
    }


    public static String getTabTitle(Context context, String tabKey) {
        return getTabTitle(context, tabKey, "");
    }

    /**
     * @param tabkey
     * @return
     */
    private static String getCompont(String tabkey) {
        if (tabkey.startsWith("CommunicationFragment") || tabkey.startsWith("CommunicationV0Fragment")) {
            return Constant.APP_TAB_BAR_COMMUNACATE;
        } else if (tabkey.equals("FindFragment")) {
            return Constant.APP_TAB_BAR_RN_FIND;
        } else if (tabkey.equals("WorkFragment")) {
            return Constant.APP_TAB_BAR_WORK;
        } else if (tabkey.equals("MyAppFragment")) {
            return Constant.APP_TAB_BAR_APPLICATION;
        } else if (tabkey.equals("MoreFragment")) {
            return Constant.APP_TAB_BAR_PROFILE;
        } else if (tabkey.equals("ContactSearchFragment")) {
            return Constant.APP_TAB_BAR_CONTACT;
        } else if (tabkey.equals("NotSupportFragment")) {
            return "";
        }
        return "";
    }

    /**
     * 根据tabkey取出title
     *
     * @param tabList
     * @return
     */
    public static MainTabResult getTabByTabKey(ArrayList<MainTabResult> tabList, String tabCompont) {
        for (int i = 0; i < tabList.size(); i++) {
            if (tabList.get(i).getUri().equals(tabCompont)) {
                return tabList.get(i);
            }
        }
        return null;
    }

    /**
     * 获取MainTabProperty
     *
     * @param context
     * @param tabKey
     * @return
     */
    public static MainTabProperty getMainTabProperty(Context context, String tabKey) {
        String appTabs = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
        ArrayList<MainTabResult> tabList = new GetAppMainTabResult(appTabs).getMainTabPayLoad().getMainTabResultList();
        String tabCompont = getCompont(tabKey);
        MainTabResult tab = getTabByTabKey(tabList, tabCompont);
        if (tab != null) {
            return tab.getMainTabProperty();
        }
        return null;
    }

    /**
     * 获取当前mainTabList
     *
     * @param context
     * @return
     */
    public static ArrayList<MainTabResult> getMainTabResultList(Context context) {
        ArrayList<MainTabResult> mainTabResultList = new ArrayList<>();
        String currentTabLayoutName = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(),Constant.APP_TAB_LAYOUT_NAME,"");
        NaviBarModel naviBarModel = new NaviBarModel(PreferencesByUserAndTanentUtils.getString(context,Constant.APP_TAB_LAYOUT_DATA,""));
        List<NaviBarScheme> naviBarSchemeList = naviBarModel.getNaviBarPayload().getNaviBarSchemeList();
        //首先根据用户设置的模式来获取naviBarSchemeList
        for (int i = 0; i < naviBarSchemeList.size(); i++) {
            if(naviBarSchemeList.get(i).getName().equals(currentTabLayoutName)){
                mainTabResultList = naviBarSchemeList.get(i).getMainTabResultList();
                break;
            }
        }
        //如果没有用户设置的模式或者是第一次安装默认的模式  使用defaultScheme
        if((mainTabResultList == null || mainTabResultList.size() == 0) && naviBarSchemeList.size() > 0){
            String defaultTabLayoutName = naviBarModel.getNaviBarPayload().getDefaultScheme();
            for (int i = 0; i < naviBarSchemeList.size(); i++) {
                if(naviBarSchemeList.get(i).getName().equals(defaultTabLayoutName)){
                    mainTabResultList = naviBarSchemeList.get(i).getMainTabResultList();
                    PreferencesByUserAndTanentUtils.putString(context,Constant.APP_TAB_LAYOUT_NAME,defaultTabLayoutName);
                    break;
                }
            }
        }
        //如果前面两个都没有则使用mainTab
        if (mainTabResultList == null || mainTabResultList.size() == 0) {
            String appTabs = PreferencesByUserAndTanentUtils.getString(context,
                    Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
            GetAppMainTabResult getAppMainTabResult = new GetAppMainTabResult(appTabs);
            mainTabResultList = getAppMainTabResult.getMainTabPayLoad().getMainTabResultList();
        }
        //如果MainTab也没有则使用默认
        if(mainTabResultList == null || mainTabResultList.size() == 0){
            mainTabResultList.add(getApplicationMainTab());
            mainTabResultList.add(getMineTab());
        }
        //最终得到tab的List之后发送给CommunicationFragment
        if (mainTabResultList != null) {
            GetAppMainTabResult getAppMainTabResult = new GetAppMainTabResult();
            MainTabPayLoad mainTabPayLoad = new MainTabPayLoad();
            mainTabPayLoad.setMainTabResultList(mainTabResultList);
            getAppMainTabResult.setMainTabPayLoad(mainTabPayLoad);
            // 发送到CommunicationFragment
            EventBus.getDefault().post(getAppMainTabResult);
        }
        return mainTabResultList;
    }

    /**
     * 生成applicationMainTab
     *
     * @return
     */
    private static MainTabResult getApplicationMainTab() {
        MainTabResult applicationTabResult = new MainTabResult();
        applicationTabResult.setIcon("application");
        applicationTabResult.setName("application");
        applicationTabResult.setUri(Constant.APP_TAB_BAR_APPLICATION);
        applicationTabResult.setType("native");
        applicationTabResult.setSelected(false);
        MainTabTitleResult applicationTabTitleResult = new MainTabTitleResult();
        applicationTabTitleResult.setZhHant("應用");
        applicationTabTitleResult.setZhHans("应用");
        applicationTabTitleResult.setEnUS("Apps");
        applicationTabResult.setMainTabTitleResult(applicationTabTitleResult);
        return applicationTabResult;
    }

    /**
     * 生成mainTab
     *
     * @return
     */
    private static MainTabResult getMineTab() {
        MainTabResult mineTabResult = new MainTabResult();
        mineTabResult.setIcon("me");
        mineTabResult.setName("me");
        mineTabResult.setUri(Constant.APP_TAB_BAR_PROFILE);
        mineTabResult.setType("native");
        mineTabResult.setSelected(false);
        MainTabTitleResult mainTabTitleResult = new MainTabTitleResult();
        mainTabTitleResult.setZhHant("我");
        mainTabTitleResult.setZhHans("我");
        mainTabTitleResult.setEnUS("Me");
        mineTabResult.setMainTabTitleResult(mainTabTitleResult);
        return mineTabResult;
    }

}
