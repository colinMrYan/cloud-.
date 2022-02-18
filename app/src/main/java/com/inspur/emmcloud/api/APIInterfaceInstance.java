package com.inspur.emmcloud.api;


import com.inspur.emmcloud.basemodule.application.GetClientIdRsult;
import com.inspur.emmcloud.basemodule.bean.GetUploadPushInfoResult;
import com.inspur.emmcloud.bean.ChatFileUploadInfo;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.GetAllRobotsResult;
import com.inspur.emmcloud.bean.chat.GetChannelListResult;
import com.inspur.emmcloud.bean.chat.GetChannelMessagesResult;
import com.inspur.emmcloud.bean.chat.GetConversationListResult;
import com.inspur.emmcloud.bean.chat.GetFileUploadResult;
import com.inspur.emmcloud.bean.chat.GetMsgCommentCountResult;
import com.inspur.emmcloud.bean.chat.GetMsgCommentResult;
import com.inspur.emmcloud.bean.chat.GetMsgResult;
import com.inspur.emmcloud.bean.chat.GetNewMsgsResult;
import com.inspur.emmcloud.bean.chat.GetNewsImgResult;
import com.inspur.emmcloud.bean.chat.GetNewsInstructionResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.GetWebSocketUrlResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.chat.ScanCodeJoinConversationBean;
import com.inspur.emmcloud.bean.contact.GetContactOrgListUpateResult;
import com.inspur.emmcloud.bean.contact.GetContactUserListUpateResult;
import com.inspur.emmcloud.bean.contact.GetMultiContactResult;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.find.GetKnowledgeInfo;
import com.inspur.emmcloud.bean.find.GetTripArriveCity;
import com.inspur.emmcloud.bean.find.Trip;
import com.inspur.emmcloud.bean.system.GetAppConfigResult;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.bean.system.GetUpgradeResult;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.componentservice.application.maintab.GetAppMainTabResult;
import com.inspur.emmcloud.componentservice.application.navibar.NaviBarModel;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.GetCreateSingleChannelResult;
import com.inspur.emmcloud.componentservice.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.reactnative.bean.ReactNativeDownloadUrlBean;
import com.inspur.reactnative.bean.ReactNativeInstallUriBean;
import com.inspur.reactnative.bean.ReactNativeUpdateBean;

import java.util.List;

public class APIInterfaceInstance implements APIInterface {

    public APIInterfaceInstance() {
        super();
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
    public void returnGetReactNativeInstallUrlSuccess(ReactNativeInstallUriBean reactNativeInstallUriBean) {

    }

    @Override
    public void returnGetReactNativeInstallUrlFail(String error, int errorCode) {

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
    public void returnCreateSingleChannelSuccess(
            GetCreateSingleChannelResult getCreatSingleChannelResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreateSingleChannelFail(String error, int errorCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreateChannelGroupSuccess(ChannelGroup channelGroup) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnCreateChannelGroupFail(String error, int errorCode) {
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
    public void returnUploadTrainTicketSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnUploadTrainTicketFail(String error, int errorCode) {
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
    public void returnDndSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDndFail(String error, int errorCode) {
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
    public void returnDelMembersSuccess(
            ChannelGroup channelGroup) {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnDelMembersFail(String error, int errorCode) {
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
    public void returnFindSearchFail(String error, int errorCode) {
    }

    @Override
    public void returnFindMixSearchFail(String error, int errorCode) {
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
    public void returnSplashPageInfoSuccess(SplashPageBean splashPageBean) {

    }

    @Override
    public void returnSplashPageInfoFail(String error, int errorCode) {

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
    public void returnSaveConfigSuccess() {
    }

    @Override
    public void returnSaveConfigFail() {
    }

    @Override
    public void returnChatFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, ChatFileUploadInfo chatFileUploadInfo) {

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
    public void returnChatFileUploadTokenFail(String error, int errorCode, ChatFileUploadInfo chatFileUploadInfo) {

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
    public void returnMultiContactOrgSuccess(GetMultiContactResult getMultiContactResult) {

    }

    @Override
    public void returnMultiContactOrgFail(String error, int errorCode) {

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
    public void returnNaviBarModelSuccess(NaviBarModel naviBarModel) {

    }

    @Override
    public void returnNaviBarModelFail(String error, int errorCode) {

    }


    @Override
    public void returnShareFileToFriendsFromVolumeSuccess(String newPath, VolumeFile volumeFile) {

    }

    @Override
    public void returnShareFileToFriendsFromVolumeFail(String error, int errorCode) {

    }

    @Override
    public void returnInvitationContentSuccess(ScanCodeJoinConversationBean scanCodeJoinConversationBean) {

    }

    @Override
    public void returnInvitationContentFail(String error, int errorCode) {

    }

    @Override
    public void returnAppRoleSuccess(String appRole) {

    }

    @Override
    public void returnAppRoleFail(String error, int errorCode) {

    }

    @Override
    public void returnIsAgreedSuccess(String isSuccess) {

    }

    @Override
    public void returnIsAgreedFail(String error, int errorCode) {

    }

    @Override
    public void returnSaveAgreedSuccess(String isSaveSuccess) {

    }

    @Override
    public void returnSaveAgreedFail(String error, int errorCode) {

    }

    @Override
    public void returnTransmitPictureSuccess(String cid, String description, Message message) {

    }

    @Override
    public void returnTransmitPictureError(String error, int errorCode) {

    }


    @Override
    public void returnGetDownloadReactNativeUrlSuccess(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean) {

    }

    @Override
    public void returnGetDownloadReactNativeUrlFail(String error, int errorCode) {

    }

    @Override
    public void returnGetConversationServiceListSuccess(GetConversationListResult getConversationListResult) {

    }

    @Override
    public void returnGetConversationServiceListFail(String error, int errorCode) {

    }

    @Override
    public void returnGetConversationServiceListAllSuccess(GetConversationListResult getConversationListResult) {

    }

    @Override
    public void returnGetConversationServiceListAllFail(String error, int errorCode) {

    }

    @Override
    public void returnSearchConversationServiceSuccess(GetConversationListResult getConversationListResult) {

    }

    @Override
    public void returnSearchConversationServiceFail(String error, int errorCode) {

    }

    @Override
    public void returnFollowConversationServiceSuccess(Conversation conversations) {

    }

    @Override
    public void returnFollowConversationServiceFail(String error, int errorCode) {

    }
}
