package com.tms.mysongs.adapers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tms.mysongs.R;
import com.tms.mysongs.listeners.IPlaylistsListener;
import com.tms.mysongs.models.Playlist;

import java.io.InputStream;
import java.util.ArrayList;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {

    private ArrayList<Playlist> playlistArrayList;
    private IPlaylistsListener iPlaylistsListener;

    public PlaylistsAdapter(ArrayList<Playlist> playlistArrayList, IPlaylistsListener iPlaylistsListener) {
        this.playlistArrayList = playlistArrayList;
        this.iPlaylistsListener = iPlaylistsListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaylistsAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.layout_custom_item_playlists,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(playlistArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return playlistArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgPlaylist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlaylist = itemView.findViewById(R.id.image_playlist);
        }

        public void setData(Playlist playlist) {
            new DownloadImageTask(imgPlaylist).execute(playlist.getImgPlaylist());

            imgPlaylist.setOnClickListener(view -> iPlaylistsListener.toPlaylistActivity(playlist));
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
