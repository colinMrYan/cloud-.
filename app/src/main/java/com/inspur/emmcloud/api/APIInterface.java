package com.inspur.emmcloud.api;

import com.inspur.emmcloud.basemodule.bean.AppException;
import com.inspur.emmcloud.basemodule.bean.GetUploadPushInfoResult;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.GetAddAppResult;
import com.inspur.emmcloud.bean.appcenter.GetAllAppResult;
import com.inspur.emmcloud.bean.appcenter.GetAppGroupResult;
import com.inspur.emmcloud.bean.appcenter.GetClientIdRsult;
import com.inspur.emmcloud.bean.appcenter.GetIDResult;
import com.inspur.emmcloud.bean.appcenter.GetMyAppResult;
import com.inspur.emmcloud.bean.appcenter.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.bean.appcenter.GetRemoveAppResult;
import com.inspur.emmcloud.bean.appcenter.GetSearchAppResult;
import com.inspur.emmcloud.bean.appcenter.GetWebAppRealUrlResult;
import com.inspur.emmcloud.bean.appcenter.ReactNativeDownloadUrlBean;
import com.inspur.emmcloud.bean.appcenter.ReactNativeInstallUriBean;
import com.inspur.emmcloud.bean.appcenter.ReactNativeUpdateBean;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailDetailResult;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailFolderResult;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailListResult;
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
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.GetWebSocketUrlResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.contact.GetContactOrgListUpateResult;
import com.inspur.emmcloud.bean.contact.GetContactUserListUpateResult;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.find.GetKnowledgeInfo;
import com.inspur.emmcloud.bean.find.GetTripArriveCity;
import com.inspur.emmcloud.bean.find.Trip;
import com.inspur.emmcloud.bean.mine.GetBindingDeviceResult;
import com.inspur.emmcloud.bean.mine.GetCardPackageResult;
import com.inspur.emmcloud.bean.mine.GetDeviceLogResult;
import com.inspur.emmcloud.bean.mine.GetExperienceUpgradeFlagResult;
import com.inspur.emmcloud.bean.mine.GetFaceSettingResult;
import com.inspur.emmcloud.bean.mine.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.mine.GetUserCardMenusResult;
import com.inspur.emmcloud.bean.mine.GetUserHeadUploadResult;
import com.inspur.emmcloud.bean.mine.UserProfileInfoBean;
import com.inspur.emmcloud.bean.schedule.GetScheduleListResult;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.calendar.GetHolidayDataResult;
import com.inspur.emmcloud.bean.schedule.calendar.GetMyCalendarResult;
import com.inspur.emmcloud.bean.schedule.calendar.GetScheduleBasicDataResult;
import com.inspur.emmcloud.bean.schedule.meeting.Building;
import com.inspur.emmcloud.bean.schedule.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetLocationResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetMeetingListResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetMeetingRoomListResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetOfficeListResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetTagResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.Office;
import com.inspur.emmcloud.bean.schedule.task.Attachment;
import com.inspur.emmcloud.bean.schedule.task.GetTaskAddResult;
import com.inspur.emmcloud.bean.schedule.task.GetTaskListResult;
import com.inspur.emmcloud.bean.schedule.task.Task;
import com.inspur.emmcloud.bean.system.GetAppConfigResult;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.bean.system.GetUpgradeResult;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.bean.system.badge.BadgeBodyModel;
import com.inspur.emmcloud.bean.system.navibar.NaviBarModel;

import java.util.Calendar;
import java.util.List;

public interface APIInterface {

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

