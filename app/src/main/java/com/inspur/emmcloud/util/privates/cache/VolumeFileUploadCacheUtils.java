package com.inspur.emmcloud.util.privates.cache;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFileUpload;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/9/16.
 */

public class VolumeFileUploadCacheUtils {
    public static List<VolumeFileUpload> getVolumeFileUploadList() {
        List<VolumeFileUpload> volumeFileUploadList = null;
        try {
            volumeFileUploadList = DbCacheUtils.getDb().selector(VolumeFileUpload.class).orderBy("id").findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (volumeFileUploadList == null) {
            volumeFileUploadList = new ArrayList<>();
        }
        return volumeFileUploadList;
    }

    public static void saveVolumeFileUpload(VolumeFileUpload volumeFileUpload) {
        try {
            if (volumeFileUpload != null) {
                DbCacheUtils.getDb().saveOrUpdate(volumeFileUpload);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveVolumeFileUploadList(List<VolumeFileUpload> volumeFileUploadList) {
        try {
            if (volumeFileUploadList != null && volumeFileUploadList.size() > 0) {
                DbCacheUtils.getDb().saveOrUpdate(volumeFileUploadList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteVolumeFileUpload(VolumeFileUpload volumeFileUpload) {
        try {
            DbCacheUtils.getDb().delete(volumeFileUpload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
