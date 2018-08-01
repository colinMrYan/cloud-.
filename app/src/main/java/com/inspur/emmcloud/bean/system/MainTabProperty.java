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
    private List<MineLayoutItemGroup> mineLayoutItemGroupList = new ArrayList<>();

    public MainTabProperty(String response) {
        canContact = JSONUtils.getBoolean(response, "canOpenContact", true);
        canCreate = JSONUtils.getBoolean(response, "canCreateChannel", true);
        isHaveNavbar = JSONUtils.getBoolean(response,"isHaveNavbar",false);
        JSONArray jsonArray = JSONUtils.getJSONArray(response,"menus",new JSONArray());
        for (int i = 0; i < (jsonArray.length()>2?2:jsonArray.length()); i++) {
            mainTabMenuList.add(new MainTabMenu(JSONUtils.getJSONObject(jsonArray,i,new JSONObject())));
        }
        JSONArray mineLayoutItemGroupArray = JSONUtils.getJSONArray(response,"tablist",new JSONArray());
        for (int i = 0; i < mineLayoutItemGroupArray.length(); i++) {
            JSONArray mineLayoutItemArray = JSONUtils.getJSONArray(mineLayoutItemGroupArray,i,new JSONArray());
            mineLayoutItemGroupList.add(new MineLayoutItemGroup(mineLayoutItemArray));

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

    public List<MineLayoutItemGroup> getMineLayoutItemGroupList() {
        return mineLayoutItemGroupList;
    }

    public void setMineLayoutItemGroupList(List<MineLayoutItemGroup> mineLayoutItemGroupList) {
        this.mineLayoutItemGroupList = mineLayoutItemGroupList;
    }
}
