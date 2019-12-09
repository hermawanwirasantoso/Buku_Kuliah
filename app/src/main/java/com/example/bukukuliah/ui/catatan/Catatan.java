package com.example.bukukuliah.ui.catatan;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;


public class Catatan {
    public String page;
    public String content;
    @ServerTimestamp
    public Object timestamp;

    public Catatan(String page, String content, Object timestamp) {
        this.page = page;
        this.content = content;
        this.timestamp = timestamp;
    }
}
