package com.inspur.emmcloud.ui.appcenter.webex;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.WebexAPIService;
import com.inspur.emmcloud.bean.appcenter.webex.GetWebexTKResult;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.net.URLEncoder;
import java.util.Calendar;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_detail)
public class WebexMeetingDetailActivity extends BaseActivity {

    public static final String EXTRA_WEBEXMEETING = "WebexMeeting";
    private final String webexAppPackageName = "com.cisco.webex.meetings";
    @ViewInject(R.id.bt_function)
    private Button functionBtn;
    @ViewInject(R.id.iv_photo)
    private ImageView photoImg;
    @ViewInject(R.id.tv_title)
    private TextView titleText;
    @ViewInject(R.id.tv_owner)
    private TextView ownerText;
    @ViewInject(R.id.tv_time)
    private TextView timeText;
    @ViewInject(R.id.tv_duration)
    private TextView durationText;
    @ViewInject(R.id.tv_meeting_id)
    private TextView meetingIdText;
    @ViewInject(R.id.tv_meeting_password)
    private TextView meetingPasswordText;
    @ViewInject(R.id.rl_host_key)
    private RelativeLayout hostKeyLayout;
    @ViewInject(R.id.tv_host_key)
    private TextView hostKeyText;
    @ViewInject(R.id.iv_delete)
    private ImageView deleteImg;

