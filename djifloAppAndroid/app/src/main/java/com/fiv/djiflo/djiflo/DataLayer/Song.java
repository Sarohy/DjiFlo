package com.fiv.djiflo.djiflo.DataLayer;

import java.util.ArrayList;

/**
 * Created by apple on 05/09/2017.
 */

public class Song {
    private String artURL,artist,category,duration,name,musicURL,stream,date, fileName, musicArtFile;
    private String musicId;

    public String getMusicArtFile() {
        return musicArtFile;
    }

    public void setMusicArtFile(String musicArtFile) {
        this.musicArtFile = musicArtFile;
    }

    public Song() {
    }

    public String getArtURL() {
        return artURL;
    }

    public void setArtURL(String artURL) {
        this.artURL = artURL;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMusicURL() {
        return musicURL;
    }

    public void setMusicURL(String musicURL) {
        this.musicURL = musicURL;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public void incStream() {
        int s= Integer.parseInt(stream);
        s++;
        stream= String.valueOf(s);
    }

}
