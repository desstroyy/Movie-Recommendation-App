package com.shrisha.movierec;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;

import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_MOVIES = "movies";
    private static final String TABLE_WATCHED_MOVIES = "watched_movies";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create movies table with all necessary columns
        String CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_MOVIES + " (" +
                "id INTEGER PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "overview TEXT, " +
                "poster_path TEXT, " +
                "genre_ids TEXT, " +  // Store as JSON string
                "vote_average REAL)";

        // Create watched movies table
        String CREATE_WATCHED_MOVIES_TABLE = "CREATE TABLE " + TABLE_WATCHED_MOVIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_id INTEGER, " +
                "rating FLOAT, " +
                "FOREIGN KEY(movie_id) REFERENCES " + TABLE_MOVIES + "(id))";

        db.execSQL(CREATE_MOVIES_TABLE);
        db.execSQL(CREATE_WATCHED_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATCHED_MOVIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        onCreate(db);
    }



    public Cursor searchMovies(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String searchQuery = "SELECT * FROM " + TABLE_MOVIES + " WHERE title LIKE ?";
            cursor = db.rawQuery(searchQuery, new String[]{"%" + query + "%"});
            if (cursor != null && cursor.getCount() > 0) {
                return cursor;
            } else {
                Log.d("DatabaseHelper", "No movies found matching query: " + query);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error searching movies", e);
        }

        return cursor;
    }


    public long addWatchedMovie(Movie movie, float rating) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First, insert or update the movie details
        ContentValues movieValues = new ContentValues();
        movieValues.put("id", movie.id);  // Using public field directly
        movieValues.put("title", movie.title);  // Using public field directly
        movieValues.put("overview", movie.overview);
        movieValues.put("poster_path", movie.posterPath);
        movieValues.put("genre_ids", new Gson().toJson(movie.genreIds));
        movieValues.put("vote_average", movie.voteAverage);

        db.insertWithOnConflict(TABLE_MOVIES, null, movieValues, SQLiteDatabase.CONFLICT_REPLACE);

        // Then insert the watched movie entry
        ContentValues watchedValues = new ContentValues();
        watchedValues.put("movie_id", movie.id);
        watchedValues.put("rating", rating);

        return db.insert(TABLE_WATCHED_MOVIES, null, watchedValues);
    }



    public int removeWatchedMovie(int movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_WATCHED_MOVIES, "movie_id = ?",
                new String[]{String.valueOf(movieId)});
    }
    public Cursor getRecommendations() {
        SQLiteDatabase db = this.getReadableDatabase();

        // This query gets movies that aren't watched yet,
        // ordered by similarity to user's highly rated movies
        String query = "SELECT DISTINCT m.id, m.title, m.genre " +
                "FROM " + TABLE_MOVIES + " m " +
                "WHERE m.id NOT IN " +
                "(SELECT movie_id FROM " + TABLE_WATCHED_MOVIES + ") " +
                "AND m.genre IN " +
                "(SELECT m2.genre FROM " + TABLE_MOVIES + " m2 " +
                "JOIN " + TABLE_WATCHED_MOVIES + " w ON m2.id = w.movie_id " +
                "WHERE w.rating >= 4) " +
                "LIMIT 10";

        return db.rawQuery(query, null);
    }

    public List<Movie> getWatchedMovies() {
        List<Movie> movieList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Modified query to join watched_movies with movies table to get all info
        String query = "SELECT w.movie_id, w.rating, " +
                "m.title, m.overview, m.poster_path, m.genre_ids " +
                "FROM " + TABLE_WATCHED_MOVIES + " w " +
                "JOIN " + TABLE_MOVIES + " m ON w.movie_id = m.id";

        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                // Get column indices once before the loop
                int movieIdIndex = cursor.getColumnIndexOrThrow("movie_id");
                int ratingIndex = cursor.getColumnIndexOrThrow("rating");
                int titleIndex = cursor.getColumnIndexOrThrow("title");
                int overviewIndex = cursor.getColumnIndexOrThrow("overview");
                int posterPathIndex = cursor.getColumnIndexOrThrow("poster_path");
                int genreIdsIndex = cursor.getColumnIndexOrThrow("genre_ids");

                do {
                    Movie movie = new Movie();
                    movie.id = cursor.getInt(movieIdIndex);
                    movie.rating = cursor.getFloat(ratingIndex);
                    movie.title = cursor.getString(titleIndex);
                    movie.overview = cursor.getString(overviewIndex);
                    movie.posterPath = cursor.getString(posterPathIndex);

                    String genreIdsJson = cursor.getString(genreIdsIndex);
                    if (genreIdsJson != null) {
                        movie.genreIds = new Gson().fromJson(genreIdsJson,
                                new TypeToken<List<Integer>>(){}.getType());
                    }

                    movieList.add(movie);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return movieList;
    }


    public List<Movie> getWatchedMoviesWithRatings() {
        return getWatchedMovies();  // Since all watched movies have ratings
    }
}

