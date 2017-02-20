package com.inspur.emmcloud.util;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.widget.TextView;


public class TransHtmlToTextUtils {

	public static void stripUnderlines(TextView textView,int color) {
		Spannable spannable = new SpannableString(textView.getText());
		URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
		for (URLSpan span : spans) {
			int start = spannable.getSpanStart(span);
			int end = spannable.getSpanEnd(span);
			spannable.removeSpan(span);
			span = new URLSpanNoUnderline(span.getURL());
			spannable.setSpan(span, start, end, 0);
			spannable.setSpan(new ForegroundColorSpan(color),
					start, end, 0);
		}
		textView.setText(spannable);
	}
	
	public static class URLSpanNoUnderline extends URLSpan {
		public URLSpanNoUnderline(String url) {
			super(url);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			super.updateDrawState(ds);
			ds.setUnderlineText(false);
		}
		
	}

}
