package com.inspur.emmcloud.basemodule.config;

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
    public static final String CONCIG_FORCE_PULL_MESSAGE = "force_pull_message";
    public static final String CONCIG_FORCE_PULL_BADGE = "force_pull_badge";
    public static final String CONCIG_UPDATE_2_NEWVERSION = "forceUpdate20";//更新到云+2.0  即 云+协同应用
    public static final String CONCIG_CLOUD_PLUS_UUID_FILE = Environment.getExternalStorageDirectory() + "/gsp.uuid";


    public static final String PATTERN_URL = "(((https?)://[a-zA-Z0-9\\_\\-]+(\\.[a-zA-Z0-9\\_\\-]+)*(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?)" +
            "|([a-zA-Z0-9\\-\\_]+\\.)+([a-zA-Z\\-\\_]+)(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?|\\d+(\\.\\d+){3}(\\:\\d{2,4})?)";
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
    public static final String PREF_APP_OPEN_VOICE_WORD_SWITCH = "voice_2_word_switch_default_false";
    public static final String PREF_APP_OPEN_NATIVE_ROTATE_SWITCH = "native_rotate_switch_default_false";
    public static final String PREF_WEBEX_DOWNLOAD_URL = "webex_download_url";
    public static final String PREF_WEBEX_FIRST_ENTER = "webex_first_enter";
    public static final String PREF_GET_OFFLINE_LAST_MID = "get_offline_last_mid";
    public static final String PREF_LAST_LANGUAGE = "last_language";
    public static final String PREF_BADGE_NUM_COMMUNICATION = "badge_num_communication";
    public static final String PREF_BADGE_NUM_APPSTORE = "badge_num_appstore";
    public static final String PREF_BADGE_NUM_SNS = "badge_num_sns";
    public static final String PREF_DESKTOP_BADGE = "push_desktop_badge";
    public static final String PREF_MNM_DOUBLE_VALIADATION = "mdm_double_validation";//是否强制开启手势密码

    public static final String PREF_MDM_STATUS_PASS = "isMDMStatusPass";//是否设备管理通过

    public static final String PREF_V_CONFIG_ALL = "v_config_all";
    public static final String PREF_EXPERIENCE_UPGRATE = "experience_upgrade";
    public static final String PREF_APP_THEME = "app_theme_num_v1";
    public static final String PREF_FOLLOW_SYSTEM_THEME = "pref_follow_system_theme"; // 是否跟随系统打开或关闭深色模式
    public static final String APP_TAB_LAYOUT_NAME = "app_tab_layout_name";
    public static final String APP_TAB_LAYOUT_DATA = "app_tab_layout_data";
    public static final String PREF_LOGIN_USERNAME = "userName";
    public static final String PREF_LOGIN_PASSWORD = "password";
    public static final String PREF_CLOUD_IDM = "cloud_idm";

    public static final String PREF_APP_PREVIOUS_VERSION = "previousVersion";
    public static final String PREF_LOGIN_ENTERPRISE_NAME = "login_enterprise_name";
    public static final String PREF_LOGIN_HAVE_SET_PASSWORD = "hasPassword";
    public static final String PREF_MAIL_ACCOUNT = "exchange_account";
    public static final String PREF_MAIL_PASSWORD = "exchange_pw";
    public static final String PREF_MY_INFO_SHOW_CONFIG = "user_profiles";
    public static final String PREF_MINE_USER_MENUS = "mine_user_menus";

    public static final String PREF_CALENDAR_EVENT_SHOW_TYPE = "calendar_event_show_type";
    public static final String PREF_MEETING_OFFICE_ID_LIST = "meeting_office_id_list";
    public static final String PREF_IS_MEETING_ADMIN = "is_meeting_admin";
    public static final String PREF_LANGUAGE_CURRENT_LOCAL = "language_current_local";
    public static final String PREF_SERVER_SUPPORT_LANGUAGE = "server_support_language";
    public static final String PREF_CURRENT_LANGUAGE = "current_language";
    public static final String PREF_VOICE_INPUT_LANGUAGE = "voice_input_language";
    public static final String PREF_CURRENT_LANGUAGE_NAME = "current_language_name";
    public static final String PREF_IS_CONTACT_READY = "isContactReady";
    public static final String PREF_APP_BACKGROUND_TIME = "app_background_time";
    public static final String PREF_SCHEDULE_ENABLE_EXCHANGE = "schedule_enable_exchange";
    public static final String PREF_SCHEDULE_BASIC_DATA_VERSION = "schedule_basic_data_version";
    public static final String PREF_SCHEDULE_HOLIDAY_STATE = "schedule_holiday_state";
    public static final String PREF_VOLUME_FILE_SORT_TYPE = "volume_file_sort_type";

    public static final String ACTION_MEETING = "com.inspur.meeting";
    public static final String ACTION_TASK = "com.inspur.task";
    public static final String ACTION_CALENDAR = "com.inspur.calendar";
    public static final String ACTION_VOLUME_INFO_UPDATE = "com.inspur.volume.info.update";

    public static final String DEFAULT_CLUSTER_EMM = "https://emm.inspur.com/";//EMM默认
    public static final String DEFAULT_CLUSTER_ECM = "https://ecm.inspur.com/";//ECM默认
    public static final String DEFAULT_CLUSTER_ID = "https://id.inspuronline.com/";//ID默认
    public static final String DEFAULT_CLUSTER_ID_ZHIHUIGUOZI = "http://cloud-id.sasac.tj.gov.cn/";

    public static final String EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE = "tag_reveive_single_ws_message";
    public static final String EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE_CONVERSATION = "tag_reveive_single_ws_message_conversation";
    public static final String EVENTBUS_TAG_GET_OFFLINE_WS_MESSAGE = "tag_get_offline_ws_message";
    public static final String EVENTBUS_TAG_GET_CHANNEL_RECENT_MESSAGE = "tag_get_channel_recent_message";
    public static final String EVENTBUS_TAG_GET_MESSAGE_BY_ID = "tag_get_message_by_id";
    public static final String EVENTBUS_TAG_GET_MESSAGE_COMMENT = "tag_get_message_comment";
    public static final String EVENTBUS_TAG_SEND_VOICE_CALL_MESSAGE = "tag_send_voice_call_mesage";
    public static final String EVENTBUS_TAG_GET_HISTORY_MESSAGE = "tag_get_history_message";
    public static final String EVENTBUS_TAG_DELETE_UNREAD_MESSAGE = "tag_delete_unread_message";
    public static final String EVENTBUS_TAG_GET_NEW_MESSAGE = "tag_get_new_message";
    public static final String EVENTBUS_TAG_REFRESH_CONVERSATION = "tag_refresh_conversation";
    public static final String EVENTBUS_TAG_GET_MESSAGE_COMMENT_COUNT = "tag_get_message_comment_count";
    public static final String EVENTBUS_TAG_RECALL_MESSAGE = "tag_recall_message";
    public static final String EVENTBUS_TAG_CURRENT_CHANNEL_RECALL_MESSAGE = "tag_current_channel_recall_message";
    public static final String EVENTBUS_TAG_COMMAND_BATCH_MESSAGE = "tag_command_batch_message";
    public static final String EVENTBUS_TAG_CURRENT_CHANNEL_COMMAND_BATCH_MESSAGE = "tag_current_channel_command_batch_message";
    public static final String EVENTBUS_TAG_VIDEO_CALL = "client.video.call";
    public static final String EVENTBUS_TAG_ON_PHOTO_TAB = "tag_on_photo_tab";
    public static final String EVENTBUS_TAG_ON_PHOTO_CLOSE = "tag_on_photo_close";
    //频道本身属性发生变化（非消息导致）
    public static final String EVENTBUS_TAG_CONVERSATION_SELF_DATA_CHANGED = "tag_conversation_self_data_changed";
    //频道消息发生变化
    public static final String EVENTBUS_TAG_CONVERSATION_MESSAGE_DATA_CHANGED = "tag_conversation_message_data_changed";
    public static final String EVENTBUS_TAG_WEBSOCKET_STATUS_REMOVE = "tag_websocket_status_remove";
    public static final String EVENTBUS_TAG_SET_CHANNEL_MESSAGE_READ = "tag_set_channel_message_read";
    public static final String EVENTBUS_TAG_RECERIVER_MESSAGE_STATE_READ = "tag_receive_message_state_read";
    public static final String EVENTBUS_TAG_SET_ALL_MESSAGE_UNREAD_COUNT = "tag_set_all_message_unread_count";
    public static final String EVENTBUS_TAG_CHANNEL_MESSAGE_STATES = "tag_set_channel_message_states";
    public static final String EVENTBUS_TAG_OPEN_DEFALT_TAB = "tag_open_defalt_tab";
    public static final String EVENTBUS_TAG_REFRESH_CONVERSATION_ADAPTER = "tag_refresh_conversation_adapter";
    public static final String EVENTBUS_TAG_SEND_ACTION_CONTENT_MESSAGE = "tag_send_action_content_message";
    public static final String EVENTBUS_TAG_UPDATE_CHANNEL_NAME = "tag_update_channel_name";
    public static final String EVENTBUS_TAG_UPDATE_CHANNEL_MEMBERS = "tag_update_channel_members";
    public static final String EVENTBUS_TAG_QUIT_CHANNEL_GROUP = "tag_quit_channel_group";
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
    public static final String EVENTBUS_TAG_SCHEDULE_HOLIDAY_CHANGE = "tag_schedule_holiday_changed";
    public static final String EVENTBUS_TAG_SCHEDULE_TASK_DATA_CHANGED = "tag_schedule_task_data_changed";
    public static final String EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED = "tag_schedule_calendar_data_changed";
    public static final String EVENTBUS_TAG_SCHEDULE_HIDE_EXCHANGE_ACCOUNT_ERROR = "tag_schedule_hide_exchange_account_error";
    public static final String EVENTBUS_TASK_ORDER_CHANGE = "task_order_change";
    public static final String EVENTBUS_TAG_SELECT_CALENDAR_CHANGED = "tag_select_calendar_changed";
    public static final String EVENTBUS_TAG_SAFE_UNLOCK = "tag_safe_unlock";//二次认证（刷脸、手势认证通过）
    public static final String EVENTBUS_TAG_CHAT_CHANGE = "tag_chat_change"; //会议详情发起群聊后，通知聊天界面同步
    public static final String EVENTBUS_TAG_SCHEDULE_MEETING_COMMON_OFFICE_CHANGED = "tag_schedule_meeting_common_office_changed";
    public static final String EVENTBUS_TAG_OPEN_WORK_TAB = "tag_open_work_tab";
    public static final String EVENTBUS_TAG_EWS_401 = "tag_ews_401";
    public static final String EVENTBUS_TAG_VOLUME_FILE_SORT_TIME_CHANGED = "volume_file_sort_time_changed";
    public static final String EVENTBUS_TAG_VOLUME_FILE_LOCATION_SELECT_CLOSE = "volume_file_locaiton_select_close";
    public static final String EVENTBUS_TAG_MESSAGE_ADD_SHOW_CONTENT = "tag_message_add_show_content";
    public static final String EVENTBUS_TAG_CONVERSATION_ADD_SHOW_CONTENT = "tag_conversation_add_show_content";
    public static final String EVENTBUS_TAG_VOLUME_FILE_UPLOAD_SUCCESS = "tag_volume_file_upload_success";
    public static final String EVENTBUS_TAG_VOLUME_FILE_DOWNLOAD_SUCCESS = "tag_volume_file_download_success";
    public static final String EVENTBUS_TAG_VOLUME_ = "tag_volume_upload";
    public static final String EVENTBUS_TAG_WECHAT_RESULT = "tag_wechat_result";
    public static final String EVENTBUS_TAG_REFRESH_VOICE_CALL_SMALL_WINDOW = "tag_wechat_result";
    public static final String SERVICE_VERSION_CHAT_V0 = "v0";
    public static final String SERVICE_VERSION_CHAT_V1 = "v1";
    public static final String COMMAND_ECC_CLOUDPLUS_CMD = "ecc-cloudplus-cmd";
    public static final String COMMAND_INVITE = "invite";
    public static final String COMMAND_REFUSE = "refuse";
    public static final String COMMAND_DESTROY = "destroy";
    public static final String COMMAND_CMD = "cmd";
    public static final String COMMAND_UID = "uid";
    public static final String COMMAND_CHANNEL_ID = "channelid";
    public static final String COMMAND_ROOM_ID = "roomid";
    /**
     * agora的channelId
     */
    public static final String VOICE_VIDEO_CALL_AGORA_ID = "channelId";
    /**
     * 通话类型ECMChatInputMenu.VIDEO_CALL或者ECMChatInputMenu.VOICE_CALL
     */
    public static final String VOICE_VIDEO_CALL_TYPE = "voice_video_call_type";
    /**
     * 通话中来自schema的uid，这个uid表示来自云+中的哪个人
     */
    public static final String SCHEMA_FROM_UID = "voice_video_UID";
    /**
     * 传递页面布局样式的
     */
    public static final String VOICE_COMMUNICATION_STATE = "voice_communication_state";
    /**
     * 传递页面是否来自小窗
     */
    public static final String VOICE_IS_FROM_SMALL_WINDOW = "voice_is_from_window";
