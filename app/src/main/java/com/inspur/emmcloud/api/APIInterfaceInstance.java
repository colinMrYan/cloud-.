package com.inspur.emmcloud.api;


import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.AppRedirectResult;
import com.inspur.emmcloud.bean.appcenter.GetAddAppResult;
import com.inspur.emmcloud.bean.appcenter.GetAllAppResult;
import com.inspur.emmcloud.bean.appcenter.GetAppBadgeResult;
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
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.GetAddMembersSuccessResult;
import com.inspur.emmcloud.bean.chat.GetAllRobotsResult;
import com.inspur.emmcloud.bean.chat.GetChannelInfoResult;
import com.inspur.emmcloud.bean.chat.GetChannelListResult;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.GetFileUploadResult;
import com.inspur.emmcloud.bean.chat.GetMsgCommentCountResult;
import com.inspur.emmcloud.bean.chat.GetMsgCommentResult;
import com.inspur.emmcloud.bean.chat.GetMsgResult;
import com.inspur.emmcloud.bean.chat.GetNewMessagesResult;
import com.inspur.emmcloud.bean.chat.GetNewMsgsResult;
import com.inspur.emmcloud.bean.chat.GetNewsImgResult;
import com.inspur.emmcloud.bean.chat.GetNewsInstructionResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.GetUploadPushInfoResult;
import com.inspur.emmcloud.bean.chat.GetWebSocketUrlResult;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.contact.GetAllContactResult;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.find.GetKnowledgeInfo;
import com.inspur.emmcloud.bean.find.GetTripArriveCity;
import com.inspur.emmcloud.bean.find.Trip;
import com.inspur.emmcloud.bean.login.GetDeviceCheckResult;
import com.inspur.emmcloud.bean.login.GetLoginResult;
import com.inspur.emmcloud.bean.login.GetMDMStateResult;
import com.inspur.emmcloud.bean.login.GetSignoutResult;
import com.inspur.emmcloud.bean.login.GetUpdatePwdBySMSCodeBean;
import com.inspur.emmcloud.bean.login.LoginDesktopCloudPlusBean;
import com.inspur.emmcloud.bean.mine.GetBindingDeviceResult;
import com.inspur.emmcloud.bean.mine.GetDeviceLogResult;
import com.inspur.emmcloud.bean.mine.GetFaceSettingResult;
import com.inspur.emmcloud.bean.mine.GetLanguageResult;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.bean.mine.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.mine.GetUserHeadUploadResult;
import com.inspur.emmcloud.bean.mine.UserProfileInfoBean;
import com.inspur.emmcloud.bean.system.GetAppConfigResult;
import com.inspur.emmcloud.bean.system.GetAppTabAutoResult;
import com.inspur.emmcloud.bean.system.GetAppTabsResult;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.bean.system.GetUpgradeResult;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.bean.work.Attachment;
import com.inspur.emmcloud.bean.work.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.work.GetCreateOfficeResult;
import com.inspur.emmcloud.bean.work.GetIsAdmin;
import com.inspur.emmcloud.bean.work.GetLoctionResult;
import com.inspur.emmcloud.bean.work.GetMeetingListResult;
import com.inspur.emmcloud.bean.work.GetMeetingReplyResult;
import com.inspur.emmcloud.bean.work.GetMeetingRoomsResult;
import com.inspur.emmcloud.bean.work.GetMeetingsResult;
import com.inspur.emmcloud.bean.work.GetMyCalendarResult;
import com.inspur.emmcloud.bean.work.GetOfficeResult;
import com.inspur.emmcloud.bean.work.GetTagResult;
import com.inspur.emmcloud.bean.work.GetTaskAddResult;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.work.TaskResult;

import java.util.List;

