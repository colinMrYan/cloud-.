package com.inspur.emmcloud.ui.app;

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
import com.inspur.emmcloud.api.apiservice.ReactNativeAPIService;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.GetAddAppResult;
import com.inspur.emmcloud.util.AppCenterNativeAppUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.greenrobot.eventbus.EventBus;

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
    private ImageDisplayUtils imageDisplayUtils;
    private LoadingDialog loadingDlg;
    private MyAppAPIService apiService;
    private ReactNativeAPIService reactNativeApiService;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);
        imageDisplayUtils = new ImageDisplayUtils(R.drawable.icon_empty_icon);
        loadingDlg = new LoadingDialog(AppDetailActivity.this);
        String appId = ((App) getIntent().getExtras().getSerializable("app")).getAppID();
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        reactNativeApiService = new ReactNativeAPIService(AppDetailActivity.this);
        reactNativeApiService.setAPIInterface(new WebService());
        getAppInfoById(appId);
    }

    private void initView(final App app) {
        // TODO Auto-generated method stub
        appIconImg = (ImageView) findViewById(R.id.app_icon_img);
        imageDisplayUtils.displayImage(appIconImg, app.getAppIcon());
        statusBtn = (Button) findViewById(R.id.app_status_btn);
        intrImgListView = (RecyclerView) findViewById(R.id.intr_app_img_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppDetailActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        intrImgListView.setLayoutManager(linearLayoutManager);
        AppDetailImageAdapter appDetailImageAdapter = new AppDetailImageAdapter(AppDetailActivity.this,app.getLegends());
        appDetailImageAdapter.setOnRecommandItemClickListener(new OnAppDetailImageItemClickListener() {
            @Override
            public void onAppDetailImageItemClick(View view, int position) {
                Intent intent = new Intent();
					intent.setClass(getApplicationContext(), AppImgDisPlayActivity.class);
					intent.putExtra("currentIndex", position);
                    intent.putStringArrayListExtra("legends", (ArrayList<String>) app.getLegends());
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
        private List<String> legends;
        private ImageDisplayUtils imageDisplayUtils;
        public AppDetailImageAdapter(Context context,List<String> legends) {
            inflater = LayoutInflater.from(context);
            imageDisplayUtils = new ImageDisplayUtils();
            this.legends = legends;
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
            imageDisplayUtils.displayImage(holder.appDetailImg,legends.get(position));
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
            return legends.size();
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
            loadingDlg.show();
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
        if (app.getUseStatus() == 0) {
            addApp(statusBtn, app.getAppID());
        } else if (app.getUseStatus() == 1) {
            EventBus.getDefault().post(app);
            if (app.getAppType() == 2) {
                new AppCenterNativeAppUtils().InstallOrOpen(AppDetailActivity.this, app);
            } else {
                UriUtils.openApp(AppDetailActivity.this, app);
            }
        }
    }

    /**
     * 封装网络请求的方法
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
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            AppDetailActivity.this.app = app;
            initView(app);
        }

        @Override
        public void returnAppInfoFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            app = (App) getIntent().getExtras().getSerializable("app");
            WebServiceMiddleUtils.hand(AppDetailActivity.this, error, errorCode);
        }
    }

}
