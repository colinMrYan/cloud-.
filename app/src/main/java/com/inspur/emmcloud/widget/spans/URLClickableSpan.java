package com.inspur.emmcloud.widget.spans;

import android.app.Activity;
import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.inspur.emmcloud.util.UriUtils;

/**
 * Created by yufuchang on 2017/3/21.
 */

public class URLClickableSpan extends ClickableSpan {

    /**
     * 打开uri
     */
    private String openUri;
    public URLClickableSpan(String openUrl){
        this.openUri = openUrl;
    }
    public void onClick(View view) {
        //Do something with URL here.
        Context context = view.getContext();
//        Uri uri = Uri.parse(openUri);
//        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//        intent.setClass(context, AppWebOpenActivity.class);
//        intent.putExtra("url",openUri);
//        context.startActivity(intent);
        UriUtils.openUrl((Activity) context,openUri);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }
}
