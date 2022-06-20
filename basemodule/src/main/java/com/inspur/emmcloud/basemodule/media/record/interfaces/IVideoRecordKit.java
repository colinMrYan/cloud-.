package com.inspur.emmcloud.basemodule.media.record.interfaces;


import com.inspur.emmcloud.basemodule.media.record.VideoRecordConfig;
import com.inspur.emmcloud.basemodule.media.record.basic.VideoKitResult;

/**
 * 腾讯云短视频UGCKit({@code IVideoRecordKit}):视频录制。
 * 本组件包含视频录制的所有SDK功能和UI展示，包括拍照，多段录制，草稿箱，美颜，变声和混响，添加背景音乐，多屏比录制，变速录制等等。<br>
 * 您可以通过UGCKit很简单搭建使用诸多视频录制功能，也可以定制视频录制的功能和UI
 * <p>
 * 如下演示了UGCKit视频录制模块简单用法：<br>
 * 1、在xml中设置
 * <pre>
 * {@code
 * <com.tencent.qcloud.xiaoshipin.uikit.module.record.VideoRecord
 *         android:id="@+id/video_record_layout"
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent" />
 * }</pre>
 * <p>
 * 2、在Activity中设置
 * <pre>
 *     &#064;Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.ugcrecord_activity_video_record);
 *
 *         mVideoRecord = (VideoRecord) findViewById(R.id.video_record_layout);
 *         mVideoRecord.setOnRecordListener(new IVideoRecordKit.OnRecordListener() {
 *             &#064;Override
 *             public void onRecordCanceled() {
 *             }
 *
 *             &#064;Override
 *             public void onRecordCompleted(String outputPath) {
 *             }
 *         });
 *     }
 *
 *     &#064;Override
 *     protected void onStart() {
 *         super.onStart();
 *         if (hasPermission()) {
 *             mVideoRecord.start();
 *         }
 *     }
 *
 *     &#064;Override
 *     protected void onStop() {
 *         super.onStop();
 *         mVideoRecord.stop();
 *     }
 *
 *     &#064;Override
 *     protected void onDestroy() {
 *         super.onDestroy();
 *         mVideoRecord.release();
 *     }
 * </pre>
 * <p>
 * UGCKit视频录制模块的生命周期方法<br>
 * 1、当Activity执行生命周期方法{@code onStart()}时，UGCKit需要执行{@link #start()}初始化录制配置，开始视频录制预览界面<br>
 * 2、当Activity执行生命周期方法{@code onStop()}时，UGCKit需要执行{@link #stop()}暂停视频录制，关闭视频录制预览界面，并更新视频录制界面<br>
 * 3、当Activity执行生命周期方法{@code onDestroy()}时，UGCKit需要执行{@link #release()}来释放资源<br>
 * 4、当您开启Activity 的界面旋转后，Activity执行生命周期方法{@code onConfigurationChanged()}时，
 * UGCKit需要执行{@link #screenOrientationChange()}来更改屏幕预览方向<br>
 * 5、当Activity执行 {@code stopPlay()} 时，UGCKit需要执行{@link #backPressed()} 退出视频录制，返回上一界面
 * <p>
 * <p>
 * 如果您不使用UGCKit视频录制组件，自行搭建UI，调用SDK功能。<br>
 * 请参照文档
 * <a href="https://cloud.tencent.com/document/product/584/9369">拍照和录制(Android)</a>
 * <a href="https://cloud.tencent.com/document/product/584/20318">多段录制(Android)</a>
 * <a href="https://cloud.tencent.com/document/product/584/20320">录制草稿箱(Android)</a>
 * <a href="https://cloud.tencent.com/document/product/584/20316">添加背景音乐(Android)</a>
 * <a href="https://cloud.tencent.com/document/product/584/20322">变声和混响(Android)</a>
 */
public interface IVideoRecordKit {

    /**
     * 当Activity执行生命周期方法{@code onStart()}时，UGCKit需要执行{@link #start()}来完成如下功能<br>
     * 1、初始化录制配置<br>
     * 2、开始视频录制预览界面
     */
    void start();

    /**
     * 当Activity执行生命周期方法{@code onStop()}时，UGCKit需要执行{@link #stop()}来完成如下功能<br>
     * 1、暂停视频录制<br>
     * 2、关闭视频录制预览界面
     * 3、更新视频录制界面
     */
    void stop();

    /**
     * 当Activity执行生命周期方法{@code onDestroy()}时，UGCKit需要执行{@link #release()}来释放资源<br>
     */
    void release();

    /**
     * 当Activity执行生命周期方法{@code onConfigurationChanged()}时，UGCKit需要执行{@link #screenOrientationChange()}来更改屏幕预览方向<br>
     */
    void screenOrientationChange();

    /**
     * 退出视频录制，返回上一界面
     */
    void backPressed();

    /**
     * 设置输入选项
     */
    void setConfig(VideoRecordConfig config);

    /**
     * 设置视频录制的监听器
     */
    void setOnRecordListener(OnRecordListener listener);

    /**
     * 设置视频录制背景音乐选择监听器
     */
    void setOnMusicChooseListener(OnMusicChooseListener listener);

    interface OnMusicChooseListener {
        /**
         * 选择音乐
         *
         * @param position 音乐在音乐列表的第几个位置
         */
        void onChooseMusic(int position);
    }

    interface OnRecordListener {
        /**
         * 视频录制被取消
         */
        void onRecordCanceled();

        /**
         * 视频录制完成回调
         *
         * @param
         */
        void onRecordCompleted(VideoKitResult result);
    }

    /************************************************************************/
    /*****                     功能定制化                                 *****/
    /************************************************************************/

    /**
     * 视频录制完是否进行视频特效处理
     *
     * @param enable {@code true} 进行视频特效编辑<br>
     *               {@code false} 直接输出录制完视频<br>
     *               默认为true
     */
    void setEditVideoFlag(boolean enable);

    /**
     * 禁用录制速度功能<br>
     * 1、使用标准速度进行视频录制<br>
     * 2、关闭录制速度面板
     */
    void disableRecordSpeed();

    /**
     * 禁用拍照功能<br>
     * 不显示拍照界面
     */
    void disableTakePhoto();

    /**
     * 禁用长按录制
     */
    void disableLongPressRecord();

    /**
     * 禁用屏比功能<br>
     * 默认使用全屏进行视频录制
     */
    void disableAspect();

    /**
     * 禁用美颜功能
     */
    void disableBeauty();

}
