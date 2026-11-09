package com.example.fitness;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class EditWorkoutDialog extends DialogFragment {

    public interface OnSavedListener {
        void onSaved();
    }

    private static final String ARG_WORKOUT = "workout";

    private workout workoutItem;
    private OnSavedListener onSavedListener;
    private EditText etName, etType, etDescription, etMuscleGroup;

    public static EditWorkoutDialog newInstance(workout workoutItem) {
        EditWorkoutDialog dialog = new EditWorkoutDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WORKOUT, workoutItem);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnSavedListener(OnSavedListener listener) {
        this.onSavedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            workoutItem = (workout) getArguments().getSerializable(ARG_WORKOUT);
        }

        etName = view.findViewById(R.id.etName);
        etType = view.findViewById(R.id.etType);
        etDescription = view.findViewById(R.id.etDescription);
        etMuscleGroup = view.findViewById(R.id.etMuscleGroup);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (workoutItem != null) {
            etName.setText(workoutItem.getName());
            etType.setText(workoutItem.getType());
            etDescription.setText(workoutItem.getDescription());
            etMuscleGroup.setText(workoutItem.getMuscleGroup());
        }

        btnSave.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void saveChanges() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        if (workoutItem == null) return;

        workoutItem.setName(name);
        workoutItem.setType(etType.getText().toString().trim());
        workoutItem.setDescription(etDescription.getText().toString().trim());
        workoutItem.setMuscleGroup(etMuscleGroup.getText().toString().trim());

        FIrebaseServices.getInstance().getDb()
                .collection("workouts").document(workoutItem.getId())
                .set(workoutItem)
                .addOnSuccessListener(unused -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Changes saved", Toast.LENGTH_SHORT).show();
                    if (onSavedListener != null) {
                        onSavedListener.onSaved();
                    }
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Save failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
