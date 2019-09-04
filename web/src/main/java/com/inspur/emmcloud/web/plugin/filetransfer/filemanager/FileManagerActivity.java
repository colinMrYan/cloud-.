package com.inspur.emmcloud.web.plugin.filetransfer.filemanager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.adapter.FileAdapter;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.adapter.TitleAdapter;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.adapter.base.RecyclerViewAdapter;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.bean.FileBean;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.bean.FileType;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.bean.TitlePath;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Route(path = Constant.AROUTER_CLASS_WEB_FILEMANAGER)
public class FileManagerActivity extends BaseActivity {
    public static final String EXTRA_MAXIMUM = "extra_maximum";
    public static final String EXTRA_FILTER_FILE_TYPE = "extra_filter_file_type";
    private RecyclerView titleRecyclerview;
    private RecyclerView fileRecyclerView;
    private TextView okText;
    private FileAdapter fileAdapter;
    private List<FileBean> beanList = new ArrayList<>();
    private File rootFile;
    private LinearLayout empty_rel;
    private int PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 100;
    private String rootPath;
    private boolean isStatusSelect = true;
    private int maximum = 1;
    private ArrayList<String> filterFileTypeList = new ArrayList<>();
    private TitleAdapter titleAdapter;
    private List<FileBean> selectFileBeanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        getIntentParam();
        titleRecyclerview = (RecyclerView) findViewById(R.id.rcv_title);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        titleRecyclerview.setLayoutManager(layoutManager);
        titleAdapter = new TitleAdapter(FileManagerActivity.this, new ArrayList<TitlePath>());
        titleRecyclerview.setAdapter(titleAdapter);
        okText = (TextView) findViewById(R.id.tv_ok);
        if (1 == maximum) {
            okText.setVisibility(View.GONE);
        }
        setOKTextStatus();
        fileRecyclerView = (RecyclerView) findViewById(R.id.rcv_file);

        fileAdapter = new FileAdapter(this, beanList, selectFileBeanList, maximum);
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileRecyclerView.setAdapter(fileAdapter);

        empty_rel = (LinearLayout) findViewById(R.id.ll_empty);

        fileAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                FileBean file = beanList.get(position);
                FileType fileType = file.getFileType();
                if (fileType == FileType.directory) {
                    getFile(file.getPath());

                    refreshTitleState(file.getName(), file.getPath());
                } else {
                    if (isStatusSelect) {
                        if (maximum == 1) {
                            selectFileBeanList.add(file);
                            returnSelectResult();
                            return;
                        }
                        if (selectFileBeanList.contains(file)) {
                            selectFileBeanList.remove(file);
                            setOKTextStatus();
                            fileAdapter.notifyItemChanged(position);
                        } else if (selectFileBeanList.size() == maximum) {
                            ToastUtils.show(FileManagerActivity.this, getString(R.string.file_select_limit_warning, maximum));
                        } else {
                            selectFileBeanList.add(file);
                            setOKTextStatus();
                            fileAdapter.notifyItemChanged(position);
                        }

                    } else {
                        FileUtils.openFile(FileManagerActivity.this, file.getPath());
                    }
                }

            }
        });

//        fileAdapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
//                if (isStatusSelect) {
//                    return true;
//                }
//
//                FileBean fileBean = (FileBean) fileAdapter.getItem(position);
//                FileType fileType = fileBean.getFileType();
//                if (fileType != null && fileType != FileType.directory) {
//                    FileUtil.sendFile(FileManagerActivity.this, new File(fileBean.getPath()));
//                }
//                return false;
//            }
//        });

        titleAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                TitlePath titlePath = (TitlePath) titleAdapter.getItem(position);
                getFile(titlePath.getPath());

                int count = titleAdapter.getItemCount();
                int removeCount = count - position - 1;
                for (int i = 0; i < removeCount; i++) {
                    titleAdapter.removeLast();
                }
            }
        });


        rootPath = Environment.getExternalStorageDirectory().

                getAbsolutePath();

        refreshTitleState(getString(R.string.internal_shared_storage), rootPath);

        getFile(rootPath);

        //        // 先判断是否有权限。
//        if(AndPermission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE )) {
//            // 有权限，直接do anything.
        //           getFile(rootPath);
