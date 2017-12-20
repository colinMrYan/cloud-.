package com.inspur.emmcloud.ui.appcenter.volume;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeRecentUseAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeListResult;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
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
public class VolumeHomePageActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    @ViewInject(volume_capacity_text)
    private TextView volumeCapacityText;

    @ViewInject(R.id.volume_recent_use_list)
    private ListView volumeRecentUseListView;

    @ViewInject(R.id.volume_recent_use_layout)
    private LinearLayout volumeRecentUseLayout;

    @ViewInject(R.id.refresh_layout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    private VolumeRecentUseAdapter adapter;
    private MyAppAPIService apiService;
    private LoadingDialog loadingDlg;

    private Volume myVolume;
    private List<Volume> shareVolumeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        getVolumeList(true);
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        adapter = new VolumeRecentUseAdapter(this);
        volumeRecentUseListView.setAdapter(adapter);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.header_bg), ContextCompat.getColor(getApplicationContext(), R.color.header_bg));
        swipeRefreshLayout.setOnRefreshListener(this);
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

    @Override
    public void onRefresh() {
        getVolumeList(false);
    }

    /**
     * 获取云盘列表
     */
    private void getVolumeList(boolean isShowDlg) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show(isShowDlg);
            apiService.getVolumeList();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            swipeRefreshLayout.setRefreshing(false);
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
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }

}
