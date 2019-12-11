package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;

/**
 * 云盘文件分类展示
 */

public class VolumeFileFilterActvity extends VolumeFileBaseActivity {

    private BroadcastReceiver broadcastReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        uploadFileBtn.setVisibility(View.GONE);
        headerOperationLayout.setVisibility(View.INVISIBLE);
        adapter.setShowFileOperationSelcteImage(false);
        setListIemClick();
        registerReceiver();
    }

    private void setListIemClick() {
        adapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                downloadOrOpenVolumeFile(volumeFile);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                showFileOperationDlg(volumeFileList.get(position));
            }

            @Override
            public void onSelectedItemClick(View view, int position) {

            }
        });

    }

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String directoryId = intent.getStringExtra("directoryId");
                if (directoryId != null && directoryId.equals(getVolumeFileListResult.getId())) {
                    String command = intent.getStringExtra("command");
                    if (command != null && command.equals("refresh")) {
                        getVolumeFileList(true);
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter("broadcast_volume");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finishActivity();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    /**
     * 分类展示返回时,前一个页面进行数据刷新，以便于更新在此页面进行操作的数据
     */
    private void finishActivity() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        super.onDestroy();
    }
}
