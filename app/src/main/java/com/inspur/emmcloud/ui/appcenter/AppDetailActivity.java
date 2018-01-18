package com.inspur.emmcloud.ui.appcenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.GetAddAppResult;
import com.inspur.emmcloud.ui.chat.ImagePagerActivity;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppCenterNativeAppUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用详情界面
 *
 * @author Administrator
 */
public class AppDetailActivity extends BaseActivity {
    private static final String ACTION_NAME = "add_app";
    private ImageView appIconImg;
    private Button statusBtn;
    private RecyclerView intrImgListView;
    private LoadingDialog loadingDlg;
    private MyAppAPIService apiService;
    private App app;
    private long lastOnItemClickTime = 0;//防止多次点击

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);
        loadingDlg = new LoadingDialog(AppDetailActivity.this);
        app = ((App) getIntent().getExtras().getSerializable("app"));
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        initView();
        getAppInfoById(app.getAppID());
    }

    private void initView() {
        appIconImg = (ImageView) findViewById(R.id.app_icon_img);
        ImageDisplayUtils.getInstance().displayImage(appIconImg, app.getAppIcon(), R.drawable.ic_app_default);
        statusBtn = (Button) findViewById(R.id.app_status_btn);
        intrImgListView = (RecyclerView) findViewById(R.id.intr_app_img_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppDetailActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        intrImgListView.setLayoutManager(linearLayoutManager);
        AppDetailImageAdapter appDetailImageAdapter = new AppDetailImageAdapter(AppDetailActivity.this, app.getLegendList());
        appDetailImageAdapter.setOnRecommandItemClickListener(new OnAppDetailImageItemClickListener() {
            @Override
            public void onAppDetailImageItemClick(View view, int position) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), ImagePagerActivity.class);
                intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
                intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, (ArrayList<String>) app.getLegendList());
                startActivity(intent);
            }
        });
        intrImgListView.setAdapter(appDetailImageAdapter);
        ((TextView) findViewById(R.id.name_text)).setText(app.getAppName());
        ((TextView) findViewById(R.id.profile_text)).setText(app.getNote());
        if (!StringUtils.isBlank(app.getDescription())) {
            ((TextView) findViewById(R.id.intr_text)).setText(app
                    .getDescription());
        }
        showAppStatusBtn(app);
    }

    /**
     * 推荐应用的Adapter
     */
    public class AppDetailImageAdapter extends RecyclerView.Adapter<AppDetailImageAdapter.AppDetailImageViewHolder> {
        private LayoutInflater inflater;
        private OnAppDetailImageItemClickListener onAppDetailImageItemClickListener;
        private List<String> legendList;

        public AppDetailImageAdapter(Context context, List<String> legendList) {
            inflater = LayoutInflater.from(context);
            this.legendList = legendList;
        }

        @Override
        public AppDetailImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.app_intr_img_view, null);
            AppDetailImageViewHolder viewHolder = new AppDetailImageViewHolder(view);
            viewHolder.appDetailImg = (ImageView) view.findViewById(R.id.detail_img);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final AppDetailImageViewHolder holder, final int position) {
            ImageDisplayUtils.getInstance().displayImage(holder.appDetailImg, legendList.get(position), R.drawable.ic_app_default);
            if (onAppDetailImageItemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAppDetailImageItemClickListener.onAppDetailImageItemClick(holder.itemView, position);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return legendList.size();
        }

        public void setOnRecommandItemClickListener(OnAppDetailImageItemClickListener l) {
            this.onAppDetailImageItemClickListener = l;
        }

        public class AppDetailImageViewHolder extends RecyclerView.ViewHolder {
            ImageView appDetailImg;

            public AppDetailImageViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    public interface OnAppDetailImageItemClickListener {
        void onAppDetailImageItemClick(View view, int position);
    }

    /**
     * 根据id获取app详情
     */
    private void getAppInfoById(String appId) {
        if (NetUtils.isNetworkConnected(AppDetailActivity.this)) {
            apiService.getAppInfo(appId);
        }
    }

    /**
     * 显示app状态的按钮
     */
    private void showAppStatusBtn(App app) {
        if (app.getUseStatus() == 1) {
            statusBtn.setText(getString(R.string.open));
        } else if (app.getUseStatus() == 0) {
            statusBtn.setText(getString(R.string.add));
        } else {
            statusBtn.setText(getString(R.string.update));
        }
    }

    public void onClick(View v) {
        if (!isFastDoubleClick()){
            if (app.getUseStatus() == 0) {
                addApp(statusBtn, app.getAppID());
            } else if (app.getUseStatus() == 1) {
                if (app.getAppType() == 2) {
                    new AppCenterNativeAppUtils().InstallOrOpen(AppDetailActivity.this, app);
                } else {
                    UriUtils.openApp(AppDetailActivity.this, app);
                }
            }
        }
    }

    /**
     * 判断是否连点
     * @return
     */
    private boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastOnItemClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastOnItemClickTime = time;
        return false;

    }

    /**
     * 封装网络请求的方法
     *
     * @param statusBtn
     * @param appID
     */
    private void addApp(Button statusBtn, String appID) {
        if (NetUtils.isNetworkConnected(AppDetailActivity.this)) {
            statusBtn.setText(getString(R.string.adding));
            loadingDlg.show();
            apiService.addApp(appID);
        }
    }

    public void onBack(View v) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class WebService extends APIInterfaceInstance {
        @Override
        public void returnAddAppSuccess(GetAddAppResult getAddAppResult) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            statusBtn.setText(getString(R.string.open));
            app.setUseStatus(1);
            Intent mIntent = new Intent(ACTION_NAME);
            mIntent.putExtra("app", app);
            // 发送广播
            sendBroadcast(mIntent);
        }

        @Override
        public void returnAddAppFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            statusBtn.setText(getString(R.string.download));
            WebServiceMiddleUtils.hand(AppDetailActivity.this,
                    error, errorCode);
        }

        @Override
        public void returnAppInfoSuccess(App app) {
            AppDetailActivity.this.app = app;
            initView();
        }

        @Override
        public void returnAppInfoFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(AppDetailActivity.this, error, errorCode);
        }
    }

}
