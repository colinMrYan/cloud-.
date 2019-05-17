package com.inspur.emmcloud.ui.work.task;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.chat.GetFileUploadResult;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.schedule.task.Attachment;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.schedule.task.Task;
import com.inspur.emmcloud.bean.schedule.task.TaskColorTag;
import com.inspur.emmcloud.bean.schedule.task.TaskSubject;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.common.EditTextUtils;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.privates.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TaskTagColorUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.HorizontalProgressBarWithNumber;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyDatePickerDialog;
import com.inspur.emmcloud.widget.SegmentControl;
import com.inspur.emmcloud.widget.SegmentControl.OnSegmentControlClickListener;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MessionDetailActivity extends BaseActivity {

    private static final int TAG_TYPE = 0;
    private static final int MANAGER = 1;
    private static final int MEMBER = 2;
    private static final int LINK = 3;
    private static final int UPLOAD_FILE = 4;
    private EditText messionNameEdit;
    private SegmentControl segmentControl;
    private int segmentIndex = 1;
    private TextView messionEndTime;
    private TextView messionStatus;
    private TextView memberText;
    private List<SearchModel> selectMemList = new ArrayList<SearchModel>();
    private List<SearchModel> addMemList = new ArrayList<SearchModel>();
    private List<SearchModel> deleteMemList = new ArrayList<SearchModel>();
    private List<SearchModel> oldMemList = new ArrayList<SearchModel>();
    private TextView managerText;
    private Task task;
    private List<TaskColorTag> tagList;
    private Calendar dueDate;
    private WorkAPIService apiService;
    private LoadingDialog loadingDlg;

    private List<Attachment> attachments = new ArrayList<Attachment>();
    private ImageView[] tagImgs = new ImageView[3];
    private AttachmentGridAdapter attachmentAdapter;

    private boolean isRefreshList = false;// 判断页面数据是否修改让taskList能够刷新
    private boolean isCanModify = true;// 已关注的任务不能进行修改

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mession_detail);
        initViews();
    }

    /**
     * 初始化
     */
    private void initViews() {
        apiService = new WorkAPIService(MessionDetailActivity.this);
        apiService.setAPIInterface(new WebService());
//		initTask();
        task = (Task) getIntent().getExtras().getSerializable("task");
        attachments = task.getAttachments();
        initUI();
        getTasks();
        EditTextUtils.setText(messionNameEdit, task.getTitle());
        handleState();
//		setSegmentControlIndex();
        segmentIndex = (2 - task.getPriority());
        segmentControl.setCurrentIndex(segmentIndex);
        handleTags();
        handleManager();
        handleDeadline();
    }

//	/**
//	 * 设置segment的Index
//	 */
//	private void setSegmentControlIndex() {
//
//	}

