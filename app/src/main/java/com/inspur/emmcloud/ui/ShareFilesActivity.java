package com.inspur.emmcloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.ui.appcenter.volume.VolumeHomePageActivity;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.chat.mvp.view.ConversationSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.TabAndAppExistUtils;
import com.itheima.roundedimageview.RoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/5/12.
 */
public class ShareFilesActivity extends BaseActivity {

    private final static int SHARE_IMAGE_OR_FILES = 0;
    private final static int SHARE_FROM_RECENT_CHAT = 1;    //分享到最近聊天
    private final static int SHARE_FILES_LIMIT = 5;
    @BindView(R.id.rv_file_list)
    RecyclerView recyclerView;
    @BindView(R.id.img_file_icon)
    ImageView imageView;
    @BindView(R.id.rl_channel_share)
    RelativeLayout channelRelativeLayout;
    @BindView(R.id.rl_volume_share)
    RelativeLayout volumeRelativeLayout;
    @BindView(R.id.view_line_volume)
    View viewLineVolume;
    @BindView(R.id.tv_img_file_name)
    TextView imageFileNameTextView;
    @BindView(R.id.rl_file)
    RelativeLayout fileLayout;
    @BindView(R.id.rl_image)
    RelativeLayout imageLayout;
    @BindView(R.id.iv_file_icon)
    ImageView fileImageView;
    @BindView(R.id.tv_file_name)
    TextView fileTextView;
    private List<String> uriList = new ArrayList<>();

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        this.uriList.addAll((List<String>) getIntent().getSerializableExtra(Constant.SHARE_FILE_URI_LIST));
        if (uriList == null || uriList.size() == 0) {
            ToastUtils.show(ShareFilesActivity.this, getString(R.string.baselib_share_fail));
            finish();
        }
        if (uriList.size() > SHARE_FILES_LIMIT) {
            ToastUtils.show(ShareFilesActivity.this, getString(R.string.share_no_more_than_five));
            finish();
        }
        initViews();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.uriList.clear();
        this.uriList.addAll((List<String>) intent.getSerializableExtra(Constant.SHARE_FILE_URI_LIST));
        if (uriList == null || uriList.size() == 0) {
            ToastUtils.show(ShareFilesActivity.this, getString(R.string.baselib_share_fail));
            finish();
        }
        if (uriList.size() > SHARE_FILES_LIMIT) {
            ToastUtils.show(ShareFilesActivity.this, getString(R.string.share_no_more_than_five));
            finish();
        }
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_share_files;
    }

    /**
     * 分享方式
     */
    private void initSharingMode() {
        boolean isCommunicateExist = TabAndAppExistUtils.isTabExist(MyApplication.getInstance(), Constant.APP_TAB_BAR_COMMUNACATE);
        boolean isVolumeAppExist = TabAndAppExistUtils.isAppExist(MyApplication.getInstance(), "emm://volume");
        channelRelativeLayout.setVisibility(isCommunicateExist ? View.VISIBLE : View.GONE);
        volumeRelativeLayout.setVisibility(isVolumeAppExist ? View.VISIBLE : View.GONE);
        viewLineVolume.setVisibility((isCommunicateExist && isVolumeAppExist) ? View.VISIBLE : View.GONE);
        if (!(isCommunicateExist || isVolumeAppExist)) {
            ToastUtils.show(MyApplication.getInstance(), getString(R.string.share_no_share_way));
        }
    }

    private void showImageLayout(String filePath) {
        imageLayout.setVisibility(View.VISIBLE);
        fileLayout.setVisibility(View.GONE);
        ImageDisplayUtils.getInstance().displayImage(imageView, uriList.get(0).toString(), R.drawable.default_image);
    }

    private void showFileLayout(String filePath) {
        fileLayout.setVisibility(View.VISIBLE);
        imageLayout.setVisibility(View.GONE);
        if (!StringUtils.isBlank(filePath) && filePath.contains("/")) {
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
            fileTextView.setText(fileName);
            fileTextView.setVisibility(View.VISIBLE);
        } else {
            fileTextView.setVisibility(View.GONE);
        }
        fileImageView.setImageResource(FileUtils.getFileIconResIdByFilePath(filePath));
    }

    /**
     * 初始化
     */
    private void initViews() {
        initSharingMode();
        ImageDisplayUtils.getInstance().displayImage(imageView, TabAndAppExistUtils.getVolumeIconUrl(MyApplication.getInstance(),
                "emm://volume"), R.drawable.ic_app_default);
        int uriListSize = uriList.size();
        switch (uriListSize) {
            case 0:
                finish();
                break;
            case 1:
                recyclerView.setVisibility(View.GONE);
                String filePath = uriList.get(0);
                if (!isImageUriList(uriList)) {
                    showFileLayout(filePath);
                } else {
                    showImageLayout(filePath);
                }
                break;
            default:
                recyclerView.setVisibility(View.VISIBLE);
                imageLayout.setVisibility(View.GONE);
                fileLayout.setVisibility(View.GONE);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(MyApplication.getInstance(), getGridViewColumn());
                recyclerView.setLayoutManager(gridLayoutManager);
                if (isImageUriList(uriList)) {
                    recyclerView.setAdapter(new ShareImagesAdapter());
                } else {
                    recyclerView.setAdapter(new ShareFilesAdapter());
                }
                break;
        }

    }

    /**
     * 根据文件列数修改
     */
    private int getGridViewColumn() {
        if (uriList.size() == 2 || uriList.size() == 4) {
            return 2;
        }
        return 3;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_channel_share:
                shareFilesToFriends(); //分享到聊天
                break;
            case R.id.rl_volume_share:
                startVolumeShareActivity(uriList); //分享到网盘
                break;
        }
    }

    /**
     * 判断是ImageUri
     *
     * @param uriList
     * @return
     */
    private boolean isImageUriList(List<String> uriList) {
        for (int i = 0; i < uriList.size(); i++) {
            String uriString = uriList.get(i).toString().toLowerCase();
            if (!uriString.endsWith("png") && !uriString.endsWith("jpg") && !uriString.endsWith("jpeg") && !uriString.endsWith("dng")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 分享功能中不再判断文件来源（例如来自文件系统还是图库）
     *
     * @param uriList
     */
    private void startVolumeShareActivity(List<String> uriList) {
        if (FileUtils.isFileInListExist(uriList)) {
            //提前一步清空上次一分享未成功的页面残余
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_VOLUME_FILE_LOCATION_SELECT_CLOSE));
            Intent intent = new Intent();
            intent.setClass(ShareFilesActivity.this, VolumeHomePageActivity.class);
            intent.putExtra(Constant.SHARE_FILE_URI_LIST, (Serializable) uriList);
            startActivity(intent);
        } else {
            ToastUtils.show(MyApplication.getInstance(), getString(R.string.share_has_not_exist_file));
        }
        finish();
    }

    /**
     * 给朋友分享图片或文件
     */
    private void shareFilesToFriends() {
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            Intent intent = new Intent();
            String firstFileName = "";
            File file = new File(uriList.get(0));
            firstFileName = file.getName();
            String shareManyPictures = getResources().getString(isImageUriList(uriList) ? R.string.baselib_share_many_picture : R.string.baselib_share_many_files, uriList.size());
            String fileType = isImageUriList(uriList) ? getString(R.string.baselib_share_image) : getString(R.string.baselib_share_file);
            String detailResult = fileType + " " + firstFileName + (uriList.size() > 1 ? shareManyPictures : "");
            intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 0);
            intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, false);
            intent.putExtra(ContactSearchFragment.EXTRA_SHOW_COMFIRM_DIALOG_WITH_MESSAGE, detailResult);
            intent.putExtra(ContactSearchFragment.EXTRA_SHOW_COMFIRM_DIALOG, true);
            ArrayList<String> uidList = new ArrayList<>();
            uidList.add(MyApplication.getInstance().getUid());
            intent.putStringArrayListExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, uidList);
            intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.baselib_share_to));
            intent.setClass(getApplicationContext(),
                    ContactSearchActivity.class);
            startActivityForResult(intent, SHARE_IMAGE_OR_FILES);
        } else {
            File file = new File(uriList.get(0));
            String firstFileName = file.getName();
            String shareManyPictures = getResources().getString(isImageUriList(uriList) ? R.string.baselib_share_many_picture : R.string.baselib_share_many_files, uriList.size());
            String fileType = isImageUriList(uriList) ? getString(R.string.baselib_share_image) : getString(R.string.baselib_share_file);
            String shareContent = fileType + " " + firstFileName + (uriList.size() > 1 ? shareManyPictures : "");
            Intent shareIntent = new Intent(this, ConversationSearchActivity.class);
            shareIntent.putExtra(Constant.SHARE_CONTENT, shareContent);

            startActivityForResult(shareIntent, SHARE_FROM_RECENT_CHAT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("zhang", "ShareFile onActivityResult: ");
        super.onActivityResult(requestCode, resultCode, data);
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            if (requestCode == SHARE_IMAGE_OR_FILES && resultCode == RESULT_OK
                    && NetUtils.isNetworkConnected(getApplicationContext())) {
                String result = data.getStringExtra("searchResult");
                try {
                    String userOrChannelId = "";
                    boolean isGroup = false;
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("people")) {
                        JSONArray peopleArray = jsonObject.getJSONArray("people");
                        if (peopleArray.length() > 0) {
                            JSONObject peopleObj = peopleArray.getJSONObject(0);
                            userOrChannelId = peopleObj.getString("pid");
                            isGroup = false;
                        }
                    }

                    if (jsonObject.has("channelGroup")) {
                        JSONArray channelGroupArray = jsonObject
                                .getJSONArray("channelGroup");
                        if (channelGroupArray.length() > 0) {
                            JSONObject cidObj = channelGroupArray.getJSONObject(0);
                            userOrChannelId = cidObj.getString("cid");
                            isGroup = true;
                        }
                    }
                    if (StringUtils.isBlank(userOrChannelId)) {
                        ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
                    } else {
                        if (isGroup) {
                            startChannelActivity(userOrChannelId);
                        } else {
                            createDirectChannel(userOrChannelId);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
                }
            }
        } else {
            if (resultCode != RESULT_OK || !NetUtils.isNetworkConnected(getApplicationContext())) {
                return;
            }
            if (requestCode == SHARE_IMAGE_OR_FILES) {
                String result = data.getStringExtra("searchResult");
                try {
                    String userOrChannelId = "";
                    boolean isGroup = false;
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("people")) {
                        JSONArray peopleArray = jsonObject.getJSONArray("people");
                        if (peopleArray.length() > 0) {
                            JSONObject peopleObj = peopleArray.getJSONObject(0);
                            userOrChannelId = peopleObj.getString("pid");
                            isGroup = false;
                        }
                    }

                    if (jsonObject.has("channelGroup")) {
                        JSONArray channelGroupArray = jsonObject
                                .getJSONArray("channelGroup");
                        if (channelGroupArray.length() > 0) {
                            JSONObject cidObj = channelGroupArray.getJSONObject(0);
                            userOrChannelId = cidObj.getString("cid");
                            isGroup = true;
                        }
                    }

                    share2Conversation(userOrChannelId, isGroup);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
                }
            } else if (requestCode == SHARE_FROM_RECENT_CHAT) {
                SearchModel searchModel = (SearchModel) data.getSerializableExtra("searchModel");
                if (searchModel != null) {
                    String userOrChannelId = searchModel.getId();
                    boolean isUser = searchModel.getType().equals(SearchModel.TYPE_USER);
                    share2Conversation(userOrChannelId, isUser);
                }
            }
        }
    }

    /**
     * 分享到聊天界面
     *
     * @param userOrChannelId
     * @param isUser
     */
    private void share2Conversation(String userOrChannelId, boolean isUser) {
        if (StringUtils.isBlank(userOrChannelId)) {
            ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
        } else {
            if (isUser) {
                createDirectChannel(userOrChannelId);
            } else {
                startChannelActivity(userOrChannelId);
            }
        }
    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        bundle.putString("share_type", isImageUriList(uriList) ? "image" : "file");
        bundle.putSerializable("share_paths", (Serializable) uriList);
        IntentUtils.startActivity(ShareFilesActivity.this, WebServiceRouterManager.getInstance().isV0VersionChat() ?
                ChannelV0Activity.class : ConversationActivity.class, bundle, true);
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(ShareFilesActivity.this, uid,
                    new ConversationCreateUtils.OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            startChannelActivity(conversation.getId());
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        } else {
            new ChatCreateUtils().createDirectChannel(ShareFilesActivity.this, uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            startChannelActivity(getCreateSingleChannelResult.getCid());
                        }

                        @Override
                        public void createDirectChannelFail() {
                        }
                    });
        }

    }


    private void showFileIcon(ImageView imageView, String filePath) {
        int iconResId = FileUtils.getFileIconResIdByFilePath(filePath);
        if (iconResId == R.drawable.baselib_file_type_img) {
            ImageDisplayUtils.getInstance().displayImage(imageView, filePath, R.drawable.default_image);
        } else {
            imageView.setImageResource(iconResId);
        }
    }


    /**
     * 多图片适配器
     */
    class ShareImagesAdapter extends RecyclerView.Adapter<FileHolder> {
        LayoutInflater inflater;

        public ShareImagesAdapter() {
            inflater = LayoutInflater.from(ShareFilesActivity.this);
        }

        @Override
        public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.share_image_item, null);
            FileHolder holder = new FileHolder(view);
            holder.imageView = view.findViewById(R.id.img_share_file);
            return holder;
        }

        @Override
        public void onBindViewHolder(FileHolder holder, int position) {
            showFileIcon(holder.imageView, uriList.get(position));
        }

        @Override
        public int getItemCount() {
            return uriList.size();
        }
    }

    /**
     * 多文件适配器
     */
    class ShareFilesAdapter extends RecyclerView.Adapter<FileHolder> {
        LayoutInflater inflater;

        public ShareFilesAdapter() {
            inflater = LayoutInflater.from(ShareFilesActivity.this);
        }

        @Override
        public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.share_file_item, null);
            FileHolder holder = new FileHolder(view);
            holder.imageView = view.findViewById(R.id.iv_file_ic);
            holder.textView = view.findViewById(R.id.tv_file_name);
            return holder;
        }

        @Override
        public void onBindViewHolder(FileHolder holder, int position) {
            showFileIcon(holder.imageView, uriList.get(position));
            // holder.imageView.setImageResource(R.drawable.default_image);
            // Bitmap bm = BitmapFactory.decodeFile(path);
            // image2.setImageBitmap(bm);//不会变形
            String filePath = uriList.get(position);
            if (!StringUtils.isBlank(filePath) && filePath.contains("/")) {
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
                holder.textView.setText(fileName);
            } else {
                holder.textView.setText(R.string.no_file);
            }
        }

        @Override
        public int getItemCount() {
            return uriList.size();
        }
    }

    class FileHolder extends RecyclerView.ViewHolder {
        private RoundedImageView imageView;
        private TextView textView;

        public FileHolder(View itemView) {
            super(itemView);
        }
    }
}
