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
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.chat.GetFileUploadResult;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.work.Attachment;
import com.inspur.emmcloud.bean.work.GetTaskAddResult;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.work.TaskResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.work.task.MessionListActivity;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.SegmentControl;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/3/28.
 */
@ContentView(R.layout.activity_add_task)
public class TaskAddActivity extends BaseActivity {
    @ViewInject(R.id.et_input_content)
    private EditText contentInputEdit;
    @ViewInject(R.id.segment_control)
    private SegmentControl segmentControl;
    @ViewInject(R.id.iv_task_type_tap)
    private ImageView taskTypeTapImage;
    @ViewInject(R.id.tv_task_type_name)
    private TextView taskTypeNameText;
    @ViewInject(R.id.tv_deadline_time)
    private TextView deadlineTimeText;
    @ViewInject(R.id.tv_deadline_time)
    private TextView stateText;
    @ViewInject(R.id.iv_more)
    private ImageView moreImage;


    //相册请求码
    private static final int ALBUM_REQUEST_CODE = 1;


    private WorkAPIService apiService;
    private LoadingDialog loadingDlg;
    private TaskResult taskResult;
    private List<Attachment> attachments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_save:
                break;
            case R.id.tv_cancel:
                break;
            case R.id.rl_task_type:
                break;
            case R.id.rl_task_manager:
                break;
            case R.id.rl_task_parter:
                break;
            case R.id.rl_deadline:
                break;
            case R.id.rl_state:
                break;
            case R.id.rl_more:
                break;
            case R.id.rl_attachments_pictures:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, ALBUM_REQUEST_CODE);
                break;
            case R.id.rl_attachments_others:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(RESULT_OK==resultCode){
             switch (requestCode){
                 case ALBUM_REQUEST_CODE:
                     ChatAPIService apiService = new ChatAPIService(TaskAddActivity.this);
                     apiService.setAPIInterface(new TaskAddActivity.WebService());
                     sendFileMsg(data);
                     break;
             }
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

    /**
     * 图片 Adapter*/
    public class attachmentPictureAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
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
            return null;
        }
    }

    /**
     * 文本 Adapter*/
    public class attachmentOthersAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 0;
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
            return null;
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult) {
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            TaskResult taskResult = new TaskResult();
            taskResult.setTitle(contentInputEdit.getText().toString());
            taskResult.setId(getTaskAddResult.getId());
            taskResult.setOwner(PreferencesUtils.getString(
                    TaskAddActivity.this, "userID"));
            taskResult.setState("ACTIVED");
            //调用
             apiService.addAttachments(getTaskAddResult.getId(),"");
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
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnUpdateTaskSuccess(int defaultValue) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            Intent mIntent = new Intent(Constant.ACTION_TASK);
            mIntent.putExtra("refreshTask", "refreshTask");
            EventBus.getDefault().post(new SimpleEventMessage("refreshTask","refreshTask"));
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.mession_saving_success));
            setResult(RESULT_OK);
        }

        @Override
        public void returnUpdateTaskFail(String error, int errorCode, int defaultValue) {
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
//            isRefreshList = true;
             JSONObject jsonAttachment = organizeAttachment(getFileUploadResult.getFileMsgBody());
             addAttachMents(jsonAttachment);
        }

        @Override
        public void returnUpLoadResFileFail(String error, int errorCode, String temp) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TaskAddActivity.this, error, errorCode);
        }

        @Override
        public void returnAttachmentSuccess(TaskResult taskResult) {
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
           //     apiService.getSigleTask(task.getId());
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
