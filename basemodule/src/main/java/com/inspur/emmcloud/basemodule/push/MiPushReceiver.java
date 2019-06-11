package com.inspur.emmcloud.basemodule.push;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.ClientIDUtils;
import com.inspur.emmcloud.basemodule.util.ECMTransparentUtils;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.login.communication.CommunicationService;
import com.luojilab.component.componentlib.router.Router;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 1、PushMessageReceiver 是个抽象类，该类继承了 BroadcastReceiver。<br/>
 * 2、需要将自定义的 DemoMessageReceiver 注册在 AndroidManifest.xml 文件中：
 * <pre>
 * {@code
 *  <receiver
 *      android:name="com.xiaomi.mipushdemo.DemoMessageReceiver"
 *      android:exported="true">
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.RECEIVE_MESSAGE" />
 *      </intent-filter>
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.MESSAGE_ARRIVED" />
 *      </intent-filter>
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.ERROR" />
 *      </intent-filter>
 *  </receiver>
 *  }</pre>
 * 3、DemoMessageReceiver 的 onReceivePassThroughMessage 方法用来接收服务器向客户端发送的透传消息。<br/>
 * 4、DemoMessageReceiver 的 onNotificationMessageClicked 方法用来接收服务器向客户端发送的通知消息，
 * 这个回调方法会在用户手动点击通知后触发。<br/>
 * 5、DemoMessageReceiver 的 onNotificationMessageArrived 方法用来接收服务器向客户端发送的通知消息，
 * 这个回调方法是在通知消息到达客户端时触发。另外应用在前台时不弹出通知的通知消息到达客户端也会触发这个回调函数。<br/>
 * 6、DemoMessageReceiver 的 onCommandResult 方法用来接收客户端向服务器发送命令后的响应结果。<br/>
 * 7、DemoMessageReceiver 的 onReceiveRegisterResult 方法用来接收客户端向服务器发送注册命令后的响应结果。<br/>
 * 8、以上这些方法运行在非 UI 线程中。
 *
 * @author mayixiang
 */
public class MiPushReceiver extends PushMessageReceiver {
    private static final String TAG = "MiPush";
    private String mRegId;
    private String mTopic;
    private String mAlias;
    private String mAccount;
    private String mStartTime;
    private String mEndTime;

    @SuppressLint("SimpleDateFormat")
    private static String getSimpleDate() {
        return new SimpleDateFormat("MM-dd hh:mm:ss").format(new Date());
    }

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        LogUtils.debug(TAG,"onReceivePassThroughMessage is called. " + message.toString());
        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
            ECMTransparentUtils.handleTransparentMsg(context, mTopic);
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        }
    }

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        LogUtils.debug(TAG,"onNotificationMessageClicked is called. " + message.toString());
        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        }
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        LogUtils.debug(TAG, "onNotificationMessageArrived is called. " + message.toString());
        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        }

    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        LogUtils.debug(TAG, "onCommandResult is called. " + message.toString());
//        String command = message.getCommand();
//        List<String> arguments = message.getCommandArguments();
//        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
//        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
//        String log;
//        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
//            if (message.getResultCode() == ErrorCode.SUCCESS) {
//                mRegId = cmdArg1;
//            } else {
//                MyApplication.getInstance().startJPush();
//            }
//        }
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        LogUtils.debug(TAG, "onReceiveRegisterResult is called. " + message.toString());
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                PreferencesUtils.putString(context, Constant.MIPUSH_REGISTER_ID, mRegId);
                PushManagerUtils.getInstance().registerPushId2Emm();
                new ClientIDUtils(context).upload();
                Router router = Router.getInstance();
                if (router.getService(CommunicationService.class.getSimpleName()) != null) {
                    CommunicationService service = (CommunicationService) router.getService(CommunicationService.class.getSimpleName());
                    service.startWebSocket();
                }
            }
            // else {
            // PushManagerUtils.getInstance().setJpushStatus(true);
            // }
        }

    }

    @Override
    public void onRequirePermissions(Context context, String[] permissions) {
        super.onRequirePermissions(context, permissions);
        LogUtils.debug(TAG,
                "onRequirePermissions is called. need permission" + arrayToString(permissions));
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(context, permissions, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                PushManagerUtils.getInstance().setMiPushStatus(true);
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                        // PushManagerUtils.getInstance().setJpushStatus(true);
            }
        });

    }

    public String arrayToString(String[] strings) {
        String result = " ";
        for (String str : strings) {
            result = result + str + " ";
        }
        return result;
    }
}
