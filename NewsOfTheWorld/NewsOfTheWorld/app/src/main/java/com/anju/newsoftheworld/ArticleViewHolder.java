package com.anju.newsoftheworld;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anju.newsoftheworld.databinding.NewsArticleBinding;

public class ArticleViewHolder extends RecyclerView.ViewHolder {
    final NewsArticleBinding binding;

    public ArticleViewHolder(@NonNull NewsArticleBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
