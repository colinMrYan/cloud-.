package com.inspur.emmcloud.config;

import android.os.Environment;

/**
 * Created by chenmch on 2017/10/12.
 */

public class Constant {
    public static final String CONCIG_WEB_AUTO_ROTATE = "WebAutoRotate";
    public static final String CONCIG_COMMON_FUNCTIONS = "CommonFunctions";
    public static final String CONCIG_POS_REPORT_TIME_INTERVAL_ = "PosReportTimeInterval";
    public static final String CONCIG_SHOW_FEEDBACK = "IsShowFeedback";
    public static final String CONCIG_SHOW_CUSTOMER_SERVICE = "IsShowCustomerService";
    public static final String CONCIG_CLOUD_PLUS_UUID_FILE = Environment.getExternalStorageDirectory() + "/gsp.uuid";


    public static final String PATTERN_URL = "(((https?)://[a-zA-Z0-9\\_\\-]+(\\.[a-zA-Z0-9\\_\\-]+)*(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?)" +
            "|([a-zA-Z0-9\\-\\_]+\\.)+([a-zA-Z\\-\\_]+)(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?|\\d+(\\.\\d+){3}(\\:\\d{2,4})?)";
    public static final String PREF_APP_RUN_BACKGROUND = "app_set_run_background";
    public static final String PREF_REACT_NATIVE_CLIENTID = "react_native_clientid";
    public static final String PREF_CHAT_CLIENTID = "chat_clientid";
    public static final String PREF_WORK_INFO_BAR_OPEN = "work_open_info";
    public static final String PREF_WORK_PORTLET_CONFIG_UPLOAD = "is_work_portlet_config_upload";
    public static final String PREF_HAS_MY_APP_RECOMMEND = "has_my_app_recommend";
    public static final String PREF_MY_APP_RECOMMEND_DATA = "my_app_recommend_data";
    public static final String PREF_MY_APP_RECOMMEND_DATE = "my_app_recommend_date";
    public static final String PREF_MY_APP_RECOMMEND_EXPIREDDATE = "my_app_recommend_expired_date";
    public static final String PREF_MY_APP_RECOMMEND_LASTUPDATE_HOUR = "my_app_recommend_lastupdate_hour";
    public static final String PREF_SOFT_INPUT_HEIGHT = "soft_input_height";
    public static final String PREF_SELECT_LOGIN_ENTERPRISE_ID = "select_login_tenent_id";
    public static final String PREF_CURRENT_ENTERPRISE_ID = "current_enterprise_id";
    public static final String PREF_DELETE_ILLEGAL_USER = "has_delete_illegal_user";

    public static final String ACTION_MEETING = "com.inspur.meeting";
    public static final String ACTION_TASK = "com.inspur.task";
    public static final String ACTION_CALENDAR = "com.inspur.calendar";
    public static final String ACTION_SAFE_UNLOCK = "com.inspur.safe.unlock";
    public static final String ACTION_VOLUME_INFO_UPDATE = "com.inspur.volume.info.update";

    public static final String DEFAULT_CLUSTER_EMM = "https://emm.inspur.com/";//EMM默认
    public static final String DEFAULT_CLUSTER_ECM = "https://ecm.inspur.com/";//ECM默认
    public static final String DEFAULT_CLUSTER_ID = "https://id.inspuronline.com/";//ID默认

    public static final String EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE = "tag_reveive_single_ws_message";
    public static final String EVENTBUS_TAG_GET_OFFLINE_WS_MESSAGE = "tag_get_offline_ws_message";
    public static final String EVENTBUS_TAG_GET_MESSAGE_BY_ID = "tag_get_message_by_id";

}
