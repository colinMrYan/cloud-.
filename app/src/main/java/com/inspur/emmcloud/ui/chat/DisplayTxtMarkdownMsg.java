package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.bean.chat.MarkDownLink;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.cache.MarkDownLinkCacheUtils;
import com.inspur.emmcloud.util.privates.richtext.CacheType;
import com.inspur.emmcloud.util.privates.richtext.ImageHolder;
import com.inspur.emmcloud.util.privates.richtext.LinkHolder;
import com.inspur.emmcloud.util.privates.richtext.RichText;
import com.inspur.emmcloud.util.privates.richtext.RichTextConfig;
import com.inspur.emmcloud.util.privates.richtext.RichType;
import com.inspur.emmcloud.util.privates.richtext.callback.DrawableGetter;
import com.inspur.emmcloud.util.privates.richtext.callback.ImageFixCallback;
import com.inspur.emmcloud.util.privates.richtext.callback.LinkFixCallback;
import com.inspur.emmcloud.util.privates.richtext.callback.OnUrlClickListener;
import com.inspur.emmcloud.util.privates.richtext.callback.OnUrlLongClickListener;
import com.inspur.emmcloud.util.privates.richtext.ig.MyImageDownloader;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * DisplayTxtRichMsg
 *
 * @author sunqx 展示富文本卡片 2016-08-19
 */
public class DisplayTxtMarkdownMsg {

    private static final DrawableGetter drawableGetter = new DrawableGetter() {
        @Override
        public Drawable getDrawable(ImageHolder holder, RichTextConfig config, TextView textView) {
            Bitmap bmp = BitmapFactory.decodeResource(MyApplication.getInstance().getResources(), R.drawable.default_image);
            Drawable drawable = new BitmapDrawable(MyApplication.getInstance().getResources(), bmp);
            drawable.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
            return drawable;
        }
    };

    /**
     * 富文本卡片
     *
     * @param context
     * @param msg
     */
    public static View getView(final Context context, Message msg, List<MarkDownLink> markDownLinkList) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_text_markdown_view, null);
        final boolean isMyMsg = msg.getFromUser().equals(MyApplication.getInstance().getUid());
        final TextView titleText = cardContentView.findViewById(R.id.tv_name_tips);
        final TextView contentText = cardContentView.findViewById(R.id.tv_content);
        BubbleLayout cardLayout = cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : ResourceUtils.getResValueOfAttr(context, R.attr.bubble_bg_color)));
        cardLayout.setStrokeWidth(isMyMsg ? 0 : 0.5f);
        titleText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        contentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : ResourceUtils.getResValueOfAttr(context, R.attr.text_color)));
        String content = msg.getMsgContentTextMarkdown().getText();
        String title = msg.getMsgContentTextMarkdown().getTitle();
        if (StringUtils.isBlank(title)) {
            titleText.setVisibility(View.GONE);
        } else {
            titleText.setVisibility(View.VISIBLE);
            showContentByMarkdown(context, title, titleText, isMyMsg, msg.getId(), markDownLinkList);
        }
        showContentByMarkdown(context, content, contentText, isMyMsg, msg.getId(), markDownLinkList);
        contentText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //防止改动ui层级后报错
                try {
                    ViewParent parent = v.getParent().getParent();
                    if (parent instanceof ViewGroup) {
                        // 获取被点击控件的父容器，让父容器执行点击；
                        ((ViewGroup) parent).performLongClick();
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        return cardContentView;
    }

    private static void showContentByMarkdown(final Context context, final String content, final TextView textView,
                                              final boolean isMyMsg, final String mid, final List<MarkDownLink> markDownLinks) {
        final int holderWidth = ResolutionUtils.getWidth(context) - DensityUtil.dip2px(MyApplication.getInstance(), 141);
        final List<MarkDownLink> markDownLinkList = markDownLinks;
        RichText.initCacheDir(new File(MyAppConfig.LOCAL_CACHE_MARKDOWN_PATH));
        RichText.from(content)
                .type(RichType.markdown)
                .scaleType(ImageHolder.ScaleType.center_crop)
                .linkFix(new LinkFixCallback() {
                    @Override
                    public void fix(LinkHolder holder) {
                        holder.setUnderLine(false);
                        holder.setColor(context.getResources().getColor(
                                isMyMsg ? R.color.hightlight_in_blue_bg
                                        : R.color.header_bg_blue));
                        for (int i = 0; i < markDownLinkList.size(); i++) {
                            if (holder.getUrl().equals(markDownLinkList.get(i).getLink())) {
                                holder.setColor(context.getResources().getColor(R.color.mark_down_url_read));
                            }
                        }
                    }
                })
                .urlClick(new OnUrlClickListener() {
                    @Override
                    public boolean urlClicked(String url) {
                        List<MarkDownLink> clickedLinkList = new ArrayList<>();
                        for (int i = 0; i < markDownLinkList.size(); i++) {
                            if (url.equals(markDownLinkList.get(i).getLink())) {
                                MarkDownLink markDownLink = new MarkDownLink(mid, url);
                                clickedLinkList.add(markDownLink);
                                break;
                            }
                        }
                        if (!(clickedLinkList.size() > 0)) {
                            MarkDownLink markDownLink = new MarkDownLink(mid, url);
                            markDownLinkList.add(markDownLink);
                            MarkDownLinkCacheUtils.saveMarkDownLink(context,markDownLink);
                            showContentByMarkdown(context, content, textView, isMyMsg, mid, markDownLinkList);
                        }
                        if (url.startsWith("http")) {
                            UriUtils.openUrl((Activity) context, url);
                            return true;
                        }
                        /**加上这个目的是为了重新刷新该Ui*/
                        return false;
                    }
                })
                .urlLongClick(new OnUrlLongClickListener() {
                    @Override
                    public boolean urlLongClick(String url) {
                        return true;
                    }
                })
                .singleLoad(false)
                .imageDownloader(new MyImageDownloader())
                .fix(new ImageFixCallback() {
                    @Override
                    public void onInit(ImageHolder holder) {
                        holder.setWidth(holderWidth);
                        holder.setHeight(holderWidth);
                    }

                    @Override
                    public void onLoading(ImageHolder holder) {
                    }

                    @Override
                    public void onSizeReady(ImageHolder holder, int imageWidth, int imageHeight, ImageHolder.SizeHolder sizeHolder) {

                    }

                    @Override
                    public void onImageReady(ImageHolder holder, int width, int height) {
                    }

                    @Override
                    public void onFailure(ImageHolder holder, Exception e) {
                    }
                })
                .placeHolder(drawableGetter) // 设置加载中显示的占位图
                .errorImage(drawableGetter) // 设置加载失败的错误图
                .cache(CacheType.all)
                .autoFix(true)
                .into(textView);
    }
}
