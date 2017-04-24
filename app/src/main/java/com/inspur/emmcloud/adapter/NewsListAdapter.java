package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.GroupNews;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;

import java.util.List;

/**
 * 新闻列表
 * @author sunqx
 *
 */

public class NewsListAdapter extends BaseAdapter{
	
	private Context context;
	
	private List<GroupNews> groupNewsList;
	private ImageDisplayUtils imageDisplayUtils;
	public NewsListAdapter(Context context,List<GroupNews> groupNewsList) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.groupNewsList =groupNewsList;
		imageDisplayUtils = new ImageDisplayUtils(context, R.drawable.group_news_ic);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return groupNewsList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return groupNewsList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		LayoutInflater inflater = LayoutInflater.from(context);
		NewsHolder holder = new NewsHolder();
		if(convertView == null){
			
			convertView = inflater.inflate(R.layout.news_fragment_listitem, null);
			holder.imageView = (ImageView) convertView.findViewById(R.id.news_leftImg_img);
			holder.title = (TextView) convertView.findViewById(R.id.news_middleUp_text);
			holder.content = (TextView) convertView.findViewById(R.id.news_middleDown_text);
			holder.textposer = (TextView) convertView.findViewById(R.id.news_middlemid_text);
			
			convertView.setTag(holder);
		}else {
			holder = (NewsHolder) convertView.getTag();
		}
		String uri = handlePoster(position);
		imageDisplayUtils.display(holder.imageView, uri);
		if(groupNewsList.get(position).isImportant()){
			holder.title.setTextColor(Color.RED);
			holder.title.setText((String)groupNewsList.get(position).getTitle());
		}else {
			holder.title.setTextColor(0xff203b4f);
			holder.title.setText((String)groupNewsList.get(position).getTitle());
		}
		
		holder.content.setText((String)groupNewsList.get(position).getDigest());
		String postTime = groupNewsList.get(position).getPosttime();
		if (postTime != null && postTime.endsWith("Z")) {
			postTime = postTime.substring(0,postTime.length()-1);
		}
		holder.textposer.setText(groupNewsList.get(position).getAuthor()+"  "+postTime);
		
		return convertView;
	}

	/**
	 * 处理poster
	 * @param position
	 * @return
     */
	private String handlePoster(int position) {
		if(!StringUtils.isBlank(groupNewsList.get(position).getPoster())){
			return UriUtils.getPreviewUri(groupNewsList.get(position).getPoster());
		}
		return "";
	}

	public static class NewsHolder {
		ImageView imageView;
		TextView title;
		TextView content;
		TextView textposer;
	}

}
