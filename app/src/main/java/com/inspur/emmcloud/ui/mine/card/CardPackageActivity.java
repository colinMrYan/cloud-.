package com.inspur.emmcloud.ui.mine.card;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.CardStackAdapter;
import com.inspur.emmcloud.widget.cardstack.RxAdapterUpDownStackAnimator;
import com.inspur.emmcloud.widget.cardstack.RxCardStackView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.Arrays;

/**
 * Created by yufuchang on 2018/7/27.
 */
@ContentView(R.layout.activity_card_package)
public class CardPackageActivity extends BaseActivity  implements RxCardStackView.ItemExpendListener{

    public static Integer[] TEST_DATAS = new Integer[]{
            R.color.custom_progress_green_header,
            R.color.custom_progress_green_progress,
            R.color.custom_progress_orange_header,
            R.color.custom_progress_orange_progress,
            R.color.custom_progress_blue_header,
            R.color.custom_progress_purple_header,
            R.color.custom_progress_red_header,
            R.color.custom_progress_red_progress
    };
    @ViewInject(R.id.stackview_card_package)
    private RxCardStackView cardStackView;
    private CardStackAdapter cardStackAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cardStackView.setItemExpendListener(this);
        cardStackAdapter = new CardStackAdapter(this);
        cardStackView.setAdapter(cardStackAdapter);
//        cardStackView.setRxAdapterAnimator(new RxAdapterAllMoveDownAnimator(cardStackView));
//        cardStackView.setRxAdapterAnimator(new RxAdapterUpDownAnimator(cardStackView));
        cardStackView.setRxAdapterAnimator(new RxAdapterUpDownStackAnimator(cardStackView));

        new Handler().postDelayed(new Runnable() {
            public void run() {
                cardStackAdapter.updateData(Arrays.asList(TEST_DATAS));
            }
        }, 200);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
        }
    }

    @Override
    public void onItemExpend(boolean expend) {

    }
}