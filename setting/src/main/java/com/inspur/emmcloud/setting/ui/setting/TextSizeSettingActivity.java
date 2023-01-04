package com.inspur.emmcloud.setting.ui.setting;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.widget.ImageViewRound;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiUri;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.ui.IIgnoreFontScaleActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.widget.TextRatingBar;

public class TextSizeSettingActivity extends BaseActivity implements IIgnoreFontScaleActivity {

    private float mFontScale = 1.0f;
    private ImageViewRound mHead1;
    private TextView mText1;
    private TextView mText2;

    private TextView mText3;

    @Override
    public void onCreate() {
        mHead1 = findViewById(R.id.font_set_head1);
        mText1 = findViewById(R.id.font_set_text1);
        mText2 = findViewById(R.id.font_set_text2);
        mText3 = findViewById(R.id.font_set_text3);
        updateHeader();
        TextView saveText = findViewById(R.id.font_size_set_save);
        TextRatingBar textRatingBar = findViewById(R.id.text_rating_bar);
        textRatingBar.setOnRatingListener(new TextRatingBar.OnRatingListener() {
            @Override
            public void onRating(int rating) {
                mFontScale = getFontScaleByRating(rating);
                refreshChatUi();
            }
        });
        saveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CustomDialog.MessageDialogBuilder(TextSizeSettingActivity.this)
                        .setCancelable(false)
                        .setMessage(getString(R.string.caring_switch_tip))
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                PreferencesUtils.putFloat(TextSizeSettingActivity.this, Constant.CARING_SWITCH_FLAG, mFontScale);
                                com.blankj.utilcode.util.AppUtils.relaunchApp(true);
                            }
                        })
                        .show();

            }
        });
        mFontScale = PreferencesUtils.getFloat(this, Constant.CARING_SWITCH_FLAG, 1);
        textRatingBar.setRating(getRatingByFontScale(mFontScale));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshChatUi();
            }
        });
    }

    private void updateHeader() {
        String myInfo = PreferencesUtils.getString(this, "myInfo", "");
        if (TextUtils.isEmpty(myInfo)) {
            return;
        }
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        String photoUri = BaseModuleApiUri.getUserPhoto(BaseApplication.getInstance(), getMyInfoResult.getID());
        ImageDisplayUtils.getInstance().displayImage(mHead1, photoUri, ResourceUtils.getResValueOfAttr(this, R.attr.design3_icon_person_default));
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ibt_back) {
            finish();
        }
    }

    private void refreshChatUi() {
        mText1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * mFontScale);
        mText2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * mFontScale);
        mText3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * mFontScale);
        mText1.postInvalidate();
    }

    private float getFontScaleByRating(int rating) {
        switch (rating) {
            case 0:
                return 0.9f;
            case 1:
                return 1f;
            case 2:
                return 1.1f;
            case 3:
                return 1.2f;
            case 4:
                return 1.3f;
        }
        return 1f;
    }

    private int getRatingByFontScale(float fontScale) {
        if (Float.compare(fontScale, 0.9f) == 0) {
            return 0;
        } else if (Float.compare(fontScale, 1f) == 0) {
            return 1;
        } else if (Float.compare(fontScale, 1.1f) == 0) {
            return 2;
        } else if (Float.compare(fontScale, 1.2f) == 0) {
            return 3;
        } else if (Float.compare(fontScale, 1.3f) == 0) {
            return 4;
        }
        return 1;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_font_size_set;
    }
}
