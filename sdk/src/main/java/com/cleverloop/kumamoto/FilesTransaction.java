package com.cleverloop.kumamoto;

import java.io.File;
import java.util.List;

/**
 * Created by xuemingxiang on 16/5/12.
 */
public class FilesTransaction {

    private List<File> files;

    public FilesTransaction(List<File> files) {
        this.files = files;
    }

    public void finish() {
        for (File file : files) {
            file.delete();
        }
    }
}
