package com.inspur.emmcloud.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
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
     * 是不是要打开mentionList,等布局加载完成之后再打开
     */
    private boolean isOpen = false;

    public ChatInputEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init();
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
                boolean needToMoveSelection = (selStart > mentionStart  && selStart < mentionEnd) ||
                        (selEnd >mentionStart && selEnd < mentionEnd) || (selStart <= mentionStart && selEnd >= mentionEnd);
                if (needToMoveSelection) {
                    setSelection(mentionEnd);
                }
            }
            isOpen = false;
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    /**
     * 传入MentionList
     *
     * @param mentionBeenList
     */
    public void setMentionBeenList(ArrayList<MentionBean> mentionBeenList) {
        this.mentionBeenList = mentionBeenList;
    }

    /**
     * mention开关
     *
     * @param isOpen
     */
    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

    /**
     * 初始化
     */
    private void init() {
        this.setCustomSelectionActionModeCallback(new ActionModeCallbackInterceptor());
        //这里修改长按事件
//        this.setLongClickable(false);
    }


    /**
     * Prevents the action bar (top horizontal bar with cut, copy, paste, etc.)
     * from appearing by intercepting the callback that would cause it to be
     * created, and returning false.
     */
    private class ActionModeCallbackInterceptor implements ActionMode.Callback {
        private final String TAG = ChatInputEdit.class.getSimpleName();


        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }}

