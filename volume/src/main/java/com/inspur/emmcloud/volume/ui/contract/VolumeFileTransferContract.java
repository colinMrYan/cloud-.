package com.inspur.emmcloud.volume.ui.contract;

import com.inspur.emmcloud.basemodule.mvp.BaseView;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;

import java.util.List;

public interface VolumeFileTransferContract {
    interface Model {

    }

    interface View extends BaseView {
        void showNoDataLayout();

        void showListLayout();
    }

    interface Presenter {
        List<VolumeFile> getFinishVolumeFileList(int index);

        List<VolumeFile> getUnFinishVolumeFileList(int index);
    }
}
