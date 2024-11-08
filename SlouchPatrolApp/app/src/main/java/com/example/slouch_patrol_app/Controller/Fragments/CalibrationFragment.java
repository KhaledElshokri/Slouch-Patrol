package com.example.slouch_patrol_app.Controller.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.slouch_patrol_app.R;

public class CalibrationFragment extends DialogFragment {

    public interface onCalibrateListener {
        void onCalibrate(Boolean isConnected);
    }

    private onCalibrateListener calibrateListener;

    public CalibrationFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            calibrateListener = (onCalibrateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate layout
        View view = inflater.inflate(R.layout.fragment_calibration, container, false);

        Button calibrateButton = view.findViewById(R.id.calibrateButton);

        calibrateButton.setOnClickListener(v -> calibrateDevice());

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

    private void calibrateDevice() {
        //TODO: ENSURE THE DEVICE IS CONNECTED
        //      SET IDEAL POSITIONS
        Boolean isConnected = true;
        if (calibrateListener!=null) {
            calibrateListener.onCalibrate(isConnected);
        }
        dismiss();
    }


}