    private WebexMeeting webexMeeting;
    private boolean isOwner = false;
    private LoadingDialog loadingDialog;
    private WebexAPIService apiService;
    private static final int SHOW_PEOGRESS_LAODING_DLG = 0;
    private static final int DOWNLOAD = 3;
    private static final int DOWNLOAD_FINISH = 4;
    private static final int DOWNLOAD_FAIL = 5;
    private long totalSize;
    private long downloadSize;
    private int progressSize;
    private MyDialog downloadingDialog;
    private TextView progressTv;
    private Callback.Cancelable cancelableDownloadRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(this);
        webexMeeting = (WebexMeeting) getIntent().getSerializableExtra(EXTRA_WEBEXMEETING);
        apiService = new WebexAPIService(this);
        apiService.setAPIInterface(new WebService());
        getWebexMeeting();
    }

    private void showWebexMeetingDetial() {
        titleText.setText(webexMeeting.getConfName());
        Calendar startCalendar = webexMeeting.getStartDateCalendar();
        String timeYDM = TimeUtils.Calendar2TimeString(startCalendar, TimeUtils.getFormat(MyApplication.getInstance(), TimeUtils.FORMAT_YEAR_MONTH_DAY));
        String week = TimeUtils.getWeekDay(MyApplication.getInstance(), startCalendar);
        String timeHrMin = TimeUtils.Calendar2TimeString(startCalendar, TimeUtils.getFormat(MyApplication.getInstance(), TimeUtils.FORMAT_HOUR_MINUTE));
        timeText.setText(timeYDM + " " + week + " " + timeHrMin);
        int duration = webexMeeting.getDuration();
        int hour = duration / 60;
        int min = duration % 60;
        String hourtext = (hour == 0) ? "" : hour + getString(R.string.hour);
        String mintext = (min == 0) ? "" : min + getString(R.string.min);
        durationText.setText("(" + hourtext + mintext + ")");
        meetingPasswordText.setText(webexMeeting.getMeetingPassword());
        meetingIdText.setText(webexMeeting.getMeetingID());
        String photoUrl = APIUri.getWebexPhotoUrl(webexMeeting.getHostWebExID());
        ImageDisplayUtils.getInstance().displayImage(photoImg, photoUrl, R.drawable.icon_person_default);
        String myInfo = PreferencesUtils.getString(this, "myInfo", "");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        String myEmail = getMyInfoResult.getMail();
        isOwner = webexMeeting.getHostWebExID().equals(myEmail);
        deleteImg.setVisibility(isOwner ? View.VISIBLE : View.INVISIBLE);
        ownerText.setText(isOwner ? getString(R.string.mine) : webexMeeting.getHostUserName());
        functionBtn.setText(isOwner ? getString(R.string.webex_start) : getString(R.string.join));
        hostKeyLayout.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        hostKeyText.setText(webexMeeting.getHostKey());
        boolean isMeetingEnd = isMeetingEnd();
        functionBtn.setEnabled(!isMeetingEnd);
        functionBtn.setTextColor(isMeetingEnd ? Color.parseColor("#999999") : Color.parseColor("#ffffff"));
        functionBtn.setBackgroundColor(isMeetingEnd ? Color.parseColor("#D7D7D7") : Color.parseColor("#0F7BCA"));
        functionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppUtils.isAppInstalled(MyApplication.getInstance(), webexAppPackageName)) {
                    if (!isMeetingEnd()) {
                        if (isOwner) {
                            getWebexTK();
                        } else {
                            joinWebexMeeting();
                        }
                    } else {
                        functionBtn.setEnabled(false);
                        functionBtn.setTextColor(Color.parseColor("#999999"));
                        functionBtn.setBackgroundColor(Color.parseColor("#D7D7D7"));
                        ToastUtils.show(MyApplication.getInstance(), R.string.webex_meeting_ended);
                    }
                } else {
                    showInstallDialog();
                }


            }
        });

    }

    private boolean isMeetingEnd() {
        return webexMeeting.getStartDateCalendar().getTimeInMillis() + webexMeeting.getDuration() * 60000 <= System.currentTimeMillis();
    }

    /**
     * 安装提示
     */
    private void showInstallDialog() {
        new MyQMUIDialog.MessageDialogBuilder(WebexMeetingDetailActivity.this)
                .setMessage(getString(R.string.webex_install_tips))
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        String downloadUrl = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_WEBEX_DOWNLOAD_URL,"");
                        showDownloadDialog(downloadUrl);
                    }
                })
                .show();
    }

    /**
     * 展示下载Dialog
     *
     * @param appUrl
     */
    private void showDownloadDialog(String appUrl) {
        downloadingDialog = new MyDialog(WebexMeetingDetailActivity.this, R.layout.dialog_app_update_progress);
        downloadingDialog.setCancelable(false);
        progressTv = (TextView) downloadingDialog.findViewById(R.id.ratio_text);
        Button cancelBtn = (Button) downloadingDialog.findViewById(R.id.cancel_bt);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancelableDownloadRequest != null) {
                    cancelableDownloadRequest.cancel();
                }
                if (downloadingDialog != null && downloadingDialog.isShowing()) {
                    downloadingDialog.dismiss();
                }
            }
        });
        // 下载apk文件
        downloadWeBexApk(appUrl);
    }

    Handler downloadWeBexHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD:
                    if (progressTv != null) {
                        progressTv.setText(progressSize + "%," + "  " + AppUtils.getKBOrMBFormatString(downloadSize) + "/"
                                + AppUtils.getKBOrMBFormatString(totalSize));
                    }
                    break;
                case DOWNLOAD_FINISH:
                    if (downloadingDialog != null && downloadingDialog.isShowing()) {
                        downloadingDialog.dismiss();
                    }
                    AppUtils.installApk(MyApplication.getInstance(), MyAppConfig.LOCAL_DOWNLOAD_PATH, "webex.apk");
                    break;
                case DOWNLOAD_FAIL:
                    if (downloadingDialog != null && downloadingDialog.isShowing()) {
                        downloadingDialog.dismiss();
                    }
                    ToastUtils.show(MyApplication.getInstance(), getString(R.string.download_fail));
                    break;
                case SHOW_PEOGRESS_LAODING_DLG:
                    if (downloadingDialog != null && !downloadingDialog.isShowing()) {
                        downloadingDialog.show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 下载安装包
     */
    private void downloadWeBexApk(String appUrl) {
        // 判断SD卡是否存在，并且是否具有读写权限
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            RequestParams params = new RequestParams(appUrl);
            params.setSaveFilePath(MyAppConfig.LOCAL_DOWNLOAD_PATH + "webex.apk");
            cancelableDownloadRequest = x.http().get(params,
                    new Callback.ProgressCallback<File>() {

                        @Override
                        public void onCancelled(CancelledException arg0) {
                        }

                        @Override
                        public void onError(Throwable arg0, boolean arg1) {
                            sendCallBackMessage(DOWNLOAD_FAIL);
                        }

                        @Override
                        public void onFinished() {
                        }

                        @Override
                        public void onSuccess(File arg0) {
                            sendCallBackMessage(DOWNLOAD_FINISH);
                        }

                        @Override
                        public void onLoading(long arg0, long arg1, boolean arg2) {
                            totalSize = arg0;
                            downloadSize = arg1;
                            progressSize = (int) (((float) arg1 / arg0) * 100);
                            // 更新进度
                            if (downloadingDialog != null && downloadingDialog.isShowing()) {
                                sendCallBackMessage(DOWNLOAD);
                            }
                        }

                        @Override
                        public void onStarted() {
                            sendCallBackMessage(SHOW_PEOGRESS_LAODING_DLG);
                        }

                        @Override
                        public void onWaiting() {
                        }
                    });
        } else {
            sendCallBackMessage(DOWNLOAD_FAIL);
        }
    }

    /**
     * 发送回调消息给主线程
     *
     * @param downloadState
     */
    private void sendCallBackMessage(int downloadState) {
        if (downloadWeBexHandler != null) {
            downloadWeBexHandler.sendEmptyMessage(downloadState);
        }
    }


    private void joinWebexMeeting() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("wbx://meeting"));
            intent.putExtra("MK", webexMeeting.getMeetingID());
            intent.putExtra("MPW", "123123");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startWebexMeeting(String tk) {
        try {
            String sessionTicket = URLEncoder.encode(tk, "UTF-8");
            String webexID = URLEncoder.encode(webexMeeting.getHostWebExID(), "UTF-8");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            String uri = "wbx://meeting/inspurcloud.webex.com.cn/inspurcloud?MK=" + webexMeeting.getMeetingID() + "&MPW=" + webexMeeting.getMeetingPassword() + "&MTGTK=&sitetype=TRAIN&r2sec=1&UN=" + webexID + "&TK=" + sessionTicket;
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.bt_function:
                break;
            case R.id.iv_delete:
                showDeleteMeetingWarningDlg();

                break;
        }
    }

    private void showDeleteMeetingWarningDlg() {
        new MyQMUIDialog.MessageDialogBuilder(WebexMeetingDetailActivity.this)
                .setTitle(getString(R.string.remove_webex_meeting_warning_title))
                .setMessage(getString(R.string.remove_webex_meeting_warning_info))
                .addAction(getString(R.string.cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        removeWebexMeeting();
                    }
                })
                .show();
    }

    private void getWebexMeeting() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDialog.show();
            apiService.getWebexMeeting(webexMeeting.getMeetingID());
        }
    }

    private void getWebexTK() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDialog.show();
            apiService.getWebexTK();
        }
    }

    private void removeWebexMeeting() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDialog.show();
            apiService.removeMeeting(webexMeeting.getMeetingID());
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnWebexMeetingSuccess(WebexMeeting webexMeeting) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebexMeetingDetailActivity.this.webexMeeting = webexMeeting;
            showWebexMeetingDetial();
        }

        @Override
        public void returnWebexMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            finish();
        }

        @Override
        public void returnWebexTKSuccess(GetWebexTKResult getWebexTKResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            String tk = getWebexTKResult.getTk();
            startWebexMeeting(tk);
        }

        @Override
        public void returnWebexTKFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }

        @Override
        public void returnRemoveWebexMeetingSuccess() {
            LoadingDialog.dimissDlg(loadingDialog);
            Intent intent = new Intent();
            intent.putExtra(EXTRA_WEBEXMEETING, webexMeeting);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void returnRemoveWebexMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }
    }
}
