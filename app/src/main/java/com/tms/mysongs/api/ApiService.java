package com.tms.mysongs.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tms.mysongs.models.Advertisement;
import com.tms.mysongs.models.Playlist;
import com.tms.mysongs.models.Singer;
import com.tms.mysongs.models.Song;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    ApiService apiService = new Retrofit.Builder()
            .baseUrl("https://mysongtms.000webhostapp.com/Service/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);

    @GET("getAdvertisements.php")
    Call<ArrayList<Advertisement>> getAdvertisement();

    @GET("getRank.php")
    Call<ArrayList<Song>> getRank();

    @GET("getPlaylists.php")
    Call<ArrayList<Playlist>> getPlaylists();

    @GET("getSingers.php")
    Call<ArrayList<Singer>> getSingers();

    @GET("getSongArrayList.php")
    Call<ArrayList<Song>> getSongs();

    @FormUrlEncoded
    @POST("getSingerById.php")
    Call<Singer> getSingerById(@Field("idSinger") int idSinger);

    @FormUrlEncoded
    @POST("getSongById.php")
    Call<Song> getSongById(@Field("idSong") int idSong);

    @FormUrlEncoded
    @POST("getSongsByIdPlaylist.php")
    Call<ArrayList<Song>> getSongsByIdPlaylist(@Field("idPlayList") int idPlayList);

    @FormUrlEncoded
    @POST("getSongsByIdSinger.php")
    Call<ArrayList<Song>> getSongsByIdSinger(@Field("idSinger") int idSinger);

    @FormUrlEncoded
    @POST("getSongsByName.php")
    Call<ArrayList<Song>> getSongsByName(@Field("nameSong") String nameSong);

    @FormUrlEncoded
    @POST("getSingersByName.php")
    Call<ArrayList<Singer>> getSingersByName(@Field("nameSinger") String nameSinger);

    @FormUrlEncoded
    @POST("getPlaylistsByName.php")
    Call<ArrayList<Playlist>> getPlaylistsByName(@Field("namePlaylist") String namePlaylist);
}
