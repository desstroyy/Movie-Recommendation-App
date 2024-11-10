package com.shrisha.movierec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendationSystem {
    private final DatabaseHelper dbHelper;
    private final TMDbApi api;
    private static final int MIN_RATING_THRESHOLD = (int) 3.5f; // Minimum rating to consider for recommendations
    private static final int MAX_RECOMMENDATIONS = 10;

    public RecommendationSystem(DatabaseHelper dbHelper, TMDbApi api) {
        this.dbHelper = dbHelper;
        this.api = api;
    }

    public interface RecommendationCallback {
        void onRecommendationsReady(List<Movie> recommendations);
        void onError(String message);
    }

    public void getRecommendations(String apiKey, RecommendationCallback callback) {
        // Get user's watched movies and their ratings
        List<Movie> watchedMovies = dbHelper.getWatchedMovies();

        // Filter movies with high ratings
        List<Movie> highlyRatedMovies = new ArrayList<>();
        for (Movie movie : watchedMovies) {
            if (movie.getUserRating() >= MIN_RATING_THRESHOLD) {
                highlyRatedMovies.add(movie);
            }
        }

        if (highlyRatedMovies.isEmpty()) {
            callback.onError("Not enough rated movies to make recommendations");
            return;
        }

        // Get similar movies for each highly rated movie
        final Map<Movie, Integer> recommendationScores = new HashMap<>();
        final int[] completedRequests = {0};
        final int totalRequests = highlyRatedMovies.size();

        for (Movie movie : highlyRatedMovies) {
            api.getSimilarMovies(movie.getId(), apiKey).enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Movie> similarMovies = response.body().getMovies();
                        processSimilarMovies(similarMovies, movie.getUserRating(), recommendationScores, watchedMovies);
                    }
                    checkAndDeliverResults(++completedRequests[0], totalRequests, recommendationScores, callback);
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    checkAndDeliverResults(++completedRequests[0], totalRequests, recommendationScores, callback);
                }
            });
        }
    }

    private void processSimilarMovies(List<Movie> similarMovies, float originalRating,
                                      Map<Movie, Integer> recommendationScores,
                                      List<Movie> watchedMovies) {
        for (Movie similar : similarMovies) {
            // Skip if movie is already watched
            if (isMovieWatched(similar, watchedMovies)) {
                continue;
            }

            // Calculate score based on similarity and original movie's rating
            int score = (int) (originalRating * 20); // Convert rating to a 0-100 scale
            recommendationScores.merge(similar, score, Integer::sum);
        }
    }

    private boolean isMovieWatched(Movie movie, List<Movie> watchedMovies) {
        for (Movie watched : watchedMovies) {
            if (watched.getId() == movie.getId()) {
                return true;
            }
        }
        return false;
    }

    private void checkAndDeliverResults(int completed, int total,
                                        Map<Movie, Integer> recommendationScores,
                                        RecommendationCallback callback) {
        if (completed == total) {
            List<Movie> recommendations = new ArrayList<>(recommendationScores.keySet());

            // Sort by score
            Collections.sort(recommendations, new Comparator<Movie>() {
                @Override
                public int compare(Movie m1, Movie m2) {
                    return recommendationScores.get(m2).compareTo(recommendationScores.get(m1));
                }
            });

            // Limit to top recommendations
            if (recommendations.size() > MAX_RECOMMENDATIONS) {
                recommendations = recommendations.subList(0, MAX_RECOMMENDATIONS);
            }

            callback.onRecommendationsReady(recommendations);
        }
    }
}