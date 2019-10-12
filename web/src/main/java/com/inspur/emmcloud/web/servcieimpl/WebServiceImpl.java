package com.inspur.emmcloud.web.servcieimpl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.support.v4.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.imagepicker.ImagePicker;
import com.inspur.emmcloud.basemodule.util.imagepicker.ui.ImageGridActivity;
import com.inspur.emmcloud.basemodule.util.mycamera.MyCameraActivity;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.componentservice.web.WebService;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.PluginMgr;
import com.inspur.emmcloud.web.plugin.barcode.ScanResultActivity;
import com.inspur.emmcloud.web.plugin.barcode.decoder.PreviewDecodeActivity;
import com.inspur.emmcloud.web.ui.ImpFragment;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by chenmch on 2019/6/3.
 */

public class WebServiceImpl implements WebService {

    @Override
    public Class getImpFragmentClass() {
        return ImpFragment.class;
    }

    @Override
    public void openCamera(final Activity activity, final String picPath, final int requestCode) {
        // 判断存储卡是否可以用，可用进行存储
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            PermissionRequestManagerUtils.getInstance().requestRuntimePermission(activity, Permissions.CAMERA,
                    new PermissionRequestCallback() {
                        @Override
                        public void onPermissionRequestSuccess(List<String> permissions) {
                            File appDir = new File(Environment.getExternalStorageDirectory(), "DCIM");
                            if (!appDir.exists()) {
                                appDir.mkdir();
                            }
                            Intent intent = new Intent();
                            intent.putExtra(MyCameraActivity.EXTRA_PHOTO_DIRECTORY_PATH, appDir.getAbsolutePath());
                            intent.putExtra(MyCameraActivity.EXTRA_PHOTO_NAME, picPath);
                            intent.setClass(activity, MyCameraActivity.class);
                            activity.startActivityForResult(intent, requestCode);
                        }

                        @Override
                        public void onPermissionRequestFail(List<String> permissions) {
                            ToastUtils.show(activity, PermissionRequestManagerUtils.getInstance()
                                    .getPermissionToast(activity, permissions));
                        }
                    });
        } else {
            ToastUtils.show(activity, R.string.baselib_sd_not_exist);
        }
    }

    @Override
    public void openScanCode(final Activity activity, final int requestCode) {
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(activity, Permissions.CAMERA,
                new PermissionRequestCallback() {
                    @Override
                    public void onPermissionRequestSuccess(List<String> permissions) {
                        Intent intent = new Intent();
                        intent.setClass(activity, PreviewDecodeActivity.class);
                        activity.startActivityForResult(intent, requestCode);
                    }

                    @Override
                    public void onPermissionRequestFail(List<String> permissions) {
                        ToastUtils.show(activity,
                                PermissionRequestManagerUtils.getInstance().getPermissionToast(activity, permissions));
                    }

                });
    }

    @Override
    public void openScanCode(final Fragment fragment, final int requestCode) {
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(fragment.getActivity(), Permissions.CAMERA,
                new PermissionRequestCallback() {
                    @Override
                    public void onPermissionRequestSuccess(List<String> permissions) {
                        Intent intent = new Intent();
                        intent.setClass(fragment.getActivity(), PreviewDecodeActivity.class);
                        fragment.startActivityForResult(intent, requestCode);
                    }

                    @Override
                    public void onPermissionRequestFail(List<String> permissions) {
                        ToastUtils.show(fragment.getActivity(), PermissionRequestManagerUtils.getInstance()
                                .getPermissionToast(fragment.getActivity(), permissions));
                    }
                });
    }

    @Override
    public void openGallery(Activity activity, int limit, int requestCode) {
        openGallery(activity, limit, requestCode, false);
    }

    @Override
    public void openGallery(Activity activity, int limit, int requestCode, boolean isSupportOrigin) {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setShowCamera(false); // 显示拍照按钮
        imagePicker.setCrop(false); // 允许裁剪（单选才有效）
        imagePicker.setSelectLimit(limit);
        imagePicker.setMultiMode(true);
        imagePicker.setSupportOrigin(isSupportOrigin);
        Intent intent = new Intent(activity, ImageGridActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 根据传入字符串和大小获取二维码
     *
     * @param qrString
     * @param qrSize
     * @return
     */
    @Override
    public Bitmap getQrCodeWithContent(String qrString, int qrSize) {
        // TODO Auto-generated method stub
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, 2);
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(qrString,
                    BarcodeFormat.QR_CODE, qrSize, qrSize, hints);
            int[] pixels = new int[qrSize * qrSize];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < qrSize; y++) {
                for (int x = 0; x < qrSize; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * qrSize + x] = 0xff000000;
                    } else {
                        pixels[y * qrSize + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(qrSize, qrSize,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, qrSize, 0, 0, qrSize, qrSize);
            // 显示到一个ImageView上面
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Bitmap getQrCodeWithContent(String qrString, Bitmap centerLogo, int qrSize) {
        //宽度值，影响中间图片大小
        int IMAGE_HALF_WIDTH = 50;
        try {
            IMAGE_HALF_WIDTH = qrSize / 10;
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
            BitMatrix bitMatrix = new QRCodeWriter().encode(qrString,
                    BarcodeFormat.QR_CODE, qrSize, qrSize, hints);

            int width = bitMatrix.getWidth();//矩阵高度
            int height = bitMatrix.getHeight();//矩阵宽度
            int halfW = width / 2;
            int halfH = height / 2;

            Matrix m = new Matrix();
            float sx = (float) 2 * IMAGE_HALF_WIDTH / centerLogo.getWidth();
            float sy = (float) 2 * IMAGE_HALF_WIDTH
                    / centerLogo.getHeight();
            m.setScale(sx, sy);
            //设置缩放信息
            //将logo图片按martix设置的信息缩放
            centerLogo = Bitmap.createBitmap(centerLogo, 0, 0,
                    centerLogo.getWidth(), centerLogo.getHeight(), m, false);

            int[] pixels = new int[qrSize * qrSize];
            for (int y = 0; y < qrSize; y++) {
                for (int x = 0; x < qrSize; x++) {
                    if (x > halfW - IMAGE_HALF_WIDTH && x < halfW + IMAGE_HALF_WIDTH
                            && y > halfH - IMAGE_HALF_WIDTH
                            && y < halfH + IMAGE_HALF_WIDTH) {
                        //该位置用于存放图片信息
                        //记录图片每个像素信息
                        pixels[y * width + x] = centerLogo.getPixel(x - halfW
                                + IMAGE_HALF_WIDTH, y - halfH + IMAGE_HALF_WIDTH);
                    } else {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * qrSize + x] = 0xff000000;
                        } else {
                            pixels[y * qrSize + x] = 0xffffffff;
                        }
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(qrSize, qrSize,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, qrSize, 0, 0, qrSize, qrSize);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void showScanResult(Context context, String result) {
        Intent intent = new Intent();
        intent.putExtra("result", result);
        intent.setClass(context, ScanResultActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void fileTransferServiceDownload(Context context, String json) {
        PluginMgr pluginMgr = new PluginMgr(context, null);
        pluginMgr.execute("FileTransferService", "download", json);
        pluginMgr.onDestroy();
    }
}
