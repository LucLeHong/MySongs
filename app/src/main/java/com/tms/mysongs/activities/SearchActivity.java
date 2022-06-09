package com.tms.mysongs.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tms.mysongs.R;
import com.tms.mysongs.adapers.PlaylistAdapter;
import com.tms.mysongs.adapers.PlaylistsAdapter;
import com.tms.mysongs.adapers.SingersAdapter;
import com.tms.mysongs.api.ApiService;
import com.tms.mysongs.listeners.IPlaySongListener;
import com.tms.mysongs.listeners.IPlaylistsListener;
import com.tms.mysongs.listeners.ISingersListener;
import com.tms.mysongs.models.Playlist;
import com.tms.mysongs.models.Singer;
import com.tms.mysongs.models.Song;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity implements ISingersListener, IPlaylistsListener, IPlaySongListener {
    private AutoCompleteTextView autoEdtSearch;
    private ImageView imgBack;
    private RecyclerView rvSingers, rvPlaylists, rvSongs;
    private TextView txtResultSingers, txtResultPlaylists, txtResultSongs;

    private ArrayList<String> stringArrayList;
    private SingersAdapter singersAdapter;
    private PlaylistsAdapter playlistsAdapter;
    private PlaylistAdapter songsAdapter;
    private ArrayList<Song> songArrayList;
    private Song song;
    private int isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        autoEdtSearch = findViewById(R.id.auto_edit_text_search);
        imgBack = findViewById(R.id.image_back);
        rvSingers = findViewById(R.id.recycler_view_singers);
        rvPlaylists = findViewById(R.id.recycler_view_playlists);
        rvSongs = findViewById(R.id.recycler_view_songs);
        txtResultSingers = findViewById(R.id.text_result_singers);
        txtResultPlaylists = findViewById(R.id.text_result_playlists);
        txtResultSongs = findViewById(R.id.text_result_songs);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        stringArrayList = bundle.getStringArrayList(getString(R.string.object_auto_text));
        String[] strings = stringArrayList.toArray(new String[0]);


        if (stringArrayList != null) {
            autoEdtSearch.setAdapter(new ArrayAdapter<>(
                    SearchActivity.this,
                    R.layout.layout_item_auto,
                    strings
            ));
        }

        imgBack.setOnClickListener(view -> onBackPressed());

        autoEdtSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (!autoEdtSearch.getText().toString().trim().equals("")) {
                getResult(autoEdtSearch.getText().toString().trim());
                return true;
            } else {
                autoEdtSearch.setError(getString(R.string.error_empty_search));
                return false;
            }
        });
    }

    private void getResult(String name) {
        getResultSingers(name);
        getResultSongs(name);
        getResultPlaylists(name);
    }

    private void getResultPlaylists(String namePlaylist) {
        ApiService.apiService.getPlaylistsByName(namePlaylist).enqueue(new Callback<ArrayList<Playlist>>() {
            @Override
            public void onResponse(Call<ArrayList<Playlist>> call, Response<ArrayList<Playlist>> response) {
                ArrayList<Playlist> playlistArrayList = response.body();
                if (playlistArrayList != null && playlistArrayList.size() > 0) {
                    playlistsAdapter = new PlaylistsAdapter(playlistArrayList, SearchActivity.this);
                    rvPlaylists.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
                    rvPlaylists.setAdapter(playlistsAdapter);

                    txtResultPlaylists.setVisibility(View.VISIBLE);
                    rvPlaylists.setVisibility(View.VISIBLE);
                } else {
                    txtResultPlaylists.setVisibility(View.GONE);
                    rvPlaylists.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Playlist>> call, Throwable t) {
                txtResultPlaylists.setVisibility(View.GONE);
                rvPlaylists.setVisibility(View.GONE);
            }
        });
    }

    private void getResultSongs(String nameSong) {
        ApiService.apiService.getSongsByName(nameSong).enqueue(new Callback<ArrayList<Song>>() {
            @Override
            public void onResponse(Call<ArrayList<Song>> call, Response<ArrayList<Song>> response) {
                ArrayList<Song> songArrayList = response.body();
                if (songArrayList != null && songArrayList.size() > 0) {
                    songsAdapter = new PlaylistAdapter(songArrayList, SearchActivity.this);
                    rvSongs.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
                    rvSongs.setAdapter(songsAdapter);

                    txtResultSongs.setVisibility(View.VISIBLE);
                    rvSongs.setVisibility(View.VISIBLE);
                } else {
                    txtResultSongs.setVisibility(View.GONE);
                    rvSongs.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Song>> call, Throwable t) {
                txtResultSongs.setVisibility(View.GONE);
                rvSongs.setVisibility(View.GONE);
            }
        });
    }

    private void getResultSingers(String nameSinger) {
        ApiService.apiService.getSingersByName(nameSinger).enqueue(new Callback<ArrayList<Singer>>() {
            @Override
            public void onResponse(Call<ArrayList<Singer>> call, Response<ArrayList<Singer>> response) {
                ArrayList<Singer> singerArrayList = response.body();
                if (singerArrayList != null && singerArrayList.size() > 0) {
                    singersAdapter = new SingersAdapter(singerArrayList, SearchActivity.this);
                    rvSingers.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
                    rvSingers.setAdapter(singersAdapter);

                    txtResultSingers.setVisibility(View.VISIBLE);
                    rvSingers.setVisibility(View.VISIBLE);
                } else {
                    txtResultSingers.setVisibility(View.GONE);
                    rvSingers.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Singer>> call, Throwable t) {
                txtResultSingers.setVisibility(View.GONE);
                rvSingers.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void toPlaylistActivity(Singer singer) {
        getSongArraylistBySinger(singer);
    }

    private void getSongArraylistBySinger(Singer singer) {
        ApiService.apiService.getSongsByIdSinger(singer.getIdSinger()).enqueue(new Callback<ArrayList<Song>>() {
            @Override
            public void onResponse(Call<ArrayList<Song>> call, Response<ArrayList<Song>> response) {
                songArrayList = response.body();

                Bundle bundle = new Bundle();
                bundle.putSerializable(getString(R.string.object_singer), singer);
                bundle.putSerializable(getString(R.string.object_song_arraylist), songArrayList);

                Intent intent = new Intent(SearchActivity.this, PlaylistActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<ArrayList<Song>> call, Throwable t) {

            }
        });
    }

    @Override
    public void toPlaylistActivity(Playlist playlist) {
        getSongArraylistByPlaylist(playlist);
    }

    private void getSongArraylistByPlaylist(Playlist playlist) {
        ApiService.apiService.getSongsByIdPlaylist(playlist.getIdPlaylist()).enqueue(new Callback<ArrayList<Song>>() {
            @Override
            public void onResponse(Call<ArrayList<Song>> call, Response<ArrayList<Song>> response) {
                songArrayList = response.body();

                Bundle bundle = new Bundle();
                bundle.putSerializable(getString(R.string.object_playlist), playlist);
                bundle.putSerializable(getString(R.string.object_song_arraylist), songArrayList);

                Intent intent = new Intent(SearchActivity.this, PlaylistActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<ArrayList<Song>> call, Throwable t) {

            }
        });
    }

    @Override
    public void playSong(ArrayList<Song> songArrayList, Song song) {
        Intent intent = new Intent(this, PlayingSongActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.object_song_arraylist), songArrayList);
        bundle.putSerializable(getString(R.string.object_song), song);

        if (this.song != null && song.getIdSong() == this.song.getIdSong()) {
            bundle.putInt(getString(R.string.status), isPlaying);
        }

        intent.putExtras(bundle);
        startActivity(intent);
    }
}