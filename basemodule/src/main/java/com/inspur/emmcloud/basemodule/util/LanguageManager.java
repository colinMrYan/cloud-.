/**
 * LanguageUtils.java
 * classes : com.inspur.emmcloud.util.privates.LanguageUtils
 * V 1.0.0
 * Create at 2016年10月9日 下午5:04:20
 */
package com.inspur.emmcloud.basemodule.util;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.ClientConfigItem;
import com.inspur.emmcloud.basemodule.bean.GetLanguageResult;
import com.inspur.emmcloud.basemodule.bean.Language;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 客户端语音管理类
 * 将客户端和服务端支持语言列表做交集，然后再根据设备系统语言设置App当前语言
 * Constant.PREF_CURRENT_LANGUAGE 存储当前Language的Json
 * Constant.PREF_CURRENT_LANGUAGE_NAME 存储当前选择的语音的名称（包含跟随系统）
 * Constant.PREF_SERVER_SUPPORT_LANGUAGE 存储服务端支持的语音列表
 * Constant.PREF_LAST_LANGUAGE只有在没有租户信息的时候使用，记录最后一次设置的语音
 */
public class LanguageManager extends BaseModuleAPIInterfaceInstance {
    public static final String LANGUAGE_NAME_FOLLOW_SYS = "followSys";
    private static final Language defaultLanguage = new Language("中文简体", "zh-CN", "zh-Hans", "zh-CN", "zh-CN", "zh-Hans");
    private static LanguageManager mInstance;
    private GetServerLanguageListener getServerLanguageListener;

    private LanguageManager() {
    }

    public static LanguageManager getInstance() {
        if (mInstance == null) {
            synchronized (LanguageManager.class) {
                if (mInstance == null) {
                    mInstance = new LanguageManager();
                }
            }
        }
        return mInstance;
    }

