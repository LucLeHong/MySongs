package com.tms.mysongs.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tms.mysongs.R;
import com.tms.mysongs.adapers.AdvertisementsAdapter;
import com.tms.mysongs.adapers.PlaylistsAdapter;
import com.tms.mysongs.adapers.RankAdapter;
import com.tms.mysongs.adapers.SingersAdapter;
import com.tms.mysongs.api.ApiService;
import com.tms.mysongs.listeners.IPlaySongListener;
import com.tms.mysongs.listeners.IPlaylistsListener;
import com.tms.mysongs.listeners.ISingersListener;
import com.tms.mysongs.models.Advertisement;
import com.tms.mysongs.models.Playlist;
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

public class MainActivity extends AppCompatActivity implements IPlaySongListener, IPlaylistsListener, ISingersListener {
    private final int TIME_DELAY = 500;
    private final int TIME_PERIOD = 5000;

    private ArrayList<Advertisement> advertisementArrayList;
    private ArrayList<Song> rankArrayList;
    private ArrayList<Playlist> playlistArrayList;
    private ArrayList<Singer> singerArrayList;

    private AdvertisementsAdapter adapterView;
    private RankAdapter rankAdapter;
    private PlaylistsAdapter playlistsAdapter;
    private SingersAdapter singersAdapter;

    private ViewPager vpAdvertisement;
    private RecyclerView rvRank, rvPlaylists, rvSingers;
    private TextView txtRank, txtPlaylists, txtSingers;
    private ImageView imgSong, imgPlayOrPause , imgClear;
    private TextView txtNameSong, txtNameSinger;
    private RelativeLayout layoutControlSong;
    private EditText edtSearch;

    private Song song;
    private int isPlaying;
    private Singer singer;
    private ArrayList<Song> songArrayList;

    private int currentPage = 0;
    private boolean flag = true; // #true -> currentPage++  #false -> currentPage--

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

