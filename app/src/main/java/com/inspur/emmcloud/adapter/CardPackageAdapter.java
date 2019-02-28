package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.mine.CardPackageBean;
import com.inspur.emmcloud.interf.OnCardPackageClickListener;
import com.inspur.emmcloud.widget.SwitchView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/8/1.
 */

public class CardPackageAdapter extends RecyclerView.Adapter<CardPackageAdapter.CardPackageHold> {

    public static final int CARD_PACKAGE_OPEN = 1;
    public static final int CARD_PACKAGE_CLOUSE = 0;
    private OnCardPackageClickListener listener;
    private LayoutInflater inflater;
    private List<CardPackageBean> cardPackageBeanList = new ArrayList<>();

    public CardPackageAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public CardPackageHold onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.card_package_set_item, null);
        CardPackageHold holder = new CardPackageHold(view);
        holder.companyNameText = (TextView) view.findViewById(R.id.tv_card_package_set_item);
        holder.switchView = (SwitchView) view.findViewById(R.id.switch_card_package_set_item);
        return holder;
    }

    @Override
    public void onBindViewHolder(final CardPackageHold holder, final int position) {
        holder.companyNameText.setText(cardPackageBeanList.get(position).getCompany());
        holder.switchView.init();
        holder.switchView.setOpened(cardPackageBeanList.get(position).getState() == 1);
        holder.switchView.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                holder.switchView.setOpened(true);
                changeCardPackageState(CARD_PACKAGE_OPEN, cardPackageBeanList.get(position));
            }

            @Override
            public void toggleToOff(View view) {
                holder.switchView.setOpened(false);
                changeCardPackageState(CARD_PACKAGE_CLOUSE, cardPackageBeanList.get(position));
            }
        });
    }

    /**
     * 改变Card状态
     *
     * @param state
     */
    private void changeCardPackageState(int state, CardPackageBean cardPackageBean) {
        cardPackageBean.setState(state);
        listener.onCardPackageClick(cardPackageBean);
    }

    @Override
    public int getItemCount() {
        return cardPackageBeanList.size();
    }

    /**
     * 设置监听器
     *
     * @param l
     */
    public void setOnCardPackageClickListener(OnCardPackageClickListener l) {
        this.listener = l;
    }

    /**
     * 设置数据并刷新adapter
     *
     * @param cardPackageBeanList
     */
    public void setAndRefreshCardPackageAdapter(List<CardPackageBean> cardPackageBeanList) {
        this.cardPackageBeanList.addAll(cardPackageBeanList);
        notifyDataSetChanged();
    }

    class CardPackageHold extends RecyclerView.ViewHolder {
        TextView companyNameText;
        SwitchView switchView;

        public CardPackageHold(View itemView) {
            super(itemView);
        }
    }
}
