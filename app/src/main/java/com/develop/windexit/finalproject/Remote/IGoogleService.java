package com.develop.windexit.finalproject.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by WINDEX IT on 01-Apr-18.
 */

public interface IGoogleService {
    @GET
    Call<String> getAdressName(@Url String url);

    @GET
    Call<String> getLocationFromAddress(@Url String url);

    /*@GET("maps/api/geocode/json")
    Call<String>getGeoCode(@Query("address") String address);
    */

    @GET("maps/api/directions/json")
    Call<String>getDirections(@Query("origin") String origin, @Query("destination") String destination);
}
