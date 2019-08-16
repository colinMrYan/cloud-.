package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CommonCircleIndicator.CircleIndicator;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.system.GetUpgradeResult;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class UpgradeUtils extends APIInterfaceInstance {

    protected static final int SHOW_PEOGRESS_LAODING_DLG = 0;
    private static final int notUpdateInterval = 86400000;
    private static final int DOWNLOAD = 3;
    private static final int DOWNLOAD_FINISH = 4;
    private static final int DOWNLOAD_FAIL = 5;
    private static final int NO_NEED_UPGRADE = 10;
    private static final int UPGRADE_FAIL = 11;
    private static final int DONOT_UPGRADE = 12;
    private static final String TAG = "UpgradeUtils";
    private static double MBDATA = 1048576.0;
    private static double KBDATA = 1024.0;
    private static String DOWNLOAD_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/download/";
    private Boolean cancelUpdate = false;
    private GetUpgradeResult getUpgradeResult;
    private Context context;
    private Handler upgradeHandler;
    private Handler handler;
    private int upgradeCode;
    private String upgradeUrl;
    private long totalSize;
    private long downloadSize;
    /**
     * 记录进度条数量*
     */
    private int progress;
    private MyDialog progressDownloadDialog;
    private TextView ratioText;
    private String upgradeMsg;
    private String downloadPercent;
    private LoadingDialog loadingDlg;
    private Cancelable cancelable;
    private boolean isManualCheck;
    private ProgressBar downloadProgressBar;
    private TextView percentText;

    private List<String> updateMsgList = new ArrayList<>();
    private List<String> updateImageUriList = new ArrayList<>();
    private UpdateContentPagerAdapter updateContentPagerAdapter;

    //isManualCheck 是否在关于中手动检查更新
    public UpgradeUtils(Context context, Handler handler, boolean isManualCheck) {
        this.context = context;
        this.handler = handler;
        this.isManualCheck = isManualCheck;
        loadingDlg = new LoadingDialog(context);
        handMessage();
        updateMsgList.add("测试条目测试测试测试测试测试测试测试学而思测测试测试测试测试测试测试测试123456");
        updateMsgList.add("测试条目2");
        updateMsgList.add("测试条目3");
        updateImageUriList.add("ceshitiaomu1");
        updateImageUriList.add("ceshitiaomu2");
        updateImageUriList.add("ceshitiaomu3");
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        upgradeHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case UPGRADE_FAIL:
                        if (handler != null) {
                            handler.sendEmptyMessage(UPGRADE_FAIL);
                        }
                        break;
                    case DOWNLOAD:
                        downloadPercent = getPercent(progress);
                        String text = setFormat(downloadSize) + "/"
                                + setFormat(totalSize);
                        if (ratioText != null) {
                            ratioText.setText(text);
                        }
                        if (downloadProgressBar != null) {
                            downloadProgressBar.setProgress(progress);
                        }
                        if (percentText != null) {
                            percentText.setText(downloadPercent);
                        }
                        break;
                    case DOWNLOAD_FINISH:
                        if (progressDownloadDialog != null && progressDownloadDialog.isShowing()) {
                            progressDownloadDialog.dismiss();
                        }
//                        AppUtils.installApk(context,DOWNLOAD_PATH, "update.apk");
                        FileUtils.openFile(context, DOWNLOAD_PATH + "update.apk");
                        if (context instanceof MainActivity) {
                            ((Activity) context).finish();
                        }
                        break;

                    case DOWNLOAD_FAIL:
                        if (progressDownloadDialog != null && progressDownloadDialog.isShowing()) {
                            progressDownloadDialog.dismiss();
                        }
                        ToastUtils.show(context,
                                context.getString(R.string.update_fail));
                        if (context != null) {
                            if (upgradeCode == 2) {
                                showForceUpgradeDlg();
                            } else {
                                showSelectUpgradeDlg();
                            }
                        }
                        break;
                    case SHOW_PEOGRESS_LAODING_DLG:
                        if (null != progressDownloadDialog) {
                            progressDownloadDialog.show();
                        }
                        break;

                    default:
                        break;
                }
            }

        };
    }

    public void checkUpdate(boolean isShowLoadingDlg) {
        if (NetUtils.isNetworkConnected(context, isShowLoadingDlg)) {
            loadingDlg.show(isShowLoadingDlg);
            AppAPIService apiService = new AppAPIService(context);
            apiService.setAPIInterface(UpgradeUtils.this);
            apiService.checkUpgrade(isManualCheck);
        } else if (handler != null) {
            handler.sendEmptyMessage(UPGRADE_FAIL);
        }
    }

    private void handleUpgrade() {
        // TODO Auto-generated method stub
        upgradeCode = getUpgradeResult.getUpgradeCode();
        upgradeMsg = getUpgradeResult.getUpgradeMsg();
        String changeLog = getUpgradeResult.getChangeLog();
        if (!StringUtils.isBlank(changeLog)) {
            upgradeMsg = changeLog;
        }
        upgradeUrl = getUpgradeResult.getUpgradeUrl();
        switch (upgradeCode) {
            case 0: // 无须升级
                if (handler != null) {
                    handler.sendEmptyMessage(NO_NEED_UPGRADE);
                }
                break;
            case 1: // 可选升级
                long appNotUpdateTime = PreferencesUtils.getLong(context, "appNotUpdateTime");
                if (isManualCheck || System.currentTimeMillis() - appNotUpdateTime > notUpdateInterval) {
                    showSelectUpgradeDlg();
                } else if (handler != null) {
                    handler.sendEmptyMessage(NO_NEED_UPGRADE);
                }

                break;
            case 2: // 必须升级
                showForceUpgradeDlg();
                break;

            default:
                if (handler != null) {
                    handler.sendEmptyMessage(NO_NEED_UPGRADE);
                }
                break;
        }
    }

    private void showSelectUpgradeDlg() {
        final boolean isDownloadLatestVersion = isDownloadedLatestVersion();
        final MyDialog dialog = new MyDialog(context,
                R.layout.basewidget_dialog_update);
        dialog.setCancelable(false);
        Button okBtn = dialog.findViewById(R.id.btn_update);   //tv_update_version
        final TextView updateTipTextView = dialog.findViewById(R.id.tv_wifi_download_hint);
        updateTipTextView.setVisibility(isDownloadLatestVersion ? View.VISIBLE : View.GONE);
        ViewPager viewPager = dialog.findViewById(R.id.viewpager_update_content);
        updateContentPagerAdapter = new UpdateContentPagerAdapter();
        viewPager.setAdapter(updateContentPagerAdapter);
        CircleIndicator commonCircleIndicator = dialog.findViewById(R.id.cc_index);
        commonCircleIndicator.setViewPager(viewPager);
        updateContentPagerAdapter.registerDataSetObserver(commonCircleIndicator.getDataSetObserver());
        String okBtnContent = isDownloadLatestVersion ? context.getString(R.string.upgrade_install_now) : context.getString(R.string.upgrade_download);
        okBtn.setText(okBtnContent + "(" + getUpgradeResult.getLatestVersion() + ")");
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        TextView cancelBt = dialog.findViewById(R.id.tv_update_state);
        cancelBt.setText(isDownloadLatestVersion ? context.getString(R.string.upgrade_install_next_time) : context.getString(R.string.upgrade_later));
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PreferencesUtils.putLong(context, "appNotUpdateTime", System.currentTimeMillis());
                if (handler != null) {
                    handler.sendEmptyMessage(DONOT_UPGRADE);
                }
            }
        });
        if (context != null) {
            dialog.show();
        }

    }

    private void showForceUpgradeDlg() {
        // TODO Auto-generated method stub
        final MyDialog dialog = new MyDialog(context,
                R.layout.basewidget_dialog_update);
        dialog.setCancelable(false);
        Button okBtn = dialog.findViewById(R.id.btn_update);
        okBtn.setText(context.getString(R.string.upgrade));
        ViewPager viewPager = dialog.findViewById(R.id.viewpager_update_content);
        updateContentPagerAdapter = new UpdateContentPagerAdapter();
        viewPager.setAdapter(updateContentPagerAdapter);
        CircleIndicator commonCircleIndicator = dialog.findViewById(R.id.cc_index);
        commonCircleIndicator.setViewPager(viewPager);
        updateContentPagerAdapter.registerDataSetObserver(commonCircleIndicator.getDataSetObserver());
        String okBtnContent = context.getString(R.string.upgrade) + "(" + getUpgradeResult.getLatestVersion() + ")";
        okBtn.setText(okBtnContent);
        okBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        TextView cancelBt = dialog.findViewById(R.id.tv_update_state);
        cancelBt.setText(context.getString(R.string.exit));
        cancelBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((MyApplication) context.getApplicationContext()).exit();
            }
        });
        if (context != null) {
            dialog.show();
        }
    }

    /**
     * 判断下载的是否为最新版本
     * 判别方式为Md5
     */
    private boolean isDownloadedLatestVersion() {
        String apkName = DOWNLOAD_PATH + "update.apk";
        File file = new File(apkName);
        if (file.exists()) {
            String currentApkMd5 = FileUtils.getFileMD5(file);
            String upgradeMd5 = getUpgradeResult.getApkMd5();
            if (StringUtils.isBlank(upgradeMd5)) {
                final PackageManager pm = context.getPackageManager();
                PackageInfo info = pm.getPackageArchiveInfo(apkName, 0);
                LogUtils.LbcDebug("info.versionCode" + info.versionCode);
                int currentVersionCode = 0;
                try {
                    PackageManager manager = context.getPackageManager();
                    PackageInfo currentInfo = manager.getPackageInfo(context.getPackageName(), 0);
                    currentVersionCode = currentInfo.versionCode;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (currentVersionCode < info.versionCode) {
                    return true;
                }
            } else {
                if (currentApkMd5.equals(upgradeMd5)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showDownloadDialog() {
        cancelUpdate = false;
        progressDownloadDialog = new MyDialog(context, R.layout.app_dialog_down_progress_one_button);
        progressDownloadDialog.setDimAmount(0.2f);
        progressDownloadDialog.setCancelable(false);
        progressDownloadDialog.setCanceledOnTouchOutside(false);
        ((TextView) progressDownloadDialog.findViewById(R.id.tv_permission_dialog_title)).
                setText(context.getString(R.string.cloud_plus_update, AppUtils.getAppName(context)));
        progressDownloadDialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDownloadDialog.dismiss();
                if (cancelable != null) {
                    cancelable.cancel();
                }
                // 设置取消状态
                cancelUpdate = true;
                if (upgradeCode == 2) { // 强制升级时
                    ((MyApplication) context.getApplicationContext()).exit();
                } else if (handler != null) {
                    handler.sendEmptyMessage(DONOT_UPGRADE);
                }
            }
        });
        percentText = progressDownloadDialog.findViewById(R.id.tv_num_percent);
        ratioText = progressDownloadDialog.findViewById(R.id.tv_num_progress);
        downloadProgressBar = progressDownloadDialog.findViewById(R.id.progress_bar_apk_download);
        if (context != null) {
            // 下载文件
            downloadApk();
        }
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        // 判断SD卡是否存在，并且是否具有读写权限
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            RequestParams params = new RequestParams(upgradeUrl);
            params.setSaveFilePath(DOWNLOAD_PATH + "update.apk");
            cancelable = x.http().get(params,
                    new Callback.ProgressCallback<File>() {

                        @Override
                        public void onCancelled(CancelledException arg0) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onError(Throwable arg0, boolean arg1) {
                            // TODO Auto-generated method stub
                            upgradeHandler.sendEmptyMessage(DOWNLOAD_FAIL);
                        }

                        @Override
                        public void onFinished() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onSuccess(File arg0) {
                            // TODO Auto-generated method stub
                            upgradeHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                        }

                        @Override
                        public void onLoading(long arg0, long arg1, boolean arg2) {
                            // TODO Auto-generated method stub
                            totalSize = arg0;
                            downloadSize = arg1;
                            progress = (int) (((float) arg1 / arg0) * 100);
                            // 更新进度
                            if (progressDownloadDialog != null
                                    && progressDownloadDialog.isShowing()) {
                                upgradeHandler.sendEmptyMessage(DOWNLOAD);
                            }
                        }

                        @Override
                        public void onStarted() {
                            // TODO Auto-generated method stub
                            upgradeHandler.sendEmptyMessage(SHOW_PEOGRESS_LAODING_DLG);
                        }

                        @Override
                        public void onWaiting() {
                            // TODO Auto-generated method stub

                        }
                    });
        } else {
            upgradeHandler.sendEmptyMessage(DOWNLOAD_FAIL);
        }

    }


    /**
     * 获取百分率
     **/
    private String getPercent(int progress) {
        // TODO Auto-generated method stub
        return progress + "%";
    }

    /**
     * 格式化数据
     **/
    private String setFormat(long data) {
        // TODO Auto-generated method stub
        if (data < KBDATA) {
            return data + "B";
        } else if (data < MBDATA) {
            return new DecimalFormat(("####0.00")).format(data / KBDATA) + "KB";
        } else {
            return new DecimalFormat(("####0.00")).format(data / MBDATA) + "MB";
        }
    }

    @Override
    public void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult, boolean isManualCheck) {
        // TODO Auto-generated method stub
        if (loadingDlg != null && loadingDlg.isShowing()) {
            loadingDlg.dismiss();
        }
        this.getUpgradeResult = getUpgradeResult;
        handleUpgrade();
    }

    @Override
    public void returnUpgradeFail(String error, boolean isManualCheck, int errorCode) {
        // TODO Auto-generated method stub
        if (loadingDlg != null && loadingDlg.isShowing()) {
            loadingDlg.dismiss();
        }
        upgradeHandler.sendEmptyMessage(UPGRADE_FAIL);
        //在未登陆之前，为防止还不知道升级服务所在的地址导致检测升级失败，不再弹出提示
//        WebServiceMiddleUtils.hand(context, error, errorCode);
//		WebServiceMiddleUtils.hand(context, error, upgradeHandler,
//				UPGRADE_FAIL);
    }

    /**
     * 更新内容
     */
    private class UpdateContentPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return updateMsgList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return o == view;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LogUtils.LbcDebug("22222222222222222222222");
            View rootView = View.inflate(context, R.layout.basewiget_update_content_viewpager_item, null);
            ImageView updateImageView = rootView.findViewById(R.id.iv_update_content);
            TextView versionCodeText = rootView.findViewById(R.id.tv_update_version);
            TextView updateContentText = rootView.findViewById(R.id.tv_update_content_data);
            ImageDisplayUtils.getInstance().displayImage(updateImageView, updateImageUriList.get(position), R.drawable.ic_update_default);
            versionCodeText.setText(getUpgradeResult.getLatestVersion());
            updateContentText.setText(updateMsgList.get(position));
            container.addView(rootView);
            return rootView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            LogUtils.LbcDebug("333333333333333333");
            container.removeView((View) object);
        }
    }

}
