package com.inspur.emmcloud.ui.schedule.task;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
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
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.work.Attachment;
import com.inspur.emmcloud.bean.work.GetTaskAddResult;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.work.Task;
import com.inspur.emmcloud.bean.work.TaskColorTag;
import com.inspur.emmcloud.bean.work.TaskSubject;
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
import com.inspur.emmcloud.widget.SegmentControl;
import com.inspur.imp.plugin.filetransfer.filemanager.FileManagerActivity;

import org.greenrobot.eventbus.EventBus;
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
    @ViewInject(R.id.et_input_content)
    private EditText contentInputEdit;
    @ViewInject(R.id.segment_control)
    private SegmentControl segmentControl;
    @ViewInject(R.id.iv_task_type_tap)
    private ImageView taskTypeTapImage;
    @ViewInject(R.id.tv_task_type_name)
    private TextView taskTypeNameText;
    @ViewInject(R.id.ll_single_tag)
    LinearLayout singleTagLayout;
    @ViewInject(R.id.ll_tags)
    LinearLayout tagsLayout;
    @ViewInject(R.id.tv_deadline_time)
    private TextView deadlineTimeText;
    @ViewInject(R.id.tv_deadline_time)
    private TextView stateText;
    @ViewInject(R.id.iv_more)
    private ImageView moreImage;
    @ViewInject(R.id.iv_parter_head_three)
    private ImageView parterHeadThreeImageView;
    @ViewInject(R.id.iv_parter_head_two)
    private ImageView parterHeadTwoImageView;
    @ViewInject(R.id.iv_parter_head_one)
    private ImageView parterHeadOneImageView;
    @ViewInject(R.id.iv_task_parter_add)
    private ImageView parterAddImageView;
    @ViewInject(R.id.tv_parter_num)
    private TextView parterNumText;
    @ViewInject(R.id.iv_manager_head)
    private ImageView managerHeadImageView;
    @ViewInject(R.id.tv_manager_num)
    private TextView managerNumText;
    @ViewInject(R.id.iv_task_manager_add)
    private ImageView managerAddImageView;
    @ViewInject(R.id.tv_end_task_alert_time)
    private TextView taskAlertTimeView;
    @ViewInject(R.id.lv_attachment_abstract_picture)
    private ListView attachmentPicturesList;
    @ViewInject(R.id.lv_attachment_abstract_other)
    private ListView attachmentOthersList;
    @ViewInject(R.id.ll_more_content)
    private LinearLayout moreContentLayout;
    @ViewInject(R.id.v_priority)
    private SegmentControlView segmentControlView;
    @ViewInject(R.id.tv_title)
    private TextView titleText;

    private static final int MANGER_REQUEST_CODE = 1;
    private static final int ALBUM_REQUEST_CODE = 2;
    private static final int PARTER_REQUEST_CODE = 3;
    private static final int ALERT_TIME_REQUEST_CODE = 4;
    private static final int ATTACHMENT_REQUEST_CODE = 5;
    public static final int CLASS_TAG_REQUEST_CODE = 6;


    private WorkAPIService apiService;
    private LoadingDialog loadingDlg;
    private Task taskResult = new Task();
    private List<Attachment> pictureAttachments = new ArrayList<>();
    private List<Attachment> otherAttachments = new ArrayList<>();
    private List<Attachment> attachments = new ArrayList<>();
    private List<JsonAttachmentAndUri> pictureJsonAttachments = new ArrayList<>();
    private List<JsonAttachmentAndUri> otherJsonAttachments = new ArrayList<>();
    private List<SearchModel> taskManger = new ArrayList<>();
    private List<SearchModel> taskParters = new ArrayList<>();
    private ArrayList<TaskColorTag> taskColorTags = new ArrayList<>();
    private Calendar deadLineCalendar;
    private AttachmentPictureAdapter attachmentPictureAdapter;
    private AttachmentOthersAdapter attachmentOtherAdapter;

    private String attachemntLocalPath = "";
    private Boolean isCreateTask = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
    }

    private void initData() {
        pictureJsonAttachments = new ArrayList<>();
        otherJsonAttachments = new ArrayList<>();
        taskManger = new ArrayList<>();
        taskParters = new ArrayList<>();
        attachmentPictureAdapter = new AttachmentPictureAdapter();
        attachmentOtherAdapter = new AttachmentOthersAdapter();
        loadingDlg = new LoadingDialog(this);
        apiService = new WorkAPIService(this);
        attachmentPicturesList.setAdapter(attachmentPictureAdapter);
        attachmentOthersList.setAdapter(attachmentOtherAdapter);
        apiService.setAPIInterface(new TaskAddActivity.WebService());
        //判断是否为新建任务
        if (getIntent().hasExtra("task")) {
            taskResult = (Task) getIntent().getSerializableExtra("task");
            taskColorTags = (ArrayList<TaskColorTag>) taskResult.getTags();
            isCreateTask = false;
            List<Attachment> attachments = taskResult.getAttachments();
            LogUtils.LbcDebug("attachments"+JSONUtils.toJSONString(attachments));
            for (int i = 0; i < attachments.size(); i++) {
                if (attachments.get(i).getCategory().equals("IMAGE")) {
                    pictureAttachments.add(attachments.get(i));
                   // JSONUtils.getJSONObject(JSONUtils.toJSONString(attachments.get(i)));
                    JSONUtils.getString(JSONUtils.getJSONObject(JSONUtils.toJSONString(attachments.get(i))),"uri","ces");
                    LogUtils.LbcDebug("Uri:::"+JSONUtils.toJSONString(attachments.get(i)));
                  // JsonAttachmentAndUri jsonAttachmentAndUri = new JsonAttachmentAndUri(attachments.get(i),)
                  // pictureJsonAttachments.add()
                    ///
                    String fileName = attachments.get(i).getName();
                    final String fileUri = attachments.get(i).getUri();
                    final String target = MyAppConfig.LOCAL_DOWNLOAD_PATH
                            + fileName;
                    final String downlaodSource = APIUri.getPreviewUrl(fileUri);
                    if (FileUtils.isFileExist(fileUri)) {
                    } else if (FileUtils.isFileExist(target)) {
                       //如果存在文件
                    } else {
                        //如果不存在文件，进行下载，下载成功后刷新列表UI
                        APIDownloadCallBack downLoadallback = new APIDownloadCallBack(TaskAddActivity.this, downlaodSource) {
                            @Override
                            public void callbackStart() {

                            }

                            @Override
                            public void callbackLoading(long total, long current,
                                                        boolean isUploading) {

                            }

                            @Override
                            public void callbackSuccess(File file) {
                             attachmentPictureAdapter.notifyDataSetChanged();
                             attachmentOtherAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void callbackError(Throwable arg0, boolean arg1) {

                            }

                            @Override
                            public void callbackCanceled(CancelledException e) {

                            }
                        };
                        new DownLoaderUtils().startDownLoad(downlaodSource, target,
                                downLoadallback);
                }
                } else {
                    otherAttachments.add(attachments.get(i));
                }
            }
        }
    }





    private void initView() {
        if (getIntent().hasExtra("task")) {
            contentInputEdit.setText(taskResult.getTitle());
            segmentControlView.setSelectedIndex(taskResult.getPriority());
            setTaskColorTags();
            titleText.setText("任务详情");

        }
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.tv_save:
                if (!isAbleCreateOrUpdateTask())
                    return;
                if (isCreateTask) {
                    createTask();
                } else {
                    updateTask();
                }
                break;
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.rl_task_type:
                intent.setClass(this, TaskTagsManageActivity.class);
                intent.putExtra(TaskTagsManageActivity.EXTRA_TAGS, taskColorTags);
                startActivityForResult(intent, CLASS_TAG_REQUEST_CODE);
                break;
            case R.id.rl_task_manager:
                intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                intent.putExtra(ContactSearchFragment.EXTRA_LIMIT, 1);
                intent.putExtra(ContactSearchFragment.EXTRA_TITLE, "添加负责人");
                intent.putExtra(ContactSearchFragment.EXTRA_HAS_SELECT, (Serializable) taskManger);
                intent.setClass(getApplicationContext(), ContactSearchActivity.class);
                startActivityForResult(intent, MANGER_REQUEST_CODE);
                break;
            case R.id.rl_task_parter:
                intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                intent.putExtra(ContactSearchFragment.EXTRA_LIMIT, 20);
                intent.putExtra(ContactSearchFragment.EXTRA_TITLE, "添加参与人");
                intent.putExtra(ContactSearchFragment.EXTRA_HAS_SELECT, (Serializable) taskParters);
                intent.setClass(getApplicationContext(), ContactSearchActivity.class);
                startActivityForResult(intent, PARTER_REQUEST_CODE);
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
                intent = new Intent();
                intent.setClass(this, ScheduleAlertTimeActivity.class);
                String alertTimeData = taskAlertTimeView.getText().toString();
                intent.putExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME, alertTimeData);
                startActivityForResult(intent, ALERT_TIME_REQUEST_CODE);
                break;
            case R.id.rl_more:
                moreContentLayout.setVisibility(moreContentLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.rl_attachments_pictures:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, ALBUM_REQUEST_CODE);
                break;
            case R.id.rl_attachments_others:
                intent.setClass(this, FileManagerActivity.class);
                intent.putExtra(FileManagerActivity.EXTRA_MAXIMUM, 1);
                startActivityForResult(intent, ATTACHMENT_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case ALBUM_REQUEST_CODE:
                    updateAttachment(data, true);
                    break;
                case MANGER_REQUEST_CODE:
                    taskManger = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    showManagerImage();
                    break;
                case PARTER_REQUEST_CODE:
                    taskParters = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    showPartersImage();
                    break;
                case ALERT_TIME_REQUEST_CODE:
                    String alertData = data.getStringExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME);
                    taskAlertTimeView.setText(alertData);
                    break;
                case CLASS_TAG_REQUEST_CODE:
                    taskColorTags = (ArrayList<TaskColorTag>) data.getSerializableExtra(TaskTagsManageActivity.EXTRA_TAGS);
                    setTaskColorTags();
                    break;
                case ATTACHMENT_REQUEST_CODE:
                    updateAttachment(data, false);
                    break;
            }
        }
    }

    private void setTaskColorTags() {
        if (taskColorTags.size() == 1) {
            singleTagLayout.setVisibility(View.VISIBLE);
            tagsLayout.setVisibility(View.GONE);
            taskTypeTapImage.setImageResource(CalendarColorUtils.getColorCircleImage(taskColorTags.get(0).getColor()));
            taskTypeNameText.setText(taskColorTags.get(0).getTitle());
        } else if (taskColorTags.size() > 1) {
            singleTagLayout.setVisibility(View.GONE);
            tagsLayout.setVisibility(View.VISIBLE);
            int widthAndHigh = DensityUtil.dip2px(this, 8);
            tagsLayout.removeAllViews();
            for (int i = 0; i < taskColorTags.size(); i++) {
                ImageView view = new ImageView(this);
                int rightPaddingPixNum = DensityUtil.dip2px(this, 5);
                view.setPadding(rightPaddingPixNum, 0, 0, 0);
                view.setLayoutParams(new ViewGroup.LayoutParams(widthAndHigh + rightPaddingPixNum, widthAndHigh));
                view.setImageResource(CalendarColorUtils.getColorCircleImage(taskColorTags.get(i).getColor()));
                tagsLayout.addView(view);
            }
        }
    }

    /**
     * 上传附件公共函数
     */
    private void updateAttachment(Intent data, boolean isPictureAttach) {
        ChatAPIService apiService = new ChatAPIService(TaskAddActivity.this);
        apiService.setAPIInterface(new TaskAddActivity.WebService());
        sendFileMsg(data, isPictureAttach);
    }

    /**
     * 发送文件
     *
     * @param data
     */
    private void sendFileMsg(Intent data, boolean isPictureAttachment) {
        ChatAPIService apiService = new ChatAPIService(TaskAddActivity.this);
        apiService.setAPIInterface(new TaskAddActivity.WebService());
        String filePath;
        if (isPictureAttachment) {
            Uri uri = data.getData();
            filePath = GetPathFromUri4kitkat.getPathByUri(this, uri);
        } else {
            ArrayList<String> pathList = data.getStringArrayListExtra("pathList");
            filePath = pathList.get(0);
        }
        attachemntLocalPath = filePath;
        File tempFile = new File(filePath);
        if (TextUtils.isEmpty(FileUtils.getSuffix(tempFile))) {
            ToastUtils.show(this, getString(R.string.not_support_upload));
            return;
        }
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            apiService.uploadMsgResource(filePath, System.currentTimeMillis() + "", false);
        }
    }

    /**
     * 添加或者更新Task 有效性检测
     */
    private boolean isAbleCreateOrUpdateTask() {
        if (!NetUtils.isNetworkConnected(this)) {
            ToastUtils.show(this, "网络不通，请检查网络");
            return false;
        } else if (StringUtils.isBlank(contentInputEdit.getText().toString())) {
            ToastUtils.show(this, "任务标题不可为空");
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

    /**按照现在的接口创建完任务后要继续通过更新实现*/


    /**
     * 更新任务
     */
    private void updateTask() {

    }

    /**
     * 显示管理者头像
     */
    private void showManagerImage() {
        initManagerUI();
        if (taskManger.size() < 1) {
            return;
        }
        final String id = taskManger.get(0).getId();
        String ImageUrl = APIUri.getUserIconUrl(this, id);
        ImageDisplayUtils.getInstance().displayRoundedImage(managerHeadImageView, ImageUrl, R.drawable.default_image, this, 15);
        managerAddImageView.setVisibility(View.GONE);
        managerHeadImageView.setVisibility(View.VISIBLE);
        managerNumText.setText("1人");
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
        managerAddImageView.setVisibility(View.VISIBLE);
        managerHeadImageView.setVisibility(View.GONE);
        managerNumText.setVisibility(View.GONE);
    }

    /**
     * 显示参与者头像
     */
    private void showPartersImage() {
        List<String> partersImageUrl = new ArrayList<>();
        ImageView[] ImageList = {parterHeadOneImageView, parterHeadTwoImageView, parterHeadThreeImageView};
        int taskPartersNum = taskParters.size();
        initParterUI(ImageList);
        if (taskPartersNum < 1) {
            return;
        }
        for (int i = 0; i < taskParters.size(); i++) {
            if (i == 3)
                break;
            final String parterId = taskParters.get(i).getId();
            String parterImageUrl = APIUri.getUserIconUrl(this, parterId);
            partersImageUrl.add(parterImageUrl);
            ImageDisplayUtils.getInstance().displayRoundedImage(ImageList[i], partersImageUrl.get(i), R.drawable.default_image, this, 15);
            ImageList[i].setVisibility(View.VISIBLE);
            ImageList[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", parterId);
                    IntentUtils.startActivity(TaskAddActivity.this, UserInfoActivity.class, bundle);
                }
            });
        }
        parterNumText.setText(taskPartersNum + "人");
        parterNumText.setVisibility(View.VISIBLE);
        parterAddImageView.setVisibility(View.GONE);
    }

    /**
     * 参与者UI初始化
     *
     * @param imageViews
     */
    private void initParterUI(ImageView[] imageViews) {
        parterNumText.setVisibility(View.GONE);
        parterAddImageView.setVisibility(View.VISIBLE);/**参与者UI 初始化*/
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
        // {"key":"CI9IPKWGIXJ.jpg","name":"IMG_20160510_095849.jpg","size":1444440,"type":"Photos"}
        JSONObject jsonAttachment = new JSONObject();
        String type = "", extName = "", category = "", name = "", key = "";
        name = JSONUtils.getString(msg, "name", "");
        key = JSONUtils.getString(msg, "key", "");
        extName = FileUtils.getExtensionName(name);
        type = extName;
        if (type.equals("jpg") || "png".equals(type)) {
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
     * 添加附件
     */
    protected void addAttachMents(JSONObject jsonAttachment) {
        if (NetUtils.isNetworkConnected(TaskAddActivity.this)) {
            loadingDlg.show();
            apiService.addAttachments(taskResult.getId(), jsonAttachment.toString());
        }
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
     * 图片 Adapter
     */
    public class AttachmentPictureAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return pictureJsonAttachments.size();
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
            AttachmentHolder pictureHolder = new AttachmentHolder();
            if (view == null) {
                view = View.inflate(TaskAddActivity.this, R.layout.item_attachments_abstract, null);
                pictureHolder.attachmentDeleteImageView = view.findViewById(R.id.iv_delete_attachemnt);
                pictureHolder.attachmentImageView = view.findViewById(R.id.iv_attachemnt_img);
                pictureHolder.attachmentNameText = view.findViewById(R.id.tv_add_attachment_name);
                pictureHolder.attachmentStateText = view.findViewById(R.id.tv_attachemnt_upload_state);
                view.setTag(pictureHolder);
            } else {
                pictureHolder = (AttachmentHolder) view.getTag();
            }
            String pictureUri = pictureJsonAttachments.get(i).getUri();
            ImageDisplayUtils.getInstance().displayImage(pictureHolder.attachmentImageView, pictureUri, R.drawable.default_image);
            pictureHolder.attachmentNameText.setText(JSONUtils.getString(pictureJsonAttachments.get(i).getJsonAttachemnt(), "name", ""));
            pictureHolder.attachmentDeleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pictureJsonAttachments.remove(num);
                    attachmentPictureAdapter.notifyDataSetChanged();
                }
            });
            return view;
        }
    }

    /**
     * 文本 Adapter
     */
    public class AttachmentOthersAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return otherJsonAttachments.size();
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
            String pictureUri = otherJsonAttachments.get(i).getUri();
            LogUtils.LbcDebug("pictureUri:::" + pictureUri);
            /**文件类型添加不同图片 word exel and so on*/

            otherHolder.attachmentImageView.setImageResource(getFileIconByType(JSONUtils.getString(otherJsonAttachments.get(i).getJsonAttachemnt(), "type", "")));
            otherHolder.attachmentNameText.setText(JSONUtils.getString(otherJsonAttachments.get(i).getJsonAttachemnt(), "name", ""));
            otherHolder.attachmentDeleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    otherJsonAttachments.remove(num);
                    attachmentOtherAdapter.notifyDataSetChanged();
                }
            });
            return view;
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult) {
            LogUtils.LbcDebug("创建任务成功");
            LoadingDialog.dimissDlg(loadingDlg);
            taskResult = new Task();
            taskResult.setTitle(contentInputEdit.getText().toString());
            taskResult.setId(getTaskAddResult.getId());
            taskResult.setOwner(PreferencesUtils.getString(
                    TaskAddActivity.this, "userID"));
            taskResult.setState("ACTIVED");
            //调用创建任务成功
            for (int i = 0; i < pictureJsonAttachments.size(); i++) {
                apiService.addAttachments(getTaskAddResult.getId(), pictureJsonAttachments.get(i).getJsonAttachemnt().toString());
            }
            for (int i = 0; i < otherJsonAttachments.size(); i++) {
                apiService.addAttachments(getTaskAddResult.getId(), otherJsonAttachments.get(i).getJsonAttachemnt().toString());
            }
            //添加Parter
            if (NetUtils.isNetworkConnected(TaskAddActivity.this) && taskParters.size() > 0) {
                JSONArray addMembers = new JSONArray();
                for (int i = 0; i < taskParters.size(); i++) {
                    addMembers.put(taskParters.get(i).getId());
                }
                apiService.inviteMateForTask(getTaskAddResult.getId(), addMembers);
            }
            //更改Manager
            if (NetUtils.isNetworkConnected(TaskAddActivity.this) && taskManger.size() > 0) {
                apiService.changeMessionOwner(taskResult.getId(), taskManger.get(0).getId(), taskManger.get(0).getName());
            }

            //更新Task
            if (NetUtils.isNetworkConnected(TaskAddActivity.this)) {
                String taskData = uploadTaskData();
                LogUtils.LbcDebug("taskData：：：" + taskData);
                apiService.updateTask(taskData, -1);
            }

            //更新Task
            if (NetUtils.isNetworkConnected(TaskAddActivity.this)) {
                if (getIntent().hasExtra("task")) {
                    apiService.deleteTaskTags(taskResult.getId());
                } else {
                    List<String> tagsIdList = new ArrayList<>();
                    for (int i = 0; i < taskColorTags.size(); i++) {
                        tagsIdList.add(taskColorTags.get(i).getId());
                    }
                    apiService.addTaskTags(taskResult.getId(), JSONUtils.toJSONString(tagsIdList));
                }

            }


        }

        @Override
        public void returnCreateTaskFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnInviteMateForTaskSuccess(String subject) {
            LoadingDialog.dimissDlg(loadingDlg);
            LogUtils.LbcDebug("添加Parter 成功");
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
            EventBus.getDefault().post(new SimpleEventMessage("refreshTask", "refreshTask"));
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.mession_saving_success));
            setResult(RESULT_OK);
            LogUtils.LbcDebug("修改保存成功");
            finish();
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
            LogUtils.LbcDebug("jsonAttachment::" + jsonAttachment.toString());
            if (JSONUtils.getString(jsonAttachment, "category", "").equals("IMAGE")) {
                pictureJsonAttachments.add(new JsonAttachmentAndUri(jsonAttachment, attachemntLocalPath, true));
            } else {
                otherJsonAttachments.add(new JsonAttachmentAndUri(jsonAttachment, attachemntLocalPath, true));
            }
            attachmentPictureAdapter.notifyDataSetChanged();
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
            /**判断当前的任务类型 图片或者其他类型*/

            attachments = taskResult.getAttachments();
            taskResult.setAttachments(attachments);
            ToastUtils.show(TaskAddActivity.this,
                    getString(R.string.mession_upload_attachment_success));
            LogUtils.LbcDebug("return attachments");
        }

        @Override
        public void returnAttachmentFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnAddAttachMentSuccess(Attachment attachment) {
            super.returnAddAttachMentSuccess(attachment);
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            attachments = taskResult.getAttachments();
            attachments.add(attachment);
            taskResult.setAttachments(attachments);
            LogUtils.LbcDebug("return Add Attachments");
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
            //  memebersIds = handleTaskSearhMembers(getTaskListResult);
            String memebers = "";
            int memimg = memebersIds.size();
            if (memimg > 4) {
                memimg = 4;
            }
            for (int i = 0; i < memimg; i++) {
                memebers = memebers + ContactUserCacheUtils.getUserName(memebersIds.get(i)) + " ";
            }
            //   memberText.setText(memebers);
        }

        @Override
        public void returnGetTasksFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnDelTaskMemSuccess() {
            super.returnDelTaskMemSuccess();
            //isRefreshList = true;
            LoadingDialog.dimissDlg(loadingDlg);
            // displayInviteMates();
        }

        @Override
        public void returnDelTaskMemFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnDelAttachmentSuccess(int position) {
            super.returnDelAttachmentSuccess(position);
            //isRefreshList = true;
            LoadingDialog.dimissDlg(loadingDlg);
            pictureAttachments.remove(position);
            pictureJsonAttachments.remove(position);
//            attachments.remove(position);
//            task.setAttachments(attachments);
//            attachmentAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnDelAttachmentFail(String error, int errorCode, int position) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnChangeMessionOwnerSuccess(String managerName) {
            super.returnChangeMessionOwnerSuccess(managerName);
            taskResult.setOwner(managerName);
            LoadingDialog.dimissDlg(loadingDlg);
            LogUtils.LbcDebug("改变Owner成功");
            //managerText.setText(managerName);
            // 这里这样写的原因是修改完负责人，需要跳转到我关注的任务，如果从任务列表进入则可以直接finish
            // 如果从工作进入则不可以直接finish
//            Intent intent = new Intent();
//            intent.setClass(TaskAddActivity.this,
//                    MessionListActivity.class);
//            intent.putExtra("index", 2);
//            startActivity(intent);
//            finish();
        }

        @Override
        public void returnChangeMessionOwnerFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnChangeMessionTagSuccess() {
            super.returnChangeMessionTagSuccess();
            //isRefreshList = true;
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
            super.returnAddTaskTagSuccess();
            LogUtils.LbcDebug("add Task Tags Success");
        }

        @Override
        public void returnAddTaskTagFail(String error, int errorCode) {
            super.returnAddTaskTagFail(error, errorCode);
            LogUtils.LbcDebug("add Task Tags Fails");
        }

        @Override
        public void returnDelTaskTagSuccess() {
            super.returnDelTaskTagSuccess();
            List<String> tagsIdList = new ArrayList<>();
            for (int i = 0; i < taskColorTags.size(); i++) {
                tagsIdList.add(taskColorTags.get(i).getId());
            }
            String colorData = JSONUtils.toJSONString(tagsIdList);
            apiService.addTaskTags(taskResult.getId(), colorData);
            LogUtils.LbcDebug("del Task Tags Success");
        }

        @Override
        public void returnDelTaskTagFail(String error, int errorCode) {
            super.returnDelTaskTagFail(error, errorCode);
            LogUtils.LbcDebug("del Task Tags Fail");
        }
    }

    /**
     * 附件对象及Uri
     */
    public class JsonAttachmentAndUri {
        private JSONObject jsonAttachemnt;
        private String uri;
        private boolean isNew = true;

        public JsonAttachmentAndUri(JSONObject jsonObject, String uri, boolean isNew) {
            this.jsonAttachemnt = jsonObject;
            this.uri = uri;
            this.isNew = isNew;
            //根据路径组装成uri
        }

        public JSONObject getJsonAttachemnt() {
            return jsonAttachemnt;
        }

        public void setJsonAttachemnt(JSONObject jsonAttachemnt) {
            this.jsonAttachemnt = jsonAttachemnt;
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
        taskResult.setTags(taskColorTags);
        if (deadLineCalendar != null)
            taskResult.setDueDate(TimeUtils.localCalendar2UTCCalendar(deadLineCalendar));
        String taskJson = JSONUtils.toJSONString(taskResult);
        LogUtils.LbcDebug("taskJson" + taskJson);
        return taskJson;
    }
}
