package com.example.myapplication.view.adapter;

import android.view.ViewGroup;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.SharedCard;

import java.util.List;

public class SharedAdapter extends RecyclerView.Adapter<SharedAdapter.SharedViewHolder> {
    private List<SharedCard> sharedList;

    public SharedAdapter(List<SharedCard> shared) {
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
        SharedCard sharedCard = sharedList.get(position);

        holder.sharedTitle.setText(sharedCard.getName());
        holder.sharedPeople.setText(sharedCard.getMembers());
        holder.colourAccent.setBackgroundColor(sharedCard.getColour());
    }

    @Override
    public int getItemCount() {
        return sharedList.size();
    }

    static class SharedViewHolder extends RecyclerView.ViewHolder {
        TextView sharedTitle, sharedPeople;
        View colourAccent;
        public SharedViewHolder(@NonNull View itemView) {
            super(itemView);

            sharedTitle = itemView.findViewById(R.id.sharedTitle);
            sharedPeople = itemView.findViewById(R.id.sharedPeople);
            colourAccent = itemView.findViewById(R.id.colorAccent);
        }
    }
}
