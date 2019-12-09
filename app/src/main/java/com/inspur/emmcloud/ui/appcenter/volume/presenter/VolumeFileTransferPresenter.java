package com.inspur.emmcloud.ui.appcenter.volume.presenter;

import com.inspur.emmcloud.basemodule.mvp.BasePresenter;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.ui.appcenter.volume.contract.VolumeFileTransferContract;
import com.inspur.emmcloud.util.privates.VolumeFileDownloadManager;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件传输
 *
 * @author zhangyj.lc
 */
public class VolumeFileTransferPresenter extends BasePresenter<VolumeFileTransferContract.View> implements VolumeFileTransferContract.Presenter {

    public void setData() {

    }

    @Override
    public List<VolumeFile> getUnFinishVolumeFileList(int index) {
        List<VolumeFile> list = new ArrayList<>();
        switch (index) {
            case 0:
                list = VolumeFileDownloadManager.getInstance().getDownloadingList();
                break;
            case 1:
                list = VolumeFileUploadManager.getInstance().getUnFinishUploadList();
                break;
        }
        return list;
    }

    @Override
    public List<VolumeFile> getFinishVolumeFileList(int index) {
        List<VolumeFile> list = new ArrayList<>();
        switch (index) {
            case 0:
                list = VolumeFileDownloadManager.getInstance().getDownloadedList();
                break;
            case 1:
                list = VolumeFileUploadManager.getInstance().getFinishUploadList();
                break;
//            case 2:
//                List<File> fileList = FileDownloadManager.getInstance().getFileDownloadFileList(DownloadFileCategory.CATEGORY_VOLUME_FILE);
//                for (File file : fileList) {
//                    VolumeFile volumeFile = VolumeFile.getMockVolumeFile(file, "123");
//                    volumeFile.setLastUpdate(file.lastModified());
//                    volumeFile.setStatus(VolumeFile.STATUS_NORMAL);
//                    list.add(volumeFile);
//                }
//                break;
            default:
                break;
        }
//        if (list.size() > 0) {
//            mView.showListLayout();
//        } else {
//            mView.showNoDataLayout();
//        }
        return list;
    }
}
