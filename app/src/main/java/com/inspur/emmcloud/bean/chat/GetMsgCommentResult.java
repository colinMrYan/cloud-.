package com.inspur.emmcloud.bean.chat;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class GetMsgCommentResult {

    private List<Comment> commentList = new ArrayList<Comment>();

    public GetMsgCommentResult(String response) {


        try {
            JSONArray commentArray = new JSONArray(response);
            for (int i = 0; i < commentArray.length(); i++) {
                Comment comment = new Comment(commentArray.getJSONObject(i));
                commentList.add(comment);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public List<Comment> getCommentList() {

        return commentList;
    }

}
