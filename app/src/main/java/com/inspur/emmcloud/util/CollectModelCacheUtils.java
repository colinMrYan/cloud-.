package com.inspur.emmcloud.util;

import java.util.ArrayList;
import java.util.List;

import com.inspur.emmcloud.bean.CollectModel;
import com.lidroid.xutils.exception.DbException;

import android.content.Context;

public class CollectModelCacheUtils {
	public static void saveCollectModel(Context context,
			CollectModel collectModel) {
		try {
			DbCacheUtils.getDb(context).save(collectModel);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<CollectModel> getCollectModelList(Context context){
		List<CollectModel> collectModelList = null;
		try {
			collectModelList = DbCacheUtils.getDb(context).findAll(CollectModel.class);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (collectModelList == null) {
			collectModelList = new ArrayList<CollectModel>();
		}
		return collectModelList;
	}
	
	
	public static void deleteAllCollectModel(Context context){
		try {
			DbCacheUtils.getDb(context).deleteAll(CollectModel.class);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
