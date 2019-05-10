package com.inspur.emmcloud.ui.schedule.task;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.chat.GetFileUploadResult;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.schedule.RemindEvent;
import com.inspur.emmcloud.bean.schedule.task.Attachment;
import com.inspur.emmcloud.bean.schedule.task.GetTaskAddResult;
import com.inspur.emmcloud.bean.schedule.task.Task;
import com.inspur.emmcloud.bean.schedule.task.TaskColorTag;
import com.inspur.emmcloud.bean.schedule.task.TaskSubject;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.privates.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.DateTimePickerDialog;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.plugin.filetransfer.filemanager.FileManagerActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.carbs.android.segmentcontrolview.library.SegmentControlView;

/**
 * Created by libaochao on 2019/3/28.
 */
@ContentView(R.layout.activity_task_add)
public class TaskAddActivity extends BaseActivity {
    private static final int REQUEST_MANGER = 1;
    private static final int REQUEST_ALBUM = 2;
    private static final int REQUEST_PARTICIPANT = 3;
    private static final int REQUEST_ALERT_TIME = 4;
    private static final int REQUEST_ATTACHMENT = 5;
    public static final int REQUEST_CLASS_TAG = 6;
    @ViewInject(R.id.ll_single_tag)
    LinearLayout singleTagLayout;
    @ViewInject(R.id.ll_tags)
    LinearLayout tagsLayout;
    @ViewInject(R.id.et_input_title)
    private EditText contentInputEdit;
    @ViewInject(R.id.iv_task_type_tap)
    private ImageView taskTypeTapImage;
    @ViewInject(R.id.tv_task_type_name)
    private TextView taskTypeNameText;
    @ViewInject(R.id.tv_deadline_time)
    private TextView deadlineTimeText;
    @ViewInject(R.id.iv_participant_head_three)
    private ImageView participantHeadThreeImageView;
    @ViewInject(R.id.iv_participant_head_two)
    private ImageView participantHeadTwoImageView;
    @ViewInject(R.id.iv_participant_head_one)
    private ImageView participantHeadOneImageView;
    @ViewInject(R.id.tv_participant_num)
    private TextView participantNumText;
    @ViewInject(R.id.iv_manager_head)
    private ImageView managerHeadImageView;
    @ViewInject(R.id.tv_manager_num)
    private TextView managerNumText;
    @ViewInject(R.id.tv_end_task_alert_time)
    private TextView taskAlertTimeView;
    @ViewInject(R.id.lv_attachment_abstract_other)
    private ListView attachmentOthersList;
    @ViewInject(R.id.ll_more_content)
    private LinearLayout moreContentLayout;
    @ViewInject(R.id.v_priority)
    private SegmentControlView segmentControlView;
    @ViewInject(R.id.tv_title)
    private TextView titleText;


    private WorkAPIService apiService;
    private LoadingDialog loadingDlg;
    private Task taskResult = new Task();

