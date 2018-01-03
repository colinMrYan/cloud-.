package com.inspur.emmcloud.util.privates;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.widget.TextView;

import com.inspur.emmcloud.widget.spans.URLClickableSpan;
import com.inspur.emmcloud.widget.spans.URLSpanNoUnderline;


public class TransHtmlToTextUtils {

	/**
	 * 去掉Span的下划线，设置Span前景色
	 * @param textView
	 * @param color
     */
	public static void stripUnderlines(TextView textView,int color) {
		Spannable spannable = new SpannableString(textView.getText());
		URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
		for (URLSpan span : spans) {
			int start = spannable.getSpanStart(span);
			int end = spannable.getSpanEnd(span);
			spannable.removeSpan(span);
			String openUrl = span.getURL();
			CharacterStyle characterStyle;
			if(openUrl.startsWith("http")){
				characterStyle = new URLClickableSpan(openUrl);
			}else {
				characterStyle = new URLSpanNoUnderline(openUrl);
			}
			spannable.setSpan(characterStyle, start, end, 0);
			spannable.setSpan(new ForegroundColorSpan(color),
					start, end, 0);
		}
		textView.setText(spannable);
	}
}
