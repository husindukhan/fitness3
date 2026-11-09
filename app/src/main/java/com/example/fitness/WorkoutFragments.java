package com.example.fitness;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class WorkoutFragments extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private FIrebaseServices fbs;

    // Search UI
    private EditText etSearchWorkout;
    private Button btnSearch;
    private RecyclerView rvWorkoutResults;
    private WorkoutAdapter adapter;
    private List<workout> workoutList;

    // Add Workout UI (with Image Storage)
    private ImageView ivWorkout;
    private EditText etAddName, etAddType, etAddMuscleGroup, etAddDescription;
    private Button btnAddWorkout;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public WorkoutFragments() {
        // Required empty public constructor
    }

    public static WorkoutFragments newInstance(String param1, String param2) {
        WorkoutFragments fragment = new WorkoutFragments();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivWorkout.setImageURI(selectedImageUri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout_fragments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fbs = FIrebaseServices.getInstance();

        // Initialize Search UI
        etSearchWorkout = view.findViewById(R.id.etSearchWorkout);
        btnSearch = view.findViewById(R.id.btnSearch);
        rvWorkoutResults = view.findViewById(R.id.rvWorkoutResults);

        rvWorkoutResults.setLayoutManager(new LinearLayoutManager(getContext()));
        workoutList = new ArrayList<>();
        
        // Initialize adapter with listener for Update and Delete clicks (matching teacher's pattern)
        adapter = new WorkoutAdapter(getContext(), workoutList, new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onUpdateClick(workout wrk) {
                Toast.makeText(getContext(), "Update clicked for: " + wrk.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Populate form or open update screen with wrk data
            }

            @Override
            public void onDeleteClick(workout wrk) {
                fbs.getDb().collection("workouts").document(wrk.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "Workout deleted successfully", Toast.LENGTH_SHORT).show();
                            workoutList.remove(wrk);
                            adapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(WorkoutFragments.this::showError);
            }
        });
        rvWorkoutResults.setAdapter(adapter);

        // Search Button Action
        btnSearch.setOnClickListener(v -> {
            String searchQuery = etSearchWorkout.getText().toString().trim();
            if (searchQuery.isEmpty()) {
                etSearchWorkout.setError("Please enter a workout name");
                return;
            }

            fbs.getDb().collection("workouts")
                    .whereEqualTo("name", searchQuery)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!isAdded()) return;
                        workoutList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            workout wrk = doc.toObject(workout.class);
                            if (wrk != null) {
                                workoutList.add(wrk);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (workoutList.isEmpty()) {
                            Toast.makeText(getContext(), "No workouts found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(this::showError);
        });

        // Initialize Add Workout UI
        ivWorkout = view.findViewById(R.id.ivWorkout);
        etAddName = view.findViewById(R.id.etAddName);
        etAddType = view.findViewById(R.id.etAddType);
        etAddMuscleGroup = view.findViewById(R.id.etAddMuscleGroup);
        etAddDescription = view.findViewById(R.id.etAddDescription);
        btnAddWorkout = view.findViewById(R.id.btnAddWorkout);

        // Pick Image on ImageView click
        ivWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Add Workout Button Action (Matching instructor's Storage + DocumentReference pattern)
        btnAddWorkout.setOnClickListener(v -> {
            String name = etAddName.getText().toString().trim();
            String type = etAddType.getText().toString().trim();
            String muscleGroup = etAddMuscleGroup.getText().toString().trim();
            String description = etAddDescription.getText().toString().trim();

            if (name.isEmpty() || type.isEmpty() || muscleGroup.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri == null) {
                Toast.makeText(getContext(), "Please pick a workout image", Toast.LENGTH_SHORT).show();
                return;
            }

            btnAddWorkout.setEnabled(false); // prevent double clicks

            // Step 1: DocumentReference for unique ID
            DocumentReference doc = fbs.getDb().collection("workouts").document();
            StorageReference workoutImageRef = fbs.getStorage().getReference()
                    .child("workouts/" + doc.getId() + ".jpg");

            // Step 2: Upload image to Firebase Storage
            workoutImageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(snapshot -> {
                        if (!isAdded()) return;
                        // Step 3: Get download URL and save workout to Firestore
                        workoutImageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    saveWorkoutToFirestore(doc, name, type, description, muscleGroup, uri.toString());
                                })
                                .addOnFailureListener(this::showError);
                    })
                    .addOnFailureListener(this::showError);
        });
    }

    private void saveWorkoutToFirestore(DocumentReference doc, String name, String type, String description, String muscleGroup, String imageUrl) {
        workout newWorkout = new workout(doc.getId(), name, type, description, muscleGroup, imageUrl);

        doc.set(newWorkout)
                .addOnSuccessListener(unused -> {
                    if (!isAdded()) return;
                    btnAddWorkout.setEnabled(true);
                    Toast.makeText(getContext(), "Workout added!", Toast.LENGTH_SHORT).show();
                    
                    // Reset form
                    etAddName.setText("");
                    etAddType.setText("");
                    etAddMuscleGroup.setText("");
                    etAddDescription.setText("");
                    ivWorkout.setImageResource(R.mipmap.ic_launcher);
                    selectedImageUri = null;
                })
                .addOnFailureListener(this::showError);
    }

    private void showError(Exception e) {
        if (!isAdded()) return;
        btnAddWorkout.setEnabled(true);
        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
