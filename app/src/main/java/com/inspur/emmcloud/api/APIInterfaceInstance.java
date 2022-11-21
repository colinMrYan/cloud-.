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
import com.inspur.emmcloud.bean.chat.GetServiceChannelInfoListResult;
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.GetWebSocketUrlResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.chat.ScanCodeJoinConversationBean;
import com.inspur.emmcloud.bean.chat.TransferGroupBean;
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
import com.inspur.emmcloud.componentservice.communication.ServiceChannelInfo;
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
        // TODO Auto-generated method stub
    }

    @Override
    public void returnMsgCommentCountFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult, boolean isManualCheck) {
    }

    @Override
    public void returnUpgradeFail(String error, boolean isManualCheck, int errorCode) {
    }

    @Override
    public void returnGetReactNativeInstallUrlSuccess(ReactNativeInstallUriBean reactNativeInstallUriBean) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetReactNativeInstallUrlFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }


    @Override
    public void returnChannelListSuccess(
            GetChannelListResult getSessionListResult) {

    }

    @Override
    public void returnChannelListFail(String error, int errorCode) {

    }

    @Override
    public void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult) {

    }

    @Override
    public void returnNewMessagesSuccess(GetChannelMessagesResult getChannelMessagesResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnNewMessagesFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnNewMsgsFail(String error, int errorCode) {

    }

    @Override
    public void returnMsgCommentSuccess(GetMsgCommentResult getMsgCommentResult, String mid) {

    }

    @Override
    public void returnMsgCommentFail(String error, int errorCode) {

    }


    @Override
    public void returnWebSocketUrlSuccess(
            GetWebSocketUrlResult getWebSocketResult) {

    }

    @Override
    public void returnWebSocketUrlFail(String error, int errorCode) {

    }

    @Override
    public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult, String fakeMessageId) {

    }

    @Override
    public void returnSendMsgFail(String error, String fakeMessageId, int errorCode) {

    }

    @Override
    public void returnUploadResImgSuccess(GetNewsImgResult getNewsImgResult, String fakeMessageId) {

    }

    @Override
    public void returnUploadResImgFail(String error, int errorCode, String fakeMessageId) {

    }


    @Override
    public void returnMsgSuccess(GetMsgResult getMsgResult) {

    }

    @Override
    public void returnMsgFail(String error, int errorCode) {

    }

    @Override
    public void returnBookingRoomSuccess() {

    }

    @Override
    public void returnBookingRoomFail(String error, int errorCode) {
    }

    @Override
    public void returnChannelInfoSuccess(
            ChannelGroup channelGroup) {

    }

    @Override
    public void returnChannelInfoFail(String error, int errorCode) {
    }

    @Override
    public void returnUpLoadResFileSuccess(GetFileUploadResult getFileUploadResult, String fakeMessageId) {
    }

    @Override
    public void returnUpLoadResFileFail(String error, int errorCode, String fakeMessageId) {

    }

    @Override
    public void returnSearchChannelGroupSuccess(GetSearchChannelGroupResult getSearchChannelGroupResult) {
    }

    @Override
    public void returnSearchChannelGroupFail(String error, int errorCode) {
    }


    @Override
    public void returnCreateSingleChannelSuccess(GetCreateSingleChannelResult getCreatSingleChannelResult) {
    }

    @Override
    public void returnCreateSingleChannelFail(String error, int errorCode) {

    }

    @Override
    public void returnCreateChannelGroupSuccess(ChannelGroup channelGroup) {

    }

    @Override
    public void returnCreateChannelGroupFail(String error, int errorCode) {

    }

    @Override
    public void returnTripSuccess(Trip trip) {

    }

    @Override
    public void returnTripFail(String error, int errorCode) {

    }

    @Override
    public void returnLastUploadTripSuccess(Trip trip) {

    }

    @Override
    public void returnLastUploadTripFail(String error, int errorCode) {

    }

    @Override
    public void returnUpdateChannelGroupNameSuccess(
            GetBoolenResult getBoolenResult) {

    }

    @Override
    public void returnUpdateChannelGroupNameFail(String error, int errorCode) {

    }

    @Override
    public void returnUploadTrainTicketSuccess() {

    }

    @Override
    public void returnUploadTrainTicketFail(String error, int errorCode) {

    }

    @Override
    public void returnAddMembersSuccess(ChannelGroup channelGroup) {

    }

    @Override
    public void returnAddMembersFail(String error, int errorCode) {

    }


    @Override
    public void returnDndSuccess() {

    }

    @Override
    public void returnDndFail(String error, int errorCode) {

    }

    @Override
    public void returnAddAdministratorSuccess(String result) {

    }

    @Override
    public void returnAddAdministratorFail(String error, int errorCode) {

    }

    @Override
    public void returnRemoveAdministratorSuccess(String result) {

    }

    @Override
    public void returnRemoveAdministratorFail(String error, int errorCode) {

    }

    @Override
    public void returnEnableGroupSilentSuccess(String result) {

    }

    @Override
    public void returnEnableGroupSilentFail(String error, int errorCode) {

    }

    @Override
    public void returnDisableGroupSilentSuccess(String result) {

    }

    @Override
    public void returnDisableGroupSilentFail(String error, int errorCode) {

    }

    @Override
    public void returnTripArriveSuccess(GetTripArriveCity getTripArriveCity) {

    }

    @Override
    public void returnTripArriveFail(String error, int errorCode) {

    }

    @Override
    public void returnDelMembersSuccess(ChannelGroup channelGroup) {

    }

    @Override
    public void returnDelMembersFail(String error, int errorCode) {

    }

    @Override
    public void returnKnowledgeListSuccess(GetKnowledgeInfo getKnowledgeInfo) {

    }

    @Override
    public void returnKnowledgeListFail(String error, int errorCode) {
    }


    @Override
    public void returnFindSearchFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnFindMixSearchFail(String error, int errorCode) {
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
    public void returnReactNativeUpdateSuccess(ReactNativeUpdateBean reactNativeUpdateBean) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnReactNativeUpdateFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetClientIdResultFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }


    @Override
    public void returnVeriryApprovalPasswordSuccess(String password) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnVeriryApprovalPasswordFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnNewsInstructionSuccess(GetNewsInstructionResult getNewsInstructionResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnNewsInstructionFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAppTabAutoSuccess(GetAppMainTabResult getAppMainTabResult, String mainTabSaveConfigVersion) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAppTabAutoFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }


    @Override
    public void returnSplashPageInfoSuccess(SplashPageBean splashPageBean) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSplashPageInfoFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }


    @Override
    public void returnAppConfigSuccess(GetAppConfigResult getAppConfigResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAppConfigFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSaveWebAutoRotateConfigSuccess(boolean isWebAutoRotate) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSaveWebAutoRotateConfigFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnUploadPositionSuccess() {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSaveConfigSuccess() {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSaveConfigFail() {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnChatFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, ChatFileUploadInfo chatFileUploadInfo) {
        // TODO Auto-generated method stub
    }


    @Override
    public void returnOpenActionBackgroudUrlSuccess() {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnOpenActionBackgroudUrlFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnOpenDecideBotRequestSuccess() {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnOpenDecideBotRequestFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnUploadPushInfoResultSuccess(GetUploadPushInfoResult getUploadPushInfoResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnUploadPushInfoResultFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnChatFileUploadTokenFail(String error, int errorCode, ChatFileUploadInfo chatFileUploadInfo) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnContactUserListSuccess(byte[] bytes, String saveConfigVersion) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnContactUserListFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnContactOrgListSuccess(byte[] bytes, String saveConfigVersion) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnContactOrgListFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnMultiContactOrgSuccess(GetMultiContactResult getMultiContactResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnMultiContactOrgFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnContactUserListUpdateSuccess(GetContactUserListUpateResult getContactUserListUpateResult, String saveConfigVersion) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnContactUserListUpdateFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnContactOrgListUpdateSuccess(GetContactOrgListUpateResult getContactOrgListUpateResult, String saveConfigVersion) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnContactOrgListUpdateFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }


    @Override
    public void returnGetVoiceCommunicationResultSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetVoiceCommunicationResultFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetVoiceCommunicationChannelInfoSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetVoiceCommunicationChannelInfoFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnJoinVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnJoinVoiceCommunicationChannelFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnRefuseVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnRefuseVoiceCommunicationChannelFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnLeaveVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnLeaveVoiceCommunicationChannelFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnQuitChannelGroupSuccess() {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnQuitChannelGroupSuccessFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnConversationListSuccess(GetConversationListResult getConversationListResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnConversationListFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSetConversationStickSuccess(String id, boolean isStick) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSetConversationStickFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSetConversationHideSuccess(String id, boolean isHide) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSetConversationHideFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAddConversationGroupMemberSuccess(List<String> uidList) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAddConversationGroupMemberFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnDelConversationGroupMemberSuccess(List<String> uidList) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnDelConversationGroupMemberFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnConversationInfoSuccess(Conversation conversation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnConversationInfoFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnUpdateConversationNameSuccess() {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnUpdateConversationNameFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnUpdateConversationNicknameSuccess(Conversation conversation) {

    }

    @Override
    public void returnUpdateConversationNicknameFail(String error, int errorCode) {

    }


    @Override
    public void returnCreateDirectConversationSuccess(Conversation conversation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnCreateDirectConversationFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnCreateGroupConversationSuccess(Conversation conversation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnCreateGroupConversationFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnDeleteConversationSuccess(String cid) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnDeleteConversationFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnNaviBarModelSuccess(NaviBarModel naviBarModel) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnNaviBarModelFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }


    @Override
    public void returnShareFileToFriendsFromVolumeSuccess(String newPath, VolumeFile volumeFile) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnShareFileToFriendsFromVolumeFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnInvitationContentSuccess(ScanCodeJoinConversationBean scanCodeJoinConversationBean) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnInvitationContentFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAppRoleSuccess(String appRole) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnAppRoleFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnIsAgreedSuccess(String isSuccess) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnIsAgreedFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSaveAgreedSuccess(String isSaveSuccess) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSaveAgreedFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnTransmitPictureSuccess(String cid, String description, Message message) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnTransmitPictureError(String error, int errorCode) {
        // TODO Auto-generated method stub
    }


    @Override
    public void returnGetDownloadReactNativeUrlSuccess(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetDownloadReactNativeUrlFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetConversationServiceListSuccess(GetServiceChannelInfoListResult getConversationListResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetConversationServiceListFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetConversationServiceListAllSuccess(GetServiceChannelInfoListResult getConversationListResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnGetConversationServiceListAllFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSearchConversationServiceSuccess(GetServiceChannelInfoListResult getConversationListResult) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnSearchConversationServiceFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnFollowConversationServiceSuccess(ServiceChannelInfo conversations) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnFollowConversationServiceFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnTransferGroupFail(String error, int errorCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnTransferGroupSuccess(TransferGroupBean bean) {
        // TODO Auto-generated method stub
    }
}
