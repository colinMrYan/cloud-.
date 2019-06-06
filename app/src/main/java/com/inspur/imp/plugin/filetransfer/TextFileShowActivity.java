package com.inspur.imp.plugin.filetransfer;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.imp.api.ImpBaseActivity;


public class TextFileShowActivity extends ImpBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(Res.getLayoutID("plugin_activity_show_txt"));
        Intent intent = getIntent();
        String content = intent.getExtras().getString("text");
        TextView contentText = (TextView) findViewById(Res.getWidgetID("content_text"));
        contentText.setText(content);
    }

    public void onClick(View v) {
        finish();
    }

}
