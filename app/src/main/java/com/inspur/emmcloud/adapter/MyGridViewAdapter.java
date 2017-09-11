package com.inspur.emmcloud.adapter;

import java.util.ArrayList;
import java.util.List;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Attachment;
import com.inspur.emmcloud.util.ImageDisplayUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 废弃
 * @author sunqx
 *
 */
public class MyGridViewAdapter extends BaseAdapter {

	private Context context;
	private List<Attachment> mLists;
	public static final int PAGE_SIZE = 4; // 每一屏幕显示4个
	private ImageDisplayUtils imageDisplayUtils;
	public MyGridViewAdapter(Context context, List<Attachment> attachments, int page) {
		this.context = context;
		
		imageDisplayUtils = new ImageDisplayUtils(R.drawable.icon_photo_default);
		mLists = new ArrayList<Attachment>();
		int i = page *PAGE_SIZE;
		int end = i +PAGE_SIZE;
		while ((i < attachments.size()) && (i < end)) {
			mLists.add(attachments.get(i));
			i++;
		}
	}
	@Override
	public int getCount() {
		if(mLists.size()<4){
			return mLists.size()+1;
		}else {
			return mLists.size();
		}
		
	}
	@Override
	public Object getItem(int position) {
		
		return mLists.get(position);
	}
	@Override
	public long getItemId(int position) {	
		return position;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		Holder _Holder = null;
		if (null == convertView) {
			_Holder = new Holder();
			LayoutInflater mInflater = LayoutInflater.from(context);
			convertView = mInflater.inflate(R.layout.gridview_item, null);
			_Holder.btn_gv_item = (ImageView) convertView
					.findViewById(R.id.btn_gv_item);
			_Holder.btn_gv_item.setFocusable(false);
			convertView.setTag(_Holder);
			_Holder.textView = (TextView) convertView.findViewById(R.id.file_text);
		} else {
			_Holder = (Holder) convertView.getTag();
		}
		
		if(position < mLists.size()){
			if(mLists.get(position).getType().equals("JPEG")){
				imageDisplayUtils.displayImage(_Holder.btn_gv_item, "drawable://"
						+ R.drawable.icon_file_photos);
			}else if (mLists.get(position).getType().equals("MS_WORD")) {
				imageDisplayUtils.displayImage(_Holder.btn_gv_item, "drawable://"
						+ R.drawable.icon_file_word);
			}else if (mLists.get(position).getType().equals("MS_EXCEL")) {
				imageDisplayUtils.displayImage(_Holder.btn_gv_item, "drawable://"
						+ R.drawable.icon_file_excel);
			}else if (mLists.get(position).getType().equals("MS_PPT")) {
				imageDisplayUtils.displayImage(_Holder.btn_gv_item, "drawable://"
						+ R.drawable.icon_file_ppt);
			}else if (mLists.get(position).getType().equals("TEXT")) {
				imageDisplayUtils.displayImage(_Holder.btn_gv_item, "drawable://"
						+ R.drawable.icon_file_word);
			}	
			_Holder.textView.setText(mLists.get(position).getName());
		}else {
			
				imageDisplayUtils.displayImage(_Holder.btn_gv_item, "drawable://"
						+ R.drawable.icon_member_add);
				_Holder.textView.setText(context.getString(R.string.add));
				
		}
		
		
//		_Holder.btn_gv_item.setText(mLists.get(position));
		
		return convertView;
	}

	private static class Holder {
		ImageView btn_gv_item;
		TextView textView;
	}
	
}