    private void handleService(int action) {
        Bundle bundleService = new Bundle();
        bundleService.putInt(getString(R.string.action), action);
        bundleService.putSerializable(getString(R.string.object_singer), singer);
        bundleService.putSerializable(getString(R.string.object_song_arraylist), songArrayList);

        Intent intentService = new Intent(this, MyService.class);
        intentService.putExtras(bundleService);

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

    private void setOnClick() {
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

                Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<ArrayList<Song>> call, Throwable t) {

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

                Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<ArrayList<Song>> call, Throwable t) {

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
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vpAdvertisement = findViewById(R.id.view_paper_advertisements);
        rvRank = findViewById(R.id.recycler_view_rank);
        rvPlaylists = findViewById(R.id.recycler_view_playlists);
        rvSingers = findViewById(R.id.recycler_view_singers);
        txtRank = findViewById(R.id.text_rank);
        txtPlaylists = findViewById(R.id.text_playlists);
        txtSingers = findViewById(R.id.text_singers);
        txtNameSong = findViewById(R.id.text_name_song);
        txtNameSinger = findViewById(R.id.text_name_singer);
        imgSong = findViewById(R.id.image_song);
        imgPlayOrPause = findViewById(R.id.image_play_or_pause);
        imgClear = findViewById(R.id.image_clear);
        layoutControlSong = findViewById(R.id.layout_control_song);
        edtSearch = findViewById(R.id.auto_edit_text_search);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(getString(R.string.send_main_activity)));

        checkSongIsPlaying();

        getAdvertisement();
        getRank();
        getPlaylists();
        getSingers();

        edtSearch.setOnClickListener(view -> {
            ArrayList<String> strings = new ArrayList<>();
            for (Playlist p : playlistArrayList) {
                strings.add(p.getNamePlaylist());
            }

            for (Singer s : singerArrayList) {
                strings.add(s.getNameSinger());
            }

            ApiService.apiService.getSongs().enqueue(new Callback<ArrayList<Song>>() {
                @Override
                public void onResponse(Call<ArrayList<Song>> call, Response<ArrayList<Song>> response) {
                    ArrayList<Song> songArrayList = response.body();
                    for (Song s : songArrayList) {
                        strings.add(s.getNameSong());
                    }

                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(getString(R.string.object_auto_text), strings);

                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

                @Override
                public void onFailure(Call<ArrayList<Song>> call, Throwable t) {

                }
            });
        });
    }

    private void checkSongIsPlaying() {
        Intent intentService = new Intent(this, MyService.class);
        intentService.putExtra(getString(R.string.action), MyService.ACTION_START);
        startService(intentService);
    }

    private void getSingers() {
        ApiService.apiService.getSingers().enqueue(new Callback<ArrayList<Singer>>() {
            @Override
            public void onResponse(Call<ArrayList<Singer>> call, Response<ArrayList<Singer>> response) {
                 singerArrayList = response.body();

                if (singerArrayList != null && singerArrayList.size() >= 0) {
                    singersAdapter = new SingersAdapter(singerArrayList, MainActivity.this);
                    rvSingers.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
                    rvSingers.setAdapter(singersAdapter);

                    txtSingers.setVisibility(View.VISIBLE);
                    rvSingers.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Singer>> call, Throwable t) {
                txtSingers.setVisibility(View.GONE);
                rvSingers.setVisibility(View.GONE);
            }
        });
    }

    private void getPlaylists() {
        ApiService.apiService.getPlaylists().enqueue(new Callback<ArrayList<Playlist>>() {
            @Override
            public void onResponse(Call<ArrayList<Playlist>> call, Response<ArrayList<Playlist>> response) {
                playlistArrayList = response.body();

                if (playlistArrayList != null && playlistArrayList.size() >= 0) {
                    playlistsAdapter = new PlaylistsAdapter(playlistArrayList, MainActivity.this);
                    rvPlaylists.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
                    rvPlaylists.setAdapter(playlistsAdapter);

                    txtPlaylists.setVisibility(View.VISIBLE);
                    rvPlaylists.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Playlist>> call, Throwable t) {
                txtPlaylists.setVisibility(View.GONE);
                rvPlaylists.setVisibility(View.GONE);
            }
        });
    }

    private void getRank() {
        ApiService.apiService.getRank().enqueue(new Callback<ArrayList<Song>>() {
            @Override
            public void onResponse(Call<ArrayList<Song>> call, Response<ArrayList<Song>> response) {
                rankArrayList = response.body();

                if (rankArrayList != null && rankArrayList.size() > 0) {
                    rankAdapter = new RankAdapter(rankArrayList, MainActivity.this);
                    rvRank.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
                    rvRank.setAdapter(rankAdapter);

                    txtRank.setVisibility(View.VISIBLE);
                    rvRank.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Song>> call, Throwable t) {
                txtRank.setVisibility(View.GONE);
                rvRank.setVisibility(View.GONE);
            }
        });
    }

    private void getAdvertisement() {
        ApiService.apiService.getAdvertisement().enqueue(new Callback<ArrayList<Advertisement>>() {
            @Override
            public void onResponse(Call<ArrayList<Advertisement>> call, Response<ArrayList<Advertisement>> response) {
                advertisementArrayList = response.body();

                if (advertisementArrayList != null && advertisementArrayList.size() > 0) {
                    adapterView = new AdvertisementsAdapter(advertisementArrayList, MainActivity.this);
                    vpAdvertisement.setAdapter(adapterView);
                    setAutoScrollViewPaper(advertisementArrayList.size() - 1);
                    vpAdvertisement.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Advertisement>> call, Throwable t) {
                vpAdvertisement.setVisibility(View.GONE);
            }
        });
    }

    private void setAutoScrollViewPaper(int numberPaper) {
        // Set change paper
        vpAdvertisement.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // Set time auto change paper
        final Handler handler = new Handler();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    vpAdvertisement.setCurrentItem(currentPage, true);

                    if (currentPage == numberPaper) {
                        flag = false;
                    }
                    if (currentPage == 0){
                        flag = true;
                    }

                    if (flag) {
                        currentPage++;
                    } else {
                        currentPage--;
                    }
                });
            }
        }, TIME_DELAY, TIME_PERIOD);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}