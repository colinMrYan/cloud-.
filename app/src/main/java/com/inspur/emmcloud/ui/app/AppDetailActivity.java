package com.inspur.emmcloud.ui.app;

import android.content.Intent;
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
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.api.apiservice.ReactNativeAPIService;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.GetAddAppResult;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.HorizontalListView;
import com.inspur.emmcloud.widget.LoadingDialog;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);
        ((MyApplication) getApplicationContext()).addActivity(this);
        imageDisplayUtils = new ImageDisplayUtils(getApplicationContext(), R.drawable.icon_empty_icon);
        app = (App) getIntent().getExtras().getSerializable("app");
        initView();
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        reactNativeApiService = new ReactNativeAPIService(AppDetailActivity.this);
        reactNativeApiService.setAPIInterface(new WebService());
    }

    private void initView() {
        // TODO Auto-generated method stub
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
                int appType = app.getAppType();
                //外部原生应用
                if (appType == 2){

                }else {
                    if (app.getUseStatus() == 0) {
                        installApp(appType, app.getAppID(), statusBtn);
                    } else if (app.getUseStatus() == 1) {
                        if(app.getAppType() == 5){
                            Intent intent = new Intent();
                            intent.setClass(AppDetailActivity.this,ReactNativeAppActivity.class);
                            intent.putExtra("ecc-app-react-native",app.getUri());
                            startActivity(intent);
                        }else{
                            UriUtils.openApp(AppDetailActivity.this, app);
                        }
                    }
                }


            }

        });
    }


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
                addApp(statusBtn, appID);
                break;

            default:
                break;
        }
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

    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        super.finish();
    }

}
