package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.res.Configuration;

import com.inspur.emmcloud.bean.system.AppTabAutoBean;
import com.inspur.emmcloud.bean.system.AppTabDataBean;
import com.inspur.emmcloud.bean.system.AppTabPayloadBean;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2017/4/27.
 */

public class AppTitleUtils {
    public static String getTabTitle(Context context,String tabKey){
        if(tabKey.equals("contact")){
            return "通讯录";
        }
        String appTabs = PreferencesByUserAndTanentUtils.getString(context,"app_tabbar_info_current","");
        ArrayList<AppTabDataBean> tabList = new ArrayList<>();
        AppTabPayloadBean appTabPayloadBean = new AppTabAutoBean(appTabs).getPayload();
        if(appTabPayloadBean != null){
            tabList.addAll(appTabPayloadBean.getTabs());
        }
        String tabCompont = getCompont(tabKey);
        AppTabDataBean tab = getTabByTabKey(tabList,tabCompont);
        if(tab == null){
            return "";
        }
//        String environmentLanguage = "";
//        String languageJson = PreferencesUtils.getString(
//                context, MyApplication.getInstance().getTanent() + "appLanguageObj");
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
    private static AppTabDataBean getTabByTabKey(ArrayList<AppTabDataBean> tabList, String tabKey) {
        for(int i = 0; i < tabList.size(); i++){
            if(tabList.get(i).getTabId().equals(tabKey)){
                return tabList.get(i);
            }
        }
        return null;
    }


}
