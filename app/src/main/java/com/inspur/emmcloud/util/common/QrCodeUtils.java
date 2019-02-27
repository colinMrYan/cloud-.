package com.inspur.emmcloud.util.common;

/**
 * Created by yufuchang on 2019/1/15.
 * 二维码生成工具类
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.config.MyAppConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.xutils.common.util.IOUtil.copy;

/**
 * 生成二维码Bitmap工具类
 */
public class QrCodeUtils {

    private static final int IO_BUFFER_SIZE = 1024;
    private static int IMAGE_HALF_WIDTH = 50;//宽度值，影响中间图片大小

    /**
     * 生成二维码，默认大小为500*500
     *
     * @param qrcodeContent 需要生成二维码的文字、网址等
     * @return bitmap
     */
    public static Bitmap createQRCode(String qrcodeContent) {
        return createQRCode(qrcodeContent, 500);
    }

    /**
     * 生成二维码
     *
     * @param text 需要生成二维码的文字、网址等
     * @param size 需要生成二维码的大小（）
     * @return bitmap
     */
    public static Bitmap createQRCode(String text, int size) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, size, size, hints);
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * size + x] = 0xff000000;
                    } else {
                        pixels[y * size + x] = 0xffffffff;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            saveBitmap(bitmap);
            return bitmap;
        } catch (Exception e) {
            LogUtils.YfcDebug("异常：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成带logo的二维码，默认二维码的大小为500，logo为二维码的1/5
     *
     * @param qrcodeContent 需要生成二维码的文字、网址等
     * @param mBitmap       logo文件
     * @return bitmap
     */
    public static Bitmap createQRCodeWithLogo(String qrcodeContent, Bitmap mBitmap) {
        return createQRCodeWithLogo(qrcodeContent, 500, mBitmap);
    }

    /**
     * 根据uri获取带logo的二维码图片
     *
     * @param qrcodeContent
     * @param size
     * @param pathUri
     * @return
     */
    public static Bitmap createQRCodeWithLogo(String qrcodeContent, int size, String pathUri) {
        Bitmap bitmap = getLocalOrNetBitmap(pathUri);
        return createQRCodeWithLogo(qrcodeContent, size, bitmap);
    }

    /**
     * 生成带logo的二维码，logo默认为二维码的1/5
     *
     * @param qrcodeContent 需要生成二维码的文字、网址等
     * @param size          需要生成二维码的大小（）
     * @param mBitmap       logo文件
     * @return bitmap
     */
    public static Bitmap createQRCodeWithLogo(String qrcodeContent, int size, Bitmap mBitmap) {
        try {
            IMAGE_HALF_WIDTH = size / 10;
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            /*
             * 设置容错级别，默认为ErrorCorrectionLevel.L
             * 因为中间加入logo所以建议你把容错级别调至H,否则可能会出现识别不了
             */
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            //设置空白边距的宽度
            hints.put(EncodeHintType.MARGIN, 1); //default is 4
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(qrcodeContent,
                    BarcodeFormat.QR_CODE, size, size, hints);

            int width = bitMatrix.getWidth();//矩阵高度
            int height = bitMatrix.getHeight();//矩阵宽度
            int halfW = width / 2;
            int halfH = height / 2;

            Matrix m = new Matrix();
            float sx = (float) 2 * IMAGE_HALF_WIDTH / mBitmap.getWidth();
            float sy = (float) 2 * IMAGE_HALF_WIDTH
                    / mBitmap.getHeight();
            m.setScale(sx, sy);
            //设置缩放信息
            //将logo图片按martix设置的信息缩放
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                    mBitmap.getWidth(), mBitmap.getHeight(), m, false);

            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (x > halfW - IMAGE_HALF_WIDTH && x < halfW + IMAGE_HALF_WIDTH
                            && y > halfH - IMAGE_HALF_WIDTH
                            && y < halfH + IMAGE_HALF_WIDTH) {
                        //该位置用于存放图片信息
                        //记录图片每个像素信息
                        pixels[y * width + x] = mBitmap.getPixel(x - halfW
                                + IMAGE_HALF_WIDTH, y - halfH + IMAGE_HALF_WIDTH);
                    } else {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * size + x] = 0xff000000;
                        } else {
                            pixels[y * size + x] = 0xffffffff;
                        }
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 生成二维码Bitmap  此方法与上面的createQRCodeWithLogo方法效果一样（设置Bitmap两种写方法）
     *
     * @param context
     * @param data    文本内容
     * @param logoBm  二维码中心的Logo图标（可以为null）
     * @return 合成后的bitmap
     */
    public static Bitmap createQRImage(Context context, String data, Bitmap logoBm) {
        try {
            if (data == null || "".equals(data)) {
                return null;
            }
            int widthPix = ((Activity) context).getWindowManager().getDefaultDisplay()
                    .getWidth();
            widthPix = widthPix / 5 * 3;
            int heightPix = widthPix;

            //配置参数
            Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //容错级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            //设置空白边距的宽度
            hints.put(EncodeHintType.MARGIN, 1); //default is 4

            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);

            if (logoBm != null) {
                bitmap = addLogo(bitmap, logoBm);
            }
            return bitmap;
            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            //return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }
        if (logo == null) {
            return src;
        }
        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }
        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }

    /**
     * 从网络或本地路径读取bitmap
     *
     * @param url
     * @return
     */
    public static Bitmap getLocalOrNetBitmap(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), IO_BUFFER_SIZE);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 保存方法
     */
    private static String saveBitmap(Bitmap bitmap) {
        File dir = new File(MyAppConfig.LOCAL_IMG_CREATE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(MyAppConfig.LOCAL_IMG_CREATE_PATH, MyApplication.getInstance().getTanent() + MyApplication.getInstance().getUid() + "qrcode.png");
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
            out.flush();
            out.close();
            return "file://" + file.getAbsolutePath();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if (file.exists()) {
                file.delete();
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        return null;
    }
}

