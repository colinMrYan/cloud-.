package com.inspur.emmcloud.api;


import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.AppRedirectResult;
import com.inspur.emmcloud.bean.Attachment;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.GetAddAppResult;
import com.inspur.emmcloud.bean.GetAddMembersSuccessResult;
import com.inspur.emmcloud.bean.GetAdressUsersResult;
import com.inspur.emmcloud.bean.GetAllAppResult;
import com.inspur.emmcloud.bean.GetAllContactResult;
import com.inspur.emmcloud.bean.GetAllRobotsResult;
import com.inspur.emmcloud.bean.GetAppBadgeResult;
import com.inspur.emmcloud.bean.GetAppConfigResult;
import com.inspur.emmcloud.bean.GetAppGroupResult;
import com.inspur.emmcloud.bean.GetAppTabAutoResult;
import com.inspur.emmcloud.bean.GetAppTabsResult;
import com.inspur.emmcloud.bean.GetBindingDeviceResult;
import com.inspur.emmcloud.bean.GetBoolenResult;
import com.inspur.emmcloud.bean.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.GetCardPackageListResult;
import com.inspur.emmcloud.bean.GetChannelInfoResult;
import com.inspur.emmcloud.bean.GetChannelListResult;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.bean.GetCreateOfficeResult;
import com.inspur.emmcloud.bean.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.GetDeviceCheckResult;
import com.inspur.emmcloud.bean.GetDeviceLogResult;
import com.inspur.emmcloud.bean.GetFileUploadResult;
import com.inspur.emmcloud.bean.GetFindMixSearchResult;
import com.inspur.emmcloud.bean.GetFindSearchResult;
import com.inspur.emmcloud.bean.GetGroupNewsDetailResult;
import com.inspur.emmcloud.bean.GetIDResult;
import com.inspur.emmcloud.bean.GetIsAdmin;
import com.inspur.emmcloud.bean.GetKnowledgeInfo;
import com.inspur.emmcloud.bean.GetLanguageResult;
import com.inspur.emmcloud.bean.GetLoctionResult;
import com.inspur.emmcloud.bean.GetLoginResult;
import com.inspur.emmcloud.bean.GetMDMStateResult;
import com.inspur.emmcloud.bean.GetMeetingListResult;
import com.inspur.emmcloud.bean.GetMeetingReplyResult;
import com.inspur.emmcloud.bean.GetMeetingRoomsResult;
import com.inspur.emmcloud.bean.GetMeetingsResult;
import com.inspur.emmcloud.bean.GetMsgCommentCountResult;
import com.inspur.emmcloud.bean.GetMsgCommentResult;
import com.inspur.emmcloud.bean.GetMsgResult;
import com.inspur.emmcloud.bean.GetMyAppResult;
import com.inspur.emmcloud.bean.GetMyCalendarResult;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.GetNewMsgsResult;
import com.inspur.emmcloud.bean.GetNewsImgResult;
import com.inspur.emmcloud.bean.GetNewsInstructionResult;
import com.inspur.emmcloud.bean.GetNewsResult;
import com.inspur.emmcloud.bean.GetNewsTitleResult;
import com.inspur.emmcloud.bean.GetOfficeResult;
import com.inspur.emmcloud.bean.GetRegisterCheckResult;
import com.inspur.emmcloud.bean.GetRegisterResult;
import com.inspur.emmcloud.bean.GetRemoveAppResult;
import com.inspur.emmcloud.bean.GetSearchAppResult;
import com.inspur.emmcloud.bean.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.GetSendMsgResult;
import com.inspur.emmcloud.bean.GetSignoutResult;
import com.inspur.emmcloud.bean.GetTagResult;
import com.inspur.emmcloud.bean.GetTaskAddResult;
import com.inspur.emmcloud.bean.GetTaskListResult;
import com.inspur.emmcloud.bean.GetTripArriveCity;
import com.inspur.emmcloud.bean.GetUpdatePwdBySMSCodeBean;
import com.inspur.emmcloud.bean.GetUpgradeResult;
import com.inspur.emmcloud.bean.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.GetUserHeadUploadResult;
import com.inspur.emmcloud.bean.GetWebSocketUrlResult;
import com.inspur.emmcloud.bean.LoginDesktopCloudPlusBean;
import com.inspur.emmcloud.bean.ReactNativeDownloadUrlBean;
import com.inspur.emmcloud.bean.ReactNativeInstallUriBean;
import com.inspur.emmcloud.bean.ReactNativeUpdateBean;
import com.inspur.emmcloud.bean.Robot;
import com.inspur.emmcloud.bean.SplashPageBean;
import com.inspur.emmcloud.bean.TaskResult;
import com.inspur.emmcloud.bean.Trip;
import com.inspur.emmcloud.bean.UserProfileInfoBean;


