package com.example.slouch_patrol_app.Controller.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.example.slouch_patrol_app.Helpers.DatabaseHelper;
import com.example.slouch_patrol_app.R;

public class StopSessionFragment extends DialogFragment {

    public interface onStopFragmentEventListener {
        void onStopFragmentEvent(String event);
    }

    private onStopFragmentEventListener stopFragmentEventListener;
    private TextView textViewSessionTime, textViewSessionAverage;
    private int userID;
    private String sessionTime;
    private int averageScore;

    public StopSessionFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            stopFragmentEventListener = (onStopFragmentEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate layout
        View view = inflater.inflate(R.layout.fragment_stopped, container, false);
        textViewSessionTime = view.findViewById(R.id.textViewSessionTime);
        textViewSessionAverage = view.findViewById(R.id.textViewSessionAverage);

        // get arguments passed from parent activity
        assert getArguments() != null;
        userID = getArguments().getInt("userID");
        sessionTime = getArguments().getString("runtime");
        averageScore = getArguments().getInt("avgScore");

        // get buttons
        ImageButton recalibrateButton = view.findViewById(R.id.recalibrateButton);
        Button resumeButton = view.findViewById(R.id.resumeButton);
        Button finishButton = view.findViewById(R.id.finishButton);

        // set button actions
        recalibrateButton.setOnClickListener(v -> onStopFragmentEvent("recalibrate"));
        resumeButton.setOnClickListener(v -> onStopFragmentEvent("resume"));
        finishButton.setOnClickListener(v -> onStopFragmentEvent("finish"));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }

        // display them
        textViewSessionTime.setText("Runtime:\n" + sessionTime);
        textViewSessionAverage.setText("Average Score:\n" + averageScore);
    }

    private void onStopFragmentEvent(String event) {
        if (stopFragmentEventListener != null) {
            stopFragmentEventListener.onStopFragmentEvent(event);
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
