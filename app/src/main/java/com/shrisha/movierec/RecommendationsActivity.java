package com.shrisha.movierec;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class RecommendationsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView recommendationsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        dbHelper = new DatabaseHelper(this);
        recommendationsListView = findViewById(R.id.recommendationsListView);

        updateRecommendations();
    }
    private List<Movie> convertCursorToList(Cursor cursor) {
        List<Movie> movies = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Movie movie = new Movie();
                // Assuming you have these columns in your cursor
                movie.setId((int) cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                // Set other fields as needed
                movies.add(movie);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return movies;
    }

    private void updateRecommendations() {

        List<Movie> recommendations = convertCursorToList(dbHelper.getRecommendations());
        ArrayAdapter<Movie> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, recommendations);
        recommendationsListView.setAdapter(adapter);
    }
}