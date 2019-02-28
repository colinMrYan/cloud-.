package com.inspur.imp.plugin.barcode.decoder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.funcode.decoder.inspuremmcloud.FunDecode;
import com.funcode.decoder.inspuremmcloud.FunDecodeHandler;
import com.funcode.decoder.inspuremmcloud.FunDecodeSurfaceView;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.util.common.systool.permission.Permissions;
import com.inspur.imp.api.Res;

import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * Created by chenmch on 2018/11/16.
 */

public class PreviewDecodeActivity extends Activity implements FunDecodeHandler {
    private FunDecode mDecode = null;
    private FunDecodeSurfaceView mDecodeView = null;
    private RangeView mRangeView;
    private int surface_ready = 0;
    private Button torchBtn;
    private boolean isTorchOn = false;
    private TextView lampText;
    private Rect frameRect;
    private Rect rangeRect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, Permissions.CAMERA, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                setContentView(Res.getLayoutID("activity_preview_decode"));
                initView();
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(PreviewDecodeActivity.this, PermissionRequestManagerUtils.getInstance().getPermissionToast(PreviewDecodeActivity.this, permissions));
                finish();
            }

        });
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

    private void setRangeView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        int screenLittleSize = screenWidth < screenHeight ? screenWidth : screenHeight;
        int frameRectWidth = (int) (screenLittleSize * 0.6);
//        //长和宽必须是4的倍数
//        frameRectWidth = frameRectWidth - frameRectWidth % 4;
        int frameRectHeight = frameRectWidth;
        int frameRectLeftOffset = (screenWidth - frameRectWidth) / 2;
        int frameRectTopOffset = (int) ((screenHeight - frameRectHeight) / 2);
        frameRect = new Rect(frameRectLeftOffset, frameRectTopOffset, frameRectLeftOffset + frameRectWidth,
                frameRectTopOffset + frameRectHeight);
        mRangeView.setRange(frameRect);

        int rangeRectWidth = screenLittleSize;
        rangeRectWidth = rangeRectWidth - rangeRectWidth % 4;
        int rangeRectHeight = rangeRectWidth;
        int rangeRectLeftOffset = (screenWidth - rangeRectWidth) / 2;
        int rangeRectTopOffset = (int) ((screenHeight - rangeRectHeight) / 2.5);
        rangeRect = new Rect(rangeRectLeftOffset, rangeRectTopOffset, rangeRectLeftOffset + rangeRectWidth,
                rangeRectTopOffset + rangeRectHeight);
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
        mDecode.setZoomLevel(0.1);
        mDecodeView.startScan();
        mDecodeView.setRange(new Rect(0, 0, 0, 0));
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


        /*
        if (mDecodeView.mHandler !=  null) {
            //Set Rectangle in RangeView for indicator display.
            Rect r_view = new Rect();
            int rangeWidth = mRangeView.getWidth();
            int rangeHeight = mRangeView.getHeight();
            int rect_len = rangeWidth / 2;

            //Draw a rect  on the center of rnageView which with width and height : rect_len.
            r_view.left = rangeWidth / 2 - rect_len / 2;
            r_view.top = rangeHeight / 2 - rect_len / 2;
            r_view.right = rangeWidth / 2 + rect_len / 2;
            r_view.bottom = rangeHeight / 2 + rect_len / 2;

            mRangeView.setRectangle(r_view);
            mDecodeView.startScan();
            //2017/08/17 Range scan demo , only decode left,top 1/4 corner
            int prv_width = mDecodeView.getPreviewWidth();
            int prv_height = mDecodeView.getPreviewHeight();

            float ratio_w = (float) prv_width / rangeWidth;
            float ratio_h = (float) prv_height / rangeHeight;

            Rect r = new Rect();

            //Set the ScanRange base on RangeView indicator.
            r.left = (int) (ratio_w * r_view.left);
            r.top = (int) (ratio_h * r_view.top);
            r.right = (int) (ratio_w * r_view.right);
            r.bottom = (int) (ratio_h * r_view.bottom);
            mDecodeView.setRange(r);

            mDecode.setZoomLevel(0.1);
        }
        */

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
