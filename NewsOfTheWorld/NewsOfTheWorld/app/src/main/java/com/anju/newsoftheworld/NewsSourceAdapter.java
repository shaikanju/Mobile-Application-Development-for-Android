package com.anju.newsoftheworld;



import android.content.Intent;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anju.newsoftheworld.databinding.NewsArticleBinding;
import com.squareup.picasso.Picasso;

//import com.bumptech.glide.Glide;

import java.util.List;

public class NewsSourceAdapter extends RecyclerView.Adapter<ArticleViewHolder> {

    private final List<NewsArticle> articles;
    private final MainActivity mainActivity;

    public NewsSourceAdapter(MainActivity mainActivity,List<NewsArticle> articles) {
        this.mainActivity = mainActivity;
        this.articles = articles;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate using ViewBinding
        NewsArticleBinding binding = NewsArticleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ArticleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        NewsArticle article = articles.get(position);
        String imageUrl = article.getImageUrl();
        // Bind data to views

        holder.binding.headline.setText(article.getTitle());
        holder.binding.date.setText(article.getPublishedAt());
        holder.binding.scrollableTextView.setText(article.getDescription());
        holder.binding.author.setText((article.getAuthor()));
        Picasso.get()
                .load(imageUrl)

                .error(R.drawable.noimage)
                .into(holder.binding.flagImage);
        holder.binding.pageNum.setText((position + 1) + " of " + articles.size());
        String articleUrl = article.getUrl();

        // Add click listeners to navigate to the URL
        View.OnClickListener navigateToUrl = v -> {
            if (articleUrl != null && !articleUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
                holder.itemView.getContext().startActivity(intent);
            } else {
                Toast.makeText(holder.itemView.getContext(), "URL not available", Toast.LENGTH_SHORT).show();
            }
        };

        // Assign the same click listener to all clickable views
        holder.binding.headline.setOnClickListener(navigateToUrl);
        holder.binding.flagImage.setOnClickListener(navigateToUrl);
        holder.binding.scrollableTextView.setOnClickListener(navigateToUrl);

        // Load the image using Glide
//        Glide.with(holder.binding.getRoot().getContext())
//                .load(article.getFlagImageUrl())
//                .into(holder.binding.flagImage);

        // Enable scrolling for the TextView
        holder.binding.scrollableTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }
}
