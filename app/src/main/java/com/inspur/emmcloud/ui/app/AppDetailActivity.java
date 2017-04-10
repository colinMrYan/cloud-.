package com.inspur.emmcloud.ui.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.api.apiservice.ReactNativeAPIService;
import com.inspur.emmcloud.bean.AndroidBundleBean;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.GetAddAppResult;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.bean.ReactNativeDownloadUrlBean;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesByUserUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.HorizontalListView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.reactnative.ReactNativeFlow;

import org.xutils.common.Callback;

import java.io.File;
import java.util.List;

/**
 * 应用详情界面
 *
 * @author Administrator
 */
public class AppDetailActivity extends BaseActivity {

    private static final int ADD_APP_FAIL = 3;
    private static final String ACTION_NAME = "add_app";
    private ImageView appIconImg;
    private Button statusBtn;
    private HorizontalListView intrImgListView;
    private ImageDisplayUtils imageDisplayUtils;
    private LoadingDialog loadingDlg;
    private MyAppAPIService apiService;
    private ReactNativeAPIService reactNativeApiService;
    private App app;
    private String reactAppFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);
        ((MyApplication) getApplicationContext()).addActivity(this);
        imageDisplayUtils = new ImageDisplayUtils(getApplicationContext(), R.drawable.icon_empty_icon);
        app = (App) getIntent().getExtras().getSerializable("app");
        initView();

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.recommand_tab_text:
//                viewPager.setCurrentItem(0);
                changeFoucus(0);
                break;
            case R.id.class_tab_text:
