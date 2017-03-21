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
import com.inspur.emmcloud.bean.GetAppTabsResult;
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
import com.inspur.emmcloud.bean.ReactNativeUpdateBean;
import com.inspur.emmcloud.bean.Robot;
import com.inspur.emmcloud.bean.TaskResult;
import com.inspur.emmcloud.bean.Trip;


public interface APIInterface {

	 void returnOauthSigninSuccess(GetLoginResult getLoginResult);
	 void returnOauthSigninFail(String error, int errorCode);
	 void returnOauthSigninFail(String error);

	public void returnAllAppsSuccess(GetAllAppResult getAllAppResult);
	public void returnAllAppsFail(String error);

	public void returnAllAppsFreshSuccess(GetAllAppResult getAllAppResult);
	public void returnAllAppsFreshFail(String error);

	public void returnAllAppsMoreSuccess(GetAllAppResult getAllAppResult);
	public void returnAllAppsMoreFail(String error);

	public void returnAddAppSuccess(GetAddAppResult getAddAppResult);
	public void returnAddAppFail(String error);

	public void returnRemoveAppSuccess(GetRemoveAppResult getRemoveAppResult);
	public void returnRemoveAppFail(String error);

	public void returnMyAppSuccess(GetMyAppResult getMyAppResult);
	public void returnMyAppFail(String error);

	public void returnSignoutSuccess(GetSignoutResult getSignoutResult);
	public void returnSignoutFail(String error);

