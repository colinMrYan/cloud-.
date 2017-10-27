package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;

import java.util.ArrayList;
import java.util.List;


public class MsgInputAddItemAdapter extends BaseAdapter {
	
	private Context context;
	private List<Integer> functionImgList = new ArrayList<>();
	private List<String> functionNameList = new ArrayList<>();
	public MsgInputAddItemAdapter(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return functionImgList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		convertView = LayoutInflater.from(context).inflate(R.layout.msg_add_item_view, null);
		((ImageView)convertView.findViewById(R.id.img)).setImageResource(functionImgList.get(position));
		((TextView)convertView.findViewById(R.id.text)).setText(functionNameList.get(position));
		return convertView;
	}
	
	/**
	 * 更新聊天页面输入框添加功能显示列表
	 */
	public void updateGridView(List<Integer> functionImgList,List<String> functionNameList){
		this.functionImgList = functionImgList;
		this.functionNameList = functionNameList;
		notifyDataSetChanged();
	}
	
}