    private List<JsonAttachmentAndUri> jsonAttachmentList = new ArrayList<>();
    private List<SearchModel> taskMangerList = new ArrayList<>();
    private List<SearchModel> taskParticipantList = new ArrayList<>();
    private List<SearchModel> orgTaskParticipantList = new ArrayList<>();
    private List<TaskColorTag> taskColorTagList = new ArrayList<>();
    private List<TaskColorTag> orgTaskColorTagList = new ArrayList<>();
    private Calendar deadLineCalendar;
    private AttachmentAdapter attachmentOtherAdapter;
    private String attachmentLocalPath = "";
    private Boolean isCreateTask = true;
    private RemindEvent remindEvent = new RemindEvent();
    private int taskType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
        EventBus.getDefault().register(this);
    }

    private void initData() {
        jsonAttachmentList = new ArrayList<>();
        taskMangerList = new ArrayList<>();
        taskParticipantList = new ArrayList<>();
        attachmentOtherAdapter = new AttachmentAdapter();
        loadingDlg = new LoadingDialog(this);
        apiService = new WorkAPIService(this);
        attachmentOthersList.setAdapter(attachmentOtherAdapter);
        apiService.setAPIInterface(new TaskAddActivity.WebService());

        //判断是否为新建任务
        if (getIntent().hasExtra("task")) {
            taskResult = (Task) getIntent().getSerializableExtra("task");
            taskType = getIntent().getIntExtra(TaskListFragment.TASK_CURRENT_INDEX, 0);
            deadLineCalendar = taskResult.getDueDate();
            taskColorTagList = taskResult.getTags();
            for (int i=0;i<taskColorTagList.size();i++){
                orgTaskColorTagList.add(taskColorTagList.get(i));
            }
            isCreateTask = false;
            //taskMangerList = ContactUserCache taskResult.getOwner();
            String masterUid = taskResult.getOwner();
            if (!StringUtils.isBlank(masterUid)) {
                ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(masterUid);
                if (contactUser != null) {
                    SearchModel searchModel = new SearchModel(contactUser);
                    taskMangerList.add(searchModel);
                }
            }
            List<Attachment> attachments = taskResult.getAttachments();
            for (int i = 0; i < attachments.size(); i++) {
                JsonAttachmentAndUri jsonAttachmentAndUri = new JsonAttachmentAndUri(JSONUtils.getJSONObject(JSONUtils.toJSONString(attachments.get(i))), "", false);
                jsonAttachmentList.add(jsonAttachmentAndUri);
            }
            getTasks();
            attachmentOthersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        File dir = new File(MyAppConfig.LOCAL_DOWNLOAD_PATH);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        String filename = taskResult.getAttachments().get(i).getName();
                        String fileUri = taskResult.getAttachments().get(i).getUri();
                        String target = MyAppConfig.LOCAL_DOWNLOAD_PATH + filename;
                        String downLoadSource = APIUri.getPreviewUrl(fileUri);
                        if (FileUtils.isFileExist(fileUri)) {
                        } else if ((FileUtils.isFileExist(target))) {
                            FileUtils.openFile(TaskAddActivity.this, target);
                        } else {
                            APIDownloadCallBack downLoadCallBack = new APIDownloadCallBack(TaskAddActivity.this, downLoadSource) {
                                @Override
                                public void callbackStart() {

                                }

                                @Override
                                public void callbackLoading(long total, long current,
                                                            boolean isUploading) {

                                }

                                @Override
                                public void callbackSuccess(File file) {
                                    attachmentOtherAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void callbackError(Throwable arg0, boolean arg1) {

                                }

                                @Override
                                public void callbackCanceled(CancelledException e) {

                                }
                            };
                            new DownLoaderUtils().startDownLoad(downLoadSource, target, downLoadCallBack);
                        }
                    }
                }
            });
        }
    }

    private void initView() {
        if (!isCreateTask) {
            contentInputEdit.setText(taskResult.getTitle());
            segmentControlView.setSelectedIndex(taskResult.getPriority());
            titleText.setText(getApplication().getString(R.string.schedule_task_detail));
            if (deadLineCalendar != null) {
                deadlineTimeText.setText(TimeUtils.calendar2FormatString(this, deadLineCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
            }
            setTaskColorTags();
            showManagerImage();
            setClickable(taskType);
        }
        attachmentOtherAdapter.notifyDataSetChanged();
    }


    private void setClickable(int taskType) {
        //大于1 设置为不可点击，否则不做处理
        if (taskType > 1) {
            titleText.setClickable(false);
            contentInputEdit.setEnabled(false);
            tagsLayout.setClickable(false);
            segmentControlView.setClickable(false);
            findViewById(R.id.rl_task_type).setClickable(false);
            findViewById(R.id.rl_task_manager).setClickable(false);
            findViewById(R.id.rl_task_participant).setClickable(false);
            findViewById(R.id.rl_deadline).setClickable(false);
            findViewById(R.id.rl_task_end_alert).setClickable(false);
            findViewById(R.id.rl_attachments_others).setClickable(false);
            findViewById(R.id.tv_save).setVisibility(View.GONE);

        }
    }


    /**
     * 获取任务
     */
    private void getTasks() {
        TaskSubject taskSubject = taskResult.getSubject();
        if (NetUtils.isNetworkConnected(TaskAddActivity.this)
                && taskSubject != null) {
            loadingDlg.show();
            apiService.getTask(taskSubject.getId());
        }
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.tv_save:
                if (!checkingAddOrUpdateTaskAvailable())
                    return;
                if (isCreateTask) {
                    createTask();
                } else {
                    updateTask();
                }
                break;
            case R.id.ibt_back:
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_TASK_DATA_CHANGED, ""));
                finish();
                break;
            case R.id.rl_task_type:
                intent.setClass(this, TaskTagsManageActivity.class);
                intent.putExtra(TaskTagsManageActivity.EXTRA_TAGS, (ArrayList<TaskColorTag>) taskColorTagList);
                startActivityForResult(intent, REQUEST_CLASS_TAG);
                break;
            case R.id.rl_task_manager:
                intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                intent.putExtra(ContactSearchFragment.EXTRA_LIMIT, 1);
                intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.schedule_task_add_manager));
                intent.putExtra(ContactSearchFragment.EXTRA_HAS_SELECT, (Serializable) taskMangerList);
                intent.setClass(getApplicationContext(), ContactSearchActivity.class);
                startActivityForResult(intent, REQUEST_MANGER);
                break;
            case R.id.rl_task_participant:
                intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                intent.putExtra(ContactSearchFragment.EXTRA_LIMIT, 20);
                intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.schedule_task_add_participant));
                intent.putExtra(ContactSearchFragment.EXTRA_HAS_SELECT, (Serializable) taskParticipantList);
                intent.setClass(getApplicationContext(), ContactSearchActivity.class);
                startActivityForResult(intent, REQUEST_PARTICIPANT);
                break;
            case R.id.rl_deadline:
                DateTimePickerDialog dataTimePickerDialog = new DateTimePickerDialog(this);
                dataTimePickerDialog.setDataTimePickerDialogListener(new DateTimePickerDialog.TimePickerDialogInterface() {
                    @Override
                    public void positiveListener(Calendar calendar) {
                        deadLineCalendar = calendar;
                        String deadLineData = TimeUtils.calendar2FormatString(TaskAddActivity.this, deadLineCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
                        deadlineTimeText.setText(deadLineData);
                    }

                    @Override
                    public void negativeListener(Calendar calendar) {
                    }
                });
                deadLineCalendar = deadLineCalendar == null ? Calendar.getInstance() : deadLineCalendar;
                dataTimePickerDialog.showDatePickerDialog(false, deadLineCalendar);
                break;
            case R.id.rl_state:
                break;
            case R.id.rl_task_end_alert:
                intent.setClass(getApplicationContext(),
                        ScheduleAlertTimeActivity.class);
                intent.putExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME, remindEvent.getAdvanceTimeSpan());
                intent.putExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_IS_ALL_DAY, false);
                intent.putExtra(ScheduleAlertTimeActivity.EXTRA_IS_TASK, true);
                startActivityForResult(intent, REQUEST_ALERT_TIME);
                break;
            case R.id.rl_more:
                moreContentLayout.setVisibility(moreContentLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.rl_attachments_pictures:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, REQUEST_ALBUM);
                break;
            case R.id.rl_attachments_others:
                intent.setClass(this, FileManagerActivity.class);
                intent.putExtra(FileManagerActivity.EXTRA_MAXIMUM, 1);
                startActivityForResult(intent, REQUEST_ATTACHMENT);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case REQUEST_ALBUM:
                    updateAttachment(data, true);
                    break;
                case REQUEST_MANGER:
                    taskMangerList = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    showManagerImage();
                    break;
                case REQUEST_PARTICIPANT:
                    taskParticipantList = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    showParticipantImage();
                    break;
                case REQUEST_ALERT_TIME:
                    remindEvent = (RemindEvent) data.getSerializableExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME);
                    remindEvent.setRemindType("in_app");
                    taskAlertTimeView.setText(remindEvent.getName());
                    break;
                case REQUEST_CLASS_TAG:
                    taskColorTagList.clear();
                    ArrayList<TaskColorTag> arrayTaskColorTags = (ArrayList<TaskColorTag>) data.getSerializableExtra(TaskTagsManageActivity.EXTRA_TAGS);
                    LogUtils.LbcDebug("返回的tags::"+arrayTaskColorTags.size());
                    for (int i=0;i<arrayTaskColorTags.size();i++){
                        LogUtils.LbcDebug("Tag:::"+arrayTaskColorTags.get(i).getTitle()+arrayTaskColorTags.get(i).getId());
                    }
                    taskColorTagList.addAll(arrayTaskColorTags);
                    setTaskColorTags();
                    break;
                case REQUEST_ATTACHMENT:
                    updateAttachment(data, false);
                    break;
            }
        }
    }

    private void setTaskColorTags() {
        if (taskColorTagList.size() == 1) {
            singleTagLayout.setVisibility(View.VISIBLE);
            tagsLayout.setVisibility(View.GONE);
            taskTypeTapImage.setImageResource(CalendarColorUtils.getColorCircleImage(taskColorTagList.get(0).getColor()));
            taskTypeNameText.setText(taskColorTagList.get(0).getTitle());
        } else if (taskColorTagList.size() > 1) {
            singleTagLayout.setVisibility(View.GONE);
            tagsLayout.setVisibility(View.VISIBLE);
            int widthAndHigh = DensityUtil.dip2px(this, 8);
            tagsLayout.removeAllViews();
            for (int i = 0; i < taskColorTagList.size(); i++) {
                ImageView view = new ImageView(this);
                int rightPaddingPixNum = DensityUtil.dip2px(this, 5);
                view.setPadding(rightPaddingPixNum, 0, 0, 0);
                view.setLayoutParams(new ViewGroup.LayoutParams(widthAndHigh + rightPaddingPixNum, widthAndHigh));
                view.setImageResource(CalendarColorUtils.getColorCircleImage(taskColorTagList.get(i).getColor()));
                tagsLayout.addView(view);
            }
        }
    }

    /**
     * 上传附件公共函数
     */
    private void updateAttachment(Intent data, boolean isPictureAttach) {
        sendFileMsg(data, isPictureAttach);
    }

    /**
     * 发送文件
     *
     * @param data
     */
    private void sendFileMsg(Intent data, boolean isPictureAttachment) {

        String filePath;
        if (isPictureAttachment) {
            Uri uri = data.getData();
            filePath = GetPathFromUri4kitkat.getPathByUri(this, uri);
        } else {
            ArrayList<String> pathList = data.getStringArrayListExtra("pathList");
            filePath = pathList.get(0);
        }
        attachmentLocalPath = filePath;
        File tempFile = new File(filePath);
        if (TextUtils.isEmpty(FileUtils.getSuffix(tempFile))) {
            ToastUtils.show(this, getString(R.string.not_support_upload));
            return;
        }
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            ChatAPIService apiService = new ChatAPIService(TaskAddActivity.this);
            apiService.setAPIInterface(new TaskAddActivity.WebService());
            apiService.uploadMsgResource(filePath, System.currentTimeMillis() + "", false);
        }
    }

    /**
     * 添加或者更新Task 有效性检测
     */
    private boolean checkingAddOrUpdateTaskAvailable() {
        if (!NetUtils.isNetworkConnected(this)) {
            ToastUtils.show(this,getString(R.string.net_connected_error));
            return false;
        } else if (StringUtils.isBlank(contentInputEdit.getText().toString())) {
            ToastUtils.show(this,getString(R.string.schedule_task_title_is_empty));
            return false;
        }
        return true;
    }

    /**
     * 创建任务
     */
    private void createTask() {
        String taskContent = contentInputEdit.getText().toString();
        loadingDlg.show();
        apiService.createTasks(taskContent);
    }

    /**
     * 更新任务
     */
    private void updateTask() {
        if (NetUtils.isNetworkConnected(this)) {
            if (taskParticipantList.size() > 0) {
                JSONArray dleMembers = new JSONArray();
                for (int i = 0; i < taskParticipantList.size(); i++) {
                    dleMembers.put(taskParticipantList.get(i).getId());
                }
                apiService.deleteMateForTask(taskResult.getId(), dleMembers);
            }

            String taskData = uploadTaskData();
            apiService.updateTask(taskData, -1);
        }


    }

    /**
     * 显示管理者头像
     */
    private void showManagerImage() {
        initManagerUI();
        if (taskMangerList.size() < 1) {
            return;
        }
        final String id = taskMangerList.get(0).getId();
        String ImageUrl = APIUri.getUserIconUrl(this, id);
        ImageDisplayUtils.getInstance().displayRoundedImage(managerHeadImageView, ImageUrl, R.drawable.default_image, this, 15);
        managerHeadImageView.setVisibility(View.VISIBLE);
        managerNumText.setText(1+getString(R.string.schedule_task_a_person));
        managerNumText.setVisibility(View.VISIBLE);
        managerHeadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("uid", id);
                IntentUtils.startActivity(TaskAddActivity.this, UserInfoActivity.class, bundle);
            }
        });
    }

    /**
     * 负责人UI初始化
     */
    private void initManagerUI() {
        managerHeadImageView.setVisibility(View.GONE);
        managerNumText.setVisibility(View.GONE);
    }

    /**
     * 显示参与者头像
     */
    private void showParticipantImage() {
        List<String> participantImageUrl = new ArrayList<>();
        ImageView[] ImageList = {participantHeadOneImageView, participantHeadTwoImageView, participantHeadThreeImageView};
        initParticipantUI(ImageList);
        if (taskParticipantList.size() < 1) {
            return;
        }
        for (int i = 0; i < taskParticipantList.size(); i++) {
            if (i == 3)
                break;
            final String participantId = taskParticipantList.get(i).getId();
            participantImageUrl.add(APIUri.getUserIconUrl(this, participantId));
            ImageDisplayUtils.getInstance().displayRoundedImage(ImageList[i], participantImageUrl.get(i), R.drawable.default_image, this, 15);
            ImageList[i].setVisibility(View.VISIBLE);
            ImageList[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", participantId);
                    IntentUtils.startActivity(TaskAddActivity.this, UserInfoActivity.class, bundle);
                }
            });
        }
        participantNumText.setText(taskParticipantList.size() + getString(R.string.schedule_task_a_person));
        participantNumText.setVisibility(View.VISIBLE);
    }

    /**
     * 参与者UI初始化
     *
     * @param imageViews
     */
    private void initParticipantUI(ImageView[] imageViews) {
        participantNumText.setVisibility(View.GONE);
        for (int j = 0; j < 3; j++) {
            imageViews[j].setVisibility(View.GONE);
        }
    }

    /**
     * 组织附件数据
     *
     * @param msg
     * @return
     */
    protected JSONObject organizeAttachment(String msg) {
        // 返回的数据格式
        JSONObject jsonAttachment = new JSONObject();
        String type = "", extName = "", category = "", name = "", key = "";
        name = JSONUtils.getString(msg, "name", "");
        key = JSONUtils.getString(msg, "key", "");
        extName = FileUtils.getExtensionName(name);
        type = extName;
        if ("jpg".equals(type) || "png".equals(type)) {
            type = "JPEG";
            category = "IMAGE";
        } else if ("doc".equals(type) || "docx".equals(type)) {
            type = "MS_WORD";
            category = "DOCUMENT";
        } else if ("xls".equals(type) || "xlsx".equals(type)) {
            type = "MS_EXCEL";
            category = "DOCUMENT";
        } else if ("ppt".equals(type) || "pptx".equals(type)) {
            type = "MS_PPT";
            category = "DOCUMENT";
        } else if ("txt".equals(type)) {
            type = "TEXT";
            category = "DOCUMENT";
        } else {
            type = "TEXT";
            category = "DOCUMENT";
        }
        try {
            jsonAttachment.put("name", name);
            jsonAttachment.put("uri", key);
            jsonAttachment.put("category", category);
            jsonAttachment.put("type", type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonAttachment;
    }

    /**
     * 附件holder
     */
    private class AttachmentHolder {
        public ImageView attachmentImageView;
        public TextView attachmentNameText;
        public TextView attachmentStateText;
        public ImageView attachmentDeleteImageView;
    }

    /**
     * 附件 Adapter
     */
    public class AttachmentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return jsonAttachmentList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final int num = i;
            AttachmentHolder otherHolder = new AttachmentHolder();
            if (view == null) {
                view = View.inflate(TaskAddActivity.this, R.layout.item_attachments_abstract, null);
                otherHolder.attachmentDeleteImageView = view.findViewById(R.id.iv_delete_attachemnt);
                otherHolder.attachmentImageView = view.findViewById(R.id.iv_attachemnt_img);
                otherHolder.attachmentNameText = view.findViewById(R.id.tv_add_attachment_name);
                otherHolder.attachmentStateText = view.findViewById(R.id.tv_attachemnt_upload_state);
                view.setTag(otherHolder);
            } else {
                otherHolder = (AttachmentHolder) view.getTag();
            }
            if (JSONUtils.getString(jsonAttachmentList.get(i).getJsonAttachment(), "type", "").equals("JPEG")) {
                String imageUri = getImgPreviewUri(jsonAttachmentList.get(i).getJsonAttachment().toString());
                ImageDisplayUtils.getInstance().displayImage(otherHolder.attachmentImageView,
                        imageUri, R.drawable.ic_volume_file_typ_img);
            } else {
                otherHolder.attachmentImageView.setImageResource(getFileIconByType(JSONUtils.getString(jsonAttachmentList.get(i).getJsonAttachment(), "type", "")));
            }
            otherHolder.attachmentNameText.setText(JSONUtils.getString(jsonAttachmentList.get(i).getJsonAttachment(), "name", ""));
            otherHolder.attachmentDeleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteAttachment(num);
                }
            });
            return view;
        }
    }

    /**
     * 获取图片预览路径
     */
    private String getImgPreviewUri(String attachmentJson) {
        File dir = new File(MyAppConfig.LOCAL_DOWNLOAD_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filename = JSONUtils.getString(attachmentJson, "name", "");
        String fileUri = JSONUtils.getString(attachmentJson, "uri", "");
        String target = MyAppConfig.LOCAL_DOWNLOAD_PATH + filename;
        String downLoadSource = APIUri.getPreviewUrl(fileUri);
        if ((FileUtils.isFileExist(target))) {
            return target;
        } else {
            return downLoadSource;
        }
    }

    private void deleteAttachment(int currentIndex) {
        if (NetUtils.isNetworkConnected(this)) {
            if (isCreateTask) {
                jsonAttachmentList.remove(currentIndex);
                attachmentOtherAdapter.notifyDataSetChanged();
            } else {
                apiService.deleteAttachments(taskResult.getId(), taskResult.getAttachments().get(currentIndex).getId(), currentIndex);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dealTagRequest(SimpleEventMessage messageEvent) {
        if (messageEvent.getAction().equals("deleteTagsRequest")) {
            //更新Task
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            taskResult = new Task();
            taskResult.setTitle(contentInputEdit.getText().toString());
            taskResult.setId(getTaskAddResult.getId());
            taskResult.setOwner(PreferencesUtils.getString(
                    TaskAddActivity.this, "userID"));
            taskResult.setState("ACTIVED");
            for (int i = 0; i < jsonAttachmentList.size(); i++) {
                apiService.addAttachments(getTaskAddResult.getId(), jsonAttachmentList.get(i).getJsonAttachment().toString());
            }
            if (NetUtils.isNetworkConnected(TaskAddActivity.this) && taskParticipantList.size() > 0) {
                JSONArray addMembers = new JSONArray();
                for (int i = 0; i < taskParticipantList.size(); i++) {
                    addMembers.put(taskParticipantList.get(i).getId());
                }
                apiService.inviteMateForTask(getTaskAddResult.getId(), addMembers);
            }
            if (NetUtils.isNetworkConnected(TaskAddActivity.this) && taskMangerList.size() > 0) {
                apiService.changeMessionOwner(taskResult.getId(), taskMangerList.get(0).getId(), taskMangerList.get(0).getName());
            }
            //更新Task
            if (NetUtils.isNetworkConnected(TaskAddActivity.this)) {
                String taskData = uploadTaskData();
                apiService.updateTask(taskData, -1);
            }
                List<String> tagsIdList = new ArrayList<>();
                for (int i = 0; i < taskColorTagList.size(); i++) {
                    tagsIdList.add(taskColorTagList.get(i).getId());
                }
                apiService.addTaskTags(taskResult.getId(), JSONUtils.toJSONString(tagsIdList));
        }

        @Override
        public void returnCreateTaskFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnInviteMateForTaskSuccess(String subject) {
            LoadingDialog.dimissDlg(loadingDlg);
            taskResult.setSubject(new TaskSubject(subject));
        }

        @Override
        public void returnInviteMateForTaskFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }


        @Override
        public void returnUpdateTaskSuccess(int defaultValue) {
            LoadingDialog.dimissDlg(loadingDlg);
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.mession_saving_success));
            setResult(RESULT_OK);
            LogUtils.LbcDebug("修改保存成功");

            if (!isCreateTask) {
                List<String> tagsIdList = new ArrayList<>();
                LogUtils.LbcDebug("orgSize::" +orgTaskColorTagList.size());
                for (int i = 0; i < orgTaskColorTagList.size(); i++) {
                    tagsIdList.add(orgTaskColorTagList.get(i).getId());
                }
                apiService.deleteTaskTags(taskResult.getId(),JSONUtils.toJSONString(tagsIdList));
            } else {
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_TASK_DATA_CHANGED, ""));
                finish();
            }

        }

        @Override
        public void returnUpdateTaskFail(String error, int errorCode, int defaultValue) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnUpLoadResFileSuccess(
                GetFileUploadResult getFileUploadResult, String fakeMessageId) {
            LoadingDialog.dimissDlg(loadingDlg);
            JSONObject jsonAttachment = organizeAttachment(getFileUploadResult.getFileMsgBody());
            if (!isCreateTask && NetUtils.isNetworkConnected(TaskAddActivity.this)) {
                apiService.addAttachments(taskResult.getId(), jsonAttachment.toString());
            }
            jsonAttachmentList.add(new JsonAttachmentAndUri(jsonAttachment, attachmentLocalPath, true));
            attachmentOtherAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnUpLoadResFileFail(String error, int errorCode, String temp) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnAttachmentSuccess(Task taskResult) {
            super.returnAttachmentSuccess(taskResult);
            LoadingDialog.dimissDlg(loadingDlg);
            List<Attachment> attachments = taskResult.getAttachments();
            taskResult.setAttachments(attachments);
            ToastUtils.show(TaskAddActivity.this,
                    getString(R.string.mession_upload_attachment_success));
        }

        @Override
        public void returnAttachmentFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnAddAttachMentSuccess(Attachment attachment) {
            super.returnAddAttachMentSuccess(attachment);
            LoadingDialog.dimissDlg(loadingDlg);
            List<Attachment> attachments = taskResult.getAttachments();
            attachments.add(attachment);
            taskResult.setAttachments(attachments);
        }

        @Override
        public void returnAddAttachMentFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnGetTasksSuccess(GetTaskListResult getTaskListResult) {
            super.returnGetTasksSuccess(getTaskListResult);
            LoadingDialog.dimissDlg(loadingDlg);
            ArrayList<String> memebersIds = new ArrayList<String>();
            memebersIds = handleTaskSearchMembers(getTaskListResult);
            showParticipantImage();
        }

        @Override
        public void returnGetTasksFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnDelTaskMemSuccess() {
            super.returnDelTaskMemSuccess();
            if (NetUtils.isNetworkConnected(TaskAddActivity.this) && taskParticipantList.size() > 0) {
                JSONArray addMembers = new JSONArray();
                for (int i = 0; i < taskParticipantList.size(); i++) {
                    addMembers.put(taskParticipantList.get(i).getId());
                }
                apiService.inviteMateForTask(taskResult.getId(), addMembers);
            }
            LoadingDialog.dimissDlg(loadingDlg);
        }

        @Override
        public void returnDelTaskMemFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnDelAttachmentSuccess(int position) {
            LoadingDialog.dimissDlg(loadingDlg);
            List<Attachment> attachments = taskResult.getAttachments();
            attachments.remove(position);
            taskResult.setAttachments(attachments);
            jsonAttachmentList.remove(position);
            attachmentOtherAdapter.notifyDataSetChanged();
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_TASK_DATA_CHANGED, ""));

        }

        @Override
        public void returnDelAttachmentFail(String error, int errorCode, int position) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnChangeMessionOwnerSuccess(String managerName) {
            LoadingDialog.dimissDlg(loadingDlg);
            taskResult.setOwner(managerName);
        }

        @Override
        public void returnChangeMessionOwnerFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnChangeMessionTagSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
        }

        @Override
        public void returnChangeMessionTagFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnDelelteCalendarByIdSuccess() {
            super.returnDelelteCalendarByIdSuccess();
        }

        @Override
        public void returnAddTaskTagSuccess() {
            if (!isCreateTask) {
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_TASK_DATA_CHANGED, ""));
                 finish();
            }
        }

        @Override
        public void returnAddTaskTagFail(String error, int errorCode) {
            super.returnAddTaskTagFail(error, errorCode);
        }

        @Override
        public void returnDelTaskTagSuccess() {
            List<String> tagsIdList = new ArrayList<>();
            for (int i = 0; i < taskColorTagList.size(); i++) {
                tagsIdList.add(taskColorTagList.get(i).getId());
            }
            apiService.addTaskTags(taskResult.getId(), JSONUtils.toJSONString(tagsIdList));
        }

        @Override
        public void returnDelTaskTagFail(String error, int errorCode) {
            super.returnDelTaskTagFail(error, errorCode);
        }

    }

    /**
     * 筛选任务获取参与人员的list
     *
     * @param getTaskListResult
     * @return
     */
    public ArrayList<String> handleTaskSearchMembers(
            GetTaskListResult getTaskListResult) {
        ArrayList<String> membersIds = new ArrayList<String>();
        for (int i = 0; i < getTaskListResult.getTaskList().size(); i++) {
            if (getTaskListResult.getTaskList().get(i).getState()
                    .contains("ACTIVED")
                    || getTaskListResult.getTaskList().get(i).getState()
                    .contains("REMOVED")) {
                membersIds.add(getTaskListResult.getTaskList().get(i)
                        .getMaster());
                String masterUid = getTaskListResult.getTaskList().get(i).getMaster();
                if (!StringUtils.isBlank(masterUid)) {
                    ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(masterUid);
                    if (contactUser != null) {
                        SearchModel searchModel = new SearchModel(contactUser);
                        orgTaskParticipantList.add(searchModel);
                        taskParticipantList.add(searchModel);
                    }
                }

            }
        }
        return membersIds;
    }

    /**
     * 附件对象及Uri
     */
    public class JsonAttachmentAndUri {
        private JSONObject jsonAttachment;
        private String uri;
        private boolean isNew = true;

        public JsonAttachmentAndUri(JSONObject jsonObject, String uri, boolean isNew) {
            this.jsonAttachment = jsonObject;
            this.uri = uri;
            this.isNew = isNew;
            //根据路径组装成uri
        }

        public JSONObject getJsonAttachment() {
            return jsonAttachment;
        }

        public void setJsonAttachment(JSONObject jsonAttachemnt) {
            this.jsonAttachment = jsonAttachemnt;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public boolean isNew() {
            return isNew;
        }

        public void setNew(boolean aNew) {
            isNew = aNew;
        }
    }

    /**
     * 获取文件类型对应的图标
     */
    public int getFileIconByType(String fileType) {
        int icId = 0;
        switch (fileType) {
            case "TEXT":
                icId = R.drawable.ic_volume_file_typ_txt;
                break;
            case "MS_WORD":
                icId = R.drawable.ic_volume_file_typ_word;
                break;
            case "MS_EXCEL":
                icId = R.drawable.ic_volume_file_typ_excel;
                break;
            case "MS_PPT":
                icId = R.drawable.ic_volume_file_typ_ppt;
                break;
            case "JPEG":
                icId = R.drawable.ic_volume_file_typ_img;
                break;
            default:
                icId = R.drawable.ic_volume_file_typ_unknown;
                break;
        }
        return icId;
    }

    /**
     * 上传任务数据
     */
    private String uploadTaskData() {
        String title = contentInputEdit.getText().toString();
        int priority = segmentControlView.getSelectedIndex();
        taskResult.setTitle(title);
        taskResult.setPriority(priority);
        taskResult.setTags(taskColorTagList);
        if (deadLineCalendar != null)
            taskResult.setDueDate(TimeUtils.localCalendar2UTCCalendar(deadLineCalendar));
        String taskJson = JSONUtils.toJSONString(taskResult);
        return taskJson;
    }
}
