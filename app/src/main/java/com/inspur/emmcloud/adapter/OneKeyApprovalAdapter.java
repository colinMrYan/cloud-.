package com.inspur.emmcloud.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OneKeyApprovalAdapter extends BaseAdapter{

	/**
	 * 和Adapter关联的Activity的引用
	 */
	private Context context;
	
	/**
	 * 接收到的item数据
	 */
	private ArrayList<HashMap<String,Object>> arrayList;
	
	/**
	 * 复用的Holder
	 */
	private OneKeyApprovalHolder holder;
	
	/**
	 * 缓存，防止item错位
	 */
	private HashMap<Integer,View> convertViewMap;
	public OneKeyApprovalAdapter(Context context,ArrayList<HashMap<String,Object>> arrayList) {
		this.context = context;
		this.arrayList = arrayList;
		holder = new OneKeyApprovalHolder();
		convertViewMap = new HashMap<Integer, View>();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(arrayList.size() >= 0){
			return arrayList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(context);
		holder = new OneKeyApprovalHolder();
		if(convertViewMap.get(position) == null){
			convertView = inflater.inflate(R.layout.onekey_approval_item, null);
			holder.oneKeyUserHeadImg = (ImageView) convertView.findViewById(R.id.onekey_head_img);
			holder.oneKeyUserNameText = (TextView) convertView.findViewById(R.id.onekey_username_text);
			holder.oneKeyTimeText = (TextView) convertView.findViewById(R.id.onekey_timetitle_text);
			holder.oneKeyMoneyText = (TextView) convertView.findViewById(R.id.onekey_moneynum_text);
			holder.oneKeyTrafficImg = (ImageView) convertView.findViewById(R.id.onekey_middle_img);
			holder.oneKeyTitleText = (TextView) convertView.findViewById(R.id.onekey_middleup_text);
			holder.oneKeyContentText = (TextView) convertView.findViewById(R.id.onekey_middledown_text);
			holder.oneKeyFloat = (ImageView) convertView.findViewById(R.id.onekey_floatimg_img);
			holder.oneKeyPassImg = (ImageView) convertView.findViewById(R.id.onekey_pass_img);
			holder.oneKeyPassText = (TextView) convertView.findViewById(R.id.onekey_pass_text);
			holder.oneKeyUnPassImg = (ImageView) convertView.findViewById(R.id.onekey_unpass_img);
			holder.oneKeyUnPassText = (TextView) convertView.findViewById(R.id.onekey_unpass_text);
			
			holder.passLayout = (LinearLayout) convertView.findViewById(R.id.onekey_pass_layout);
			holder.unpassLayout = (LinearLayout) convertView.findViewById(R.id.onekey_unpass_layout);
			holder.passornotLayout = (LinearLayout) convertView.findViewById(R.id.onekey_bottom_layout);
			
			holder.headNameLayout = (LinearLayout) convertView.findViewById(R.id.onekey_headmiddle_layout);
//			holder.userHeadImg = (ImageView) convertView.findViewById(R.id.onekey_head_img);
			
			convertViewMap.put(position, convertView);
			convertView.setTag(holder);
		}else{
			convertView = convertViewMap.get(position);
			holder = (OneKeyApprovalHolder) convertView.getTag();
		}
		
		holder.oneKeyUserHeadImg.setImageResource((Integer) arrayList.get(position).get("userHeadImg"));
		holder.oneKeyUserNameText.setText((String)arrayList.get(position).get("headMiddleup"));
		holder.oneKeyTimeText.setText((String)arrayList.get(position).get("headMiddledown"));
		holder.oneKeyMoneyText.setText((String)arrayList.get(position).get("headMoney"));
		holder.oneKeyTrafficImg.setImageResource((Integer) arrayList.get(position).get("middleImg"));
		holder.oneKeyTitleText.setText((String)arrayList.get(position).get("middleUp"));
		holder.oneKeyContentText.setText((String)arrayList.get(position).get("middleDown"));
		holder.oneKeyPassImg.setImageResource((Integer) arrayList.get(position).get("bottomLeftImg"));
		holder.oneKeyPassText.setText((String)arrayList.get(position).get("bottomLeftText"));
		holder.oneKeyUnPassImg.setImageResource((Integer) arrayList.get(position).get("bottomWrongImg"));
		holder.oneKeyUnPassText.setText((String)arrayList.get(position).get("bottomRightText"));
		if((Integer)arrayList.get(position).get("status") == 0){
			holder.passornotLayout.setVisibility(View.VISIBLE);
			holder.oneKeyFloat.setVisibility(View.GONE);
		}else if ((Integer)arrayList.get(position).get("status") == 1) {
			holder.passornotLayout.setVisibility(View.GONE);
			holder.oneKeyFloat.setImageResource(R.drawable.approval);
			holder.oneKeyFloat.setVisibility(View.VISIBLE);
		}else {
			holder.passornotLayout.setVisibility(View.GONE);
			holder.oneKeyFloat.setImageResource(R.drawable.unapproval);
			holder.oneKeyFloat.setVisibility(View.VISIBLE);
		}
		
		holder.headNameLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(context, UserInfoActivity.class);
				context.startActivity(intent);
				
			}
		});
		
		holder.passLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				holder.passornotLayout.setVisibility(View.GONE);
//				holder.oneKeyFloat.setImageResource(R.drawable.approval);
//				holder.oneKeyFloat.setVisibility(View.VISIBLE);
				arrayList.get(position).put("status", 1);
				notifyDataSetChanged();
				
			}
		});
		
		holder.unpassLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				holder.passornotLayout.setVisibility(View.GONE);
//				holder.oneKeyFloat.setImageResource(R.drawable.unapproval);
//				holder.oneKeyFloat.setVisibility(View.VISIBLE);
				arrayList.get(position).put("status", 2);
				notifyDataSetChanged();
			}
		});
		
		return convertView;
	}

	/**
	 * 
	 * holder类，存储用到的控件
	 */
	static  class OneKeyApprovalHolder{
		ImageView oneKeyUserHeadImg;
		TextView oneKeyUserNameText;
		TextView oneKeyTimeText;
		TextView oneKeyMoneyText;
		ImageView oneKeyTrafficImg;
		TextView oneKeyTitleText;
		TextView oneKeyContentText;
		ImageView oneKeyPassImg;
		ImageView oneKeyUnPassImg;
		TextView oneKeyPassText;
		TextView oneKeyUnPassText;
		ImageView oneKeyFloat;
		
//		ImageView userHeadImg;
		LinearLayout headNameLayout;
		
		LinearLayout passLayout;
		LinearLayout unpassLayout;
		LinearLayout passornotLayout;
		
	}

}
