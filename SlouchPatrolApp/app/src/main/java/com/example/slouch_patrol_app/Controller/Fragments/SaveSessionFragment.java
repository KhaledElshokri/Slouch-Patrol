package com.example.slouch_patrol_app.Controller.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.example.slouch_patrol_app.Helpers.DatabaseHelper;
import com.example.slouch_patrol_app.Helpers.SharedPreferencesHelper;
import com.example.slouch_patrol_app.Model.SessionData;
import com.example.slouch_patrol_app.R;
import com.google.gson.Gson;

public class SaveSessionFragment extends DialogFragment {

    public interface onSaveFragmentEventListener {
        void onSaveFragmentEvent(String event);
    }

    private onSaveFragmentEventListener saveFragmentEventListener;
    private EditText name, notes;
    private Spinner sessionTypeSpinner;
    private DatabaseHelper databaseHelper;
    private SharedPreferencesHelper sharedPreferencesHelper;

    public SaveSessionFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            saveFragmentEventListener = (onSaveFragmentEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onEventListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate layout
        View view = inflater.inflate(R.layout.fragment_save_session, container, false);

        // initialize helpers
        databaseHelper = new DatabaseHelper(getActivity());
        sharedPreferencesHelper = new SharedPreferencesHelper(getActivity());

        // get buttons
        Button resumeButton = view.findViewById(R.id.resumeButton);
        Button saveSessionButton = view.findViewById(R.id.saveSessionButton);
        Button discardSessionButton = view.findViewById(R.id.discardSessionButton);

        // get edit text
        name = view.findViewById(R.id.editTextName);
        notes = view.findViewById(R.id.editTextNotes);

        // get spinner
        this.sessionTypeSpinner = view.findViewById(R.id.activityTypeSpinner);

        // set click listeners
        resumeButton.setOnClickListener(v -> onSaveFragmentEvent("resume"));
        saveSessionButton.setOnClickListener(v -> onSaveFragmentEvent("save"));
        discardSessionButton.setOnClickListener(v -> onSaveFragmentEvent("discard"));

        // get spinner options from strings.xml
        String[] sessionTypes = getResources().getStringArray(R.array.session_types);

        // set spinner options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, sessionTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.sessionTypeSpinner.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    // TO HANDLE EVENTS FROM FRAGMENT
    private void onSaveFragmentEvent(String event) {

        if (event.equals("save")) {
            // Get required information to be saved in activity table
            String username = getCurrentUser();
            int userID = databaseHelper.getUserIdByUsername(username);
            String sessionType = sessionTypeSpinner.getSelectedItem().toString();
            String sessionName = name.getText().toString();
            String sessionNotes = notes.getText().toString();
            int[] postureScores = databaseHelper.getPostureScoresByUserID(userID);

            // serialize data to be saved in activity table
            SessionData sessionData = new SessionData(sessionType, sessionName, sessionNotes, postureScores);
            Gson gson = new Gson();
            String sessionDataJSON = gson.toJson(sessionData);

            // save to database
            boolean success = databaseHelper.insertActivity(userID, sessionDataJSON);
            if (success) {
                Toast.makeText(getActivity(), "Session saved to activity log", Toast.LENGTH_SHORT).show();
            }
        }

        if (saveFragmentEventListener!=null) {
            saveFragmentEventListener.onSaveFragmentEvent(event);
        }
        dismiss();
    }

    public String getCurrentUser() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Check if the "loggedIn" flag is true
        boolean isLoggedIn = sharedPreferences.getBoolean("loggedIn", false);

        if (isLoggedIn) {
            return sharedPreferences.getString("username", null);  // Return username if logged in
        } else {
            return null;  // Return null if not logged in
        }
    }

}
