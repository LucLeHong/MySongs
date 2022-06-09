package com.tms.mysongs.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tms.mysongs.R;
import com.tms.mysongs.adapers.PlaylistAdapter;
import com.tms.mysongs.api.ApiService;
import com.tms.mysongs.listeners.IPlaySongListener;
import com.tms.mysongs.models.Playlist;
import com.tms.mysongs.models.Singer;
import com.tms.mysongs.models.Song;
import com.tms.mysongs.services.MyService;

import java.io.InputStream;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaylistActivity extends AppCompatActivity implements IPlaySongListener {
    private ImageView imgPlaylist, imgBack, imgSong, imgPlayOrPause , imgClear;
    private RecyclerView rvPlaylist;
    private Button btnPlayAll;
    private RelativeLayout layoutControlSong;
    private TextView txtNameSong, txtNameSinger;

    private Playlist playlist;
    private PlaylistAdapter playlistAdapter;
    private ArrayList<Song> songArrayList;
    private Song song;
    private Singer singer;
    private int isPlaying;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                song = (Song) bundle.getSerializable(getString(R.string.object_song));
                isPlaying = bundle.getInt(getString(R.string.status), 0);
                singer = (Singer) bundle.getSerializable(getString(R.string.object_singer));
                songArrayList = (ArrayList<Song>) bundle.getSerializable(getString(R.string.object_song_arraylist));
                int action = bundle.getInt(getString(R.string.action));

                handleAction(action);
            }
        }
    };

    private void handleAction(int action) {
        switch (action) {
            case MyService.ACTION_START:
                startSong();
                break;

            case MyService.ACTION_PAUSE:
                imgPlayOrPause.setImageResource(R.drawable.ic_play);
                isPlaying = -1;
                break;

            case MyService.ACTION_RESUME:
                imgPlayOrPause.setImageResource(R.drawable.ic_pause);
                isPlaying = 1;
                break;

            case MyService.ACTION_PREVIOUS:

            case MyService.ACTION_NEXT:
                checkSongIsPlaying();
                break;

            case MyService.ACTION_CLEAR:
                layoutControlSong.setVisibility(View.GONE);
                song = null;
                break;
        }
    }

    private void checkSongIsPlaying() {
        Intent intentService = new Intent(this, MyService.class);
        intentService.putExtra(getString(R.string.action), MyService.ACTION_START);
        startService(intentService);
    }

    private void startSong() {
        txtNameSong.setText(song.getNameSong());
        txtNameSinger.setText(singer.getNameSinger());
        new DownloadImageTask(imgSong).execute(song.getImgSong());

        if (isPlaying == -1) {
            imgPlayOrPause.setImageResource(R.drawable.ic_play);
        } else {
            imgPlayOrPause.setImageResource(R.drawable.ic_pause);
        }

        layoutControlSong.setVisibility(View.VISIBLE);
        setOnClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        imgPlaylist = findViewById(R.id.image_playlist);
        imgBack = findViewById(R.id.image_back);
        rvPlaylist = findViewById(R.id.recycler_view_playlist);
        btnPlayAll = findViewById(R.id.button_play_all);
        imgSong = findViewById(R.id.image_song);
        imgPlayOrPause = findViewById(R.id.image_play_or_pause);
        imgClear = findViewById(R.id.image_clear);
        layoutControlSong = findViewById(R.id.layout_control_song);
        txtNameSinger = findViewById(R.id.text_name_singer);
        txtNameSong = findViewById(R.id.text_name_song);

        checkSongIsPlaying();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(getString(R.string.send_main_activity)));

        setData();
        setOnClick();
    }

    private void setOnClick() {
        imgBack.setOnClickListener(view -> onBackPressed());

        btnPlayAll.setOnClickListener(view -> playAll());

        imgPlayOrPause.setOnClickListener(view -> {
            if (isPlaying == 1) {
                isPlaying = -1;
                imgPlayOrPause.setImageResource(R.drawable.ic_play);
                handleService(MyService.ACTION_PAUSE);
            } else {
                isPlaying = 1;
                imgPlayOrPause.setImageResource(R.drawable.ic_play);
                handleService(MyService.ACTION_RESUME);
            }
        });

        imgClear.setOnClickListener(view -> {
            isPlaying = -1;
            layoutControlSong.setVisibility(View.GONE);
            handleService(MyService.ACTION_CLEAR);
        });

        layoutControlSong.setOnClickListener(view -> playSong(songArrayList, song));
    }

    private void handleService(int action) {
        Bundle bundleService = new Bundle();
        bundleService.putInt(getString(R.string.action), action);
        bundleService.putSerializable(getString(R.string.object_singer), singer);
        bundleService.putSerializable(getString(R.string.object_song_arraylist), songArrayList);

        Intent intentService = new Intent(this, MyService.class);
        intentService.putExtras(bundleService);

        startService(intentService);
    }

    private void playAll() {
        if (songArrayList != null && songArrayList.size() > 0) {

            Bundle bundle = new Bundle();
            bundle.putSerializable(getString(R.string.object_song_arraylist), songArrayList);
            bundle.putSerializable(getString(R.string.object_song), songArrayList.get(0));

            Intent intent = new Intent(this, PlayingSongActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private void setData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        playlist = (Playlist) bundle.getSerializable(getString(R.string.object_playlist));
        singer = (Singer) bundle.getSerializable(getString(R.string.object_singer));
        songArrayList = (ArrayList<Song>) bundle.getSerializable(getString(R.string.object_song_arraylist));

        playlistAdapter = new PlaylistAdapter(songArrayList, PlaylistActivity.this);
        rvPlaylist.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        rvPlaylist.setAdapter(playlistAdapter);

        if (playlist != null) {
            new DownloadImageTask(imgPlaylist).execute(playlist.getImgPlaylist());
        }

        if (singer != null) {
            new DownloadImageTask(imgPlaylist).execute(singer.getImgSinger());
        }
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}