    void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult, boolean isManualCheck);

    void returnUpgradeFail(String error, boolean isManualCheck, int errorCode);

    void returnSearchAppSuccess(GetSearchAppResult getAllAppResult);

    void returnSearchAppFail(String error, int errorCode);

    void returnSearchAppMoreSuccess(GetSearchAppResult getAllAppResult);

    void returnSearchAppMoreFail(String error, int errorCode);





    void returnUploadMyHeadSuccess(GetUploadMyHeadResult getUploadMyInfoResult, String filePath);

    void returnUploadMyHeadFail(String error, int errorCode);

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

    void returnModifyUserInfoSucces(GetBoolenResult getBoolenResult);

    void returnModifyUserInfoFail(String error, int errorCode);

    void returnWebSocketUrlSuccess(GetWebSocketUrlResult getWebSocketResult);

    void returnWebSocketUrlFail(String error, int errorCode);

    void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult, String fakeMessageId);

    void returnSendMsgFail(String error, String fakeMessageId, int errorCode);

    void returnUploadResImgSuccess(GetNewsImgResult getNewsImgResult, String fakeMessageId);

    void returnUploadResImgFail(String error, int errorCode, String fakeMessageId);


    void returnMeetingRoomListSuccess(GetMeetingRoomListResult getMeetingRoomsResult);

    void returnMeetingRoomListFail(String error, int errorCode);

    void returnMeetingRoomListSuccess(GetMeetingRoomListResult getMeetingRoomsResult, boolean isFilte);

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

    void returnUserHeadUploadSuccess(GetUserHeadUploadResult getUserHeadUploadResult);

    void returnUserHeadUploadFail(String error, int errorCode);

    void returnCreateSingleChannelSuccess(GetCreateSingleChannelResult getCreatSingleChannelResult);

    void returnCreatSingleChannelFail(String error, int errorCode);

    void returnCreatChannelGroupSuccess(ChannelGroup channelGroup);

    void returnCreateChannelGroupFail(String error, int errorCode);

    void returnTripSuccess(Trip trip);

    void returnTripFail(String error, int errorCode);

    void returnLastUploadTripSuccess(Trip trip);

    void returnLastUploadTripFail(String error, int errorCode);

    void returnUpdateChannelGroupNameSuccess(GetBoolenResult getBoolenResult);

    void returnUpdateChannelGroupNameFail(String error, int errorCode);

    void returnUploadTrainTicketSuccess();

    void returnUploadTrainTicketFail(String error, int errorCode);

    void returnUploadExceptionSuccess();

    void returnUploadExceptionSuccess(final List<AppException> appExceptionList);

    void returnUploadExceptionFail(String error, int errorCode);

    void returnLocationResultSuccess(GetLocationResult getLoctionResult);

    void returnLocationResultFail(String error, int errorCode);

    void returnOfficeListResultSuccess(GetOfficeListResult getOfficeListResult);

    void returnOfficeListResultFail(String error, int errorCode);

    void returnAddMeetingOfficeSuccess(Office office, Building building);

    void returnAddMeetingOfficeFail(String error, int errorCode);

    void returnAddMembersSuccess(ChannelGroup channelGroup);

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

    void returnUpdateTaskSuccess(int position);

    void returnUpdateTaskFail(String error, int errorCode, int position);

    void returnAttachmentSuccess(Task taskResult);

    void returnAttachmentFail(String error, int errorCode);

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



    void returnTripArriveSuccess(GetTripArriveCity getTripArriveCity);

    void returnTripArriveFail(String error, int errorCode);

    void returnDeleteMeetingSuccess(Meeting meeting);

    void returnDeleteMeetingFail(String error, int errorCode);

    void returnDelMembersSuccess(ChannelGroup channelGroup);

    void returnDelMembersFail(String error, int errorCode);

    void returnDelAttachmentSuccess(int position);

    void returnDelAttachmentFail(String error, int errorCode, int position);

    void returnChangeMessionOwnerSuccess(String managerName);

    void returnChangeMessionOwnerFail(String error, int errorCode);

    void returnChangeMessionTagSuccess();

    void returnChangeMessionTagFail(String error, int errorCode);

    void returnDelTaskTagSuccess();

    void returnDelTaskTagFail(String error, int errorCode);

    void returnAddTaskTagSuccess();

    void returnAddTaskTagFail(String error, int errorCode);

    void returnDeleteOfficeSuccess(Office office);

    void returnDeleteOfficeFail(String error, int errorCode);

    void returnSetMeetingCommonBuildingSuccess(Building building);

    void returnSetMeetingCommonBuildingFail(String error, int errorCode);

    void returnCancelMeetingCommonBuildingSuccess(Building building);

    void returnCancelMeetingCommonBuildingFail(String error, int errorCode);

    void returnKnowledgeListSuccess(GetKnowledgeInfo getKnowledgeInfo);

    void returnKnowledgeListFail(String error, int errorCode);

    void returnIsMeetingAdminSuccess(GetIsMeetingAdminResult getIsAdmin);

    void returnIsMeetingAdminFail(String error, int errorCode);

    void returnScheduleBasicDataSuccess(GetScheduleBasicDataResult getScheduleBasicDataResult);

    void returnScheduleBasicDataFail(String error, int errorCode);



    void returnFindSearchFail(String error, int errorCode);

    void returnFindMixSearchFail(String error, int errorCode);

    void returnAllRobotsSuccess(GetAllRobotsResult getAllRobotsResult);

    void returnAllRobotsFail(String error, int errorCode);

    void returnRobotByIdSuccess(Robot robot);

    void returnRobotByIdFail(String error, int errorCode);



    void returnGetAppTabsSuccess(GetAppMainTabResult getAppTabsResult);

    void returnGetAppTabsFail(String error, int errorCode);

    void returnUserAppsSuccess(GetAppGroupResult getAppGroupResult, String clientConfigMyAppVersion);

    void returnUserAppsFail(String error, int errorCode);

    void returnReactNativeUpdateSuccess(ReactNativeUpdateBean reactNativeUpdateBean);

    void returnReactNativeUpdateFail(String error, int errorCode);

    void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult);

    void returnGetClientIdResultFail(String error, int errorCode);

    void returnGetDownloadReactNativeUrlSuccess(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean);

    void returnGetDownloadReactNativeUrlFail(String error, int errorCode);

    void returnGetReactNativeInstallUrlSuccess(ReactNativeInstallUriBean reactNativeInstallUriBean);

    void returnGetReactNativeInstallUrlFail(String error, int errorCode);

    void returnVeriryApprovalPasswordSuccess(String password);

    void returnVeriryApprovalPasswordFail(String error, int errorCode);

    void returnNewsInstructionSuccess(GetNewsInstructionResult getNewsInstructionResult);

    void returnNewsInstructionFail(String error, int errorCode);

    void returnAppTabAutoSuccess(GetAppMainTabResult getAppMainTabResult, String mainTabSaveConfigVersion);

    void returnAppTabAutoFail(String error, int errorCode);

    void returnUserProfileConfigSuccess(UserProfileInfoBean userProfileInfoBean);

    void returnUserProfileConfigFail(String error, int errorCode);

    void returnBindingDeviceListSuccess(GetBindingDeviceResult getBindingDeviceResult);

    void returnBindingDeviceListFail(String error, int errorCode);

    void returnUnBindDeviceSuccess();

    void returnUnBindDeviceFail(String error, int errorCode);



    void returnSplashPageInfoSuccess(SplashPageBean splashPageBean);

    void returnSplashPageInfoFail(String error, int errorCode);




    void returnDeviceLogListSuccess(GetDeviceLogResult getDeviceLogResult);

    void returnDeviceLogListFail(String error, int errorCode);

    void returnAppInfoSuccess(App app);

    void returnAppInfoFail(String error, int errorCode);

    void returnAppConfigSuccess(GetAppConfigResult getAppConfigResult);

    void returnAppConfigFail(String error, int errorCode);

    void returnSaveWebAutoRotateConfigSuccess(boolean isWebAutoRotate);

    void returnSaveWebAutoRotateConfigFail(String error, int errorCode);

    void returnUploadPositionSuccess();

    void returnWebAppRealUrlSuccess(GetWebAppRealUrlResult getWebAppRealUrlResult);

    void returnWebAppRealUrlFail();

    void returnSaveConfigSuccess();

    void returnSaveConfigFail();

    void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult);

    void returnVolumeListFail(String error, int errorCode);

    void returnVolumeFileListSuccess(GetVolumeFileListResult getVolumeFileListResult);

    void returnVolumeFileListFail(String error, int errorCode);

    void returnVolumeFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult,
                                            String fileLocalPath, VolumeFile mockVolumeFile);

    void returnVolumeFileUploadTokenFail(VolumeFile mockVolumeFile, String error, int errorCode, String filePath);

    void returnChatFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult);

    void returnChatFileUploadTokenFail(String error, int errorCode);

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

    void returnRecommendAppWidgetListSuccess(GetRecommendAppWidgetListResult getRecommendAppWidgetListResult);

    void returnRecommendAppWidgetListFail(String error, int errorCode);

    void returnCreateShareVolumeSuccess(Volume volume);

    void returnCreateShareVolumeFail(String error, int errorCode);

    void returnUpdateShareVolumeNameSuccess(Volume volume, String name);

    void returnUpdateShareVolumeNameFail(String error, int errorCode);

    void retrunRemoveShareVolumeSuccess(Volume volume);

    void returnRemoveShareVolumeFail(String error, int errorCode);

    void returnFaceSettingSuccess(GetFaceSettingResult getFaceSettingResult);

    void returnFaceSettingFail(String error, int errorCode);

    void returnFaceVerifySuccess(GetFaceSettingResult getFaceSettingResult);

    void returnFaceVerifyFail(String error, int errorCode);

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

    void returnCardPackageListSuccess(GetCardPackageResult getCardPackageResult);

    void returnCardPackageListFail(String error, int errorCode);

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

    void returnWebexMeetingListSuccess(GetWebexMeetingListResult getWebexMeetingListResult);

    void returnWebexMeetingListFail(String error, int errorCode);

    void returnScheduleWebexMeetingSuccess(GetScheduleWebexMeetingSuccess getScheduleWebexMeetingSuccess);

    void returnScheduleWebexMeetingFail(String error, int errorCode);

    void returnWebexMeetingSuccess(WebexMeeting webexMeeting);

    void returnWebexMeetingFail(String error, int errorCode);

    void returnWebexTKSuccess(GetWebexTKResult getWebexTKResult);

    void returnWebexTKFail(String error, int errorCode);

    void returnRemoveWebexMeetingSuccess();

    void returnRemoveWebexMeetingFail(String error, int errorCode);

    void returnConversationInfoSuccess(Conversation conversation);

    void returnConversationInfoFail(String error, int errorCode);

    void returnUpdateConversationNameSuccess();

    void returnUpdateConversationNameFail(String error, int errorCode);

    void returnExperienceUpgradeFlagSuccess(GetExperienceUpgradeFlagResult getExperienceUpgradeFlagResult);

    void returnExperienceUpgradeFlagFail(String error, int errorCode);

    void returnUpdateExperienceUpgradeFlagSuccess();

    void returnUpdateExperienceUpgradeFlagFail(String error, int errorCode);

    void returnCreateDirectConversationSuccess(Conversation conversation);

    void returnCreateDirectConversationFail(String error, int errorCode);

    void returnCreateGroupConversationSuccess(Conversation conversation);

    void returnCreateGroupConversationFail(String error, int errorCode);

    void returnBadgeCountSuccess(BadgeBodyModel badgeBodyModel);

    void returnBadgeCountFail(String error, int errorCode);

    void returnDeleteConversationSuccess(String cid);

    void returnDeleteConversationFail(String error, int errorCode);

    void returnCheckCloudPluseConnectionSuccess(byte[] arg0, String url);

    void returnCheckCloudPluseConnectionError(String error, int responseCode, String url);

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

    void returnUserCardMenusSuccess(GetUserCardMenusResult getUserCardMenusResult);

    void returnUserCardMenusFail(String error, int errorCode);

    void returnScheduleListSuccess(GetScheduleListResult getScheduleListResult, Calendar startCalendar, Calendar endCalendar, List<String> calendarIdList, List<String> meetingIdList, List<String> taskIdList);

    void returnScheduleListFail(String error, int errorCode);

    void returnAddScheduleSuccess(GetIDResult getIDResult);

    void returnAddScheduleFail(String error, int errorCode);

    void returnUpdateScheduleSuccess();

    void returnUpdateScheduleFail(String error, int errorCode);

    void returnDeleteScheduleSuccess(String scheduleId);

    void returnDeleteScheduleFail(String error, int errorCode);

    void returnAddMeetingSuccess();

    void returnAddMeetingFail(String error, int errorCode);

    void returnDelMeetingSuccess(Meeting meeting);

    void returnDelMeetingFail(String error, int errorCode);

    //会议-通过id获取
    void returnMeetingDataFromIdSuccess(Meeting meeting);

    void returnMeetingDataFromIdFail(String error, int errorCode);

    //日程-通过id获取
    void returnScheduleDataFromIdSuccess(Schedule schedule);

    void returnScheduleDataFromIdFail(String error, int errorCode);

    void returnMeetingListSuccess(GetMeetingListResult getMeetingListByMeetingRoomResult);

    void returnMeetingListFail(String error, int errorCode);

    void returnMeetingListByMeetingRoomFail(String error, int errorCode);

    void returnNaviBarModelSuccess(NaviBarModel naviBarModel);

    void returnNaviBarModelFail(String error, int errorCode);


    void returnMeetingHistoryListSuccess(GetMeetingListResult getMeetingListByMeetingRoomResult);

    void returnMeetingHistoryListFail(String error, int errorCode);

    void returnUpdateMeetingSuccess();

    void returnUpdateMeetingFail(String error, int errorCode);

    void returnHolidayDataSuccess(GetHolidayDataResult getHolidayDataResult);

    void returnHolidayDataFail(String error, int errorCode);

    void returnTransmitPictureSuccess(String cid, String description, Message message);

    void returnTransmitPictureError(String error, int errorCode);

    void returnSetCalendarChatBindSuccess(String calendarId, String chatId);

    void returnSetCalendarChatBindFail(String error, int errorCode);

    void returnGetCalendarChatBindSuccess(String calendarId, String cid);

    void returnGetCalendarChatBindFail(String error, int errorCode);

}
