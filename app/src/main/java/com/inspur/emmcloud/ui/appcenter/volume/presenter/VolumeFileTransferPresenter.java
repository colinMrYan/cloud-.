package com.inspur.emmcloud.ui.appcenter.volume.presenter;

import com.inspur.emmcloud.basemodule.mvp.BasePresenter;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.ui.appcenter.volume.contract.VolumeFileTransferContract;
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
    public List<VolumeFile> getVolumeFileList(int index) {
        List<VolumeFile> list = new ArrayList<>();
        switch (index) {
            case 0:

                break;
            case 1:
                list = VolumeFileUploadManager.getInstance().getAllUploadVolumeFile();
                break;
            case 2:

                break;
            default:
                break;
        }
        if (list.size() > 0) {
            mView.showListLayout();
        } else {
            mView.showNoDataLayout();
        }
        return list;
    }
}
