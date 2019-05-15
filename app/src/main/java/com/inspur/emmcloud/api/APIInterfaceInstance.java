package com.inspur.emmcloud.api;


import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.AppRedirectResult;
import com.inspur.emmcloud.bean.appcenter.GetAddAppResult;
import com.inspur.emmcloud.bean.appcenter.GetAllAppResult;
import com.inspur.emmcloud.bean.appcenter.GetAppGroupResult;
import com.inspur.emmcloud.bean.appcenter.GetClientIdRsult;
import com.inspur.emmcloud.bean.appcenter.GetIDResult;
import com.inspur.emmcloud.bean.appcenter.GetMyAppResult;
import com.inspur.emmcloud.bean.appcenter.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.bean.appcenter.GetRegisterCheckResult;
import com.inspur.emmcloud.bean.appcenter.GetRegisterResult;
import com.inspur.emmcloud.bean.appcenter.GetRemoveAppResult;
import com.inspur.emmcloud.bean.appcenter.GetSearchAppResult;
import com.inspur.emmcloud.bean.appcenter.GetWebAppRealUrlResult;
import com.inspur.emmcloud.bean.appcenter.ReactNativeDownloadUrlBean;
import com.inspur.emmcloud.bean.appcenter.ReactNativeInstallUriBean;
import com.inspur.emmcloud.bean.appcenter.ReactNativeUpdateBean;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailDetailResult;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailFolderResult;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailListResult;
import com.inspur.emmcloud.bean.appcenter.news.GetGroupNewsDetailResult;
import com.inspur.emmcloud.bean.appcenter.news.GetNewsTitleResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileListResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeGroupPermissionResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeGroupResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeListResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeResultWithPermissionResult;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeDetail;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.appcenter.webex.GetScheduleWebexMeetingSuccess;
import com.inspur.emmcloud.bean.appcenter.webex.GetWebexMeetingListResult;
import com.inspur.emmcloud.bean.appcenter.webex.GetWebexTKResult;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetAllRobotsResult;
import com.inspur.emmcloud.bean.chat.GetChannelListResult;
import com.inspur.emmcloud.bean.chat.GetChannelMessagesResult;
import com.inspur.emmcloud.bean.chat.GetConversationListResult;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.GetFileUploadResult;
import com.inspur.emmcloud.bean.chat.GetMsgCommentCountResult;
import com.inspur.emmcloud.bean.chat.GetMsgCommentResult;
import com.inspur.emmcloud.bean.chat.GetMsgResult;
import com.inspur.emmcloud.bean.chat.GetNewMsgsResult;
import com.inspur.emmcloud.bean.chat.GetNewsImgResult;
import com.inspur.emmcloud.bean.chat.GetNewsInstructionResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.GetUploadPushInfoResult;
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.GetWebSocketUrlResult;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.contact.GetContactOrgListUpateResult;
import com.inspur.emmcloud.bean.contact.GetContactUserListUpateResult;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.find.GetKnowledgeInfo;
import com.inspur.emmcloud.bean.find.GetTripArriveCity;
import com.inspur.emmcloud.bean.find.Trip;
import com.inspur.emmcloud.bean.login.GetDeviceCheckResult;
import com.inspur.emmcloud.bean.login.GetLoginResult;
import com.inspur.emmcloud.bean.login.GetMDMStateResult;
import com.inspur.emmcloud.bean.login.GetSignoutResult;
import com.inspur.emmcloud.bean.login.LoginDesktopCloudPlusBean;
import com.inspur.emmcloud.bean.login.UploadMDMInfoResult;
import com.inspur.emmcloud.bean.mine.GetBindingDeviceResult;
import com.inspur.emmcloud.bean.mine.GetCardPackageResult;
import com.inspur.emmcloud.bean.mine.GetDeviceLogResult;
import com.inspur.emmcloud.bean.mine.GetExperienceUpgradeFlagResult;
import com.inspur.emmcloud.bean.mine.GetFaceSettingResult;
import com.inspur.emmcloud.bean.mine.GetLanguageResult;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.bean.mine.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.mine.GetUserCardMenusResult;
import com.inspur.emmcloud.bean.mine.GetUserHeadUploadResult;
import com.inspur.emmcloud.bean.mine.UserProfileInfoBean;
import com.inspur.emmcloud.bean.schedule.GetScheduleListResult;
import com.inspur.emmcloud.bean.schedule.calendar.GetHolidayDataResult;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.meeting.Building;
import com.inspur.emmcloud.bean.schedule.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetMeetingListResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetOfficeListResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.Office;
import com.inspur.emmcloud.bean.system.AppException;
import com.inspur.emmcloud.bean.system.GetAllConfigVersionResult;
import com.inspur.emmcloud.bean.system.GetAppConfigResult;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.bean.system.GetUpgradeResult;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.bean.system.badge.BadgeBodyModel;
import com.inspur.emmcloud.bean.system.navibar.NaviBarModel;
import com.inspur.emmcloud.bean.schedule.task.Attachment;
import com.inspur.emmcloud.bean.work.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.work.GetLocationResult;
import com.inspur.emmcloud.bean.work.GetMeetingReplyResult;
import com.inspur.emmcloud.bean.work.GetMeetingRoomListResult;
import com.inspur.emmcloud.bean.work.GetMeetingsResult;
import com.inspur.emmcloud.bean.work.GetMyCalendarResult;
import com.inspur.emmcloud.bean.work.GetTagResult;
import com.inspur.emmcloud.bean.schedule.task.GetTaskAddResult;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.schedule.task.Task;

