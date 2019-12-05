package com.inspur.emmcloud.application.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.application.R;
import com.inspur.emmcloud.application.api.ApplicationAPIService;
import com.inspur.emmcloud.application.api.ApplicationApiInterfaceImpl;
import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.bean.GetAddAppResult;
import com.inspur.emmcloud.application.util.ApplicationUriUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;

import java.util.List;

public class AppCenterAdapter extends BaseAdapter {

    private static final String ACTION_NAME = "add_app";
    private List<App> appList;
    private Activity activity;
    private LoadingDialog loadingDialog;
    private ApplicationAPIService apiService;

    public AppCenterAdapter(Activity activity, List<App> appList) {
        this.appList = appList;
        this.activity = activity;
        loadingDialog = new LoadingDialog(activity);
        apiService = new ApplicationAPIService(activity);
        apiService.setAPIInterface(new WebService());
    }

    /**
     * 添加应用
     *
     * @param app
     */
    public void addApp(App app) {
        int addPosition = -1;
        for (int i = 0; i < appList.size(); i++) {
            if (appList.get(i).getAppID()
                    .equals(app.getAppID())) {
                addPosition = i;
                break;
            }
        }

        if (addPosition == -1) {
            return;
        }
        appList.get(addPosition).setUseStatus(1);
        AppCenterAdapter.this.notifyDataSetChanged();
    }

    public void notifyListData(List<App> AppList) {
        this.appList = AppList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.all_app_item_view, null);
            holder.iconImg = (ImageView) convertView
                    .findViewById(R.id.app_icon_img);
            holder.nameText = (TextView) convertView
                    .findViewById(R.id.tv_name_tips);
            holder.statusBtn = (Button) convertView
                    .findViewById(R.id.app_status_btn);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        final App app = appList.get(position);

        final String appID = app.getAppID();
        ImageDisplayUtils.getInstance().displayImage(holder.iconImg, app.getAppIcon(), R.drawable.ic_app_default);
        holder.nameText.setText(app.getAppName());
        if (app.getUseStatus() == 1) {
            holder.statusBtn.setText(activity.getString(R.string.open));
        } else if (app.getUseStatus() == 0) {
            holder.statusBtn.setText(activity.getString(R.string.add));
        } else {
            holder.statusBtn.setText(activity.getString(R.string.update));
        }
        holder.statusBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int type = app.getAppType();
                if (app.getUseStatus() == 0) {
                    installApp(type, appID, (Button) v);
                } else if (app.getUseStatus() == 1) {
                    ApplicationUriUtils.openApp(activity, app, "appcenter");
                } else {
                    // 更新
                }

            }

        });
        return convertView;
    }

    private void installApp(int type, String appID, Button statusBtn) {
        if (NetUtils.isNetworkConnected(activity)) {
            statusBtn.setText(activity.getString(R.string.adding));
            loadingDialog.show();
            apiService.addApp(appID);
        }
    }

    public static class Holder {
        ImageView iconImg;
        TextView nameText;
        Button statusBtn;
    }

    public class WebService extends ApplicationApiInterfaceImpl {

        @Override
        public void returnAddAppSuccess(GetAddAppResult getAddAppResult) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            int addPosition = -1;
            for (int i = 0; i < appList.size(); i++) {
                if (appList.get(i).getAppID()
                        .equals(getAddAppResult.getAppID())) {
                    addPosition = i;
                    break;
                }
            }

            if (addPosition == -1) {
                return;
            }

            Intent mIntent = new Intent(ACTION_NAME);
            mIntent.putExtra("app", appList.get(addPosition));
            // 发送广播
            LocalBroadcastManager.getInstance(activity).sendBroadcast(mIntent);
        }

        @Override
        public void returnAddAppFail(String error, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(activity, error, errorCode);
            AppCenterAdapter.this.notifyDataSetChanged();
        }
    }

}
