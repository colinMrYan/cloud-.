package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.bean.system.PVCollectModel;

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

	public static JSONArray getCollectModelListJson(Context context){
		JSONArray array = new JSONArray();
		List<PVCollectModel> collectModelList = getCollectModelList(context);
		if (collectModelList.size()>0){
			for (int i=0;i< collectModelList.size();i++){
				PVCollectModel pvCollectModel = collectModelList.get(i);
				JSONObject obj = pvCollectModel.getObj();
				array.put(obj);
			}
		}
		return  array;
	}
	
	
	public static void deleteAllCollectModel(Context context){
		try {
			DbCacheUtils.getDb(context).delete(PVCollectModel.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *@param  context
	 *@param  maxGetItemsNum  如果数据库剩余数据大于该值，获取数据库前maxGetItemnum条
	 *                        否则 获取数据库全部条数，生成JSON对象
	 * **/
	public static JSONArray getPartCollectModelListJson(Context context ,int maxGetItemsNum) {
		JSONArray array = new JSONArray();
		List<PVCollectModel> collectModelList = getPartCollectModelList(context,maxGetItemsNum);
		if (collectModelList.size()>0){
			for (int i=0;i< collectModelList.size();i++){
				PVCollectModel pvCollectModel = collectModelList.get(i);
				JSONObject obj = pvCollectModel.getObj();
				array.put(obj);
			}
		}
		return  array;
	}

	/**
	 *@param  context
	 *@param  maxGetItemsNum  如果数据库剩余数据大于该值，获取数据库前maxGetItemnum条
	 *                        否则 获取数据库全部条数,存入List
	 * **/
	public static List<PVCollectModel> getPartCollectModelList(Context context,int maxGetItemsNum){
		List<PVCollectModel> collectModelList = null;
		try {
			collectModelList = DbCacheUtils.getDb(context).selector(PVCollectModel.class).limit(maxGetItemsNum).findAll();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (collectModelList == null) {
			collectModelList = new ArrayList<PVCollectModel>();
		}
		return collectModelList;
	}

	/**
	 * @param context
	 * @param maxDelectItemsNum
	 * **/
	public static int deletePartCollectModel(Context context,int maxDelectItemsNum) {
		try {
			List<PVCollectModel> collectModelList = getPartCollectModelList(context,maxDelectItemsNum);
			DbCacheUtils.getDb(context).delete(collectModelList);
			return collectModelList.size();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return  -1;
		}
	}

}
