package com.inspur.emmcloud.widget.spans;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.inspur.emmcloud.util.privates.UriUtils;

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
        if (openUri.startsWith("http")) {
            UriUtils.openUrl((Activity) context, openUri);
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
}
