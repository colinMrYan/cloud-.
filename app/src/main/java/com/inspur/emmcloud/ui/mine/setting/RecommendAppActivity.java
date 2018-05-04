package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.widget.ProgressWebView;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.common.QueuedWork;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.ref.WeakReference;

/**
 * Created by chenmch on 2017/9/1.
 */

@ContentView(R.layout.activity_recommend_app)
public class RecommendAppActivity extends BaseActivity {
    private final String RECOMMAND_APP_URL = APIUri.getRecommandAppUrl();
    @ViewInject(R.id.webview)
    ProgressWebView webView;
    private CustomShareListener mShareListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        webView.loadUrl(RECOMMAND_APP_URL);
        Config.DEBUG = true;
        QueuedWork.isUseThreadPool = false;
        UMShareAPI.get(this);
        PlatformConfig.setWeixin("wx4eb8727ea9c26495", "56a0426315f1d0985a1cc1e75e96130d");
        PlatformConfig.setQQZone("1105561850", "1kaw4r1c37SUupFL");
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.share_img:
                ShareWeb();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }


    private void ShareWeb() {

        mShareListener = new CustomShareListener(this);
        new ShareAction(RecommendAppActivity.this).setDisplayList(
                SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                 SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE,SHARE_MEDIA.SMS
        )
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if(share_media == SHARE_MEDIA.SMS) {
                            new ShareAction(RecommendAppActivity.this).withText("欢迎使用【云+】  "+"https://www.inspuronline.com/yjapp/")
                                    .setPlatform(share_media)
                                    .setCallback(mShareListener)
                                    .share();
                        }else {
                            UMImage thumb = new UMImage(RecommendAppActivity.this, R.drawable.ic_launcher_share);
                            UMWeb web = new UMWeb("https://www.inspuronline.com/yjapp/");
                            web.setThumb(thumb);
                            web.setDescription("云+ -智能化的企业协同平台");
                            web.setTitle("欢迎使用【云+】");
                            new ShareAction(RecommendAppActivity.this).withMedia(web)
                                    .setPlatform(share_media)
                                    .setCallback(mShareListener)
                                    .share();
                        }
                    }
                })
               .open();

    }


    private static class CustomShareListener implements UMShareListener {

        private WeakReference<RecommendAppActivity> mActivity;

        private CustomShareListener(RecommendAppActivity activity) {
            mActivity = new WeakReference(activity);
        }

        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            ToastUtils.show(mActivity.get(),R.string.news_share_success);
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            ToastUtils.show(mActivity.get(),R.string.news_share_fail);
            if (t != null) {
                LogUtils.jasonDebug("throw:" + t.getMessage());
            }

        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {

        }
    }
}
