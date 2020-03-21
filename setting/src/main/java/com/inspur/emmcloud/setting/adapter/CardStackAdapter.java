package com.inspur.emmcloud.setting.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.BottomDialog;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.bean.CardPackageBean;
import com.inspur.emmcloud.setting.widget.cardstack.RxAdapterStack;
import com.inspur.emmcloud.setting.widget.cardstack.RxCardStackView;


public class CardStackAdapter extends RxAdapterStack<CardPackageBean> {

    private Context context;

    public CardStackAdapter(Context context) {
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
        private TextView taxpayerText;
        private TextView bankText;
        private TextView bankNumText;
        private TextView addressText;
        private TextView phoneText;

        public ColorItemViewHolder(View view) {
            super(view);
            companyNameText = (TextView) view.findViewById(R.id.tv_company_name);
            taxpayerText = (TextView) view.findViewById(R.id.tv_taxpayer);
            bankText = (TextView) view.findViewById(R.id.txt_bank);
            bankNumText = (TextView) view.findViewById(R.id.tv_bank_code);
            addressText = (TextView) view.findViewById(R.id.tv_address);
            phoneText = (TextView) view.findViewById(R.id.tv_phone);
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
                    BottomDialog.ActionListSheetBuilder builder = new BottomDialog.ActionListSheetBuilder(context)
                            .setTitle(context.getString(R.string.user_call))
                            .setTitleColor(Color.parseColor("#888888"))
                            .setItemColor(Color.parseColor("#36A5F6"))
                            .setCancelColor(Color.parseColor("#333333"));
                    BottomDialog bottomDialog = builder.build();
                    ImageDisplayUtils.getInstance().displayImage(builder.getQrCodeImage(),cardPackageBean.getBarcodeUrl());
                    bottomDialog.show();
                }
            });
            taxpayerText.setText(cardPackageBean.getTaxpayer());
            bankText.setText(cardPackageBean.getBank());
            bankNumText.setText(cardPackageBean.getBankAccount());
            addressText.setText(cardPackageBean.getAddress());
            phoneText.setText(cardPackageBean.getPhone());
        }

    }
}
