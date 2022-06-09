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
import androidx.recyclerview.widget.RecyclerView;

import com.tms.mysongs.R;
import com.tms.mysongs.listeners.ISingersListener;
import com.tms.mysongs.models.Singer;

import java.io.InputStream;
import java.util.ArrayList;

public class SingersAdapter extends RecyclerView.Adapter<SingersAdapter.ViewHolder> {
    private ArrayList<Singer> singerArrayList;
    private ISingersListener iSingersListener;

    public SingersAdapter(ArrayList<Singer> singerArrayList, ISingersListener iSingersListener) {
        this.singerArrayList = singerArrayList;
        this.iSingersListener = iSingersListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SingersAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.layout_custom_item_singer,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(singerArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return singerArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgSinger;
        private TextView txtNameSinger;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSinger = itemView.findViewById(R.id.image_singer);
            txtNameSinger = itemView.findViewById(R.id.text_name_singer);
        }

        public void setData(Singer singer) {
            txtNameSinger.setText(singer.getNameSinger());
            new DownloadImageTask(imgSinger).execute(singer.getImgSinger());

            imgSinger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iSingersListener.toPlaylistActivity(singer);
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
