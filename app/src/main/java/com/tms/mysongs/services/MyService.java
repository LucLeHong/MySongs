package com.tms.mysongs.services;

import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tms.mysongs.R;
import com.tms.mysongs.activities.PlayingSongActivity;
import com.tms.mysongs.api.ApiService;
import com.tms.mysongs.aplications.MyApplication;
import com.tms.mysongs.models.Singer;
import com.tms.mysongs.models.Song;
import com.tms.mysongs.receiver.MyReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyService extends Service {
    public static final int ACTION_PAUSE = 1;
    public static final int ACTION_RESUME = 2;
    public static final int ACTION_CLEAR = 3;
    public static final int ACTION_PREVIOUS = 4;
    public static final int ACTION_NEXT = 5;
    public static final int ACTION_START = 6;

    private Song currentSong;
    private ArrayList<Song> currentSongArrayList;
    private Singer currentSinger;
    private int isPlaying;

    public static MediaPlayer mediaPlayer;
    private NotificationCompat.Builder notification;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        ArrayList<Song> songArrayList = (ArrayList<Song>) bundle.getSerializable(getString(R.string.object_song_arraylist));
        Song song = (Song) bundle.getSerializable(getString(R.string.object_song));
        Singer singer = (Singer) bundle.getSerializable(getString(R.string.object_singer));

        if (song != null && songArrayList != null && singer != null) {
            playSong(song);
            currentSinger = singer;
            currentSongArrayList = songArrayList;

            sendNotification(song, currentSinger);
        }

        int action = intent.getIntExtra(getString(R.string.action), 0);
        handleAction(action);

        return START_NOT_STICKY;
    }

    private void sendActivities(int action, String nameBroadcast) {
        Intent intent = new Intent(nameBroadcast);

        Bundle bundle = new Bundle();
        bundle.putInt(getString(R.string.status), isPlaying);
        bundle.putInt(getString(R.string.action), action);
        bundle.putSerializable(getString(R.string.object_singer), currentSinger);
        bundle.putSerializable(getString(R.string.object_song), currentSong);
        bundle.putSerializable(getString(R.string.object_song_arraylist), currentSongArrayList);

        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendNotification(Song song, Singer singer) {
        Intent intent = new Intent(this, PlayingSongActivity.class);

        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");

        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.object_song_arraylist), currentSongArrayList);
        bundle.putSerializable(getString(R.string.object_song), song);
        bundle.putInt(getString(R.string.status), isPlaying);
        bundle.putSerializable(getString(R.string.object_singer), singer);
        intent.putExtras(bundle);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);

        notification = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setSound(null)
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(song.getNameSong())
                .setContentText(singer.getNameSinger())
                .setContentIntent(pendingIntent);

        notification.addAction(R.drawable.ic_previous, getString(R.string.previous), getPendingIntent(ACTION_PREVIOUS)); // #0 button previous
        if (isPlaying == 1) {
            notification.addAction(R.drawable.ic_pause, getString(R.string.pause), getPendingIntent(ACTION_PAUSE)); // #1 button pause
        } else {
            notification.addAction(R.drawable.ic_play, getString(R.string.play), getPendingIntent(ACTION_RESUME)); // #1 button play
        }
        notification.addAction(R.drawable.ic_next, getString(R.string.next), getPendingIntent(ACTION_NEXT)); // #2 button next
        notification.addAction(R.drawable.ic_clear, getString(R.string.clear), getPendingIntent(ACTION_CLEAR)); // #3 button clear


        notification.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1, 3)
                .setMediaSession(mediaSessionCompat.getSessionToken())
        );

        new DownloadImageTask().execute(song.getImgSong());
    }

    private PendingIntent getPendingIntent(int action) {
        Intent intentReceiver = new Intent(this, MyReceiver.class);

        intentReceiver.putExtra(getString(R.string.action), action);

        return PendingIntent.getBroadcast(this.getApplicationContext(), action, intentReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void handleAction(int action) {
        switch (action) {
            case ACTION_PAUSE:
                pauseSong();
                break;

            case ACTION_RESUME:
                resumeSong();
                break;

            case ACTION_CLEAR:
                stopSelf();
                sendActivities(ACTION_CLEAR, getString(R.string.send_playing_song_activities));
                sendActivities(ACTION_CLEAR, getString(R.string.send_main_activity));
                break;

            case ACTION_PREVIOUS:
                previousOrNextSong(-1);
                break;

            case ACTION_NEXT:
                previousOrNextSong(1);
                break;

            case ACTION_START:
                startSong();
                break;
        }
    }

    private void startSong() {
        if (currentSong != null && currentSinger != null) {
            sendActivities(ACTION_START, getString(R.string.send_main_activity));
        } else {
            stopSelf();
        }
    }

    private void previousOrNextSong(int step) {
        if (currentSongArrayList != null) {
            int nextPosition = getIndexSong() + step;
            if (nextPosition >= currentSongArrayList.size()) {
                nextPosition = 0;
            }
            if (nextPosition < 0) {
                nextPosition = currentSongArrayList.size() - 1;
            }

            playSong(currentSongArrayList.get(nextPosition));
            getNextSinger();
        }
    }

    private int getIndexSong() {
        for (Song s : currentSongArrayList) {
            if (s.getIdSong() == currentSong.getIdSong()) {
                return currentSongArrayList.indexOf(s);
            }
        }
        return 0;
    }

    private void getNextSinger() {
        ApiService.apiService.getSingerById(currentSong.getIdSinger()).enqueue(new Callback<Singer>() {
            @Override
            public void onResponse(Call<Singer> call, Response<Singer> response) {
                currentSinger = response.body();

                sendActivities(ACTION_PREVIOUS, getString(R.string.send_playing_song_activities));
                sendActivities(ACTION_PREVIOUS, getString(R.string.send_main_activity));
                sendNotification(currentSong, currentSinger);
            }

            @Override
            public void onFailure(Call<Singer> call, Throwable t) {

            }
        });
    }

    private void resumeSong() {
        if (isPlaying == -1) {
            mediaPlayer.start();
            isPlaying = 1;

            sendActivities(ACTION_RESUME, getString(R.string.send_playing_song_activities));
            sendActivities(ACTION_RESUME, getString(R.string.send_main_activity));
            sendNotification(currentSong, currentSinger);
        }
    }

    private void pauseSong() {
        if (isPlaying == 1) {
            mediaPlayer.pause();
            isPlaying = -1;

            sendActivities(ACTION_PAUSE, getString(R.string.send_playing_song_activities));
            sendActivities(ACTION_PAUSE, getString(R.string.send_main_activity));
            sendNotification(currentSong, currentSinger);
        }
    }

    private void playSong(Song song) {
        if (currentSong != null) {
            if (currentSong.getIdSong() == song.getIdSong()) {
                return;
            } else {
                mediaPlayer.release();
            }
        }

        setMediaPlayer(song);
        currentSong = song;
        isPlaying = 1;

        mediaPlayer.start();
    }

    private void setMediaPlayer(Song song) {
        mediaPlayer = new MediaPlayer();
        String url = song.getLinkSong();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        public DownloadImageTask() {
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
            notification.setLargeIcon(result);

            startForeground(1, notification.build());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
