package com.tms.mysongs.models;

import java.io.Serializable;

public class Singer implements Serializable {
    private int idSinger;
    private String nameSinger, imgSinger;

    public Singer(int idSinger, String nameSinger, String imgSinger) {
        this.idSinger = idSinger;
        this.nameSinger = nameSinger;
        this.imgSinger = imgSinger;
    }

    public int getIdSinger() {
        return idSinger;
    }

    public void setIdSinger(int idSinger) {
        this.idSinger = idSinger;
    }

    public String getNameSinger() {
        return nameSinger;
    }

    public void setNameSinger(String nameSinger) {
        this.nameSinger = nameSinger;
    }

    public String getImgSinger() {
        return imgSinger;
    }

    public void setImgSinger(String imgSinger) {
        this.imgSinger = imgSinger;
    }
}
