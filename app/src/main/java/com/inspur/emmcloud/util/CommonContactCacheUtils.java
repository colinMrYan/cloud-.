package com.inspur.emmcloud.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.inspur.emmcloud.bean.SearchModel;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

public class CommonContactCacheUtils {
	private static final int SEARCH_ALL = 0;
	private static final int SEARCH_CONTACT = 2;
	private static final int SEARCH_CHANNELGROUP = 1;
	private static final int SEARCH_NOTHIING = 4;
	/**
	 * 保存通讯录联系人或群组的搜索次数
	 * @param context
	 * @param searchModel
	 */
	public static void saveCommonContact(final Context context,final SearchModel searchModel){
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					SearchModel originSearchModel = DbCacheUtils.getDb(context).findById(SearchModel.class, searchModel.getId());
					if (originSearchModel == null) {
						searchModel.setHeat(1);
					}else {
						int heat = originSearchModel.getHeat();
						heat = heat+1;
						searchModel.setHeat(heat);
					}
					DbCacheUtils.getDb(context).saveOrUpdate(searchModel);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		new Thread(runnable).start();
	}
	
	/**
	 * 获取常用联系人列表
	 * @param context
	 * @param num
	 */
	public static List<SearchModel> getCommonContactList(Context context,int num,int selectContent){
		List<SearchModel> commonContactList = null;
		try {
			
			switch (selectContent) {
			case SEARCH_ALL:
			case SEARCH_NOTHIING:
				commonContactList = DbCacheUtils.getDb(context).findAll(Selector
						.from(SearchModel.class).orderBy("heat", true).limit(num));
				break;
			case SEARCH_CHANNELGROUP:
				commonContactList = DbCacheUtils.getDb(context).findAll(Selector
						.from(SearchModel.class).where("type", "=", "GROUP").orderBy("heat", true).limit(num));
				break;
			case SEARCH_CONTACT:
				commonContactList = DbCacheUtils.getDb(context).findAll(Selector
						.from(SearchModel.class).where("type", "=", "USER").orderBy("heat", true).limit(num));
			default:
				break;
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (commonContactList == null) {
			commonContactList = new ArrayList<SearchModel>();
		}
		return commonContactList;
	}
	

	
}
