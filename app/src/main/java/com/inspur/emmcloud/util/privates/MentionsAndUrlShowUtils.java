package com.inspur.emmcloud.util.privates;

import android.text.SpannableString;
import android.text.Spanned;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.bean.MentionsAndUrl;
import com.inspur.emmcloud.basemodule.widget.spans.URLClickableSpan;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MentionsAndUrlShowUtils {

    /**
     * 把mentions和urlList的信息组合成可渲染的字符串
     *
     * @param mentions
     * @param mentionList
     * @param urlList
     * @return
     */
    public static SpannableString getMsgContentSpannableString(String msgBody) {
        String source = JSONUtils.getString(msgBody, "source", "");
        List<String> mentionUidList = JSONUtils.getStringList(msgBody, "mentions", new ArrayList<String>());
        List<String> urlList = JSONUtils.getStringList(msgBody, "urls", new ArrayList<String>());
        ArrayList<MentionsAndUrl> mentionsAndUrlList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[[^\\]]+\\]\\([^\\)]+\\)");
        if (StringUtils.isBlank(source)) {
            return new SpannableString("");
        }
        if (mentionUidList.size() == 0 && urlList.size() == 0) {
            return new SpannableString(source);
        }
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            String patternString = matcher.group();
            String protocolResource = "";
            String content = "";
            String contentResource = "";
            String protocolResourceGS = "";
            String protocol = "";
            int index = -1;
            index = source.indexOf(patternString);
            boolean hasProtocol = false;

            StringBuilder stringBuilder = new StringBuilder();
            char leftBrackets = '(';
            for (int i = patternString.length() - 1; i > 0; i--) {
                char item = patternString.charAt(i);
                stringBuilder.append(item);
                if (item == leftBrackets) {
                    break;
                }
            }
            if (stringBuilder.toString().length() > 0) {
                protocolResource = stringBuilder.reverse().toString();
                if (stringBuilder.toString().startsWith("(ecm-contact://")) {
                    protocol = stringBuilder.toString().replace("(", "").replace(")", "");
                    protocolResource = stringBuilder.toString().replace("ecm-contact://", "")
                            .replace("(", "").replace(")", "");
                } else {
                    protocol = stringBuilder.toString().replace("(", "").replace(")", "");
                }
            }
            Pattern patternContent = Pattern.compile("\\[.*\\]");
            Matcher matcherContent = patternContent.matcher(patternString);
            while (matcherContent.find()) {
                content = matcherContent.group();
                content = content.replace("[", "").replace("]", "");
                contentResource = content;
                protocolResourceGS = protocolResource.replace("(", "").replace(")", "");
            }
            if (mentionUidList.contains(protocolResource)) {
                hasProtocol = true;
            }
            int urlSize = urlList.size();
            for (int i = 0; i < urlSize; i++) {
                String unesUrl = StringEscapeUtils.unescapeJava(urlList.get(i));
                unesUrl = unesUrl.replaceAll("\"", "");
                if (protocolResourceGS.contains(contentResource) || unesUrl.equals(protocolResourceGS)) {
                    hasProtocol = true;
                }

            }
            if (hasProtocol) {
                StringBuilder sb = new StringBuilder(source);
                source = sb.replace(index, index + patternString.length(), content).toString();
                int start = source.indexOf(content, index);
                int end = start + content.length();
                MentionsAndUrl mentionsAndUrl = new MentionsAndUrl(start, end, protocol);
                mentionsAndUrlList.add(mentionsAndUrl);
            }
        }
        SpannableString spannableString = new SpannableString(source);
        for (int i = 0; i < mentionsAndUrlList.size(); i++) {
            MentionsAndUrl mentionsAndUrl = mentionsAndUrlList.get(i);
            String url = mentionsAndUrl.getProtocol();
            URLClickableSpan urlClickableSpan = new URLClickableSpan(url);
            spannableString.setSpan(urlClickableSpan, mentionsAndUrl.getStart(), mentionsAndUrl.getEnd(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return spannableString;
    }
}
