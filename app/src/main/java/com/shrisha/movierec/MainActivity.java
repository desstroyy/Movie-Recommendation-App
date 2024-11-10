package com.shrisha.movierec;
import static android.content.ContentValues.TAG;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.ListView;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private RecyclerView watchedMoviesRecyclerView;
    private WatchedMoviesAdapter watchedMoviesAdapter;
    private DatabaseHelper dbHelper;
    private TMDbApi api;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Log.d(TAG, "Starting onCreate");
            setContentView(R.layout.activity_main);

            // Initialize API
            try {
                api = ApiClient.getApi();
                Log.d(TAG, "API initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing API", e);
                showErrorToast("Error initializing API");
                return;
            }

            // Initialize database helper
            try {
                dbHelper = new DatabaseHelper(this);
                Log.d(TAG, "Database helper initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing database", e);
                showErrorToast("Error initializing database");
                return;
            }

            // Initialize views
            try {
                initializeViews();
                Log.d(TAG, "Views initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing views", e);
                showErrorToast("Error initializing views");
                return;
            }

            // Set up RecyclerView
            try {
                setupRecyclerView();
                Log.d(TAG, "RecyclerView setup complete");
            } catch (Exception e) {
                Log.e(TAG, "Error setting up RecyclerView", e);
                showErrorToast("Error setting up movie list");
                return;
            }

            // Load watched movies
            try {
                loadWatchedMovies();
                Log.d(TAG, "Watched movies loaded successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error loading watched movies", e);
                showErrorToast("Error loading watched movies");
            }
        } catch (Exception e) {
            Log.e(TAG, "Fatal error in onCreate", e);
            showErrorToast("Error starting application");
        }
    }


    private void initializeViews() {
        watchedMoviesRecyclerView = findViewById(R.id.watchedMoviesRecyclerView);
        Button addMovieButton = findViewById(R.id.addMovieButton);
        Button recommendationsButton = findViewById(R.id.recommendationsButton);

        addMovieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchDialog();
            }
        });

        recommendationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecommendationsActivity.class);
                startActivity(intent);
            }
        });
    }


    /*
    private static void onClick(View v) {
        showSearchDialog();
    }

     */

    private void showErrorToast(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        );
    }

    private void setupRecyclerView() {
        watchedMoviesAdapter = new WatchedMoviesAdapter(
                new ArrayList<>(),
                new WatchedMoviesAdapter.OnMovieClickListener() {
                    @Override
                    public void onMovieClick(Movie movie) {
                        showSearchDialog();
                    }
                }
        );
        watchedMoviesRecyclerView.setAdapter(watchedMoviesAdapter);
        watchedMoviesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View searchView = getLayoutInflater().inflate(R.layout.search_movie_dialog, null);

        SearchView movieSearchView = searchView.findViewById(R.id.searchView);
        ListView searchResultsList = searchView.findViewById(R.id.searchResultsList);
        ProgressBar searchProgress = searchView.findViewById(R.id.searchProgress);

        MovieSearchAdapter searchAdapter = new MovieSearchAdapter(this, new ArrayList<>());
        searchResultsList.setAdapter(searchAdapter);

        AlertDialog dialog = builder.setView(searchView)
                .setTitle("Search Movies")
                .setNegativeButton("Cancel", null)
                .create();

        movieSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private Handler handler = new Handler();
            private Runnable searchRunnable;

            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query, searchProgress, searchAdapter);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchProgress.setVisibility(View.VISIBLE);

                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    if (!TextUtils.isEmpty(newText) && newText.length() >= 2) {
                        performSearch(newText, searchProgress, searchAdapter);
                    } else {
                        searchProgress.setVisibility(View.GONE);
                    }
                };

                handler.postDelayed(searchRunnable, 300);
                return true;
            }
        });

        searchResultsList.setOnItemClickListener((parent, view, position, id) -> {
            Movie selectedMovie = searchAdapter.getItem(position);
            showRatingDialog(selectedMovie);
            dialog.dismiss();
        });

        dialog.show();
    }


    private void loadWatchedMovies() {
        try {
            List<Movie> watchedMovies = dbHelper.getWatchedMovies();
            if (watchedMovies.isEmpty()) {
                // Show a more user-friendly message for empty list
                Toast.makeText(this, "No watched movies yet. Add some movies!", Toast.LENGTH_SHORT).show();
            }
            watchedMoviesAdapter.updateMovies(watchedMovies);
        } catch (Exception e) {
            Log.e(TAG, "Error loading watched movies", e);
            Toast.makeText(this, "Error loading watched movies", Toast.LENGTH_SHORT).show();
        }
    }
    private void showRatingDialog(Movie movie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View ratingView = getLayoutInflater().inflate(R.layout.rating_dialog, null);
        RatingBar ratingBar = ratingView.findViewById(R.id.movieRatingBar);

        builder.setView(ratingView)
                .setTitle("Rate " + movie.title)
                .setPositiveButton("Save", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    dbHelper.addWatchedMovie(movie, rating);  // Now passing the whole movie object
                    loadWatchedMovies();
                    Toast.makeText(MainActivity.this,
                            "Movie added to watched list", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void performSearch(String query, ProgressBar searchProgress, MovieSearchAdapter searchAdapter) {
        Log.d(TAG, "Performing search for: " + query);
        searchProgress.setVisibility(View.VISIBLE);
        String apiKey = getString(R.string.tmdb_api_key);

        api.searchMovies(apiKey, query, false)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        searchProgress.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Movie> movies = response.body().getMovies();
                            Log.d(TAG, "Search results received: " + movies.size() + " movies");

                            runOnUiThread(() -> {
                                if (movies.isEmpty()) {
                                    Toast.makeText(MainActivity.this,
                                            "No movies found for: " + query,
                                            Toast.LENGTH_SHORT).show();
                                }
                                searchAdapter.updateList(movies);
                            });
                        } else {
                            Log.e(TAG, "Error response: " + response.code() + " " + response.message());
                            String errorMsg = "Error searching movies: " + response.code();
                            Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e(TAG, "Search failed", t);
                        searchProgress.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this,
                                "Error searching movies: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}