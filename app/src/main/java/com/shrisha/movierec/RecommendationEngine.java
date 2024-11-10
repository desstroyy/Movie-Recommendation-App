package com.shrisha.movierec;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendationEngine {
    private DatabaseHelper dbHelper;
    private TMDbApi api;

    public RecommendationEngine(Context context) {
        dbHelper = new DatabaseHelper(context);
        api = ApiClient.getApi();
    }

    public void getRecommendations(RecommendationCallback callback) {
        // Get user's watched movies and ratings
        List<Movie> watchedMovies = dbHelper.getWatchedMoviesWithRatings();

        // Get recommendations based on highest rated movies
        List<Movie> recommendations = new ArrayList<>();
        AtomicInteger completed = new AtomicInteger(0);

        // Get top 3 rated movies
        watchedMovies.sort((m1, m2) -> Float.compare(m2.getUserRating(), m1.getUserRating()));
        List<Movie> topRated = watchedMovies.subList(0, Math.min(3, watchedMovies.size()));

        for (Movie movie : topRated) {
            api.getRecommendations(movie.getId(), ApiConfig.API_KEY)
                    .enqueue(new Callback<MovieResponse>() {
                        @Override
                        public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                recommendations.addAll(response.body().getMovies());
                            }

                            if (completed.incrementAndGet() == topRated.size()) {
                                // Remove duplicates and already watched movies
                                List<Movie> filteredRecommendations = filterRecommendations(recommendations, watchedMovies);
                                callback.onRecommendationsReady(filteredRecommendations);
                            }
                        }

                        @Override
                        public void onFailure(Call<MovieResponse> call, Throwable t) {
                            callback.onError(t);
                        }
                    });
        }
    }
    private List<Movie> filterRecommendations(List<Movie> recommendations, List<Movie> watchedMovies) {
        // Remove duplicates and already watched movies
        Set<Integer> watchedIds = watchedMovies.stream()
                .map(Movie::getId)
                .collect(Collectors.toSet());

        return recommendations.stream()
                .filter(m -> !watchedIds.contains(m.getId()))
                .distinct()
                .limit(20)
                .collect(Collectors.toList());
    }

    public interface RecommendationCallback {
        void onRecommendationsReady(List<Movie> recommendations);
        void onError(Throwable error);
    }
}