package com.inspur.emmcloud.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.inspur.emmcloud.bean.chat.InsertModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yufuchang on 2017/3/9.
 */
public class ChatInputEdit extends AppCompatEditText {

    private int size;
    private int maxLength = 2000;
    private List<InsertModel> insertModelList = new ArrayList<>();
    private static final int BACKGROUND_COLOR = Color.parseColor("#FFDEAD"); // 默认,话题背景高亮颜色
    private  Context mContext;
    private InputWatcher inputWatcher;

    public ChatInputEdit(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public ChatInputEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        if (isInEditMode())
            return;
        InputFilter[] filters = {new InputFilter.LengthFilter(maxLength)};
        setFilters(filters);
        size = DensityUtil.dip2px(context, 20);
        initView();
    }

    public void setInputWatcher(InputWatcher inputWatcher) {
        this.inputWatcher = inputWatcher;
    }

    public  int ParseIconResId(String name) {
        name = name.substring(1, name.length() - 1);
        int resId = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
        return resId;
    }

    /**
     * 插入图片
     *
     * @param name
     */
    public void insertIcon(String name) {
        String curString = getText().toString();
        if ((curString.length() + name.length()) > maxLength) {
            return;
        }
        Drawable drawable = ContextCompat.getDrawable(mContext, ParseIconResId(name));

        if (drawable == null)
            return;
        drawable.setBounds(0, 0, size, size);//这里设置图片的大小
        ImageSpan imageSpan = new ImageSpan(drawable);
        SpannableString spannableString = new SpannableString(name);
        spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        int index = Math.max(getSelectionStart(), 0);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getText());
        spannableStringBuilder.insert(index, spannableString);

