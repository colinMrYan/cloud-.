package com.inspur.emmcloud.ui.mine.myinfo;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;

public class ModifyUserInfoActivity extends BaseActivity {

    private ClearEditText modifyEditText;
    private Button modifyButton;
    private String modifyText;
    private MineAPIService apiService;
    private LoadingDialog loadingDialog;


    @Override
    public void onCreate() {
        modifyEditText = (ClearEditText) findViewById(R.id.modifyinfo_edit);
        modifyButton = (Button) findViewById(R.id.get_modifyinfo_btn);
        loadingDialog = new LoadingDialog(ModifyUserInfoActivity.this);
        Intent intent;
        intent = getIntent();
        String oldValue = intent.getStringExtra("oldvalue");
        modifyEditText.setText(oldValue);
        apiService = new MineAPIService(this);
        apiService.setAPIInterface(new WebService());
        modifyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                modifyText = modifyEditText.getText().toString();
                if (NetUtils.isNetworkConnected(ModifyUserInfoActivity.this)) {
                    loadingDialog.show();
                    apiService.modifyUserInfo("real_name", modifyText);
                }
                Intent in = new Intent();
                in.putExtra("newname", modifyText);
                // -1为RESULT_OK, 1为RESULT_CANCEL..
                // in 则是回调的Activity内OnActivityResult那个方法内处理
                ModifyUserInfoActivity.this.setResult(RESULT_OK, in);
                finish();
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_modify_userinfo;
    }

    public void onClick(View v) {
        finish();
    }

    class WebService extends APIInterfaceInstance {

        @Override
        public void returnModifyUserInfoSucces(
                GetBoolenResult getBoolenResult) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            finish();
        }

        @Override
        public void returnModifyUserInfoFail(String error, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(ModifyUserInfoActivity.this, error, errorCode);
        }

    }


}
