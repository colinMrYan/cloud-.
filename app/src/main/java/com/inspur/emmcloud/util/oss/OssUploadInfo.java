package com.inspur.emmcloud.util.oss;

import com.inspur.emmcloud.bean.Volume.VolumeFile;

/**
 * Created by chenmch on 2017/11/24.
 */

public class OssUploadInfo {
    private OssService ossService;
    private VolumeFile volumeFile;
    private String parentPath;

    public OssUploadInfo( OssService ossService, VolumeFile volumeFile, String parentPath) {
        this.ossService = ossService;
        this.volumeFile = volumeFile;
        this.parentPath = parentPath;
    }

    public OssService getOssService() {
        return ossService;
    }

    public void setOssService(OssService ossService) {
        this.ossService = ossService;
    }

    public VolumeFile getVolumeFile() {
        return volumeFile;
    }

    public void setVolumeFile(VolumeFile volumeFile) {
        this.volumeFile = volumeFile;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }
}
