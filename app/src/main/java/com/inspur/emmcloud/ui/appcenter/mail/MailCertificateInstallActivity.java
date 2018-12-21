package com.inspur.emmcloud.ui.appcenter.mail;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.LogUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by libaochao on 2018/12/20.
 */
@ContentView(R.layout.activity_mail_certificate_install)
public class MailCertificateInstallActivity extends BaseActivity {
    @ViewInject( R.id.tv_install_certificate)
    private TextView mInstallCertificateTV;
    @ViewInject( R.id.back_layout)
    private RelativeLayout mBackLayout;
    @ViewInject( R.id.tv_certificate_use)
    private TextView mCertificateUseTV;
    @ViewInject( R.id.tv_installed_certificate_title)
    private TextView mInstalledCerTitleTV;
    @ViewInject( R.id.tv_certificate_used_name)
    private TextView mCertificateUseNameTV;
    @ViewInject( R.id.tv_certificate_giver_id)
    private TextView mCertificateGiverIdTV;
    @ViewInject( R.id.tv_certificate_expirty_data)
    private TextView mCertificateExpirtyDataTV;
    @ViewInject( R.id.s_secrity_action1)
    private Switch mSecrity_Action1;
    @ViewInject( R.id.s_secrity_action2)
    private Switch mSecrity_Action2;


    @ViewInject( R.id.tv_new_certificate_title)
    private TextView mNewCertificateTitleTV;
    @ViewInject( R.id.tv_new_certificate_user)
    private TextView mNewCertificateUserTV;
    @ViewInject( R.id.tv_new_certificate_publisher)
    private TextView mNewCertificatePublisherTV;
    @ViewInject( R.id.tv_new_certificate_expirty_data)
    private TextView mNewCertificateExpirtyDataTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
    }

    /**
     *
     * */
    public void onClick(View v){
        switch(v.getId()){
            case R.id.back_layout:
                //finish();
                LogUtils.LbcDebug( "返回按钮" );
                break;
            case R.id.tv_install_certificate:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                LogUtils.LbcDebug("打开文件管理系统");
                startActivityForResult(intent,1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            LogUtils.LbcDebug("获取的文件路径"+uri.toString());
            String[] proj = {MediaStore.Images.Media.DATA};
             Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
           String img_path = actualimagecursor.getString(actual_image_column_index);
            LogUtils.LbcDebug( "Path::"+img_path );
            //判断文件后
        }
        super.onActivityResult( requestCode, resultCode, data );
    }
}
