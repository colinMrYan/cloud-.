package com.inspur.emmcloud.bean.mine;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2018/8/1.
 */

public class GetCardPackageResult {
    private ArrayList<CardPackageBean> cardPackageBeanList = new ArrayList<>();

    public GetCardPackageResult(String response) {
        JSONArray jsonArray = JSONUtils.getJSONArray(response, new JSONArray());
        for (int i = 0; i < jsonArray.length(); i++) {
            cardPackageBeanList.add(new CardPackageBean(JSONUtils.getJSONObject(jsonArray, i, new JSONObject())));
        }
    }

    public ArrayList<CardPackageBean> getCardPackageBeanList() {
        return cardPackageBeanList;
    }

    public void setCardPackageBeanList(ArrayList<CardPackageBean> cardPackageBeanList) {
        this.cardPackageBeanList = cardPackageBeanList;
    }
}
