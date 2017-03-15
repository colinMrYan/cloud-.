package com.inspur.emmcloud.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.EditText;

import com.inspur.emmcloud.bean.MentionBean;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2017/3/9.
 */
public class ChatInputEdit extends EditText {

    /**
     * mentionList
     */
    private ArrayList<MentionBean> mentionBeenList = new ArrayList<MentionBean>();

    /**
     * 是不是要打开mentionList功能，刚加载布局的时候不能打开
     */
    private boolean isOpen = false;

    public ChatInputEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        // TODO Auto-generated method stub
        if (isOpen) {
            for (int i = 0; i < mentionBeenList.size(); i++) {
                int mentionStart = mentionBeenList.get(i).getMentionStart();
                int mentionEnd = mentionBeenList.get(i).getMentioinEnd();
                if (selEnd > mentionStart && selEnd < mentionEnd) {
                    setSelection(mentionEnd);
                }
            }
            isOpen = false;
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    /**
     * 传入MentionList
     * @param mentionBeenList
     */
    public void setMentionBeenList(ArrayList<MentionBean> mentionBeenList) {
        this.mentionBeenList = mentionBeenList;
    }

    /**
     * mention开关
     * @param isOpen
     */
    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
}