	public void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult);
	public void returnUpgradeFail(String error);

	public void returnSearchAppSuccess(GetSearchAppResult getAllAppResult);
	public void returnSearchAppFail(String error);

	public void returnSearchAppMoreSuccess(GetSearchAppResult getAllAppResult);
	public void returnSearchAppMoreFail(String error);

	public void returnReqLoginSMSSuccess(GetBoolenResult getBoolenResult);
	public void returnReqLoginSMSFail(String error, int errorCode);


	public void returnRegisterSMSSuccess(GetRegisterResult getRegisterResult);
	public void returnRegisterSMSFail(String error);

	public void returnReisterSMSCheckSuccess(GetRegisterCheckResult getRegisterResult);
	public void returnReisterSMSCheckFail(String error);

	public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult);
	public void returnMyInfoFail(String error);


	public void returnUsersInOrgSuccess(GetAdressUsersResult getAdressUsersResult);
	public void returnUsersInOrgFail(String error);

	public void returnUploadMyHeadSuccess(GetUploadMyHeadResult getUploadMyInfoResult);
	public void returnUploadMyHeadFail(String error);

	public void returnChannelListSuccess(GetChannelListResult getSessionListResult);
	public void returnChannelListFail(String error);

	public void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult);
	public void returnNewMsgsFail(String error);



	public void returnMsgCommentSuccess(GetMsgCommentResult getMsgCommentResult,String mid);
	public void returnMsgCommentFail(String error);

	public void returnMsgCommentCountSuccess(GetMsgCommentCountResult getMsgCommentCountResult,String mid);
	public void returnMsgCommentCountFail(String error);

	public void returnNewsSuccess(GetNewsResult getNewsResult);
	public void returnNewsFail(String error);

	public void returnModifyUserInfoSucces(GetBoolenResult getBoolenResult);
	public void returnModifyUserInfoFail(String error);

	public void returnWebSocketUrlSuccess(GetWebSocketUrlResult getWebSocketResult);
	public void returnWebSocketUrlFail(String error);

	public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult, String fakeMessageId);
	public void returnSendMsgFail(String error, String fakeMessageId);

	public void returnUploadMsgImgSuccess(GetNewsImgResult getNewsImgResult, String fakeMessageId);
	public void returnUploadMsgImgFail(String error);

	public void returnGroupNewsTitleSuccess(GetNewsTitleResult getNewsTitleResult);
	public void returnGroupNewsTitleFail(String error);

	public void returnGroupNewsDetailSuccess(GetGroupNewsDetailResult getGroupNewsDetailResult);
	public void returnGroupNewsDetailFail(String error);

	public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult);
	public void returnMeetingsFail(String error);

	public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult, boolean isLoadMore);

	public void returnMeetingRoomsSuccess(GetMeetingRoomsResult getMeetingRoomsResult);
	public void returnMeetingRoomsFail(String error);

	public void returnMeetingRoomsSuccess(GetMeetingRoomsResult getMeetingRoomsResult, boolean isFilte);

	public void returnMsgSuccess(GetMsgResult getMsgResult);
	public void returnMsgFail(String error);

	public void returnBookingRoomSuccess(GetBookingRoomResult getBookingRoomResult);
	public void returnBookingRoomFail(String error, int errorCode);

	public void returnChannelInfoSuccess(GetChannelInfoResult getChannelInfoResult);
	public void returnChannelInfoFail(String error);

	public void returnRoomAvailableTimeSuccess(GetRoomAvailableResult getRoomAvailableResult);
	public void returnRoomAvailableTimeFail(String error);

	public void returnAllContactSuccess(GetAllContactResult getAllContactResult);
	public void returnAllContactFail(String error);

	public void returnFileUpLoadSuccess(GetFileUploadResult getFileUploadResult, String fakeMessageId);
	public void returnFileUpLoadFail(String error);

	public void returnSearchChannelGroupSuccess(GetSearchChannelGroupResult getSearchChannelGroupResult);
	public void returnSearchChannelGroupFail(String error);

	public void returnUserHeadUploadSuccess(GetUserHeadUploadResult getUserHeadUploadResult);
	public void returnUserHeadUploadFail(String error);

	public void returnCreateSingleChannelSuccess(GetCreateSingleChannelResult getCreatSingleChannelResult);
	public void returnCreatSingleChannelFail(String error);

	public void returnCreatChannelGroupSuccess(ChannelGroup channelGroup);
	public void returnCreateChannelGroupFail(String error);

	public void returnGetMeetingReplySuccess(GetMeetingReplyResult getMeetingReplyResult);
	public void returnGetMeetingReplyFail(String error);

	public void returnTripSuccess(Trip trip);
	public void returnTripFail(String error);

	public void returnLastUploadTripSuccess(Trip trip);
	public void returnLastUploadTripFail(String error);

	public void returnUpdateChannelGroupNameSuccess(GetBoolenResult getBoolenResult);
	public void returnUpdateChannelGroupNameFail(String error);

	public void returnMeetingListSuccess(GetMeetingListResult getMeetingListResult, String date);
	public void returnMeetingListSuccess(GetMeetingListResult getMeetingListResult);
	public void returnMeetingListFail(String error);

	public void returnUploadTrainTicketSuccess();
	public void returnUploadTrainTicketFail(String error);

	public void returnUploadExceptionSuccess(GetExceptionResult getExceptionResult);
	public void returnUploadExceptionFail(String error);

	public void returnLoctionResultSuccess(GetLoctionResult getLoctionResult);
	public void returnLoctionResultFail(String error);

	public void returnOfficeResultSuccess(GetOfficeResult getOfficeResult);
	public void returnOfficeResultFail(String error);

	public void returnCreatOfficeSuccess(GetCreateOfficeResult getCreateOfficeResult);
	public void returnCreatOfficeFail(String error);

	public void returnAddMembersSuccess(GetAddMembersSuccessResult getAddMembersSuccessResult);
	public void returnAddMembersFail(String error);

	public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult);
	public void returnRecentTasksFail(String error);

	public void returnMyCalendarSuccess(GetMyCalendarResult getMyCalendarResult);
	public void returnMyCalendarFail(String error);

	public void returnDelelteCalendarByIdSuccess();
	public void returnDelelteCalendarByIdFail(String error);

	public void returnUpdateCalendarSuccess();
	public void returnUpdateCalendarFail(String error);

	public void returnGetTagResultSuccess(GetTagResult getTagResult);
	public void returnGetTagResultFail(String error);

	public void returnAddCalEventSuccess(GetIDResult getIDResult);
	public void returnAddCalEventFail(String error);

	public void returnDeleteTagSuccess();
	public void returnDeleteTagFail(String error);

	public void returnCreateTagSuccess();
	public void returnCreateTagFail(String error);

	public void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult);
	public void returnCreateTaskFail(String error);

	public void returnDeleteTaskSuccess();
	public void returnDeleteTaskFail(String error);

	public void returnInviteMateForTaskSuccess(String subobject);
	public void returnInviteMateForTaskFail(String error);

	public void returnUpdateTaskSuccess();
	public void returnUpdateTaskFail(String error);

	public void returnCalEventsSuccess(GetCalendarEventsResult getCalendarEventsResult, boolean isRefresh);
	public void returnCalEventsFail(String error, boolean isRefresh);
	public void returnCalEventsSuccess(GetCalendarEventsResult getCalendarEventsResult);
	public void returnCalEventsFail(String error);

	public void returnAttachmentSuccess(TaskResult taskResult);
	public void returnAttachmentFail(String error);

	public void returnUpdateCalEventSuccess();
	public void returnUpdateCalEventFail(String error);

	public void returnDeleteCalEventSuccess();
	public void returnDeleteCalEventFail(String error);

	public void returnAddAttachMentSuccess(Attachment attachment);
	public void returnAddAttachMentFail(String error);

	public void returnGetTasksSuccess(GetTaskListResult getTaskListResult);
	public void returnGetTasksFail(String error);

	public void returnDelTaskMemSuccess();
	public void returnDelTaskMemFail(String error);

	public void returnDelTripSuccess();
	public void returnDelTripFail(String error);

	public void returnDndSuccess();
	public void returnDndFail(String error);

	public void returnModifyPsdSuccess();
	public void returnModifyPsdFail(String error);

	public void returnTripArriveSuccess(GetTripArriveCity getTripArriveCity);
	public void retrunTripArriveFail(String error);

	public void returnDelMeetingSuccess();
	public void returnDelMeetingFail(String error);

	public void returnDelMembersSuccess(GetChannelInfoResult getChannelInfoResult);
	public void returnDelMembersFail(String errors);

	public void returnDelAttachmentSuccess();
	public void returnDelAttachmentFail(String error);

	public void returnChangeMessionOwnerSuccess();
	public void returnChangeMessionOwnerFail(String error);

	public void returnChangeMessionTagSuccess();
	public void returnChangeMessionTagFail(String error);


	public void returnCardPackageListSuccess(GetCardPackageListResult getCardPackageListResult);
	public void returnCardPackageListFail(String error);

	public void returnDeleteOfficeSuccess();
	public void returnDeleteOfficeFail(String error);

	public void returnKnowledgeListSuccess(GetKnowledgeInfo getKnowledgeInfo);
	public void returnKnowledgeListFail(String error);

	public void returnIsAdminSuccess(GetIsAdmin getIsAdmin);
	public void returnIsAdminFail(String error);

	public void returnLanguageSuccess(GetLanguageResult getLanguageResult);
	public void returnLanguageFail(String error);

	public void returnFindSearchSuccess(GetFindSearchResult getFindSearchResult);
	public void returnFindSearchFail(String error);

	public void returnFindMixSearchSuccess(GetFindMixSearchResult getFindMixSearchResult);
	public void returnFindMixSearchFail(String error);

	public void returnAllRobotsSuccess(GetAllRobotsResult getAllRobotsResult);
	public void returnAllRobotsFail(String error);

	public void returnRobotByIdSuccess(Robot robot);
	public void returnRobotByIdFail(String error);

	public void returnUpdatePwdBySMSCodeSuccess(GetUpdatePwdBySMSCodeBean getUpdatePwdBySMSCodeBean);
	public void returnUpdatePwdBySMSCodeFail(String error);

	public void returnGetAppTabsSuccess(GetAppTabsResult getAppTabsResult);
	public void returnGetAppTabsFail(String error);

	public void returnUserAppsSuccess(GetAppGroupResult getAppGroupResult);
	public void returnUserAppsFail(String error);

	public void returnUploadCollectSuccess();
	public void returnUploadCollectFail();

	public void returnReactNativeUpdateSuccess(ReactNativeUpdateBean reactNativeUpdateBean);
	public void returnReactNativeUpdateFail(ReactNativeClientIdErrorBean reactNativeClientIdErrorBean);

	public void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult);
	public void returnGetClientIdResultFail(String error);

	public void returnGetAppAuthCodeResultSuccess(AppRedirectResult appRedirectResult);
	public void returnGetAppAuthCodeResultFail(String error);

}