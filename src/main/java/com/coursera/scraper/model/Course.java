package com.coursera.scraper.model;

public class Course {
    private String name;
    private String version;
    private String rating;
    private String ratingStats;

    public Course(String name, String version, String rating, String ratingStats) {
        this.name = name;
        this.version = version;
        this.rating = rating;
        this.ratingStats = ratingStats;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getRating() {
        return rating;
    }

    public String getRatingStats() {
        return ratingStats;
    }
}
