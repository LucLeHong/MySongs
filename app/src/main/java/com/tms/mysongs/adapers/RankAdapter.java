package com.tms.mysongs.adapers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.tms.mysongs.R;
import com.tms.mysongs.listeners.IPlaySongListener;
import com.tms.mysongs.models.Song;

import java.io.InputStream;
import java.util.ArrayList;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.ViewHolder> {
    private ArrayList<Song> topsArrayList;
    private IPlaySongListener iPlaySongListener;

    public RankAdapter(ArrayList<Song> topsArrayList, IPlaySongListener iPlaySongListener) {
        this.topsArrayList = topsArrayList;
        this.iPlaySongListener = iPlaySongListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.layout_custom_item_rank,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(topsArrayList.get(position), position + 1);
    }

    @Override
    public int getItemCount() {
        return topsArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgSong;
        private TextView txtTop;

        private ConstraintLayout layoutItemRank;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.image_song);
            txtTop = itemView.findViewById(R.id.text_top);
            layoutItemRank = itemView.findViewById(R.id.layout_item_rank);
        }

        public void setData(Song song, int top) {
            new DownloadImageTask(imgSong).execute(song.getImgSong());
            txtTop.setText("Top " + top);

            layoutItemRank.setOnClickListener(view -> iPlaySongListener.playSong(null, song));
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
