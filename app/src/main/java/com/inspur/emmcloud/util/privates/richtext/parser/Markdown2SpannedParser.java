package com.inspur.emmcloud.util.privates.richtext.parser;

import android.text.Spanned;
import android.widget.TextView;

import com.inspur.emmcloud.util.privates.richtext.markdown.MarkDown;


/**
 * Created by zhou on 16-7-27.
 * Markdown2SpannedParser
 */
public class Markdown2SpannedParser implements SpannedParser {

    private TextView textView;

    public Markdown2SpannedParser(TextView textView) {
        this.textView = textView;
    }

    @Override
    public Spanned parse(String source) {
        return MarkDown.fromMarkdown(source, null, textView);
    }
}
