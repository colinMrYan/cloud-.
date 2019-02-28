package com.inspur.imp.plugin.file;

@SuppressWarnings("serial")
public class NoModificationAllowedException extends Exception {

    public NoModificationAllowedException(String message) {
        super(message);
    }
}
