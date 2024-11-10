package com.shrisha.movierec;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecommendationsActivity extends AppCompatActivity {
    private RecyclerView recommendationsRecyclerView;
    private MovieRecommendationsAdapter recommendationsAdapter;
    private ProgressBar progressBar;
    private TextView noRecommendationsText;
    private RecommendationSystem recommendationSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        // Initialize views
        recommendationsRecyclerView = findViewById(R.id.recommendationsRecyclerView);
        progressBar = findViewById(R.id.recommendationsProgress);
        noRecommendationsText = findViewById(R.id.noRecommendationsText);

        // Setup RecyclerView
        recommendationsAdapter = new MovieRecommendationsAdapter(new ArrayList<>());
        recommendationsRecyclerView.setAdapter(recommendationsAdapter);
        recommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize recommendation system
        recommendationSystem = new RecommendationSystem(
                new DatabaseHelper(this),
                ApiClient.getApi()
        );

        // Load recommendations
        loadRecommendations();
    }

    private void loadRecommendations() {
        progressBar.setVisibility(View.VISIBLE);
        noRecommendationsText.setVisibility(View.GONE);

        String apiKey = getString(R.string.tmdb_api_key);
        recommendationSystem.getRecommendations(apiKey, new RecommendationSystem.RecommendationCallback() {
            @Override
            public void onRecommendationsReady(List<Movie> recommendations) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (recommendations.isEmpty()) {
                        noRecommendationsText.setVisibility(View.VISIBLE);
                        noRecommendationsText.setText("No recommendations available.\nTry rating more movies!");
                    } else {
                        recommendationsAdapter.updateMovies(recommendations);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RecommendationsActivity.this, message, Toast.LENGTH_LONG).show();
                    noRecommendationsText.setVisibility(View.VISIBLE);
                    noRecommendationsText.setText("Unable to load recommendations.\nPlease try again later.");
                });
            }
        });
    }
}