//        } else {
//            //申请权限。
//            AndPermission.with(this)
//                    .requestCode(PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
//                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE )
//                    .send();
//        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.web_activity_file_manager;
    }

    private void getIntentParam() {
        maximum = getIntent().getIntExtra(EXTRA_MAXIMUM, 1);
        filterFileTypeList = getIntent().getStringArrayListExtra(EXTRA_FILTER_FILE_TYPE);
        if (filterFileTypeList == null) {
            filterFileTypeList = new ArrayList<>();
        }
    }

    private void setOKTextStatus() {
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

    public void getFile(String path) {
        rootFile = new File(path + File.separator);
        new MyTask(rootFile, filterFileTypeList).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    void refreshTitleState(String title, String path) {
        TitlePath filePath = new TitlePath();
        filePath.setNameState(title + " /");
        filePath.setPath(path);
        titleAdapter.addItem(filePath);
        titleRecyclerview.smoothScrollToPosition(titleAdapter.getItemCount());
    }


    @Override
    public void onBackPressed() {
        List<TitlePath> titlePathList = (List<TitlePath>) titleAdapter.getAdapterData();
        if (titlePathList.size() == 1) {
            finish();
        } else {
            titleAdapter.removeItem(titlePathList.size() - 1);
            getFile(titlePathList.get(titlePathList.size() - 1).getPath());
        }
    }

    public class FileComparator implements Comparator {
        @Override
        public int compare(Object o, Object t1) {
            File file1 = (File) o;
            File file2 = (File) t1;
            if (file1.isDirectory() && file2.isFile()) {
                return -1;
            } else if (file1.isFile() && file2.isDirectory()) {
                return 1;
            } else {
                return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase().toString());

            }
        }
    }

    class MyTask extends AsyncTask {
        private File file;
        private ArrayList<String> filterFileTypeList;

        MyTask(File file, ArrayList<String> filterFileTypeList) {
            this.file = file;
            this.filterFileTypeList = filterFileTypeList;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            List<FileBean> fileBeenList = new ArrayList<>();
            if (file.isDirectory()) {
                File[] filesArray = file.listFiles();
                if (filesArray != null) {
                    List<File> fileList = new ArrayList<>();
                    Collections.addAll(fileList, filesArray);  //把数组转化成list
                    Collections.sort(fileList, new FileComparator());  //按照名字排序

                    for (File f : fileList) {
                        if (f.isHidden()) continue;
                        if (filterFileTypeList.size() > 0 && !f.isDirectory()) {
                            boolean isFileFileType = false;
                            for (String fileType : filterFileTypeList) {
                                if (f.getName().endsWith(fileType)) {
                                    isFileFileType = true;
                                    break;
                                }
                            }
                            if (!isFileFileType) {
                                continue;
                            }
                        }
                        FileBean fileBean = new FileBean();
                        fileBean.setName(f.getName());
                        fileBean.setPath(f.getAbsolutePath());
                        fileBean.setFileType(FileUtil.getFileType(f));
                        fileBean.setChildCount(FileUtil.getFileChildCount(f));
                        fileBean.setSize(f.length());
                        fileBeenList.add(fileBean);
                    }
                }
            }

            beanList = fileBeenList;
            return fileBeenList;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (beanList.size() > 0) {
                empty_rel.setVisibility(View.GONE);
            } else {
                empty_rel.setVisibility(View.VISIBLE);
            }
            fileAdapter.refresh(beanList);
        }

    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        // 只需要调用这一句，其它的交给AndPermission吧，最后一个参数是PermissionListener。
//        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, listener);
//    }

//    private EmmPermissionListener listener = new EmmPermissionListener() {
//        @Override
//        public void onSucceed(int requestCode, List<String> grantedPermissions) {
//            // 权限申请成功回调。
//            if(requestCode == PERMISSION_CODE_WRITE_EXTERNAL_STORAGE ) {
//                getFile(rootPath);
//            }
//        }
//
//        @Override
//        public void onFailed(int requestCode, List<String> deniedPermissions) {
//            // 权限申请失败回调。
//            AndPermission.defaultSettingDialog( FileManagerActivity.this, PERMISSION_CODE_WRITE_EXTERNAL_STORAGE )
//                    .setTitle("权限申请失败")
//                    .setMessage("我们需要的一些权限被您拒绝或者系统发生错误申请失败，请您到设置页面手动授权，否则功能无法正常使用！")
//                    .setPositiveButton("好，去设置")
//                    .show();
//        }
//    };
}
