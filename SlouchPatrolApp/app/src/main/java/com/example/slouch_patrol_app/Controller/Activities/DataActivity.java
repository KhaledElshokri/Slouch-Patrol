package com.example.slouch_patrol_app.Controller.Activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slouch_patrol_app.Controller.Fragments.StopSessionFragment;
import com.example.slouch_patrol_app.Helpers.DatabaseHelper;
import com.example.slouch_patrol_app.R;

import java.util.List;

public class DataActivity
        extends AppCompatActivity
        implements
        StopSessionFragment.onStopFragmentEventListener {

    private DatabaseHelper databaseHelper;
    private int userID;
    private TableLayout activityTable;
    private List<String> activityList;
    private RecyclerView recyclerView;
    private ActivityLogAdapter activityLogAdapter;

    private TextView usernameText;
    private TextView averageScoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data);

        // init db helper
        databaseHelper = new DatabaseHelper(this);

        // get profile id
        String username = getCurrentUser();
        userID = databaseHelper.getUserIdByUsername(username);

        // get all activity logs
        if (userID != -1) {
            activityList = databaseHelper.getActivityLogs(userID);
        }

        //set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.background_color, getTheme()));
        toolbar.setTitleTextColor(getResources().getColor(R.color.background_color, getTheme()));

        averageScoreText = findViewById(R.id.log_user_avg);
        usernameText = findViewById(R.id.log_user_text);

        averageScoreText.setText("Your Last average was: 74");
        usernameText.setText("Current user: " + username);

        //set recycler
        recyclerView = findViewById(R.id.listView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //populate recycler
        activityLogAdapter = new ActivityLogAdapter(activityList, this);
        recyclerView.setAdapter(activityLogAdapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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

    @Override
    public void onStopFragmentEvent(String event) {
        if (event.equals("delete")) {
            // delete selected activity
        }
    }
}