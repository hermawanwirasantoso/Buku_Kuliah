package com.example.bukukuliah.ui.editor.images;


import com.google.firebase.Timestamp;

public class SavedImage {
    String imgUrl;
    String desc;
    Timestamp timestamp;
    long page;

    public SavedImage(String imgUrl, String desc, Timestamp timestamp, long page){
        this.imgUrl = imgUrl;
        this.desc = desc;
        this.timestamp = timestamp;
        this.page = page;
    }
}
