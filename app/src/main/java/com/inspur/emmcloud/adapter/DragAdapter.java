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
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.AppBadgeBean;
import com.inspur.emmcloud.bean.appcenter.GetRemoveAppResult;
import com.inspur.emmcloud.util.privates.cache.AppCacheUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.GradientDrawableBuilder;
import com.inspur.emmcloud.widget.ImageViewRound;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.List;
import java.util.Map;

public class DragAdapter extends BaseAdapter {
    private Context context;
    private List<App> appList;
    private int groupPosition = -1;
    private NotifyCommonlyUseListener commonlyUseListener;
    private boolean canEdit = false;//表示排序和删除两个状态
    private LoadingDialog loadingDialog;
    private int deletePosition = -1;
    private Map<String, AppBadgeBean> appBadgeBeanMap;

    public DragAdapter(Context context, List<App> appList, int position, Map<String, AppBadgeBean> appBadgeBeanMap) {
        this.context = context;
        this.appList = appList;
        this.groupPosition = position;
        loadingDialog = new LoadingDialog(context);
        this.appBadgeBeanMap = appBadgeBeanMap;
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
        //应用图标
        ImageViewRound appIconImg = (ImageViewRound) convertView
                .findViewById(R.id.icon_image);
        setAppIconImg(app, appIconImg);
        //应用名称
        TextView appNameText = (TextView) convertView.findViewById(R.id.tv_name);
        appNameText.setText(app.getAppName());
        //未处理消息条数
        TextView unhandledBadges = (TextView) convertView.findViewById(R.id.unhandled_badges_text);
        setUnHandledBadgesDisplay(app, unhandledBadges);
        //删除图标显示和监听事件处理
        handleAppDeleteImg(app, position, convertView);
        return convertView;
    }

    /**
     * 处理应用图标显示
     *
     * @param app
     * @param appIconImg
     */
    private void setAppIconImg(App app, ImageViewRound appIconImg) {
        appIconImg.setType(ImageViewRound.TYPE_ROUND);
        appIconImg.setRoundRadius(DensityUtil.dip2px(context, 10));
        ImageDisplayUtils.getInstance().displayImage(appIconImg, app.getAppIcon(), R.drawable.ic_app_default);
    }

    /**
     * 处理删除按钮显示和事件监听
     *
     * @param app
     * @param position
     * @param convertView
     */
    private void handleAppDeleteImg(final App app, final int position, View convertView) {
        ImageView deleteImg = (ImageView) convertView
                .findViewById(R.id.delete_markview_text);
        if (canEdit) {
            if (!app.getIsMustHave()) {
                deleteImg.setVisibility(View.VISIBLE);
                deleteImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deletePosition = position;
                        removeApp(app);
                    }
                });
            }
            startAnimation(convertView, position);
        } else {
            stopAnimation(convertView);
        }
    }

    /**
     * 处理未处理消息个数的显示
     *
     * @param app
     * @param unhandledBadges
     */
    private void setUnHandledBadgesDisplay(App app, TextView unhandledBadges) {
        AppBadgeBean appBadgeBean = appBadgeBeanMap.get(app.getAppID());
        if (appBadgeBean != null && appBadgeBean.getBadgeNum() > 0) {
            unhandledBadges.setVisibility(View.VISIBLE);
            GradientDrawable gradientDrawable = new GradientDrawableBuilder()
                    .setCornerRadius(DensityUtil.dip2px(context, 40))
                    .setBackgroundColor(0xFFF74C31)
                    .setStrokeColor(0xFFF74C31).build();
            unhandledBadges.setBackground(gradientDrawable);
            unhandledBadges.setText(appBadgeBean.getBadgeNum() > 99 ? "99+" : (appBadgeBean.getBadgeNum() + ""));
        }
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

    /**
     * 卸载应用
     *
     * @param packageName
     */
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