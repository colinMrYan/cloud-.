package com.inspur.emmcloud.ui.appcenter.webex;

import android.os.Bundle;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_detail)
public class WebexMeetingDetail extends BaseActivity {

    @ViewInject(R.id.bt_function)
    private QMUIRoundButton functionBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
