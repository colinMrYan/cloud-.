package com.inspur.emmcloud.basemodule.media.selector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.selector.basic.PictureCommonFragment;
import com.inspur.emmcloud.basemodule.media.selector.config.PictureSelectionConfig;
import com.inspur.emmcloud.basemodule.media.selector.entity.LocalMedia;
import com.inspur.emmcloud.basemodule.media.selector.manager.SelectedManager;
import com.inspur.emmcloud.basemodule.media.selector.permissions.PermissionChecker;
import com.inspur.emmcloud.basemodule.media.selector.permissions.PermissionConfig;
import com.inspur.emmcloud.basemodule.media.selector.permissions.PermissionResultCallback;
import com.inspur.emmcloud.basemodule.media.selector.utils.SdkVersionUtils;
import com.inspur.emmcloud.basemodule.media.selector.utils.ToastUtils;


/**
 * @author：luck
 * @date：2021/11/22 2:26 下午
 * @describe：PictureOnlyCameraFragment
 */
public class PictureOnlyCameraFragment extends PictureCommonFragment {
    public static final String TAG = PictureOnlyCameraFragment.class.getSimpleName();

    public static PictureOnlyCameraFragment newInstance() {
        return new PictureOnlyCameraFragment();
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getResourceId() {
        return R.layout.ps_empty;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 这里只有非内存回收状态下才走，否则当内存不足Fragment被回收后会重复执行
        if (savedInstanceState == null) {
            if (SdkVersionUtils.isQ()) {
                openSelectedCamera();
            } else {
                PermissionChecker.getInstance().requestPermissions(this,
                        PermissionConfig.WRITE_EXTERNAL_STORAGE, new PermissionResultCallback() {
                            @Override
                            public void onGranted() {
                                openSelectedCamera();
                            }

                            @Override
                            public void onDenied() {
                                handlePermissionDenied(PermissionConfig.WRITE_EXTERNAL_STORAGE);
                            }
                        });
            }
        }
    }

    @Override
    public void dispatchCameraMediaResult(LocalMedia media) {
        int selectResultCode = confirmSelect(media, false);
        if (selectResultCode == SelectedManager.ADD_SUCCESS) {
            dispatchTransformResult();
        } else {
            onKeyBackFragmentFinish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            onKeyBackFragmentFinish();
        }
    }

    @Override
    public void handlePermissionSettingResult(String[] permissions) {
        onPermissionExplainEvent(false, null);
        boolean isHasPermissions;
        if (PictureSelectionConfig.onPermissionsEventListener != null) {
            isHasPermissions = PictureSelectionConfig.onPermissionsEventListener
                    .hasPermissions(this, permissions);
        } else {
            isHasPermissions = PermissionChecker.isCheckCamera(getContext());
            if (SdkVersionUtils.isQ()) {
            } else {
                if (SdkVersionUtils.isR() && config.isAllFilesAccess){
//                    isHasPermissions = Environment.isExternalStorageManager();
                    isHasPermissions = true;
                } else {
                    isHasPermissions = PermissionChecker.isCheckWriteStorage(getContext());
                }
            }
        }
        if (isHasPermissions) {
            openSelectedCamera();
        } else {
            if (!PermissionChecker.isCheckCamera(getContext())) {
                ToastUtils.showToast(getContext(), getString(R.string.ps_camera));
            } else {
//                boolean isCheckWriteStorage = SdkVersionUtils.isR() && config.isAllFilesAccess
//                        ? Environment.isExternalStorageManager() : PermissionChecker.isCheckWriteStorage(getContext());
                boolean isCheckWriteStorage = SdkVersionUtils.isR() && config.isAllFilesAccess
                        ? true : PermissionChecker.isCheckWriteStorage(getContext());
                if (!isCheckWriteStorage) {
                    ToastUtils.showToast(getContext(), getString(R.string.ps_jurisdiction));
                }
            }
            onKeyBackFragmentFinish();
        }
        PermissionConfig.CURRENT_REQUEST_PERMISSION = null;
    }
}
