package com.inspur.emmcloud.basemodule.util.protocol;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.baselib.widget.roundbutton.CustomRoundButton;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.widget.spans.URLClickableSpan;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.util.LanguageManager;

public class ProtocolUtil {

    public static final String PREF_PROTOCOL_DLG_AGREED = "protocol_dlg_agreed";
    public static final String PRIVATE_AGREEMENT = "http://emm.inspuronline.com:83/cloudplus_policy.html";
    public static final String SERVICE_AGREEMENT = "http://emm.inspuronline.com:83/cloudplus_service_";
    private static final String AGREEMENT_COLOR = "#180000";

    private static SpannableString agreement;

    public interface ProtocolDialogCallback {
        void onAgreeDialog();
    }


    public static void showProtocolDialog(final Context context, @Nullable final ProtocolDialogCallback callback) {
        if (PreferencesUtils.getBoolean(context, PREF_PROTOCOL_DLG_AGREED, false)) {
            if (callback != null) {
                callback.onAgreeDialog();
            }
            return;
        }
        final MyDialog dialog = new MyDialog(context,
                R.layout.basewidget_agreement_dialog_two_buttons);
        dialog.setCancelable(false);
        TextView textView = dialog.findViewById(R.id.tv_agreement_content);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        getAgreeContent(LanguageManager.getInstance().getCurrentAppLanguage());
        textView.setText(agreement);
        CustomRoundButton customRoundButtonNotAgree = dialog.findViewById(R.id.btn_agreement_not_agree);
        customRoundButtonNotAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDlg(context);
            }
        });
        CustomRoundButton customRoundButtonAgree = dialog.findViewById(R.id.btn_agreement_agree);
        customRoundButtonAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PreferencesUtils.putBoolean(context, PREF_PROTOCOL_DLG_AGREED, true);
                if (callback != null) {
                    callback.onAgreeDialog();
                }
            }
        });
        dialog.show();
    }


    /**
     * 弹出注销提示框
     */
    private static void showConfirmDlg(Context context) {
        new CustomDialog.MessageDialogBuilder(context)
                .setMessage(context.getString(R.string.privacy_reject_dialog_title))
                .setNegativeButton(context.getString(R.string.privacy_reject_dialog_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(context.getString(R.string.privacy_reject_dialog_quit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        BaseApplication.getInstance().signout();
                        BaseApplication.getInstance().exit();

                    }
                })
                .show();
    }

    /**
     * 获取协议字符串
     *
     * @param environmentLanguage 语言环境
     */
    private static void getAgreeContent(String environmentLanguage) {
        String agreementStr;
        int startIndexPrivate;
        int startIndexService;
        switch (environmentLanguage.toLowerCase()) {
            case "zh-hant":
                agreementStr = "歡迎使用雲+!\n        雲+非常重視您的個人信息和隱私保護，為了更好的向您提供交流溝通、文件傳送、電話撥打、位置定位等相關服務，我們會根據您使用服務的具體功能需要，收集必要的用戶信息（可能涉及賬號、設備、日誌等相關內容）。" +
                        "\n        在使用我們的產品和服務前，請您務必仔細閱讀、充分理解《服務協定》和《隱私協定》各條款。我們將嚴格按照上述條款為您提供服務，保護您的信息安全，點擊“同意”即表示您已閱讀並同意全部條款，可以開始使用我們的產品和服務。";
                agreement = new SpannableString(agreementStr);
                startIndexPrivate = agreementStr.indexOf("《隱私協定》");
                startIndexService = agreementStr.indexOf("《服務協定》");
                ForegroundColorSpan colorSpan2 = new ForegroundColorSpan(Color.parseColor(AGREEMENT_COLOR));
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor(AGREEMENT_COLOR));
                agreement.setSpan(colorSpan, startIndexPrivate, startIndexPrivate + 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                agreement.setSpan(colorSpan2, startIndexService, startIndexService + 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                URLClickableSpan urlClickableSpan = new URLClickableSpan(PRIVATE_AGREEMENT);
                URLClickableSpan urlClickableSpan2 = new URLClickableSpan(SERVICE_AGREEMENT);
                agreement.setSpan(urlClickableSpan, startIndexPrivate, startIndexPrivate + 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                agreement.setSpan(urlClickableSpan2, startIndexService, startIndexService + 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                break;
            case "en":
            case "en-us":
                agreementStr = "Cloud+ attaches great importance to your personal information and privacy protection. In order to provide you with related services such as communication, file transfer, telephone dialing, and location service, we will collect the necessary users according to the specific functional needs of your use of the service Information (may involve accounts, devices, logs, etc.). \n     Before using our products and services, please read and fully understand the terms of the \"Service Agreement\" and \"Privacy Policy\". We will provide services to you in strict accordance with the above terms and protect the security of your information. Clicking \"Agree\" means that you have read and agreed to all the terms and can start using our products and services.";
                agreement = new SpannableString(agreementStr);
                startIndexPrivate = agreementStr.indexOf("Privacy Policy");
                startIndexService = agreementStr.indexOf("Service Agreement");
                ForegroundColorSpan colorSpanEn = new ForegroundColorSpan(Color.parseColor(AGREEMENT_COLOR));
                ForegroundColorSpan colorSpanEn2 = new ForegroundColorSpan(Color.parseColor(AGREEMENT_COLOR));
                agreement.setSpan(colorSpanEn, startIndexPrivate, startIndexPrivate + 17, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                agreement.setSpan(colorSpanEn2, startIndexService, startIndexService + 17, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                URLClickableSpan urlClickableSpanEn = new URLClickableSpan(PRIVATE_AGREEMENT);
                URLClickableSpan urlClickableSpanEn2 = new URLClickableSpan(SERVICE_AGREEMENT);
                agreement.setSpan(urlClickableSpanEn, startIndexPrivate, startIndexPrivate + 17, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                agreement.setSpan(urlClickableSpanEn2, startIndexService, startIndexService + 17, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                break;
            default:
                agreementStr = "欢迎使用云+!\n        云+非常重视您的个人信息和隐私保护，为了更好的向您提供交流沟通、文件传送、电话拨打、位置定位等相关服务，我们会根据您使用服务的具体功能需要，收集必要的用户信息（可能涉及账号、设备、日志等相关内容）。" +
                        "\n        在使用我们的产品和服务前，请您务必仔细阅读、充分理解《服务协议》和《隐私政策》各条款。我们将严格按照上述条款为您提供服务，保护您的信息安全，点击“同意”即表示您已阅读并同意全部条款，可以开始使用我们的产品和服务。";
                agreement = new SpannableString(agreementStr);
                startIndexPrivate = agreementStr.indexOf("《隐私政策》");
                startIndexService = agreementStr.indexOf("《服务协议》");
                ForegroundColorSpan colorSpanZh = new ForegroundColorSpan(Color.parseColor(AGREEMENT_COLOR));
                ForegroundColorSpan colorSpanZh2 = new ForegroundColorSpan(Color.parseColor(AGREEMENT_COLOR));
                agreement.setSpan(colorSpanZh, startIndexPrivate, startIndexPrivate + 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                agreement.setSpan(colorSpanZh2, startIndexService, startIndexService + 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                URLClickableSpan urlClickableSpanZh = new URLClickableSpan(PRIVATE_AGREEMENT);
                URLClickableSpan urlClickableSpanZh2 = new URLClickableSpan(SERVICE_AGREEMENT);
                agreement.setSpan(urlClickableSpanZh, startIndexPrivate, startIndexPrivate + 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                agreement.setSpan(urlClickableSpanZh2, startIndexService, startIndexService + 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                break;
        }
    }

}
