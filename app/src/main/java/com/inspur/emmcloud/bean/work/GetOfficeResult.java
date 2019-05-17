package com.inspur.emmcloud.bean.work;

import com.inspur.emmcloud.bean.schedule.meeting.Office;
import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetOfficeResult {

    private List<Office> officeList = new ArrayList<>();
    private List<String> officeIdList = new ArrayList<>();

    public GetOfficeResult(String response) {
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
