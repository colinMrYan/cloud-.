package com.inspur.imp.plugin.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.imp.api.Res;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


/**
 * 文件浏览帮助类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class FileActivityHelper {

    /**
     * 获取一个文件夹下的所有文件
     **/
    public static ArrayList<FileInfo> getFiles(Activity activity, String path) {
        File f = new File(path);
        File[] files = f.listFiles();
        if (files == null) {
            ToastUtils.show(activity, "文件为空,无法打开!\t" + path);
            return null;
        }

        ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
        // 获取文件列表
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileInfo fileInfo = new FileInfo();
            fileInfo.Name = file.getName();
            fileInfo.IsDirectory = file.isDirectory();
            fileInfo.Path = file.getPath();
            fileInfo.Size = file.length();
            fileInfo.canRead = file.canRead();
            fileList.add(fileInfo);
        }

        // 排序
        Collections.sort(fileList, new FileComparator());

        return fileList;
    }

    //获取文件信息
    public static void fileInfo(Context con, File f) {
        View layout = LayoutInflater.from(con).inflate(Res.getLayoutID("fileinfo"), null);
        FileInfo info = FileUtil.getFileInfo(f);
        ((TextView) layout.findViewById(Res.getWidgetID("file_name"))).setText("" + f.getName());
        ((TextView) layout.findViewById(Res.getWidgetID("file_size"))).setText("" + FileUtil.formetFileSize(info.Size));
        ((TextView) layout.findViewById(Res.getWidgetID("file_lastmodified"))).setText("" + new Date(f.lastModified()).toLocaleString());
        ((TextView) layout.findViewById(Res.getWidgetID("file_contents"))).setText("Folder " + info.FolderCount + ", File " + info.FileCount);

        new AlertDialog.Builder(con)
                .setIcon(Res.getDrawable("plugin_file_dialogtitle_icon"))
                .setTitle("该文件或文件夹的详细信息如下所示:")
                .setView(layout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                })
                .show();

    }

}
