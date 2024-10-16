package com.example.proiectlicenta.ui.calorie;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proiectlicenta.R;
import com.example.proiectlicenta.data.model.Article;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private List<Article> articles = new ArrayList<>();
    private Context context;

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article currentArticle = articles.get(position);
        holder.articleTitle.setText(currentArticle.getTitle());

        // adaugarea log-ului pentru URL-ul imaginii
        Log.d("ArticleAdapter", "image URL: " + currentArticle.getImageUrl());

        // verificam URL-ul imaginii inainte de a-l incarca
        if (currentArticle.getImageUrl() != null && !currentArticle.getImageUrl().isEmpty()) {
            Picasso.get().load(currentArticle.getImageUrl()).into(holder.articleImage);
        } else {
            holder.articleImage.setImageResource(R.drawable.ic_profile_placeholder); // placeholder in cazul in care URL-ul imaginii este nul sau gol
        }

        holder.articleImage.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentArticle.getUrl()));
            context.startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
        notifyDataSetChanged();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        private ImageView articleImage;
        private TextView articleTitle;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            articleImage = itemView.findViewById(R.id.article_image);
            articleTitle = itemView.findViewById(R.id.article_title);
        }
    }
}
