package com.example.proiectlicenta.data.model;

import com.google.gson.annotations.SerializedName;

public class Article {
    private String title;

    @SerializedName("urlToImage")
    private String imageUrl;

    private String url;

    private String description;

    private String category;



    public Article(String title, String imageUrl, String url, String description, String category) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.url = url;
        this.description = description;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
