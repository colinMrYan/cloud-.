package com.inspur.emmcloud.ui.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.GetUpgradeResult;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.WeakHandler;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.DecimalFormat;


/**
 * Created by Administrator on 2017/5/10.
 */

public class AppUpgradeNotifyActivity extends BaseActivity {

	protected static final int SHOW_PEOGRESS_LAODING_DLG = 0;
	private static final int DOWNLOADING = 3;
	private static final int DOWNLOAD_FINISH = 4;
	private static final int DOWNLOAD_FAIL = 5;
	private static final double MBDATA = 1048576.0;
	private static final double KBDATA = 1024.0;
	private boolean cancelUpdate = false;
	private GetUpgradeResult getUpgradeResult;
	private long totalSize;
	private long downloadSize;
	/** 记录进度条数量* */
	private int progress;
	private MyDialog downloadProgressDlg;
	private TextView ratioText;
	private Cancelable cancelable;
	private WeakHandler handler;
	private String downloadPercent;
	private String upgradeMsg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StateBarColor.changeStateBarColor(this, R.color.white);
		setContentView(R.layout.activity_app_upgrade_notify);
		getUpgradeResult = (GetUpgradeResult)getIntent().getSerializableExtra("getUpgradeResult");
		int updateType = getUpgradeResult.getUpgradeCode();
		if (updateType == 1) {
			upgradeMsg = getUpgradeResult.getUpgradeMsg();
			String changeLog = getUpgradeResult.getChangeLog();
			if (!StringUtils.isBlank(changeLog)) {
				upgradeMsg = changeLog;
			}
			showSelectUpgradeDlg();
		} else {
			showForceUpgradeDlg();
		}
		handMessage();
	}

	private void handMessage() {
		// TODO Auto-generated method stub
		handler = new WeakHandler(AppUpgradeNotifyActivity.this) {
			@Override
			protected void handleMessage(Object o, Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
					case DOWNLOADING:
						downloadPercent = getPercent(progress);
						String text = downloadPercent + "," + "  "
								+ setFormat(downloadSize) + "/"
								+ setFormat(totalSize);
						ratioText.setText(text);
						break;

					case DOWNLOAD_FINISH:
						if(downloadProgressDlg != null && downloadProgressDlg.isShowing()){
							downloadProgressDlg.dismiss();
						}
						installApk();
						break;

					case DOWNLOAD_FAIL:
						ToastUtils.show(AppUpgradeNotifyActivity.this,
								getString(R.string.update_fail));
							if (getUpgradeResult.getUpgradeCode() == 2) {
								showForceUpgradeDlg();
							} else {
								showSelectUpgradeDlg();
							}
						break;
					case SHOW_PEOGRESS_LAODING_DLG:
						downloadProgressDlg.show();
						break;

					default:
						break;
				}
			}

		};
	}

	private void showSelectUpgradeDlg() {
		// TODO Auto-generated method stub
		final MyDialog dialog = new MyDialog(this,
				R.layout.dialog_two_buttons);
		dialog.setCancelable(false);
		Button okBt = (Button) dialog.findViewById(R.id.ok_btn);
		okBt.setText(getString(R.string.upgrade));
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(upgradeMsg);
		TextView appUpdateTitle = (TextView) dialog.findViewById(R.id.app_update_title);
		TextView appUpdateVersion = (TextView) dialog.findViewById(R.id.app_update_version);
		appUpdateTitle.setText(getString(R.string.app_update_remind));
		appUpdateVersion.setText(getString(R.string.app_last_version) + "(" + getUpgradeResult.getLatestVersion() + ")");
		okBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				showDownloadDialog();
			}

		});
		Button cancelBt = (Button) dialog.findViewById(R.id.cancel_btn);
		cancelBt.setText(getString(R.string.not_upgrade));
		cancelBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PreferencesUtils.putLong(getApplicationContext(),"appNotUpdateTime",System.currentTimeMillis());
				dialog.dismiss();
				finish();
			}
		});
		dialog.show();

	}

	private void showForceUpgradeDlg() {
		// TODO Auto-generated method stub
		final MyDialog dialog = new MyDialog(this,
				R.layout.dialog_two_buttons);
		dialog.setCancelable(false);
		Button okBt = (Button) dialog.findViewById(R.id.ok_btn);
		okBt.setText(getString(R.string.upgrade));
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(upgradeMsg);
		okBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				showDownloadDialog();
			}
		});
		Button cancelBt = (Button) dialog.findViewById(R.id.cancel_btn);
		cancelBt.setText(getString(R.string.exit));
		cancelBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				finish();
				((MyApplication) getApplicationContext()).exit();
			}
		});
			dialog.show();
	}

	private void showDownloadDialog() {
		// TODO Auto-generated method stub
		cancelUpdate = false;
		downloadProgressDlg = new MyDialog(this,
				R.layout.dialog_app_update_progress);
		downloadProgressDlg.setCancelable(false);
		ratioText = (TextView) downloadProgressDlg.findViewById(R.id.ratio_text);
		Button cancelBt = (Button) downloadProgressDlg.findViewById(R.id.cancel_bt);
		cancelBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				downloadProgressDlg.dismiss();
				finish();
				if (cancelable != null) {
					cancelable.cancel();
				}
				// 设置取消状态
				cancelUpdate = true;
				if (getUpgradeResult.getUpgradeCode() == 2) { // 强制升级时
					((MyApplication)getApplicationContext()).exit();
				}
			}
		});
			downloadApk();
	}

	/**
	 * 下载apk文件
	 */
	private void downloadApk() {

		// 判断SD卡是否存在，并且是否具有读写权限
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			RequestParams params = new RequestParams(getUpgradeResult.getUpgradeUrl());
			params.setSaveFilePath(MyAppConfig.LOCAL_DOWNLOAD_PATH + "update.apk");
			cancelable = x.http().get(params,
					new Callback.ProgressCallback<File>() {
						@Override
						public void onCancelled(CancelledException arg0) {
							// TODO Auto-generated method stub
						}

						@Override
						public void onError(Throwable arg0, boolean arg1) {
							// TODO Auto-generated method stub
							handler.sendEmptyMessage(DOWNLOAD_FAIL);
						}

						@Override
						public void onFinished() {
							// TODO Auto-generated method stub

						}

						@Override
						public void onSuccess(File arg0) {
							// TODO Auto-generated method stub
							handler.sendEmptyMessage(DOWNLOAD_FINISH);
						}

						@Override
						public void onLoading(long arg0, long arg1, boolean arg2) {
							// TODO Auto-generated method stub
							LogUtils.debug("jason", "onLoading");
							totalSize = arg0;
							downloadSize = arg1;
							progress = (int) (((float) arg1 / arg0) * 100);
							// 更新进度
							if (downloadProgressDlg != null
									&& downloadProgressDlg.isShowing()) {
								handler.sendEmptyMessage(DOWNLOADING);
							}
						}

						@Override
						public void onStarted() {
							// TODO Auto-generated method stub
							handler.sendEmptyMessage(SHOW_PEOGRESS_LAODING_DLG);
						}

						@Override
						public void onWaiting() {
							// TODO Auto-generated method stub

						}
					});
		} else {
			handler.sendEmptyMessage(DOWNLOAD_FAIL);
		}

	}

	/**
	 * 安装APK文件
	 */
	public void installApk() {
		File apkfile = new File(MyAppConfig.LOCAL_DOWNLOAD_PATH, "update.apk");
		if (!apkfile.exists()) {
			ToastUtils.show(this, R.string.update_fail);
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		// 更新后启动
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		startActivity(i);
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


}
