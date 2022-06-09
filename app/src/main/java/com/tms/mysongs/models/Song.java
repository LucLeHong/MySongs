package com.tms.mysongs.models;

import java.io.Serializable;

public class Song implements Serializable {

    private int idSong, idAlbum, idType, idSinger, favorites;
    private String nameSong, imgSong, linkSong;

    public Song(int idSong, int idAlbum, int idType, int idSinger, String nameSong, String imgSong, String linkSong, int favorites) {
        this.idSong = idSong;
        this.idAlbum = idAlbum;
        this.idType = idType;
        this.idSinger = idSinger;
        this.nameSong = nameSong;
        this.imgSong = imgSong;
        this.linkSong = linkSong;
        this.favorites = favorites;
    }

    public int getIdSong() {
        return idSong;
    }

    public void setIdSong(int idSong) {
        this.idSong = idSong;
    }

    public int getIdAlbum() {
        return idAlbum;
    }

    public void setIdAlbum(int idAlbum) {
        this.idAlbum = idAlbum;
    }

    public int getIdType() {
        return idType;
    }

    public void setIdType(int idType) {
        this.idType = idType;
    }

    public int getIdSinger() {
        return idSinger;
    }

    public void setIdSinger(int idSinger) {
        this.idSinger = idSinger;
    }

    public String getNameSong() {
        return nameSong;
    }

    public void setNameSong(String nameSong) {
        this.nameSong = nameSong;
    }

    public String getImgSong() {
        return imgSong;
    }

    public void setImgSong(String imgSong) {
        this.imgSong = imgSong;
    }

    public String getLinkSong() {
        return linkSong;
    }

    public void setLinkSong(String linkSong) {
        this.linkSong = linkSong;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }
}
