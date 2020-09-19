package com.inspur.emmcloud.widget.spans;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.LanguageManager;

/**
 * Created by yufuchang on 2017/3/21.
 */

public class URLClickableSpan extends ClickableSpan {

    /**
     * 打开uri
     */
    private String openUri;

    public URLClickableSpan(String openUrl) {
        this.openUri = openUrl;
    }

    public void onClick(View view) {
        //Do something with URL here.
        Context context = view.getContext();
        if (Constant.SERVICE_AGREEMENT.equals(openUri)) {
            openUrl(openUri + LanguageManager.getInstance().getCurrentAppLanguage() + ".html");
        } else if (openUri.toLowerCase().startsWith("http")) {
            openUrl(openUri);
        } else {
            try {
                Intent intent = Intent.parseUri(openUri, Intent.URI_INTENT_SCHEME);
                intent.setComponent(null);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }

    /**
     * 打开url
     *
     * @param uri
     */
    private void openUrl( String uri) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
    }
}
