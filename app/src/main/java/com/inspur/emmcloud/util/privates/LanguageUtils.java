/**
 * 
 * LanguageUtils.java
 * classes : com.inspur.emmcloud.util.privates.LanguageUtils
 * V 1.0.0
 * Create at 2016年10月9日 下午5:04:20
 */
package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.mine.GetLanguageResult;
import com.inspur.emmcloud.bean.mine.Language;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;

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

	public LanguageUtils(Activity context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	public void getServerSupportLanguage() {
		String languageResult = PreferencesUtils.getString(context,
				MyApplication.getInstance().getTanent() + "languageResult");
		if (NetUtils.isNetworkConnected(context,false)) {
			MineAPIService apiService = new MineAPIService(context);
			apiService.setAPIInterface(new WebService());
			apiService.getLanguage();
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
		String languageName= "";
		String savelanguageName = ""; //Preference中保存的语言名字
		// 当本地已经没有存储了languageObj信息时候
		if (languageJson == null) {
			languageName = Locale.getDefault().getCountry();
			savelanguageName = "followSys";
		} else {
			languageName = new Language(languageJson).getIso();
			savelanguageName = PreferencesUtils.getString(
					context, MyApplication.getInstance().getTanent()+"language", "");
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
		String commonLanguageListJson = JSON.toJSONString(commonLanguageList);
		PreferencesUtils.putString(context, MyApplication.getInstance().getTanent()+"commonLanguageList", commonLanguageListJson);
		((MyApplication) context.getApplicationContext())
				.setAppLanguageAndFontScale();
		handler.sendEmptyMessage(GET_LANGUAGE_SUCCESS);
		
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
			handData(getLanguageResult);
		}

		@Override
		public void returnLanguageFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			String languageResult = PreferencesUtils.getString(context,
					MyApplication.getInstance().getTanent() + "languageResult");
			if (languageResult != null) {
				handData(new GetLanguageResult(languageResult));
			}else {
				setAppLanguage(commonLanguageList);
			}
		}

	}
}
