package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.MarkDownLink;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.ResolutionUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.richtext.CacheType;
import com.inspur.emmcloud.util.common.richtext.ImageHolder;
import com.inspur.emmcloud.util.common.richtext.LinkHolder;
import com.inspur.emmcloud.util.common.richtext.RichText;
import com.inspur.emmcloud.util.common.richtext.RichTextConfig;
import com.inspur.emmcloud.util.common.richtext.RichType;
import com.inspur.emmcloud.util.common.richtext.callback.DrawableGetter;
import com.inspur.emmcloud.util.common.richtext.callback.ImageFixCallback;
import com.inspur.emmcloud.util.common.richtext.callback.LinkFixCallback;
import com.inspur.emmcloud.util.common.richtext.callback.OnUrlClickListener;
import com.inspur.emmcloud.util.common.richtext.ig.MyImageDownloader;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.cache.MarkDownLinkCacheUtils;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

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
        cardLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : R.color.bg_other_card));
        cardLayout.setStrokeWidth(isMyMsg ? 0 : 0.5f);
        titleText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        contentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        String content = msg.getMsgContentTextMarkdown().getText();
        String title = msg.getMsgContentTextMarkdown().getTitle();
        if (StringUtils.isBlank(title)) {
            titleText.setVisibility(View.GONE);
        } else {
            titleText.setVisibility(View.VISIBLE);
            showContentByMarkdown(context, title, titleText, isMyMsg, msg.getId(), markDownLinkList);
        }
        showContentByMarkdown(context, content, contentText, isMyMsg, msg.getId(), markDownLinkList);
        return cardContentView;
    }

    private static void showContentByMarkdown(final Context context, final String content, final TextView textView,
                                              final boolean isMyMsg, final String mid, final List<MarkDownLink> markDownLinks) {
        final int holderWidth = ResolutionUtils.getWidth(context) - DensityUtil.dip2px(MyApplication.getInstance(), 141);
        List<MarkDownLink> markDownLinkList = markDownLinks;
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
