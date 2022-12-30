package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.FileFilterPopGridAdapter;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.FileActionData;
import com.inspur.emmcloud.baselib.widget.FileActionLayout;
import com.inspur.emmcloud.baselib.widget.progressbar.CircleProgressBar;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetworkMobileTipUtil;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.DownloadInfo;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.interf.ChatProgressCallback;
import com.inspur.emmcloud.util.privates.ChatFileDownloadManager;
import com.inspur.emmcloud.util.privates.DownloadCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;

import java.io.File;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupFileActivity extends BaseActivity {

    protected static final String SORT_BY_NAME_UP = "sort_by_name_up";
    protected static final String SORT_BY_NAME_DOWN = "sort_by_name_down";
    protected static final String SORT_BY_TIME_UP = "sort_by_time_up";
    protected static final String SORT_BY_TIME_DOWN = "sort_by_time_down";
    protected String sortType = "sort_by_name_up";
    @BindView(R.id.lv_file)
    ListView fileListView;
    @BindView(R.id.rl_no_channel_file)
    RelativeLayout noChannelFileLayout;
    @BindView(R.id.tv_order_by_name_asc)
    TextView operationSortText;
    @BindView(R.id.tv_filter_by_file_type)
    TextView filterByFileTypeText;
    final List<FileActionData> fileActionDataList = new ArrayList<>();
    private String cid;
    private List<Message> fileMessageList = new ArrayList<>();
    private PopupWindow sortOperationPop;
    private GroupFileAdapter adapter;
    private FileSortComparable fileSortComparable;
    @BindView(R.id.ll_file_action)
    FileActionLayout fileActionLayout;
    private List<Message> selectGroupFileList = new ArrayList<>();
    private String downLoadAction; //弹框点击状态
    List<Message> fileTypeMessageListWithOrder = new ArrayList<>();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        cid = getIntent().getExtras().getString("cid");
        getFileMsgList();
        noChannelFileLayout.setVisibility(fileMessageList.size() == 0 ? View.VISIBLE : View.GONE);
        fileSortComparable = new FileSortComparable();
        Collections.sort(fileMessageList, fileSortComparable);
        fileTypeMessageListWithOrder.addAll(fileMessageList);
        adapter = new GroupFileAdapter(fileTypeMessageListWithOrder);
        fileListView.setAdapter(adapter);
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                downloadOrOpenFile(position);
            }
        });
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = fileTypeMessageListWithOrder.get(position);
                final MsgContentRegularFile msgContentFile = message.getMsgContentAttachmentFile();
                final String fileDownloadPath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_MESSAGE, message.getId(), msgContentFile.getName());
                if (!StringUtils.isBlank(fileDownloadPath)) {
                    FileUtils.openFile(GroupFileActivity.this, fileDownloadPath);
                } else {
                    Intent intent = new Intent(GroupFileActivity.this, ChatFileDownloadActivtiy.class);
                    intent.putExtra("message", message);
                    startActivity(intent);
                }
                selectGroupFileList.clear();
                setBottomOperationItemShow(selectGroupFileList);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    //    private void handleMessageOrder(List<Message> fileInfoList) {
