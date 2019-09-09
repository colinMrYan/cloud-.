package com.inspur.emmcloud.web.plugin.filetransfer.filemanager;

import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.bean.FileType;

import java.io.File;

/**
 * Created by ${zhaoyanjun} on 2017/1/11.
 */

public class FileUtil {

    /**
     * 获取文件类型
     *
     * @param file
     * @return
     */
    public static FileType getFileType(File file) {
        if (file.isDirectory()) {
            return FileType.directory;
        }
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".mp3") || fileName.endsWith(".amr") || fileName.endsWith(".wav")) {
            return FileType.music;
        }

        if (fileName.endsWith(".mp4") || fileName.endsWith(".avi")
                || fileName.endsWith(".3gp") || fileName.endsWith(".mov")
                || fileName.endsWith(".rmvb") || fileName.endsWith(".mkv")
                || fileName.endsWith(".flv") || fileName.endsWith(".rm")) {
            return FileType.video;
        }

        if (fileName.endsWith(".txt") || fileName.endsWith(".log") || fileName.endsWith(".xml")) {
            return FileType.txt;
        }

        if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
            return FileType.zip;
        }

        if (fileName.endsWith(".png") || fileName.endsWith(".gif")
                || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") || fileName.equals(".dng")) {
            return FileType.image;
        }

        if (fileName.endsWith(".apk")) {
            return FileType.apk;
        }
        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return FileType.word;
        }
        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            return FileType.excel;
        }
        if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
            return FileType.ppt;
        }
        if (fileName.endsWith(".pdf")) {
            return FileType.pdf;
        }
        return FileType.other;
    }

//    /**
//     * 文件按照名字排序
//     */
//    public static Comparator comparator = new Comparator<File>() {
//        @Override
//        public int compare(File file1 , File file2 ) {
//            if ( file1.isDirectory() && file2.isFile() ){
//                return -1 ;
//            }else if ( file1.isFile() && file2.isDirectory() ){
//                return 1 ;
//            }else {
//                return file1.getName().toLowerCase().compareTo( file2.getName().toString() ) ;
//            }
//        }
//    } ;

    /**
     * 获取文件的子文件个数
     *
     * @param file
     * @return
     */
    public static int getFileChildCount(File file) {
        int count = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isHidden()) continue;
                count++;
            }
        }
        return count;
    }

    /**
     * 文件大小转换
     *
     * @param size
     * @return
     */
    public static String sizeToChange(long size) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");  //字符格式化，为保留小数做准备

        double G = size * 1.0 / 1024 / 1024 / 1024;
        if (G >= 1) {
            return df.format(G) + " GB";
        }

        double M = size * 1.0 / 1024 / 1024;
        if (M >= 1) {
            return df.format(M) + " MB";
        }

        double K = size * 1.0 / 1024;
        if (K >= 1) {
            return df.format(K) + " KB";
        }

        return size + " B";
    }

}
