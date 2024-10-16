package com.example.proiectlicenta.data.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ArticleApiServiceFactory {
    private static final String BASE_URL = "https://newsapi.org/v2/"; // newsAPI

    public static ArticleApiService create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ArticleApiService.class);
    }
}
