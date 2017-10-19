package com.inspur.emmcloud.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;

public class MsgAddItemAdapter extends BaseAdapter {
	
	private Context context;
	private int[] imgArray = {R.drawable.icon_select_album,R.drawable.icon_select_take_photo,R.drawable.icon_select_file};
	private int[] textArray = {R.string.album,R.string.take_photo,R.string.file};

	private List<Integer> imgList = new ArrayList<Integer>();
	private List<Integer> textList = new ArrayList<Integer>();
	public  MsgAddItemAdapter(Context context) {
		this.context = context;
	}
	
	
	@Override
	public int getCount() {
		return imgList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater vi = (LayoutInflater) context
				.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.msg_add_item_view, null);
		ImageView img = (ImageView)convertView.findViewById(R.id.img);
		TextView text = (TextView)convertView.findViewById(R.id.text);
		img.setImageResource(imgList.get(position));
		text.setText(textList.get(position));
		return convertView;
	}
	
	/**
	 * 更新GridView
	 */
	public void updateGridView(List<Integer> imgList,List<Integer> textList){
		this.imgList = imgList;
		this.textList = textList;
		notifyDataSetChanged();
	}
	
}
