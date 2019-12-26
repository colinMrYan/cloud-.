package com.inspur.emmcloud.basemodule.app.maintab;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/7/31.
 */

public class MineLayoutItemGroup {
    private List<MineLayoutItem> mineLayoutItemList = new ArrayList<>();

    public MineLayoutItemGroup(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            String content = JSONUtils.getString(array, i, "");
            mineLayoutItemList.add(new MineLayoutItem(content));
        }
    }

    public MineLayoutItemGroup() {

    }

    public List<MineLayoutItem> getMineLayoutItemList() {
        return mineLayoutItemList;
    }

    public void setMineLayoutItemList(List<MineLayoutItem> mineLayoutItemList) {
        this.mineLayoutItemList = mineLayoutItemList;
    }
}
