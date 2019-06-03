package com.inspur.emmcloud.bean.mine;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.bean.system.MineLayoutItem;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/2/19.
 */

public class GetUserCardMenusResult {
    private String response;
    private List<MineLayoutItem> mineLayoutItemList = new ArrayList<>();

    public GetUserCardMenusResult(String response) {
        this.response = response;
        JSONArray userCardArray = JSONUtils.getJSONArray(response, "userCard", new JSONArray());
        for (int i = 0; i < userCardArray.length(); i++) {
            MineLayoutItem mineLayoutItem = new MineLayoutItem(JSONUtils.getString(userCardArray, i, ""));
            mineLayoutItemList.add(mineLayoutItem);
        }
    }

    public List<MineLayoutItem> getMineLayoutItemList() {
        return mineLayoutItemList;
    }

    public void setMineLayoutItemList(List<MineLayoutItem> mineLayoutItemList) {
        this.mineLayoutItemList = mineLayoutItemList;
    }

    public String getResponse() {
        return response;
    }
}
