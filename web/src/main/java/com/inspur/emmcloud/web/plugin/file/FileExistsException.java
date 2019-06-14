package com.inspur.emmcloud.web.plugin.file;

@SuppressWarnings("serial")
public class FileExistsException extends Exception {

    public FileExistsException(String message) {
        super(message);
    }
}
