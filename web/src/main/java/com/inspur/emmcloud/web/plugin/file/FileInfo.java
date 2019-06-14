package com.inspur.emmcloud.web.plugin.file;

/**
 * 文件信息类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class FileInfo {
    public String Name;//名字
    public String Path;//路径
    public long Size;//大小
    public boolean IsDirectory = false;//是否是目录（默认为否）
    public boolean canRead = false;//可读？
    public int FileCount = 0;//文件个数
    public int FolderCount = 0;//文件夹的个数

}