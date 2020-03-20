package com.inspur.emmcloud.bean.contact;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: yufuchang
 * Date: 2020/3/17
 */
public class GetMultiContactResult {

    private List<MultiOrg> multiOrgList = new ArrayList<>();
    public GetMultiContactResult(String response){
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                multiOrgList.add(new MultiOrg(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<MultiOrg> getMultiOrgList() {
        return multiOrgList;
    }

    public void setMultiOrgList(List<MultiOrg> multiOrgList) {
        this.multiOrgList = multiOrgList;
    }
}
