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
            DbCacheUtils.getDb().saveOrUpdate(apiRequestRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<ApiRequestRecord> getApiRequestRecordList() {
        List<ApiRequestRecord> apiRequestRecordList = null;
        try {
            apiRequestRecordList = DbCacheUtils.getDb().findAll(ApiRequestRecord.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (apiRequestRecordList == null) {
            apiRequestRecordList = new ArrayList<>();
        }
        return apiRequestRecordList;

    }
}
