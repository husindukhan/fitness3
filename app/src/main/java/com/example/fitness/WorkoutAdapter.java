package com.example.fitness;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    public interface OnWorkoutClickListener {
        void onUpdateClick(workout wrk);
        void onDeleteClick(workout wrk);
    }


    private final Context context;
    private final List<workout> workoutList;
    private final OnWorkoutClickListener listener;

    public WorkoutAdapter(Context context, List<workout> workoutList, OnWorkoutClickListener listener) {
        this.context = context;
        this.workoutList = workoutList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        workout wrk = workoutList.get(position);
        holder.tvName.setText(wrk.getName());
        holder.tvType.setText(wrk.getType());

        // Glide downloads the image from its URL and shows it
        Glide.with(holder.itemView.getContext())
                .load(wrk.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivItem);

        // Update and Delete button clicks
        if (listener != null) {
            holder.btnUpdate.setOnClickListener(v -> listener.onUpdateClick(wrk));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(wrk));
        }

        // Click row to show details dialog
        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(wrk.getName());
            builder.setMessage(
                    "Type: " + wrk.getType() + "\n" +
                    "Muscle Group: " + wrk.getMuscleGroup() + "\n\n" +
                    "Steps / Description:\n" + wrk.getDescription()
            );
            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    // ViewHolder keeps the views of one row so we find them only once
    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType;
        ImageView ivItem;
        Button btnUpdate, btnDelete;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvWorkoutName);
            tvType = itemView.findViewById(R.id.tvWorkoutType);
            ivItem = itemView.findViewById(R.id.ivWorkoutItem);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