//        //根据fileInfoList的排序排列fileTypeMessage
//        fileTypeMessageListWithOrder.clear();
//        List<Message> fileTypeMessageList = MessageCacheUtil.getFileTypeMsgList(MyApplication.getInstance(), cid);
//            for (Message message : fileTypeMessageList) {
//                String url = APIUri.getChatFileResouceUrl(message.getChannel(), message.getMsgContentAttachmentFile().getMedia());
//                if (.equals(url)) {
//                    fileTypeMessageListWithOrder.add(message);
//                }
//            }
//    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_file;
    }

    private void getFileMsgList() {
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            List<Msg> fileTypeMsgList = MsgCacheUtil.getFileTypeMsgList(
                    GroupFileActivity.this, cid);
            for (Msg msg : fileTypeMsgList) {
                fileMessageList.add(msg.msg2Message());
            }
        } else {
            fileMessageList = MessageCacheUtil.getFileTypeMsgList(MyApplication.getInstance(), cid);
        }
    }

    private void showSortOperationPop() {
        View contentView = LayoutInflater.from(GroupFileActivity.this)
                .inflate(R.layout.file_sort_operation_pop, null);
        sortOperationPop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        ((TextView) contentView.findViewById(R.id.sort_by_time_up_text)).setTextColor(Color.parseColor(sortType.equals(SORT_BY_TIME_UP) ? "#2586CD" : "#666666"));
        ((TextView) contentView.findViewById(R.id.sort_by_time_down_text)).setTextColor(Color.parseColor(sortType.equals(SORT_BY_TIME_DOWN) ? "#2586CD" : "#666666"));
        ((TextView) contentView.findViewById(R.id.sort_by_name_up_text)).setTextColor(Color.parseColor(sortType.equals(SORT_BY_NAME_UP) ? "#2586CD" : "#666666"));
        ((TextView) contentView.findViewById(R.id.sort_by_name_down_text)).setTextColor(Color.parseColor(sortType.equals(SORT_BY_NAME_DOWN) ? "#2586CD" : "#666666"));
        (contentView.findViewById(R.id.sort_by_time_up_select_img)).setVisibility(sortType.equals(SORT_BY_TIME_UP) ? View.VISIBLE : View.INVISIBLE);
        (contentView.findViewById(R.id.sort_by_time_down_select_img)).setVisibility(sortType.equals(SORT_BY_TIME_DOWN) ? View.VISIBLE : View.INVISIBLE);
        (contentView.findViewById(R.id.sort_by_name_up_select_img)).setVisibility(sortType.equals(SORT_BY_NAME_UP) ? View.VISIBLE : View.INVISIBLE);
        (contentView.findViewById(R.id.sort_by_name_down_select_img)).setVisibility(sortType.equals(SORT_BY_NAME_DOWN) ? View.VISIBLE : View.INVISIBLE);
        sortOperationPop.setTouchable(true);
        sortOperationPop.setBackgroundDrawable(ContextCompat.getDrawable(
                getApplicationContext(), R.drawable.pop_window_view_tran));
        sortOperationPop.setOutsideTouchable(true);
        sortOperationPop.showAsDropDown(operationSortText);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_drop_up);
        drawable.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 14), DensityUtil.dip2px(getApplicationContext(), 14));
        operationSortText.setCompoundDrawables(null, null, drawable, null);
        sortOperationPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setOperationSort();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setOperationSort() {
        Drawable drawableDown = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_drop_down);
        drawableDown.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 14), DensityUtil.dip2px(getApplicationContext(), 14));
        operationSortText.setCompoundDrawables(null, null, drawableDown, null);
        String sortTypeShowTxt;
        switch (sortType) {
            case SORT_BY_NAME_DOWN:
                sortTypeShowTxt = getString(R.string.sort_by_name_dasc);
                break;
            case SORT_BY_TIME_UP:
                sortTypeShowTxt = getString(R.string.sort_by_time_asc);
                break;
            case SORT_BY_TIME_DOWN:
                sortTypeShowTxt = getString(R.string.sort_by_time_dasc);
                break;
            default:
                sortTypeShowTxt = getString(R.string.sort_by_name_asc);
                break;
        }
        operationSortText.setText(sortTypeShowTxt);
        Collections.sort(fileMessageList, fileSortComparable);
