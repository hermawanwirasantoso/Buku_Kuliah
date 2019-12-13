package com.example.bukukuliah.ui.editor.images;


import com.google.firebase.Timestamp;

public class SavedFile {
    String fileUrl;
    String desc;
    Timestamp timestamp;
    long page;

    public SavedFile(String fileUrl, String desc, Timestamp timestamp, long page){
        this.fileUrl = fileUrl;
        this.desc = desc;
        this.timestamp = timestamp;
        this.page = page;
    }
}
