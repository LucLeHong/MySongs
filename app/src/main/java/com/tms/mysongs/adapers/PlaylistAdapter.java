package com.tms.mysongs.adapers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tms.mysongs.R;
import com.tms.mysongs.api.ApiService;
import com.tms.mysongs.listeners.IPlaySongListener;
import com.tms.mysongs.models.Singer;
import com.tms.mysongs.models.Song;

import java.io.InputStream;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private ArrayList<Song> songArrayList;
    private IPlaySongListener iPlaySongListener;

    public PlaylistAdapter(ArrayList<Song> songArrayList, IPlaySongListener iPlaySongListener) {
        this.songArrayList = songArrayList;
        this.iPlaySongListener = iPlaySongListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaylistAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.layout_custom_item_playlist,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(songArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgSong;
        private TextView txtNameSong, txtNameSinger;
        private RelativeLayout layoutItemPlaylist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.image_song);
            txtNameSong = itemView.findViewById(R.id.text_name_song);
            txtNameSinger = itemView.findViewById(R.id.text_name_singer);
            layoutItemPlaylist = itemView.findViewById(R.id.layout_item_playlist);
        }

        public void setData(Song song) {
            txtNameSong.setText(song.getNameSong());
            new DownloadImageTask(imgSong).execute(song.getImgSong());
            getSingerById(song.getIdSinger());

            layoutItemPlaylist.setOnClickListener(view -> iPlaySongListener.playSong(songArrayList, song));
        }

        private void getSingerById(int idSinger) {
            ApiService.apiService.getSingerById(idSinger).enqueue(new Callback<Singer>() {
                @Override
                public void onResponse(Call<Singer> call, Response<Singer> response) {
                    Singer singer = response.body();
                    txtNameSinger.setText(singer.getNameSinger());
                }

                @Override
                public void onFailure(Call<Singer> call, Throwable t) {

                }
            });
        }
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

}
