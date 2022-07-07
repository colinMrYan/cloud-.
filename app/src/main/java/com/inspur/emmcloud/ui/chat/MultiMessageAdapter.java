package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.MultiMessageItem;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.widget.TextViewFixTouchConsume;
import com.itheima.roundedimageview.RoundedImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MultiMessageAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<MultiMessageItem> uiMessages;
    String cid;

    public MultiMessageAdapter(Context context, ArrayList<MultiMessageItem> uiMessages, String cid) {
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
        switch (item.type) {
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
            case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                holder.message.setVisibility(View.VISIBLE);
                holder.imageLayout.setVisibility(View.GONE);
                holder.fileLayout.setVisibility(View.GONE);
                wrapperTextMessage(holder,item);
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                holder.message.setVisibility(View.GONE);
                holder.fileLayout.setVisibility(View.GONE);
                holder.imageLayout.setVisibility(View.VISIBLE);
                wrapperImageMessages(holder, item);
                break;
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                holder.message.setVisibility(View.GONE);
                holder.imageLayout.setVisibility(View.GONE);
                holder.fileLayout.setVisibility(View.VISIBLE);
                wrapperFileMessages(holder, item);
        }
        holder.time.setText(TimeUtils.getChannelMsgDisplayTime(context, item.sendTime * 1000));
    }

    private void wrapperTextMessage(MultiMessageItemViewHolder holder, MultiMessageItem item) {
        holder.message.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());
        holder.message.setFocusable(false);
        holder.message.setFocusableInTouchMode(false);
        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(item.text, item.transferMessage(cid).getMsgContentTextPlain().getMentionsMap() );
        Spannable span = EmotionUtil.getInstance(context).getSmiledText(spannableString, holder.message.getTextSize());
        holder.message.setText(span);
        TransHtmlToTextUtils.stripUnderlines(
                holder.message, context.getResources().getColor(R.color.header_bg_blue));
    }

    private void wrapperImageMessages(final MultiMessageItemViewHolder viewHolder, final MultiMessageItem item) {
        String imageUri = item.raw.optString("media");
        if (!imageUri.startsWith("http") && !imageUri.startsWith("file:") && !imageUri.startsWith("content:") && !imageUri.startsWith("assets:") && !imageUri.startsWith("drawable:")) {
            imageUri = APIUri.getChatFileResouceUrl(cid, imageUri);
        }
        ImageDisplayUtils.getInstance().displayImage(viewHolder.imageContent, imageUri, ResourceUtils.getResValueOfAttr(context, R.attr.bg_chat_img));
        viewHolder.imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                view.invalidate();
                int width = view.getWidth();
                int height = view.getHeight();
                Intent intent = new Intent(context,
                        ImagePagerActivity.class);
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
    }

    private void wrapperFileMessages(MultiMessageItemViewHolder holder, final MultiMessageItem item) {
        holder.fileIcon.setImageResource(FileUtils.getFileIconResIdByFileName(item.name));
        holder.fileName.setText(item.name);
        holder.fileSize.setText(FileUtils.formatFileSize(item.size));
        holder.fileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fileDownloadPath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_MESSAGE, item.tmpId, item.name);
                if (!StringUtils.isBlank(fileDownloadPath)) {
                    FileUtils.openFile(context, fileDownloadPath);
                } else {
                    Intent intent = new Intent(context, ChatFileDownloadActivtiy.class);
                    intent.putExtra("message", item.transferMessage(cid));
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return uiMessages.size();
    }

    static class MultiMessageItemViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        CircleTextImageView headerImage;
        TextView nameText;
        TextView message;
        TextView time;
        View imageLayout;
        RoundedImageView imageContent;

        View fileLayout;
        ImageView fileIcon;
        TextView fileName;
        TextView fileSize;

        public MultiMessageItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            headerImage = itemView.findViewById(R.id.multi_message_item_header);
            nameText = itemView.findViewById(R.id.multi_message_item_name);
            message = itemView.findViewById(R.id.multi_message_item_message);
            time = itemView.findViewById(R.id.multi_message_item_time);
            imageLayout = itemView.findViewById(R.id.multi_message_item_ll_image);
            imageContent = itemView.findViewById(R.id.content_img);


            fileLayout = itemView.findViewById(R.id.multi_message_item_ll_file);
            fileIcon = itemView.findViewById(R.id.iv_file_icon);
            fileName = itemView.findViewById(R.id.tv_file_name);
            fileSize = itemView.findViewById(R.id.tv_file_size);
        }
    }
}



