package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.res.Configuration;

import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.MainTabProperty;
import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.StringUtils;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2017/4/27.
 */

public class AppTabUtils {
    public static String getTabTitle(Context context, String tabKey, String tabCompont){
        String appTabs = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_APP_TAB_BAR_INFO_CURRENT,"");
        ArrayList<MainTabResult> tabList = new GetAppMainTabResult(appTabs).getMainTabPayLoad().getMainTabResultList();
        String tabCompontText = !StringUtils.isBlank(tabCompont)?tabCompont:getCompont(tabKey);
        MainTabResult tab = getTabByTabKey(tabList,tabCompontText);
        if(tab == null){
            return "";
        }
        Configuration config = context.getResources().getConfiguration();
        String environmentLanguage = config.locale.getLanguage();
        if(environmentLanguage.toLowerCase().equals("zh")||environmentLanguage.toLowerCase().equals("zh-Hans".toLowerCase())){
            return tab.getMainTabTitleResult().getZhHans();
        }else if(environmentLanguage.toLowerCase().equals("zh-Hant".toLowerCase())){
            return tab.getMainTabTitleResult().getZhHant();
        }else if(environmentLanguage.toLowerCase().equals("en-US".toLowerCase())||
                environmentLanguage.toLowerCase().equals("en".toLowerCase())){
            return tab.getMainTabTitleResult().getEnUS();
        }else{
            return tab.getMainTabTitleResult().getZhHans();
        }
    }


    public static String getTabTitle(Context context,String tabKey){
        return getTabTitle(context,tabKey,"");
    }

    /**
     *
     * @param tabkey
     * @return
     */
    private static String getCompont(String tabkey) {
        if(tabkey.startsWith("CommunicationFragment")||tabkey.startsWith("CommunicationV0Fragment") ){
            return Constant.APP_TAB_BAR_COMMUNACATE;
        }else if(tabkey.equals("FindFragment")){
            return Constant.APP_TAB_BAR_RN_FIND;
        }else if(tabkey.equals("WorkFragment")){
            return Constant.APP_TAB_BAR_WORK;
        }else if(tabkey.equals("MyAppFragment")){
            return Constant.APP_TAB_BAR_APPLICATION;
        }else if(tabkey.equals("MoreFragment")){
            return Constant.APP_TAB_BAR_PROFILE;
        }else if(tabkey.equals("ContactSearchFragment")){
            return Constant.APP_TAB_BAR_CONTACT;
        }else if(tabkey.equals("NotSupportFragment")){
            return "";
        }
        return  "";
    }

    /**
     * 根据tabkey取出title
     * @param tabList
     * @return
     */
    public static MainTabResult getTabByTabKey(ArrayList<MainTabResult> tabList, String tabCompont) {
        for(int i = 0; i < tabList.size(); i++){
            if(tabList.get(i).getUri().equals(tabCompont)){
                return tabList.get(i);
            }
        }
        return null;
    }

    /**
     * 获取MainTabProperty
     * @param context
     * @param tabKey
     * @return
     */
    public static MainTabProperty getMainTabProperty(Context context,String tabKey){
        String appTabs = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_APP_TAB_BAR_INFO_CURRENT,"");
        ArrayList<MainTabResult> tabList = new GetAppMainTabResult(appTabs).getMainTabPayLoad().getMainTabResultList();
        String tabCompont = getCompont(tabKey);
        MainTabResult tab = getTabByTabKey(tabList,tabCompont);
        if(tab != null){
            return  tab.getMainTabProperty();
        }
        return null;
    }

}
