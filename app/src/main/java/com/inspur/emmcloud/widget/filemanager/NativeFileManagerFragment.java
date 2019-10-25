package com.inspur.emmcloud.widget.filemanager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.widget.filemanager.adapter.FileAdapter;
import com.inspur.emmcloud.widget.filemanager.adapter.TitleAdapter;
import com.inspur.emmcloud.widget.filemanager.adapter.base.RecyclerViewAdapter;
import com.inspur.emmcloud.widget.filemanager.bean.FileBean;
import com.inspur.emmcloud.widget.filemanager.bean.FileType;
import com.inspur.emmcloud.widget.filemanager.bean.TitlePath;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by libaochao on 2019/10/23.
 */

public class NativeFileManagerFragment extends BaseFragment {

    public static final String EXTRA_MAXIMUM = "extra_maximum";
    public static final String EXTRA_FILTER_FILE_TYPE = "extra_filter_file_type";
    private RecyclerView titleRecyclerview;
    private RecyclerView fileRecyclerView;
    private FileAdapter fileAdapter;
    private List<FileBean> beanList = new ArrayList<>();
    private File rootFile;
    private LinearLayout empty_rel;
    private String rootPath;
    private boolean isStatusSelect = true;
    private int maximum = 1;
    private ArrayList<String> filterFileTypeList = new ArrayList<>();
    private TitleAdapter titleAdapter;
    private List<FileBean> selectFileBeanList = new ArrayList<>();
    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_file_manager, null);
        getIntentParam();
        titleRecyclerview = rootView.findViewById(R.id.rcv_title);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        titleRecyclerview.setLayoutManager(layoutManager);
        titleAdapter = new TitleAdapter(getActivity(), new ArrayList<TitlePath>());
        titleRecyclerview.setAdapter(titleAdapter);
        fileRecyclerView = rootView.findViewById(R.id.rcv_file);
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
                            fileAdapter.notifyItemChanged(position);
                        } else if (selectFileBeanList.size() == maximum) {
                            ToastUtils.show(getActivity(), getString(com.inspur.emmcloud.web.R.string.file_select_limit_warning, maximum));
                        } else {
                            selectFileBeanList.add(file);
                            fileAdapter.notifyItemChanged(position);
                        }
                    } else {
                        FileUtils.openFile(getActivity(), file.getPath());
                    }
                }
            }
        });

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
        refreshTitleState(getString(com.inspur.emmcloud.web.R.string.internal_shared_storage), rootPath);
        getFile(rootPath);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater
                    .inflate(R.layout.fragment_file_manager, container, false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    private void getIntentParam() {
        maximum = getActivity().getIntent().getIntExtra(EXTRA_MAXIMUM, 1);
        filterFileTypeList = getActivity().getIntent().getStringArrayListExtra(EXTRA_FILTER_FILE_TYPE);
        if (filterFileTypeList == null) {
            filterFileTypeList = new ArrayList<>();
        }
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == com.inspur.emmcloud.web.R.id.ibt_back) {
        } else if (i == com.inspur.emmcloud.web.R.id.tv_ok) {
            returnSelectResult();

        }
    }

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
