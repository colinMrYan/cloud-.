package com.inspur.emmcloud.api;


import com.inspur.emmcloud.bean.AppRedirectResult;
import com.inspur.emmcloud.bean.Attachment;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.GetAddAppResult;
import com.inspur.emmcloud.bean.GetAddMembersSuccessResult;
import com.inspur.emmcloud.bean.GetAdressUsersResult;
import com.inspur.emmcloud.bean.GetAllAppResult;
import com.inspur.emmcloud.bean.GetAllContactResult;
import com.inspur.emmcloud.bean.GetAllRobotsResult;
import com.inspur.emmcloud.bean.GetAppGroupResult;
import com.inspur.emmcloud.bean.GetAppTabAutoResult;
import com.inspur.emmcloud.bean.GetAppTabsResult;
import com.inspur.emmcloud.bean.GetBindingDeviceResult;
import com.inspur.emmcloud.bean.GetBookingRoomResult;
import com.inspur.emmcloud.bean.GetBoolenResult;
import com.inspur.emmcloud.bean.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.GetCardPackageListResult;
import com.inspur.emmcloud.bean.GetChannelInfoResult;
import com.inspur.emmcloud.bean.GetChannelListResult;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.bean.GetCreateOfficeResult;
import com.inspur.emmcloud.bean.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.GetExceptionResult;
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
import com.inspur.emmcloud.bean.GetRoomAvailableResult;
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
import com.inspur.emmcloud.bean.ReactNativeClientIdErrorBean;
import com.inspur.emmcloud.bean.ReactNativeDownloadUrlBean;
import com.inspur.emmcloud.bean.ReactNativeInstallUriBean;
import com.inspur.emmcloud.bean.ReactNativeUpdateBean;
import com.inspur.emmcloud.bean.Robot;
import com.inspur.emmcloud.bean.TaskResult;
import com.inspur.emmcloud.bean.Trip;
import com.inspur.emmcloud.bean.UserProfileInfoBean;


public interface APIInterface {

	void returnOauthSigninSuccess(GetLoginResult getLoginResult);

	void returnOauthSigninFail(String error, int errorCode);

	void returnOauthSigninFail(String error);

	void returnAllAppsSuccess(GetAllAppResult getAllAppResult);

	void returnAllAppsFail(String error);

	void returnAllAppsFreshSuccess(GetAllAppResult getAllAppResult);

	void returnAllAppsFreshFail(String error);

	void returnAllAppsMoreSuccess(GetAllAppResult getAllAppResult);

	void returnAllAppsMoreFail(String error);

	void returnAddAppSuccess(GetAddAppResult getAddAppResult);

	void returnAddAppFail(String error);

	void returnRemoveAppSuccess(GetRemoveAppResult getRemoveAppResult);

	void returnRemoveAppFail(String error);

	void returnMyAppSuccess(GetMyAppResult getMyAppResult);

	void returnMyAppFail(String error);

	void returnSignoutSuccess(GetSignoutResult getSignoutResult);

	void returnSignoutFail(String error);

