package com.inspur.emmcloud.web.webview;

import static android.Manifest.permission.CAMERA;
import static com.inspur.emmcloud.basemodule.config.MyAppConfig.LOCAL_IMG_CREATE_PATH;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.filetransfer.FilePathUtils;
import com.inspur.emmcloud.web.ui.ImpCallBackInterface;
import com.inspur.emmcloud.web.ui.iLog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * webview接收js端的弹出窗口
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class ImpWebChromeClient extends WebChromeClient {

    // File Chooser
    public static final int FILE_CHOOSER_RESULT_CODE = 5173;
    private Context context;
    private ValueCallback<Uri> mUploadMessage;// 回调图片选择，4.4以下
    private ValueCallback<Uri[]> mUploadCallbackAboveL;// 回调图片选择，5.0以上
    private ImpWebView mWebView;
    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private String mCameraPhotoPath = null;
//    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    public ImpWebChromeClient(Context context, ImpWebView webView, FrameLayout frameLayout) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.mWebView = webView;
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(context, Permissions.LOCATION, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                callback.invoke(origin, false, false);
            }
        });
    }


    @Override
    public void onCloseWindow(WebView window) {
        super.onCloseWindow(window);
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean dialog,
                                  boolean userGesture, Message resultMsg) {
        return super.onCreateWindow(view, dialog, userGesture, resultMsg);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        showCustomView(view, callback);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//播放时横屏幕，如果需要改变横竖屏，只需该参数就行了
    }

    @Override
    public void onHideCustomView() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        hideCustomView();
    }

    /**
     * 视频播放全屏
     *
     * @param view
     * @param callback
     */
    private void showCustomView(View view, CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }
        getActivity().getWindow().getDecorView();
        FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
        fullscreenContainer = new FullscreenHolder(getActivity());
        FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        decor.addView(fullscreenContainer, containerLayoutParams);
        FrameLayout.LayoutParams viewLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fullscreenContainer.addView(view, viewLayoutParams);
        customView = view;
        setStatusBarVisibility(false);
        customViewCallback = callback;
        mWebView.setVisibility(View.GONE);
    }

    /**
     * 退出视频播放全屏
     *
     * @return 是否消费掉返回键
     */
    public boolean hideCustomView() {
        if (customView == null) {
            return false;
        }

        setStatusBarVisibility(true);
        FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        mWebView.setVisibility(View.VISIBLE);
        return true;
    }

    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getActivity().getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 转化为Activity
     *
     * @return
     */
    private Activity getActivity() {
        if (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;
    }

    /**
     * 覆盖默认的window.alert展示界面
     */
    public boolean onJsAlert(WebView view, String url, String message,
                             final JsResult result) {
        // 防止activity已经finish，导致dialog代码崩溃
        boolean destroyed;
        if (context instanceof Activity) {
            Activity activity = (Activity) this.context;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                destroyed = activity.isDestroyed();
            } else {
                destroyed = activity.isFinishing();
            }
        } else {
            destroyed = true;
        }
        if (!destroyed) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    this.context, AlertDialog.THEME_HOLO_LIGHT);

            builder.setTitle(Res.getStringID("msg_title")).setMessage(message);

            builder.setPositiveButton(Res.getStringID("file_ok"),
                    new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }

                    });
            // 禁止取消按钮
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            result.confirm();
        }

        return true;
    }

    public boolean onJsBeforeUnload(WebView view, String url, String message,
                                    JsResult result) {
        return super.onJsBeforeUnload(view, url, message, result);
    }

    /**
     * 覆盖默认的window.confirm
     */
    public boolean onJsConfirm(WebView view, String url, String message,
                               final JsResult result) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                view.getContext(), AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(Res.getStringID("msg_makesure"))
                .setMessage(message)
                .setPositiveButton(Res.getStringID("file_ok"),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                result.confirm();
                            }
                        })
                .setNeutralButton(Res.getStringID("file_cancel"),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                result.cancel();
                            }
                        });
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                result.cancel();
            }
        });

        // 监听对话框的点击事件
        builder.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                iLog.v("onJsConfirm", "keyCode==" + keyCode + "event=" + event);
                return true;
            }
        });
        // 禁止响应按back键的事件
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    /**
     * 覆盖默认的window.prompt
     */
    public boolean onJsPrompt(WebView view, String url, String message,
                              String defaultValue, final JsPromptResult result) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                view.getContext(), AlertDialog.THEME_HOLO_LIGHT);

        builder.setTitle(Res.getStringID("edit")).setMessage(message);

        final EditText et = new EditText(view.getContext());
        et.setSingleLine();
        et.setText(defaultValue);
        builder.setView(et)
                .setPositiveButton(Res.getStringID("file_ok"),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                result.confirm(et.getText().toString());
                            }

                        })
                .setNeutralButton(Res.getStringID("file_cancel"),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                result.cancel();
                            }
                        });

        // 监听对话框的点击事件
        builder.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                iLog.v("onJsPrompt", "keyCode==" + keyCode + "event=" + event);
                return true;
            }
        });

        // 禁止响应按back键的事件
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    /****** IMP修改处，web页面可以直接获取文件 *******/
    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        this.openFileChooser(uploadMsg, "*/*");
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        this.openFileChooser(uploadMsg, acceptType, null);
    }

    // For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                String acceptType, String capture) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        if (TextUtils.isEmpty(capture)) {
            acceptType = "*/*";
        }
        i.setType(acceptType);
        if (mWebView.getImpCallBackInterface() != null) {
            mWebView.getImpCallBackInterface().onStartActivityForResult(Intent.createChooser(i, "File Browser"), FILE_CHOOSER_RESULT_CODE);
        }
    }

    // For Android 5.0+
    @SuppressLint("NewApi")
    public boolean onShowFileChooser(WebView webView,
                                     ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        mUploadCallbackAboveL = filePathCallback;
        // 针对广水添加相机和相册选项
        showFileChooser();
        return true;
//        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//        i.addCategory(Intent.CATEGORY_OPENABLE);
//        String type = "*/*";
//        if (fileChooserParams != null
//                && fileChooserParams.getAcceptTypes() != null
//                && fileChooserParams.getAcceptTypes().length > 0) {
//            if (!TextUtils.isEmpty(fileChooserParams.getAcceptTypes()[0])) {
//                type = fileChooserParams.getAcceptTypes()[0];
//            }
//        }
//        i.setType(type);
//
//        if (mWebView.getImpCallBackInterface() != null) {
//            mWebView.getImpCallBackInterface().onStartActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
//        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, 10000);
    }

    private void showFileChooser() {
        if (checkPermission()) {
            // 指定拍照存储位置的方式调起相机
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                Uri photoFile;
                try {
                    File file = createImageFile();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        photoFile = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
                        if (photoFile != null) {
                            mCameraPhotoPath = "file:" + file.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
                        } else {
                            takePictureIntent = null;
                        }
                    } else {
                        String path = LOCAL_IMG_CREATE_PATH;
                        File mediaStorageDir = new File(path);
                        photoFile = Uri.fromFile(mediaStorageDir);
                        if (photoFile != null) {
                            mCameraPhotoPath = mediaStorageDir.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
                        } else {
                            takePictureIntent = null;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Intent contentSelectionIntent = new Intent(Intent.ACTION_PICK);
            contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            contentSelectionIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[2];
            }
            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            if (mWebView.getImpCallBackInterface() != null) {
                mWebView.getImpCallBackInterface().onStartActivityForResult(Intent.createChooser(chooserIntent, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
            }
        } else {
            PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getActivity(), Permissions.CAMERA, new PermissionRequestCallback() {
                @Override
                public void onPermissionRequestSuccess(List<String> permissions) {
                    showFileChooser();
                }

                @Override
                public void onPermissionRequestFail(List<String> permissions) {
                    ToastUtils.show(BaseApplication.getInstance(), PermissionRequestManagerUtils.getInstance().getPermissionToast(BaseApplication.getInstance(), permissions));
                }
            });
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String dirPath = LOCAL_IMG_CREATE_PATH;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dirPath, System.currentTimeMillis() + ".png");
    }

    public ValueCallback<Uri> getValueCallback() {
        return mUploadMessage;
    }

    public ValueCallback<Uri[]> getValueCallbackAboveL() {
        return mUploadCallbackAboveL;
    }

    public void setmCameraPhotoPath(String mCameraPhotoPath) {
        this.mCameraPhotoPath = mCameraPhotoPath;
    }

    public String getmCameraPhotoPath() {
        return mCameraPhotoPath;
    }

    /*
     * 根据网页加载速度更改进度条显示进度
     */
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        ImpCallBackInterface impCallBackInterface = mWebView.getImpCallBackInterface();
        if (impCallBackInterface != null) {
            impCallBackInterface.onProgressChanged(newProgress);
        }
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        super.onReceivedIcon(view, icon);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        if (null != title && !getRemoveHttpUrl(title).equals(getRemoveHttpUrl(view.getUrl())) && !getRemoveHttpUrl(title).equals(getRemoveHttpUrl(view.getOriginalUrl()))) {
            ImpCallBackInterface impCallBackInterface = mWebView.getImpCallBackInterface();
            if (impCallBackInterface != null) {
                impCallBackInterface.onSetTitle(title);
            }
        }
    }

    @Override
    public void onRequestFocus(WebView view) {
        super.onRequestFocus(view);
    }

    public String getRemoveHttpUrl(String url) {
        return url.replace("http://", "").replace("https://", "").trim();
    }

    /**
     * 全屏容器界面
     */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }


}
