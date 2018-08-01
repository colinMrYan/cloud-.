package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/7/31.
 */

public class MineLayoutItemGroup {
    private List<String> mineLayoutItemList = new ArrayList<>();
    public MineLayoutItemGroup(JSONArray array){
        for (int i =0;i<array.length();i++){
            mineLayoutItemList.add(JSONUtils.getString(array,i,""));
        }
    }

    public MineLayoutItemGroup(){

    }

    public List<String> getMineLayoutItemList() {
        return mineLayoutItemList;
    }

    public void setMineLayoutItemList(List<String> mineLayoutItemList) {
        this.mineLayoutItemList = mineLayoutItemList;
    }
}
