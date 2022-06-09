package com.tms.mysongs.listeners;

import com.tms.mysongs.models.Song;

import java.util.ArrayList;

public interface IPlaySongListener {
    void playSong(ArrayList<Song> songArrayList, Song song);
}
