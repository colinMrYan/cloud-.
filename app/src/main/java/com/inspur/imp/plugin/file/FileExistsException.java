package com.inspur.imp.plugin.file;

@SuppressWarnings("serial")
public class FileExistsException extends Exception {

    public FileExistsException(String message) {
        super(message);
    }
}
