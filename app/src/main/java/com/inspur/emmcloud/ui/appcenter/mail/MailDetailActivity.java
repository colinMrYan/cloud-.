package com.inspur.emmcloud.ui.appcenter.mail;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MailApiService;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailDetailResult;
import com.inspur.emmcloud.bean.appcenter.mail.Mail;
import com.inspur.emmcloud.util.common.NetUtils;

import org.xutils.view.annotation.ContentView;

/**
 * Created by chenmch on 2018/12/24.
 */

@ContentView(R.layout.activity_mail_details)
public class MailDetailActivity extends BaseActivity {
    public static final String EXTRA_MAIL = "extra_mail";
    private Mail mail;
    private MailApiService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mail = (Mail) getIntent().getSerializableExtra(EXTRA_MAIL);
        apiService = new MailApiService(this);
        apiService.setAPIInterface(new WebService());
        getMailDetail();
    }


    public void onClick(View v){
        switch (v.getId()){
            case R.id.ibt_back:
                finish();
                break;
            case R.id.bt_mail_forward:
                break;
            case R.id.bt_mail_reply_all:
                break;
            case R.id.bt_mail_reply:
                break;
            case R.id.bt_mail_delete:
                break;
            case R.id.tv_mail_receiver_open:
                break;
            case R.id.tv_mail_cc_open:
                break;
        }
    }


    private void getMailDetail(){
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())){
            apiService.getMailDetail(mail.getId());
        }
    }

    private class WebService extends APIInterfaceInstance{

        @Override
        public void returnMailDetailSuccess(GetMailDetailResult getMailDetailResult) {
            super.returnMailDetailSuccess(getMailDetailResult);
        }

        @Override
        public void returnMailDetailFail(String error, int errorCode) {
            super.returnMailDetailFail(error, errorCode);
        }
    }
}