import java.util.Calendar;
import java.util.List;

public class APIInterfaceInstance implements APIInterface {
    @Override
    public void returnOauthSignInSuccess(GetLoginResult getLoginResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnOauthSignInFail(String error, int errorCode, String headerLimitRemaining, String headerRetryAfter) {

    }

    @Override
    public void returnRefreshTokenSuccess(GetLoginResult getLoginResult) {

    }

    @Override
    public void returnRefreshTokenFail(String error, int errorCode) {

    }

    public APIInterfaceInstance() {
        super();
    }

    @Override
    public void returnDeviceCheckSuccess(GetDeviceCheckResult getDeviceCheckResult) {
    }

    @Override
    public void returnUploadExceptionSuccess(List<AppException> appExceptionList) {

    }

    @Override
    public void returnUploadCollectSuccess(List<PVCollectModel> collectModelList) {

    }

    @Override
    public void returnDeviceCheckFail(String error, int errorCode) {
    }

    @Override
    public void returnUnBindDeviceSuccess() {
    }

    @Override
    public void returnUnBindDeviceFail(String error, int errorCode) {
    }

    @Override
    public void returnBindingDeviceListSuccess(GetBindingDeviceResult getBindingDeviceResult) {
    }

    @Override
    public void returnBindingDeviceListFail(String error, int errorCode) {
    }

    @Override
    public void returnAddAppSuccess(GetAddAppResult getAddAppResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAddAppFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnRemoveAppSuccess(GetRemoveAppResult getRemoveAppResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnRemoveAppFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnMyAppSuccess(GetMyAppResult getMyAppResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnMyAppFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSignoutSuccess(GetSignoutResult getSignoutResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSignoutFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnMsgCommentCountSuccess(GetMsgCommentCountResult getMsgCommentCountResult, String mid) {
    }

    @Override
    public void returnMsgCommentCountFail(String error, int errorCode) {
    }

    @Override
    public void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult, boolean isManualCheck) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnUpgradeFail(String error, boolean isManualCheck, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSearchAppSuccess(GetSearchAppResult getAllAppResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnSearchAppFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnSearchAppMoreSuccess(GetSearchAppResult getAllAppResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnSearchAppMoreFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCheckCloudPluseConnectionSuccess(byte[] arg0, String url) {

    }

    @Override
    public void returnCheckCloudPluseConnectionError(String error, int responseCode, String url) {

    }

    @Override
    public void returnLoginSMSCaptchaSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnLoginSMSCaptchaFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnRegisterSMSSuccess(GetRegisterResult getRegisterResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnRegisterSMSFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnReisterSMSCheckSuccess(
            GetRegisterCheckResult getRegisterResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnReisterSMSCheckFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMyInfoFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUploadMyHeadSuccess(
            GetUploadMyHeadResult getUploadMyInfoResult, String filePath) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnUploadMyHeadFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnChannelListSuccess(
            GetChannelListResult getSessionListResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnChannelListFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnNewMessagesSuccess(GetChannelMessagesResult getChannelMessagesResult) {

    }

    @Override
    public void returnNewMessagesFail(String error, int errorCode) {

    }

    @Override
    public void returnNewMsgsFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMsgCommentSuccess(GetMsgCommentResult getMsgCommentResult, String mid) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMsgCommentFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnModifyUserInfoSucces(GetBoolenResult getBoolenResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnModifyUserInfoFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnWebSocketUrlSuccess(
            GetWebSocketUrlResult getWebSocketResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnWebSocketUrlFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult, String fakeMessageId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnSendMsgFail(String error, String fakeMessageId, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUploadResImgSuccess(GetNewsImgResult getNewsImgResult, String fakeMessageId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUploadResImgFail(String error, int errorCode, String fakeMessageId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGroupNewsTitleSuccess(
            GetNewsTitleResult getNewsTitleResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGroupNewsTitleFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGroupNewsDetailSuccess(
            GetGroupNewsDetailResult getGroupNewsDetailResult, int page) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGroupNewsDetailFail(String error, int errorCode, int page) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMeetingsFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMeetingRoomListSuccess(
            GetMeetingRoomListResult getMeetingRoomsResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMeetingRoomListFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMsgSuccess(GetMsgResult getMsgResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMsgFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnBookingRoomSuccess(
    ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnBookingRoomFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnChannelInfoSuccess(
            ChannelGroup channelGroup) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnChannelInfoFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpLoadResFileSuccess(GetFileUploadResult getFileUploadResult, String fakeMessageId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpLoadResFileFail(String error, int errorCode, String fakeMessageId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnSearchChannelGroupSuccess(
            GetSearchChannelGroupResult getSearchChannelGroupResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnSearchChannelGroupFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUserHeadUploadSuccess(
            GetUserHeadUploadResult getUserHeadUploadResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUserHeadUploadFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreateSingleChannelSuccess(
            GetCreateSingleChannelResult getCreatSingleChannelResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreatSingleChannelFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreatChannelGroupSuccess(ChannelGroup channelGroup) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreateChannelGroupFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGetMeetingReplySuccess(
            GetMeetingReplyResult getMeetingReplyResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGetMeetingReplyFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnTripSuccess(Trip trip) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnTripFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnLastUploadTripSuccess(Trip trip) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnLastUploadTripFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpdateChannelGroupNameSuccess(
            GetBoolenResult getBoolenResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpdateChannelGroupNameFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMeetingListSuccess(
            com.inspur.emmcloud.bean.work.GetMeetingListResult getMeetingListResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMeetingListFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUploadTrainTicketSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUploadTrainTicketFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUploadExceptionSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUploadExceptionFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnLocationResultSuccess(GetLocationResult getLoctionResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnLocationResultFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnOfficeListResultSuccess(GetOfficeListResult getOfficeListResult) {

    }

    @Override
    public void returnOfficeListResultFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAddMeetingOfficeSuccess(Office office, Building building) {

    }

    @Override
    public void returnAddMeetingOfficeFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAddMembersSuccess(ChannelGroup channelGroup) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAddMembersFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnRecentTasksFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMyCalendarSuccess(GetMyCalendarResult getMyCalendarResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMyCalendarFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelelteCalendarByIdSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelelteCalendarByIdFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpdateCalendarSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpdateCalendarFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGetTagResultSuccess(GetTagResult getTagResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGetTagResultFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAddCalEventSuccess(GetIDResult getIDResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAddCalEventFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDeleteTagSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDeleteTagFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreateTagSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreateTagFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreateTaskFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDeleteTaskSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDeleteTaskFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnInviteMateForTaskSuccess(String subject) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnInviteMateForTaskFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpdateTaskSuccess(int position) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpdateTaskFail(String error, int errorCode, int position) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAttachmentSuccess(Task taskResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAttachmentFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpdateCalEventSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpdateCalEventFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDeleteCalEventSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDeleteCalEventFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAddAttachMentSuccess(Attachment attachment) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAddAttachMentFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGetTasksSuccess(GetTaskListResult getTaskListResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGetTasksFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelTaskMemSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelTaskMemFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelTripSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelTripFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCalEventsSuccess(
            GetCalendarEventsResult getCalendarEventsResult, boolean isRefresh) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCalEventsFail(String error, boolean isRefresh, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCalEventsSuccess(
            GetCalendarEventsResult getCalendarEventsResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCalEventsFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDndSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDndFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnModifyPasswordSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnModifyPasswordFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnTripArriveSuccess(GetTripArriveCity getTripArriveCity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnTripArriveFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDeleteMeetingSuccess(Meeting meeting) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnDeleteMeetingFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnDelMembersSuccess(
            ChannelGroup channelGroup) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelMembersFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelAttachmentSuccess(int position) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelAttachmentFail(String error, int errorCode, int position) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnChangeMessionOwnerSuccess(String managerName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnChangeMessionOwnerFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnChangeMessionTagSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnChangeMessionTagFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDeleteOfficeSuccess(Office office) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDeleteOfficeFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnKnowledgeListSuccess(GetKnowledgeInfo getKnowledgeInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnKnowledgeListFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnIsMeetingAdminSuccess(GetIsMeetingAdminResult getIsAdmin) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnIsMeetingAdminFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult, int page) {
    }

    @Override
    public void returnLanguageSuccess(GetLanguageResult getLanguageResult) {
    }

    @Override
    public void returnLanguageFail(String error, int errorCode) {
    }

    @Override
    public void returnMeetingRoomListSuccess(
            GetMeetingRoomListResult getMeetingRoomsResult, boolean isFilte) {
    }

    @Override
    public void returnFindSearchFail(String error, int errorCode) {
    }

    @Override
    public void returnFindMixSearchFail(String error, int errorCode) {
    }

    @Override
    public void returnMeetingListSuccess(
            com.inspur.emmcloud.bean.work.GetMeetingListResult getMeetingListResult, String date) {
    }

    @Override
    public void returnAllRobotsSuccess(GetAllRobotsResult getAllRobotsResult) {
    }

    @Override
    public void returnAllRobotsFail(String error, int errorCode) {
    }

    @Override
    public void returnRobotByIdSuccess(Robot robot) {
    }

    @Override
    public void returnRobotByIdFail(String error, int errorCode) {
    }

    @Override
    public void returnResetPasswordSuccess() {
    }

    @Override
    public void returnResetPasswordFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetAppTabsSuccess(GetAppMainTabResult getAppTabsResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetAppTabsFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnUserAppsSuccess(GetAppGroupResult getAppGroupResult, String clientConfigMyAppVersion) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUserAppsFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAllAppsSuccess(GetAllAppResult getAllAppResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAllAppsFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAllAppsFreshSuccess(GetAllAppResult getAllAppResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAllAppsFreshFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAllAppsMoreSuccess(GetAllAppResult getAllAppResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAllAppsMoreFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUploadCollectSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUploadCollectFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnReactNativeUpdateSuccess(ReactNativeUpdateBean reactNativeUpdateBean) {

    }

    @Override
    public void returnReactNativeUpdateFail(String error, int errorCode) {

    }

    @Override
    public void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult) {

    }

    @Override
    public void returnGetClientIdResultFail(String error, int errorCode) {

    }

    @Override
    public void returnGetAppAuthCodeResultSuccess(AppRedirectResult appRedirectResult) {

    }

    @Override
    public void returnGetAppAuthCodeResultFail(String error, int errorCode) {

    }

    @Override
    public void returnGetDownloadReactNativeUrlSuccess(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean) {

    }

    @Override
    public void returnGetDownloadReactNativeUrlFail(String error, int errorCode) {

    }

    @Override
    public void returnGetReactNativeInstallUrlSuccess(ReactNativeInstallUriBean reactNativeInstallUriBean) {

    }

    @Override
    public void returnGetReactNativeInstallUrlFail(String error, int errorCode) {

    }

    @Override
    public void returnVeriryApprovalPasswordSuccess(String password) {

    }

    @Override
    public void returnVeriryApprovalPasswordFail(String error, int errorCode) {

    }

    @Override
    public void returnNewsInstructionSuccess(GetNewsInstructionResult getNewsInstructionResult) {

    }

    @Override
    public void returnNewsInstructionFail(String error, int errorCode) {

    }

    @Override
    public void returnAppTabAutoSuccess(GetAppMainTabResult getAppMainTabResult, String mainTabSaveConfigVersion) {

    }

    @Override
    public void returnAppTabAutoFail(String error, int errorCode) {

    }

    @Override
    public void returnUserProfileConfigSuccess(UserProfileInfoBean userProfileInfoBean) {

    }

    @Override
    public void returnUserProfileConfigFail(String error, int errorCode) {

    }

    @Override
    public void returnSplashPageInfoSuccess(SplashPageBean splashPageBean) {

    }

    @Override
    public void returnSplashPageInfoFail(String error, int errorCode) {

    }

    @Override
    public void returnMDMStateSuccess(GetMDMStateResult getMDMStateResult) {

    }

    @Override
    public void returnMDMStateFail(String error, int errorCode) {

    }

    @Override
    public void returnUploadMDMInfoSuccess(UploadMDMInfoResult uploadMDMInfoResult) {

    }

    @Override
    public void returnUploadMDMInfoFail() {

    }

    @Override
    public void returnLoginDesktopCloudPlusSuccess(LoginDesktopCloudPlusBean loginDesktopCloudPlusBean) {

    }

    @Override
    public void returnLoginDesktopCloudPlusFail(String error, int errorCode) {

    }

    @Override
    public void returnDeviceLogListSuccess(GetDeviceLogResult getDeviceLogResult) {

    }

    @Override
    public void returnDeviceLogListFail(String error, int errorCode) {

    }

    @Override
    public void returnAppInfoSuccess(App app) {

    }

    @Override
    public void returnAppInfoFail(String error, int errorCode) {

    }

    @Override
    public void returnAppConfigSuccess(GetAppConfigResult getAppConfigResult) {

    }

    @Override
    public void returnAppConfigFail(String error, int errorCode) {

    }

    @Override
    public void returnSaveWebAutoRotateConfigSuccess(boolean isWebAutoRotate) {
    }

    @Override
    public void returnSaveWebAutoRotateConfigFail(String error, int errorCode) {
    }

    @Override
    public void returnUploadPositionSuccess() {
    }

    @Override
    public void returnWebAppRealUrlSuccess(GetWebAppRealUrlResult getWebAppRealUrlResult) {
    }

    @Override
    public void returnWebAppRealUrlFail() {
    }

    @Override
    public void returnSaveConfigSuccess() {
    }

    @Override
    public void returnSaveConfigFail() {
    }

    @Override
    public void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult) {
    }

    @Override
    public void returnVolumeListFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeFileListSuccess(GetVolumeFileListResult getVolumeFileListResult) {
    }

    @Override
    public void returnVolumeFileListFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, String fileLocalPath, VolumeFile mockVolumeFile) {
    }

    @Override
    public void returnVolumeFileUploadTokenFail(VolumeFile mockVolumeFile, String error, int errorCode, String filePath) {
    }

    @Override
    public void returnCreateForderSuccess(VolumeFile volumeFile) {
    }

    @Override
    public void returnCreateForderFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeFileDeleteSuccess(List<VolumeFile> deleteVolumeFileList) {
    }

    @Override
    public void returnVolumeFileDeleteFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeFileRenameSuccess(VolumeFile oldVolumeFile, String fileNewName) {
    }

    @Override
    public void returnVolumeFileRenameFail(String error, int errorCode) {
    }

    @Override
    public void returnMoveFileSuccess(List<VolumeFile> movedVolumeFileList) {
    }

    @Override
    public void returnMoveFileFail(String error, int errorCode) {
    }

    @Override
    public void returnRecommendAppWidgetListSuccess(GetRecommendAppWidgetListResult getRecommendAppWidgetListResult) {
    }

    @Override
    public void returnRecommendAppWidgetListFail(String error, int errorCode) {
    }

    @Override
    public void returnCopyFileSuccess() {
    }

    @Override
    public void returnCopyFileFail(String error, int errorCode) {
    }

    @Override
    public void returnCreateShareVolumeSuccess(Volume volume) {
    }

    @Override
    public void returnCreateShareVolumeFail(String error, int errorCode) {
    }

    @Override
    public void returnUpdateShareVolumeNameSuccess(Volume volume, String name) {
    }

    @Override
    public void returnUpdateShareVolumeNameFail(String error, int errorCode) {
    }

    @Override
    public void retrunRemoveShareVolumeSuccess(Volume volume) {
    }

    @Override
    public void returnRemoveShareVolumeFail(String error, int errorCode) {
    }

    @Override
    public void returnFaceSettingSuccess(GetFaceSettingResult getFaceSettingResult) {
    }

    @Override
    public void returnFaceSettingFail(String error, int errorCode) {
    }

    @Override
    public void returnFaceVerifySuccess(GetFaceSettingResult getFaceSettingResult) {
    }

    @Override
    public void returnFaceVerifyFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeDetailSuccess(VolumeDetail volumeDetail) {
    }

    @Override
    public void returnVolumeDetailFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeMemAddSuccess(List<String> uidList) {
    }

    @Override
    public void returnVolumeMemAddFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeMemDelSuccess(List<String> uidList) {
    }

    @Override
    public void returnVolumeMemDelFail(String error, int errorCode) {
    }

    @Override
    public void returnUpdateGroupNameSuccess(String name) {
    }

    @Override
    public void returnUpdateGroupNameFail(String error, int errorCode) {
    }

    @Override
    public void returnGroupMemAddSuccess(List<String> uidList) {
    }

    @Override
    public void returnGroupMemAddFail(String error, int errorCode) {
    }

    @Override
    public void returnGroupMemDelSuccess(List<String> uidList) {
    }

    @Override
    public void returnGroupMemDelFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeGroupContainMeSuccess(GetVolumeGroupResult getVolumeGroupResult) {
    }

    @Override
    public void returnVolumeGroupContainMeFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeGroupSuccess(GetVolumeResultWithPermissionResult getVolumeResultWithPermissionResult) {

    }

    @Override
    public void returnVolumeGroupFail(String error, int errorCode) {

    }

    @Override
    public void returnUpdateVolumeGroupPermissionSuccess(GetVolumeGroupPermissionResult getVolumeGroupPermissionResult) {

    }

    @Override
    public void returnUpdateVolumeGroupPermissionFail(String error, int errorCode) {

    }

    @Override
    public void returnOpenActionBackgroudUrlSuccess() {
    }

    @Override
    public void returnOpenActionBackgroudUrlFail(String error, int errorCode) {
    }

    @Override
    public void returnOpenDecideBotRequestSuccess() {

    }

    @Override
    public void returnOpenDecideBotRequestFail(String error, int errorCode) {

    }

    @Override
    public void returnUploadPushInfoResultSuccess(GetUploadPushInfoResult getUploadPushInfoResult) {
    }

    @Override
    public void returnUploadPushInfoResultFail(String error, int errorCode) {
    }

    @Override
    public void returnFaceLoginGSSuccess() {
    }

    @Override
    public void returnFaceLoginGSFail(String error, int errorCode) {
    }

    @Override
    public void returnChatFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult) {

    }

    @Override
    public void returnChatFileUploadTokenFail(String error, int errorCode) {

    }

    @Override
    public void returnContactUserListSuccess(byte[] bytes, String saveConfigVersion) {

    }

    @Override
    public void returnContactUserListFail(String error, int errorCode) {

    }

    @Override
    public void returnContactOrgListSuccess(byte[] bytes, String saveConfigVersion) {

    }

    @Override
    public void returnContactOrgListFail(String error, int errorCode) {

    }

    @Override
    public void returnContactUserListUpdateSuccess(GetContactUserListUpateResult getContactUserListUpateResult, String saveConfigVersion) {

    }

    @Override
    public void returnContactUserListUpdateFail(String error, int errorCode) {

    }

    @Override
    public void returnContactOrgListUpdateSuccess(GetContactOrgListUpateResult getContactOrgListUpateResult, String saveConfigVersion) {

    }

    @Override
    public void returnContactOrgListUpdateFail(String error, int errorCode) {

    }

    @Override
    public void returnCardPackageListSuccess(GetCardPackageResult getCardPackageResult) {

    }

    @Override
    public void returnCardPackageListFail(String error, int errorCode) {

    }

    @Override
    public void returnAllConfigVersionSuccess(GetAllConfigVersionResult getAllConfigVersionResult) {

    }

    @Override
    public void returnAllConfigVersionFail(String error, int errorCode) {

    }

    @Override
    public void returnGetVoiceCommunicationResultSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {

    }

    @Override
    public void returnGetVoiceCommunicationResultFail(String error, int errorCode) {

    }

    @Override
    public void returnGetVoiceCommunicationChannelInfoSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {

    }

    @Override
    public void returnGetVoiceCommunicationChannelInfoFail(String error, int errorCode) {

    }

    @Override
    public void returnJoinVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {

    }

    @Override
    public void returnJoinVoiceCommunicationChannelFail(String error, int errorCode) {

    }

    @Override
    public void returnRefuseVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {

    }

    @Override
    public void returnRefuseVoiceCommunicationChannelFail(String error, int errorCode) {

    }

    @Override
    public void returnLeaveVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {

    }

    @Override
    public void returnLeaveVoiceCommunicationChannelFail(String error, int errorCode) {

    }

    @Override
    public void returnQuitChannelGroupSuccess() {
    }

    @Override
    public void returnQuitChannelGroupSuccessFail(String error, int errorCode) {
    }

    @Override
    public void returnConversationListSuccess(GetConversationListResult getConversationListResult) {
    }

    @Override
    public void returnConversationListFail(String error, int errorCode) {
    }

    @Override
    public void returnSetConversationStickSuccess(String id, boolean isStick) {
    }

    @Override
    public void returnSetConversationStickFail(String error, int errorCode) {
    }

    @Override
    public void returnSetConversationHideSuccess(String id, boolean isHide) {
    }

    @Override
    public void returnSetConversationHideFail(String error, int errorCode) {
    }

    @Override
    public void returnAddConversationGroupMemberSuccess(List<String> uidList) {

    }

    @Override
    public void returnAddConversationGroupMemberFail(String error, int errorCode) {

    }

    @Override
    public void returnDelConversationGroupMemberSuccess(List<String> uidList) {

    }

    @Override
    public void returnDelConversationGroupMemberFail(String error, int errorCode) {

    }

    @Override
    public void returnWebexMeetingListSuccess(GetWebexMeetingListResult getWebexMeetingListResult) {
    }

    @Override
    public void returnWebexMeetingListFail(String error, int errorCode) {
    }

    @Override
    public void returnScheduleWebexMeetingSuccess(GetScheduleWebexMeetingSuccess getScheduleWebexMeetingSuccess) {
    }

    @Override
    public void returnScheduleWebexMeetingFail(String error, int errorCode) {
    }

    @Override
    public void returnWebexMeetingSuccess(WebexMeeting webexMeeting) {
    }

    @Override
    public void returnWebexMeetingFail(String error, int errorCode) {
    }

    @Override
    public void returnWebexTKSuccess(GetWebexTKResult getWebexTKResult) {
    }

    @Override
    public void returnWebexTKFail(String error, int errorCode) {
    }

    @Override
    public void returnRemoveWebexMeetingSuccess() {
    }

    @Override
    public void returnRemoveWebexMeetingFail(String error, int errorCode) {
    }

    @Override
    public void returnConversationInfoSuccess(Conversation conversation) {
    }

    @Override
    public void returnConversationInfoFail(String error, int errorCode) {
    }

    @Override
    public void returnUpdateConversationNameSuccess() {
    }

    @Override
    public void returnUpdateConversationNameFail(String error, int errorCode) {
    }

    @Override
    public void returnExperienceUpgradeFlagSuccess(GetExperienceUpgradeFlagResult getExperienceUpgradeFlagResult) {
    }

    @Override
    public void returnExperienceUpgradeFlagFail(String error, int errorCode) {
    }

    @Override
    public void returnUpdateExperienceUpgradeFlagSuccess() {
    }

    @Override
    public void returnUpdateExperienceUpgradeFlagFail(String error, int errorCode) {
    }

    @Override
    public void returnCreateDirectConversationSuccess(Conversation conversation) {
    }

    @Override
    public void returnCreateDirectConversationFail(String error, int errorCode) {
    }

    @Override
    public void returnCreateGroupConversationSuccess(Conversation conversation) {
    }

    @Override
    public void returnCreateGroupConversationFail(String error, int errorCode) {
    }

    @Override
    public void returnDeleteConversationSuccess(String cid) {
    }

    @Override
    public void returnDeleteConversationFail(String error, int errorCode) {
    }

    @Override
    public void returnBadgeCountSuccess(BadgeBodyModel badgeBodyModel) {
    }

    @Override
    public void returnBadgeCountFail(String error, int errorCode) {
    }

    @Override
    public void returnMailFolderSuccess(GetMailFolderResult getMailForderResult) {
    }

    @Override
    public void returnMailFolderFail(String error, int errorCode) {
    }

    @Override
    public void returnMailListSuccess(String folderId, int pageSize, int offset, GetMailListResult getMailListResult) {
    }

    @Override
    public void returnMailListFail(String folderId, int pageSize, int offset, String error, int errorCode) {
    }

    @Override
    public void returnMailDetailSuccess(GetMailDetailResult getMailDetailResult) {
    }

    @Override
    public void returnMailDetailSuccess(byte[] arg0) {
    }

    @Override
    public void returnMailDetailFail(String error, int errorCode) {
    }

    @Override
    public void returnMailLoginSuccess() {
    }

    @Override
    public void returnMailLoginFail(String error, int errorCode) {
    }

    @Override
    public void returnMailCertificateUploadSuccess(byte[] arg0) {

    }

    @Override
    public void returnMailCertificateUploadFail(String error, int errorCode) {

    }

    @Override
    public void returnSendMailSuccess() {
    }

    @Override
    public void returnSendMailFail(String error, int errorCode) {
    }

    @Override
    public void returnRemoveMailSuccess() {
    }

    @Override
    public void returnRemoveMailFail(String error, int errorCode) {
    }

    @Override
    public void returnUserCardMenusSuccess(GetUserCardMenusResult getUserCardMenusResult) {
    }

    @Override
    public void returnUserCardMenusFail(String error, int errorCode) {
    }

    @Override
    public void returnScheduleListSuccess(GetScheduleListResult getScheduleListResult, Calendar startCalendar, Calendar endCalendar, List<String> calendarIdList, List<String> meetingIdList, List<String> taskIdList) {
    }

    @Override
    public void returnScheduleListFail(String error, int errorCode) {
    }

    @Override
    public void returnAddScheduleSuccess(GetIDResult getIDResult) {

    }

    @Override
    public void returnAddScheduleFail(String error, int errorCode) {

    }

    @Override
    public void returnUpdateScheduleSuccess() {

    }

    @Override
    public void returnUpdateScheduleFail(String error, int errorCode) {

    }

    @Override
    public void returnDeleteScheduleSuccess() {

    }

    @Override
    public void returnDeleteScheduleFail(String error, int errorCode) {

    }

    @Override
    public void returnDelTaskTagSuccess() {

    }

    @Override
    public void returnDelTaskTagFail(String error, int errorCode) {

    }

    @Override
    public void returnAddTaskTagSuccess() {

    }

    @Override
    public void returnAddTaskTagFail(String error, int errorCode) {

    }

    @Override
    public void returnAddMeetingSuccess() {

    }

    @Override
    public void returnAddMeetingFail(String error, int errorCode) {

    }

    @Override
    public void returnDelMeetingSuccess(Meeting meeting) {

    }

    @Override
    public void returnDelMeetingFail(String error, int errorCode) {

    }

    @Override
    public void returnMeetingDataFromIdSuccess(Meeting meeting) {

    }

    @Override
    public void returnMeetingDataFromIdFail(String error, int errorCode) {

    }

    @Override
    public void returnScheduleDataFromIdSuccess(Schedule schedule) {

    }

    @Override
    public void returnScheduleDataFromIdFail(String error, int errorCode) {

    }

    @Override
    public void returnMeetingListSuccess(GetMeetingListResult getMeetingListByMeetingRoomResult) {

    }

    @Override
    public void returnMeetingListByMeetingRoomFail(String error, int errorCode) {

    }

    @Override
    public void returnNaviBarModelSuccess(NaviBarModel naviBarModel) {

    }

    @Override
    public void returnNaviBarModelFail(String error, int errorCode) {

    }

    @Override
    public void returnMeetingHistoryListSuccess(GetMeetingListResult getMeetingListByMeetingRoomResult) {

    }

    @Override
    public void returnMeetingHistoryListFail(String error, int errorCode) {

    }

    @Override
    public void returnUpdateMeetingSuccess() {

    }

    @Override
    public void returnUpdateMeetingFail(String error, int errorCode) {

    }

    @Override
    public void returnHolidayDataSuccess(GetHolidayDataResult getHolidayDataResult) {

    }

    @Override
    public void returnHolidayDataFail(String error, int errorCode) {

    }
}
