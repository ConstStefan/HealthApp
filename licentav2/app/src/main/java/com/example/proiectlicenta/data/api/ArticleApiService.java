package com.example.proiectlicenta.data.api;

import com.example.proiectlicenta.data.model.ArticleResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ArticleApiService {
    @GET("everything")
    Call<ArticleResponse> getArticles(
            @Query("q") String query,
            @Query("apiKey") String apiKey
    );
}
