package com.example.proiectlicenta.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.proiectlicenta.data.api.ArticleApiService;
import com.example.proiectlicenta.data.api.ArticleApiServiceFactory;
import com.example.proiectlicenta.data.model.Article;
import com.example.proiectlicenta.data.model.ArticleResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArticleRepository {
    private final ArticleApiService articleApiService;
    private final String apiKey = "33fb7f0c82b646b1ad0f9586c21707e3";

    public ArticleRepository() {
        articleApiService = ArticleApiServiceFactory.create();
    }

    public LiveData<List<Article>> getArticles(String query) {
        MutableLiveData<List<Article>> articlesLiveData = new MutableLiveData<>();
        articleApiService.getArticles(query, apiKey).enqueue(new Callback<ArticleResponse>() {
            @Override
            public void onResponse(Call<ArticleResponse> call, Response<ArticleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Article> articles = response.body().getArticles();

                    // setarea categoriilor manual
                    for (Article article : articles) {
                        String category = determineCategory(article);
                        article.setCategory(category);
                        Log.d("ArticleRepository", "Article: " + article.getTitle() + ", Category: " + category + ", Image URL: " + article.getImageUrl());
                    }

                    articlesLiveData.setValue(articles);
                }
            }

            @Override
            public void onFailure(Call<ArticleResponse> call, Throwable t) {
                articlesLiveData.setValue(null);
            }
        });
        return articlesLiveData;
    }

    // metoda pentru a determina categoria articolului
    private String determineCategory(Article article) {
        String title = article.getTitle().toLowerCase();

        if (title.contains("weight loss") || title.contains("slim") || title.contains("fat loss") || title.contains("lose weight") || title.contains("lose fat")) {
            return "lose";
        } else if (title.contains("gain weight") || title.contains("weight gain") || title.contains("muscle gain") || title.contains("bulk") || title.contains("mass") || title.contains("gain muscle")) {
            return "gain";
        } else {
            return "other";
        }
    }
}
