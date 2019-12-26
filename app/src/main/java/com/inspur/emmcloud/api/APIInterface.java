package com.inspur.emmcloud.api;

import com.inspur.emmcloud.basemodule.bean.GetUploadPushInfoResult;
import com.inspur.emmcloud.bean.ChatFileUploadInfo;
import com.inspur.emmcloud.bean.appcenter.GetClientIdRsult;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailDetailResult;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailFolderResult;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailListResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetReturnMoveOrCopyErrorResult;
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
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.GetWebSocketUrlResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.chat.ScanCodeJoinConversationBean;
import com.inspur.emmcloud.bean.contact.GetContactOrgListUpateResult;
import com.inspur.emmcloud.bean.contact.GetContactUserListUpateResult;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.find.GetKnowledgeInfo;
import com.inspur.emmcloud.bean.find.GetTripArriveCity;
import com.inspur.emmcloud.bean.find.Trip;
import com.inspur.emmcloud.bean.system.GetAppConfigResult;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.bean.system.GetUpgradeResult;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.bean.system.navibar.NaviBarModel;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.reactnative.bean.ReactNativeDownloadUrlBean;
import com.inspur.reactnative.bean.ReactNativeInstallUriBean;
import com.inspur.reactnative.bean.ReactNativeUpdateBean;

import java.util.List;

public interface APIInterface {




