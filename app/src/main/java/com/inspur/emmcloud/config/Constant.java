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
    public static final String PREF_CLIENTID = "chat_clientid";
    public static final String PREF_WORK_INFO_BAR_OPEN = "work_open_info";
    public static final String PREF_WORK_PORTLET_CONFIG_UPLOAD = "is_work_portlet_config_upload";
    public static final String PREF_HAS_MY_APP_RECOMMEND = "has_my_app_recommend";
    public static final String PREF_MY_APP_RECOMMEND_DATA = "my_app_recommend_data";
    public static final String PREF_MY_APP_RECOMMEND_DATE = "my_app_recommend_date";
    public static final String PREF_MY_APP_RECOMMEND_EXPIREDDATE = "my_app_recommend_expired_date";
    public static final String PREF_MY_APP_RECOMMEND_LASTUPDATE_HOUR = "my_app_recommend_lastupdate_hour";
    public static final String PREF_SOFT_INPUT_HEIGHT = "soft_keybord_height";
    public static final String PREF_SELECT_LOGIN_ENTERPRISE_ID = "select_login_tenent_id";
    public static final String PREF_CURRENT_ENTERPRISE_ID = "current_enterprise_id";
    public static final String PREF_CONTACT_USER_LASTQUERYTIME = "contact_user_lastquerytime";
    public static final String PREF_CONTACT_ORG_LASTQUERYTIME = "contact_org_lastquerytime";
    public static final String PREF_MY_INFO_OLD = "my_info_old";
    public static final String PREF_CONTACT_ORG_ROOT_ID = "contact_org_root_id";
    public static final String PREF_APP_TAB_BAR_VERSION = "app_tab_bar_version";
    public static final String PREF_APP_TAB_BAR_INFO_CURRENT = "app_tab_bar_info_current";
    public static final String PREF_APP_LOAD_ALIAS = "app_load_alias";
    public static final String PREF_APP_OPEN_VOICE_WORD_SWITCH = "voice_2_word_switch";
    public static final String PREF_WEBEX_DOWNLOAD_URL = "webex_download_url";
    public static final String PREF_WEBEX_FIRST_ENTER = "webex_first_enter";
    public static final String PREF_GET_OFFLINE_LAST_MID = "get_offline_last_mid";
    public static final String PREF_LAST_LANGUAGE = "last_language";
    public static final String PREF_BADGE_NUM_COMMUNICATION = "badge_num_communication";
    public static final String PREF_BADGE_NUM_APPSTORE = "badge_num_appstore";
    public static final String PREF_BADGE_NUM_SNS = "badge_num_sns";
    public static final String PREF_MNM_DOUBLE_VALIADATION = "mdm_double_validation";//是否强制开启手势密码

    public static final String PREF_MDM_STATUS_PASS = "isMDMStatusPass";//是否设备管理通过

    public static final String PREF_V_CONFIG_ALL = "v_config_all";
    public static final String PREF_EXPERIENCE_UPGRATE = "experience_upgrade";
    public static final String PREF_APP_THEME = "app_theme_num_v1";
    public static final String APP_TAB_LAYOUT_NAME = "app_tab_layout_name";
    public static final String APP_TAB_LAYOUT_DATA = "app_tab_layout_data";
    public static final String PREF_LOGIN_USERNAME = "userName";
    public static final String PREF_LOGIN_PASSWORD = "password";
    public static final String PREF_CLOUD_IDM = "cloud_idm";

    public static final String PREF_APP_PREVIOUS_VERSION = "previousVersion";
    public static final String PREF_LOGIN_ENTERPRISE_NAME = "login_enterprise_name";
    public static final String PREF_LOGIN_HAVE_SET_PASSWORD = "hasPassword";
    public static final String PREF_MAIL_ACCOUNT = "mail_account";
    public static final String PREF_MAIL_PASSWORD = "mail_password";
    public static final String PREF_MY_INFO_SHOW_CONFIG = "user_profiles";
    public static final String PREF_MINE_USER_MENUS = "mine_user_menus";

    public static final String PREF_CALENDAR_EVENT_SHOW_TYPE = "calendar_event_show_type";
    public static final String PREF_MEETING_OFFICE_ID_LIST = "meeting_office_id_list";
    public static final String PREF_IS_MEETING_ADMIN = "is_meeting_admin";
    public static final String PREF_SERVER_SUPPORT_LANGUAGE = "server_support_language";
    public static final String PREF_CURRENT_LANGUAGE = "current_language";
    public static final String PREF_CURRENT_LANGUAGE_NAME = "current_language_name";
    public static final String PREF_IS_CONTACT_READY = "isContactReady";
    public static final String PREF_APP_BACKGROUND_TIME = "app_background_time";

    public static final String ACTION_MEETING = "com.inspur.meeting";
    public static final String ACTION_TASK = "com.inspur.task";
    public static final String ACTION_CALENDAR = "com.inspur.calendar";
    public static final String ACTION_VOLUME_INFO_UPDATE = "com.inspur.volume.info.update";

    public static final String DEFAULT_CLUSTER_EMM = "https://emm.inspur.com/";//EMM默认
    public static final String DEFAULT_CLUSTER_ECM = "https://ecm.inspur.com/";//ECM默认
    public static final String DEFAULT_CLUSTER_ID = "https://id.inspuronline.com/";//ID默认

    public static final String EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE = "tag_reveive_single_ws_message";
    public static final String EVENTBUS_TAG_GET_OFFLINE_WS_MESSAGE = "tag_get_offline_ws_message";
    public static final String EVENTBUS_TAG_GET_CHANNEL_RECENT_MESSAGE = "tag_get_channel_recent_message";
    public static final String EVENTBUS_TAG_GET_MESSAGE_BY_ID = "tag_get_message_by_id";
    public static final String EVENTBUS_TAG_GET_MESSAGE_COMMENT = "tag_get_message_comment";
    public static final String EVENTBUS_TAG_GET_HISTORY_MESSAGE = "tag_get_history_message";
    public static final String EVENTBUS_TAG_GET_NEW_MESSAGE = "tag_get_new_message";
    public static final String EVENTBUS_TAG_GET_MESSAGE_COMMENT_COUNT = "tag_get_message_comment_count";
    public static final String EVENTBUS_TAG_ON_PHOTO_TAB = "tag_on_photo_tab";
    public static final String EVENTBUS_TAG_ON_PHOTO_CLOSE = "tag_on_photo_close";
    public static final String EVENTBUS_TAG_WEBSOCKET_STATUS_REMOVE = "tag_websocket_status_remove";
    public static final String EVENTBUS_TAG_SET_CHANNEL_MESSAGE_READ = "tag_set_channel_message_read";
    public static final String EVENTBUS_TAG_RECERIVER_MESSAGE_STATE_READ = "tag_receive_message_state_read";
    public static final String EVENTBUS_TAG_SET_ALL_MESSAGE_UNREAD_COUNT = "tag_set_all_message_unread_count";
    public static final String EVENTBUS_TAG_OPEN_DEFALT_TAB = "tag_open_defalt_tab";
    public static final String EVENTBUS_TAG_REFRESH_CONVERSATION_ADAPTER = "tag_refresh_conversation_adapter";
    public static final String EVENTBUS_TAG_SEND_ACTION_CONTENT_MESSAGE = "tag_send_action_content_message";
    public static final String EVENTBUS_TAG_UPDATE_CHANNEL_NAME = "tag_update_channel_name";
    public static final String EVENTBUS_TAG_QUIT_CHANNEL_GROUP = "tag_quit_channel_group";
    public static final String EVENTBUS_TAG_UPDATE_CHANNEL_FOCUS = "tag_update_channel_focus";
    public static final String EVENTBUS_TAG_UPDATE_CHANNEL_DND = "tag_update_channel_dnd";
    public static final String EVENTBUS_TAG_CURRENT_CHANNEL_OFFLINE_MESSAGE = "tag_current_channel_offline_message";
    public static final String EVENTBUS_TAG_NET_STATE_CHANGE = "tag_net_state_change";
    public static final String EVENTBUS_TAG_NET_EXCEPTION_HINT = "tag_net_exception_hint";
    public static final String EVENTBUS_TAG_NET_PING_CONNECTION = "tag_net_ping_connection";
    public static final String EVENTBUS_TAG_NET_PORTAL_HTTP_POST = "tag_net_portal_http_post";
    public static final String EVENTBUS_TAG_NET_HTTP_POST_CONNECTION = "tag_net_http_post_connection";
    public static final String EVENTBUS_TAG_COMMENT_MESSAGE = "tag_comment_message";
    public static final String EVENTBUS_TAG_GET_MAIL_BY_FOLDER = "tag_get_mail_by_folder";
    public static final String EVENTBUS_TAG_MAIL_ACCOUNT_DELETE = "tag_mail_account_delelte";
    public static final String EVENTBUS_TAG_MAIL_LOGIN_SUCCESS = "tag_mail_login_success";
    public static final String EVENTBUS_TAG_MAIL_LOGIN_FAIL = "tag_mail_login_fail";
    public static final String EVENTBUS_TAG_MAIL_REMOVE = "tag_mail_remove";
    public static final String EVENTBUS_TAG_SCHEDULE_CALENDAR_SETTING_CHANGED = "tag_schedule_calendar_setting_changed";
    public static final String EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED = "tag_schedule_meeting_data_changed";
    public static final String EVENTBUS_TAG_SCHEDULE_TASK_DATA_CHANGED = "tag_schedule_task_data_changed";
    public static final String EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED = "tag_schedule_calendar_data_changed";
    public static final String EVENTBUS_TASK_ORDER_CHANGE = "task_order_change";
    public static final String EVENTBUS_TAG_SELECT_CALENDAR_CHANGED = "tag_select_calendar_changed";
    public static final String EVENTBUS_TAG_SAFE_UNLOCK = "tag_safe_unlock";//二次认证（刷脸、手势认证通过）

    public static final String SERVICE_VERSION_CHAT_V0 = "v0";
    public static final String SERVICE_VERSION_CHAT_V1 = "v1";

    public static final String APP_TAB_TYPE_NATIVE = "native";
    public static final String APP_TAB_TYPE_RN = "react-native";
    public static final String APP_TAB_TYPE_WEB = "web";

    public static final String APP_TAB_NAME_COMMUNACATE = "web";

    public static final String APP_TAB_BAR_COMMUNACATE = "native://communicate";
    public static final String APP_TAB_BAR_WORK = "native://work";
    public static final String APP_TAB_BAR_RN_FIND = "ecc-app-react-native://discover";
    public static final String APP_TAB_BAR_APPLICATION = "native://application";
    public static final String APP_TAB_BAR_PROFILE = "native://me";
    public static final String APP_TAB_BAR_CONTACT = "native://contact";
    public static final String APP_TAB_BAR_NOSUPPORT = "native://nosupport"; //lbc

    public static final String APP_TAB_BAR_COMMUNACATE_NAME = "communicate";
    public static final String APP_TAB_BAR_WORK_NAME = "work";
    public static final String APP_TAB_BAR_APPLICATION_NAME = "application";
    public static final String APP_TAB_BAR_MOMENT_NAME = "moment";
    public static final String APP_TAB_BAR_ME_NAME = "me";
    public static final String APP_TAB_BAR_CONTACT_NAME = "contact";
    public static final String APP_TAB_BAR_DISCOVER_NAME = "discover";

    public static final String SHARE_LINK = "shareLink";
    public static final String SHARE_FILE_URI_LIST = "fileShareUriList";

    public static final String WEB_FRAGMENT_VERSION = "version";
    public static final String WEB_FRAGMENT_APP_NAME = "appName";
    public static final String WEB_FRAGMENT_MENU = "menuList";
    public static final String WEB_FRAGMENT_SHOW_HEADER = "show_webview_header";

    public static final String INSPUR_HOST_URL = ".inspur.com";
    public static final String INSPURONLINE_HOST_URL = ".inspuronline.com";

    public static final String BATTERY_WHITE_LIST_STATE = "battery_white_list_state";

    //推送相关配置
    public static final String PUSH_FLAG = "pushFlag";
    public static final String HUAWEI_PUSH_TOKEN = "huawei_push_token";
    public static final String PUSH_SWITCH_FLAG = "push_switch_flag";
    public static final String JPUSH_REGISTER_ID = "JpushRegId";
    public static final String MIPUSH_REGISTER_ID = "mi_push_register_id";
    public static final String HUAWEI_FLAG = "huawei";
    public static final String JPUSH_FLAG = "Jpush";
    public static final String XIAOMI_FLAG = "xiaomi";
    public static final String MEIZU_FLAG = "meizu";
    public static final String PUSH_HUAWEI_COM = "@push.huawei.com";
    public static final String PUSH_XIAOMI_COM = "@push.xiaomi.com";

    public static final String APP_WEB_URI = "uri";

    public static final int APP_EXCEPTION_LEVEL = 4;

    //邮箱
    public static final String MAIL_ENCRYPT_IV = "inspurcloud+2019";

    public static final String SF_OPEN_URL = "open-url";

    //沟通文本消息长按至日程
    public static final String COMMUNICATION_LONG_CLICK_TO_SCHEDULE = "message_long_click_to_schedule";

    public static final String SCHEDULE_QUERY = "schedule_query";
    public static final String SCHEDULE_DETAIL = "schedule_detail";

}
