package com.inspur.emmcloud.web.plugin.filetransfer.filemanager.bean;

public class FileBean {

    private String name;
    private String path;
    private FileType fileType;
    private int childCount;
    private long size;
    private int holderType;

    public FileBean(String path) {
        this.path = path;
    }

    public FileBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getHolderType() {
        return holderType;
    }

    public void setHolderType(int holderType) {
        this.holderType = holderType;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof FileBean))
            return false;

        final FileBean otherFileBean = (FileBean) other;
        return getPath().equals(otherFileBean.getPath());
    }
}