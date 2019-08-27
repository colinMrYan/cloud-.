package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileListResult;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeGroupContainMe;
import com.inspur.emmcloud.util.privates.cache.VolumeGroupContainMeCacheUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenmch on 2018/1/26.
 */

public class VolumeFilePrivilegeUtils {

    public static boolean canGetVolumeFilePrivilege(Context context, Volume volume) {
        return (!volume.getType().equals("public") || VolumeGroupContainMeCacheUtils.getVolumeGroupContainMe(context, volume.getId()) != null);
    }

    /**
     * 判断一组文件的最低权限
     *
     * @param context
     * @param volumeFileList
     * @return
     */
    public static boolean getVolumeFileListWriteable(Context context, List<VolumeFile> volumeFileList) {
        for (int i = 0; i < volumeFileList.size(); i++) {
            if (getVolumeFileWriteable(context, volumeFileList.get(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否有写权限
     *
     * @param context
     * @param volumeFile
     * @return
     */
    public static boolean getVolumeFileWriteable(Context context, VolumeFile volumeFile) {
        int privilege = 0;
        String myUid = MyApplication.getInstance().getUid();
        LogUtils.jasonDebug("======myUid=" + myUid);
        LogUtils.jasonDebug("=======volumeFile.getOwner()=" + volumeFile.getOwner());
        if (volumeFile.getOwner().equals(myUid)) {
            LogUtils.jasonDebug("===========isowner");
            privilege = volumeFile.getOwnerPrivilege();
        } else {
            VolumeGroupContainMe volumeGroupContainMe = VolumeGroupContainMeCacheUtils.getVolumeGroupContainMe(context, volumeFile.getVolume());
            if (volumeGroupContainMe != null) {
                LogUtils.jasonDebug("===========有自己所属组数据");
                List<String> groupIdList = volumeGroupContainMe.getGroupIdList();
                List<Integer> privilegeList = new ArrayList<>();
                for (int i = 0; i < groupIdList.size(); i++) {
                    String groupId = groupIdList.get(i);
                    Map<String, Integer> groupPrivilegeMap = volumeFile.getGroupPrivilegeMap();
                    Integer groupPrivilege = groupPrivilegeMap.get(groupId);
                    if (groupPrivilege != null) {
                        privilegeList.add(groupPrivilege);
                    }
                }
                privilege = Collections.max(privilegeList);
            } else {
                LogUtils.jasonDebug("===========没有自己所属组数据");
                privilege = volumeFile.getOthersPrivilege();
            }
        }
        LogUtils.jasonDebug("===========最终privilege=" + privilege);
        return (privilege > 4);
    }

    public static boolean getVolumeFileWriteable(Context context, GetVolumeFileListResult getVolumeFileListResult) {
        int privilege = 0;
        String myUid = MyApplication.getInstance().getUid();
        if (getVolumeFileListResult.getOwner().equals(myUid)) {
            privilege = getVolumeFileListResult.getOwnerPrivilege();
        } else {
            VolumeGroupContainMe volumeGroupContainMe = VolumeGroupContainMeCacheUtils.getVolumeGroupContainMe(context, getVolumeFileListResult.getVolume());
            if (volumeGroupContainMe != null) {
                List<String> groupIdList = volumeGroupContainMe.getGroupIdList();
                List<Integer> privilegeList = new ArrayList<>();
                for (int i = 0; i < groupIdList.size(); i++) {
                    String groupId = groupIdList.get(i);
                    Map<String, Integer> groupPrivilegeMap = getVolumeFileListResult.getGroupPrivilegeMap();
                    Integer groupPrivilege = groupPrivilegeMap.get(groupId);
                    if (groupPrivilege != null) {
                        privilegeList.add(groupPrivilege);
                    }
                }
                privilege = Collections.max(privilegeList);
                LogUtils.YfcDebug("privilege:" + privilege);
            } else {
                privilege = getVolumeFileListResult.getOthersPrivilege();
                LogUtils.YfcDebug("privilege:" + privilege);
            }
        }
        return (privilege > 4);
    }


//    private static VolumeFilePrivilegeUtils mInstance;
//
//
//    private VolumeFilePrivilegeUtils() {
//    }
//
//    public static VolumeFilePrivilegeUtils getInstance() {
//        if (mInstance == null) {
//            synchronized (VolumeFilePrivilegeUtils.class) {
//                if (mInstance == null) {
//                    mInstance = new VolumeFilePrivilegeUtils();
//                }
//            }
//        }
//        return mInstance;
//    }
//
//    /**
//     * 判断是否有写权限
//     * @param context
//     * @param volumeFile
//     * @return
//     */
//    public boolean getFileWriteable(Context context, VolumeFile volumeFile){
//        int privilege = 0;
//        String myUid = MyApplication.getInstance().getUid();
//        if (volumeFile.getOwner().equals(myUid)){
//            privilege = volumeFile.getOwnerPrivilege();
//        }else {
//            VolumeGroupContainMe volumeGroupContainMe = VolumeGroupContainMeCacheUtils.getVolumeGroupContainMe(context, volumeFile.getVolume());
//            if (volumeGroupContainMe != null) {
//                List<String> groupIdList = volumeGroupContainMe.getGroupIdList();
//                List<Integer> privilegeList = new ArrayList<>();
//                for (int i = 0; i < groupIdList.size(); i++) {
//                    String groupId = groupIdList.get(i);
//                    Map<String, Integer> groupPrivilegeMap = volumeFile.getGroupPrivilegeMap();
//                    Integer groupPrivilege = groupPrivilegeMap.get(groupId);
//                    if (groupPrivilege != null) {
//                        privilegeList.add(groupPrivilege);
//                    }
//                }
//                privilege = Collections.max(privilegeList);
//            } else {
//                getVolumeGroupContainMe();
//            }
//        }
//        return (privilege > 4);
//    }
//
//    /**
//     * 获取网盘下包含自己的组
//     */
//    private void getVolumeGroupContainMe() {
//        if (NetUtils.isNetworkConnected(MyApplication.getInstance(),false)) {
//            MyAppAPIService apiServiceBase = new MyAppAPIService(MyApplication.getInstance());
//            apiServiceBase.getVolumeGroupContainMe(volume.getId());
//        } else {
//            LoadingDialog.dimissDlg(loadingDlg);
//        }
//    }
//
//    private class Webservice extends APIInterfaceInstance{
//        @Override
//        public void returnVolumeGroupContainMeSuccess(GetVolumeGroupResult getVolumeGroupResult) {
//            List<String> groupIdList = getVolumeGroupResult.getGroupIdList();
//            isFileWriteable = getVolumeFileListResult.isFileWriteable(groupIdList);
//            setCurrentDirectoryLayoutByPrivilege();
//            VolumeGroupContainMe volumeGroupContainMe = new VolumeGroupContainMe(volume.getId(), groupIdList);
//            VolumeGroupContainMeCacheUtils.saveVolumeGroupContainMe(getApplicationContext(), volumeGroupContainMe);
//        }
//
//        @Override
//        public void returnVolumeGroupContainMeFail(String error, int errorCode) {
//            LoadingDialog.dimissDlg(loadingDlg);
//            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
//        }
//    }
}
