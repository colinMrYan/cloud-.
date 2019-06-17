package com.inspur.emmcloud.ui.chat;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
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
import com.inspur.emmcloud.adapter.VolumeFileFilterPopGridAdapter;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.GroupFileInfo;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;
import com.inspur.emmcloud.widget.HorizontalProgressBarWithNumber;

import java.io.File;
import java.text.Collator;
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
    private String cid;
    private List<GroupFileInfo> fileInfoList = new ArrayList<>();
    private PopupWindow sortOperationPop;
    private GroupFileAdapter adapter;
    private FileSortComparable fileSortComparable;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        cid = getIntent().getExtras().getString("cid");
        getFileMsgList();
        noChannelFileLayout.setVisibility(fileInfoList.size() == 0 ? View.VISIBLE : View.GONE);
        fileSortComparable = new FileSortComparable();
        Collections.sort(fileInfoList, fileSortComparable);
        adapter = new GroupFileAdapter();
        fileListView.setAdapter(adapter);
        adapter.setAndReFreshList(fileInfoList);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_file;
    }

    private void getFileMsgList() {
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            List<Msg> fileTypeMsgList = MsgCacheUtil.getFileTypeMsgList(
                    GroupFileActivity.this, cid);
            for (Msg msg : fileTypeMsgList) {
                GroupFileInfo groupFileInfo = new GroupFileInfo(msg);
                fileInfoList.add(groupFileInfo);
            }
        } else {
            List<Message> fileTypeMessageList = MessageCacheUtil.getFileTypeMsgList(MyApplication.getInstance(), cid);
            for (Message message : fileTypeMessageList) {
                MsgContentRegularFile msgContentRegularFile = message.getMsgContentAttachmentFile();
                String url = APIUri.getChatFileResouceUrl(message.getChannel(), msgContentRegularFile.getMedia());
                GroupFileInfo groupFileInfo = new GroupFileInfo(url, msgContentRegularFile.getName(), msgContentRegularFile.getSize() + "", message.getCreationDate(), ContactUserCacheUtils.getUserName(message.getFromUser()));
                fileInfoList.add(groupFileInfo);
            }
        }
    }

    private void showSortOperationPop() {
        View contentView = LayoutInflater.from(GroupFileActivity.this)
                .inflate(R.layout.app_volume_file_sort_operation_pop, null);
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
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_volume_menu_drop_up);
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
        Drawable drawableDown = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_volume_menu_drop_down);
        drawableDown.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 14), DensityUtil.dip2px(getApplicationContext(), 14));
        operationSortText.setCompoundDrawables(null, null, drawableDown, null);
        String sortTypeShowTxt;
        switch (sortType) {
            case SORT_BY_NAME_DOWN:
                sortTypeShowTxt = getString(R.string.clouddriver_sort_by_name_dasc);
                break;
            case SORT_BY_TIME_UP:
                sortTypeShowTxt = getString(R.string.clouddriver_sort_by_time_asc);
                break;
            case SORT_BY_TIME_DOWN:
                sortTypeShowTxt = getString(R.string.clouddriver_sort_by_time_dasc);
                break;
            default:
                sortTypeShowTxt = getString(R.string.clouddriver_sort_by_name_asc);
                break;
        }
        operationSortText.setText(sortTypeShowTxt);
        Collections.sort(fileInfoList, fileSortComparable);
        adapter.setAndReFreshList(fileInfoList);
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
                .inflate(R.layout.app_volume_file_filter_pop, null);
        final PopupWindow fileFilterPop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        GridView fileFilterGrid = contentView.findViewById(R.id.file_filter_type_grid);
        fileFilterGrid.setAdapter(new VolumeFileFilterPopGridAdapter(GroupFileActivity.this));
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
        Drawable drawableUp = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_volume_menu_drop_up);
        drawableUp.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 14), DensityUtil.dip2px(getApplicationContext(), 14));
        filterByFileTypeText.setCompoundDrawables(null, null, drawableUp, null);
        fileFilterPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Drawable drawableDown = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_volume_menu_drop_down);
                drawableDown.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 14), DensityUtil.dip2px(getApplicationContext(), 14));
                filterByFileTypeText.setCompoundDrawables(null, null, drawableDown, null);
            }
        });
    }

    private void filterFilesByFileType(String fileFilterType) {
        List<GroupFileInfo> fileInfoFilterList = new ArrayList<>();
        fileInfoFilterList.clear();
        for (GroupFileInfo groupFileInfo : fileInfoList) {
            if (FileUtils.getFileTypeByName(groupFileInfo.getName()).equals(fileFilterType)) {
                fileInfoFilterList.add(groupFileInfo);
            }
        }
        adapter.setAndReFreshList(fileInfoFilterList);
    }

    public void displayFiles(View convertView, int position, List<GroupFileInfo> groupFileInfoList) {
        ImageView fileImg = convertView.findViewById(R.id.file_img);
        TextView fileNameText = convertView.findViewById(R.id.tv_file_name);
        TextView fileSizeText = convertView.findViewById(R.id.tv_file_size);
        TextView fileTimeText = convertView.findViewById(R.id.file_time_text);
        TextView fileMonthText = convertView.findViewById(R.id.tv_file_month);
        convertView.findViewById(R.id.v_line).setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        final HorizontalProgressBarWithNumber progressBar = convertView.findViewById(R.id.file_download_progressbar);
        GroupFileInfo groupFileInfo = groupFileInfoList.get(position);
        final String fileName = groupFileInfo.getName();
        final String source = groupFileInfo.getUrl();
        fileNameText.setText(fileName);
        fileSizeText.setText(groupFileInfo.getSize());
        if (sortType.equals(SORT_BY_TIME_DOWN) || sortType.equals(SORT_BY_TIME_UP)) {
            String currentTime = TimeUtils.timeLong2YMString(GroupFileActivity.this, groupFileInfo.getLongTime());
            if (position >= 1) {
                String lastTime = TimeUtils.timeLong2YMString(GroupFileActivity.this, groupFileInfoList.get(position - 1).getLongTime());
                fileMonthText.setVisibility(!lastTime.equals(currentTime) ? View.VISIBLE : View.GONE);
                fileMonthText.setText(currentTime);
            } else {
                fileMonthText.setText(currentTime.equals(TimeUtils.timeLong2YMString(GroupFileActivity.this, groupFileInfo.getLongTime())) ? getString(R.string.current_month) : currentTime);
            }
        } else {
            fileMonthText.setVisibility(View.GONE);
        }
        fileTimeText.setText(TimeUtils.getChannelMsgDisplayTime(GroupFileActivity.this, groupFileInfo.getLongTime()));
        fileImg.setImageResource(FileUtils.getRegularFileIconResId(fileName));
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当文件正在下载中 点击不响应
                final String fileDownloadPath = MyAppConfig.LOCAL_DOWNLOAD_PATH + fileName;
                progressBar.setTag(fileDownloadPath);
                // 当文件正在下载中 点击不响应
                if ((0 < progressBar.getProgress())
                        && (progressBar.getProgress() < 100)) {
                    return;
                }
                APIDownloadCallBack progressCallback = new APIDownloadCallBack(GroupFileActivity.this, source) {
                    @Override
                    public void callbackStart() {
                        if ((progressBar.getTag() != null)
                                && (progressBar.getTag() == fileDownloadPath)) {

                            progressBar.setVisibility(View.VISIBLE);
                        } else {
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void callbackLoading(long total, long current, boolean isUploading) {
                        if (total == 0) {
                            total = 1;
                        }
                        int progress = (int) ((current * 100) / total);
                        if (!(progressBar.getVisibility() == View.VISIBLE)) {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                        progressBar.setProgress(progress);
                        progressBar.refreshDrawableState();
                    }

                    @Override
                    public void callbackSuccess(File file) {
                        progressBar.setVisibility(View.GONE);
                        ToastUtils.show(getApplicationContext(), R.string.filetransfer_download_success);
                    }

                    @Override
                    public void callbackError(Throwable arg0, boolean arg1) {
                        progressBar.setVisibility(View.GONE);
                        ToastUtils.show(getApplicationContext(), R.string.filetransfer_download_failed);
                    }

                    @Override
                    public void callbackCanceled(CancelledException e) {

                    }
                };
                if (FileUtils.isFileExist(fileDownloadPath)) {
                    FileUtils.openFile(getApplicationContext(), fileDownloadPath);
                } else {
                    new DownLoaderUtils().startDownLoad(source, fileDownloadPath, progressCallback);
                }
            }
        });
    }

    private class FileSortComparable implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            GroupFileInfo groupFileInfoA = (GroupFileInfo) o1;
            GroupFileInfo groupFileInfoB = (GroupFileInfo) o2;
            int sortResult = 0;
            switch (sortType) {
                case SORT_BY_NAME_UP:
                    sortResult = Collator.getInstance(Locale.CHINA).compare(groupFileInfoA.getName(), groupFileInfoB.getName());
                    break;
                case SORT_BY_NAME_DOWN:
                    sortResult = 0 - Collator.getInstance(Locale.CHINA).compare(groupFileInfoA.getName(), groupFileInfoB.getName());
                    break;
                case SORT_BY_TIME_DOWN:
                    if (groupFileInfoA.getLongTime() == groupFileInfoB.getLongTime()) {
                        sortResult = 0;
                    } else if (groupFileInfoA.getLongTime() < groupFileInfoB.getLongTime()) {
                        sortResult = 1;
                    } else {
                        sortResult = -1;
                    }
                    break;
                case SORT_BY_TIME_UP:
                    if (groupFileInfoA.getLongTime() == groupFileInfoB.getLongTime()) {
                        sortResult = 0;
                    } else if (groupFileInfoA.getLongTime() < groupFileInfoB.getLongTime()) {
                        sortResult = -1;
                    } else {
                        sortResult = 1;
                    }
                    break;
                default:
                    sortResult = Collator.getInstance(Locale.CHINA).compare(groupFileInfoA.getName(), groupFileInfoB.getName());
                    break;
            }
            return sortResult;
        }
    }

    private class GroupFileAdapter extends BaseAdapter {
        private List<GroupFileInfo> groupFileInfoList = new ArrayList<>();

        @Override
        public int getCount() {
            return groupFileInfoList.size();
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
            convertView = LayoutInflater.from(GroupFileActivity.this).inflate(R.layout.group_file_item_view, null);
            displayFiles(convertView, position, groupFileInfoList);
            return convertView;
        }

        public void setAndReFreshList(List<GroupFileInfo> groupFileInfoList) {
            this.groupFileInfoList = groupFileInfoList;
            notifyDataSetChanged();
        }
    }
}
