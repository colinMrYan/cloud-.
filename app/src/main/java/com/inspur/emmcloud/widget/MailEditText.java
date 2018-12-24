package com.inspur.emmcloud.widget;

import android.content.Context;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.inspur.emmcloud.util.common.LogUtils;

/**
 * Created by libaochao on 2018/12/24.
 */


public class MailEditText extends android.support.v7.widget.AppCompatEditText {

    public MailEditText(Context context) {
        super(context);
    }


    public MailEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MailEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged( text, start, lengthBefore, lengthAfter );
    }

    /**
     * 设置最后状态*/
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged( selStart, selEnd );
        //保证光标始终在最后面
        if(selStart==selEnd){//防止不能多选
            setSelection(getText().length());
        }
    }

    @Override
    public void setKeyListener(KeyListener input) {
        super.setKeyListener( input );

    }

    @Override
    public void setOnKeyListener(OnKeyListener l) {
        new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //String content = mRecipientEditText.getText().toString();
                    //int length = content.length();
                    //content.endsWith("-");
                    LogUtils.LbcDebug("lbc11111111111");
                }
                return false;
            }
        };
    }

}
