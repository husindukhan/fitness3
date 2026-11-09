package com.example.fitness;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.app.AlertDialog;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShowWorkoutsFragment extends Fragment {

    private FIrebaseServices fbs;
    private RecyclerView rvWorkouts;
    private WorkoutAdapter adapter;
    private List<workout> workoutList;

    public ShowWorkoutsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_workouts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fbs = FIrebaseServices.getInstance();

        rvWorkouts = view.findViewById(R.id.rvWorkouts);
        rvWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        workoutList = new ArrayList<>();

        adapter = new WorkoutAdapter(getContext(), workoutList, new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onUpdateClick(workout wrk) {
                Toast.makeText(getContext(), "Update: " + wrk.getName(), Toast.LENGTH_SHORT).show();
                EditWorkoutDialog dialog = EditWorkoutDialog.newInstance(wrk);
                dialog.setOnSavedListener(() -> loadWorkouts());
                dialog.show(getParentFragmentManager(), "editWorkoutDialog");
            }

            @Override
            public void onDeleteClick(workout wrk) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete workout")
                        .setMessage("Are you sure you want to delete "
                                + wrk.getName() + "?")
                        .setPositiveButton("Yes", (dialog, which) -> deleteWorkout(wrk))
                        .setNegativeButton("No", null)
                        .show();
                fbs.getDb().collection("workouts").document(wrk.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "Workout deleted", Toast.LENGTH_SHORT).show();
                            workoutList.remove(wrk);
                            adapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        rvWorkouts.setAdapter(adapter);
        loadWorkouts();
    }
    private void deleteWorkout(workout wrk) {
        fbs.getDb().collection("workouts").document(wrk.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    showMessage("Workout deleted");
                    loadWorkouts();
                })
                .addOnFailureListener(e -> showMessage("Delete failed: " + e.getMessage()));
    }

    private void showMessage(String message) {
        if (!isAdded()) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loadWorkouts() {
        fbs.getDb().collection("workouts").get()
                .addOnSuccessListener(result -> {
                    if (!isAdded()) return;
                    workoutList.clear();
                    for (DocumentSnapshot document : result.getDocuments()) {
                        workout workoutItem = document.toObject(workout.class);
                        if (workoutItem != null) {
                            workoutItem.setId(document.getId());
                            workoutList.add(workoutItem);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Could not load: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