//                viewPager.setCurrentItem(1);
                changeFoucus(1);
                break;

            default:
                break;
        }
    }

    /**
     * 改变详情和评论的选中状态
     * @param arg0
     */
    private void changeFoucus(int arg0) {
        int recommandTabTextColor = arg0 == 0 ? Color.parseColor("#4990E2")
                : Color.parseColor("#999999");
        int classTabTextColor = arg0 == 1 ? Color.parseColor("#4990E2")
                : Color.parseColor("#999999");
        int recommandTabFooterViewVisible = arg0 == 0 ? View.VISIBLE
                : View.INVISIBLE;
        int classTabFooterViewVisible = arg0 == 1 ? View.VISIBLE
                : View.INVISIBLE;
        ((TextView) findViewById(R.id.recommand_tab_text))
                .setTextColor(recommandTabTextColor);
        ((TextView) findViewById(R.id.class_tab_text))
                .setTextColor(classTabTextColor);
        ((TextView) findViewById(R.id.class_tab_text))
                .setTextColor(classTabTextColor);
        findViewById(R.id.recommand_tab_footer_view).setVisibility(
                recommandTabFooterViewVisible);
        findViewById(R.id.class_tab_footer_view).setVisibility(
                classTabFooterViewVisible);
    }

    private void initView() {
        // TODO Auto-generated method stub
        reactAppFilePath = MyAppConfig.getReactAppFilePath(AppDetailActivity.this,
                ((MyApplication)getApplication()).getUid(),app.getUri().split("//")[1]);
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        reactNativeApiService = new ReactNativeAPIService(AppDetailActivity.this);
        reactNativeApiService.setAPIInterface(new WebService());
        loadingDlg = new LoadingDialog(AppDetailActivity.this);
        appIconImg = (ImageView) findViewById(R.id.app_icon_img);
        imageDisplayUtils.display(appIconImg, app.getAppIcon());
        statusBtn = (Button) findViewById(R.id.app_status_btn);
        intrImgListView = (HorizontalListView) findViewById(R.id.intr_img_list);
        if (app.getLegends() != null) {
            intrImgListView.setAdapter(new Adapter(app.getLegends()));
            intrImgListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), AppImgDisPlayActivity.class);
                    intent.putExtra("currentIndex", position);
                    startActivity(intent);
                }
            });
        }
        ((TextView) findViewById(R.id.name_text)).setText(app.getAppName());
        ((TextView) findViewById(R.id.profile_text)).setText(app.getNote());
        if (!StringUtils.isBlank(app.getDescription())) {
            ((TextView) findViewById(R.id.intr_text)).setText(app
                    .getDescription());
        }
        if (app.getUseStatus() == 1) {
            statusBtn.setText(getString(R.string.open));
        } else if (app.getUseStatus() == 0) {
            statusBtn.setText(getString(R.string.add));
        } else {
            statusBtn.setText(getString(R.string.update));
        }
        statusBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                int system = app.getAppType();
                if (app.getUseStatus() == 0) {
                    installApp(system, app.getAppID(), statusBtn);
                } else if (app.getUseStatus() == 1) {
                    if(app.getAppType() == 5){
                        Intent intent = new Intent();
                        intent.setClass(AppDetailActivity.this,ReactNativeAppActivity.class);
                        intent.putExtra("ecc-app-react-native",app.getUri());
                        startActivity(intent);
                    }else{
                        UriUtils.openApp(AppDetailActivity.this, app);
                    }
                } else {
                    // 更新
                }

            }

        });
    }


    /**
     * 安装app
     * @param type
     * @param appID
     * @param statusBtn
     */
    private void installApp(int type, String appID, Button statusBtn) {
        // TODO Auto-generated method stub
        switch (type) {
            case 2:

                break;
            case 3:
            case 4:
                addApp(statusBtn, appID);
                break;
            case 5:
                addReactNativeApp();
                break;

            default:
                break;
        }
    }

    /**
     *  添加reactNativeApp
     */
    private void addReactNativeApp() {
        if(NetUtils.isNetworkConnected(AppDetailActivity.this)){
            statusBtn.setText(getString(R.string.adding));
            loadingDlg.show();
            if(ReactNativeFlow.checkClientIdExist(AppDetailActivity.this)){
                installReactNativeApp();
            }else{
                reactNativeApiService.getClientId(AppUtils.getMyUUID(AppDetailActivity.this), AppUtils.GetChangShang());
            }
        }

    }

    /**
     * 安装ReactNative应用
     */
    private void installReactNativeApp() {
        StringBuilder describeVersionAndTime = FileUtils.readFile(reactAppFilePath +"/bundle.json", "UTF-8");

        AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
        String clientId = PreferencesByUserUtils.getString(AppDetailActivity.this,"react_native_clientid", "");
        reactNativeApiService.getDownLoadUrl(AppDetailActivity.this,app.getInstallUri(),clientId, androidBundleBean.getVersion());
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

    private class Adapter extends BaseAdapter {
        public List<String> legends;

        public Adapter(List<String> legends) {
            // TODO Auto-generated constructor stub
            this.legends = legends;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return legends.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.app__intr_img_view, null);
            ImageView image = (ImageView) convertView.findViewById(R.id.detail_img);
            imageDisplayUtils.display(image, legends.get(position));
            return convertView;
        }

    }

    public class WebService extends APIInterfaceInstance {

        @Override
        public void returnAddAppSuccess(GetAddAppResult getAddAppResult) {
            // TODO Auto-generated method stub
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
        public void returnAddAppFail(String error) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            statusBtn.setText(getString(R.string.download));
            WebServiceMiddleUtils.hand(AppDetailActivity.this,
                    error);
        }

        @Override
        public void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult) {
            super.returnGetClientIdResultSuccess(getClientIdRsult);
            if(loadingDlg!= null && loadingDlg.isShowing()){
                loadingDlg.dismiss();
            }
            PreferencesByUserUtils.putString(AppDetailActivity.this,  "react_native_clientid", getClientIdRsult.getClientId());
            installReactNativeApp();
        }

        @Override
        public void returnGetClientIdResultFail(String error) {
            if(loadingDlg!= null && loadingDlg.isShowing()){
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(AppDetailActivity.this,
                    error);
        }

        @Override
        public void returnGetDownloadReactNativeUrlSuccess(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean) {
            super.returnGetDownloadReactNativeUrlSuccess(reactNativeDownloadUrlBean);
            downloadReactNativeZip(reactNativeDownloadUrlBean);
        }

        @Override
        public void returnGetDownloadReactNativeUrlFail(String error) {
            if(loadingDlg!= null && loadingDlg.isShowing()){
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(AppDetailActivity.this,
                    error);
        }
    }

    /**
     * 下载reactNative的zip包
     * @param reactNativeDownloadUrlBean
     */
    private void downloadReactNativeZip(final ReactNativeDownloadUrlBean reactNativeDownloadUrlBean) {
        final String userId = ((MyApplication)getApplication()).getUid();
        String reactZipDownloadFromUri = APIUri.getZipUrl() + reactNativeDownloadUrlBean.getUri();
        final String reactZipFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH  + userId + "/" + reactNativeDownloadUrlBean.getUri() ;

        Callback.ProgressCallback<File> progressCallback = new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                LogUtils.YfcDebug("下载开始");
            }

            @Override
            public void onLoading(long l, long l1, boolean b) {
            }

            @Override
            public void onSuccess(File file) {

            }

            @Override
            public void onError(Throwable throwable, boolean b) {

            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {
                String reactAppInstallPath = MyAppConfig.getReactAppFilePath(AppDetailActivity.this,userId,reactNativeDownloadUrlBean.getId().getDomain());
                ReactNativeFlow.unZipFile(reactZipFilePath,reactAppInstallPath);
            }
        };
        reactNativeApiService.downloadReactNativeModuleZipPackage(reactZipDownloadFromUri,reactZipFilePath,progressCallback);
        addApp(statusBtn,app.getAppID());
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        super.finish();
    }

}
