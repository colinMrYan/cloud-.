package com.inspur.emmcloud.basemodule.util.imageedit;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ViewSwitcher;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.util.imageedit.core.IMGMode;
import com.inspur.emmcloud.basemodule.util.imageedit.core.IMGText;
import com.inspur.emmcloud.basemodule.util.imageedit.view.IMGColorGroup;
import com.inspur.emmcloud.basemodule.util.imageedit.view.IMGView;

/**
 * Created by felix on 2017/12/5 下午3:08.
 */

abstract class IMGEditBaseActivity extends BaseFragmentActivity implements View.OnClickListener,
        IMGTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener,
        DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    public static final int OP_HIDE = -1;
    public static final int OP_NORMAL = 0;
    public static final int OP_CLIP = 1;
    public static final int OP_SUB_DOODLE = 0;
    public static final int OP_SUB_MOSAIC = 1;
    protected IMGView mImgView;
    private RadioGroup mModeGroup;
    private IMGColorGroup mColorGroup;
    private IMGTextEditDialog mTextDialog;
    private View mLayoutOpSub;
    private ViewSwitcher mOpSwitcher, mOpSubSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    public void onCreate() {
//        setNavigationBarColor(android.R.color.black);
        Bitmap bitmap = getBitmap();
        if (bitmap != null) {
            setContentView(R.layout.plugin_camera_image_edit_activity);
//            ImmersionBar.with(this).statusBarColor(R.color.black).navigationBarColor(R.color.black).init();
            initViews();
            mImgView.setImageBitmap(bitmap);
        } else finish();
    }


    private void initViews() {
        mImgView = (IMGView) findViewById(R.id.image_canvas);
        mModeGroup = (RadioGroup) findViewById(R.id.rg_modes);

        mOpSwitcher = (ViewSwitcher) findViewById(R.id.vs_op);
        mOpSubSwitcher = (ViewSwitcher) findViewById(R.id.vs_op_sub);

        mColorGroup = (IMGColorGroup) findViewById(R.id.cg_colors);
        mColorGroup.setOnCheckedChangeListener(this);

        mLayoutOpSub = findViewById(R.id.layout_op_sub);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.rb_doodle) {
            onModeClick(IMGMode.DOODLE);
        } else if (vid == R.id.btn_text) {
            onTextModeClick();
        } else if (vid == R.id.rb_mosaic) {
            onModeClick(IMGMode.MOSAIC);
        } else if (vid == R.id.btn_clip) {
            onModeClick(IMGMode.CLIP);
        } else if (vid == R.id.btn_undo) {
            onUndoClick();
        } else if (vid == R.id.tv_done) {
            onDoneClick();
        } else if (vid == R.id.tv_cancel) {
            onCancelClick();
        } else if (vid == R.id.ib_clip_cancel) {
            onCancelClipClick();
        } else if (vid == R.id.ib_clip_done) {
            onDoneClipClick();
        } else if (vid == R.id.tv_clip_reset) {
            onResetClipClick();
        } else if (vid == R.id.ib_clip_rotate) {
            onRotateClipClick();
        }
    }

    public void updateModeUI() {
        IMGMode mode = mImgView.getMode();
        switch (mode) {
            case DOODLE:
                mModeGroup.check(R.id.rb_doodle);
                setOpSubDisplay(OP_SUB_DOODLE);
                break;
            case MOSAIC:
                mModeGroup.check(R.id.rb_mosaic);
                setOpSubDisplay(OP_SUB_MOSAIC);
                break;
            case NONE:
                mModeGroup.clearCheck();
                setOpSubDisplay(OP_HIDE);
                break;
        }
    }

    public void onTextModeClick() {
        if (mTextDialog == null) {
            mTextDialog = new IMGTextEditDialog(this, this);
            mTextDialog.setOnShowListener(this);
            mTextDialog.setOnDismissListener(this);
        }
        mTextDialog.show();
    }

    @Override
    public final void onCheckedChanged(RadioGroup group, int checkedId) {
        onColorChanged(mColorGroup.getCheckColor());
    }

    public void setOpDisplay(int op) {
        if (op >= 0) {
            mOpSwitcher.setDisplayedChild(op);
        }
    }

    public void setOpSubDisplay(int opSub) {
        if (opSub < 0) {
            mLayoutOpSub.setVisibility(View.GONE);
        } else {
            mOpSubSwitcher.setDisplayedChild(opSub);
            mLayoutOpSub.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onShow(DialogInterface dialog) {
        mOpSwitcher.setVisibility(View.GONE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mOpSwitcher.setVisibility(View.VISIBLE);
    }

    public abstract Bitmap getBitmap();

    public abstract void onModeClick(IMGMode mode);

    public abstract void onUndoClick();

    public abstract void onCancelClick();

    public abstract void onDoneClick();

    public abstract void onCancelClipClick();

    public abstract void onDoneClipClick();

    public abstract void onResetClipClick();

    public abstract void onRotateClipClick();

    public abstract void onColorChanged(int checkedColor);

    @Override
    public abstract void onText(IMGText text);
}
