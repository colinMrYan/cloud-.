package com.inspur.emmcloud.ui.app.volume;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeRecentUseAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.Volume.GetVolumeListResult;
import com.inspur.emmcloud.bean.Volume.Volume;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.plugin.file.FileUtil;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

import static com.inspur.emmcloud.R.id.volume_capacity_text;


/**
 * 云盘首页
 */

@ContentView(R.layout.activity_volume_homepage)
public class VolumeHomePageActivity extends BaseActivity {

    @ViewInject(volume_capacity_text)
    private TextView volumeCapacityText;

    @ViewInject(R.id.volume_recent_use_list)
    private ListView volumeRecentUseListView;

    @ViewInject(R.id.volume_recent_use_layout)
    private LinearLayout volumeRecentUseLayout;

    private VolumeRecentUseAdapter adapter;
    private MyAppAPIService apiService;
    private LoadingDialog loadingDlg;

    private Volume myVolume;
    private List<Volume> shareVolumeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        getVolumeList();
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        adapter = new VolumeRecentUseAdapter(this);
        volumeRecentUseListView.setAdapter(adapter);
    }

    public void onClick(View v) {
        Bundle bundle;
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.option_img:
                break;
            case R.id.my_file_layout:
                if (myVolume != null){
                    bundle = new Bundle();
                    bundle.putSerializable("volume", myVolume);
                    bundle.putSerializable("title", "我的文件");
                    IntentUtils.startActivity(VolumeHomePageActivity.this, VolumeFileActivity.class,bundle);
                }
                break;
            case R.id.share_volume_layout:
                break;
            case R.id.enterprise_file_layout:
                break;
            default:
                break;
        }
    }

    /**
     * 获取云盘列表
     */
    private void getVolumeList() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.getVolumeList();
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }

            shareVolumeList = getVolumeListResult.getShareVolumeList();
            myVolume = getVolumeListResult.getMyVolume();
            if (myVolume != null){
                String volumeUsedSize = FileUtil.formetFileSizeMinM(myVolume.getUserdSize());
                String volumeMaxSize = FileUtil.formetFileSizeMinM(myVolume.getMaxSize());
                volumeCapacityText.setText(volumeUsedSize+" / "+volumeMaxSize);
            }
        }

        @Override
        public void returnVolumeListFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }

}
