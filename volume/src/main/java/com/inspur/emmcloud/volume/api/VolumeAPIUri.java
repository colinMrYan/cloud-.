package com.inspur.emmcloud.volume.api;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;

public class VolumeAPIUri {

    /***********************VOLUME云盘****************/
    /**
     * ECMCloudDriver服务
     *
     * @return
     */
    public static String getCloudDriver() {
//        return "https://cloud-drive.inspures.com/cloud-drive/rest/v1";
        return WebServiceRouterManager.getInstance().getClusterCloudDrive();
    }

    /**
     * 获取云盘列表
     *
     * @return
     */
    public static String getUrlBaseVolume() {
        //return getCloudDriver() + "/volume";
        /** 测试 用return "http://10.25.12.114:3001/cloud-drive/rest/v1/volume";**/
        return getCloudDriver() + "/volume";
    }

    public static String getVolumeListUrl() {
        return getUrlBaseVolume();
    }

    /**
     * 更新网盘信息
     *
     * @param volumeId
     * @return
     */
    public static String getUpdateVolumeInfoUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId;
    }

    /**
     * 获取云盘成员url
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeMemUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/member";
    }

    /**
     * 获取云盘组url
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeGroupUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/group";
    }

    public static String getUrlBaseGroup() {
        return getCloudDriver() + "/group";
    }

    /**
     * 获取组url
     *
     * @param groupId
     * @return
     */
    public static String getGroupBaseUrl(String groupId) {
        return getUrlBaseGroup() + "/" + groupId;
    }

    /**
     * 获取组成员URL
     *
     * @param groupId
     * @return
     */
    public static String getGroupMemBaseUrl(String groupId) {
        return getGroupBaseUrl(groupId) + "/member";
    }

    /**
     * 获取云盘文件列表
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeFileOperationUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file";
    }

    /**
     * 获取云盘上传STS token
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeFileUploadSTSTokenUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file/request";
    }

    public static String getVolumeFileTypeImgThumbnailUrl(VolumeFile volumeFile, String volumeFilePath) {
//        volumeFilePath = StringUtils.utf8Encode(volumeFilePath, volumeFilePath);
        return getVolumeFileUploadSTSTokenUrl(volumeFile.getVolume()) + "?path=" + StringUtils.encodeURIComponent(volumeFilePath) + "&volumeId=" + volumeFile.getVolume() + "&resize=true&l=300&m=mfit";
    }

    public static String getVolumeFileDownloadUrl(VolumeFile volumeFile, String currentDirAbsolutePath) {
        return getUrlBaseVolume() + "/" + volumeFile.getVolume() + "/file/request?path=" + StringUtils.encodeURIComponent(currentDirAbsolutePath + volumeFile.getName());
    }

    /**
     * 获取云盘创建文件夹url
     *
     * @param volumeId
     * @return
     */
    public static String getCreateForderUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/directory";
    }

    /**
     * 获取文件重命名url
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeFileRenameUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file/name";
    }

    /**
     * 获取云盘文件移动url
     *
     * @param volumeId
     * @return
     */
    public static String getMoveVolumeFileUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file/path";
    }

    /**
     * 获取复制文件的url
     *
     * @param volumeId
     * @return
     */
    public static String getCopyVolumeFileUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file/duplication";
    }

    /**
     * 夸网盘复制文件或者文件夹的Url
     ***/
    public static String getCopyFileBetweenVolumeUrl(String fromVolumeId, String toVolumeId) {
        return getUrlBaseVolume() + "/" + fromVolumeId + "/file/share/volume/" + toVolumeId;
    }

    /**
     * 夸网盘复制文件或者文件夹的Url
     ***/
    public static String getCopyOrMoveFileBetweenVolumeUrl(String fromVolumeId) {
        return getUrlBaseVolume() + "/" + fromVolumeId + "/file/operation";
        // return "http://10.25.12.114:3001/cloud-drive/rest/v1/volume/"+ fromVolumeId + "/file/operation";
    }

    /**
     * 根据volumeId
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeFileGroupUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file/group/privilege";
    }
}
