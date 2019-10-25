package com.inspur.emmcloud.widget.filemanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileListResult;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.widget.filemanager.adapter.TitleAdapter;
import com.inspur.emmcloud.widget.filemanager.adapter.base.RecyclerViewAdapter;
import com.inspur.emmcloud.widget.filemanager.bean.TitlePath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by libaochao on 2019/10/23.
 */

public class VolumeFileManagerFragment extends Fragment {

    protected VolumeFileAdapter fileAdapter;
    protected List<VolumeFile> volumeFileList = new ArrayList<>();//云盘列表
    protected String currentDirAbsolutePath;//当前文件夹路径
    protected GetVolumeFileListResult getVolumeFileListResult;
    protected String fileFilterType = "";  //显示的文件类型
    protected List<VolumeFile> volumeFilePath = new ArrayList<>();//云盘列表
    boolean isBack = false;
    private RecyclerView titleRecyclerview;
    private RecyclerView fileRecyclerView;
    private TitleAdapter titleAdapter;
    private LoadingDialog loadingDlg;
    private MyAppAPIService apiServiceBase;
    private Volume volume;
    private List<TitlePath> backlist = new ArrayList<>();

    private int maximum = 1;
    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDirAbsolutePath = "/";
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_file_manager, null);
        titleRecyclerview = rootView.findViewById(R.id.rcv_title);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        titleRecyclerview.setLayoutManager(layoutManager);
        titleAdapter = new TitleAdapter(getActivity(), new ArrayList<TitlePath>());
        titleRecyclerview.setAdapter(titleAdapter);
        fileRecyclerView = rootView.findViewById(R.id.rcv_file);
        fileAdapter = new VolumeFileAdapter(getContext(), volumeFileList);
        fileAdapter.setCurrentDirAbsolutePath(currentDirAbsolutePath);
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        fileRecyclerView.setAdapter(fileAdapter);
        refreshTitleState("我的网盘", currentDirAbsolutePath);
        loadingDlg = new LoadingDialog(getContext());
        apiServiceBase = new MyAppAPIService(getContext());
        apiServiceBase.setAPIInterface(new WebServiceBase());
        fileAdapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (volumeFileList.get(position).getType().equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
                    isBack = false;
                    refreshTitleState(volumeFileList.get(position).getName(), currentDirAbsolutePath);
                    currentDirAbsolutePath = titleAdapter.getCurrentPath();  /**获取当前路径**/
                    getVolumeFileList(true);
                } else if (volumeFileList.get(position).getType().equals(VolumeFile.FILE_TYPE_REGULAR)) {

                } else {

                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onItemDropDownImgClick(View view, int position) {

            }

            @Override
            public void onItemOperationTextClick(View view, int position) {

            }

            @Override
            public void onSelectedItemClick(View view, int position) {

            }
        });

        titleAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                isBack = true;
                backlist.clear();
                int count = titleAdapter.getItemCount();
                int removeCount = count - position - 1;
                for (int i = 0; i < removeCount; i++) {
                    backlist.add(titleAdapter.getLast());
                    titleAdapter.removeLast();
                }
                /**删除完成后刷新数据**/
                currentDirAbsolutePath = titleAdapter.getCurrentPath();  /**获取当前路径**/
                getVolumeFileList(true);
            }
        });

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


    public void setMyVolume(Volume volume) {
        this.volume = volume;
        getVolumeFileList(false);
    }


    void refreshTitleState(String title, String path) {
        TitlePath filePath = new TitlePath();
        filePath.setNameState(title + "/");
        filePath.setPath(path);
        titleAdapter.addItem(filePath);
        titleRecyclerview.smoothScrollToPosition(titleAdapter.getItemCount());
    }

    /**
     * 获取文件列表
     */
    protected void getVolumeFileList(boolean isShowDlg) {
        if (NetUtils.isNetworkConnected(getContext())) {
            loadingDlg.show(isShowDlg);
            String path = currentDirAbsolutePath;
            if (currentDirAbsolutePath.length() > 1) {
                path = currentDirAbsolutePath.substring(0, currentDirAbsolutePath.length() - 1);
            }
            apiServiceBase.getVolumeFileList(volume.getId(), path);
        }
    }

    private class WebServiceBase extends APIInterfaceInstance {
        @Override
        public void returnVolumeFileListSuccess(GetVolumeFileListResult volumeFileListResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            getVolumeFileListResult = volumeFileListResult;
            if (StringUtils.isBlank(fileFilterType)) {
                volumeFileList = getVolumeFileListResult.getVolumeFileList();
            } else if (fileFilterType.equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
                volumeFileList = getVolumeFileListResult.getVolumeFileDirectoryList();
            } else {
                volumeFileList = getVolumeFileListResult.getVolumeFileFilterList(fileFilterType);
            }
            fileAdapter.setVolumeFileList(volumeFileList);
            fileAdapter.setCurrentDirAbsolutePath(currentDirAbsolutePath);
            fileAdapter.notifyDataSetChanged();
            titleAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnVolumeFileListFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (isBack) {
                if (backlist.size() > 0) {
                    Collections.reverse(backlist);
                    for (int i = 0; i < backlist.size(); i++) {
                        titleAdapter.addItem(backlist.get(i));
                        currentDirAbsolutePath = titleAdapter.getCurrentPath();
                    }
                    backlist.clear();
                }
            } else {
                if (titleAdapter.getItemCount() > 1) {
                    titleAdapter.removeLast();
                    currentDirAbsolutePath = titleAdapter.getCurrentPath();
                }
            }

        }

    }

}
