package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeGroupContainMe;

/**
 * Created by chenmch on 2018/1/24.
 */

public class VolumeGroupContainMeCacheUtils {

    /**
     * 存储
     *
     * @param context
     */
    public static void saveVolumeGroupContainMe(Context context,
                                                VolumeGroupContainMe volumeGroupContainMe) {
        if (volumeGroupContainMe == null) {
            return;
        }
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(volumeGroupContainMe);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /***
     * 获取
     * @param context
     * @param volumeId
     * @return
     */
    public static VolumeGroupContainMe getVolumeGroupContainMe(Context context, String volumeId) {
        try {
            if (!StringUtils.isBlank(volumeId)) {
                return DbCacheUtils.getDb(context).findById(VolumeGroupContainMe.class, volumeId);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
