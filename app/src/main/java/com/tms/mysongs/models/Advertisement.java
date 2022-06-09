package com.tms.mysongs.models;

public class Advertisement {

    private int idAdvertisement;
    private String imgAdvertisement;
    private int idSong;

    public Advertisement(int idAdvertisement, String imgAdvertisement, int idSong) {
        this.idAdvertisement = idAdvertisement;
        this.imgAdvertisement = imgAdvertisement;
        this.idSong = idSong;
    }

    public int getIdAdvertisement() {
        return idAdvertisement;
    }

    public void setIdAdvertisement(int idAdvertisement) {
        this.idAdvertisement = idAdvertisement;
    }

    public String getImgAdvertisement() {
        return imgAdvertisement;
    }

    public void setImgAdvertisement(String imgAdvertisement) {
        this.imgAdvertisement = imgAdvertisement;
    }

    public int getIdSong() {
        return idSong;
    }

    public void setIdSong(int idSong) {
        this.idSong = idSong;
    }
}