public class APIInterfaceInstance implements APIInterface {
    @Override
    public void returnDeviceCheckSuccess(GetDeviceCheckResult getDeviceCheckResult) {
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
    public void returnOauthSigninSuccess(GetLoginResult getLoginResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnOauthSigninFail(String error, int errorCode) {
        // TODO Auto-generated method stub
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
    public void returnReqLoginSMSSuccess(GetBoolenResult getBoolenResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnReqLoginSMSFail(String error, int errorCode) {
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
            GetUploadMyHeadResult getUploadMyInfoResult) {
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
    public void returnNewMessagesSuccess(GetNewMessagesResult getNewMessagesResult) {

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
    public void returnMeetingRoomsSuccess(
            GetMeetingRoomsResult getMeetingRoomsResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnMeetingRoomsFail(String error, int errorCode) {
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
            GetChannelInfoResult getChannelInfoResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnChannelInfoFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }


    @Override
    public void returnAllContactSuccess(GetAllContactResult getAllContactResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAllContactFail(String error, int errorCode) {
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
            GetMeetingListResult getMeetingListResult) {
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
    public void returnLoctionResultSuccess(GetLoctionResult getLoctionResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnLoctionResultFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnOfficeResultSuccess(GetOfficeResult getOfficeResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnOfficeResultFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreatOfficeSuccess(
            GetCreateOfficeResult getCreateOfficeResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreatOfficeFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAddMembersSuccess(
            GetAddMembersSuccessResult getAddMembersSuccessResult) {
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
    public void returnAttachmentSuccess(TaskResult taskResult) {
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
    public void returnModifyPsdSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnModifyPsdFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnTripArriveSuccess(GetTripArriveCity getTripArriveCity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void retrunTripArriveFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelMeetingSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelMeetingFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelMembersSuccess(
            GetChannelInfoResult getChannelInfoResult) {
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
    public void returnDeleteOfficeSuccess(int position) {
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
    public void returnIsAdminSuccess(GetIsAdmin getIsAdmin) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnIsAdminFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.inspur.emmcloud.api.APIInterface#returnMeettingsSuccess(com.inspur.emmcloud.bean.work.GetMeetingsResult, boolean)
     */
    @Override
    public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult,
                                      int page) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.inspur.emmcloud.api.APIInterface#returnLanguageSuccess(com.inspur.emmcloud.bean.mine.GetLanguageResult)
     */
    @Override
    public void returnLanguageSuccess(GetLanguageResult getLanguageResult) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.inspur.emmcloud.api.APIInterface#returnLanguageFail(java.lang.String)
     */
    @Override
    public void returnLanguageFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.inspur.emmcloud.api.APIInterface#returnMeettingRoomsSuccess(com.inspur.emmcloud.bean.work.GetMeetingRoomsResult, boolean)
     */
    @Override
    public void returnMeetingRoomsSuccess(
            GetMeetingRoomsResult getMeetingRoomsResult, boolean isFilte) {
        // TODO Auto-generated method stub

    }


    /* (non-Javadoc)
     * @see com.inspur.emmcloud.api.APIInterface#returnFindSearchFail(java.lang.String)
     */
    @Override
    public void returnFindSearchFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.inspur.emmcloud.api.APIInterface#returnFindMixSearchFail(java.lang.String)
     */
    @Override
    public void returnFindMixSearchFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }


    /* (non-Javadoc)
     * @see com.inspur.emmcloud.api.APIInterface#returnMeettingListSuccess(com.inspur.emmcloud.bean.work.GetMeetingListResult, int)
     */
    @Override
    public void returnMeetingListSuccess(
            GetMeetingListResult getMeetingListResult, String date) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAllRobotsSuccess(GetAllRobotsResult getAllRobotsResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnAllRobotsFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnRobotByIdSuccess(Robot robot) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnRobotByIdFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }


    @Override
    public void returnUpdatePwdBySMSCodeSuccess(
            GetUpdatePwdBySMSCodeBean getUpdatePwdBySMSCodeBean) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpdatePwdBySMSCodeFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGetAppTabsSuccess(GetAppTabsResult getAppTabsResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGetAppTabsFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUserAppsSuccess(GetAppGroupResult getAppGroupResult) {
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
    public void returnAppTabAutoSuccess(GetAppTabAutoResult getAppTabAutoResult) {

    }

    @Override
    public void returnAppTabAutoFail(String error, int errorCode) {

    }

    @Override
    public void returnUserProfileSuccess(UserProfileInfoBean userProfileInfoBean) {

    }

    @Override
    public void returnUserProfileFail(String error, int errorCode) {

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
    public void returnGetAppBadgeResultSuccess(GetAppBadgeResult getAppBadgeResult) {
    }

    @Override
    public void returnGetAppBadgeResultFail(String error, int errorCode) {
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
    public void returnContactUserListSuccess(byte[] bytes) {

    }

    @Override
    public void returnContactUserListFail(String error, int errorCode) {

    }

    @Override
    public void returnContactUserSuccess(String result) {

    }
}
