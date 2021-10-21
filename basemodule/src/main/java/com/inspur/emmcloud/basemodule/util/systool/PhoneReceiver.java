package com.inspur.emmcloud.basemodule.util.systool;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiUri;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.AppTabUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;

public class PhoneReceiver extends BroadcastReceiver {

    private WindowManager windowManager;
    private View phoneView;
    private final PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    hideWindow();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    showUserInfoWindow(incomingNumber);
                    break;
            }
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private ContactUser getInComingUserInfoByPhoneNum(String phoneNumber) {
        ContactUser contactUser = null;
        ContactService service = Router.getInstance().getService(ContactService.class);
        if (service != null) {
            contactUser = service.getContactUserByPhoneNumber(phoneNumber);
        }
        if (contactUser != null && !TextUtils.isEmpty(contactUser.getId())) {
            String organizeNameStr = service.getOrganizeName(contactUser.getId());
            if (TextUtils.isEmpty(organizeNameStr)) {
                contactUser.setOffice(contactUser.getName());
            } else {
                String[] organizeNames = organizeNameStr.split("-");
                int length = organizeNames.length;
                //组织信息：最后一级（部门信息）+人名
                contactUser.setOffice(organizeNames[length - 1] + "-" + contactUser.getName());
            }
        }
        return contactUser;
    }

    private void showUserInfoWindow(String incomingNumber) {
        //有通讯录权限才能监听来电显示身份识别信息
        hideWindow();
        if (!AppTabUtils.hasContactPermission(BaseApplication.getInstance())) return;
        if (TextUtils.isEmpty(incomingNumber)) return;
        ContactUser contactUser = getInComingUserInfoByPhoneNum(incomingNumber);
        if (contactUser == null || contactUser.getId() == null || !contactUser.getMobile().equals(incomingNumber))
            return;
        if (windowManager == null) {
            windowManager = (WindowManager) BaseApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        }
        if (phoneView == null) {
            phoneView = LayoutInflater.from(BaseApplication.getInstance()).inflate(com.inspur.emmcloud.basemodule.R.layout.phone_alert, null);
        }
        if (windowManager != null && phoneView != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            layoutParams.y = (int) dp2px(150);
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            layoutParams.gravity = Gravity.TOP;
            layoutParams.format = PixelFormat.TRANSPARENT;
            ((TextView) phoneView.findViewById(R.id.user_name)).setText(contactUser.getOffice());
            ((TextView) phoneView.findViewById(R.id.user_mobile)).setText(incomingNumber);
            ((ImageView) phoneView.findViewById(R.id.close_icon)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideWindow();
                }
            });
            String photoUri = BaseModuleApiUri.getUserPhoto(BaseApplication.getInstance(), contactUser.getId());
            ImageDisplayUtils.getInstance().displayImage(((ImageView) phoneView.findViewById(R.id.user_header)), photoUri, R.drawable.icon_person_default);
            windowManager.addView(phoneView, layoutParams);
        }
    }

    public void hideWindow() {
        try {
            if (windowManager != null && phoneView != null) {
                windowManager.removeView(phoneView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            windowManager = null;
            phoneView = null;
        }
    }

    private float dp2px(int dp) {
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }
}