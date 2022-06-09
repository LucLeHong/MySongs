package com.tms.mysongs.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tms.mysongs.R;
import com.tms.mysongs.api.ApiService;
import com.tms.mysongs.models.Singer;
import com.tms.mysongs.models.Song;
import com.tms.mysongs.services.MyService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayingSongActivity extends AppCompatActivity {
    private final int ANIMATION_DURATION = 20000;
    private final int ANIMATION_TO_DERGREES = 360;
    private final int TIME_DELAY = 0;
    private final int TIME_PERIOD = 500;

    private TextView txtNameSong, txtNameSinger, txtCurrentTime, txtMaxTime;
    private ImageView imgSong, imgPlayOrPause, imgForward, imgRewind, imgNext, imgPrevios, imgShuffle, imgLoop, imgBack, imgClear;
    private SeekBar sbSong;

    private ArrayList<Song> songArrayList;
    private Song song;
    private Singer singer;
    private int isPlaying; // #1: is playing #-1: paused #0: when from MainActivity to this

    private ObjectAnimator objectAnimator;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                song = (Song) bundle.getSerializable(getString(R.string.object_song));
                isPlaying = bundle.getInt(getString(R.string.status), 0);
                int action = bundle.getInt(getString(R.string.action));

                handleAction(action);
            }
        }
    };

    private void handleAction(int action) {
        switch (action) {
            case MyService.ACTION_PAUSE:
                objectAnimator.pause();
                isPlaying = -1;
                imgPlayOrPause.setImageResource(R.drawable.ic_play);
                break;

            case MyService.ACTION_RESUME:
                objectAnimator.resume();
                isPlaying = 1;
                imgPlayOrPause.setImageResource(R.drawable.ic_pause);
                break;

            case MyService.ACTION_PREVIOUS:

            case MyService.ACTION_NEXT:
                isPlaying = 1;
                setDataSong();
                break;

            case MyService.ACTION_CLEAR:
                song = null;
                songArrayList = null;
                isPlaying = 0;

                onBackPressed();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_song);

        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();
        songArrayList = (ArrayList<Song>) bundle.getSerializable(getString(R.string.object_song_arraylist));
        song = (Song) bundle.getSerializable(getString(R.string.object_song));
        isPlaying = bundle.getInt(getString(R.string.status), 0);

        txtNameSong = findViewById(R.id.text_name_song);
        txtNameSinger = findViewById(R.id.text_name_singer);
        txtCurrentTime = findViewById(R.id.text_current_time);
        txtMaxTime = findViewById(R.id.text_max_time);
        imgSong = findViewById(R.id.image_song);
        imgPlayOrPause = findViewById(R.id.image_play_or_pause);
        imgForward = findViewById(R.id.image_forward);
        imgRewind = findViewById(R.id.image_rewind);
        imgNext = findViewById(R.id.image_next);
        imgPrevios = findViewById(R.id.image_previous);
        imgShuffle = findViewById(R.id.image_shuffle);
        imgLoop = findViewById(R.id.image_loop);
        imgBack = findViewById(R.id.image_back);
        imgClear = findViewById(R.id.image_clear);
        sbSong = findViewById(R.id.seekbar_song);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(getString(R.string.send_playing_song_activities)));

        if (song != null) {
            setDataSong();
        }
    }

    private void setDataSong() {
        txtNameSong.setText(song.getNameSong());

        getNameSinger();
    }

    private void getNameSinger() {
        ApiService.apiService.getSingerById(song.getIdSinger()).enqueue(new Callback<Singer>() {
            @Override
            public void onResponse(Call<Singer> call, Response<Singer> response) {
                singer = response.body();
                txtNameSinger.setText(singer.getNameSinger());
                new DownloadImageTask(imgSong).execute(song.getImgSong());
            }

            @Override
            public void onFailure(Call<Singer> call, Throwable t) {
                txtNameSinger.setVisibility(View.INVISIBLE);
            }
        });
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

            if (songArrayList == null) {
                getSongArrayList();
                return;
            }

            setDataByIsPlaying();
        }
    }

    private void getSongArrayList() {
        ApiService.apiService.getSongs().enqueue(new Callback<ArrayList<Song>>() {
            @Override
            public void onResponse(Call<ArrayList<Song>> call, Response<ArrayList<Song>> response) {
                songArrayList = response.body();
                setDataByIsPlaying();
            }

            @Override
            public void onFailure(Call<ArrayList<Song>> call, Throwable t) {
                songArrayList = null;
            }
        });
    }

    private void setDataByIsPlaying() {
        playSong();
        if (isPlaying == 0) {
            isPlaying = 1;
        }

        if (isPlaying == 1) {
            imgPlayOrPause.setImageResource(R.drawable.ic_pause);
        } else {
            imgPlayOrPause.setImageResource(R.drawable.ic_play);
        }
        startAnimationImgSong();
        updateTimeSeekbar();
    }

    private void playSong() {
        Intent intent = new Intent(this, MyService.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.object_song_arraylist), songArrayList);
        bundle.putSerializable(getString(R.string.object_song), song);
        bundle.putSerializable(getString(R.string.object_singer), singer);

        intent.putExtras(bundle);
        startService(intent);
    }

    private void startAnimationImgSong() {
        if (objectAnimator == null) {
            objectAnimator = ObjectAnimator.ofFloat(imgSong, View.ROTATION, ANIMATION_TO_DERGREES)
                    .setDuration(ANIMATION_DURATION);
            objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
            objectAnimator.setRepeatMode(ValueAnimator.RESTART);
            objectAnimator.setInterpolator(new LinearInterpolator());
            objectAnimator.start();

            setOnClick();
        }

        if (isPlaying == -1) {
            objectAnimator.pause();
        } else {
            objectAnimator.resume();
        }
    }

    private void updateTimeSeekbar() {
        Handler handler = new Handler();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    if (MyService.mediaPlayer != null) {
                        txtCurrentTime.setText(getTimeByDuration(MyService.mediaPlayer.getCurrentPosition()));
                        txtMaxTime.setText(getTimeByDuration(MyService.mediaPlayer.getDuration()));

                        sbSong.setMax(MyService.mediaPlayer.getDuration());
                        sbSong.setProgress(MyService.mediaPlayer.getCurrentPosition());

                        if (MyService.mediaPlayer.getDuration() <= MyService.mediaPlayer.getCurrentPosition()) {
                            handleService(MyService.ACTION_NEXT);
                        }
                    }
                });
            }
        }, TIME_DELAY, TIME_PERIOD);
    }

    private void setOnClick() {
        imgPlayOrPause.setOnClickListener(view -> {
            playOrPauseSong();
        });

        imgNext.setOnClickListener(view -> nextOrPreviousSong(true));

        imgPrevios.setOnClickListener(view -> nextOrPreviousSong(false));

        imgForward.setOnClickListener(view -> forwardOrRewind(true));

        imgRewind.setOnClickListener(view -> forwardOrRewind(false));

        imgClear.setOnClickListener(view -> clearSong());

        imgBack.setOnClickListener(view -> onBackPressed());

        sbSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    MyService.mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void forwardOrRewind(boolean isRewind) { // #true is rewind #false is forward
        if (isRewind) {
            MyService.mediaPlayer.seekTo(MyService.mediaPlayer.getCurrentPosition() + 10000);
        } else {
            MyService.mediaPlayer.seekTo(MyService.mediaPlayer.getCurrentPosition() - 10000);
        }
    }

    private String getTimeByDuration(int duration) {
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        return String.format("%02d:%02d", min, sec);
    }

    private void clearSong() {
        handleService(MyService.ACTION_CLEAR);
    }

    private void nextOrPreviousSong(boolean isNext) {
        if (isNext) {
            handleService(MyService.ACTION_NEXT);
        } else {
            handleService(MyService.ACTION_PREVIOUS);
        }
    }

    private void playOrPauseSong() {
        if (isPlaying == -1) {
            handleService(MyService.ACTION_RESUME);
        } else {
            handleService(MyService.ACTION_PAUSE);
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleService(MyService.ACTION_START);
    }
}