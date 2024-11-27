package com.example.slouch_patrol_app.Controller.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.slouch_patrol_app.Helpers.DatabaseHelper;
import com.example.slouch_patrol_app.R;
import com.example.slouch_patrol_app.holographlibrary.*;

public class SessionDataActivity extends AppCompatActivity {

    public interface onDeleteActivityEventListener {
        void onDeleteActivityEvent(String event);
    }

    private onDeleteActivityEventListener deleteActivityEventListener;
    private TextView textViewSessionName, textViewSessionSummary;
    private DatabaseHelper databaseHelper;
    private int userID;
    private int activityID;
    private int[] postureScores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_data);

        databaseHelper = new DatabaseHelper(this);

        textViewSessionName = findViewById(R.id.textViewSessionName);
        textViewSessionSummary = findViewById(R.id.textViewSessionSummary);

        String username = getCurrentUser();
        userID = databaseHelper.getUserIdByUsername(username);

        activityID = getIntent().getIntExtra("logID", -1);
        postureScores = getIntent().getIntArrayExtra("postureScores");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Line graph = new Line();
        for (int i = 0; i < postureScores.length; i++) {
            LinePoint p = new LinePoint(i, postureScores[i]);
            graph.addPoint(p);
        }

        LineGraph lineGraph = findViewById(R.id.graph);
        lineGraph.addLine(graph);
        lineGraph.setRangeY(0, 100);

    }

    //allow toolbar to route back to parent activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public String getCurrentUser() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if the "loggedIn" flag is true
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            return sharedPreferences.getString("username", null);  // Return username if logged in
        } else {
            return null;  // Return null if not logged in
        }
    }
}
