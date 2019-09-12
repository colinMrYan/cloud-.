package com.inspur.emmcloud.web.servcieimpl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
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