//    /**
//     * 屏幕宽度
//     */
//    public static final String SCREEN_SIZE = "screen_size";

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
    public static final String WEB_FRAGMENT_TITLE_PRIORITY_FIRST = "title_priority_first";
    public static final String WEB_FRAGMENT_BAR_TINT_COLOR = "bar_tint_color";
    public static final String WEB_FRAGMENT_TITLE = "title";
    public static final String WEB_FRAGMENT_TITLE_COLOR = "title_color";
    public static final String WEB_FRAGMENT_TITLE_IMAGE = "title_image";
    public static final String WEB_FRAGMENT_TITLE_BAR_HEIGHT = "title_bar_height";
    public static final String Web_STATIC_TITLE = "web_from_index";

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
    public static final String EXTRA_SCHEDULE_TITLE_EVENT = "schedule_title_event";
    //COMMUNICATION_LONG_CLICK_TO_SCHEDULE

    public static final String SCHEDULE_QUERY = "schedule_query";
    public static final String OPEN_SCHEDULE_DETAIL = "schedule_detail";


    public static final String SMS_LOGIN_START_TIME = "sms_login_start_time";
    public static final String EXTRA_IS_HISTORY_MEETING = "is_history_meeting"; //来源  历史会议

    public static final String AROUTER_CLASS_COMMUNICATION_MEMBER = "/communication/MembersActivity";
    public static final String AROUTER_CLASS_COMMUNICATION_IMAGEPAGER = "/communication/ImagePagerActivity";
    public static final String AROUTER_CLASS_COMMUNICATION_SHARE_FILE = "/communication/ShareFilesActivity";
    public static final String AROUTER_CLASS_COMMUNICATION_MEMBER_DEL = "/communication/ChannelMembersDelActivity";
    public static final String AROUTER_CLASS_CONVERSATION = "/communication/ConversationActivity";

    public static final String AROUTER_CLASS_LOGIN_MAIN = "/login/LoginActivity";
    public static final String AROUTER_CLASS_LOGIN_PASSWORD_MODIFY = "/login/PasswordModifyActivity";
    public static final String AROUTER_CLASS_LOGIN_GS = "/login/ScanQrCodeLoginGSActivity";
    public static final String AROUTER_CLASS_LOGIN_BY_SMS = "/login/LoginBySmsActivity";


    public static final String AROUTER_CLASS_FILEMANAGER_WITH_VOLUME = "/App/NativeVolumeFileManagerActivity";

    public static final String AROUTER_CLASS_WEB_FILEMANAGER = "/web/FileManagerActivity";
    public static final String AROUTER_CLASS_WEB_SCANRESULT = "/web/ScanResultActivity";
    public static final String AROUTER_CLASS_WEB_MAIN = "/web/ImpActivity";
    public static final String AROUTER_CLASS_WEB_MAIN_TEST = "/web/ScanResultActivity";

    public static final String AROUTER_CLASS_CONTACT_SEARCH = "/contact/ContactSearchActivity"; //通讯录选择界面
    public static final String AROUTER_CLASS_CONVERSATION_SEARCH = "/chat/ConversationSearchActivity"; //最近联系人选择界面
    public static final String AROUTER_CLASS_CONTACT_USERINFO = "/contact/UserInfoActivity";

    public static final String AROUTER_CLASS_SETTING_SERVICE_NO_PERMISSION = "/setting/NoPermissionDialogActivity";

    public static final String AROUTER_CLASS_APP_INDEX = "/app/IndexActivity";
    public static final String AROUTER_CLASS_APP_NETWORK_DETAIL = "/setting/NetWorkStateDetailActivity";
    public static final String AROUTER_CLASS_APP_WEB_ERROR_DETAIL = "/setting/WebViewNetStateDetailActivity";
    public static final String AROUTER_CLASS_APPCENTER_REACT_NATIVE = "/app/ReactNativeAppActivity";

    public static final String AROUTER_CLASS_AROUTER_DEGRADE = "/aRouter/degrade";
    public static final String AROUTER_CLASS_MAIL_LOGIN = "/mail/login";
    public static final String AROUTER_CLASS_MAIL_HOME = "/mail/home";
    public static final String AROUTER_CLASS_GROUP_NEWS = "/group/news";
    public static final String AROUTER_CLASS_WEBEX_MAIN = "/webex/WebexMyMeetingActivity";
    public static final String AROUTER_CLASS_VOLUME_HOME = "/volume/home";

    public static final String AROUTER_CLASS_APPCENTER_TEST = "/application/ApplicationTestActivity";
    public static final String AROUTER_CLASS_APPCENTER = "/application/AppCenterActivity";
    public static final String AROUTER_CLASS_APPCENTER_MORE = "/application/AppCenterMoreActivity";
    public static final String AROUTER_CLASS_APPCENTER_DETAIL = "/application/AppDetailActivity";
    public static final String AROUTER_CLASS_APPCENTER_GROUP = "/application/AppGroupActivity";
    public static final String AROUTER_CLASS_APPCENTER_SEARCH = "/application/AppSearchActivity";

    public static final String AROUTER_CLASS_SCHEDLE_TEST = "/schedule/test";
    public static final String AROUTER_CLASS_SCHEDLE_ADD = "/schedule/add";
    public static final String AROUTER_CLASS_SCHEDLE_DETAIL = "/schedule/detail";

    public static final String AROUTER_CLASS_APP_MAIN = "/app/main";
    public static final String AROUTER_CLASS_APP_CONVERSATION_V1 = "/app/ConversationActivity";
    public static final String AROUTER_CLASS_APP_CHANNEL_V0 = "/app/ChannelV0Activity";

    public static final String AROUTER_CLASS_SETTING_CREATE_GESTURE = "/setting/CreateGestureActivity";
    public static final String AROUTER_CLASS_SETTING_TEST = "/setting/test";
    public static final String AROUTER_CLASS_SETTING_GUIDE = "/setting/guide";
    public static final String AROUTER_CLASS_SETTING_SETTING = "/setting/setting";
    public static final String AROUTER_CLASS_SETTING_FACEVERIFY = "/setting/FaceVerify";
    public static final String AROUTER_CLASS_SETTING_SERVICE_TERM = "/setting/serviceTerm";


    //参会状态
    public static final int ATTEND_MEETING_STATUS_ACCEPT = 1;
    public static final int ATTEND_MEETING_STATUS_REJECT = 2;
    public static final int ATTEND_MEETING_STATUS_TENTATIVE = 3;
    public static final String APP_MYAPP_LIST_FROM_NET = "my_app_list_from_net";

    public static final String SAFE_CENTER_FINGER_PRINT = "safe_center_finger_print";
    //分享内容
    public static final String SHARE_CONTENT = "share_content";
    //TODO:更新除微信外appid
    public static final String UMENT_APPKEY = "5c6cf18ff1f55645e900092a";
    public static final String WECHAT_APPID = "wx4eb8727ea9c26495";
    public static final String WECHAT_APP_SECRET = "b0f39cb7f260634d5b0b4a3fe0399f8a";
    public static final String QQ_APPID = "101983127";
    public static final String QQ_APP_SECRET = "d6c027d90ae69817b9a03a16bef8e7f8";

    public static final String VIDEO_CALL_INVITE = "server.chat.video-call.invite";
    public static final String VIDEO_CALL_REFUSE = "server.chat.video-call.refuse";
    public static final String VIDEO_CALL_HANG_UP = "server.chat.video-call.hang-up";
    /**
     * 云盘
     */
    public static final String TYPE_DOWNLOAD = "download";
    public static final String TYPE_UPLOAD = "upload";

    /**
     * 语音转文字开关
     */
    public static final boolean IS_VOICE_WORD_OPEN = true;
    public static final boolean IS_VOICE_WORD_CLOUSE = false;

    public static final String APP_ROLE = "app_role";

    public static final int APP_THEME_DARK = 3;
}
