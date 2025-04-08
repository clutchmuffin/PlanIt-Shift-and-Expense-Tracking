package com.example.myapplication.view.adapter;

import android.content.Intent;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.SharedCalendarActivity;
import com.example.myapplication.model.SharedCal;

import java.util.List;

public class SharedAdapter extends RecyclerView.Adapter<SharedAdapter.SharedViewHolder> {
    private final List<SharedCal> sharedList;

    public SharedAdapter(List<SharedCal> shared) {
        this.sharedList = shared;
    }

    @NonNull
    @Override
    public SharedAdapter.SharedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shared_card, parent, false);
        return new SharedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedAdapter.SharedViewHolder holder, int position) {
        SharedCal sharedCal = sharedList.get(position);

        holder.sharedTitle.setText(sharedCal.getName());
        String people = sharedCal.getMembers().size() + " member(s)";
        holder.sharedPeople.setText(people);
        holder.sharedCode.setText(sharedCal.getSharedId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SharedCalendarActivity.class);
            intent.putExtra(SharedCalendarActivity.EXTRA_SHARED, sharedCal);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sharedList.size();
    }

    public static class SharedViewHolder extends RecyclerView.ViewHolder {
        TextView sharedTitle, sharedPeople, sharedCode;
        public SharedViewHolder(@NonNull View itemView) {
            super(itemView);

            sharedTitle = itemView.findViewById(R.id.sharedTitle);
            sharedPeople = itemView.findViewById(R.id.sharedPeople);
            sharedCode = itemView.findViewById(R.id.sharedCode);
        }
    }
}
