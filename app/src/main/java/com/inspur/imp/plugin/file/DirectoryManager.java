package com.inspur.imp.plugin.file;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;


public class DirectoryManager {

    /*
     * @description 检测文件或者目录是否已经存在
     *
     * @param name 待检测文件或者目录名
     *
     * @return true即已存在，false即不存在
     */
    public static boolean testFileExists(String name) {
        boolean status;

        // 如果SD卡存在
        if ((testSaveLocationExists()) && (!name.equals(""))) {
            File path = Environment.getExternalStorageDirectory();
            File newPath = constructFilePaths(path.toString(), name);
            status = newPath.exists();
        }
        // 如果没有SD卡
        else {
            status = false;
        }
        return status;
    }
    /*
     * @description 获得用户SD卡上的剩余空间值
	 * 
	 * @param checkInternal 是否允许应用程序访问内部存储
	 * @return  在SD卡上的剩余大小，单位是KB
	 */

    public static long getFreeDiskSpace(boolean checkInternal) {
        String status = Environment.getExternalStorageState();
        long freeSpace = 0;

        // 如果有SD卡
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            freeSpace = freeSpaceCalculation(Environment.getExternalStorageDirectory().getPath());
        } else if (checkInternal) {
            freeSpace = freeSpaceCalculation("/");
        }
        // 如果没有SD卡则直接返回-1或者不允许访问内部存储
        else {
            return -1;
        }

        return freeSpace;
    }

    /*
     * @description 计算空间的方法
     * @path  SD卡路径
     *
     * @return 获得可用的SD卡空间
     */
    @SuppressWarnings("deprecation")
    private static long freeSpaceCalculation(String path) {
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize / 1024;
    }

    /*
     * @description 查看SD卡是否存且可用
     *
     * @return trueSD卡存在 falseSD卡不存在
     */
    public static boolean testSaveLocationExists() {
        String sDCardStatus = Environment.getExternalStorageState();
        boolean status;

        // 如果SD卡的权限是允许访问的
        // 如果没有SD卡
        status = sDCardStatus.equals(Environment.MEDIA_MOUNTED);
        return status;
    }

    /*
     * @description 构建一个文件路径
     *
     * @param file1     文件路径
     * @param file2     创建文件所在的路径或者文件名称
     *
     * @return File object
     */
    private static File constructFilePaths(String file1, String file2) {
        File newPath;
        if (file2.startsWith(file1)) {
            newPath = new File(file2);
        } else {
            newPath = new File(file1 + "/" + file2);
        }
        return newPath;
    }
}
