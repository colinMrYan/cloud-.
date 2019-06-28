package com.inspur.emmcloud.basemodule.util.mycamera;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/12/15.
 */

public class GetReatScaleResult {

    private List<RectScale> rectScaleList = new ArrayList<>();

    public GetReatScaleResult(String reatScaleLsitJson) {
        JSONArray array = JSONUtils.getJSONArray(reatScaleLsitJson, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = JSONUtils.getJSONObject(array, i, new JSONObject());
            RectScale rectScale = new RectScale(object);
            rectScaleList.add(rectScale);
        }
    }

    public List<RectScale> getRectScaleList() {
        return rectScaleList;
    }
}
