package com.inspur.emmcloud.web.plugin.file;


import java.util.Comparator;


/**
 * 文件排序类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class FileComparator implements Comparator<FileInfo> {


    public int compare(FileInfo file1, FileInfo file2) {

        if (file1.IsDirectory && !file2.IsDirectory) {
            return -1000;
        } else if (!file1.IsDirectory && file2.IsDirectory) {
            return 1000;
        }
        // 相同类型按名称排序
        return file1.Name.compareTo(file2.Name);
    }
}