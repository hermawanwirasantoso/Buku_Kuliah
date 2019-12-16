package com.example.bukukuliah.ui.editor.images;


import com.google.firebase.Timestamp;

public class SavedFile {
    String fileUrl;
    String desc;
    Timestamp timestamp;
    long page;
    String name;
    String uid;

    public SavedFile(String fileUrl, String desc, Timestamp timestamp, long page, String filename, String uid){
        this.fileUrl = fileUrl;
        this.desc = desc;
        this.timestamp = timestamp;
        this.page = page;
        this.name = filename;
        this.uid = uid;
    }
}
