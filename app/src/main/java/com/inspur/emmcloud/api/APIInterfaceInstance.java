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
import com.inspur.emmcloud.bean.GetAppConfigResult;
import com.inspur.emmcloud.bean.GetAppBadgeResult;
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
    public void returnUsersInOrgSuccess(
            GetAdressUsersResult getAdressUsersResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUsersInOrgFail(String error, int errorCode) {
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
    public void returnNewsSuccess(GetNewsResult getNewsResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnNewsFail(String error, int errorCode) {
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
    public void returnUploadMsgImgSuccess(GetNewsImgResult getNewsImgResult, String fakeMessageId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUploadMsgImgFail(String error, int errorCode) {
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
            GetGroupNewsDetailResult getGroupNewsDetailResult,int page) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnGroupNewsDetailFail(String error, int errorCode,int page) {
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
    public void returnFileUpLoadSuccess(GetFileUploadResult getFileUploadResult, String fakeMessageId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnFileUpLoadFail(String error, int errorCode) {
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
    public void returnUpdateTaskSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUpdateTaskFail(String error, int errorCode) {
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
    public void returnDelAttachmentFail(String error, int errorCode,int position) {
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
    public void returnCardPackageListSuccess(
            GetCardPackageListResult getCardPackageListResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCardPackageListFail(String error, int errorCode) {
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
     * @see com.inspur.emmcloud.api.APIInterface#returnMeettingsSuccess(com.inspur.emmcloud.bean.GetMeetingsResult, boolean)
     */
    @Override
    public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult,
                                      boolean isLoadMore) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.inspur.emmcloud.api.APIInterface#returnLanguageSuccess(com.inspur.emmcloud.bean.GetLanguageResult)
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
     * @see com.inspur.emmcloud.api.APIInterface#returnMeettingRoomsSuccess(com.inspur.emmcloud.bean.GetMeetingRoomsResult, boolean)
     */
    @Override
    public void returnMeetingRoomsSuccess(
            GetMeetingRoomsResult getMeetingRoomsResult, boolean isFilte) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.inspur.emmcloud.api.APIInterface#returnFindSearchSuccess(com.inspur.emmcloud.bean.GetFindSearchResult)
     */
    @Override
    public void returnFindSearchSuccess(GetFindSearchResult getFindSearchResult) {
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
     * @see com.inspur.emmcloud.api.APIInterface#returnFindMixSearchSuccess(com.inspur.emmcloud.bean.GetFindMixSearchResult)
     */
    @Override
    public void returnFindMixSearchSuccess(
            GetFindMixSearchResult getFindMixSearchResult) {
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
     * @see com.inspur.emmcloud.api.APIInterface#returnMeettingListSuccess(com.inspur.emmcloud.bean.GetMeetingListResult, int)
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
}
