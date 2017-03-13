package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.bean.PVCollectModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PVCollectModelCacheUtils {
	public static void saveCollectModel(Context context,
			PVCollectModel collectModel) {
		try {
			DbCacheUtils.getDb(context).save(collectModel);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<PVCollectModel> getCollectModelList(Context context){
		List<PVCollectModel> collectModelList = null;
		try {
			collectModelList = DbCacheUtils.getDb(context).findAll(PVCollectModel.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (collectModelList == null) {
			collectModelList = new ArrayList<PVCollectModel>();
		}
		return collectModelList;
	}

	public static String getCollectModelListJson(Context context){
		String CollectModelListJson = null;
		JSONArray array = new JSONArray();
		List<PVCollectModel> collectModelList = getCollectModelList(context);
		if (collectModelList.size()>0){
			for (int i=0;i< collectModelList.size();i++){
				PVCollectModel pvCollectModel = collectModelList.get(i);
				JSONObject obj = pvCollectModel.getObj();
				array.put(obj);
			}
			JSONObject allObj = new JSONObject();
			try {
				allObj.put("PVList",array);
				CollectModelListJson = allObj.toString();
			}catch (Exception e){
				e.printStackTrace();
			}

		}
		return  CollectModelListJson;
	}
	
	
	public static void deleteAllCollectModel(Context context){
		try {
			DbCacheUtils.getDb(context).deleteAll(PVCollectModel.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
