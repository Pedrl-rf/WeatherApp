package com.myapp.weatherapp.APIresponse

import com.google.android.gms.common.api.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApi {
    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("q") location: String,
        @Query("key") apiKey: String
    ): WeatherResponse
}


