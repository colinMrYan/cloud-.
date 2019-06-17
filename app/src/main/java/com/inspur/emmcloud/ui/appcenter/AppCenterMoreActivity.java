package com.inspur.emmcloud.ui.appcenter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.appcenter.App;

import java.util.List;

public class AppCenterMoreActivity extends BaseActivity {

    public static final String APP_CENTER_APPLIST = "appList";
    public static final String APP_CENTER_CATEGORY_NAME = "category_name";
    private ListView appCenterMoreListView;
    private List<App> appList;

    @Override
    public void onCreate() {
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_app_center_more;
    }


    /**
     * 初始化views
     */
    private void initView() {
        appCenterMoreListView = (ListView) findViewById(R.id.app_center_more_apps);
        if (getIntent().hasExtra(APP_CENTER_APPLIST)) {
            appList = (List<App>) getIntent().getSerializableExtra(APP_CENTER_APPLIST);
            if (appList != null) {
                AppMoreAdapter adapter = new AppMoreAdapter();
                appCenterMoreListView.setAdapter(adapter);
                appCenterMoreListView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("app", appList.get(position));
                        IntentUtils.startActivity(AppCenterMoreActivity.this, AppDetailActivity.class, bundle);
                    }
                });
            }
        }
        if (getIntent().hasExtra(APP_CENTER_CATEGORY_NAME)) {
            ((TextView) findViewById(R.id.header_text)).setText(getIntent().getStringExtra(APP_CENTER_CATEGORY_NAME));
        }
    }

    /**
     * 关闭
     *
     * @param v
     */
    public void onClick(View v) {
        finish();
    }

    class AppMoreAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return appList.size();
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
            App app = appList.get(position);
            convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.app_center_more_app_item_view, null);
            ((TextView) convertView.findViewById(R.id.app_name_text)).setText(app.getAppName());
            ((TextView) convertView.findViewById(R.id.txt_app_note)).setText(app.getNote());
            ImageDisplayUtils.getInstance().displayImage((ImageView) convertView.
                    findViewById(R.id.app_icon_img), app.getAppIcon(), R.drawable.ic_app_default);
            return convertView;
        }

    }
}
