package com.inspur.emmcloud.basemodule.media.record.interfaces;

/**
 * 定制化"多模式录制的录制按钮"
 */

public interface IRecordButton {

    /**
     * 设置录制按钮监听器
     *
     */
    void setOnRecordButtonListener(OnRecordButtonListener listener);

    interface OnRecordButtonListener {
        /**
         * 录制点击开始
         */
        void onRecordStart();

        /**
         * 录制点击结束
         */
        void onRecordFinish();

        /**
         * 拍照
         */
        void onTakePhoto();

    }

    /**
     * 设置当前拍摄模式
     *
     */
    void setCurrentRecordMode(int recordMode);
}
