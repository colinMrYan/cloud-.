package com.inspur.emmcloud.util;

import android.content.Context;
import android.content.res.Configuration;

import com.inspur.emmcloud.bean.AppTabAutoBean;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2017/4/27.
 */

public class AppTitleUtils {
    public static String getTabTitle(Context context,String tabkey){
        String appTabs = PreferencesByUserAndTanentUtils.getString(context,"app_tabbar_info_current","");
        ArrayList<AppTabAutoBean.PayloadBean.TabsBean> tabList =
                (ArrayList<AppTabAutoBean.PayloadBean.TabsBean>) new AppTabAutoBean(appTabs).getPayload().getTabs();
        String tabCompont = getCompont(tabkey);
        AppTabAutoBean.PayloadBean.TabsBean tab = getTabByTabKey(tabList,tabCompont);
        if(tab == null){
            return "";
        }
//        String environmentLanguage = "";
//        String languageJson = PreferencesUtils.getString(
//                context, UriUtils.tanent + "appLanguageObj");
//        if (languageJson != null) {
//            Language language = new Language(languageJson);
//            environmentLanguage = language.getIana();
//        }
        Configuration config = context.getResources().getConfiguration();
        String environmentLanguage = config.locale.getLanguage();
        if(environmentLanguage.toLowerCase().equals("zh")||environmentLanguage.toLowerCase().equals("zh-Hans".toLowerCase())){
            return tab.getTitle().getZhHans();
        }else if(environmentLanguage.toLowerCase().equals("zh-Hant".toLowerCase())){
            return tab.getTitle().getZhHant();
        }else if(environmentLanguage.toLowerCase().equals("en-US".toLowerCase())||
                environmentLanguage.toLowerCase().equals("en".toLowerCase())){
            return tab.getTitle().getEnUS();
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
        if(tabkey.equals("MessageFragment")){
            return "communicate";
        }else if(tabkey.equals("FindFragment")){
            return "find";
        }else if(tabkey.equals("WorkFragment")){
            return "work";
        }else if(tabkey.equals("MyAppFragment")){
            return "application";
        }else if(tabkey.equals("MoreFragment")){
            return "mine";
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
    private static AppTabAutoBean.PayloadBean.TabsBean getTabByTabKey(ArrayList<AppTabAutoBean.PayloadBean.TabsBean> tabList,String tabKey) {
        for(int i = 0; i < tabList.size(); i++){
            if(tabList.get(i).getComponent().equals(tabKey)){
                return tabList.get(i);
            }
        }
        return null;
    }


}
