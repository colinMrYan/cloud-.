package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * 校正输入文字
 * Created by yufuchang on 2018/9/13.
 */
@ContentView(R.layout.activity_corrected_speech_input)
public class CorrectedSpeechInputActivity extends BaseActivity {
    public static final String RAW_SPEECH_INPUT_WORDS = "raw_speech_input_words";
    public static final String CORRECTED_SPEECH_INPUT_WORDS = "corrected_speech_input_words";
    @ViewInject(R.id.etv_corrected_speech_input)
    private EditText etvCorrectedSpeechInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        etvCorrectedSpeechInput.setText(getRawSpeechInputWords());
    }

    /**
     * 获取原始文字
     *
     * @return
     */
    private String getRawSpeechInputWords() {
        String rawSpeechInputWords = "";
        if (getIntent() != null && getIntent().hasExtra(RAW_SPEECH_INPUT_WORDS)) {
            rawSpeechInputWords = getIntent().getStringExtra(RAW_SPEECH_INPUT_WORDS);
        }
        return rawSpeechInputWords;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.tv_ok:
                Intent intent = new Intent();
                intent.putExtra(CORRECTED_SPEECH_INPUT_WORDS, etvCorrectedSpeechInput.getText());
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }
}