	void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult, boolean isManualCheck);

	void returnUpgradeFail(String error, boolean isManualCheck);

	void returnSearchAppSuccess(GetSearchAppResult getAllAppResult);

	void returnSearchAppFail(String error);

	void returnSearchAppMoreSuccess(GetSearchAppResult getAllAppResult);

	void returnSearchAppMoreFail(String error);

	void returnReqLoginSMSSuccess(GetBoolenResult getBoolenResult);

	void returnReqLoginSMSFail(String error, int errorCode);


	void returnRegisterSMSSuccess(GetRegisterResult getRegisterResult);

	void returnRegisterSMSFail(String error);

	void returnReisterSMSCheckSuccess(GetRegisterCheckResult getRegisterResult);

	void returnReisterSMSCheckFail(String error);

	void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult);

	void returnMyInfoFail(String error);


	void returnUsersInOrgSuccess(GetAdressUsersResult getAdressUsersResult);

	void returnUsersInOrgFail(String error);

	void returnUploadMyHeadSuccess(GetUploadMyHeadResult getUploadMyInfoResult);

	void returnUploadMyHeadFail(String error);

	void returnChannelListSuccess(GetChannelListResult getSessionListResult);

	void returnChannelListFail(String error);

	void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult);

	void returnNewMsgsFail(String error);


	void returnMsgCommentSuccess(GetMsgCommentResult getMsgCommentResult, String mid);

	void returnMsgCommentFail(String error);

	void returnMsgCommentCountSuccess(GetMsgCommentCountResult getMsgCommentCountResult, String mid);

	void returnMsgCommentCountFail(String error);

	void returnNewsSuccess(GetNewsResult getNewsResult);

	void returnNewsFail(String error);

	void returnModifyUserInfoSucces(GetBoolenResult getBoolenResult);

	void returnModifyUserInfoFail(String error);

	void returnWebSocketUrlSuccess(GetWebSocketUrlResult getWebSocketResult);

	void returnWebSocketUrlFail(String error);

	void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult, String fakeMessageId);

	void returnSendMsgFail(String error, String fakeMessageId);

	void returnUploadMsgImgSuccess(GetNewsImgResult getNewsImgResult, String fakeMessageId);

	void returnUploadMsgImgFail(String error);

	void returnGroupNewsTitleSuccess(GetNewsTitleResult getNewsTitleResult);

	void returnGroupNewsTitleFail(String error);

	void returnGroupNewsDetailSuccess(GetGroupNewsDetailResult getGroupNewsDetailResult);

	void returnGroupNewsDetailFail(String error);

	void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult);

	void returnMeetingsFail(String error);

	void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult, boolean isLoadMore);

	void returnMeetingRoomsSuccess(GetMeetingRoomsResult getMeetingRoomsResult);

	void returnMeetingRoomsFail(String error);

	void returnMeetingRoomsSuccess(GetMeetingRoomsResult getMeetingRoomsResult, boolean isFilte);

	void returnMsgSuccess(GetMsgResult getMsgResult);

	void returnMsgFail(String error);

	void returnBookingRoomSuccess(GetBookingRoomResult getBookingRoomResult);

	void returnBookingRoomFail(String error, int errorCode);

	void returnChannelInfoSuccess(GetChannelInfoResult getChannelInfoResult);

	void returnChannelInfoFail(String error);

	void returnRoomAvailableTimeSuccess(GetRoomAvailableResult getRoomAvailableResult);

	void returnRoomAvailableTimeFail(String error);

	void returnAllContactSuccess(GetAllContactResult getAllContactResult);

	void returnAllContactFail(String error);

	void returnFileUpLoadSuccess(GetFileUploadResult getFileUploadResult, String fakeMessageId);

	void returnFileUpLoadFail(String error);

	void returnSearchChannelGroupSuccess(GetSearchChannelGroupResult getSearchChannelGroupResult);

	void returnSearchChannelGroupFail(String error);

	void returnUserHeadUploadSuccess(GetUserHeadUploadResult getUserHeadUploadResult);

	void returnUserHeadUploadFail(String error);

	void returnCreateSingleChannelSuccess(GetCreateSingleChannelResult getCreatSingleChannelResult);

	void returnCreatSingleChannelFail(String error);

	void returnCreatChannelGroupSuccess(ChannelGroup channelGroup);

	void returnCreateChannelGroupFail(String error);

	void returnGetMeetingReplySuccess(GetMeetingReplyResult getMeetingReplyResult);

	void returnGetMeetingReplyFail(String error);

	void returnTripSuccess(Trip trip);

	void returnTripFail(String error);

	void returnLastUploadTripSuccess(Trip trip);

	void returnLastUploadTripFail(String error);

	void returnUpdateChannelGroupNameSuccess(GetBoolenResult getBoolenResult);

	void returnUpdateChannelGroupNameFail(String error);

	void returnMeetingListSuccess(GetMeetingListResult getMeetingListResult, String date);

	void returnMeetingListSuccess(GetMeetingListResult getMeetingListResult);

	void returnMeetingListFail(String error);

	void returnUploadTrainTicketSuccess();

	void returnUploadTrainTicketFail(String error);

	void returnUploadExceptionSuccess(GetExceptionResult getExceptionResult);

	void returnUploadExceptionFail(String error);

	void returnLoctionResultSuccess(GetLoctionResult getLoctionResult);

	void returnLoctionResultFail(String error);

	void returnOfficeResultSuccess(GetOfficeResult getOfficeResult);

	void returnOfficeResultFail(String error);

	void returnCreatOfficeSuccess(GetCreateOfficeResult getCreateOfficeResult);

	void returnCreatOfficeFail(String error);

	void returnAddMembersSuccess(GetAddMembersSuccessResult getAddMembersSuccessResult);

	void returnAddMembersFail(String error);

	void returnRecentTasksSuccess(GetTaskListResult getTaskListResult);

	void returnRecentTasksFail(String error);

	void returnMyCalendarSuccess(GetMyCalendarResult getMyCalendarResult);

	void returnMyCalendarFail(String error);

	void returnDelelteCalendarByIdSuccess();

	void returnDelelteCalendarByIdFail(String error);

	void returnUpdateCalendarSuccess();

	void returnUpdateCalendarFail(String error);

	void returnGetTagResultSuccess(GetTagResult getTagResult);

	void returnGetTagResultFail(String error);

	void returnAddCalEventSuccess(GetIDResult getIDResult);

	void returnAddCalEventFail(String error);

	void returnDeleteTagSuccess();

	void returnDeleteTagFail(String error);

	void returnCreateTagSuccess();

	void returnCreateTagFail(String error);

	void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult);

	void returnCreateTaskFail(String error);

	void returnDeleteTaskSuccess();

	void returnDeleteTaskFail(String error);

	void returnInviteMateForTaskSuccess(String subobject);

	void returnInviteMateForTaskFail(String error);

	void returnUpdateTaskSuccess();

	void returnUpdateTaskFail(String error);

	void returnCalEventsSuccess(GetCalendarEventsResult getCalendarEventsResult, boolean isRefresh);

	void returnCalEventsFail(String error, boolean isRefresh);

	void returnCalEventsSuccess(GetCalendarEventsResult getCalendarEventsResult);

	void returnCalEventsFail(String error);

	void returnAttachmentSuccess(TaskResult taskResult);

	void returnAttachmentFail(String error);

	void returnUpdateCalEventSuccess();

	void returnUpdateCalEventFail(String error);

	void returnDeleteCalEventSuccess();

	void returnDeleteCalEventFail(String error);

	void returnAddAttachMentSuccess(Attachment attachment);

	void returnAddAttachMentFail(String error);

	void returnGetTasksSuccess(GetTaskListResult getTaskListResult);

	void returnGetTasksFail(String error);

	void returnDelTaskMemSuccess();

	void returnDelTaskMemFail(String error);

	void returnDelTripSuccess();

	void returnDelTripFail(String error);

	void returnDndSuccess();

	void returnDndFail(String error);

	void returnModifyPsdSuccess();

	void returnModifyPsdFail(String error);

	void returnTripArriveSuccess(GetTripArriveCity getTripArriveCity);

	void retrunTripArriveFail(String error);

	void returnDelMeetingSuccess();

	void returnDelMeetingFail(String error);

	void returnDelMembersSuccess(GetChannelInfoResult getChannelInfoResult);

	void returnDelMembersFail(String errors);

	void returnDelAttachmentSuccess();

	void returnDelAttachmentFail(String error);

	void returnChangeMessionOwnerSuccess();

	void returnChangeMessionOwnerFail(String error);

	void returnChangeMessionTagSuccess();

	void returnChangeMessionTagFail(String error);


	void returnCardPackageListSuccess(GetCardPackageListResult getCardPackageListResult);

	void returnCardPackageListFail(String error);

	void returnDeleteOfficeSuccess();

	void returnDeleteOfficeFail(String error);

	void returnKnowledgeListSuccess(GetKnowledgeInfo getKnowledgeInfo);

	void returnKnowledgeListFail(String error);

	void returnIsAdminSuccess(GetIsAdmin getIsAdmin);

	void returnIsAdminFail(String error);

	void returnLanguageSuccess(GetLanguageResult getLanguageResult);

	void returnLanguageFail(String error);

	void returnFindSearchSuccess(GetFindSearchResult getFindSearchResult);

	void returnFindSearchFail(String error);

	void returnFindMixSearchSuccess(GetFindMixSearchResult getFindMixSearchResult);

	void returnFindMixSearchFail(String error);

	void returnAllRobotsSuccess(GetAllRobotsResult getAllRobotsResult);

	void returnAllRobotsFail(String error);

	void returnRobotByIdSuccess(Robot robot);

	void returnRobotByIdFail(String error);

	void returnUpdatePwdBySMSCodeSuccess(GetUpdatePwdBySMSCodeBean getUpdatePwdBySMSCodeBean);

	void returnUpdatePwdBySMSCodeFail(String error);

	void returnGetAppTabsSuccess(GetAppTabsResult getAppTabsResult);

	void returnGetAppTabsFail(String error);

	void returnUserAppsSuccess(GetAppGroupResult getAppGroupResult);

	void returnUserAppsFail(String error);

	void returnUploadCollectSuccess();

	void returnUploadCollectFail();

	void returnReactNativeUpdateSuccess(ReactNativeUpdateBean reactNativeUpdateBean);

	void returnReactNativeUpdateFail(ReactNativeClientIdErrorBean reactNativeClientIdErrorBean);

	void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult);

	void returnGetClientIdResultFail(String error);

	void returnGetAppAuthCodeResultSuccess(AppRedirectResult appRedirectResult);

	void returnGetAppAuthCodeResultFail(String error);

	void returnGetDownloadReactNativeUrlSuccess(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean);

	void returnGetDownloadReactNativeUrlFail(String error);

	void returnGetReactNativeInstallUrlSuccess(ReactNativeInstallUriBean reactNativeInstallUriBean);

	void returnGetReactNativeInstallUrlFail(String error);

	void returnVeriryApprovalPasswordSuccess(String password);

	void returnVeriryApprovalPasswordFail(String error);

	void returnNewsInstructionSuccess(GetNewsInstructionResult getNewsInstructionResult);

	void returnNewsInstructionFail(String error);

	void returnAppTabAutoSuccess(GetAppTabAutoResult getAppTabAutoResult);

	void returnAppTabAutoFail(String error);

	void returnUserProfileSuccess(UserProfileInfoBean userProfileInfoBean);

	void returnUserProfileFail(String error);

	void returnBindingDeviceListSuccess(GetBindingDeviceResult getBindingDeviceResult);

	void returnBindingDeviceListFail(String error, int errorCode);

	void returnUnBindDeviceSuccess();

	void returnUnBindDeviceFail(String error, int errorCode);

	void returnMDMStateSuccess(GetMDMStateResult getMDMStateResult);
	void returnMDMStateFail(String error, int errorCode);


}