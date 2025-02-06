package com.anju.newsoftheworld;

public class NewsSource {
    private String id;          // Unique ID of the source
    private String name;        // Name of the source
    private String category;    // Topic category (e.g., business, sports, etc.)
    private String language;    // Language code (e.g., en, fr, etc.)
    private String country;     // Country code (e.g., US, GB, etc.)

    // Constructor
    public NewsSource(String id, String name, String category, String language, String country) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.language = language;
        this.country = country;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    // Optional: toString method for debugging or displaying

    @Override
    public String toString() {
        return "NewsSource{id='" + id + "', name='" + name + "', category='" + category + "', language='" + language + "', country='" + country + "'}";
    }

}

