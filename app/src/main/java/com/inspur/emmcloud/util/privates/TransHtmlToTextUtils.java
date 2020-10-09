package com.inspur.emmcloud.util.privates;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;
import com.inspur.emmcloud.basemodule.widget.spans.URLClickableSpan;


public class TransHtmlToTextUtils {

    /**
     * 去掉Span的下划线，设置Span前景色
     *
     * @param textView
     * @param color
     */
    public static void stripUnderlines(TextView textView, int color) {
        Spannable spannable = new SpannableString(textView.getText());
        spannable = EmotionUtil.getInstance(BaseApplication.getInstance()).getSmiledText(spannable, textView.getTextSize());
        URLClickableSpan[] spans = spannable.getSpans(0, spannable.length(), URLClickableSpan.class);
        for (URLClickableSpan span : spans) {
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            spannable.setSpan(new ForegroundColorSpan(color),
                    start, end, 0);
        }
        textView.setText(spannable);
    }
}
