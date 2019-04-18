package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.system.PVCollectModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PVCollectModelCacheUtils {

    public static void saveCollectModel(String functionID, String functionType) {
        PVCollectModel pvCollectModel = new PVCollectModel(functionID, functionType);
        saveCollectModel(MyApplication.getInstance(), pvCollectModel);
    }

    private static void saveCollectModel(final Context context, final PVCollectModel collectModel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DbCacheUtils.getDb(context).save(collectModel);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static List<PVCollectModel> getCollectModelList(Context context) {
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

    public static JSONArray getCollectModelListJson(Context context) {
        JSONArray array = new JSONArray();
        List<PVCollectModel> collectModelList = getCollectModelList(context);
        if (collectModelList.size() > 0) {
            for (int i = 0; i < collectModelList.size(); i++) {
                PVCollectModel pvCollectModel = collectModelList.get(i);
                JSONObject obj = pvCollectModel.getObj();
                array.put(obj);
            }
        }
        return array;
    }


    public static void deleteAllCollectModel(Context context) {
        try {
            DbCacheUtils.getDb(context).delete(PVCollectModel.class);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param context 如果数据库剩余数据大于该值，获取数据库前maxGetItemnum条
     *                否则 获取数据库全部条数，生成JSON对象
     **/
    public static JSONArray getCollectModelListJson(Context context, List<PVCollectModel> parCollectModelList) {
        List<PVCollectModel> collectModelList = new ArrayList<>();
        collectModelList = parCollectModelList;
        JSONArray array = new JSONArray();
        if (collectModelList.size() > 0) {
            for (int i = 0; i < collectModelList.size(); i++) {
                PVCollectModel pvCollectModel = collectModelList.get(i);
                JSONObject obj = pvCollectModel.getObj();
                array.put(obj);
            }
        }
        return array;
    }

    /**
     * @param context
     * @param maxGetItemsNum 如果数据库剩余数据大于该值，获取数据库前maxGetItemnum条
     *                       否则 获取数据库全部条数,存入List
     **/
    public static List<PVCollectModel> getCollectModelList(Context context, int maxGetItemsNum) {
        List<PVCollectModel> collectModelList = new ArrayList<PVCollectModel>();
        try {
            collectModelList = DbCacheUtils.getDb(context).selector(PVCollectModel.class).orderBy("id").limit(maxGetItemsNum).findAll();
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
     **/
    public static void deleteCollectModel(Context context, List<PVCollectModel> collectModelList) {
        try {
            DbCacheUtils.getDb(context).delete(collectModelList);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
