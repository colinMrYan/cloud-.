package com.inspur.emmcloud.util;

import android.text.SpannableStringBuilder;
import android.widget.EditText;

import com.inspur.emmcloud.widget.spans.ForeColorSpan;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandleMsgTextUtils {

    //原来逻辑，已放弃此方法
//    public static String handleMentionAndURL(String content,
//                                             List<String> userNameList, List<String> uidList
//    ) {
//        String temp = "";
//        temp = content;
//        if (userNameList.size() > 0) {
//            for (int i = 0; i < userNameList.size(); i++) {
//                temp = temp.replace(
//                        userNameList.get(i),
//                        "[" + userNameList.get(i) + "]"
//                                + ProtocolUtils.getMentionProtoUtils(uidList
//                                .get(i)));
//            }
//        }
////		Pattern pattern = Pattern.compile("(https?://)?\\w+(\\.\\w+)+(/\\S+)?");
//        Pattern pattern = Pattern.compile("(https?://)?[a-zA-Z0-9]*[a-zA-z]+[a-zA-Z0-9]*(\\.\\w+)+(/\\S+)?");
//        Matcher matcher = pattern.matcher(temp);
//        int offset = 0;
//        while (matcher.find()) {
//            String replaceUrl = matcher.group(0);
//            StringBuilder sb = new StringBuilder(temp);
//            int replaceUrlBeginLocation = sb.indexOf(replaceUrl, offset);
//            String matchedUrl = matcher.group(0);
//            if (matchedUrl.startsWith("http://") || matchedUrl.startsWith("https://")) {
//                temp = sb.replace(replaceUrlBeginLocation, replaceUrlBeginLocation + replaceUrl.length(), "[" + matcher.group(0) + "]" + "(" + matcher.group(0) + ")").toString();
//                offset = replaceUrlBeginLocation + replaceUrl.length() * 2 + 4;//4代表两个中小括号
//            } else {
//                temp = sb.replace(replaceUrlBeginLocation, replaceUrlBeginLocation + replaceUrl.length(), "[" + matcher.group(0) + "]" + "(http://" + matcher.group(0) + ")").toString();
//                offset = replaceUrlBeginLocation + replaceUrl.length() * 2 + 4 + 7;//4代表两个中小括号,7代表http://
//            }
//        }
//        LogUtils.YfcDebug("检查过Url后生成的temp：" + temp);
//        return temp;
//    }


    //为了避免同一名字如果一个是真正的@一个是写出来的@时逻辑source拼接问题
    public static String handleMentionAndURL(EditText editText, String content,
                                             List<String> userNameList, List<String> uidList
    ) {
        String temp = "";
            temp = content;
            StringBuilder mentionStringBuilder = new StringBuilder(content);
            ForeColorSpan[] mSpans = editText.getText().getSpans(0, editText.length(), ForeColorSpan.class);
            if ((mSpans != null) && (mSpans.length > 0) && userNameList.size() > 0) {
                int offset = 0;
                for (int i = 0; i < userNameList.size(); i++) {
                    SpannableStringBuilder spanStr = (SpannableStringBuilder) editText.getText();
                    int spanStart = spanStr.getSpanStart(mSpans[i]) + offset;
                    int spanEnd = spanStr.getSpanEnd(mSpans[i]) + offset;
                    String replaceStr =
                            "[" + userNameList.get(i) + "]"
                                    + ProtocolUtils.getMentionProtoUtils(uidList
                                    .get(i));
                    mentionStringBuilder.replace(spanStart,spanEnd,
                            replaceStr);
                    offset = offset + replaceStr.length() - userNameList.get(i).length();
                }
            }
            temp = mentionStringBuilder.toString();
//		Pattern pattern = Pattern.compile("(https?://)?\\w+(\\.\\w+)+(/\\S+)?");
            Pattern pattern = Pattern.compile("(https?://)?[a-zA-Z0-9]*[a-zA-z]+[a-zA-Z0-9]*(\\.\\w+)+(/\\S+)?");
            Matcher matcher = pattern.matcher(temp);
            int offset = 0;
            while (matcher.find()) {
                String replaceUrl = matcher.group(0);
                StringBuilder sb = new StringBuilder(temp);
                int replaceUrlBeginLocation = sb.indexOf(replaceUrl, offset);
                String matchedUrl = matcher.group(0);
                if (matchedUrl.startsWith("http://") || matchedUrl.startsWith("https://")) {
                    temp = sb.replace(replaceUrlBeginLocation, replaceUrlBeginLocation + replaceUrl.length(), "[" + matcher.group(0) + "]" + "(" + matcher.group(0) + ")").toString();
                    offset = replaceUrlBeginLocation + replaceUrl.length() * 2 + 4;//4代表两个中小括号
                } else {
                    temp = sb.replace(replaceUrlBeginLocation, replaceUrlBeginLocation + replaceUrl.length(), "[" + matcher.group(0) + "]" + "(http://" + matcher.group(0) + ")").toString();
                    offset = replaceUrlBeginLocation + replaceUrl.length() * 2 + 4 + 7;//4代表两个中小括号,7代表http://
                }
            }

        return temp;
    }

}
