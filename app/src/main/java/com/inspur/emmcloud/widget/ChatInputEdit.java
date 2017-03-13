package com.inspur.emmcloud.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by yufuchang on 2017/3/9.
 */
public class ChatInputEdit extends EditText {

    private ECMChatInputMenu ecmChatInputMenu;
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
        super.onSelectionChanged(selStart, selEnd);
    }

    public void setECMChatInputMenu(ECMChatInputMenu chatInputMenu){
        this.ecmChatInputMenu = chatInputMenu;
    }
}