//	/**
//	 * 初始化部分task数据
//	 */
//	private void initTask() {
//		task = (TaskResult) getIntent().getExtras().getSerializable("task");
//		attachments = task.getAttachments();
//	}

    /**
     * 处理截止时间
     */
    private void handleDeadline() {
        dueDate = task.getDueDate();
        messionEndTime.setText(dueDate == null ? "" : dueDate.get(Calendar.YEAR) + "-"
                + (dueDate.get(Calendar.MONTH) + 1) + "-"
                + dueDate.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 处理负责人显示
     */
    private void handleManager() {
        String ownerUid = task.getOwner();
        String ownerName = ContactUserCacheUtils.getUserName(ownerUid);
        String masterId = task.getMaster();
        String masterName = ContactUserCacheUtils.getUserName(masterId);
        if (!StringUtils.isEmpty(masterId) && !masterId.contains("null")) {
            managerText.setText(masterName);
        } else if (!StringUtils.isEmpty(ownerUid)) {
            managerText.setText(ownerName);
        } else {
            managerText.setText(getString(R.string.mession_no_person_charge));
        }
    }

    /**
     * 处理tags的显示
     */
    private void handleTags() {
        tagList = task.getTags();
        if (tagList.size() > 0) {
            int tagSize = tagList.size() > 3 ? 3 : tagList.size();
            for (int i = 0; i < tagSize; i++) {
                tagImgs[i].setVisibility(View.VISIBLE);
                TaskTagColorUtils.setTagColorImg(tagImgs[i], tagList.get(i)
                        .getColor());
            }
        }
    }

    /**
     * 把状态转化为容易理解的文字
     */
    private void handleState() {
        String taskState = task.getState();
        if (taskState.contains("ACTIVED") || taskState.contains("PENDING")) {
            messionStatus.setText(getString(R.string.mession_detial_doing));
        } else if (taskState.contains("REMOVED")
                || taskState.contains("SUSPENDED")) {
            messionStatus.setText(getString(R.string.mession_detial_finish));
        }
    }

    /**
     * 获取任务
     */
    private void getTasks() {
        TaskSubject taskSubject = task.getSubject();
        if (NetUtils.isNetworkConnected(MessionDetailActivity.this)
                && taskSubject != null) {
            loadingDlg.show();
            apiService.getTask(taskSubject.getId());
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
        if (NetUtils.isNetworkConnected(MessionDetailActivity.this)) {
            loadingDlg.show();
            apiService.addAttachments(task.getId(), jsonAttachment.toString());
        }
    }

    /**
     * 初始化UI界面上的一些控件
     */
    private void initUI() {
        int tabIndex = -1;
        messionNameEdit = (EditText) findViewById(R.id.mession_name_edit);
        if (getIntent().hasExtra("tabIndex")) {
            tabIndex = (Integer) getIntent().getExtras().getSerializable(
                    "tabIndex");
        }
        if (tabIndex == 2) {
            isCanModify = false;
            messionNameEdit.setFocusable(false);
            messionNameEdit.setFocusableInTouchMode(false);
        }
        segmentControl = (SegmentControl) findViewById(R.id.segment_control);
//		typeImg = (ImageView) findViewById(R.id.mession_typecolor_img);
        messionEndTime = (TextView) findViewById(R.id.mession_endtime_text);
        messionStatus = (TextView) findViewById(R.id.mession_state_text);
        tagImgs[0] = (ImageView) findViewById(R.id.mession_typecolor_img);
        tagImgs[1] = (ImageView) findViewById(R.id.mession_typecolor_img2);
        tagImgs[2] = (ImageView) findViewById(R.id.mession_typecolor_img3);
        memberText = (TextView) findViewById(R.id.mession_allmembers_text);
        managerText = (TextView) findViewById(R.id.mession_managerm_text);
        if (!isCanModify) {
            segmentControl.setIsEnable(false);
        }
        segmentControl
                .setOnSegmentControlClickListener(new OnSegmentControlClickListener() {
                    @Override
                    public void onSegmentControlClick(int index) {
                        segmentIndex = index;
                    }
                });
        loadingDlg = new LoadingDialog(this);
        initAttachments();
    }

    /**
     * 附件展示，及监听逻辑设置
     */
    public void initAttachments() {
        GridView attachmentGridView = (GridView) findViewById(R.id.mession_file_grid);
        attachmentAdapter = new AttachmentGridAdapter();
        attachmentGridView.setAdapter(attachmentAdapter);
        attachmentGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String ownerUid = task.getOwner();
                String userID = ((MyApplication) getApplication()).getUid();
                if (ownerUid.contains(userID)) {
                    if (position == attachments.size()) {
                        AppUtils.openFileSystem(MessionDetailActivity.this, UPLOAD_FILE);
                    } else {
                        downLoadFile(view, position);
                    }
                } else {
                    if (attachments.size() == 0 || ((attachments.size() > 0) && (position == attachments.size()))) {
                        ToastUtils.show(MessionDetailActivity.this,
                                getString(R.string.mession_upload_attachment));
                    }
                }
            }
        });
        attachmentGridView
                .setOnItemLongClickListener(new OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent,
                                                   View view, final int position, long id) {
                        if (position != attachments.size()) {
                            String ownerUid = task.getOwner();
                            String uid = ((MyApplication) getApplication()).getUid();
                            if (ownerUid.contains(uid)) {
                                showDeleteAttachmentsPromptDlg(position);
                            } else {
                                ToastUtils.show(MessionDetailActivity.this,
                                        R.string.mession_delete_attachment);
                            }
                        }
                        return true;
                    }
                });

    }

    /**
     * 弹出删除附件提示框
     *
     * @param position
     */
    private void showDeleteAttachmentsPromptDlg(final int position) {
        new MyQMUIDialog.MessageDialogBuilder(MessionDetailActivity.this)
                .setMessage(R.string.mession_delete_atachment)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        deleteAttachments(position);
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (data != null)) {
            switch (requestCode) {
                case TAG_TYPE:
                    handleMessionTags(data);
                    break;
                case MANAGER:
                    handleManagerChange(data);
                    break;
                case MEMBER:
                    handleMemberChange(data);
                    break;
                case LINK:
                    break;
                case UPLOAD_FILE:
                    ChatAPIService apiService = new ChatAPIService(MessionDetailActivity.this);
                    apiService.setAPIInterface(new WebService());
                    sendFileMsg(data);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 邀请的参与人员修改
     *
     * @param data
     */
    private void handleMemberChange(Intent data) {
        if (data.getExtras().containsKey("selectMemList")) {
            selectMemList = (List<SearchModel>) data.getExtras()
                    .getSerializable("selectMemList");
            addMemList = (List<SearchModel>) data.getExtras().getSerializable(
                    "selectMemList");
            deleteMemList.removeAll(addMemList);
            addMemList.removeAll(oldMemList);
            inviteMatesForTask();
            deleteMemList = (List<SearchModel>) data.getExtras()
                    .getSerializable("selectMemList");
            oldMemList = (List<SearchModel>) data.getExtras().getSerializable(
                    "selectMemList");
        }
    }

    /**
     * Manager变化时处理逻辑
     *
     * @param data
     */
    private void handleManagerChange(Intent data) {
        List<SearchModel> managerList = (List<SearchModel>) data.getExtras()
                .getSerializable("selectMemList");
        if (managerList == null || managerList.size() == 0) {
            return;
        }
        SearchModel searchModel = managerList.get(0);
        final String managerName = searchModel.getName();
        final String managerID = searchModel.getId();
        new MyQMUIDialog.MessageDialogBuilder(MessionDetailActivity.this)
                .setMessage(R.string.mession_change_charge_person)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        changeMessionOwner(managerID, managerName);
                        managerText.setText(managerName);
                    }
                })
                .show();
    }

    /**
     * tag变化的逻辑
     *
     * @param data
     */
    private void handleMessionTags(Intent data) {
        ArrayList<TaskColorTag> addTags = (ArrayList<TaskColorTag>) data
                .getSerializableExtra("tag");
        if (addTags == null || addTags.size() == 0) {
            return;
        }
        int tagNumber = addTags.size() > 3 ? 3 : addTags.size();
        for (int i = 0; i < 3; i++) {
            tagImgs[i].setVisibility(View.GONE);
        }
        for (int i = 0; i < tagNumber; i++) {
            tagImgs[i].setVisibility(View.VISIBLE);
            TaskTagColorUtils.setTagColorImg(tagImgs[i], addTags.get(i).getColor());
        }
        tagList.clear();
        tagList.addAll(addTags);
        task.setTags(tagList);
    }

    /**
     * 负责人变更
     *
     * @param managerID
     */
    protected void changeMessionOwner(String managerID, String managerName) {
        if (NetUtils.isNetworkConnected(MessionDetailActivity.this)) {
            loadingDlg.show();
            apiService.changeMessionOwner(task.getId(), managerID, managerName);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isRefreshList) {
            setResult(RESULT_OK);
        }
        finish();
    }

    /**
     * 删除附件
     *
     * @param position
     */
    private void deleteAttachments(int position) {
        loadingDlg.show();
        apiService.deleteAttachments(task.getId(), attachments.get(position)
                .getId(), position);
    }

    /**
     * 附件显示逻辑
     *
     * @param position
     * @param attachmentImg
     */
    public void displayAttachments(int position, ImageView attachmentImg) {
        String displayImg = "drawable://";
        if ("JPEG".equals(attachments.get(position).getType())) {
            displayImg = displayImg + R.drawable.icon_file_photos;
        } else if ("MS_WORD".equals(attachments.get(position).getType())) {
            displayImg = displayImg + R.drawable.icon_file_word;
        } else if ("MS_EXCEL".equals(attachments.get(position).getType())) {
            displayImg = displayImg + R.drawable.icon_file_excel;
        } else if ("MS_PPT".equals(attachments.get(position).getType())) {
            displayImg = displayImg + R.drawable.icon_file_ppt;
        } else if ("TEXT".equals(attachments.get(position).getType())) {
            displayImg = displayImg + R.drawable.icon_file_unknown;
        } else {
            displayImg = displayImg + R.drawable.icon_file_unknown;
        }
        ImageDisplayUtils.getInstance().displayImage(attachmentImg, displayImg, R.drawable.icon_file_unknown);
    }

    /**
     * 展示邀请的同伴
     */
    private void displayInviteMates() {
        String members = "";
        for (int i = 0; i < selectMemList.size(); i++) {
            members = members + selectMemList.get(i).getName() + " ";
        }
        memberText.setText(members);
    }

    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ibt_back:
                if (isRefreshList) {
                    setResult(RESULT_OK);
                }
                finish();
                break;
            case R.id.mession_type_layout:
                addTags(intent);
                break;
            case R.id.mession_manager_layout:
                String ownerUid = task.getOwner();
                String myUid = ((MyApplication) getApplication()).getUid();
                if (!ownerUid.contains(myUid)) {
                    Toast.makeText(MessionDetailActivity.this,
                            getString(R.string.mession_not_change_charge_person),
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                changeMessionManager();
                break;
            case R.id.mession_members_layout:
                inviteMembers();
                break;
            case R.id.mession_endtime_layout:
                handleDeadlineTimeDialog();
                break;
            case R.id.mession_state_layout:
                break;
            case R.id.mession_link_layout:
                break;
            case R.id.save_btn:
                saveTask();
                break;
            default:
                break;
        }
    }

    /**
     * 变更任务负责人
     */
    private void changeMessionManager() {
        if (isCanModify) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(),
                    ContactSearchActivity.class);
            intent.putExtra("select_content", 2);
            intent.putExtra("isMulti_select", false);
            intent.putExtra("title",
                    getString(R.string.mession_charge_person));
            startActivityForResult(intent, MANAGER);
        }
    }

    /**
     * 保存任务
     */
    private void saveTask() {
        if (isCanModify) {
            String title = messionNameEdit.getText().toString();
            int priority = 2 - segmentIndex;
            task.setTitle(title);
            task.setPriority(priority);
            // task.setAttachments(null);
            if (dueDate != null) {
                task.setDueDate(TimeUtils.localCalendar2UTCCalendar(dueDate));
            }
            String taskJson = JSONUtils.toJSONString(task);
            updateTask(taskJson);
        }
    }

    /**
     * 添加tag
     *
     * @param intent
     */
    private void addTags(Intent intent) {
        if (isCanModify) {
            intent.setClass(MessionDetailActivity.this,
                    MessionTagsManageActivity.class);
            intent.putExtra("from", "mession");
            intent.putExtra("tag", (ArrayList<TaskColorTag>) task.getTags());
            startActivityForResult(intent, TAG_TYPE);
        }
    }

    /**
     * 邀请任务参与成员
     */
    private void inviteMembers() {
        if (isCanModify) {
            Intent intentMem = new Intent();
            intentMem.putExtra("select_content", 2);
            intentMem.putExtra("isMulti_select", true);
            intentMem.putExtra("title",
                    getString(R.string.mession_invate_members));
            if (selectMemList != null) {
                intentMem.putExtra("hasSearchResult",
                        (Serializable) selectMemList);
            }
            intentMem.setClass(getApplicationContext(),
                    ContactSearchActivity.class);
            startActivityForResult(intentMem, MEMBER);
        }
    }

    /**
     * 处理截止时间Dialog
     */
    private void handleDeadlineTimeDialog() {
        if (isCanModify) {
            Calendar cal = dueDate == null ? Calendar.getInstance() : dueDate;
            Locale locale = getResources().getConfiguration().locale;
            Locale.setDefault(locale);
            MyDatePickerDialog dialog = new MyDatePickerDialog(
                    MessionDetailActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    messionEndTime.setText(year + "-"
                            + (monthOfYear + 1) + "-" + dayOfMonth);
                    dueDate = Calendar.getInstance();
                    dueDate.set(Calendar.YEAR, year);
                    dueDate.set(Calendar.MONTH, monthOfYear);
                    dueDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    task.setDueDate(dueDate);
                }
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        }
    }

    /**
     * 更新任务
     */
    private void updateTask(String task) {
        if (NetUtils.isNetworkConnected(MessionDetailActivity.this)) {
            loadingDlg.show();
            apiService.updateTask(task, -1);
        }
    }

    /**
     * 邀请其他人协作，同时包含增减人员
     */
    private void inviteMatesForTask() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            JSONArray addMembers = new JSONArray();
            JSONArray delMembers = new JSONArray();
            for (int i = 0; i < addMemList.size(); i++) {
                addMembers.put(addMemList.get(i).getId());
            }
            for (int i = 0; i < deleteMemList.size(); i++) {
                delMembers.put(deleteMemList.get(i).getId());
            }
            if (addMembers.length() > 0) {
                if (!loadingDlg.isShowing()) {
                    loadingDlg.show();
                }
                apiService.inviteMateForTask(task.getId(), addMembers);
            }
            if (delMembers.length() > 0) {
                if (!loadingDlg.isShowing()) {
                    loadingDlg.show();
                }
                apiService.deleteMateForTask(task.getId(), delMembers);
            }
        }

    }

    /**
     * 发送文件
     *
     * @param data
     */
    private void sendFileMsg(Intent data) {
        ChatAPIService apiService = new ChatAPIService(MessionDetailActivity.this);
        apiService.setAPIInterface(new WebService());
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
     * 下载文件
     *
     * @param convertView
     * @param position
     */
    private void downLoadFile(View convertView, int position) {
        if (!(position < attachments.size())) {
            return;
        }
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File dir = new File(MyAppConfig.LOCAL_DOWNLOAD_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = attachments.get(position).getName();
            final String fileUri = attachments.get(position).getUri();
            final String target = MyAppConfig.LOCAL_DOWNLOAD_PATH
                    + fileName;
            final String downlaodSource = APIUri.getPreviewUrl(fileUri);
            final HorizontalProgressBarWithNumber fileProgressbar = (HorizontalProgressBarWithNumber) convertView
                    .findViewById(R.id.filecard_progressbar);
            fileProgressbar.setTag(target);
            fileProgressbar.setVisibility(View.VISIBLE);
            // 当文件正在下载中 点击不响应
            if ((0 < fileProgressbar.getProgress())
                    && (fileProgressbar.getProgress() < 100)) {
                return;
            }

            APIDownloadCallBack progressCallback = new APIDownloadCallBack(MessionDetailActivity.this, downlaodSource) {
                @Override
                public void callbackStart() {
                    if ((fileProgressbar.getTag() != null)
                            && (fileProgressbar.getTag() == target)) {
                        fileProgressbar.setVisibility(View.VISIBLE);
                    } else {
                        fileProgressbar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void callbackLoading(long total, long current,
                                            boolean isUploading) {
                    if (total == 0) {
                        total = 1;
                    }
                    int progress = (int) ((current * 100) / total);
                    if (!(fileProgressbar.getVisibility() == View.VISIBLE)) {
                        fileProgressbar.setVisibility(View.VISIBLE);
                    }
                    fileProgressbar.setProgress(progress);
                    fileProgressbar.refreshDrawableState();
                }

                @Override
                public void callbackSuccess(File file) {
                    fileProgressbar.setVisibility(View.GONE);
                    ToastUtils.show(MessionDetailActivity.this,
                            getString(R.string.download_success));
                }

                @Override
                public void callbackError(Throwable arg0, boolean arg1) {
                    fileProgressbar.setVisibility(View.GONE);
                    ToastUtils.show(MessionDetailActivity.this,
                            getString(R.string.download_fail));
                }

                @Override
                public void callbackCanceled(CancelledException e) {

                }
            };

            if (FileUtils.isFileExist(fileUri)) {
                fileProgressbar.setVisibility(View.INVISIBLE);
                FileUtils.openFile(MessionDetailActivity.this, fileUri);
            } else if (FileUtils.isFileExist(target)) {
                fileProgressbar.setVisibility(View.INVISIBLE);
                FileUtils.openFile(MessionDetailActivity.this, target);
            } else {
                new DownLoaderUtils().startDownLoad(downlaodSource, target,
                        progressCallback);
            }

        } else {
            ToastUtils.show(getApplicationContext(),
                    R.string.filetransfer_sd_not_exist);
        }

    }

    /**
     * 筛选任务获取参与人员的list
     *
     * @param getTaskListResult
     * @return
     */
    public ArrayList<String> handleTaskSearhMembers(
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
                        selectMemList.add(searchModel);
                        deleteMemList.add(searchModel);
                        oldMemList.add(searchModel);
                    }
                }

            }
        }
        return membersIds;
    }

    private static class Holder {
        ImageView attachmentImg;
        TextView textView;
    }

    /**
     * 附件
     */
    class AttachmentGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return attachments.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            Holder holder = null;
            if (null == convertView) {
                holder = new Holder();
                LayoutInflater mInflater = LayoutInflater
                        .from(MessionDetailActivity.this);
                convertView = mInflater.inflate(R.layout.gridview_item, null);
                holder.attachmentImg = (ImageView) convertView
                        .findViewById(R.id.btn_gv_item);
                holder.attachmentImg.setFocusable(false);
                convertView.setTag(holder);
                holder.textView = (TextView) convertView
                        .findViewById(R.id.file_text);
            } else {
                holder = (Holder) convertView.getTag();
            }
            if (position < attachments.size()) {
                displayAttachments(position, holder.attachmentImg);
                holder.textView.setText(attachments.get(position).getName());
            } else if (position == attachments.size()) {
                ImageDisplayUtils.getInstance().displayImage(holder.attachmentImg, "drawable://"
                        + R.drawable.icon_member_add, R.drawable.icon_member_add);
                holder.textView.setText(getString(R.string.add));
            }
            return convertView;
        }

    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnInviteMateForTaskSuccess(String subject) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            isRefreshList = true;
            task.setSubject(new TaskSubject(subject));
            displayInviteMates();
        }

        @Override
        public void returnInviteMateForTaskFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnUpdateTaskSuccess(int defaultValue) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            Intent mIntent = new Intent(Constant.ACTION_TASK);
            mIntent.putExtra("refreshTask", "refreshTask");
            LocalBroadcastManager.getInstance(MessionDetailActivity.this).sendBroadcast(mIntent);
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.mession_saving_success));
            setResult(RESULT_OK);
        }

        @Override
        public void returnUpdateTaskFail(String error, int errorCode, int defaultValue) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnUpLoadResFileSuccess(
                GetFileUploadResult getFileUploadResult, String fakeMessageId) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            isRefreshList = true;
            JSONObject jsonAttachment = organizeAttachment(getFileUploadResult.getFileMsgBody());
            addAttachMents(jsonAttachment);
        }

        @Override
        public void returnUpLoadResFileFail(String error, int errorCode, String temp) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnAttachmentSuccess(Task taskResult) {
            super.returnAttachmentSuccess(taskResult);
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            attachments = taskResult.getAttachments();
            task.setAttachments(attachments);
            initAttachments();
            EventBus.getDefault().post(task);
            ToastUtils.show(MessionDetailActivity.this,
                    getString(R.string.mession_upload_attachment_success));
        }

        @Override
        public void returnAttachmentFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnAddAttachMentSuccess(Attachment attachment) {
            super.returnAddAttachMentSuccess(attachment);
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
                apiService.getSigleTask(task.getId());
            }
            isRefreshList = true;
        }

        @Override
        public void returnAddAttachMentFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnGetTasksSuccess(GetTaskListResult getTaskListResult) {
            super.returnGetTasksSuccess(getTaskListResult);
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            ArrayList<String> memebersIds = new ArrayList<String>();
            memebersIds = handleTaskSearhMembers(getTaskListResult);
            String memebers = "";
            int memimg = memebersIds.size();
            if (memimg > 4) {
                memimg = 4;
            }
            for (int i = 0; i < memimg; i++) {
                memebers = memebers + ContactUserCacheUtils.getUserName(memebersIds.get(i)) + " ";
            }
            memberText.setText(memebers);
        }

        @Override
        public void returnGetTasksFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnDelTaskMemSuccess() {
            super.returnDelTaskMemSuccess();
            isRefreshList = true;
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            displayInviteMates();
        }

        @Override
        public void returnDelTaskMemFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnDelAttachmentSuccess(int position) {
            super.returnDelAttachmentSuccess(position);
            isRefreshList = true;
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            attachments.remove(position);
            task.setAttachments(attachments);
            attachmentAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnDelAttachmentFail(String error, int errorCode, int position) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnChangeMessionOwnerSuccess(String managerName) {
            super.returnChangeMessionOwnerSuccess(managerName);
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            managerText.setText(managerName);
            // 这里这样写的原因是修改完负责人，需要跳转到我关注的任务，如果从任务列表进入则可以直接finish
            // 如果从工作进入则不可以直接finish
            Intent intent = new Intent();
            intent.setClass(MessionDetailActivity.this,
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
            WebServiceMiddleUtils.hand(MessionDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnChangeMessionTagSuccess() {
            super.returnChangeMessionTagSuccess();
            isRefreshList = true;
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
        }

        @Override
        public void returnChangeMessionTagFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionDetailActivity.this, error, errorCode);
        }

    }


}
