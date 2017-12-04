package com.inspur.emmcloud.ui.app.volume;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.bean.Volume.VolumeFile;

import org.xutils.view.annotation.ViewInject;

/**
 * 云盘文件分类展示
 */

public class VolumeFileFilterActvity extends VolumeFileBaseActivity {

    @ViewInject(R.id.header_operation_layout)
    private RelativeLayout headerOperationLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        headerOperationLayout.setVisibility(View.INVISIBLE);
        setListIemClick();
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
        });
        adapter.setItemDropDownImgClickListener(new VolumeFileAdapter.MyItemDropDownImgClickListener() {
            @Override
            public void onItemDropDownImgClick(View view, int position) {
                showFileOperationDlg(volumeFileList.get(position));
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finishActivity();
                break;
            case R.id.refresh_btn:
                getVolumeFileList(true);
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

    private void finishActivity(){
        finish();
        setResult(RESULT_OK);
    }
}
