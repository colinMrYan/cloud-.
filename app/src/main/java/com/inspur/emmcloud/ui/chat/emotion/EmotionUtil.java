package com.inspur.emmcloud.ui.chat.emotion;

import android.content.Context;
import android.text.Spannable;
import android.text.style.ImageSpan;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.widget.ChatInputEdit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmotionUtil {
    public static final String ee_1 = "[微笑]";
    public static final String ee_2 = "[调皮]";
    public static final String ee_3 = "[呲牙]";
    public static final String ee_4 = "[偷笑]";
    public static final String ee_5 = "[撇嘴]";
    public static final String ee_6 = "[害羞]";
    public static final String ee_7 = "[奋斗]";
    public static final String ee_8 = "[再见]";
    public static final String ee_9 = "[抠鼻]";
    public static final String ee_10 = "[疑问]";
    public static final String ee_11 = "[坏笑]";
    public static final String ee_12 = "[鄙视]";
    public static final String ee_13 = "[捂脸]";
    public static final String ee_14 = "[奸笑]";
    public static final String ee_15 = "[机智]";
    public static final String ee_16 = "[嘿哈]";
    public static final String ee_17 = "[破涕为笑]";
    public static final String ee_18 = "[流泪]";
    public static final String ee_19 = "[发怒]";
    public static final String ee_20 = "[色]";
    public static final String ee_21 = "[睡]";
    public static final String ee_22 = "[晕]";
    public static final String ee_23 = "[鼓掌]";
    public static final String ee_24 = "[惊恐]";
    public static final String ee_25 = "[得意]";
    public static final String ee_26 = "[礼物]";
    public static final String ee_27 = "[玫瑰]";
    public static final String ee_28 = "[爱心]";
    public static final String ee_29 = "[咖啡]";
    public static final String ee_30 = "[强]";
    public static final String ee_31 = "[太阳]";
    public static final String ee_32 = "[OK]";
    public static final String ee_33 = "[蛋糕]";
    public static final String ee_34 = "[拥抱]";

    private static final Map<Pattern, Integer> emoticons = new HashMap<>();
    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    static {
        addPattern(emoticons, ee_1, R.drawable.ee_1);
        addPattern(emoticons, ee_2, R.drawable.ee_2);
        addPattern(emoticons, ee_3, R.drawable.ee_3);
        addPattern(emoticons, ee_4, R.drawable.ee_4);
        addPattern(emoticons, ee_5, R.drawable.ee_5);
        addPattern(emoticons, ee_6, R.drawable.ee_6);
        addPattern(emoticons, ee_7, R.drawable.ee_7);
        addPattern(emoticons, ee_8, R.drawable.ee_8);
        addPattern(emoticons, ee_9, R.drawable.ee_9);
        addPattern(emoticons, ee_10, R.drawable.ee_10);
        addPattern(emoticons, ee_11, R.drawable.ee_11);
        addPattern(emoticons, ee_12, R.drawable.ee_12);
        addPattern(emoticons, ee_13, R.drawable.ee_13);
        addPattern(emoticons, ee_14, R.drawable.ee_14);
        addPattern(emoticons, ee_15, R.drawable.ee_15);
        addPattern(emoticons, ee_16, R.drawable.ee_16);
        addPattern(emoticons, ee_17, R.drawable.ee_17);
        addPattern(emoticons, ee_18, R.drawable.ee_18);
        addPattern(emoticons, ee_19, R.drawable.ee_19);
        addPattern(emoticons, ee_20, R.drawable.ee_20);
        addPattern(emoticons, ee_21, R.drawable.ee_21);
        addPattern(emoticons, ee_22, R.drawable.ee_22);
        addPattern(emoticons, ee_23, R.drawable.ee_23);
        addPattern(emoticons, ee_24, R.drawable.ee_24);
        addPattern(emoticons, ee_25, R.drawable.ee_25);
        addPattern(emoticons, ee_26, R.drawable.ee_26);
        addPattern(emoticons, ee_27, R.drawable.ee_27);
        addPattern(emoticons, ee_28, R.drawable.ee_28);
        addPattern(emoticons, ee_29, R.drawable.ee_29);
        addPattern(emoticons, ee_30, R.drawable.ee_30);
        addPattern(emoticons, ee_31, R.drawable.ee_31);
        addPattern(emoticons, ee_32, R.drawable.ee_32);
        addPattern(emoticons, ee_33, R.drawable.ee_33);
        addPattern(emoticons, ee_34, R.drawable.ee_34);
    }

    private static void addPattern(Map<Pattern, Integer> map, String smile,
                                   int resource) {
        map.put(Pattern.compile(Pattern.quote(smile)), resource);
    }

    public static boolean addSmiles(Context context, Spannable spannable) {
        boolean hasChanges = false;
        for (Map.Entry<Pattern, Integer> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(),
                        matcher.end(), ImageSpan.class))
                    if (spannable.getSpanStart(span) >= matcher.start()
                            && spannable.getSpanEnd(span) <= matcher.end())
                        spannable.removeSpan(span);
                    else {
                        set = false;
                        break;
                    }
                if (set) {
                    hasChanges = true;
                    spannable.setSpan(new ImageSpan(context, entry.getValue()),
                            matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return hasChanges;
    }

    public static Spannable getSmiledText(Context context, CharSequence text) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmiles(context, spannable);
        return spannable;
    }

    public static boolean containsKey(String key) {
        boolean b = false;
        for (Map.Entry<Pattern, Integer> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(key);
            if (matcher.find()) {
                b = true;
                break;
            }
        }
        return b;
    }

    /**
     * 获取所有表情资源
     *
     * @param getSum
     * @return
     */
    public static List<String> getExpressionRes(int getSum) {
        List<String> resList = new ArrayList<>();
        for (int x = 1; x <= getSum; x++) {
            String filename = "ee_" + x;

            resList.add(filename);

        }
        return resList;
    }

    /**
     * 表情删除键操作
     *
     * @param mEditTextContent
     */
    public static void deleteSingleEmojcon(ChatInputEdit mEditTextContent) {
        if (!StringUtils.isBlank(mEditTextContent.getText().toString())) {

            int selectionStart = mEditTextContent.getSelectionStart();// 获取光标的位置

            if (selectionStart > 0) {
                String body = mEditTextContent.getText().toString();
                String tempStr = body.substring(0, selectionStart);
                int i = tempStr.lastIndexOf("[");// 获取最后一个表情的位置
                if (i != -1) {
                    CharSequence cs = tempStr.substring(i, selectionStart);
                    if (EmotionUtil.containsKey(cs.toString()) && tempStr.endsWith("]"))
                        mEditTextContent.getEditableText().delete(i, selectionStart);
                    else
                        mEditTextContent.getEditableText().delete(selectionStart - 1,
                                selectionStart);
                } else {
                    mEditTextContent.getEditableText().delete(selectionStart - 1, selectionStart);
                }
            }
        }
    }
}
