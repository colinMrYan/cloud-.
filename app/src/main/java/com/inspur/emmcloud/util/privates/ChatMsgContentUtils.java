package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;

import com.inspur.emmcloud.bean.work.MentionsAndUrl;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.emmcloud.widget.spans.URLClickableSpan;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenmch on 2018/2/7.
 */

public class ChatMsgContentUtils {
    public static SpannableString mentionsAndUrl2Span(Context context, String content, Map<String,String> mentionsMap){
        if(StringUtils.isBlank(content)){
            return new SpannableString("");
        }
        StringBuilder contentStringBuilder = new StringBuilder();
        contentStringBuilder.append(content);
        Pattern mentionPattern = Pattern.compile("@[0-9]+\\s");
        Matcher mentionMatcher = mentionPattern.matcher(contentStringBuilder);
        ArrayList<MentionsAndUrl> MentionProtocolList = new ArrayList<MentionsAndUrl>();
        while (mentionMatcher.find()) {
            String patternString = mentionMatcher.group();
            String uid = patternString.substring(1,patternString.length()-1).trim();
            if (mentionsMap.containsKey(uid)){
                String protocol = "ecm-contact://"+uid;
                String newString = "@"+ ContactCacheUtils.getUserName(context,uid);
                //int startPosition = contentStringBuilder.indexOf(patternString);
                int startPosition = mentionMatcher.start();
                int endPosition = mentionMatcher.end();
                contentStringBuilder.replace(startPosition,endPosition,newString);
                MentionProtocolList.add(new MentionsAndUrl(startPosition,startPosition+newString.length(),protocol));
            }
        }


        Pattern urlPattern = Pattern.compile(Constant.PATTERN_URL);
        Matcher urlMatcher = urlPattern.matcher(content);
        while (urlMatcher.find()) {
            String patternString = urlMatcher.group();
            MentionProtocolList.add(new MentionsAndUrl(urlMatcher.start(),urlMatcher.end(),patternString));
        }

        content = contentStringBuilder.toString();
        SpannableString spannableString = new SpannableString(content);
        for (int i=0;i<MentionProtocolList.size();i++){
            MentionsAndUrl mentionsAndUrl = MentionProtocolList.get(i);
            String url = mentionsAndUrl.getProtocol();
            if(!url.startsWith("http")){
                url = "http://"+url;
            }
            URLClickableSpan urlClickableSpan = new URLClickableSpan(url);
            spannableString.setSpan(urlClickableSpan, mentionsAndUrl.getStart(), mentionsAndUrl.getEnd(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return  spannableString;
    }

}
