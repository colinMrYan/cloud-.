package com.inspur.emmcloud.util.privates;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.MentionsAndUrl;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.basemodule.widget.spans.URLClickableSpan;

import org.json.JSONArray;
import org.json.JSONObject;

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

    public static String getMentions(String content, Map<String, String> mentionsMap) {
        if (StringUtils.isBlank(content)) {
            return new String("");
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
//        Pattern urlPattern = Pattern.compile(Constant.PATTERN_URL, Pattern.CASE_INSENSITIVE);
//        Matcher urlMatcher = urlPattern.matcher(content);
//        while (urlMatcher.find()) {
//            String patternString = urlMatcher.group();
//            if (!patternString.toLowerCase().startsWith("http")) {
//                patternString = "http://" + patternString;
//            }
//            MentionProtocolList.add(new MentionsAndUrl(urlMatcher.start(), urlMatcher.end(), patternString));
//        }
//
//        SpannableString spannableString = new SpannableString(content);
//        for (int i = 0; i < MentionProtocolList.size(); i++) {
//            MentionsAndUrl mentionsAndUrl = MentionProtocolList.get(i);
//            String url = mentionsAndUrl.getProtocol();
//            URLClickableSpan urlClickableSpan = new URLClickableSpan(url);
//            spannableString.setSpan(urlClickableSpan, mentionsAndUrl.getStart(), mentionsAndUrl.getEnd(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//        }
        return content;
    }

    /**
     * @param content
     * @param mentionsMap
     * @param membersDetail @功能使用，用于显示昵称
     * @return
     */
    public static SpannableString mentionsAndUrl2Span(String content, Map<String, String> mentionsMap, JSONArray membersDetail) {
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
                    if (membersDetail != null) {
                        // 获取昵称，群聊时成员可修改昵称
                        newString = "@" + getUserNicknameOrName(membersDetail, uid) + " ";
                    } else {
                        newString = "@" + ContactUserCacheUtils.getUserName(uid) + " ";
                    }
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

    public static String getMentions(String content, Map<String, String> mentionsMap, JSONArray membersDetail) {
        if (StringUtils.isBlank(content)) {
            return new String("");
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
                    if (membersDetail != null) {
                        // 获取昵称，群聊时成员可修改昵称
                        newString = "@" + getUserNicknameOrName(membersDetail, uid) + " ";
                    } else {
                        newString = "@" + ContactUserCacheUtils.getUserName(uid) + " ";
                    }
                }
                int startPosition = contentStringBuilder.indexOf(patternString);
                contentStringBuilder.replace(startPosition, startPosition + patternString.length(), newString);
                MentionProtocolList.add(new MentionsAndUrl(startPosition, startPosition + newString.length(), protocol));
            }
        }

        content = contentStringBuilder.toString();
        return content;
    }

    public static String getUserNicknameOrName(JSONArray membersDetailArray, String uid) {
        String username = "";
        for (int i = 0; i < membersDetailArray.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(membersDetailArray, i, new JSONObject());
            if (uid != null && uid.equals(JSONUtils.getString(obj, "user", ""))) {
                String nickname = JSONUtils.getString(obj, "nickname", "");
                if (TextUtils.isEmpty(nickname)) {
                    username = ContactUserCacheUtils.getUserName(uid);
                } else {
                    username = nickname;
                }
                break;
            }
        }
        if (TextUtils.isEmpty(username)) {
            username = ContactUserCacheUtils.getUserName(uid);
        }
        return username;
    }

    // 仅获取昵称
    public static String getUserNickname(JSONArray membersDetailArray, String uid) {
        String username = "";
        for (int i = 0; i < membersDetailArray.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(membersDetailArray, i, new JSONObject());
            if (uid != null && uid.equals(JSONUtils.getString(obj, "user", ""))) {
                username = JSONUtils.getString(obj, "nickname", "");
                membersDetailArray.remove(i);
                break;
            }
        }
        return username;
    }

}
