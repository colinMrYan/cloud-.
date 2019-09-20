package com.inspur.emmcloud.util.privates;

import android.text.SpannableString;
import android.text.Spanned;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.bean.schedule.MentionsAndUrl;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.spans.URLClickableSpan;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenmch on 2018/2/7.
 */

public class ChatMsgContentUtils {
    public static SpannableString mentionsAndUrl2Span(String content, Map<String, String> mentionsMap) {
        if (StringUtils.isBlank(content)) {
            return new SpannableString("");
        }
        StringBuilder contentStringBuilder = new StringBuilder();
        contentStringBuilder.append(content);
        Pattern mentionPattern = Pattern.compile("@[a-z]*\\d+\\s");
        Matcher mentionMatcher = mentionPattern.matcher(contentStringBuilder);
        ArrayList<MentionsAndUrl> MentionProtocolList = new ArrayList<MentionsAndUrl>();
        while (mentionMatcher.find()) {
            String patternString = mentionMatcher.group();
            String key = patternString.substring(1, patternString.length() - 1).trim();
            if (mentionsMap.containsKey(key)) {
                String uid = mentionsMap.get(key);
                String protocol = "ecm-contact://" + uid;
                String newString;
                if (uid.equals("EVERYBODY")) {
                    newString = "@" + BaseApplication.getInstance().getString(R.string.chat_search_mention_all) + " ";
                } else {
                    newString = "@" + ContactUserCacheUtils.getUserName(uid) + " ";
                }
                int startPosition = contentStringBuilder.indexOf(patternString);
                contentStringBuilder.replace(startPosition, startPosition + patternString.length(), newString);
                MentionProtocolList.add(new MentionsAndUrl(startPosition, startPosition + newString.length(), protocol));
            }
        }

        content = contentStringBuilder.toString();
        Pattern urlPattern = Pattern.compile(Constant.PATTERN_URL, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = urlPattern.matcher(content);
        while (urlMatcher.find()) {
            String patternString = urlMatcher.group();
            if (!patternString.toLowerCase().startsWith("http")) {
                patternString = "http://" + patternString;
            }
            MentionProtocolList.add(new MentionsAndUrl(urlMatcher.start(), urlMatcher.end(), patternString));
        }

        SpannableString spannableString = new SpannableString(content);
        for (int i = 0; i < MentionProtocolList.size(); i++) {
            MentionsAndUrl mentionsAndUrl = MentionProtocolList.get(i);
            String url = mentionsAndUrl.getProtocol();
            URLClickableSpan urlClickableSpan = new URLClickableSpan(url);
            spannableString.setSpan(urlClickableSpan, mentionsAndUrl.getStart(), mentionsAndUrl.getEnd(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return spannableString;
    }

}