        setText(spannableStringBuilder);
        setSelection(index + spannableString.length());
    }


    /**
     * 初始化控件,一些监听
     */
    private void initView() {
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (inputWatcher != null) {
                    inputWatcher.onTextChanged(s, start, before, count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                resolveDeleteSpecialStr();
            }
        });

        /**
         * 监听删除键 <br/>
         * 1.光标在话题后面,将整个话题内容删除 <br/>
         * 2.光标在普通文字后面,删除一个字符
         *
         */
        this.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {

                    int selectionStart = getSelectionStart();
                    int selectionEnd = getSelectionEnd();

                    /**
                     * 如果光标起始和结束不在同一位置,删除文本
                     */
                    if (selectionStart != selectionEnd) {
                        // 查询文本是否属于目标对象,若是移除列表数据
                        String tagetText = getText().toString().substring(
                                selectionStart, selectionEnd);
                        for (int i = 0; i < insertModelList.size(); i++) {
                            InsertModel object = insertModelList.get(i);
                            if (tagetText.equals(object.getInsertContent())) {
                                insertModelList.remove(object);
                            }
                        }
                        return false;
                    }


                    SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder) getText();
                    BackgroundColorSpan[] mSpans = getText().getSpans(0, spannableStringBuilder.length(), BackgroundColorSpan.class);
                    for (int i = 0; i < mSpans.length; i++) {
                        BackgroundColorSpan span = mSpans[i];
                        int spanStartPos = spannableStringBuilder.getSpanStart(span);
                        int spanEndPos = spannableStringBuilder.getSpanEnd(span);
                        if (selectionStart != 0 && selectionStart >= spanStartPos && selectionStart <= spanEndPos) {
                            // 选中话题
                            setSelection(spanStartPos, spanEndPos);
                        }
                    }
                }

                return false;
            }
        });
        this.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
    }

    /**
     * 监听光标的位置,若光标处于话题内容中间则移动光标到话题结束位置
     */
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (insertModelList == null || insertModelList.size() == 0)
            return;
        SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder) getText();
        BackgroundColorSpan[] mSpans = getText().getSpans(0, spannableStringBuilder.length(), BackgroundColorSpan.class);
        for (int i = 0; i < mSpans.length; i++) {
            BackgroundColorSpan span = mSpans[i];
            int spanStartPos = spannableStringBuilder.getSpanStart(span);
            int spanEndPos = spannableStringBuilder.getSpanEnd(span);
            if (selStart > spanStartPos && selStart <= spanEndPos) {
                // 选中话题
                setSelection(spanEndPos);
            }
        }
    }

    /**
     * @param isInputKeyWord 是否是输入关键字@插入mention
     * @param insertModel    插入对象
     */
    public void insertSpecialStr(boolean isInputKeyWord, InsertModel insertModel) {
        if (insertModel == null)
            return;

        String insertRule = insertModel.getInsertRule();
        String insertContent = insertModel.getInsertContent();
        String insertColor = insertModel.getInsertColor();
        if (TextUtils.isEmpty(insertRule) || TextUtils.isEmpty(insertContent))
            return;
        if (insertRule.equals("@")) {
            if (isInputKeyWord) {
                int index = getSelectionStart();
                Editable editable = getText();
                editable.delete(index - 1, index);
            }
        }
        //避免插入相同的数据
        for (InsertModel model : insertModelList) {
            if ((model.getInsertContent().replace(model.getInsertRule(), "")).equals(insertContent) && model.getInsertRule().equals(insertRule)) {
                //  Toast.makeText(mContext, "不可重复插入", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (insertRule.equals("@")) {
            insertContent = insertRule + insertContent;
        } else {
            insertContent = insertRule + insertContent + insertRule;
        }
        insertModel.setInsertContent(insertContent);
        insertModelList.add(insertModel);
        //将特殊字符插入到EditText 中显示
        int index = getSelectionStart();//光标位置
        Editable editable = getText();//原先内容
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(editable);
        spannableStringBuilder.insert(index, insertContent);
        BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(Color.parseColor(insertColor));
        spannableStringBuilder.setSpan(backgroundColorSpan, index, index + insertContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        Spanned htmlText = Html.fromHtml(String.format(String.format("<font color='%s'>" + insertContent + "</font>", insertColor)));
//        spannableStringBuilder.insert(index, htmlText);
        spannableStringBuilder.insert(index + insertContent.length(), " ");
        setText(spannableStringBuilder);
        setSelection(index + insertContent.length() + 1);
    }

    public String getRichContent(boolean isConvertUrl){
        SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder) getText();
        BackgroundColorSpan[] mSpans = getText().getSpans(0, spannableStringBuilder.length(), BackgroundColorSpan.class);
        for (int i = 0; i < mSpans.length; i++) {
            BackgroundColorSpan span = mSpans[i];
            int spanStartPos = spannableStringBuilder.getSpanStart(span);
            int spanEndPos = spannableStringBuilder.getSpanEnd(span);
            String keyword = spannableStringBuilder.subSequence(spanStartPos, spanEndPos).toString();
            if (keyword.startsWith("@")) {
                InsertModel insertModel = getMentionInsertMention(keyword);
                if (insertModel != null) {
                    spannableStringBuilder.replace(spanStartPos, spanEndPos, "[" + insertModel.getInsertContent() + "]" + getMentionProtoUtils(insertModel.getInsertId()));
                }

            }
        }
        String content = spannableStringBuilder.toString();
        if (isConvertUrl){
            Pattern pattern = Pattern.compile(Constant.PATTERN_URL);
            Matcher matcher = pattern.matcher(content);
            int offset = 0;
            while (matcher.find()) {
                String replaceUrl = matcher.group(0);
                StringBuilder sb = new StringBuilder(content);
                int replaceUrlBeginLocation = sb.indexOf(replaceUrl, offset);
                String matchedUrl = matcher.group(0);
                if (matchedUrl.startsWith("http://") || matchedUrl.startsWith("https://")) {
                    content = sb.replace(replaceUrlBeginLocation, replaceUrlBeginLocation + replaceUrl.length(), "[" + matcher.group(0) + "]" + "(" + matcher.group(0) + ")").toString();
                    offset = replaceUrlBeginLocation + replaceUrl.length() * 2 + 4;//4代表两个中小括号
                } else {
                    content = sb.replace(replaceUrlBeginLocation, replaceUrlBeginLocation + replaceUrl.length(), "[" + matcher.group(0) + "]" + "(http://" + matcher.group(0) + ")").toString();
                    offset = replaceUrlBeginLocation + replaceUrl.length() * 2 + 4 + 7;//4代表两个中小括号,7代表http://
                }
            }
        }
        return content;
    }

    /**
     * 获取富文文本内容
     */
    public String getRichContent() {
        return getRichContent(true);
    }

    /**
     * mentions字符串拼接
     * @param uid
     * @return
     */
    private String getMentionProtoUtils(String uid){
        return "(ecm-contact://"+uid+")";
    }

    /**
     * 根据keyword获取InsertModel
     *
     * @param keyword
     * @return
     */
    private InsertModel getMentionInsertMention(String keyword) {
        for (int i = 0; i < insertModelList.size(); i++) {
            InsertModel inertModel = insertModelList.get(i);
            if (inertModel.getInsertRule().equals("@")) {
                String insertContent = inertModel.getInsertContent();
                LogUtils.jasonDebug("insertContent=" + insertContent);
                if (insertContent.equals(keyword)) {
                    return inertModel;
                }

            }

        }
        return null;
    }


    /**
     * 获取特殊字符列表
     */
    public List<InsertModel> getRichInsertList() {

        return insertModelList;
    }


    /**
     * 删除缓存列表
     */
    private void resolveDeleteSpecialStr() {
        String tagetText = getText().toString();
        if (TextUtils.isEmpty(tagetText)) {
            insertModelList.clear();
            return;
        }
        for (int i = 0; i < insertModelList.size(); i++) {
            InsertModel object = insertModelList.get(i);
            if (tagetText.indexOf(object.getInsertContent()) == -1) {
                insertModelList.remove(object);
            }
        }
    }


    private boolean isRequest = false;

    public boolean isRequest() {
        return isRequest;
    }

    //是否可以点击滑动
    public void setIsRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }


    public int getEditTextMaxLength() {
        return maxLength;
    }

    //最大可输入长度
    public void setEditTextMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public interface InputWatcher {
        void onTextChanged(CharSequence s, int start, int before,
                           int count);
    }

}