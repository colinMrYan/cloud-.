package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.res.Configuration;

import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.config.Constant;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2017/4/27.
 */

public class AppTitleUtils {
    public static String getTabTitle(Context context,String tabKey){
        String appTabs = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_APP_TAB_BAR_INFO_CURRENT,"");
        ArrayList<MainTabResult> tabList = new GetAppMainTabResult(appTabs).getMainTabResultList();
        String tabCompont = getCompont(tabKey);
        MainTabResult tab = getTabByTabKey(tabList,tabCompont);
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
            return "";
        }
    }

    /**
     *
     * @param tabkey
     * @return
     */
    private static String getCompont(String tabkey) {
        if(tabkey.startsWith("CommunicationFragment")||tabkey.startsWith("CommunicationV0Fragment") ){
            return "communicate";
        }else if(tabkey.equals("FindFragment")){
            return "find";
        }else if(tabkey.equals("WorkFragment")){
            return "work";
        }else if(tabkey.equals("MyAppFragment")){
            return "application";
        }else if(tabkey.equals("MoreFragment")){
            return "mine";
        }else if(tabkey.equals("ContactSearchFragment")){
            return "contact";
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
    private static MainTabResult getTabByTabKey(ArrayList<MainTabResult> tabList, String tabKey) {
        for(int i = 0; i < tabList.size(); i++){
            if(tabList.get(i).getName().equals(tabKey)){
                return tabList.get(i);
            }
        }
        return null;
    }

}
