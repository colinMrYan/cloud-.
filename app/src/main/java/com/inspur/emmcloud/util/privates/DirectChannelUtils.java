package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;

public class DirectChannelUtils {

    /**
     * 获取单聊频道的title
     *
     * @param context
     * @param msgTitle
     * @return
     */
    public static String getDirectChannelTitle(Context context, String title) {
        String channelTitle = "";
        if (title.contains("-") && title.contains(MyApplication.getInstance().getUid())) {
            ContactUser otherContactUser = getDirctChannelOtherContactUser(context, title);
            if (otherContactUser != null) {
                channelTitle = otherContactUser.getName();
            }
        }
        return channelTitle;
    }

    /**
     * 获取单聊频道icon的url
     *
     * @param context
     * @param msgTitle
     * @return
     */
    public static String getDirectChannelIcon(Context context, String msgTitle) {
        String otherUid = getDirctChannelOtherUid(context, msgTitle);
        return APIUri.getChannelImgUrl(context, otherUid);
    }

    /**
     * 机器人头像
     *
     * @param context
     * @param title
     * @return
     */
    public static String getRobotIcon(Context context, String title) {
        String robotIcon = ((MyApplication) context.getApplicationContext()).getUserPhotoUrl(title);
        if (robotIcon == null) {
            Robot robot = getRobotInfo(context, title);
            if (robot != null) {
                robotIcon = APIUri.getRobotIconUrl(robot.getAvatar());
                ((MyApplication) context.getApplicationContext()).setUsesrPhotoUrl(title, robotIcon);
            }
        }
        return robotIcon;
    }

    /**
     * 获取单聊对方的contact
     *
     * @param context
     * @param title
     * @return
     */
    public static ContactUser getDirctChannelOtherContactUser(Context context,
                                                              String title) {
        ContactUser contactUser = null;
        try {
            String[] uidArray = title.split("-");
            String otherUid = "";
            if (uidArray[0].equals(MyApplication.getInstance().getUid())) {
                otherUid = uidArray[1];
            } else {
                otherUid = uidArray[0];
            }
            contactUser = ContactUserCacheUtils.getContactUserByUid(otherUid);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return contactUser;
    }

    /**
     * 获取单聊对方的uid
     *
     * @param context
     * @param title
     * @return
     */
    public static String getDirctChannelOtherUid(Context context,
                                                 String title) {
        String otherUid = "";
        try {
            String[] uidArray = title.split("-");
            String myUid = ((MyApplication) context.getApplicationContext()).getUid();
            if (uidArray[0].equals(myUid)) {
                otherUid = uidArray[1];
            } else {
                otherUid = uidArray[0];
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return otherUid;
    }

    /**
     * @return
     */
    public static Robot getRobotInfo(Context context,
                                     String title) {
        Robot robot = null;
        try {
            String[] uidArray = title.split("-");
            String myUid = PreferencesUtils.getString(context, "userID");
            String otherUid = "";
            if (uidArray[0].equals(myUid)) {
                otherUid = uidArray[1];
            } else {
                otherUid = uidArray[0];
            }
            robot = RobotCacheUtils.getRobotById(context, otherUid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (robot == null) {
            robot = new Robot();
        }
        return robot;
    }

}
