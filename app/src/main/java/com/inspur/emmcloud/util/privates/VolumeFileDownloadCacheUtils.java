package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/11/4.
 */

public class VolumeFileDownloadCacheUtils {

    /**
     * 获取正在下载中的云盘文件
     *
     * @return
     */
    public List<VolumeFile> getVolumeFileListInDownloading() {
        List<VolumeFile> volumeFileList = null;
        try {
            volumeFileList = DbCacheUtils.getDb().findAll(VolumeFile.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (volumeFileList == null) {
            volumeFileList = new ArrayList<>();
        }
        return volumeFileList;
    }

    public void saveVolumeFile(VolumeFile volumeFile) {
        try {
            if (volumeFile != null) {
                DbCacheUtils.getDb().saveOrUpdate(volumeFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteVolumeFile(VolumeFile volumeFile) {
        try {
            if (volumeFile != null) {
                DbCacheUtils.getDb().delete(volumeFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveVolumeFileList(List<VolumeFile> volumeFileList) {
        try {
            if (volumeFileList != null && volumeFileList.size() > 0) {
                DbCacheUtils.getDb().saveOrUpdate(volumeFileList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
