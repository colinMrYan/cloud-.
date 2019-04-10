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
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.ui.work.task.MessionListActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.DataTimePickerDialog;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.SegmentControl;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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


    private static final int MANGER_REQUEST_CODE = 1;
    private static final int ALBUM_REQUEST_CODE = 2;
    private static final int PARTER_REQUEST_CODE = 3;
    private static final int ALERT_TIME_REQUEST_CODE = 4;
    private static final int ATTACHMENT_REQUEST_CODE = 5;
    public static final int CLASS_TAG_REQUEST_CODE = 6;


    private WorkAPIService apiService;
    private LoadingDialog loadingDlg;
    private Task taskResult;
    private List<Attachment> pictureAttachments = new ArrayList<>();
    private List<Attachment> otherAttachments = new ArrayList<>();
    private List<JSONObject> pictureJsonAttachments = new ArrayList<>();
    private List<JSONObject> otherJsonAttachments = new ArrayList<>();
    private List<SearchModel> taskManger = new ArrayList<>();
    private List<SearchModel> taskParters = new ArrayList<>();
    private ArrayList<TaskColorTag> taskColorTags=new ArrayList<>();
    private Calendar deadLineCalendar;
    private AttachmentPictureAdapter attachmentPictureAdapter;
    private AttachmentOthersAdapter attachmentOtherAdapter;

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
        attachmentPicturesList.setAdapter(attachmentPictureAdapter);
        attachmentOtherAdapter = new AttachmentOthersAdapter();
        attachmentOthersList.setAdapter(attachmentOtherAdapter);
        loadingDlg = new LoadingDialog(this);
        apiService = new WorkAPIService(this);
        apiService.setAPIInterface(new TaskAddActivity.WebService());
        //判断是否为新建任务
        if(true){

        }
    }

    private void initView() {


    }

    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.tv_save:
                // createTask();
                break;
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.rl_task_type:
                intent.setClass(this,TaskTagsManageActivity.class);
                intent.putExtra(TaskTagsManageActivity.EXTRA_TAGS,taskColorTags);
                startActivityForResult(intent,CLASS_TAG_REQUEST_CODE);
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
                DataTimePickerDialog dataTimePickerDialog = new DataTimePickerDialog(this);
                dataTimePickerDialog.setDataTimePickerDialogListener(new DataTimePickerDialog.TimePickerDialogInterface() {
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
                AppUtils.openFileSystem(TaskAddActivity.this, ATTACHMENT_REQUEST_CODE);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case ALBUM_REQUEST_CODE:
                    ChatAPIService apiService = new ChatAPIService(TaskAddActivity.this);
                    apiService.setAPIInterface(new TaskAddActivity.WebService());
                    sendFileMsg(data);
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
                    taskColorTags=(ArrayList<TaskColorTag>)data.getSerializableExtra(TaskTagsManageActivity.EXTRA_TAGS);
                    if(taskColorTags.size()==1){
                        singleTagLayout.setVisibility(View.VISIBLE);
                        tagsLayout.setVisibility(View.GONE);
                        taskTypeTapImage.setImageResource(CalendarColorUtils.getColorCircleImage(taskColorTags.get(0).getColor()));
                        taskTypeNameText.setText(taskColorTags.get(0).getTitle());
                    }else if(taskColorTags.size()>1){
                        singleTagLayout.setVisibility(View.GONE);
                        tagsLayout.setVisibility(View.VISIBLE);
                        int widthAndHigh = DensityUtil.dip2px(this,8);
                        for (int i=0;i<taskColorTags.size();i++){
                            View view=new View(this);
                            view.setLayoutParams(new ViewGroup.LayoutParams(widthAndHigh,widthAndHigh));
                            view.setBackgroundResource(CalendarColorUtils.getColorCircleImage(taskColorTags.get(i).getColor()));
                            tagsLayout.addView(view);
                        }

                }
                    break;
            }
        }
    }

    /**
     * 创建任务
     */
    private void createTask() {
        String taskContent = contentInputEdit.getText().toString();
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            apiService.createTasks((StringUtils.isBlank(taskContent) ? "李宝超的测试（default）" : taskContent));
        }
    }

    /**按照现在的接口创建完任务后要继续通过更新实现*/


    /**
     * 显示管理者头像
     */
    private void showManagerImage() {
        initManagerUI();
        if (taskManger.size() < 1) {
            return;
        }
        String id = taskManger.get(0).getId();
        String ImageUrl = APIUri.getUserIconUrl(this, id);
        ImageDisplayUtils.getInstance().displayRoundedImage(managerHeadImageView, ImageUrl, R.drawable.default_image, this, 15);
        managerAddImageView.setVisibility(View.GONE);
        managerHeadImageView.setVisibility(View.VISIBLE);
        managerNumText.setText("1人");
        managerNumText.setVisibility(View.VISIBLE);
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
            String parterId = taskParters.get(i).getId();
            String parterImageUrl = APIUri.getUserIconUrl(this, parterId);
            partersImageUrl.add(parterImageUrl);
            ImageDisplayUtils.getInstance().displayRoundedImage(ImageList[i], partersImageUrl.get(i), R.drawable.default_image, this, 15);
            ImageList[i].setVisibility(View.VISIBLE);
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
     * 发送文件
     *
     * @param data
     */
    private void sendFileMsg(Intent data) {
        ChatAPIService apiService = new ChatAPIService(TaskAddActivity.this);
        apiService.setAPIInterface(new TaskAddActivity.WebService());
        Uri uri = data.getData();
        String filePath = GetPathFromUri4kitkat.getPathByUri(this, uri);
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
            String pictureUri = JSONUtils.getString(pictureJsonAttachments.get(i), "uri", "");
            LogUtils.LbcDebug("pictureUri:::" + pictureUri);
            ImageDisplayUtils.getInstance().displayImage(pictureHolder.attachmentImageView, pictureUri, R.drawable.default_image);
            pictureHolder.attachmentNameText.setText(JSONUtils.getString(pictureJsonAttachments.get(i), "name", ""));
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
            return otherAttachments.size();
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
            String pictureUri = JSONUtils.getString(pictureJsonAttachments.get(i), "uri", "");
            LogUtils.LbcDebug("pictureUri:::" + pictureUri);
            ImageDisplayUtils.getInstance().displayImage(otherHolder.attachmentImageView, pictureUri, R.drawable.default_image);
            otherHolder.attachmentNameText.setText(JSONUtils.getString(pictureJsonAttachments.get(i), "name", ""));
            otherHolder.attachmentDeleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pictureJsonAttachments.remove(num);
                    attachmentPictureAdapter.notifyDataSetChanged();
                }
            });
            return view;
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult) {
            LogUtils.LbcDebug("创建任务成功");
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            taskResult = new Task();
            taskResult.setTitle(contentInputEdit.getText().toString());
            taskResult.setId(getTaskAddResult.getId());
            taskResult.setOwner(PreferencesUtils.getString(
                    TaskAddActivity.this, "userID"));
            taskResult.setState("ACTIVED");
            //调用创建任务成功
            loadingDlg.show();
            for (int i = 0; i < pictureJsonAttachments.size(); i++) {
                apiService.addAttachments(getTaskAddResult.getId(), pictureJsonAttachments.get(i).toString());
            }
        }

        @Override
        public void returnCreateTaskFail(String error, int errorCode) {
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnInviteMateForTaskSuccess(String subject) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
//            isRefreshList = true;
//            task.setSubject(new TaskSubject(subject));
//            displayInviteMates();
        }

        @Override
        public void returnInviteMateForTaskFail(String error, int errorCode) {
            LogUtils.LbcDebug("sendFileMsg4444444444");
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnUpdateTaskSuccess(int defaultValue) {
            LogUtils.LbcDebug("sendFileMsg5555555555");
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            Intent mIntent = new Intent(Constant.ACTION_TASK);
            mIntent.putExtra("refreshTask", "refreshTask");
            EventBus.getDefault().post(new SimpleEventMessage("refreshTask", "refreshTask"));
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.mession_saving_success));
            setResult(RESULT_OK);
        }

        @Override
        public void returnUpdateTaskFail(String error, int errorCode, int defaultValue) {
            LogUtils.LbcDebug("sendFileMsg666666666666666");
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnUpLoadResFileSuccess(
                GetFileUploadResult getFileUploadResult, String fakeMessageId) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            JSONObject jsonAttachment = organizeAttachment(getFileUploadResult.getFileMsgBody());
            LogUtils.LbcDebug("jsonAttachment:::" + jsonAttachment.toString());
            if (JSONUtils.getString(jsonAttachment, "type", "").equals("IMAGE")) {
                pictureJsonAttachments.add(jsonAttachment);
            } else {
                otherJsonAttachments.add(jsonAttachment);
            }
            attachmentPictureAdapter.notifyDataSetChanged();
            attachmentOtherAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnUpLoadResFileFail(String error, int errorCode, String temp) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnAttachmentSuccess(Task taskResult) {
            super.returnAttachmentSuccess(taskResult);
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
//            attachments = taskResult.getAttachments();
//            task.setAttachments(attachments);
//            initAttachments();
//            EventBus.getDefault().post(task);
            ToastUtils.show(TaskAddActivity.this,
                    getString(R.string.mession_upload_attachment_success));
        }

        @Override
        public void returnAttachmentFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnAddAttachMentSuccess(Attachment attachment) {
            super.returnAddAttachMentSuccess(attachment);
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
                //apiService.getSigleTask(task.getId());
            }
            // isRefreshList = true;
        }

        @Override
        public void returnAddAttachMentFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnGetTasksSuccess(GetTaskListResult getTaskListResult) {
            super.returnGetTasksSuccess(getTaskListResult);
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
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
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnDelTaskMemSuccess() {
            super.returnDelTaskMemSuccess();
            //isRefreshList = true;
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            // displayInviteMates();
        }

        @Override
        public void returnDelTaskMemFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnDelAttachmentSuccess(int position) {
            super.returnDelAttachmentSuccess(position);
            //isRefreshList = true;
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            pictureAttachments.remove(position);
            pictureJsonAttachments.remove(position);
//            attachments.remove(position);
//            task.setAttachments(attachments);
//            attachmentAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnDelAttachmentFail(String error, int errorCode, int position) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnChangeMessionOwnerSuccess(String managerName) {
            super.returnChangeMessionOwnerSuccess(managerName);
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            //managerText.setText(managerName);
            // 这里这样写的原因是修改完负责人，需要跳转到我关注的任务，如果从任务列表进入则可以直接finish
            // 如果从工作进入则不可以直接finish
            Intent intent = new Intent();
            intent.setClass(TaskAddActivity.this,
                    MessionListActivity.class);
            intent.putExtra("index", 2);
            startActivity(intent);
            finish();
        }

        @Override
        public void returnChangeMessionOwnerFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnChangeMessionTagSuccess() {
            super.returnChangeMessionTagSuccess();
            //isRefreshList = true;
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
        }

        @Override
        public void returnChangeMessionTagFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

    }


}
