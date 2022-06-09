package com.tms.mysongs.models;

import java.io.Serializable;

public class Playlist implements Serializable {

    private int idPlaylist;
    private String namePlaylist, imgPlaylist;

    public Playlist(int idPlaylist, String namePlaylist, String imgPlaylist) {
        this.idPlaylist = idPlaylist;
        this.namePlaylist = namePlaylist;
        this.imgPlaylist = imgPlaylist;
    }

    public int getIdPlaylist() {
        return idPlaylist;
    }

    public void setIdPlaylist(int idPlaylist) {
        this.idPlaylist = idPlaylist;
    }

    public String getNamePlaylist() {
        return namePlaylist;
    }

    public void setNamePlaylist(String namePlaylist) {
        this.namePlaylist = namePlaylist;
    }

    public String getImgPlaylist() {
        return imgPlaylist;
    }

    public void setImgPlaylist(String imgPlaylist) {
        this.imgPlaylist = imgPlaylist;
    }
}
