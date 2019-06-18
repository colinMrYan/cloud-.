package com.inspur.emmcloud.basemodule.util.compressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created on : June 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ImageUtil {

    private ImageUtil() {

    }

    static File compressImage(File imageFile, int reqWidth, int reqHeight, Bitmap.CompressFormat compressFormat, int quality, String destinationPath) throws IOException {
        FileOutputStream fileOutputStream = null;
        File file = new File(destinationPath).getParentFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            // write the compressed bitmap at the destination specified by destinationPath.
            Bitmap bitmap = decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight);
            File outFile = new File(destinationPath);
            if (outFile.exists()) {
                outFile.delete();
            }
            fileOutputStream = new FileOutputStream(destinationPath);
            bitmap.compress(compressFormat, quality, fileOutputStream);
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }

        return new File(destinationPath);
    }

    static Bitmap decodeSampledBitmapFromFile(File imageFile, int reqWidth, int reqHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap scaledBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        //check the rotation of the image and display it properly
        int orientation = 0;
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Matrix matrix = new Matrix();
        if (orientation == 6) {
            matrix.postRotate(90);
        } else if (orientation == 3) {
            matrix.postRotate(180);
        } else if (orientation == 8) {
            matrix.postRotate(270);
        }
        if (scaledBitmap.getWidth() > reqWidth || scaledBitmap.getHeight() > reqHeight) {
            float scaleW = (float) reqWidth / scaledBitmap.getWidth();
            float scaleH = (float) reqHeight / scaledBitmap.getHeight();
            float scale = scaleW < scaleH ? scaleW : scaleH;
            //宽高等比例缩放
            matrix.postScale(scale, scale);
        }
        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        return scaledBitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            while ((height / inSampleSize) >= reqHeight || (width / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
            LogUtils.jasonDebug("inSampleSize=" + inSampleSize);
            //当bitmap压缩到最后一次时能用Matrix就用
            if (inSampleSize >= 2 && (height / inSampleSize * 2 < MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE) || (width / inSampleSize * 2 < MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE)) {
                inSampleSize = inSampleSize / 2;
            }
        }
        LogUtils.jasonDebug("inSampleSize=" + inSampleSize);
        return inSampleSize;
    }
}