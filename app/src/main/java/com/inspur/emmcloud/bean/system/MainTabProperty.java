package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/4/2.
 */

public class MainTabProperty {
    /**
     * "properties": {
     * "canContact": "false",
     * "canCreate": "true"
     * }
     */
    private boolean canContact = true;
    private boolean canCreate = true;
    private boolean isHaveNavbar = false;
    private List<MainTabMenu> mainTabMenuList = new ArrayList<>();
    private ArrayList<String> mineItemList = new ArrayList<>();

    public MainTabProperty(String response) {
        canContact = JSONUtils.getBoolean(response, "canOpenContact", true);
        canCreate = JSONUtils.getBoolean(response, "canCreateChannel", true);
        isHaveNavbar = JSONUtils.getBoolean(response,"isHaveNavbar",false);
        JSONArray jsonArray = JSONUtils.getJSONArray(response,"menus",new JSONArray());
        for (int i = 0; i < (jsonArray.length()>2?2:jsonArray.length()); i++) {
            mainTabMenuList.add(new MainTabMenu(JSONUtils.getJSONObject(jsonArray,i,new JSONObject())));
        }
        JSONArray mineItemArray = JSONUtils.getJSONArray(response,"tablist",new JSONArray());
        for (int i = 0; i < mineItemArray.length(); i++) {
            mineItemList.add(JSONUtils.getString(JSONUtils.getJSONArray(mineItemArray,i,new JSONArray()),0,""));
        }
    }

    public boolean isCanContact() {
        return canContact;
    }

    public void setCanContact(boolean canContact) {
        this.canContact = canContact;
    }

    public boolean isCanCreate() {
        return canCreate;
    }

    public void setCanCreate(boolean canCreate) {
        this.canCreate = canCreate;
    }

    public boolean isHaveNavbar() {
        return isHaveNavbar;
    }

    public void setHaveNavbar(boolean haveNavbar) {
        isHaveNavbar = haveNavbar;
    }

    public List<MainTabMenu> getMainTabMenuList() {
        return mainTabMenuList;
    }

    public void setMainTabMenuList(List<MainTabMenu> mainTabMenuList) {
        this.mainTabMenuList = mainTabMenuList;
    }

    public ArrayList<String> getMineItemList() {
        return mineItemList;
    }

    public void setMineItemList(ArrayList<String> mineItemList) {
        this.mineItemList = mineItemList;
    }
}
