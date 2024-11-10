package com.shrisha.movierec;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class MovieRecommendationsAdapter extends RecyclerView.Adapter<MovieRecommendationsAdapter.ViewHolder> {
    private List<Movie> movies;
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    public MovieRecommendationsAdapter(List<Movie> movies) {
        this.movies = movies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recommended_movie_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movies.get(position);

        holder.titleTextView.setText(movie.title);



        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            Glide.with(holder.posterImageView.getContext())
                    .load(IMAGE_BASE_URL + movie.getPosterPath())
                    .placeholder(R.drawable.movie_placeholder)
                    .error(R.drawable.movie_placeholder)
                    .into(holder.posterImageView);
        } else {
            holder.posterImageView.setImageResource(R.drawable.movie_placeholder);
        }


    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void updateMovies(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImageView;
        TextView titleTextView;
        TextView yearTextView;
        TextView descriptionTextView;

        ViewHolder(View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.recommendedMoviePoster);
            titleTextView = itemView.findViewById(R.id.recommendedMovieTitle);
            yearTextView = itemView.findViewById(R.id.recommendedMovieYear);
            descriptionTextView = itemView.findViewById(R.id.recommendedMovieDescription);
        }
    }
}
