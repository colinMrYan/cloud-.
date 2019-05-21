/**
 * LanguageUtils.java
 * classes : com.inspur.emmcloud.util.privates.LanguageUtils
 * V 1.0.0
 * Create at 2016年10月9日 下午5:04:20
 */
package com.inspur.emmcloud.util.privates;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.LocaleList;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.mine.GetLanguageResult;
import com.inspur.emmcloud.bean.mine.Language;
import com.inspur.emmcloud.bean.system.ClientConfigItem;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * com.inspur.emmcloud.util.privates.LanguageUtils create at 2016年10月9日 下午5:04:20
 */
public class LanguageUtils {
    private static final int GET_LANGUAGE_SUCCESS = 3;
    private Context context;
    private Handler handler;
    private List<Language> commonLanguageList = new ArrayList<Language>();
    private String saveConfigVersion = "";

    public LanguageUtils(Activity context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    private static Locale getLocaleByLanguage(Context context) {
        String languageJson = null;
        if (MyApplication.getInstance() == null || MyApplication.getInstance().getTanent() == null) {
            languageJson = PreferencesUtils.getString(context, Constant.PREF_LAST_LANGUAGE);

        } else {
            languageJson = PreferencesUtils
                    .getString(context, MyApplication.getInstance().getTanent()
                            + "appLanguageObj");
        }
        if (StringUtils.isBlank(languageJson)) {
            return Locale.getDefault();
        }
        String[] array = new Language(languageJson).getIso().split("-");
        String country = "";
        String variant = "";
        try {
            country = array[0];
            variant = array[1];
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return new Locale(country, variant);

    }

    public static Context attachBaseContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context);
        } else {
            return context;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context) {
        Resources resources = context.getResources();
        Locale locale = getLocaleByLanguage(context);

        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        configuration.fontScale = 1.0f;
        return context.createConfigurationContext(configuration);
    }

    public void getServerSupportLanguage() {
        boolean isLanguageUpdate = ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_LANGUAGE);
        String languageResult = PreferencesUtils.getString(context,
                MyApplication.getInstance().getTanent() + "languageResult");
        if (!isLanguageUpdate && languageResult != null) {
            if (languageResult != null) {
                handData(new GetLanguageResult(languageResult));
            } else {
                setAppLanguage(commonLanguageList);
            }
            return;
        }
        if (NetUtils.isNetworkConnected(context, false)) {
            saveConfigVersion = ClientConfigUpdateUtils.getInstance().getItemNewVersion(ClientConfigItem.CLIENT_CONFIG_LANGUAGE);
            MineAPIService apiService = new MineAPIService(context);
            apiService.setAPIInterface(new WebService());
            apiService.getLanguage("");
        } else if (languageResult != null) {
            handData(new GetLanguageResult(languageResult));
        } else {
            setAppLanguage(commonLanguageList);
        }
    }

    /**
     * 处理数据
     *
     * @param getLanguageResult
     */
    private void handData(GetLanguageResult getLanguageResult) {
        // TODO Auto-generated method stub
        List<Language> serverLanguageList = getLanguageResult.getLanguageList();
        setCommonLanguage(serverLanguageList);
        setAppLanguage(commonLanguageList);
    }

    /**
     * 获取服务端和客户端统一的语言
     *
     * @param serverLanguageList
     * @return
     */
    public void setCommonLanguage(List<Language> serverLanguageList) {
        // TODO Auto-generated method stub
        for (int i = 0; i < serverLanguageList.size(); i++) {
            Language language = serverLanguageList.get(i);
            String iso = language.getIso();
            Map<String, String> localLanguaMap = MyAppConfig
                    .getLocalLanguageMap();
            if (localLanguaMap.containsKey(iso)) {
                commonLanguageList.add(language);
            }
        }

    }

    public List<Language> getCommonLanguageList() {
        return commonLanguageList;
    }

    /**
     * 设置App的语言
     *
     * @param commonLanguageList
     */
    private void setAppLanguage(List<Language> commonLanguageList) {
        // TODO Auto-generated method stub
        if (commonLanguageList.size() == 0) {
            //使用中文
            commonLanguageList.add(MyAppConfig.getDefaultLanguage);
        }
        String languageJson = PreferencesUtils.getString(context,
                MyApplication.getInstance().getTanent() + "appLanguageObj");
        String languageName = "";
        String savelanguageName = ""; //Preference中保存的语言名字
        // 当本地已经没有存储了languageObj信息时候
        if (languageJson == null) {
            languageName = Locale.getDefault().getCountry();
            savelanguageName = "followSys";
        } else {
            languageName = new Language(languageJson).getIso();
            savelanguageName = PreferencesUtils.getString(
                    context, MyApplication.getInstance().getTanent() + "language", "");
        }
        Language language = getContainedLanguage(commonLanguageList,
                languageName);
        if (language == null) {
            language = commonLanguageList.get(0);
            savelanguageName = language.getIso();
        }

        PreferencesUtils.putString(context, MyApplication.getInstance().getTanent()
                + "appLanguageObj", language.toString());
        PreferencesUtils.putString(context, MyApplication.getInstance().getTanent()
                + "language", savelanguageName);
        String commonLanguageListJson = JSONUtils.toJSONString(commonLanguageList);
        PreferencesUtils.putString(context, MyApplication.getInstance().getTanent() + "commonLanguageList", commonLanguageListJson);
        ((MyApplication) context.getApplicationContext())
                .setAppLanguageAndFontScale();
        if (handler != null) {
            handler.sendEmptyMessage(GET_LANGUAGE_SUCCESS);
        }

    }

    public Language getContainedLanguage(List<Language> commonLanguageList,
                                         String shortLanguage) {
        for (int i = 0; i < commonLanguageList.size(); i++) {
            Language language = commonLanguageList.get(i);
            if (language.getIso().contains(shortLanguage)) {
                return language;
            }

        }
        return null;
    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnLanguageSuccess(GetLanguageResult getLanguageResult) {
            // TODO Auto-generated method stub
            PreferencesUtils.putString(context, MyApplication.getInstance().getTanent()
                    + "languageResult", getLanguageResult.getLanguageResult());
            ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_LANGUAGE, saveConfigVersion);
            handData(getLanguageResult);
        }

        @Override
        public void returnLanguageFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            String languageResult = PreferencesUtils.getString(context,
                    MyApplication.getInstance().getTanent() + "languageResult");
            if (languageResult != null) {
                handData(new GetLanguageResult(languageResult));
            } else {
                setAppLanguage(commonLanguageList);
            }
        }

    }
}
