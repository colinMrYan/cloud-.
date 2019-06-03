package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/4/10.
 */

public class GetOfficeListResult {
    private List<Office> officeList = new ArrayList<>();
    private List<String> officeIdList = new ArrayList<>();

    public GetOfficeListResult(String response) {
        JSONArray array = JSONUtils.getJSONArray(response, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            Office office = new Office(JSONUtils.getJSONObject(array, i, new JSONObject()));
            String officeId = office.getId();
            officeList.add(office);
            officeIdList.add(officeId);
        }
    }

    public List<Office> getOfficeList() {
        return officeList;
    }

    public void setOfficeList(List<Office> officeList) {
        this.officeList = officeList;
    }

    public List<String> getOfficeIdList() {
        return officeIdList;
    }
}
