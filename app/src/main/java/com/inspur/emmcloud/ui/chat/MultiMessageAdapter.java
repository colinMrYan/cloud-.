package com.inspur.emmcloud.ui.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiUri;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.media.player.VideoPlayerActivity;
import com.inspur.emmcloud.basemodule.media.player.basic.PlayerGlobalConfig;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.MultiMessageItem;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedLinks;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVideo;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.widget.TextViewFixTouchConsume;
import com.itheima.roundedimageview.RoundedImageView;
import com.tencent.rtmp.TXLiveConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.inspur.emmcloud.basemodule.media.record.activity.CommunicationRecordActivity.VIDEO_PATH;
import static com.inspur.emmcloud.basemodule.media.record.activity.CommunicationRecordActivity.VIDEO_THUMBNAIL_PATH;
import static com.inspur.emmcloud.ui.chat.DisplayMediaVideoMsg.formattedTime;

public class MultiMessageAdapter extends RecyclerView.Adapter {

    Activity context;
    ArrayList<MultiMessageItem> uiMessages;
    String cid;

    public MultiMessageAdapter(Activity context, ArrayList<MultiMessageItem> uiMessages, String cid) {
        this.context = context;
        this.uiMessages = uiMessages;
        this.cid = cid;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View item = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.multi_message_item, viewGroup, false);
        return new MultiMessageItemViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        MultiMessageItemViewHolder holder = (MultiMessageItemViewHolder) viewHolder;
        MultiMessageItem item = uiMessages.get(i);

        String photoUriItem = BaseModuleApiUri.getUserPhoto(BaseApplication.getInstance(), item.sendUserId);
        ImageDisplayUtils.getInstance().displayImage(holder.headerImage, photoUriItem, R.drawable.icon_photo_default);
        holder.nameText.setText(item.sendUserName);

        View cardContentView = null;

