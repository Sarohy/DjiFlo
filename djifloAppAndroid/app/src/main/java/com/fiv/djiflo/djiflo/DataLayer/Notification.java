package com.fiv.djiflo.djiflo.DataLayer;

/**
 * Created by apple on 18/11/2017.
 */

public class Notification {
    private String userId;
    private String message;
    private String type;
    private String oUser;
    private String id;
    private String musicId;
    private String date;

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getoUser() {
        return oUser;
    }

    public void setoUser(String oUser) {
        this.oUser = oUser;
    }

    public String getId() {
        return id;
    }

    public String getMusicId() {
        return musicId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
