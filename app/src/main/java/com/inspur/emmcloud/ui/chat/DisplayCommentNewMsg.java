package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.media.selector.demo.GlideEngine;
import com.inspur.emmcloud.basemodule.media.selector.thread.PictureThreadUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentComment;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVideo;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.widget.TextViewWithSpan;
import com.itheima.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author wz 展示评论卡片
 */
public class DisplayCommentNewMsg {

    /**
     * 评论卡片
     *
     * @param context
     * @param message
     * @return
     */
    public static View getView(final Activity context, final Message message) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_text_comment_new_view, null);
        boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        BubbleLayout cardLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : ResourceUtils.getResValueOfAttr(context, R.attr.bubble_bg_color)));
        cardLayout.setStrokeWidth(isMyMsg ? 0 : 0.5f);
        TextView nameTv = (TextView) cardContentView
                .findViewById(R.id.tv_name);
        TextView originTv = (TextView) cardContentView
                .findViewById(R.id.tv_origin);
        RoundedImageView originIv = (RoundedImageView) cardContentView
                .findViewById(R.id.iv_origin);
        RelativeLayout videoRl = (RelativeLayout) cardContentView
                .findViewById(R.id.rl_video);
        RoundedImageView videoIv = (RoundedImageView) cardContentView
                .findViewById(R.id.iv_video);
        View dividerView = (View) cardContentView
                .findViewById(R.id.view_divider);
        final TextViewWithSpan commentContentText = (TextViewWithSpan) cardContentView
                .findViewById(R.id.tv_comment);
        MsgContentComment msgContentComment = message.getMsgContentComment();
        String text = msgContentComment.getText();

        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(text, message.getMsgContentTextPlain().getMentionsMap());
        Spannable span = EmotionUtil.getInstance(context).getSmiledText(spannableString, nameTv.getTextSize());
        commentContentText.setText(span);
        TransHtmlToTextUtils.stripUnderlines(
                commentContentText, context.getResources().getColor(isMyMsg ? R.color.hightlight_in_blue_bg
                        : R.color.header_bg_blue));
        // 回复消息颜色
        commentContentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : ResourceUtils.getResValueOfAttr(context, R.attr.text_color_e1)));
        // 原始消息颜色
        nameTv.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.color_tran80_ff : ResourceUtils.getResValueOfAttr(context, R.attr.color_99_66)));
        originTv.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.color_tran80_ff : ResourceUtils.getResValueOfAttr(context, R.attr.color_99_66)));
        // 分割线背景色
        dividerView.setBackgroundColor(context.getResources().getColor(
                isMyMsg ? R.color.color_tran30_ff : ResourceUtils.getResValueOfAttr(context, R.attr.color_40_de)));

        // 获取原消息，根据原消息文字，图片，视频，文件显示不同UI
        Message commentedMessage = MessageCacheUtil.getMessageByMid(MyApplication.getInstance(), msgContentComment.getMessage());
        if (commentedMessage != null) {
            String userName = ContactUserCacheUtils.getUserName(commentedMessage.getFromUser()) + ":";
            nameTv.setText(userName);
//            commentTitleText.setText(getCommentTitle(context, commentedMessage, isMyMsg));
            String commentedMessageType = commentedMessage.getType();
            switch (commentedMessageType) {
                case "file/regular-file":
                    originTv.setVisibility(View.VISIBLE);
                    originIv.setVisibility(View.GONE);
                    videoRl.setVisibility(View.GONE);
                    String fileName = context.getResources().getString(R.string.send_a_file) +
                            commentedMessage.getMsgContentAttachmentFile().getName();
                    originTv.setText(fileName);
                    break;
                case "media/image":
                    originTv.setVisibility(View.GONE);
                    originIv.setVisibility(View.VISIBLE);
                    videoRl.setVisibility(View.GONE);
                    showImageView(commentedMessage, context, originIv);
                    break;
                case "media/video":
                    originTv.setVisibility(View.GONE);
                    originIv.setVisibility(View.GONE);
                    videoRl.setVisibility(View.VISIBLE);
                    showVideoView(commentedMessage, context, videoIv);
                    break;
                default:
                    originTv.setVisibility(View.VISIBLE);
                    originIv.setVisibility(View.GONE);
                    videoRl.setVisibility(View.GONE);
                    originTv.setText(commentedMessage.getMsgContentTextPlain().getText());
                    break;
            }
        } else {
            WSAPIService.getInstance().getMessageById(msgContentComment.getMessage());
        }

        return cardContentView;
    }

    private static void showVideoView(final Message commentedMessage, final Context context, final RoundedImageView videoIv) {
        MsgContentMediaVideo msgContentMediaVideo = commentedMessage.getMsgContentMediaVideo();
        final int chatImgBg = ResourceUtils.getResValueOfAttr(context, R.attr.bg_chat_img);
        String imagePath = msgContentMediaVideo.getImagePath();
        String localPath = commentedMessage.getLocalPath();
        // 视频压缩时，获取原路径，Glide缓存key为原路径
        if (!TextUtils.isEmpty(msgContentMediaVideo.getOriginMediaPath())) {
            localPath = msgContentMediaVideo.getOriginMediaPath();
        }
        // 先使用缓存，再使用本地加载，再请求网络
        if (imagePath.startsWith("http")) {
            GlideEngine.createGlideEngine().loadVideoThumbnailImage(context, imagePath, 0, 0, videoIv, chatImgBg, new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    if (e != null && e.getMessage().contains("FileNotFoundException")) {
                        // 防止Glide缓存失效，重新请求
                        JSONObject object = JSONUtils.getJSONObject(commentedMessage.getContent());
                        JSONObject picInfo = JSONUtils.getJSONObject(object, "thumbnail", new JSONObject());
                        try {
                            picInfo.put("media", "");
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                        commentedMessage.setContent(object.toString());
                        MessageCacheUtil.saveMessage(context, commentedMessage);
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            });
        } else {
            // 防止图片本地被删除，不为空再加载本地资源
            if (!TextUtils.isEmpty(localPath)) {
                GlideEngine.createGlideEngine().loadVideoThumbnailImage(context, localPath, 0, 0, videoIv, chatImgBg, new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        commentedMessage.setLocalPath("");
                        loadVideoCover(context, videoIv, chatImgBg, commentedMessage);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                });
            } else {
                loadVideoCover(context, videoIv, chatImgBg, commentedMessage);
            }
        }
    }

    // 重构可替换成Glide加载
    private static void showImageView(Message imageMessage, Context context, ImageView imageView) {
        int chatImgBg = ResourceUtils.getResValueOfAttr(context, R.attr.bg_chat_img);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(chatImgBg)
                .showImageOnFail(chatImgBg)
                .showImageOnLoading(chatImgBg)
                .considerExifParams(true)
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true)
                .cacheOnDisk(true).build();
        MsgContentMediaImage msgContentMediaImage = imageMessage.getMsgContentMediaImage();
        String imageUri = msgContentMediaImage.getRawMedia();
        if (!imageUri.startsWith("http") && !imageUri.startsWith("file:") && !imageUri.startsWith("content:") && !imageUri.startsWith("assets:") && !imageUri.startsWith("drawable:")) {
            imageUri = APIUri.getChatFileResouceUrl(imageMessage.getChannel(), imageUri);
        }
        if (!ImageDisplayUtils.getInstance().isHaveCacheImage(imageUri) && imageUri.startsWith("http") &&
                msgContentMediaImage.getPreviewHeight() != 0
                && (msgContentMediaImage.getRawHeight() != msgContentMediaImage.getPreviewHeight())) {
            imageUri = imageUri + "&resize=true&w=" + imageMessage.getMsgContentMediaImage().getPreviewWidth() +
                    "&h=" + imageMessage.getMsgContentMediaImage().getPreviewHeight();
        }
        ImageLoader.getInstance().displayImage(imageUri, imageView, options, null);
    }

    private static void loadVideoCover(final Context context, final ImageView imageView, final int chatImgBg, final Message message) {
        imageView.setImageResource(chatImgBg);
        BaseModuleApiService appAPIService = new BaseModuleApiService(context);
        appAPIService.setAPIInterface(new BaseModuleAPIInterfaceInstance() {
            @Override
            public void returnVideoSuccess(final String url) {
                PictureThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject object = JSONUtils.getJSONObject(message.getContent());
                        JSONObject picInfo = JSONUtils.getJSONObject(object, "thumbnail", new JSONObject());
                        try {
                            picInfo.put("media", url);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        message.setContent(object.toString());
                        MessageCacheUtil.saveMessage(context, message);
                        GlideEngine.createGlideEngine().loadVideoThumbnailImage(context, url, 0, 0, imageView, chatImgBg, null);
                    }
                });
            }

            @Override
            public void returnVideoFail(String error, int errorCode) {
            }

        });
        String originUlr = APIUri.getECMChatUrl() + "/api/v1/channel/" + message.getChannel() + "/file/request?path=" + StringUtils.encodeURIComponent(message.getMsgContentMediaVideo().getMedia()) + "&inlineContent=true";
        appAPIService.getVideoUrl(originUlr);
    }
}
