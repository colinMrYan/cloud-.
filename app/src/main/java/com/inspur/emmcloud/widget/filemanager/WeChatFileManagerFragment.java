package com.inspur.emmcloud.widget.filemanager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.widget.filemanager.adapter.FileAdapter;
import com.inspur.emmcloud.widget.filemanager.adapter.base.RecyclerViewAdapter;
import com.inspur.emmcloud.widget.filemanager.bean.FileBean;
import com.inspur.emmcloud.widget.filemanager.bean.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Date：2021/5/17
 * Author：wang zhen
 * Description 微信文件夹下文件选择界面
 */
public class WeChatFileManagerFragment extends BaseFragment {
    public static final String EXTRA_MAXIMUM = "extra_maximum";
    public static final String EXTRA_FILTER_FILE_TYPE = "extra_filter_file_type";
    private View rootView;
    private int maximum = 1;
    private ArrayList<String> filterFileTypeList;
    private FileAdapter fileAdapter;
    private LinearLayout empty_rel;
    private List<FileBean> beanList = new ArrayList<>();
    private List<FileBean> selectFileBeanList = new ArrayList<>();
    private boolean isStatusSelect = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_file_manager_wechat, null);
        getIntentParam();
        RecyclerView fileRecyclerView = rootView.findViewById(R.id.rcv_file);
        fileAdapter = new FileAdapter(getActivity(), beanList, selectFileBeanList, maximum);
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        fileRecyclerView.setAdapter(fileAdapter);
        empty_rel = rootView.findViewById(R.id.ll_empty);
        fileAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                FileBean file = beanList.get(position);
                FileType fileType = file.getFileType();
                if (fileType == FileType.directory) {
                    getFile(file.getPath());
                } else {
                    if (isStatusSelect) {
                        if (maximum == 1) {
                            selectFileBeanList.clear();
                            selectFileBeanList.add(file);
                            returnSelectResult();
                            return;
                        }
                    } else {
                        FileUtils.openFile(getActivity(), file.getPath());
                    }
                }
            }
        });
        String rootPath = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/Android/data/com.tencent.mm/MicroMsg/Download";
        getFile(rootPath);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater
                    .inflate(R.layout.fragment_file_manager_wechat, container, false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // 获取文件列表
    public void getFile(String path) {
        File rootFile = new File(path + File.separator);
        new MyTask(rootFile, filterFileTypeList).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    /**
     * 数据初始化。暂时没啥用
     */
    private void getIntentParam() {
        maximum = getActivity().getIntent().getIntExtra(EXTRA_MAXIMUM, 1);
        filterFileTypeList = getActivity().getIntent().getStringArrayListExtra(EXTRA_FILTER_FILE_TYPE);
        if (filterFileTypeList == null) {
            filterFileTypeList = new ArrayList<>();
        }
    }

    /**
     * 返回选择结果
     **/
    private void returnSelectResult() {
        ArrayList<String> pathList = new ArrayList<>();
        for (FileBean fileBean : selectFileBeanList) {
            pathList.add(fileBean.getPath());
        }
        Intent intent = new Intent();
        intent.putExtra("isNativeFile", true);
        intent.putStringArrayListExtra("pathList", pathList);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    public void onBackPress() {
        getActivity().finish();
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
}
