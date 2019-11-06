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
    static volatile EmotionUtil instance;
    private Context context;
    public static final String ee_1 = "[微笑]";
    public static final String ee_2 = "[撇嘴]";
    public static final String ee_3 = "[色]";
    public static final String ee_4 = "[得意]";
    public static final String ee_5 = "[流泪]";
    public static final String ee_6 = "[害羞]";
    public static final String ee_7 = "[瞌睡]";
    public static final String ee_8 = "[发怒]";
    public static final String ee_9 = "[惊恐]";
    public static final String ee_10 = "[调皮]";
    public static final String ee_11 = "[呲牙]";
    public static final String ee_12 = "[略尴尬]";
    public static final String ee_13 = "[偷笑]";
    public static final String ee_14 = "[了然于胸]";
    public static final String ee_15 = "[奋斗]";
    public static final String ee_16 = "[疑问]";
    public static final String ee_17 = "[晕]";
    public static final String ee_18 = "[再见]";
    public static final String ee_19 = "[抠鼻]";
    public static final String ee_20 = "[鼓掌]";
    public static final String ee_21 = "[坏笑]";
    public static final String ee_22 = "[鄙视]";
    public static final String ee_23 = "[笑哭]";
    public static final String ee_24 = "[嘿哈]";
    public static final String ee_25 = "[捂脸]";
    public static final String ee_26 = "[斜眼笑]";
    public static final String ee_27 = "[机智]";
    public static final String ee_28 = "[咖啡]";
    public static final String ee_29 = "[吃饭]";
    public static final String ee_30 = "[抱抱]";
    public static final String ee_31 = "[玫瑰]";
    public static final String ee_32 = "[玫瑰凋谢]";
    public static final String ee_33 = "[爱心]";
    public static final String ee_34 = "[蛋糕]";
    public static final String ee_35 = "[月亮]";
    public static final String ee_36 = "[太阳]";
    public static final String ee_37 = "[庆祝]";
    public static final String ee_38 = "[欢庆]";
    public static final String ee_39 = "[礼物]";
    public static final String ee_40 = "[面包]";
    public static final String ee_41 = "[强]";
    public static final String ee_42 = "[NO]";
    public static final String ee_43 = "[OK]";
    public static final String ee_44 = "[666]";
    public static final String ee_45 = "[爱你]";
    public static final String ee_46 = "[拍手]";
    public static final String ee_47 = "[拳头]";
    public static final String ee_48 = "[握手]";
    public static final String ee_49 = "[耶]";
    private static final int MAX_COUNT = 49;

    private static final Map<Pattern, Integer> emoticons = new HashMap<>();
    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    public EmotionUtil(Context context) {
        this.context = context;
    }

    public static EmotionUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (EmotionUtil.class) {
                if (instance == null) {
                    instance = new EmotionUtil(context);
                }
            }
        }
        return instance;
    }

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
        addPattern(emoticons, ee_35, R.drawable.ee_35);
        addPattern(emoticons, ee_36, R.drawable.ee_36);
        addPattern(emoticons, ee_37, R.drawable.ee_37);
        addPattern(emoticons, ee_38, R.drawable.ee_38);
        addPattern(emoticons, ee_39, R.drawable.ee_39);
        addPattern(emoticons, ee_40, R.drawable.ee_40);
        addPattern(emoticons, ee_41, R.drawable.ee_41);
        addPattern(emoticons, ee_42, R.drawable.ee_42);
        addPattern(emoticons, ee_43, R.drawable.ee_43);
        addPattern(emoticons, ee_44, R.drawable.ee_44);
        addPattern(emoticons, ee_45, R.drawable.ee_45);
        addPattern(emoticons, ee_46, R.drawable.ee_46);
        addPattern(emoticons, ee_47, R.drawable.ee_47);
        addPattern(emoticons, ee_48, R.drawable.ee_48);
        addPattern(emoticons, ee_49, R.drawable.ee_49);
    }

    private static void addPattern(Map<Pattern, Integer> map, String smile,
                                   int resource) {
        map.put(Pattern.compile(Pattern.quote(smile)), resource);
    }

    public boolean addSmiles(Context context, Spannable spannable, float textSize) {
        boolean hasChanges = false;
        for (Map.Entry<Pattern, Integer> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(),
                        matcher.end(), ImageSpan.class)) {
                    if (spannable.getSpanStart(span) >= matcher.start()
                            && spannable.getSpanEnd(span) <= matcher.end())
                        spannable.removeSpan(span);
                    else {
                        set = false;
                        break;
                    }
                }
                if (set) {
                    hasChanges = true;
//                    spannable.setSpan(new CenterAlignImageSpan(context, entry.getValue(), 2), matcher.start(), matcher.end(),
//                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    EmotionSpan span = new EmotionSpan(context, entry.getValue(),
                            (int) (textSize * 1.4f), (int) (textSize * 1.4f));
                    span.setTranslateY(2);
                    spannable.setSpan(span,
                            matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return hasChanges;
    }

    public Spannable getSmiledText(CharSequence text, float textSize) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmiles(context, spannable, textSize);
        return spannable;
    }

    public boolean containsKey(String key) {
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
     * @return
     */
    public List<String> getExpressionRes() {
        List<String> resList = new ArrayList<>();
        for (int x = 1; x <= MAX_COUNT; x++) {
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
    public void deleteSingleEmojcon(ChatInputEdit mEditTextContent) {
        if (!StringUtils.isBlank(mEditTextContent.getText().toString())) {

            int selectionStart = mEditTextContent.getSelectionStart();// 获取光标的位置

            if (selectionStart > 0) {
                String body = mEditTextContent.getText().toString();
                String tempStr = body.substring(0, selectionStart);
                int i = tempStr.lastIndexOf("[");// 获取最后一个表情的位置
                if (i != -1) {
                    CharSequence cs = tempStr.substring(i, selectionStart);
                    if (containsKey(cs.toString()) && tempStr.endsWith("]"))
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
