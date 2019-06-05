package com.inspur.imp.plugin.barcode.decoder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.funcode.decoder.inspuremmcloud.FunDecode;
import com.funcode.decoder.inspuremmcloud.FunDecodeHandler;
import com.funcode.decoder.inspuremmcloud.FunDecodeSurfaceView;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * Created by chenmch on 2018/11/16.
 */

public class PreviewDecodeActivity extends BaseActivity implements FunDecodeHandler {
    private FunDecode mDecode = null;
    private FunDecodeSurfaceView mDecodeView = null;
    private RangeView mRangeView;
    private int surface_ready = 0;
    private Button torchBtn;
    private boolean isTorchOn = false;
    private TextView lampText;
    private Rect frameRect;

    @Override
    public void onCreate() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //全屏显示
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setNavigationBarColor(ContextCompat.getColor(MyApplication.getInstance(), android.R.color.black));
        }
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, Permissions.CAMERA, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                setContentView(R.layout.activity_preview_decode);
                initView();
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(PreviewDecodeActivity.this, PermissionRequestManagerUtils.getInstance().getPermissionToast(PreviewDecodeActivity.this, permissions));
                finish();
            }

        });
    }

    @Override
    public int getLayoutResId() {
        return 0;
    }

    protected int getStatusType() {
        return STATUS_NO_SET;
    }


    private void initView() {
        mRangeView = (RangeView) findViewById(Res.getWidgetID("rv"));
        setRangeView();
        mDecode = (FunDecode) findViewById(Res.getWidgetID("fd"));
        mDecode.ZoomShow(0);
        mDecodeView = mDecode.getDecodeView();
        mDecodeView.setListener(this);
        torchBtn = (Button) findViewById(Res.getWidgetID("bt_torch"));
        lampText = (TextView) findViewById(Res.getWidgetID("tv_lamp"));

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.getInstance().attachBaseContext(newBase));
    }


    private void setRangeView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        int screenLittleSize = screenWidth < screenHeight ? screenWidth : screenHeight;
        int frameRectWidth = (int) (screenLittleSize * 0.6);
        int frameRectHeight = frameRectWidth;
        int frameRectLeftOffset = (screenWidth - frameRectWidth) / 2;
        int frameRectTopOffset = (int) ((screenHeight - frameRectHeight) / 2);
        int frameRectRightOffset = frameRectLeftOffset + frameRectWidth;
        int frameRectBottomOffset = frameRectTopOffset + frameRectHeight;
        frameRect = new Rect(frameRectLeftOffset, frameRectTopOffset, frameRectRightOffset,
                frameRectBottomOffset);
        mRangeView.setRange(frameRect);
    }


    @Override
    public void GetResult(Object object) {
        // TODO Auto-generated method stub
        //mRes_text.setText((String)(object));
        mDecodeView.stopScan();
        mDecodeView.stopPreview();
        String result = "";
        try {
            result = new String((byte[]) (object), "UTF-8");
            result = result.replaceAll("[\\t\\n\\r]", "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
        Intent intent = new Intent();
        if (StringUtils.isBlank(result)) {
            result = getString(Res.getStringID("can_not_recognize"));
            intent.putExtra("isDecodeSuccess", false);
        } else {
            intent.putExtra("isDecodeSuccess", true);
        }
        intent.putExtra("msg", result);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void SurfaceReady() {
        surface_ready = 1;
        //mDecode.setFlash("torch");
        mDecode.setZoomLevel(1);
        mDecodeView.startScan();
        int previewWidth = mDecodeView.getPreviewWidth();
        int previewHeight = mDecodeView.getPreviewHeight();
        float ratioWidth=(float)previewWidth*1.0f/mDecodeView.getWidth();
        float ratioHeight =(float)previewHeight*1.0f/mDecodeView.getHeight();
        Rect rangeRect = new Rect();
        rangeRect.left = (int)(ratioWidth*frameRect.left)-50;
        rangeRect.top = (int)(ratioHeight*frameRect.top)-50;
        rangeRect.right = (int)(ratioWidth*frameRect.right)+50;
        rangeRect.bottom = (int)(ratioHeight*frameRect.bottom)+50;
        rangeRect.left=rangeRect.left-rangeRect.left%4;
        rangeRect.top=rangeRect.top-rangeRect.top%4;
        rangeRect.right=rangeRect.right-rangeRect.right%4;
        rangeRect.bottom=rangeRect.bottom-rangeRect.bottom%4;

        mDecodeView.setRange(rangeRect);
        //Set Zoom Component visible/invisible. 1: visible, 0: invisible
        //mDecode.ZoomShow(0);

        //Set Light On/Off.  "torch" for light on, "off" for light off.
        //mDecode.setFlash("torch");

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_close:
                finish();
                break;
            case R.id.bt_torch:
                if (isTorchOn) {
                    isTorchOn = false;
                    lampText.setText(Res.getStringID("turn_on_light"));
                    torchBtn.setBackgroundResource(Res.getDrawableID("imp_lamp_off"));
                    mDecode.setFlash("off");
                } else {
                    isTorchOn = true;
                    lampText.setText(Res.getStringID("turn_off_light"));
                    torchBtn.setBackgroundResource(Res.getDrawableID("imp_lamp_on"));
                    mDecode.setFlash("torch");
                }
                break;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDecodeView != null) {
            mDecodeView.stopScan();
            mDecodeView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDecodeView != null) {
            if (surface_ready == 0) {
                mDecodeView.setVisibility(View.VISIBLE);
            } else {
                mDecodeView.setVisibility(View.VISIBLE);
                mDecodeView.startScan();
            }
        }
    }

    private void startScanQrCode() {
        if (mDecodeView != null) {
            if (surface_ready == 0) {
                mDecodeView.setVisibility(View.VISIBLE);
            } else {
                mDecodeView.setVisibility(View.VISIBLE);
                mDecodeView.startScan();
            }
        }
    }

}
