package com.shrisha.movierec;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Movie {
    @SerializedName("id")
    public int id;

    @SerializedName("title")
    public String title;

    @SerializedName("overview")
    public String overview;

    @SerializedName("poster_path")
    public String posterPath;

    @SerializedName("vote_average")
    public double voteAverage;

    @SerializedName("genre_ids")
    public List<Integer> genreIds;

    public float rating;

    // Add getters and setters

    public String getFullPosterPath() {
        if (posterPath != null) {
            return ApiConfig.IMAGE_BASE_URL + ApiConfig.POSTER_SIZE + posterPath;
        }
        return null;
    }
    public int getId(){
        return id;
    }
    public float getUserRating() {
        return rating;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}