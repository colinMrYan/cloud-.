package com.inspur.emmcloud.setting.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.BottomDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.bean.CardPackageBean;
import com.inspur.emmcloud.setting.ui.setting.RecommendAppActivity;
import com.inspur.emmcloud.setting.widget.cardstack.RxAdapterStack;
import com.inspur.emmcloud.setting.widget.cardstack.RxCardStackView;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.lang.ref.WeakReference;


public class CardStackAdapter extends RxAdapterStack<CardPackageBean> {

    private Activity context;
    private CustomShareListener mShareListener;

    public CardStackAdapter(Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    public void bindView(CardPackageBean data, int position, RxCardStackView.ViewHolder holder) {
        if (holder instanceof ColorItemViewHolder) {
            ColorItemViewHolder h = (ColorItemViewHolder) holder;
            h.onBind(data, position);
        }
    }

    @Override
    protected RxCardStackView.ViewHolder onCreateView(ViewGroup parent, int viewType) {
        View view;
        if (viewType == R.layout.setting_list_card_item) {
            view = getLayoutInflater().inflate(R.layout.setting_list_card_item, parent, false);
            return new ColorItemViewHolder(view);
        } else {
            view = getLayoutInflater().inflate(R.layout.setting_list_card_item, parent, false);
            return new ColorItemViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.setting_list_card_item;
    }

    /**
     * @param position
     * @return
     */
    private int getBackGroundImg(int position) {
        int index = position % 3;
        switch (index) {
            case 0:
                return R.drawable.icon_card_package_head_bg_1;
            case 1:
                return R.drawable.icon_card_package_head_bg_2;
            case 2:
                return R.drawable.icon_card_pakcage_head_bg_3;
            default:
                return R.drawable.icon_card_package_head_bg_1;
        }
    }

    class ColorItemViewHolder extends RxCardStackView.ViewHolder {

        private TextView companyNameText;
        private TextView companyTaxText;
        private TextView companyAdressText;
        private TextView phoneNumText;
        private TextView bankNameText;
        private TextView bankAccountText;

        public ColorItemViewHolder(View view) {
            super(view);
            companyNameText = (TextView) view.findViewById(R.id.tv_company_name);
            companyTaxText = (TextView) view.findViewById(R.id.company_tax_name);
            companyAdressText = (TextView) view.findViewById(R.id.company_address_name);
            phoneNumText = (TextView) view.findViewById(R.id.link_phone_name);
            bankNameText = (TextView) view.findViewById(R.id.bank_name);
            bankAccountText = (TextView) view.findViewById(R.id.bank_account_name);
        }

        @Override
        public void onItemExpand(boolean b) {
        }

        public void onBind(final CardPackageBean cardPackageBean, int position) {
            companyNameText.setText(cardPackageBean.getCompany());
            companyNameText.setBackgroundResource(getBackGroundImg(position));
            companyNameText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!StringUtils.isBlank(cardPackageBean.getBarcodeUrl())) {
                        BottomDialog.ActionListSheetBuilder builder = new BottomDialog.ActionListSheetBuilder(context);
                        final BottomDialog bottomDialog = builder.build();
                        ((TextView) bottomDialog.findViewById(R.id.tv_company_name)).setText(cardPackageBean.getCompany());
                        bottomDialog.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bottomDialog.dismiss();
                            }
                        });
                        bottomDialog.findViewById(R.id.btn_share_group_qrcode).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bottomDialog.dismiss();
                                mShareListener = new CustomShareListener(context);
                                new ShareAction(context).setDisplayList(
                                        SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                                        SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.SMS
                                )
                                        .setShareboardclickCallback(new ShareBoardlistener() {
                                            @Override
                                            public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                                                if (share_media == SHARE_MEDIA.SMS) {
                                                    new ShareAction(context).withText(cardPackageBean.getCompany() + "发票二维码  " + cardPackageBean.getBarcodeUrl())
                                                            .setPlatform(share_media)
                                                            .setCallback(mShareListener)
                                                            .share();
                                                } else {
                                                    UMImage thumb = new UMImage(context, R.drawable.ic_launcher);
                                                    UMWeb web = new UMWeb(cardPackageBean.getBarcodeUrl());
                                                    web.setThumb(thumb);
                                                    web.setDescription("发票二维码");
                                                    web.setTitle(cardPackageBean.getCompany());
                                                    new ShareAction(context).withMedia(web)
                                                            .setPlatform(share_media)
                                                            .setCallback(mShareListener)
                                                            .share();
                                                }
                                            }
                                        })
                                        .open();
                            }
                        });
                        ImageDisplayUtils.getInstance().displayImage(builder.getQrCodeImage(), cardPackageBean.getBarcodeUrl());
                        bottomDialog.show();
                    } else {
                        ToastUtils.show(context.getString(R.string.card_package_no_qrcode));
                    }
                }
            });
            companyTaxText.setText(cardPackageBean.getTaxpayer());
            bankNameText.setText(cardPackageBean.getBank());
            bankAccountText.setText(cardPackageBean.getBankAccount());
            companyAdressText.setText(cardPackageBean.getAddress());
            phoneNumText.setText(cardPackageBean.getPhone());
            phoneNumText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(cardPackageBean.getPhone()))
                    showCallUserDialog(cardPackageBean.getPhone());
                }
            });
        }

    }

    private void showCallUserDialog(final String mobile) {
        new CustomDialog.MessageDialogBuilder(getContext())
                .setMessage(mobile)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.user_call, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AppUtils.call(context, mobile, 1);
                    }
                })
                .show();
    }


    private static class CustomShareListener implements UMShareListener {

        private WeakReference<Activity> mActivity;

        private CustomShareListener(Activity activity) {
            mActivity = new WeakReference(activity);
        }

        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            ToastUtils.show(mActivity.get(), R.string.baselib_share_success);
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            ToastUtils.show(mActivity.get(), R.string.baselib_share_fail);
            if (t != null) {
                LogUtils.jasonDebug("throw:" + t.getMessage());
            }

        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {

        }
    }
}
