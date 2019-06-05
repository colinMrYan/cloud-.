/**
 * GetLanguageResult.java
 * classes : com.inspur.emmcloud.basemodule.bean.GetLanguageResult
 * V 1.0.0
 * Create at 2016年10月9日 下午5:15:18
 */
package com.inspur.emmcloud.basemodule.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * com.inspur.emmcloud.basemodule.bean.GetLanguageResult
 * create at 2016年10月9日 下午5:15:18
 */
public class GetLanguageResult {
    private String languageResultJson = "";
    private List<Language> languageList = new ArrayList<Language>();

    public GetLanguageResult(String response) {
        // TODO Auto-generated constructor stub
        try {
            languageResultJson = response;
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                languageList.add(new Language(obj));
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public List<Language> getLanguageList() {
        return languageList;
    }

    public String getLanguageResult() {
        return languageResultJson;
    }


}