public interface APIInterface {

    void returnOauthSigninSuccess(GetLoginResult getLoginResult);

    void returnOauthSigninFail(String error, int errorCode);

    void returnAllAppsSuccess(GetAllAppResult getAllAppResult);

    void returnAllAppsFail(String error, int errorCode);

    void returnAllAppsFreshSuccess(GetAllAppResult getAllAppResult);

    void returnAllAppsFreshFail(String error, int errorCode);

    void returnAllAppsMoreSuccess(GetAllAppResult getAllAppResult);

    void returnAllAppsMoreFail(String error, int errorCode);

    void returnAddAppSuccess(GetAddAppResult getAddAppResult);

    void returnAddAppFail(String error, int errorCode);

    void returnRemoveAppSuccess(GetRemoveAppResult getRemoveAppResult);

    void returnRemoveAppFail(String error, int errorCode);

    void returnMyAppSuccess(GetMyAppResult getMyAppResult);

    void returnMyAppFail(String error, int errorCode);

    void returnSignoutSuccess(GetSignoutResult getSignoutResult);

    void returnSignoutFail(String error, int errorCode);

    void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult, boolean isManualCheck);

    void returnUpgradeFail(String error, boolean isManualCheck, int errorCode);

    void returnSearchAppSuccess(GetSearchAppResult getAllAppResult);

    void returnSearchAppFail(String error, int errorCode);

    void returnSearchAppMoreSuccess(GetSearchAppResult getAllAppResult);

    void returnSearchAppMoreFail(String error, int errorCode);

    void returnReqLoginSMSSuccess(GetBoolenResult getBoolenResult);

    void returnReqLoginSMSFail(String error, int errorCode);


    void returnRegisterSMSSuccess(GetRegisterResult getRegisterResult);

    void returnRegisterSMSFail(String error, int errorCode);

    void returnReisterSMSCheckSuccess(GetRegisterCheckResult getRegisterResult);

    void returnReisterSMSCheckFail(String error, int errorCode);

    void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult);

    void returnMyInfoFail(String error, int errorCode);


    void returnUsersInOrgSuccess(GetAdressUsersResult getAdressUsersResult);

    void returnUsersInOrgFail(String error, int errorCode);

    void returnUploadMyHeadSuccess(GetUploadMyHeadResult getUploadMyInfoResult);

    void returnUploadMyHeadFail(String error, int errorCode);

    void returnChannelListSuccess(GetChannelListResult getSessionListResult);

    void returnChannelListFail(String error, int errorCode);

    void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult);

    void returnNewMsgsFail(String error, int errorCode);


    void returnMsgCommentSuccess(GetMsgCommentResult getMsgCommentResult, String mid);

    void returnMsgCommentFail(String error, int errorCode);

    void returnMsgCommentCountSuccess(GetMsgCommentCountResult getMsgCommentCountResult, String mid);

    void returnMsgCommentCountFail(String error, int errorCode);

    void returnNewsSuccess(GetNewsResult getNewsResult);

    void returnNewsFail(String error, int errorCode);

    void returnModifyUserInfoSucces(GetBoolenResult getBoolenResult);

    void returnModifyUserInfoFail(String error, int errorCode);

    void returnWebSocketUrlSuccess(GetWebSocketUrlResult getWebSocketResult);

    void returnWebSocketUrlFail(String error, int errorCode);

    void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult, String fakeMessageId);

    void returnSendMsgFail(String error, String fakeMessageId, int errorCode);

    void returnUploadMsgImgSuccess(GetNewsImgResult getNewsImgResult, String fakeMessageId);

    void returnUploadMsgImgFail(String error, int errorCode);

    void returnGroupNewsTitleSuccess(GetNewsTitleResult getNewsTitleResult);

    void returnGroupNewsTitleFail(String error, int errorCode);

    void returnGroupNewsDetailSuccess(GetGroupNewsDetailResult getGroupNewsDetailResult,int page);

    void returnGroupNewsDetailFail(String error, int errorCode,int page);

    void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult);

    void returnMeetingsFail(String error, int errorCode);

    void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult, boolean isLoadMore);

    void returnMeetingRoomsSuccess(GetMeetingRoomsResult getMeetingRoomsResult);

    void returnMeetingRoomsFail(String error, int errorCode);

    void returnMeetingRoomsSuccess(GetMeetingRoomsResult getMeetingRoomsResult, boolean isFilte);

    void returnMsgSuccess(GetMsgResult getMsgResult);

    void returnMsgFail(String error, int errorCode);

    void returnBookingRoomSuccess();

    void returnBookingRoomFail(String error, int errorCode);

    void returnChannelInfoSuccess(GetChannelInfoResult getChannelInfoResult);

    void returnChannelInfoFail(String error, int errorCode);

    void returnAllContactSuccess(GetAllContactResult getAllContactResult);

    void returnAllContactFail(String error, int errorCode);

    void returnFileUpLoadSuccess(GetFileUploadResult getFileUploadResult, String fakeMessageId);

    void returnFileUpLoadFail(String error, int errorCode);

    void returnSearchChannelGroupSuccess(GetSearchChannelGroupResult getSearchChannelGroupResult);

    void returnSearchChannelGroupFail(String error, int errorCode);

    void returnUserHeadUploadSuccess(GetUserHeadUploadResult getUserHeadUploadResult);

    void returnUserHeadUploadFail(String error, int errorCode);

    void returnCreateSingleChannelSuccess(GetCreateSingleChannelResult getCreatSingleChannelResult);

    void returnCreatSingleChannelFail(String error, int errorCode);

    void returnCreatChannelGroupSuccess(ChannelGroup channelGroup);

    void returnCreateChannelGroupFail(String error, int errorCode);

    void returnGetMeetingReplySuccess(GetMeetingReplyResult getMeetingReplyResult);

    void returnGetMeetingReplyFail(String error, int errorCode);

    void returnTripSuccess(Trip trip);

    void returnTripFail(String error, int errorCode);

    void returnLastUploadTripSuccess(Trip trip);

    void returnLastUploadTripFail(String error, int errorCode);

    void returnUpdateChannelGroupNameSuccess(GetBoolenResult getBoolenResult);

    void returnUpdateChannelGroupNameFail(String error, int errorCode);

    void returnMeetingListSuccess(GetMeetingListResult getMeetingListResult, String date);

    void returnMeetingListSuccess(GetMeetingListResult getMeetingListResult);

    void returnMeetingListFail(String error, int errorCode);

    void returnUploadTrainTicketSuccess();

    void returnUploadTrainTicketFail(String error, int errorCode);

    void returnUploadExceptionSuccess();

    void returnUploadExceptionFail(String error, int errorCode);

    void returnLoctionResultSuccess(GetLoctionResult getLoctionResult);

    void returnLoctionResultFail(String error, int errorCode);

    void returnOfficeResultSuccess(GetOfficeResult getOfficeResult);

    void returnOfficeResultFail(String error, int errorCode);

    void returnCreatOfficeSuccess(GetCreateOfficeResult getCreateOfficeResult);

    void returnCreatOfficeFail(String error, int errorCode);

    void returnAddMembersSuccess(GetAddMembersSuccessResult getAddMembersSuccessResult);

    void returnAddMembersFail(String error, int errorCode);

    void returnRecentTasksSuccess(GetTaskListResult getTaskListResult);

    void returnRecentTasksFail(String error, int errorCode);

    void returnMyCalendarSuccess(GetMyCalendarResult getMyCalendarResult);

    void returnMyCalendarFail(String error, int errorCode);

    void returnDelelteCalendarByIdSuccess();

    void returnDelelteCalendarByIdFail(String error, int errorCode);

    void returnUpdateCalendarSuccess();

    void returnUpdateCalendarFail(String error, int errorCode);

    void returnGetTagResultSuccess(GetTagResult getTagResult);

    void returnGetTagResultFail(String error, int errorCode);

    void returnAddCalEventSuccess(GetIDResult getIDResult);

    void returnAddCalEventFail(String error, int errorCode);

    void returnDeleteTagSuccess();

    void returnDeleteTagFail(String error, int errorCode);

    void returnCreateTagSuccess();

    void returnCreateTagFail(String error, int errorCode);

    void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult);

    void returnCreateTaskFail(String error, int errorCode);

    void returnDeleteTaskSuccess();

    void returnDeleteTaskFail(String error, int errorCode);

    void returnInviteMateForTaskSuccess(String subobject);

    void returnInviteMateForTaskFail(String error, int errorCode);

    void returnUpdateTaskSuccess();

    void returnUpdateTaskFail(String error, int errorCode);

    void returnCalEventsSuccess(GetCalendarEventsResult getCalendarEventsResult, boolean isRefresh);

    void returnCalEventsFail(String error, boolean isRefresh, int errorCode);

    void returnCalEventsSuccess(GetCalendarEventsResult getCalendarEventsResult);

    void returnCalEventsFail(String error, int errorCode);

    void returnAttachmentSuccess(TaskResult taskResult);

    void returnAttachmentFail(String error, int errorCode);

    void returnUpdateCalEventSuccess();

    void returnUpdateCalEventFail(String error, int errorCode);

    void returnDeleteCalEventSuccess();

    void returnDeleteCalEventFail(String error, int errorCode);

    void returnAddAttachMentSuccess(Attachment attachment);

    void returnAddAttachMentFail(String error, int errorCode);

    void returnGetTasksSuccess(GetTaskListResult getTaskListResult);

    void returnGetTasksFail(String error, int errorCode);

    void returnDelTaskMemSuccess();

    void returnDelTaskMemFail(String error, int errorCode);

    void returnDelTripSuccess();

    void returnDelTripFail(String error, int errorCode);

    void returnDndSuccess();

    void returnDndFail(String error, int errorCode);

    void returnModifyPsdSuccess();

    void returnModifyPsdFail(String error, int errorCode);

    void returnTripArriveSuccess(GetTripArriveCity getTripArriveCity);

    void retrunTripArriveFail(String error, int errorCode);

    void returnDelMeetingSuccess();

    void returnDelMeetingFail(String error, int errorCode);

    void returnDelMembersSuccess(GetChannelInfoResult getChannelInfoResult);

    void returnDelMembersFail(String error, int errorCode);

    void returnDelAttachmentSuccess();

    void returnDelAttachmentFail(String error, int errorCode);

    void returnChangeMessionOwnerSuccess();

    void returnChangeMessionOwnerFail(String error, int errorCode);

    void returnChangeMessionTagSuccess();

    void returnChangeMessionTagFail(String error, int errorCode);


    void returnCardPackageListSuccess(GetCardPackageListResult getCardPackageListResult);

    void returnCardPackageListFail(String error, int errorCode);

    void returnDeleteOfficeSuccess();

    void returnDeleteOfficeFail(String error, int errorCode);

    void returnKnowledgeListSuccess(GetKnowledgeInfo getKnowledgeInfo);

    void returnKnowledgeListFail(String error, int errorCode);

    void returnIsAdminSuccess(GetIsAdmin getIsAdmin);

    void returnIsAdminFail(String error, int errorCode);

    void returnLanguageSuccess(GetLanguageResult getLanguageResult);

    void returnLanguageFail(String error, int errorCode);

    void returnFindSearchSuccess(GetFindSearchResult getFindSearchResult);

    void returnFindSearchFail(String error, int errorCode);

    void returnFindMixSearchSuccess(GetFindMixSearchResult getFindMixSearchResult);

    void returnFindMixSearchFail(String error, int errorCode);

    void returnAllRobotsSuccess(GetAllRobotsResult getAllRobotsResult);

    void returnAllRobotsFail(String error, int errorCode);

    void returnRobotByIdSuccess(Robot robot);

    void returnRobotByIdFail(String error, int errorCode);

    void returnUpdatePwdBySMSCodeSuccess(GetUpdatePwdBySMSCodeBean getUpdatePwdBySMSCodeBean);

    void returnUpdatePwdBySMSCodeFail(String error, int errorCode);

    void returnGetAppTabsSuccess(GetAppTabsResult getAppTabsResult);

    void returnGetAppTabsFail(String error, int errorCode);

    void returnUserAppsSuccess(GetAppGroupResult getAppGroupResult);

    void returnUserAppsFail(String error, int errorCode);

    void returnUploadCollectSuccess();

    void returnUploadCollectFail(String error, int errorCode);

    void returnReactNativeUpdateSuccess(ReactNativeUpdateBean reactNativeUpdateBean);

    void returnReactNativeUpdateFail(String error, int errorCode);

    void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult);

    void returnGetClientIdResultFail(String error, int errorCode);

    void returnGetAppAuthCodeResultSuccess(AppRedirectResult appRedirectResult);

    void returnGetAppAuthCodeResultFail(String error, int errorCode);

    void returnGetDownloadReactNativeUrlSuccess(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean);

    void returnGetDownloadReactNativeUrlFail(String error, int errorCode);

    void returnGetReactNativeInstallUrlSuccess(ReactNativeInstallUriBean reactNativeInstallUriBean);

    void returnGetReactNativeInstallUrlFail(String error, int errorCode);

    void returnVeriryApprovalPasswordSuccess(String password);

    void returnVeriryApprovalPasswordFail(String error, int errorCode);

    void returnNewsInstructionSuccess(GetNewsInstructionResult getNewsInstructionResult);

    void returnNewsInstructionFail(String error, int errorCode);

    void returnAppTabAutoSuccess(GetAppTabAutoResult getAppTabAutoResult);

    void returnAppTabAutoFail(String error, int errorCode);

    void returnUserProfileSuccess(UserProfileInfoBean userProfileInfoBean);

    void returnUserProfileFail(String error, int errorCode);

    void returnBindingDeviceListSuccess(GetBindingDeviceResult getBindingDeviceResult);

    void returnBindingDeviceListFail(String error, int errorCode);

    void returnUnBindDeviceSuccess();

    void returnUnBindDeviceFail(String error, int errorCode);

    void returnMDMStateSuccess(GetMDMStateResult getMDMStateResult);

    void returnMDMStateFail(String error, int errorCode);


    void returnSplashPageInfoSuccess(SplashPageBean splashPageBean);

    void returnSplashPageInfoFail(String error, int errorCode);

    void returnLoginDesktopCloudPlusSuccess(LoginDesktopCloudPlusBean loginDesktopCloudPlusBean);

    void returnLoginDesktopCloudPlusFail(String error, int errorCode);


    void returnDeviceCheckSuccess(
            GetDeviceCheckResult getDeviceCheckResult);

    void returnDeviceCheckFail(String error, int errorCode);

    void returnDeviceLogListSuccess(GetDeviceLogResult getDeviceLogResult);

    void returnDeviceLogListFail(String error, int errorCode);

    void returnAppInfoSuccess(App app);
    void returnAppInfoFail(String error,int errorCode);

    void returnAppConfigSuccess(GetAppConfigResult getAppConfigResult);

    void returnAppConfigFail(String error, int errorCode);

    void returnSaveWebAutoRotateConfigSuccess(boolean isWebAutoRotate);

    void returnSaveWebAutoRotateConfigFail(String error, int errorCode);

    void returnGetAppBadgeResultSuccess(GetAppBadgeResult getAppBadgeResult);
    void returnGetAppBadgeResultFail(String error,int errorCode);

}