        switch (item.type) {
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
            case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                cardContentView = getTextViewFromItem(item);
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                cardContentView = getImageViewFromItem(item);
                break;
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                cardContentView = getFileViewFromItem(item);
                break;
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                cardContentView = getLinkedViewFromItem(item);
                break;
            case Message.MESSAGE_TYPE_MEDIA_VIDEO:
                cardContentView = getVideoViewFromItem(item);
                break;
            default:
                break;

        }
        if (cardContentView != null) {
            holder.contentParent.addView(cardContentView);
        }
        holder.time.setText(TimeUtils.getChannelMsgDisplayTime(context, item.sendTime * 1000));
    }

    private View getLinkedViewFromItem(MultiMessageItem item) {
        @SuppressLint("InflateParams") RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(
                R.layout.multi_message_item_linked, null);
        Message message = item.transferMessage(cid);
        final MsgContentExtendedLinks linkedContent = message.getMsgContentExtendedLinks();
        TextView nameView = relativeLayout.findViewById(R.id.tv_linked_name);
        TextView contentView = relativeLayout.findViewById(R.id.tv_linked_content);
        nameView.setText(linkedContent.getTitle());
        contentView.setText(linkedContent.getSubtitle());
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = linkedContent.getUrl();
                boolean showHeader = linkedContent.isShowHeader();
                UriUtils.openUrl(context, url, showHeader);
            }
        });
        return relativeLayout;
    }

    private View getVideoViewFromItem(MultiMessageItem item) {
        @SuppressLint("InflateParams") RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(
                R.layout.multi_message_item_video, null);
        final Message message = item.transferMessage(cid);
        final MsgContentMediaVideo videoContent = message.getMsgContentMediaVideo();
        TextView nameView = relativeLayout.findViewById(R.id.tv_video_name);
        TextView contentView = relativeLayout.findViewById(R.id.tv_video_duration);
        nameView.setText(context.getString(R.string.video));
        contentView.setText(formattedTime(videoContent.getVideoDuration()));
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerGlobalConfig config = PlayerGlobalConfig.getInstance();
                config.renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
                Intent intentVideo = new Intent(context, VideoPlayerActivity.class);
                String path = videoContent.getMedia();
                String videoPath;
                videoPath = APIUri.getECMChatUrl() + "/api/v1/channel/" + cid + "/file/request?path=" + StringUtils.encodeURIComponent(path) + "&inlineContent=true";
                String imagePath = videoContent.getImagePath();
                String mediaPath = StringUtils.isEmpty(videoContent.getOriginMediaPath()) ? message.getLocalPath() : videoContent.getOriginMediaPath();
                intentVideo.putExtra(VIDEO_THUMBNAIL_PATH, !StringUtils.isEmpty(imagePath) && imagePath.startsWith("http") ? imagePath : mediaPath);
                intentVideo.putExtra(VIDEO_PATH, videoPath);
                context.startActivity(intentVideo);
            }
        });
        return relativeLayout;


    }

    private View getTextViewFromItem(MultiMessageItem item) {
        @SuppressLint("InflateParams") TextView message = (TextView) LayoutInflater.from(context).inflate(
                R.layout.multi_message_item_text, null);
        message.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());
        message.setFocusable(false);
        message.setFocusableInTouchMode(false);
        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(item.text, item.transferMessage(cid).getMsgContentTextPlain().getMentionsMap());
        Spannable span = EmotionUtil.getInstance(context).getSmiledText(spannableString, message.getTextSize());
        message.setText(span);
        TransHtmlToTextUtils.stripUnderlines(
                message, context.getResources().getColor(R.color.header_bg_blue));
        return message;
    }

    private View getImageViewFromItem(final MultiMessageItem item) {
        @SuppressLint("InflateParams") RelativeLayout imageLayout = (RelativeLayout) LayoutInflater.from(context).inflate(
                R.layout.multi_message_item_image, null);
        Message message = item.transferMessage(cid);
        RoundedImageView imageContent = imageLayout.findViewById(R.id.content_img);
        String imageUri = message.getMsgContentMediaImage().getRawMedia();
        if (!imageUri.startsWith("http") && !imageUri.startsWith("file:") && !imageUri.startsWith("content:") && !imageUri.startsWith("assets:") && !imageUri.startsWith("drawable:")) {
            imageUri = APIUri.getChatFileResouceUrl(cid, imageUri);
        }
        ImageDisplayUtils.getInstance().displayImage(imageContent, imageUri, ResourceUtils.getResValueOfAttr(context, R.attr.bg_chat_img));
        imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                view.invalidate();
                int width = view.getWidth();
                int height = view.getHeight();
                Intent intent = new Intent(context,
                        ImagePagerNewActivity.class);
                Message message = item.transferMessage(cid);
                List<Message> imgTypeMsgList = new ArrayList<>();
                imgTypeMsgList.add(message);
                intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_MSG_LIST, (Serializable) imgTypeMsgList);
                intent.putExtra(ImagePagerActivity.EXTRA_CURRENT_IMAGE_MSG, message);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_X_TAG, location[0]);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_Y_TAG, location[1]);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_W_TAG, width);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_H_TAG, height);
                intent.putExtra(ImagePagerActivity.EXTRA_CHANNEL_ID, cid);
                intent.putExtra(ImagePagerActivity.EXTRA_NEED_SHOW_COMMENT, false);
                context.startActivity(intent);
            }
        });
        return imageLayout;
    }


    private View getFileViewFromItem(final MultiMessageItem item) {
        @SuppressLint("InflateParams") RelativeLayout fileLayout = (RelativeLayout) LayoutInflater.from(context).inflate(
                R.layout.multi_message_item_file, null);
        Message message = item.transferMessage(cid);
        final MsgContentRegularFile fileContent = message.getMsgContentAttachmentFile();

        ImageView fileIcon = fileLayout.findViewById(R.id.iv_file_icon);
        final TextView fileName = fileLayout.findViewById(R.id.tv_file_name);
        TextView fileSize = fileLayout.findViewById(R.id.tv_file_size);

        fileIcon.setImageResource(FileUtils.getFileIconResIdByFileName(fileContent.getName()));
        fileName.setText(fileContent.getName());
        fileSize.setText(FileUtils.formatFileSize(fileContent.getSize()));
        fileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fileDownloadPath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_MESSAGE, item.tmpId, fileContent.getName());
                if (!StringUtils.isBlank(fileDownloadPath)) {
                    FileUtils.openFile(context, fileDownloadPath);
                } else {
                    Intent intent = new Intent(context, ChatFileDownloadActivtiy.class);
                    intent.putExtra("message", item.transferMessage(cid));
                    context.startActivity(intent);
                }
            }
        });
        return fileLayout;
    }


    @Override
    public int getItemCount() {
        return uiMessages.size();
    }

    static class MultiMessageItemViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        CircleTextImageView headerImage;
        TextView nameText;
        RelativeLayout contentParent;
        TextView time;

        public MultiMessageItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            headerImage = itemView.findViewById(R.id.multi_message_item_header);
            nameText = itemView.findViewById(R.id.multi_message_item_name);
            time = itemView.findViewById(R.id.multi_message_item_time);
            contentParent = itemView.findViewById(R.id.multi_message_item_content_parent);
        }
    }
}



