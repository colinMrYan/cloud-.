package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 校正输入文字
 * Created by yufuchang on 2018/9/13.
 */
public class CorrectedSpeechInputActivity extends BaseActivity {
    public static final String RAW_SPEECH_INPUT_WORDS = "raw_speech_input_words";
    public static final String CORRECTED_SPEECH_INPUT_WORDS = "corrected_speech_input_words";
    @BindView(R.id.etv_corrected_speech_input)
    EditText etvCorrectedSpeechInput;


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_corrected_speech_input;
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
            case R.id.ibt_back:
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