    void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult, boolean isManualCheck);

    void returnUpgradeFail(String error, boolean isManualCheck, int errorCode);


    void returnGetDownloadReactNativeUrlSuccess(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean);

    void returnGetDownloadReactNativeUrlFail(String error, int errorCode);

    void returnGetReactNativeInstallUrlSuccess(ReactNativeInstallUriBean reactNativeInstallUriBean);

    void returnGetReactNativeInstallUrlFail(String error, int errorCode);


    void returnChannelListSuccess(GetChannelListResult getSessionListResult);

    void returnChannelListFail(String error, int errorCode);

    void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult);

    void returnNewMessagesSuccess(GetChannelMessagesResult getChannelMessagesResult);

    void returnNewMsgsFail(String error, int errorCode);

    void returnNewMessagesFail(String error, int errorCode);

    void returnMsgCommentSuccess(GetMsgCommentResult getMsgCommentResult, String mid);

    void returnMsgCommentFail(String error, int errorCode);

    void returnMsgCommentCountSuccess(GetMsgCommentCountResult getMsgCommentCountResult, String mid);

    void returnMsgCommentCountFail(String error, int errorCode);

    void returnWebSocketUrlSuccess(GetWebSocketUrlResult getWebSocketResult);

    void returnWebSocketUrlFail(String error, int errorCode);

    void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult, String fakeMessageId);

    void returnSendMsgFail(String error, String fakeMessageId, int errorCode);

    void returnUploadResImgSuccess(GetNewsImgResult getNewsImgResult, String fakeMessageId);

    void returnUploadResImgFail(String error, int errorCode, String fakeMessageId);

    void returnMsgSuccess(GetMsgResult getMsgResult);

    void returnMsgFail(String error, int errorCode);

    void returnBookingRoomSuccess();

    void returnBookingRoomFail(String error, int errorCode);

    void returnChannelInfoSuccess(ChannelGroup channelGroup);

    void returnChannelInfoFail(String error, int errorCode);

    void returnUpLoadResFileSuccess(GetFileUploadResult getFileUploadResult, String fakeMessageId);

    void returnUpLoadResFileFail(String error, int errorCode, String fakeMessageId);

    void returnSearchChannelGroupSuccess(GetSearchChannelGroupResult getSearchChannelGroupResult);

    void returnSearchChannelGroupFail(String error, int errorCode);



    void returnCreateSingleChannelSuccess(GetCreateSingleChannelResult getCreatSingleChannelResult);

    void returnCreateSingleChannelFail(String error, int errorCode);

    void returnCreateChannelGroupSuccess(ChannelGroup channelGroup);

    void returnCreateChannelGroupFail(String error, int errorCode);

    void returnTripSuccess(Trip trip);

    void returnTripFail(String error, int errorCode);

    void returnLastUploadTripSuccess(Trip trip);

    void returnLastUploadTripFail(String error, int errorCode);

    void returnUpdateChannelGroupNameSuccess(GetBoolenResult getBoolenResult);

    void returnUpdateChannelGroupNameFail(String error, int errorCode);

    void returnUploadTrainTicketSuccess();

    void returnUploadTrainTicketFail(String error, int errorCode);

    void returnAddMembersSuccess(ChannelGroup channelGroup);

    void returnAddMembersFail(String error, int errorCode);

    void returnDndSuccess();

    void returnDndFail(String error, int errorCode);



    void returnTripArriveSuccess(GetTripArriveCity getTripArriveCity);

    void returnTripArriveFail(String error, int errorCode);



    void returnDelMembersSuccess(ChannelGroup channelGroup);

    void returnDelMembersFail(String error, int errorCode);


    void returnKnowledgeListSuccess(GetKnowledgeInfo getKnowledgeInfo);

    void returnKnowledgeListFail(String error, int errorCode);

    void returnFindSearchFail(String error, int errorCode);

    void returnFindMixSearchFail(String error, int errorCode);

    void returnAllRobotsSuccess(GetAllRobotsResult getAllRobotsResult);

    void returnAllRobotsFail(String error, int errorCode);

    void returnRobotByIdSuccess(Robot robot);

    void returnRobotByIdFail(String error, int errorCode);

    void returnReactNativeUpdateSuccess(ReactNativeUpdateBean reactNativeUpdateBean);

    void returnReactNativeUpdateFail(String error, int errorCode);

    void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult);

    void returnGetClientIdResultFail(String error, int errorCode);

    void returnVeriryApprovalPasswordSuccess(String password);

    void returnVeriryApprovalPasswordFail(String error, int errorCode);

    void returnNewsInstructionSuccess(GetNewsInstructionResult getNewsInstructionResult);

    void returnNewsInstructionFail(String error, int errorCode);

    void returnAppTabAutoSuccess(GetAppMainTabResult getAppMainTabResult, String mainTabSaveConfigVersion);

    void returnAppTabAutoFail(String error, int errorCode);

    void returnSplashPageInfoSuccess(SplashPageBean splashPageBean);

    void returnSplashPageInfoFail(String error, int errorCode);


    void returnAppConfigSuccess(GetAppConfigResult getAppConfigResult);

    void returnAppConfigFail(String error, int errorCode);

    void returnSaveWebAutoRotateConfigSuccess(boolean isWebAutoRotate);

    void returnSaveWebAutoRotateConfigFail(String error, int errorCode);

    void returnUploadPositionSuccess();

    void returnSaveConfigSuccess();

    void returnSaveConfigFail();

    void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult);

    void returnVolumeListFail(String error, int errorCode);

    void returnVolumeFileListSuccess(GetVolumeFileListResult getVolumeFileListResult);

    void returnVolumeFileListFail(String error, int errorCode);

    void returnVolumeFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult,
                                            String fileLocalPath, VolumeFile mockVolumeFile, int transferObserverId);

    void returnVolumeFileUploadTokenFail(VolumeFile mockVolumeFile, String error, int errorCode, String filePath);

    void returnChatFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, ChatFileUploadInfo chatFileUploadInfo);

    void returnChatFileUploadTokenFail(String error, int errorCode, ChatFileUploadInfo chatFileUploadInfo);

    void returnCreateForderSuccess(VolumeFile volumeFile);

    void returnCreateForderFail(String error, int errorCode);

    void returnVolumeFileDeleteSuccess(List<VolumeFile> deleteVolumeFileList);

    void returnVolumeFileDeleteFail(String error, int errorCode);

    void returnVolumeFileRenameSuccess(VolumeFile oldVolumeFile, String fileNewName);

    void returnVolumeFileRenameFail(String error, int errorCode);

    void returnMoveFileSuccess(List<VolumeFile> movedVolumeFileList);

    void returnMoveFileFail(String error, int errorCode);

    void returnCopyFileSuccess();

    void returnCopyFileFail(String error, int errorCode);

    void returnCopyFileBetweenVolumeSuccess();

    void returnCopyFileBetweenVolumeFail(String error, int errorCode, VolumeFile volumeFile);

    void returnMoveOrCopyFileBetweenVolumeSuccess(String operation);

    void returnMoveOrCopyFileBetweenVolumeFail(GetReturnMoveOrCopyErrorResult errorResult, int errorCode, String srcVolumeFilePath, String operation, List<VolumeFile> volumeFileList);


    void returnCreateShareVolumeSuccess(Volume volume);

    void returnCreateShareVolumeFail(String error, int errorCode);

    void returnUpdateShareVolumeNameSuccess(Volume volume, String name);

    void returnUpdateShareVolumeNameFail(String error, int errorCode);

    void returnRemoveShareVolumeSuccess(Volume volume);

    void returnRemoveShareVolumeFail(String error, int errorCode);

    void returnVolumeDetailSuccess(VolumeDetail volumeDetail);

    void returnVolumeDetailFail(String error, int errorCode);

    void returnVolumeMemAddSuccess(List<String> uidList);

    void returnVolumeMemAddFail(String error, int errorCode);

    void returnVolumeMemDelSuccess(List<String> uidList);

    void returnVolumeMemDelFail(String error, int errorCode);

    void returnUpdateGroupNameSuccess(String name);

    void returnUpdateGroupNameFail(String error, int errorCode);

    void returnGroupMemAddSuccess(List<String> uidList);

    void returnGroupMemAddFail(String error, int errorCode);

    void returnGroupMemDelSuccess(List<String> uidList);

    void returnGroupMemDelFail(String error, int errorCode);

    void returnUploadPushInfoResultSuccess(GetUploadPushInfoResult getUploadPushInfoResult);

    void returnUploadPushInfoResultFail(String error, int errorCode);

    void returnVolumeGroupContainMeSuccess(GetVolumeGroupResult getVolumeGroupResult);

    void returnVolumeGroupContainMeFail(String error, int errorCode);

    void returnVolumeGroupSuccess(GetVolumeResultWithPermissionResult getVolumeResultWithPermissionResult);

    void returnVolumeGroupFail(String error, int errorCode);

    void returnUpdateVolumeGroupPermissionSuccess(GetVolumeGroupPermissionResult getVolumeGroupPermissionResult);

    void returnUpdateVolumeGroupPermissionFail(String error, int errorCode);

    void returnOpenActionBackgroudUrlSuccess();

    void returnOpenActionBackgroudUrlFail(String error, int errorCode);

    void returnOpenDecideBotRequestSuccess();

    void returnOpenDecideBotRequestFail(String error, int errorCode);



    void returnContactUserListSuccess(byte[] bytes, String saveConfigVersion);

    void returnContactUserListFail(String error, int errorCode);

    void returnContactOrgListSuccess(byte[] bytes, String saveConfigVersion);

    void returnContactOrgListFail(String error, int errorCode);

    void returnContactUserListUpdateSuccess(GetContactUserListUpateResult getContactUserListUpateResult,
                                            String saveConfigVersion);

    void returnContactUserListUpdateFail(String error, int errorCode);

    void returnContactOrgListUpdateSuccess(GetContactOrgListUpateResult getContactOrgListUpateResult,
                                           String saveConfigVersion);

    void returnContactOrgListUpdateFail(String error, int errorCode);



    void returnGetVoiceCommunicationResultSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult);

    void returnGetVoiceCommunicationResultFail(String error, int errorCode);

    void returnGetVoiceCommunicationChannelInfoSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult);

    void returnGetVoiceCommunicationChannelInfoFail(String error, int errorCode);

    void returnJoinVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult);

    void returnJoinVoiceCommunicationChannelFail(String error, int errorCode);

    void returnRefuseVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult);

    void returnRefuseVoiceCommunicationChannelFail(String error, int errorCode);

    void returnLeaveVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult);

    void returnLeaveVoiceCommunicationChannelFail(String error, int errorCode);

    void returnQuitChannelGroupSuccess();

    void returnQuitChannelGroupSuccessFail(String error, int errorCode);

    void returnConversationListSuccess(GetConversationListResult getConversationListResult);

    void returnConversationListFail(String error, int errorCode);

    void returnSetConversationStickSuccess(String id, boolean isStick);

    void returnSetConversationStickFail(String error, int errorCode);

    void returnSetConversationHideSuccess(String id, boolean isHide);

    void returnSetConversationHideFail(String error, int errorCode);

    void returnAddConversationGroupMemberSuccess(List<String> uidList);

    void returnAddConversationGroupMemberFail(String error, int errorCode);

    void returnDelConversationGroupMemberSuccess(List<String> uidList);

    void returnDelConversationGroupMemberFail(String error, int errorCode);


    void returnConversationInfoSuccess(Conversation conversation);

    void returnConversationInfoFail(String error, int errorCode);

    void returnUpdateConversationNameSuccess();

    void returnUpdateConversationNameFail(String error, int errorCode);



    void returnCreateDirectConversationSuccess(Conversation conversation);

    void returnCreateDirectConversationFail(String error, int errorCode);

    void returnCreateGroupConversationSuccess(Conversation conversation);

    void returnCreateGroupConversationFail(String error, int errorCode);



    void returnDeleteConversationSuccess(String cid);

    void returnDeleteConversationFail(String error, int errorCode);

    void returnMailFolderSuccess(GetMailFolderResult getMailForderResult);

    void returnMailFolderFail(String error, int errorCode);

    void returnMailListSuccess(String folderId, int pageSize, int offset, GetMailListResult getMailListResult);

    void returnMailListFail(String folderId, int pageSize, int offset, String error, int errorCode);

    void returnMailDetailSuccess(GetMailDetailResult getMailDetailResult);

    void returnMailDetailSuccess(byte[] arg0);

    void returnMailDetailFail(String error, int errorCode);

    void returnMailLoginSuccess();

    void returnMailLoginFail(String error, int errorCode);

    void returnMailCertificateUploadSuccess(byte[] arg0);

    void returnMailCertificateUploadFail(String error, int errorCode);

    void returnSendMailSuccess();

    void returnSendMailFail(String error, int errorCode);

    void returnRemoveMailSuccess();

    void returnRemoveMailFail(String error, int errorCode);

    void returnNaviBarModelSuccess(NaviBarModel naviBarModel);

    void returnNaviBarModelFail(String error, int errorCode);

    void returnTransmitPictureSuccess(String cid, String description, Message message);

    void returnTransmitPictureError(String error, int errorCode);

    void returnShareFileToFriendsFromVolumeSuccess(String newPath, VolumeFile volumeFile);

    void returnShareFileToFriendsFromVolumeFail(String error, int errorCode);

    void returnCallbackAfterFileUploadSuccess(VolumeFile volumeFile);

    void returnCallbackAfterFileUploadFail(String error, int errorCode);

    void returnInvitationContentSuccess(ScanCodeJoinConversationBean scanCodeJoinConversationBean);

    void returnInvitationContentFail(String error, int errorCode);

    void returnJoinConversationSuccess(Conversation conversation);

    void returnJoinConversationFail(String error, int errorCode);
}
