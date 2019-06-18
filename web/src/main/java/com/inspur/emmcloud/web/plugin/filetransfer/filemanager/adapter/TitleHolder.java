package com.inspur.emmcloud.web.plugin.filetransfer.filemanager.adapter;

import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.adapter.base.RecyclerViewAdapter;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.adapter.base.RecyclerViewHolder;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.bean.TitlePath;


/**
 * Created by ${zhaoyanjun} on 2017/1/12.
 */

public class TitleHolder extends RecyclerViewHolder<TitleHolder> {

    TextView textView;

    public TitleHolder(View itemView) {
        super(itemView);

        textView = (TextView) itemView.findViewById(R.id.title_Name);
    }

    @Override
    public void onBindViewHolder(TitleHolder lineHolder, RecyclerViewAdapter adapter, int position) {
        TitlePath titlePath = (TitlePath) adapter.getItem(position);
        lineHolder.textView.setText(titlePath.getNameState());
    }
}
