package com.thgmobi.catwalk.service;

import com.thgmobi.catwalk.models.weather.WeatherData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {

    @GET("weather?appid=c7ad2c2a3fde92342fd0d6926da6a9f8")
    Call<WeatherData> getCurrentWeather(@Query("lat") double lat, @Query("lon") double lon);
}
