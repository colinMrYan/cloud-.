package com.inspur.emmcloud.web.plugin.barcode.scan;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.plugin.barcode.alidecoder.ALiScanActivity;
import com.inspur.emmcloud.web.ui.ImpFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.List;


/**
 * 扫描二维码功能
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */

public class BarCodeService extends ImpPlugin {

    public static String functName;
    private Dialog qrCodeDlg;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        // 启动扫描二维码
        if ("scan".equals(action)) {
            scan(paramsObject);
        } else if ("closeQrCode".equals(action)) {
            closeQrCodeDlg();
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    /**
     * 关闭二维码显示Dlg
     */
    private void closeQrCodeDlg() {
        // TODO Auto-generated method stub
        if (qrCodeDlg != null && qrCodeDlg.isShowing()) {
            qrCodeDlg.dismiss();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        // TODO Auto-generated method stub
        if ("generate".equals(action)) {
            try {
                if (!paramsObject.isNull("format")) {
                    String format = paramsObject.getString("format");
                    if (format.equals("QR")) {
                        return generateQrcode(paramsObject);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        } else {
            showCallIMPMethodErrorDlg();
        }
        return "";
    }

    /**
     * 扫描二维码
     *
     * @param paramsObject
     */
    private void scan(JSONObject paramsObject) {
        // 解析json串获取到传递过来的参数和回调函数
        try {
            if (!paramsObject.isNull("callback"))
                functName = paramsObject.getString("callback");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (getImpCallBackInterface() != null) {
            PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getActivity(), Permissions.CAMERA, new PermissionRequestCallback() {
                @Override
                public void onPermissionRequestSuccess(List<String> permissions) {
                    Intent scanIntent = new Intent(getFragmentContext(), ALiScanActivity.class);
                    getImpCallBackInterface().onStartActivityForResult(scanIntent, ImpFragment.BARCODE_SERVER__SCAN_REQUEST);
                }

                @Override
                public void onPermissionRequestFail(List<String> permissions) {
                    ToastUtils.show(getFragmentContext(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getFragmentContext(), permissions));
                }
            });
        }
    }

    /**
     * 生成二维码
     *
     * @param paramsObject
     */
    private String generateQrcode(JSONObject paramsObject) {
        // TODO Auto-generated method stub
        String result = "false";
        try {
            if (!paramsObject.isNull("value")) {
                String content = paramsObject.getString("value");
                int qrSize = dip2px(getFragmentContext(), 300);
                Bitmap bitmap = creatQrCode(content, qrSize);
                if (bitmap != null) {
                    showResultDlg(bitmap);
                    result = "true";
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 弹出二维码显示界面
     *
     * @param bitmap
     */
    private void showResultDlg(Bitmap bitmap) {
        // TODO Auto-generated method stub
        qrCodeDlg = new Dialog(getActivity(), Res.getStyleID("web_plugin_fullScreenDialog"));
        qrCodeDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        qrCodeDlg.setContentView(Res.getLayoutID("web_barcode_show_qrcode"));
        qrCodeDlg.setCanceledOnTouchOutside(false);
//		qrCodeDlg.setCancelable(false);
        qrCodeDlg.findViewById(Res.getWidgetID("ibt_back")).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                qrCodeDlg.dismiss();
            }
        });
        ((ImageView) qrCodeDlg.findViewById(Res.getWidgetID("img"))).setImageBitmap(bitmap);
        Window window = qrCodeDlg.getWindow();
        window.setWindowAnimations(Res.getStyleID("main_menu_animstyle"));
        WindowManager.LayoutParams lay = qrCodeDlg.getWindow().getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        Rect rect = new Rect();
        View view = getActivity().getWindow().getDecorView();// decorView是window中的最顶层view，可以从window中获取到decorView
        view.getWindowVisibleDisplayFrame(rect);
        lay.height = dm.heightPixels - rect.top;
        lay.width = dm.widthPixels;
        qrCodeDlg.show();
    }


    /**
     * 生成二维码
     *
     * @param qrString
     * @param qrSize
     * @return
     */
    private Bitmap creatQrCode(String qrString, int qrSize) {
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

    private void showResultPop(String content) {

    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImpFragment.BARCODE_SERVER__SCAN_REQUEST && resultCode == Activity.RESULT_OK) {
            String result = data.getStringExtra("msg");
            jsCallback(functName, result);
        }
    }

    @Override
    public void onDestroy() {

    }
}
