package com.inspur.emmcloud.web.plugin.filetransfer;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.web.ui.ImpBaseActivity;


public class TextFileShowActivity extends ImpBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(Res.getLayoutID("web_activity_show_txt"));
        Intent intent = getIntent();
        String content = intent.getExtras().getString("text");
        TextView contentText = (TextView) findViewById(Res.getWidgetID("tv_content"));
        contentText.setText(content);
    }

    public void onClick(View v) {
        finish();
    }

}
