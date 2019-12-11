package com.example.bukukuliah.ui.catatan;

import com.google.firebase.firestore.ServerTimestamp;


public class Catatan {
    public String uid;

    public String page;
    public String content;
    @ServerTimestamp
    public Object timestamp;

    public Catatan(String uid, String page, String content, Object timestamp) {
        this.uid = uid;
        this.page = page;
        this.content = content;
        this.timestamp = timestamp;
    }
}
