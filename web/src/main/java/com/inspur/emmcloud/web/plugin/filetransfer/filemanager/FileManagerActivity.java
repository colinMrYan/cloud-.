package com.inspur.emmcloud.web.plugin.filetransfer.filemanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.adapter.WebFileFragmentPagerAdapter;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.bean.FileBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Description web文件管理页面
 */
@Route(path = Constant.AROUTER_CLASS_WEB_FILEMANAGER)
public class FileManagerActivity extends BaseActivity {
    public static final String EXTRA_MAXIMUM = "extra_maximum";
    public static final String EXTRA_FILTER_FILE_TYPE = "extra_filter_file_type";
    public static final String weChatRootPath = Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/Android/data/com.tencent.mm/MicroMsg/Download";// 微信下载文件目录
    public List<FileBean> selectFileBeanList = new ArrayList<>();
    TabLayout fileTabLayout;
    TextView okText;
    private ViewPager fileViewPager;
    public FileManagerFragment fileManagerFragment;
    public WebWeChatFileManagerFragment webWeChatFileManagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, Permissions.STORAGE,
                new PermissionRequestCallback() {
                    @Override
                    public void onPermissionRequestSuccess(List<String> permissions) {
                        fileTabLayout = findViewById(R.id.tl_files_source);
                        fileViewPager = findViewById(R.id.viewpager_file_fragment);
                        okText = findViewById(R.id.tv_ok);
                        fileManagerFragment = new FileManagerFragment();
                        webWeChatFileManagerFragment = new WebWeChatFileManagerFragment();
                        okText = findViewById(R.id.tv_ok);
                        initView();
                        getIntentParam();
                    }

                    @Override
                    public void onPermissionRequestFail(List<String> permissions) {
                        ToastUtils.show(FileManagerActivity.this, PermissionRequestManagerUtils.getInstance()
                                .getPermissionToast(FileManagerActivity.this, permissions));
                    }
                });
    }

    private void initView() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(fileManagerFragment);
        fragmentList.add(webWeChatFileManagerFragment);
        WebFileFragmentPagerAdapter webFileFragmentPagerAdapter = new WebFileFragmentPagerAdapter(this.getSupportFragmentManager(), fragmentList);
        fileTabLayout.addTab(fileTabLayout.newTab().setText(getString(R.string.internal_shared_storage)), true);
        fileTabLayout.addTab(fileTabLayout.newTab().setText(getString(R.string.we_chat_file_manager)), false);
        fileTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                fileViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        fileViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                fileTabLayout.getTabAt(i).select();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        fileViewPager.setAdapter(webFileFragmentPagerAdapter);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.web_activity_file_manager_new;
    }

    private void getIntentParam() {
        int maximum = getIntent().getIntExtra(EXTRA_MAXIMUM, 1);
        if (1 == maximum) {
            okText.setVisibility(View.GONE);
        }
    }

    public void setOKTextStatus() {
        if (selectFileBeanList.size() == 0) {
            okText.setClickable(false);
            okText.setText(R.string.complete);
        } else {
            okText.setClickable(true);
            okText.setText(getString(R.string.complete) + "(" + selectFileBeanList.size() + ")");
        }
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();
        } else if (i == R.id.tv_ok) {
            returnSelectResult();

        }
    }

    private void returnSelectResult() {
        ArrayList<String> pathList = new ArrayList<>();
        for (FileBean fileBean : selectFileBeanList) {
            pathList.add(fileBean.getPath());
        }
        Intent intent = new Intent();
        intent.putStringArrayListExtra("pathList", pathList);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        switch (fileViewPager.getCurrentItem()) {
            case 0:
                fileManagerFragment.onBackPressed();
                break;
            case 1:
                webWeChatFileManagerFragment.onBackPress();
                break;
        }
    }
}
