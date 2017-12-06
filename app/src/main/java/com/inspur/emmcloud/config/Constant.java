package com.inspur.emmcloud.config;

import android.os.Environment;

/**
 * Created by chenmch on 2017/10/12.
 */

public class Constant {
    public static final String CONCIG_WEB_AUTO_ROTATE = "WebAutoRotate";
    public static final String CONCIG_COMMON_FUNCTIONS = "CommonFunctions";
    public static final String CONCIG_POS_REPORT_TIME_INTERVAL_= "PosReportTimeInterval";
    public static final String CONCIG_SHOW_FEEDBACK = "IsShowFeedback";
    public static final String CONCIG_SHOW_CUSTOMER_SERVICE = "IsShowCustomerService";
    public static final String CONCIG_CLOUD_PLUS_UUID_FILE = Environment.getExternalStorageDirectory()+"/gsp.uuid";


    public static final String PATTERN_URL = "(((https?)://[a-zA-Z0-9\\_\\-]+(\\.[a-zA-Z0-9\\_\\-]+)*(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?)" +
            "|([a-zA-Z0-9\\-\\_]+\\.)+([a-zA-Z\\-\\_]+)(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?|\\d+(\\.\\d+){3}(\\:\\d{2,4})?)";
    public static final String PREF_APP_BADGE_UPDATE_TIME = "app_badge_update_time";
    public static final String PREF_APP_RUN_BACKGROUND= "app_set_run_background";
    public static final String PREF_REACT_NATIVE_CLIENTID = "react_native_clientid";
    public static final String PREF_WORK_INFO_BAR_OPEN = "work_open_info";
    public static final String PREF_WORK_PORTLET_CONFIG_UPLOAD = "is_work_portlet_config_upload";
    public static final String PREF_HAS_MY_APP_RECOMMEND = "has_my_app_recommend";
    public static final String PREF_MY_APP_RECOMMEND_DATA = "my_app_recommend_data";
    public static final String PREF_MY_APP_RECOMMEND_DATE = "my_app_recommend_date";
    public static final String PREF_MY_APP_RECOMMEND_EXPIREDDATE = "my_app_recommend_expired_date";
    public static final String PREF_MY_APP_RECOMMEND_LASTUPDATE_HOUR = "my_app_recommend_lastupdate_hour";
    public static final String PREF_SOFT_INPUT_HEIGHT = "soft_input_height";

    public static final String ACTION_MEETING = "com.inspur.meeting";
    public static final String ACTION_TASK = "com.inspur.task";
    public static final String ACTION_CALENDAR = "com.inspur.calendar";
}
