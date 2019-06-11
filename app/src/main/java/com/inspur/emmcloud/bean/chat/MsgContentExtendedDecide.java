package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentExtendedDecide {
    private String title;
    private String description;
    private String poster;
    private boolean lockAfterSelection;
    private String arrangement;
    private String tmpId = "";
    private List<Option> optionList = new ArrayList<>();

    private boolean canClickAgain = true;

    public MsgContentExtendedDecide(String content) {
        JSONObject obj = JSONUtils.getJSONObject(content);
        title = JSONUtils.getString(obj, "title", "");
        description = JSONUtils.getString(obj, "description", "");
        poster = JSONUtils.getString(obj, "poster", "");
        tmpId = JSONUtils.getString(obj, "tmpId", "");
        lockAfterSelection = JSONUtils.getBoolean(obj, "lockAfterSelection", false);
        if(obj.has("options")){
            JSONArray array = JSONUtils.getJSONArray(obj, "options", new JSONArray());
            for (int i = 0; i < array.length(); i++) {
                Option action = new Option(JSONUtils.getJSONObject(array, i, new JSONObject()));
                optionList.add(action);
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public boolean isLockAfterSelection() {
        return lockAfterSelection;
    }

    public void setLockAfterSelection(boolean lockAfterSelection) {
        this.lockAfterSelection = lockAfterSelection;
    }

    public String getArrangement() {
        return arrangement;
    }

    public void setArrangement(String arrangement) {
        this.arrangement = arrangement;
    }

    public List<Option> getOptionList() {
        return optionList;
    }

    public void setOptionList(List<Option> optionList) {
        this.optionList = optionList;
    }

    public String getTmpId() {
        return tmpId;
    }

    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }

    public boolean isCanClickAgain() {
        return canClickAgain;
    }

    public void setCanClickAgain(boolean canClickAgain) {
        this.canClickAgain = canClickAgain;
    }
}
