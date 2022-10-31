package com.inspur.emmcloud.web.plugin.filetransfer.filemanager;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
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

import static android.app.Activity.RESULT_OK;
import static com.inspur.emmcloud.web.plugin.filetransfer.filemanager.FileManagerActivity.EXTRA_FILTER_FILE_TYPE;
import static com.inspur.emmcloud.web.plugin.filetransfer.filemanager.FileManagerActivity.EXTRA_MAXIMUM;
import static com.inspur.emmcloud.web.plugin.filetransfer.filemanager.FileManagerActivity.weChatRootPath;

/**
 * Date：2021/5/17
 * Author：wang zhen
 * Description web文件选择页面
 */
public class FileManagerFragment extends BaseFragment {

    private View rootView;
    private RecyclerView titleRecyclerview;
    private TitleAdapter titleAdapter;
    private int maximum = 1;
    private List<FileBean> beanList = new ArrayList<>();
    private ArrayList<String> filterFileTypeList = new ArrayList<>();
    public FileAdapter fileAdapter;
    private LinearLayout empty_rel;
    private final boolean isStatusSelect = true;
    public String rootPath;
    private FileManagerActivity fileManagerActivity;
    public String currentPath;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
        RecyclerView fileRecyclerView = rootView.findViewById(R.id.rcv_file);

        fileAdapter = new FileAdapter(getActivity(), beanList, fileManagerActivity.selectFileBeanList, maximum);
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
                            fileManagerActivity.selectFileBeanList.add(file);
                            returnSelectResult();
                            return;
                        }
                        if (fileManagerActivity.selectFileBeanList.contains(file)) {
                            fileManagerActivity.selectFileBeanList.remove(file);
                            fileManagerActivity.setOKTextStatus();
                            fileAdapter.notifyItemChanged(position);
                            if (currentPath.equals(weChatRootPath)) {
                                fileManagerActivity.webWeChatFileManagerFragment.fileAdapter.notifyItemChanged(position);
                            }
                        } else if (fileManagerActivity.selectFileBeanList.size() == maximum) {
                            ToastUtils.show(getActivity(), getString(R.string.file_select_limit_warning, maximum));
                        } else {
                            fileManagerActivity.selectFileBeanList.add(file);
                            fileManagerActivity.setOKTextStatus();
                            fileAdapter.notifyItemChanged(position);
                            if (currentPath.equals(weChatRootPath)) {
                                fileManagerActivity.webWeChatFileManagerFragment.fileAdapter.notifyItemChanged(position);
                            }
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

        refreshTitleState(getString(R.string.internal_shared_storage), rootPath);

        getFile(rootPath);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fileManagerActivity = (FileManagerActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    public void getFile(String path) {
        currentPath = path;
        File rootFile = new File(path + File.separator);
        new MyTask(rootFile, filterFileTypeList).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    void refreshTitleState(String title, String path) {
        TitlePath filePath = new TitlePath();
        filePath.setNameState(title + " /");
        filePath.setPath(path);
        titleAdapter.addItem(filePath);
        titleRecyclerview.smoothScrollToPosition(titleAdapter.getItemCount());
    }

    private void returnSelectResult() {
        ArrayList<String> pathList = new ArrayList<>();
        for (FileBean fileBean : fileManagerActivity.selectFileBeanList) {
            pathList.add(fileBean.getPath());
        }
        Intent intent = new Intent();
        intent.putStringArrayListExtra("pathList", pathList);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    public void onBackPressed() {
        List<TitlePath> titlePathList = (List<TitlePath>) titleAdapter.getAdapterData();
        if (titlePathList.size() == 1) {
            getActivity().finish();
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
}
