package com.shrisha.movierec;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MovieSearchAdapter extends ArrayAdapter<Movie> {
    private Context context;
    private List<Movie> movies;

    public MovieSearchAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
        this.context = context;
        this.movies = movies;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.search_item, parent, false);
        }

        Movie movie = getItem(position);
        TextView titleView = convertView.findViewById(R.id.titleView);
        ImageView posterView = convertView.findViewById(R.id.moviePoster);
        RatingBar ratingBar = convertView.findViewById(R.id.movieRating);

        titleView.setText(movie.title);
        ratingBar.setRating((float) movie.voteAverage / 2);

        // Load poster using Glide with null check
        String posterPath = movie.getFullPosterPath();
        if (posterPath != null && !posterPath.isEmpty()) {
            Glide.with(context)
                    .load(posterPath)
                    .placeholder(R.drawable.default_movie_poster)
                    .into(posterView);
        } else {
            posterView.setImageResource(R.drawable.default_movie_poster);  // Set placeholder if no poster
            Log.d("MovieSearchAdapter", "No poster available for movie: " + movie.title);
        }

        return convertView;
    }


    public void updateList(List<Movie> newMovies) {
        if (newMovies == null || newMovies.isEmpty()) {
            Log.d("MovieSearchAdapter", "No movies found to update");
            Toast.makeText(context, "No movies found", Toast.LENGTH_SHORT).show();
            return;
        }

        clear();
        addAll(newMovies);
        notifyDataSetChanged();
    }

}