//        handleMessageOrder(fileMessageList);
        fileTypeMessageListWithOrder.clear();
        fileTypeMessageListWithOrder.addAll(fileMessageList);
        adapter.notifyDataSetChanged();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_order_by_name_asc:
                showSortOperationPop();
                break;
            case R.id.sort_by_time_up_layout:
                sortType = SORT_BY_TIME_UP;
                sortOperationPop.dismiss();
                break;
            case R.id.sort_by_time_down_layout:
                sortType = SORT_BY_TIME_DOWN;
                sortOperationPop.dismiss();
                break;
            case R.id.sort_by_name_up_layout:
                sortType = SORT_BY_NAME_UP;
                sortOperationPop.dismiss();
                break;
            case R.id.sort_by_name_down_layout:
                sortType = SORT_BY_NAME_DOWN;
                sortOperationPop.dismiss();
                break;
            case R.id.tv_filter_by_file_type:
                showFileFilterPop(v);
                break;
        }
    }

    private void showFileFilterPop(View v) {
        View contentView = LayoutInflater.from(GroupFileActivity.this)
                .inflate(R.layout.file_filter_pop, null);
        final PopupWindow fileFilterPop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        GridView fileFilterGrid = contentView.findViewById(R.id.file_filter_type_grid);
        fileFilterGrid.setAdapter(new FileFilterPopGridAdapter(GroupFileActivity.this));
        fileFilterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] fileFilterTypes = {FileUtils.CLOUD_DOCUMENT, FileUtils.CLOUD_PICTURE, FileUtils.CLOUD_AUDIO, FileUtils.CLOUD_VIDEO, FileUtils.CLOUD_UNKNOWN_FILE_TYPE};
                filterFilesByFileType(fileFilterTypes[position]);
                fileFilterPop.dismiss();
            }
        });
        fileFilterPop.setTouchable(true);
        fileFilterPop.setBackgroundDrawable(ContextCompat.getDrawable(
                getApplicationContext(), R.drawable.pop_window_view_tran));
        fileFilterPop.setOutsideTouchable(true);
        fileFilterPop.showAsDropDown(v);
    }

    private void filterFilesByFileType(String fileFilterType) {
        List<Message> fileInfoFilterList = new ArrayList<>();
        fileInfoFilterList.clear();
        for (Message message : fileMessageList) {
            String format = FileUtils.getMimeType(message.getMsgContentAttachmentFile().getName());
            if (fileFilterType.equals(FileUtils.getFileTypeFormat(format))) {
                fileInfoFilterList.add(message);
            }
        }
        fileTypeMessageListWithOrder.clear();
        fileTypeMessageListWithOrder.addAll(fileInfoFilterList);
//        handleMessageOrder(fileInfoFilterList);
        adapter.notifyDataSetChanged();
    }

    public void displayFiles(final ViewHolder holder, final int position, final List<Message> messageList) {
        holder.fileInfoLayout.setVisibility(View.VISIBLE);
        holder.selectImg.setVisibility(View.GONE);
        final Message message = messageList.get(position);
        final String fileName = message.getMsgContentAttachmentFile().getName();
        holder.fileNameText.setText(fileName);
        holder.fileSizeText.setText(FileUtils.formatFileSize(message.getMsgContentAttachmentFile().getSize()));
        if (selectGroupFileList.size() > 0) {
            holder.selectImg.setImageResource(selectGroupFileList.contains(message) ? R.drawable.ic_select_yes : R.drawable.ic_select_no);
        } else {
            holder.selectImg.setImageResource(R.drawable.ic_no_selected);
        }
//        holder.fileTimeText.setText(TimeUtils.getChannelMsgDisplayTime(GroupFileActivity.this, message.getCreationDate()));
        holder.fileTimeText.setText(TimeUtils.getTime(message.getCreationDate(), format));
        holder.fileImg.setImageResource(FileUtils.getFileIconResIdByFileName(message.getMsgContentAttachmentFile().getName()));
        DownloadInfo info = DownloadInfo.message2DownloadInfo(message);
        DownloadInfo downloadInfo = ChatFileDownloadManager.getInstance().getManagerDownloadInfo(info);
        if (downloadInfo != null) {
            if (DownloadInfo.STATUS_PAUSE.equals(downloadInfo.getStatus()) ||
                    DownloadInfo.STATUS_LOADING.equals(downloadInfo.getStatus())) {
                holder.statusLayout.setVisibility(View.VISIBLE);
                holder.progressBar.setStatus(downloadInfo.transfer2ProgressStatus(downloadInfo.getStatus()));
                holder.progressBar.setProgress(downloadInfo.getProgress());
                holder.statusLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeStatus(holder, position);
                    }
                });
            } else {
                holder.statusLayout.setVisibility(View.GONE);
            }
            handleDownloadCallback(holder, downloadInfo);
        }
        //暂时不支持下载多个文件
        holder.selectImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectGroupFileList.contains(messageList.get(position))) {
                    selectGroupFileList.remove(messageList.get(position));
                } else {
                    selectGroupFileList.clear();
                    selectGroupFileList.add(messageList.get(position));
                }
                adapter.notifyDataSetChanged();
                setBottomOperationItemShow(selectGroupFileList);
            }
        });
    }

    private void downloadOrOpenFile(int position) {
        if (fileTypeMessageListWithOrder != null && fileTypeMessageListWithOrder.size() > 0) {
            Message message = fileTypeMessageListWithOrder.get(position);
            final MsgContentRegularFile msgContentFile = message.getMsgContentAttachmentFile();
            final String fileDownloadPath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_MESSAGE, message.getId(), msgContentFile.getName());
            if (!StringUtils.isBlank(fileDownloadPath)) {
                FileUtils.openFile(GroupFileActivity.this, fileDownloadPath);
            } else {
                Intent intent = new Intent(GroupFileActivity.this, ChatFileDownloadActivtiy.class);
                intent.putExtra("message", message);
                startActivity(intent);
            }
        }
    }

    private void changeStatus(final ViewHolder holder, int position) {
        if (fileTypeMessageListWithOrder != null && fileTypeMessageListWithOrder.size() > 0) {
            Message message = fileTypeMessageListWithOrder.get(position);
            DownloadInfo info = DownloadInfo.message2DownloadInfo(message);
            final DownloadInfo downloadInfo = ChatFileDownloadManager.getInstance().getManagerDownloadInfo(info);
            if (downloadInfo != null) {
                String status = downloadInfo.getStatus();
                if (status.equals(DownloadInfo.STATUS_LOADING)) {
                    downloadInfo.setStatus(DownloadInfo.STATUS_PAUSE);
                    ChatFileDownloadManager.getInstance().cancelDownloadFile(downloadInfo);
                    DownloadCacheUtils.saveDownloadFile(downloadInfo);
                } else if (status.equals(DownloadInfo.STATUS_PAUSE) || status.equals(DownloadInfo.STATUS_FAIL)) {
                    NetworkMobileTipUtil.checkEnvironment(this, R.string.file_download_network_type_warning, downloadInfo.getSize(),
                            new NetworkMobileTipUtil.Callback() {
                                @Override
                                public void cancel() {

                                }

                                @Override
                                public void onNext() {
                                    downloadInfo.setStatus(DownloadInfo.STATUS_LOADING);
                                    ChatFileDownloadManager.getInstance().reDownloadFile(downloadInfo);
                                    DownloadCacheUtils.saveDownloadFile(downloadInfo);
                                    holder.progressBar.setStatus(DownloadInfo.transfer2ProgressStatus(downloadInfo.getStatus()));
                                }
                            });

                }
                holder.progressBar.setStatus(DownloadInfo.transfer2ProgressStatus(downloadInfo.getStatus()));
            }
        }
    }

    /**
     * 监听下载回调
     */
    private void handleDownloadCallback(final ViewHolder holder, final DownloadInfo downloadInfo) {
        ChatFileDownloadManager.getInstance().setBusinessProgressCallback(downloadInfo, new ChatProgressCallback() {
            @Override
            public void onSuccess(File file) {
                Log.d("zhang", "GroupFile onSuccess:");
                holder.progressBar.setStatus(CircleProgressBar.Status.Success);
                holder.statusLayout.setEnabled(false);
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        holder.statusLayout.setVisibility(View.GONE);
                    }
                }, 2000);
            }

            @Override
            public void onLoading(int progress, long current, String speed) {
//                Log.d("zhang", "GroupFile onLoading:");
                holder.progressBar.setStatus(CircleProgressBar.Status.Loading);
                holder.progressBar.setProgress(progress);
            }

            @Override
            public void onFail() {
                Log.d("zhang", "GroupFile onFail:");
                holder.statusLayout.setVisibility(View.GONE);
                holder.progressBar.setStatus(CircleProgressBar.Status.Fail);
            }
        });
    }

    /**
     * 根据所选文件的类型展示操作按钮
     */
    protected void setBottomOperationItemShow(List<Message> selectFileList) {
        downLoadAction = getString(R.string.download);
        fileActionDataList.clear();
        fileActionDataList.add(new FileActionData(downLoadAction, R.drawable.ic_file_download, true));
        for (int i = 0; i < fileActionDataList.size(); i++) {
            if (!fileActionDataList.get(i).isShow()) {
                fileActionDataList.remove(i);
                i--;
                continue;
            }
        }
        fileActionLayout.setVisibility(selectFileList.size() > 0 && fileActionDataList.size() > 0 ? View.VISIBLE : View.GONE);
        fileActionLayout.clearView();
        fileActionLayout.setFileActionData(fileActionDataList, new FileActionLayout.FileActionClickListener() {
            @Override
            public void fileActionSelectedListener(String actionName) {
                fileActionLayout.setVisibility(View.GONE);
                handleDownLoadAction(actionName);
            }
        });
    }

    /**
     * 多文件下载
     *
     * @param actionName
     */
    private void handleDownLoadAction(String actionName) {
        if (actionName.equals(downLoadAction)) {
            Message message = selectGroupFileList.get(0);
            final MsgContentRegularFile msgContentFile = message.getMsgContentAttachmentFile();
            final String fileDownloadPath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_MESSAGE, message.getId(), msgContentFile.getName());
            if (!StringUtils.isBlank(fileDownloadPath)) {
                FileUtils.openFile(GroupFileActivity.this, fileDownloadPath);
            } else {
                Intent intent = new Intent(GroupFileActivity.this, ChatFileDownloadActivtiy.class);
                intent.putExtra("message", message);
                startActivity(intent);
            }
            selectGroupFileList.clear();
            setBottomOperationItemShow(selectGroupFileList);
        }
    }

    private class FileSortComparable implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Message messageA = (Message) o1;
            Message messageB = (Message) o2;
            int sortResult = 0;
            String messageNameA = messageA.getMsgContentAttachmentFile().getName();
            String messageNameB = messageB.getMsgContentAttachmentFile().getName();
            switch (sortType) {
                case SORT_BY_NAME_UP:
                    sortResult = Collator.getInstance(Locale.CHINA).compare(messageNameA, messageNameB);
                    break;
                case SORT_BY_NAME_DOWN:
                    sortResult = 0 - Collator.getInstance(Locale.CHINA).compare(messageNameA, messageNameB);
                    break;
                case SORT_BY_TIME_DOWN:
                    if (messageA.getCreationDate() == messageB.getCreationDate()) {
                        sortResult = 0;
                    } else if (messageA.getCreationDate() < messageB.getCreationDate()) {
                        sortResult = 1;
                    } else {
                        sortResult = -1;
                    }
                    break;
                case SORT_BY_TIME_UP:
                    if (messageA.getCreationDate() == messageB.getCreationDate()) {
                        sortResult = 0;
                    } else if (messageA.getCreationDate() < messageB.getCreationDate()) {
                        sortResult = -1;
                    } else {
                        sortResult = 1;
                    }
                    break;
                default:
                    sortResult = Collator.getInstance(Locale.CHINA).compare(messageNameA, messageNameB);
                    break;
            }
            return sortResult;
        }
    }

    private class GroupFileAdapter extends BaseAdapter {

        private List<Message> messageList = new ArrayList<>();

        public GroupFileAdapter(List<Message> messageList) {
            this.messageList = messageList;
        }

        @Override
        public int getCount() {
            return messageList.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(GroupFileActivity.this).inflate(R.layout.chat_file_item_view, null);
                holder.fileImg = convertView.findViewById(R.id.file_type_img);
                holder.fileNameText = convertView.findViewById(R.id.tv_file_name);
                holder.fileSizeText = convertView.findViewById(R.id.tv_file_size);
                holder.fileTimeText = convertView.findViewById(R.id.file_time_text);
                holder.fileInfoLayout = convertView.findViewById(R.id.file_info_layout);
                holder.selectImg = convertView.findViewById(R.id.file_select_img);
                holder.statusLayout = convertView.findViewById(R.id.item_file_load_status);
                holder.progressBar = convertView.findViewById(R.id.item_file_load_progressBar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            displayFiles(holder, position, messageList);
            return convertView;
        }
    }

    class ViewHolder {
        ImageView fileImg;
        TextView fileNameText;
        TextView fileSizeText;
        TextView fileTimeText;
        RelativeLayout fileInfoLayout;
        ImageView selectImg;
        View statusLayout;
        CircleProgressBar progressBar;
    }
}
