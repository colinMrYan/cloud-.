package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.widget.ClearEditText;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by libaochao on 2019/5/20.
 */
@ContentView(R.layout.activity_communication_search_contact)
public class CommunicationSearchContactActivity extends BaseActivity implements View.OnClickListener {
    @ViewInject(R.id.ev_search_input)
    private ClearEditText searchEdit;
    @ViewInject(R.id.tv_cancel)
    private TextView cancelTextView;

    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodUtils.hide(CommunicationSearchContactActivity.this);
              //  onSearch(findViewById(R.id.search_btn));
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchEdit.addTextChangedListener(new SearchWatcher());
        cancelTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_cancel:
                finish();
                break;
        }
    }

    class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //handleSearchApp(s);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
