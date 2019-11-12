package com.inspur.emmcloud.basemodule.util;

import com.inspur.emmcloud.basemodule.bean.ApiRequestRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/10/11.
 */

public class ApiRequestRecordCacheUtils {
    public static void saveApiRequestRecord(ApiRequestRecord apiRequestRecord) {
        try {
            if (!DbCacheUtils.isDbNull()) {
                DbCacheUtils.getDb().saveOrUpdate(apiRequestRecord);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<ApiRequestRecord> getApiRequestRecordList(int maxSize) {
        List<ApiRequestRecord> apiRequestRecordList = null;
        try {
            apiRequestRecordList = DbCacheUtils.getDb().selector(ApiRequestRecord.class).where("startTime", ">", 0).limit(maxSize).orderBy("startTime", true).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (apiRequestRecordList == null) {
            apiRequestRecordList = new ArrayList<>();
        }
        return apiRequestRecordList;

    }

    public static void deleteApiRequestRecordList(List<ApiRequestRecord> apiRequestRecordList) {
        if (apiRequestRecordList == null || apiRequestRecordList.size() == 0) {
            return;
        }
        try {
            DbCacheUtils.getDb().delete(apiRequestRecordList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
