package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.LogUtils;
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
import com.umeng.socialize.utils.Log;
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
    private final String RECOMMAND_APP_URL = "https://emm.inspur.com/admin/share_qr";
    @ViewInject(R.id.webview)
    ProgressWebView webView;
    private CustomShareListener mShareListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplicationContext()).addActivity(this);
        x.view().inject(this);
        webView.loadUrl(RECOMMAND_APP_URL);
        Config.DEBUG = true;
        QueuedWork.isUseThreadPool = false;
        UMShareAPI.get(this);
        PlatformConfig.setWeixin("xxxxxx", "xxxxxx");
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
        //UMShareAPI.get(this).getPlatformInfo(this,share_media, authListener);
        UMImage thumb = new UMImage(this, R.drawable.ic_launcher_share);
        UMWeb web = new UMWeb("https://ecm.inspur.com/");
        web.setThumb(thumb);
        web.setDescription("智能化的企业移动办公平台");
        web.setTitle("云+");
        mShareListener = new CustomShareListener(this);
        new ShareAction(RecommendAppActivity.this).setDisplayList(
                SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                 SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE,SHARE_MEDIA.SMS
        )
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if(share_media == SHARE_MEDIA.SMS) {
                            new ShareAction(RecommendAppActivity.this).withText("云+ 智能化的企业移动办公平台  https://ecm.inspur.com/")
                                    .setPlatform(share_media)
                                    .setCallback(mShareListener)
                                    .share();
                        }
                    }
                })
                .withMedia(web)
                .setCallback(new CustomShareListener(this)).open();

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

            Toast.makeText(mActivity.get(), platform + " 分享成功啦", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {

            Toast.makeText(mActivity.get(), platform + " 分享失败啦", Toast.LENGTH_SHORT).show();
            if (t != null) {
                LogUtils.jasonDebug("throw:" + t.getMessage());
            }

        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {

            Toast.makeText(mActivity.get(), platform + " 分享取消了", Toast.LENGTH_SHORT).show();
        }
    }
}
