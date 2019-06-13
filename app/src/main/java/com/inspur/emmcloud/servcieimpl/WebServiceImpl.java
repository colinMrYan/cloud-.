package com.inspur.emmcloud.servcieimpl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.Fragment;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.componentservice.web.WebService;
import com.inspur.imp.plugin.barcode.ScanResultActivity;
import com.inspur.imp.plugin.barcode.decoder.PreviewDecodeActivity;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;
import com.inspur.imp.plugin.camera.mycamera.MyCameraActivity;

import java.io.File;
import java.util.List;

/**
 * Created by chenmch on 2019/6/3.
 */

public class WebServiceImpl implements WebService {
    @Override
    public void openCamera(Activity activity, String picPath, int requestCode) {
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
            ToastUtils.show(activity, R.string.filetransfer_sd_not_exist);
        }
    }

    @Override
    public void openScanCode(Activity activity, int requestCode) {
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
    public void openScanCode(Fragment fragment, int requestCode) {
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
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setShowCamera(false); // 显示拍照按钮
        imagePicker.setCrop(false); // 允许裁剪（单选才有效）
        imagePicker.setSelectLimit(limit);
        imagePicker.setMultiMode(true);
        Intent intent = new Intent(activity, ImageGridActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void showScanResult(Context context, String result) {
        Intent intent = new Intent();
        intent.putExtra("result", result);
        intent.setClass(context, ScanResultActivity.class);
        context.startActivity(intent);

    }
}
