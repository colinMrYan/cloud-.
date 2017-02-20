package com.inspur.emmcloud.util;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.widget.EditText;

import com.inspur.emmcloud.widget.spans.ForeColorSpan;

public class ChannelMentions {

	/**
	 * 处理Mentions原始数据的类，记录keywords，ueridkey
	 * @param result
	 * @param keywords
	 * @param useridkey
	 * @param msgEdit
	 */
	public static  void addMentions(String result,ArrayList<String> keywords,ArrayList<String> useridkey,
			EditText msgEdit,int beginStr,int endStr) {

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(result);
			JSONArray peopleArray = jsonObject.getJSONArray("people");
			JSONObject jsonCidObj = peopleArray.getJSONObject(0);
			String pid = "", pname = "";
			boolean havekey = false;
			boolean haveuid = false;
			if (jsonCidObj.has("cid")) {
				pid = jsonCidObj.getString("cid");
				for (int i = 0; i < useridkey.size(); i++) {
					haveuid = useridkey.get(i).contains(pid);
					if (haveuid) {
						break;
					}
				}
				if (!haveuid) {
					useridkey.add(pid);
				}
			}


		
			if (jsonCidObj.has("name")) {
				pname = jsonCidObj.getString("name");
				for (int i = 0; i < keywords.size(); i++) {
					havekey = keywords.get(i).contains(pname);
					if (havekey) {
						break;
					}
				}
				if (!havekey) {
					keywords.add("@" + pname);
				}

			}

			String oldstr = msgEdit.getText().toString();
			StringBuffer buffer = new StringBuffer(oldstr);
			
			SpannableString ss = null;
			if (!havekey) {
//				ss = new SpannableString(oldstr + pname + " ");
//				ss = new SpannableString(oldstr.replaceFirst("@", "@"+pname+" "));
				ss = new SpannableString(buffer.replace(beginStr, endStr, "@"+pname+" "));
			} else {
				ss = new SpannableString(oldstr.subSequence(0,
						oldstr.length() - 1));
			}
			// 用到keywords设置span
			for (int i = 0; i < keywords.size(); i++) {
				if (ss.toString().contains(keywords.get(i))) {
					int tempstr = ss.toString().indexOf(keywords.get(i));
//					ss.setSpan(
//							new BackColorSpan(Color.parseColor("#0f7bca"), keywords.get(i), i),
//							tempstr, tempstr + keywords.get(i).length(),
//							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					ss.setSpan(
							new ForeColorSpan(Color.parseColor("#0f7bca"), keywords.get(i), i),
							tempstr, tempstr + keywords.get(i).length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			msgEdit.setText(ss);
			msgEdit.setSelection(ss.length());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
