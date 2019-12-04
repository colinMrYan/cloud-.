package com.inspur.emmcloud.basemodule.util.imageedit;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.util.imageedit.core.IMGText;
import com.inspur.emmcloud.basemodule.util.imageedit.view.IMGColorGroup;

/**
 * Created by felix on 2017/12/1 上午11:21.
 */

public class IMGTextEditDialog extends Dialog implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "IMGTextEditDialog";

    private EditText mEditText;

    private Callback mCallback;

    private IMGText mDefaultText;

    private IMGColorGroup mColorGroup;

    public IMGTextEditDialog(Context context, Callback callback) {
        super(context, R.style.ImageTextDialog);
        setContentView(R.layout.plugin_camera_image_text_dialog);
        mCallback = callback;
        Window window = getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mColorGroup = (IMGColorGroup) findViewById(R.id.cg_colors);
        mColorGroup.setOnCheckedChangeListener(this);
        mEditText = (EditText) findViewById(R.id.et_text);

        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.tv_done).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mDefaultText != null) {
            mEditText.setText(mDefaultText.getText());
            mEditText.setTextColor(mDefaultText.getColor());
            if (!mDefaultText.isEmpty()) {
                mEditText.setSelection(mEditText.length());
            }
            mDefaultText = null;
        } else mEditText.setText("");
        mColorGroup.setCheckColor(mEditText.getCurrentTextColor());
    }

    public void setText(IMGText text) {
        mDefaultText = text;
    }

    public void reset() {
        setText(new IMGText(null, Color.WHITE));
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.tv_done) {
            onDone();
        } else if (vid == R.id.tv_cancel) {
            dismiss();
        }
    }

    private void onDone() {
        String text = mEditText.getText().toString();
        if (!TextUtils.isEmpty(text) && mCallback != null) {
            mCallback.onText(new IMGText(text, mEditText.getCurrentTextColor()));
        }
        dismiss();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mEditText.setTextColor(mColorGroup.getCheckColor());
    }

    private void fullScreenImmersive(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            view.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public interface Callback {

        void onText(IMGText text);
    }
}
