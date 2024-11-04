package com.example.slouch_patrol_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

public class MainFragment extends DialogFragment {

    protected TextView score;
    protected Button stopButton;

    public MainFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate layout
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        stopButton = view.findViewById(R.id.stop_button);
        score = view.findViewById(R.id.textViewScore);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSession();
            }
        });

        return view;
    }

    private void stopSession() {
    }

}
