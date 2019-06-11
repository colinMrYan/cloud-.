package com.inspur.emmcloud.bean.chat;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class GetMessageCommentResult {

    private List<Message> commentList = new ArrayList<>();

    public GetMessageCommentResult(String response) {
        try {
            JSONArray commentArray = new JSONArray(response);
            for (int i = 0; i < commentArray.length(); i++) {
                Message comment = new Message(commentArray.getJSONObject(i));
                commentList.add(comment);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public List<Message> getCommentList() {

        return commentList;
    }

}
