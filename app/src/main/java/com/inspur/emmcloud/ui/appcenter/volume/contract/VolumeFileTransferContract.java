package com.inspur.emmcloud.ui.appcenter.volume.contract;

import com.inspur.emmcloud.basemodule.mvp.BaseView;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;

import java.util.List;

public interface VolumeFileTransferContract {
    interface Model {

    }

    interface View extends BaseView {
        void showNoDataLayout();

        void showListLayout();
    }

    interface Presenter {
        List<VolumeFile> getVolumeFileList(int index);
    }
}
