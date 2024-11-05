package com.example.slouch_patrol_app.Controller.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.slouch_patrol_app.R;

public class SaveSessionFragment extends DialogFragment {

    public interface onSaveFragmentEventListener {
        void onSaveFragmentEvent(String event);
    }

    private onSaveFragmentEventListener saveFragmentEventListener;

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

        // get buttons
        Button resumeButton = view.findViewById(R.id.resumeButton);
        Button saveSessionButton = view.findViewById(R.id.saveSessionButton);
        Button discardSessionButton = view.findViewById(R.id.discardSessionButton);

        // get spinner
        Spinner sessionTypeSpinner = view.findViewById(R.id.activityTypeSpinner);

        // set click listeners
        resumeButton.setOnClickListener(v -> onSaveFragmentEvent("resume"));
        saveSessionButton.setOnClickListener(v -> onSaveFragmentEvent("save"));
        discardSessionButton.setOnClickListener(v -> onSaveFragmentEvent("discard"));

        // get spinner options from strings.xml
        String[] sessionTypes = getResources().getStringArray(R.array.session_types);

        // set spinner options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, sessionTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sessionTypeSpinner.setAdapter(adapter);

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
        if (saveFragmentEventListener!=null) {
            saveFragmentEventListener.onSaveFragmentEvent(event);
        }
        dismiss();
    }

}
