package com.inspur.emmcloud.basemodule.util.imageedit;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.compressor.Compressor;
import com.inspur.emmcloud.basemodule.util.imageedit.core.IMGMode;
import com.inspur.emmcloud.basemodule.util.imageedit.core.IMGText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by felix on 2017/11/14 下午2:26.
 */

public class IMGEditActivity extends IMGEditBaseActivity {


    public static final String EXTRA_IMAGE_PATH = "IMAGE_PATH";

    public static final String EXTRA_IS_COVER_ORIGIN = "IS_COVER_ORIGIN_IMG";
    public static final String EXTRA_ENCODING_TYPE = "IMAGE_ENCODING_TYPE";
    public static final String OUT_FILE_PATH = "OUT_FILE_PATH";
    boolean isHaveEdit = false;
    private int encodingType = 0;
    private String originFilePath = "";

    @Override
    public Bitmap getBitmap() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }
        originFilePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        if (originFilePath == null) {
            return null;
        }
        File file = new File(originFilePath);
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
        isHaveEdit = true;
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onDoneClick() {
        if (isHaveEdit) {
            boolean isCoverOriginImg = getIntent().getBooleanExtra(EXTRA_IS_COVER_ORIGIN, false);
            File saveFile = null;
            if (isCoverOriginImg) {
                saveFile = new File(originFilePath);
            } else {
                String dirPath = MyAppConfig.LOCAL_IMG_CREATE_PATH;
                File dir = new File(dirPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                saveFile = new File(dirPath, System.currentTimeMillis() + ".png");
            }

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
                setResult(Activity.RESULT_OK, intent);


            } else {
                setResult(Activity.RESULT_CANCELED);
            }
        } else {
            Intent intent = new Intent();
            intent.putExtra(OUT_FILE_PATH, originFilePath);
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void onDoneClickInSystemStorage() {
        if (isHaveEdit) {
            boolean isCoverOriginImg = getIntent().getBooleanExtra(EXTRA_IS_COVER_ORIGIN, false);
            File saveFile = null;
            Intent intent = new Intent();
            Bitmap bitmap = mImgView.saveBitmap();
            if (isCoverOriginImg) {
                saveFile = new File(originFilePath);
                if (saveFile.exists()) {
                    saveFile.delete();
                }
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
                    intent.putExtra(OUT_FILE_PATH, saveFile.getAbsolutePath());
                    setResult(Activity.RESULT_OK, intent);
                } else {
                    setResult(Activity.RESULT_CANCELED);
                }
            } else {
                if (bitmap != null) {
                    ContentResolver cr = getContentResolver();
                    String insertImage = MediaStore.Images.Media.insertImage(cr, bitmap, System.currentTimeMillis() + ".png", null);
                    intent.putExtra(OUT_FILE_PATH, insertImage);
                    setResult(Activity.RESULT_OK, intent);
                } else {
                    setResult(Activity.RESULT_CANCELED);
                }
            }
        } else {
            Intent intent = new Intent();
            intent.putExtra(OUT_FILE_PATH, originFilePath);
            setResult(Activity.RESULT_OK, intent);
        }
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
