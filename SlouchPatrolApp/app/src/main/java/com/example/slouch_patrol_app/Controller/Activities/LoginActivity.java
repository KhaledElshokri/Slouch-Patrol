package com.example.slouch_patrol_app.Controller.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.slouch_patrol_app.Helpers.DatabaseHelper;
import com.example.slouch_patrol_app.R;

public class LoginActivity extends AppCompatActivity {

    EditText emailField, passwordField;
    Button signInButton, signUpButton;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);
        databaseHelper = new DatabaseHelper(this);

        //Sign-in button logic
        signInButton.setOnClickListener(v -> {
            String username = emailField.getText().toString();
            String password = passwordField.getText().toString();

            //Verify user credentials from the database
            boolean isValid = databaseHelper.verifyUser(username, password);
            if (isValid) {
                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                //Store user login status in SharedPreferences
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("username", username);
                editor.apply();

                //Redirect to MainActivity
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Account does not exist. Please sign up.", Toast.LENGTH_SHORT).show();
            }
        });

        //Sign-up button logic
        signUpButton.setOnClickListener(v -> {
            String username = emailField.getText().toString();
            String password = passwordField.getText().toString();

            //Insert new user into database
            boolean isInserted = databaseHelper.insertUser(username, password);
            if (isInserted) {
                Toast.makeText(LoginActivity.this, "Account Created", Toast.LENGTH_SHORT).show();

                //Store user login status in SharedPreferences
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("userEmail", username);
                editor.apply();

                //Redirect to MainActivity
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Account Creation Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}