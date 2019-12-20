package com.inspur.emmcloud.schedule.util;

import android.app.Activity;
import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.OnCreateGroupConversationListener;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.api.ScheduleAPIInterfaceImpl;
import com.inspur.emmcloud.schedule.api.ScheduleAPIService;
import com.inspur.emmcloud.schedule.bean.Participant;
import com.inspur.emmcloud.schedule.bean.Schedule;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by libaochao on 2019/12/18.
 */

public class ScheduleGroupCreateUtils {
    public static final String EXTRA_CONVERSATION = "conversation";
    JSONArray peopleArray;
    ICreateGroupChatListener iCreateGroupChatListener;
    Schedule schedule;
    Context context;
    private LoadingDialog loadingDlg;

    /**
     * 发起群聊  入口
     *
     * @param meeting     会议对象
     * @param chatGroupId CID 群聊ID
     * @param listener    成功失败回调 可以传null
     */
    public void startGroupChat(Activity context, Schedule meeting, String chatGroupId, ICreateGroupChatListener listener) {
        this.context = context;
        this.iCreateGroupChatListener = listener;

        ScheduleAPIService scheduleAPIService = new ScheduleAPIService(context);
        scheduleAPIService.setAPIInterface(new WebService());
        this.schedule = meeting;
        if (meeting == null) return;
        peopleArray = getPeopleArray(meeting);
        if (peopleArray.length() < 2) {
            ToastUtils.show(R.string.chat_group_least_two_person);
            return;
        }

        if (StringUtils.isBlank(chatGroupId)) {
            loadingDlg = new LoadingDialog(context);
            loadingDlg.show();
            scheduleAPIService.getCalendarBindChat(meeting.getId());
        } else {
            Router router = Router.getInstance();
            if (router.getService(AppService.class) != null) {
                AppService service = router.getService(AppService.class);
                if (service.isTabExist(Constant.APP_TAB_BAR_COMMUNACATE)) {
                    if (router.getService(CommunicationService.class) != null) {
                        CommunicationService communicationService = router.getService(CommunicationService.class);
                        communicationService.openConversationByChannelId(chatGroupId);
                    }
                }
            }
        }
    }

    private JSONArray getPeopleArray(Schedule meeting) {
        List<Participant> totalList = deleteRepeatData(meeting.getAllParticipantList(), meeting.getOwner());
        JSONArray peopleArray = new JSONArray();
        for (Participant participant : totalList) {
            JSONObject json = new JSONObject();
            try {
//                if (!participant.getId().equals(BaseApplication.getInstance().getUid())) {
                json.put("pid", participant.getId());
                json.put("name", participant.getName());
                peopleArray.put(json);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return peopleArray;
    }

    //list去重
    private List<Participant> deleteRepeatData(List<Participant> list, String owner) {
        //把创建人加入到群聊
        if (!StringUtils.isBlank(owner)) {
            Participant ownerParticipant = new Participant();
            ownerParticipant.setId(owner);

            ContactService contactService = Router.getInstance().getService(ContactService.class);
            ContactUser contactUser = null;
            if (contactService != null) {
                contactUser = contactService.getContactUserByUid(owner);
            }
            ownerParticipant.setName(contactUser != null ? contactUser.getName() : "");
            list.add(ownerParticipant);
        }
        //去除通讯录中不存在的人(外部联系人)
        Iterator<Participant> iterator = list.iterator();
        while (iterator.hasNext()) {
            Participant item = iterator.next();
            ContactUser user = null;
            ContactService contactService = Router.getInstance().getService(ContactService.class);
            if (contactService != null) {
                user = contactService.getContactUserByUid(item.getId());
            }
            if (user == null) {
                iterator.remove();
            }
        }
        Set<Participant> set = new TreeSet<>(new Comparator<Participant>() {
            @Override
            public int compare(Participant o1, Participant o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        set.addAll(list);
        List<Participant> result = new ArrayList<>(set);
        Collections.reverse(result);

        return result;
    }

    public interface ICreateGroupChatListener {
        void createSuccess();

        void createFail();
    }

    class WebService extends ScheduleAPIInterfaceImpl {
        @Override
        public void returnSetCalendarChatBindSuccess(String calendarId, String chatId) {
            if (loadingDlg != null) {
                loadingDlg.dismiss();
            }
            Router router = Router.getInstance();
            if (router.getService(AppService.class) != null) {
                AppService service = router.getService(AppService.class);
                if (service.isTabExist(Constant.APP_TAB_BAR_COMMUNACATE)) {
                    CommunicationService communicationService = router.getService(CommunicationService.class);
                    communicationService.openConversationByChannelId(chatId);
                }
            }
        }

        @Override
        public void returnSetCalendarChatBindFail(String error, int errorCode) {
            if (iCreateGroupChatListener != null) {
                iCreateGroupChatListener.createFail();  //创建失败回调
            }
            if (loadingDlg != null) {
                loadingDlg.dismiss();
            }

        }

        @Override
        public void returnGetCalendarChatBindSuccess(final String calendarId, String cid) {
            if (StringUtils.isBlank(cid)) { //新建群聊
                String groupName = null;
                if (schedule != null && !StringUtils.isBlank(schedule.getTitle())) {
                    groupName = schedule.getTitle();
                }

                Router router = Router.getInstance();
                CommunicationService communicationService = router.getService(CommunicationService.class);
                if (communicationService != null) {
                    communicationService.createGroupConversation(context, peopleArray, groupName, new OnCreateGroupConversationListener() {
                        @Override
                        public void createGroupConversationSuccess(Conversation conversation) {
                            if (loadingDlg != null) {
                                loadingDlg.dismiss();
                            }
                            if (iCreateGroupChatListener != null) {
                                iCreateGroupChatListener.createSuccess();  //创建成功回调
                            }

                            ScheduleAPIService scheduleChatService = new ScheduleAPIService(context);
                            scheduleChatService.setAPIInterface(new WebService());
                            scheduleChatService.setCalendarBindChat(calendarId, conversation.getId());

                            Router router = Router.getInstance();
                            if (router.getService(AppService.class) != null) {
                                AppService service = router.getService(AppService.class);
                                if (service.isTabExist(Constant.APP_TAB_BAR_COMMUNACATE)) {
                                    CommunicationService communicationService = router.getService(CommunicationService.class);
                                    if (communicationService != null) {
                                        communicationService.openConversationByChannelId(conversation.getId());
                                        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CHAT_CHANGE, conversation));
                                    }
                                }
                            }
                        }

                        @Override
                        public void createGroupConversationFail() {
                            if (iCreateGroupChatListener != null) {
                                iCreateGroupChatListener.createFail();  //创建失败回调
                            }
                            if (loadingDlg != null) {
                                loadingDlg.dismiss();
                            }
                        }
                    });
                }
            } else {
                if (loadingDlg != null) {
                    loadingDlg.dismiss();
                }
                Router router = Router.getInstance();
                if (router.getService(AppService.class) != null) {
                    AppService service = router.getService(AppService.class);
                    if (service.isTabExist(Constant.APP_TAB_BAR_COMMUNACATE)) {
                        if (router.getService(CommunicationService.class) != null) {
                            CommunicationService communicationService = router.getService(CommunicationService.class);
                            communicationService.openConversationByChannelId(cid);
                        }
                    }
                }
            }
        }

        @Override
        public void returnGetCalendarChatBindFail(String error, int errorCode) {
            if (iCreateGroupChatListener != null) {
                iCreateGroupChatListener.createFail();  //创建失败回调
            }
            if (loadingDlg != null) {
                loadingDlg.dismiss();
            }

        }
    }
}
