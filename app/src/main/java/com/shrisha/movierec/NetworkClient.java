package com.shrisha.movierec;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static TMDbApi getTMDbApi() {
        return getRetrofitInstance().create(TMDbApi.class);
    }
}

