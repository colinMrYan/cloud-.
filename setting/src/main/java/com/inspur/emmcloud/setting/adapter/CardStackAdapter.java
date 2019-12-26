package com.inspur.emmcloud.setting.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.emmcloud.setting.bean.CardPackageBean;
import com.inspur.emmcloud.setting.widget.cardstack.RxAdapterStack;
import com.inspur.emmcloud.setting.widget.cardstack.RxCardStackView;


public class CardStackAdapter extends RxAdapterStack<CardPackageBean> {

    public CardStackAdapter(Context context) {
        super(context);
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
        switch (viewType) {
            case R.layout.list_card_item:
                view = getLayoutInflater().inflate(R.layout.list_card_item, parent, false);
                return new ColorItemViewHolder(view);
            default:
                view = getLayoutInflater().inflate(R.layout.list_card_item, parent, false);
                return new ColorItemViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.list_card_item;
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

        public void onBind(CardPackageBean cardPackageBean, int position) {
            companyNameText.setText(cardPackageBean.getCompany());
            companyNameText.setBackgroundResource(getBackGroundImg(position));
            taxpayerText.setText(cardPackageBean.getTaxpayer());
            bankText.setText(cardPackageBean.getBank());
            bankNumText.setText(cardPackageBean.getBankAccount());
            addressText.setText(cardPackageBean.getAddress());
            phoneText.setText(cardPackageBean.getPhone());
        }

    }
}
