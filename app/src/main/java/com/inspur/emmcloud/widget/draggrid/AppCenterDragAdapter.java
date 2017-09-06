package com.inspur.emmcloud.widget.draggrid;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.ui.app.AppDetailActivity;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.widget.ImageViewRound;

public class AppCenterDragAdapter extends BaseAdapter {

	private Context context;
	boolean isVisible = true;
	private List<App> appList;
	private ImageDisplayUtils imageDisplayUtils;
	public AppCenterDragAdapter(Context context, List<App> appList) {
		this.context = context;
		this.appList = appList;
		imageDisplayUtils = new ImageDisplayUtils(R.drawable.icon_empty_icon);
	}

	public AppCenterDragAdapter(Context context, List<App> appList, int position) {
		this.context = context;
		this.appList = appList;
	}

	@Override
	public int getCount() {
		int count = 0;
		if(appList != null){
			count = (appList.size()>4 ? 4 : appList.size());
		}
		return count;
	}

	@Override
	public App getItem(int position) {
		if (appList != null && appList.size() != 0) {
			return appList.get(position);
		}
		return new App();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		App app = getItem(position);
		convertView = LayoutInflater.from(context).inflate(
				R.layout.my_app_item_view, null);
		ImageViewRound iconImg = (ImageViewRound) convertView
				.findViewById(R.id.icon_image);
		iconImg.setType(ImageViewRound.TYPE_ROUND);
		iconImg.setRoundRadius(DensityUtil.dip2px(context, 10));
		TextView nameText = (TextView) convertView.findViewById(R.id.name_text);
		nameText.setText(app.getAppName());
		imageDisplayUtils.displayImage(iconImg, app.getAppIcon());
		iconImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("app", getItem(position));
				intent.setClass(context, AppDetailActivity.class);
				context.startActivity(intent);
			}
		});
		return convertView;
	}

}