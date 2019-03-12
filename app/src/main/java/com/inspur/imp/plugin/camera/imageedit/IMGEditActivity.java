package com.inspur.imp.plugin.camera.imageedit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.imp.plugin.camera.imageedit.core.IMGMode;
import com.inspur.imp.plugin.camera.imageedit.core.IMGText;
import com.inspur.imp.util.compressor.Compressor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by felix on 2017/11/14 下午2:26.
 */

public class IMGEditActivity extends IMGEditBaseActivity {


    public static final String EXTRA_IMAGE_PATH = "IMAGE_PATH";

    public static final String EXTRA_IMAGE_SAVE_DIR_PATH = "IMAGE_SAVE_DIR_PATH";
    public static final String EXTRA_ENCODING_TYPE = "IMAGE_ENCODING_TYPE";
    public static final String OUT_FILE_PATH = "OUT_FILE_PATH";
    private int encodingType = 0;


    @Override
    public void onCreated() {
        setNavigationBarColor(android.R.color.black);
    }

    @Override
    public Bitmap getBitmap() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }
        String filePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        if (filePath == null) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        try {
            encodingType = getIntent().getIntExtra(EXTRA_ENCODING_TYPE, 0);
            Bitmap bitmap = new Compressor(IMGEditActivity.this).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE).setCompressFormat((encodingType == 0) ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG).setQuality(100).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                    .compressToBitmap(file);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onText(IMGText text) {
        mImgView.addStickerText(text);
    }

    @Override
    public void onModeClick(IMGMode mode) {
        IMGMode cm = mImgView.getMode();
        if (cm == mode) {
            mode = IMGMode.NONE;
        }
        mImgView.setMode(mode);
        updateModeUI();

        if (mode == IMGMode.CLIP) {
            setOpDisplay(OP_CLIP);
        }
    }

    @Override
    public void onUndoClick() {
        IMGMode mode = mImgView.getMode();
        if (mode == IMGMode.DOODLE) {
            mImgView.undoDoodle();
        } else if (mode == IMGMode.MOSAIC) {
            mImgView.undoMosaic();
        }
    }

    @Override
    public void onCancelClick() {
        finish();
    }

    @Override
    public void onDoneClick() {
        String path = getIntent().getExtras().getString(EXTRA_IMAGE_SAVE_DIR_PATH, MyAppConfig.LOCAL_IMG_CREATE_PATH);
        if (!TextUtils.isEmpty(path)) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File saveFile = new File(path, System.currentTimeMillis() + ".png");
            if (saveFile.exists()) {
                saveFile.delete();
            }
            Bitmap bitmap = mImgView.saveBitmap();
            if (bitmap != null) {
                FileOutputStream fout = null;
                try {
                    fout = new FileOutputStream(saveFile.getAbsolutePath());
                    bitmap.compress((encodingType == 0) ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG, 100, fout);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (fout != null) {
                        try {
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Intent intent = new Intent();
                intent.putExtra(OUT_FILE_PATH, saveFile.getAbsolutePath());
                setResult(RESULT_OK, intent);
                finish();
                return;
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onCancelClipClick() {
        mImgView.cancelClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onDoneClipClick() {
        mImgView.doClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onResetClipClick() {
        mImgView.resetClip();
    }

    @Override
    public void onRotateClipClick() {
        mImgView.doRotate();
    }

    @Override
    public void onColorChanged(int checkedColor) {
        mImgView.setPenColor(checkedColor);
    }
}