    public void getServerSupportLanguage(GetServerLanguageListener getServerLanguageListener) {
        this.getServerLanguageListener = getServerLanguageListener;
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance(), false) && isNeedUpdate()) {
            String languageConfigVersion = ClientConfigUpdateUtils.getInstance().getItemNewVersion(ClientConfigItem.CLIENT_CONFIG_LANGUAGE);
            BaseModuleApiService apiService = new BaseModuleApiService(BaseApplication.getInstance());
            apiService.setAPIInterface(this);
            apiService.getLanguage(languageConfigVersion);
        } else {
            setAppLanguage(null);
            callback();
        }
    }


    public Context attachBaseContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context);
        } else {
            return context;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Context updateResources(Context context) {
        Resources resources = context.getResources();
        Locale locale = getLocaleByLanguage(context);

        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        configuration.fontScale = 1.0f;
        LogUtils.jasonDebug("updateResources===" + locale.toString());
        return context.createConfigurationContext(configuration);
    }

    private Locale getLocaleByLanguage(Context context) {
        String languageJson = null;
        if (BaseApplication.getInstance() == null || BaseApplication.getInstance().getTanent() == null) {
            languageJson = PreferencesUtils.getString(context, Constant.PREF_LAST_LANGUAGE);
        } else {
            languageJson = getCurrentLanguageJson();
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

    public String getCurrentLanguageJson() {
        return PreferencesByTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_CURRENT_LANGUAGE);
    }

    public void setCurrentLanguageJson(String json) {
        PreferencesByTanentUtils.putString(BaseApplication.getInstance(), Constant.PREF_CURRENT_LANGUAGE, json);
    }

    public String getCurrentLanguageName() {
        return PreferencesByTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_CURRENT_LANGUAGE_NAME, "");
    }

    public void setCurrentLanguageName(String saveLanguageName) {
        PreferencesByTanentUtils.putString(BaseApplication.getInstance(), Constant.PREF_CURRENT_LANGUAGE_NAME, saveLanguageName);
    }

    private boolean isNeedUpdate() {
        boolean isLanguageUpdate = ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_LANGUAGE);
        //统一更新功能检测到语言需要更新
        if (isLanguageUpdate) {
            return true;
        }
        //本地没有缓存语言
        String serverSupportLanguageInLocal = getServerSupportLanguageInLocal();
        if (serverSupportLanguageInLocal == null) {
            return true;
        }
        return false;
    }

    /**
     * 设置App语音
     *
     * @param getLanguageResult
     */
    private void setAppLanguage(GetLanguageResult getLanguageResult) {
        List<Language> commonLanguageList = getCommonLanguageList(getLanguageResult);
        String languageJson = getCurrentLanguageJson();
        String languageName = "";
        String saveLanguageName = ""; //Preference中保存的语言名字
        // 当本地已经没有存储了languageObj信息时候
        if (languageJson == null) {
            languageName = Locale.getDefault().getCountry();
            saveLanguageName = LANGUAGE_NAME_FOLLOW_SYS;
        } else {
            languageName = new Language(languageJson).getIso();
            saveLanguageName = getCurrentLanguageName();
        }
        Language language = getContainedLanguage(commonLanguageList,
                languageName);
        if (language == null) {
            language = commonLanguageList.get(0);
            saveLanguageName = language.getIso();
        }

        setCurrentLanguageJson(language.toString());
        setCurrentLanguageName(saveLanguageName);
        setLanguageLocal();
    }

    /**
     * 设置语言Local
     */
    public void setLanguageLocal() {
        String currentLocal = Resources.getSystem().getConfiguration().locale.toString();
        PreferencesUtils.putString(BaseApplication.getInstance(), Constant.PREF_LANGUAGE_CURRENT_LOCAL, currentLocal);
        Configuration config = BaseApplication.getInstance().getResources().getConfiguration();
        String languageJson = getCurrentLanguageJson();
        if (languageJson != null) {
            String languageName = getCurrentLanguageName();
            // 当系统语言选择为跟随系统的时候，要检查当前系统的语言是不是在commonList中，重新赋值
            if (languageName.equals("followSys")) {
                List<Language> commonLanguageList = getCommonLanguageList(null);
                boolean isContainDefault = false;
                if (currentLocal.contains("zh_CN")) {
                    currentLocal = "zh-CN";
                } else if (currentLocal.contains("Hant") || currentLocal.contains("zh_TW") || currentLocal.contains("zh_HK")) {
                    currentLocal = "zh-Hant";
                } else if (currentLocal.contains("en_")) {
                    currentLocal = "en-US";
                } else {
                    currentLocal = Resources.getSystem().getConfiguration().locale.getCountry();
                }
                for (int i = 0; i < commonLanguageList.size(); i++) {
                    Language commonLanguage = commonLanguageList.get(i);
                    if (commonLanguage.getIso().contains(currentLocal)) {
                        setCurrentLanguageJson(commonLanguage.toString());
                        languageJson = commonLanguage.toString();
                        isContainDefault = true;
                        break;
                    }
                }
                if (!isContainDefault) {
                    setCurrentLanguageJson(commonLanguageList.get(0).toString());
                    languageJson = commonLanguageList.get(0).toString();
                }
            }
            PreferencesUtils.putString(BaseApplication.getInstance(), Constant.PREF_LAST_LANGUAGE, languageJson);
            // 将iso字符串分割成系统的设置语言
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
            Locale locale = new Locale(country, variant);
            Locale.setDefault(locale);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLocale(locale);
            } else {
                config.locale = locale;
            }
        }
        config.fontScale = 1.0f;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            BaseApplication.getInstance().createConfigurationContext(config);
        }
        BaseApplication.getInstance().getResources().updateConfiguration(config,
                BaseApplication.getInstance().getResources().getDisplayMetrics());
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

    /**
     * 获取客户度和服务端共同支持的语音
     *
     * @param getLanguageResult
     * @return
     */
    public List<Language> getCommonLanguageList(GetLanguageResult getLanguageResult) {
        List<Language> commonLanguageList = new ArrayList<>();
        if (getLanguageResult == null) {
            String getLanguageResultJson = getServerSupportLanguageInLocal();
            if (getLanguageResultJson != null) {
                getLanguageResult = new GetLanguageResult(getLanguageResultJson);
            }

        }
        if (getLanguageResult != null) {
            List<Language> serverLanguageList = getLanguageResult.getLanguageList();
            Map<String, String> localLanguageMap = MyAppConfig.getLocalLanguageMap();
            for (Language language : serverLanguageList) {
                if (localLanguageMap.containsKey(language.getIso())) {
                    commonLanguageList.add(language);
                }
            }
        }
        if (commonLanguageList.size() == 0) {
            //使用中文
            commonLanguageList.add(defaultLanguage);
        }
        return commonLanguageList;
    }

    /**
     * 获取当前应用语言
     * @return
     */
    public String getCurrentAppLanguage() {
        String languageJson = getCurrentLanguageJson();
        if (languageJson != null) {
            Language language = new Language(languageJson);
            return language.getIana();
        }
        return "zh-Hans";
    }

    /**
     * 语音输入用户偏好
     */
    public String getVoiceInputLanguage() {
        String language = PreferencesByTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_VOICE_INPUT_LANGUAGE);
        if (!StringUtils.isBlank(language)) {
            return language;
        }
        return "";
    }

    /**
     * 设置语音输入用户偏好
     */
    public void setVoiceInputLanguage(String language) {
        PreferencesByTanentUtils.putString(BaseApplication.getInstance(), Constant.PREF_VOICE_INPUT_LANGUAGE, language);
    }
    /**
     * 从本地获取缓存的服务端支持语音列表
     *
     * @return
     */
    private String getServerSupportLanguageInLocal() {
        return PreferencesByTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_SERVER_SUPPORT_LANGUAGE, null);
    }

    /**
     * 从将服务端支持语音列表存储到本地
     *
     * @return
     */
    private void setServerSupportLanguageInLocal(String json) {
        PreferencesByTanentUtils.putString(BaseApplication.getInstance(), Constant.PREF_SERVER_SUPPORT_LANGUAGE, json);
    }

    private void callback() {
        if (getServerLanguageListener != null) {
            getServerLanguageListener.complete();
        }
    }

    @Override
    public void returnLanguageSuccess(GetLanguageResult getLanguageResult, String languageConfigVersion) {
        // TODO Auto-generated method stub
        setServerSupportLanguageInLocal(getLanguageResult.getLanguageResult());
        ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_LANGUAGE, languageConfigVersion);
        setAppLanguage(getLanguageResult);
        callback();
    }

    @Override
    public void returnLanguageFail(String error, int errorCode) {
        // TODO Auto-generated method stub
        setAppLanguage(null);
        callback();
    }

    public interface GetServerLanguageListener {
        void complete();
    }
}
