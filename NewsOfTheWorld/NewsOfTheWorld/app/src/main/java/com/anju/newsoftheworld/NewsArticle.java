package com.anju.newsoftheworld;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class NewsArticle {
    private String author;
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private String publishedAt;
    private String sourceName;

    // Constructor
    public NewsArticle(String author, String title, String description, String url, String imageUrl, String publishedAt, String sourceName) {
        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;

        this.sourceName = sourceName;
    }

    // Getters and Setters for the fields
    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPublishedAt() {
        if (publishedAt == null || publishedAt.isEmpty()) {
            return "Unknown Date";
        }

        try {
            // Input ISO 8601 format
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Ensure UTC timezone

            // Handle cases where milliseconds are missing
            if (!publishedAt.contains(".")) {
                isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            }

            // Parse the ISO date string
            Date date = isoFormat.parse(publishedAt);

            // Output format
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            return outputFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return publishedAt; // Return the original value if parsing fails
        }
    }



    public String getSourceName() {
        return sourceName;
    }
}
