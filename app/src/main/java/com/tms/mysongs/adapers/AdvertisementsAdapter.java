package com.tms.mysongs.adapers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.tms.mysongs.R;
import com.tms.mysongs.api.ApiService;
import com.tms.mysongs.listeners.IPlaySongListener;
import com.tms.mysongs.models.Advertisement;
import com.tms.mysongs.models.Song;

import java.io.InputStream;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdvertisementsAdapter extends PagerAdapter {
    private ArrayList<Advertisement> advertisementArrayList;
    private IPlaySongListener iPlaySongListener;

    public AdvertisementsAdapter(ArrayList<Advertisement> advertisementArrayList, IPlaySongListener iPlaySongListener) {
        this.advertisementArrayList = advertisementArrayList;
        this.iPlaySongListener = iPlaySongListener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.layout_custom_item_advertisement, container, false);

        ImageView imgAdvertisement = view.findViewById(R.id.image_advertisement);

        new DownloadImageTask(imgAdvertisement).execute(advertisementArrayList.get(position).getImgAdvertisement());

        imgAdvertisement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSongById(advertisementArrayList.get(position).getIdSong());
            }
        });

        container.addView(view);
        return view;
    }

    private void getSongById(int idSong) {
        ApiService.apiService.getSongById(idSong).enqueue(new Callback<Song>() {
            @Override
            public void onResponse(Call<Song> call, Response<Song> response) {
                Song song = response.body();
                iPlaySongListener.playSong(null, song);
            }

            @Override
            public void onFailure(Call<Song> call, Throwable t) {
            }
        });
    }

    @Override
    public int getCount() {
        if (advertisementArrayList != null) {
            return advertisementArrayList.size();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
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
