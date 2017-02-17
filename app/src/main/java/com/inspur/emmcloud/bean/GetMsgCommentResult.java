package com.inspur.emmcloud.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.hardware.Camera.PreviewCallback;

import com.inspur.emmcloud.util.LogUtils;

public class GetMsgCommentResult {

	private static final String TAG = "GetMsgCommentResult";
	private JSONObject jsonObject;
	private JSONArray jsonArray;
	private List<Comment> commentList = new ArrayList<Comment>();

	public GetMsgCommentResult(String response,String mid) {
		
		
		try {
				JSONArray commentArray = new JSONArray(response);
				for (int i = 0; i < commentArray.length(); i++) {
					Comment comment = new Comment(commentArray.getJSONObject(i));
					commentList.add(comment);
				}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public List<Comment> getCommentList(){
		
		return commentList;
	}
	
}
