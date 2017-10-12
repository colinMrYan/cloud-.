package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.GetRemoveAppResult;
import com.inspur.emmcloud.util.AppCacheUtils;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.GradientDrawableBuilder;
import com.inspur.emmcloud.widget.ImageViewRound;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.List;

public class DragAdapter extends BaseAdapter {
    private Context context;
    private List<App> appList;
    private int groupPosition = -1;
    private NotifyCommonlyUseListener commonlyUseListener;
    private boolean canEdit = false;
    private ImageDisplayUtils imageDisplayUtils;
    private LoadingDialog loadingDialog;
    private int deletePosition = -1;

    public DragAdapter(Context context, List<App> appList, int position) {
        this.context = context;
        this.appList = appList;
        this.groupPosition = position;
        imageDisplayUtils = new ImageDisplayUtils(R.drawable.icon_empty_icon);
        loadingDialog = new LoadingDialog(context);
    }

    @Override
    public int getCount() {
        return appList == null ? 0 : appList.size();
    }

    @Override
    public App getItem(int position) {
        if (appList != null && appList.size() != 0) {
            return appList.get(position);
        }
        return new App();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final App app = getItem(position);
        convertView = LayoutInflater.from(context).inflate(
                R.layout.my_app_item_view, null);
        ImageViewRound iconImg = (ImageViewRound) convertView
                .findViewById(R.id.icon_image);
        TextView unhandledNotification = (TextView) convertView.findViewById(R.id.unhandled_notification);
        iconImg.setType(ImageViewRound.TYPE_ROUND);
        iconImg.setRoundRadius(DensityUtil.dip2px(context, 10));
        TextView nameText = (TextView) convertView.findViewById(R.id.name_text);
        if (app.getBadge() != 0) {
            unhandledNotification.setVisibility(View.VISIBLE);
            GradientDrawable gradientDrawable = new GradientDrawableBuilder()
                    .setCornerRadius(DensityUtil.dip2px(context, 40))
                    .setBackgroundColor(0xFFFF0033)
                    .setStrokeColor(0xFFFF0033).build();
            unhandledNotification.setBackground(gradientDrawable);
            unhandledNotification.setText(app.getBadge() + "");
        }
        ImageView deleteImg = (ImageView) convertView
                .findViewById(R.id.delete_markView);
        nameText.setText(app.getAppName());
        imageDisplayUtils.displayImage(iconImg, app.getAppIcon());
        if (canEdit) {
            if (!app.getIsMustHave()) {
                deleteImg.setVisibility(View.VISIBLE);
            }
            startAnimation(convertView, position);
            deleteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deletePosition = position;
                    removeApp(app);
                }
            });
        } else {
            deleteImg.setVisibility(View.GONE);
            stopAnimation(convertView);
        }
        return convertView;
    }

    /**
     * 移除app
     *
     * @param app
     */
    private void removeApp(App app) {
        if (NetUtils.isNetworkConnected(context)) {
            loadingDialog.show();
            MyAppAPIService apiService = new MyAppAPIService(context);
            apiService.setAPIInterface(new WebService());
            apiService.removeApp(app.getAppID());
        }
    }

    private void uninstallNativeApp(String packageName) {
        if (AppUtils.isAppInstalled(context, packageName)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + packageName));
            context.startActivity(intent);
        }
    }

    /**
     * 停止动画
     *
     * @param convertView
     */
    private void stopAnimation(View convertView) {
        convertView.clearAnimation();
    }

    /**
     * 加入动画
     *
     * @param convertView
     */
    private void startAnimation(View convertView, int position) {
        Animation animation;
        if (position % 2 == 0) {
            animation = AnimationUtils.loadAnimation(context,
                    R.anim.rotate_left);
        } else {
            animation = AnimationUtils.loadAnimation(context,
                    R.anim.rotate_right);
        }
        convertView.startAnimation(animation);
    }

    /**
     * 设置可编辑状态
     *
     * @param canEdit
     */
    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    /**
     * 设置删除的position
     */
    public void setRemove(int position) {
        notifyDataSetChanged();
    }

    /**
     * 删除频道列表
     */
    public void remove() {
        notifyDataSetChanged();
    }

    /**
     * 传入分组Id
     *
     * @return
     */
    public int getGroupPosition() {
        return groupPosition;
    }

    /**
     * 获取分组Id
     *
     * @return
     */
    public void setGroupPosition(int groupPosition) {
        this.groupPosition = groupPosition;
    }

    /**
     * 刷新常用的接口
     */
    public interface NotifyCommonlyUseListener {
        void onNotifyCommonlyUseApp(App app);
    }

    /**
     * 设置刷新常用的接口
     *
     * @param l
     */
    public void setNotifyCommonlyUseListener(NotifyCommonlyUseListener l) {
        this.commonlyUseListener = l;
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnRemoveAppFail(String error, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(context, error, errorCode);
        }

        public void returnRemoveAppSuccess(GetRemoveAppResult getRemoveAppResult) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            if (deletePosition != -1) {
                App app = appList.get(deletePosition);
                AppCacheUtils.deleteAppCommonlyByAppID(context, app.getAppID());
                appList.remove(deletePosition);
                commonlyUseListener.onNotifyCommonlyUseApp(app);
                notifyDataSetChanged();
                if (app.getAppType() == 2) {
                    String packageName = app.getPackageName();
                    if (packageName.equals("cn.knowhowsoft.khmap5")) {
                        packageName = "com.knowhowsoft.khmap5";
                    }
                    uninstallNativeApp(packageName);
                }

            }
        }
    }

}