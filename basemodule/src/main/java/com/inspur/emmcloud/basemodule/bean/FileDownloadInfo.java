package com.inspur.emmcloud.basemodule.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by chenmch on 2019/9/13.
 */
@Table(name = "FileDownloadInfo")
public class FileDownloadInfo {
    @Column(name = "Id", isId = true)
    private int Id;
    @Column(name = "category")
    private String category;
    @Column(name = "categoryId")
    private String categoryId;
    @Column(name = "categoryFilename")
    private String categoryFilename;
    @Column(name = "filePath")
    private String filePath;

    public FileDownloadInfo() {

    }

    public FileDownloadInfo(String category, String categoryId, String categoryFilename, String filePath) {
        this.category = category;
        this.categoryId = categoryId;
        this.filePath = filePath;
        this.categoryFilename = categoryFilename;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCategoryFilename() {
        return categoryFilename;
    }

    public void setCategoryFilename(String categoryFilename) {
        this.categoryFilename = categoryFilename;
    }
}
