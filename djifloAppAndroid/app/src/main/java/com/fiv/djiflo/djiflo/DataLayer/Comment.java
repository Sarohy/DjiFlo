package com.fiv.djiflo.djiflo.DataLayer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * Created by apple on 28/09/2017.
 */

public class Comment {
    private String comment;
    private String date;
    private String userId;
    private String songId;

    public Comment() {
    }
    public Comment(String comment) {
        this.comment = comment;
        Date d=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy HH:MM:ss");
        this.date=sdf.format(d);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }
}
