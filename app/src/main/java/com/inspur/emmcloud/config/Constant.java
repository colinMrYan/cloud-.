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
    public static final String PREF_SOFT_INPUT_HEIGHT = "soft_input_height";
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
    public static final String PREF_LAST_LANGUAGE = "get_offline_last_mid";

    public static final String PREF_V_CONFIG_ALL = "v_config_all";
    public static final String PREF_EXPERIENCE_UPGRATE = "experience_upgrade";

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
    public static final String EVENTBUS_TAG__NET_STATE_CHANGE = "tag_net_state_change";
    public static final String EVENTBUS_TAG__NET_EXCEPTION_HINT = "tag_net_exception_hint";

    public static final String SERVICE_VERSION_CHAT_V0 = "v0";
    public static final String SERVICE_VERSION_CHAT_V1 = "v1";

    public static final String APP_TAB_TYPE_NATIVE = "native";
    public static final String APP_TAB_TYPE_RN = "react-native";
    public static final String APP_TAB_TYPE_WEB = "web";

    public static final String APP_TAB_BAR_COMMUNACATE = "native://communicate";
    public static final String APP_TAB_BAR_WORK = "native://work";
    public static final String APP_TAB_BAR_RN_FIND = "ecc-app-react-native://discover";
    public static final String APP_TAB_BAR_APPLICATION = "native://application";
    public static final String APP_TAB_BAR_PROFILE = "native://me";
    public static final String APP_TAB_BAR_CONTACT = "native://contact";
    public static final String APP_TAB_BAR_NOSUPPORT = "native://nosupport"; //lbc

    public static final String SHARE_LINK = "shareLink";
    public static final String SHARE_FILE_URI_LIST = "fileShareUriList";

    public static final String WEB_FRAGMENT_VERSION = "version";
    public static final String WEB_FRAGMENT_APP_NAME = "appName";
    public static final String WEB_FRAGMENT_MENU = "menuList";

    public static final String INSPUR_HOST_URL = ".inspur.com";
    public static final String INSPURONLINE_HOST_URL = ".inspuronline.com";

    //推送相关配置
    public static final String PUSH_FLAG = "pushFlag";
    public static final String HUAWEI_PUSH_TOKEN = "huawei_push_token";
    public static final String JPUSH_REG_ID = "JpushRegId";
    public static final String HUAWEI_FLAG = "huawei";
    public static final String JPUSH_FLAG = "Jpush";
    public static final String XIAOMI_FLAG = "xiaomi";
    public static final String MEIZU_FLAG = "meizu";
    public static final String PUSH_HUAWEI_COM = "@push.huawei.com";

    public static final String APP_WEB_URI = "uri";


}
