package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentExtendedActions {
    private String title;
    private String description;
    private String poster;
    private boolean edgeToEdge;
    private Action singleAction;
    private String arrangement;
    private String tmpId = "";
    private List<Action> actionList = new ArrayList<>();

    public MsgContentExtendedActions(String content) {
        JSONObject obj = JSONUtils.getJSONObject(content);
        title = JSONUtils.getString(obj, "title", "");
        description = JSONUtils.getString(obj, "description", "");
        poster = JSONUtils.getString(obj, "poster", "");
        tmpId = JSONUtils.getString(obj, "tmpId", "");
        edgeToEdge = JSONUtils.getBoolean(obj, "edgeToEdge", false);

        if (obj.has("singleAction")) {
            singleAction = new Action(JSONUtils.getJSONObject(obj, "singleAction", new JSONObject()));
        } else {
            arrangement = "vertical";
            JSONArray array = JSONUtils.getJSONArray(obj, "actions", new JSONArray());
            for (int i = 0; i < array.length(); i++) {
                Action action = new Action(JSONUtils.getJSONObject(array, i, new JSONObject()));
                actionList.add(action);
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

    public boolean isEdgeToEdge() {
        return edgeToEdge;
    }

    public void setEdgeToEdge(boolean edgeToEdge) {
        this.edgeToEdge = edgeToEdge;
    }

    public String getArrangement() {
        return arrangement;
    }

    public void setArrangement(String arrangement) {
        this.arrangement = arrangement;
    }

    public Action getSingleAction() {
        return singleAction;
    }

    public void setSingleAction(Action singleAction) {
        this.singleAction = singleAction;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public void setActionList(List<Action> actionList) {
        this.actionList = actionList;
    }

    public String getTmpId() {
        return tmpId;
    }

    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }
}
