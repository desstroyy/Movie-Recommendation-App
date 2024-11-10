package com.shrisha.movierec;

import com.google.gson.annotations.SerializedName;

import java.util.List;



public class MovieResponse {
    @SerializedName("results")
    private List<Movie> movies;

    @SerializedName("page")
    private int page;

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}