package com.example.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.EventSlot;

import java.util.List;

public class ShiftListAdapter extends RecyclerView.Adapter<ShiftListAdapter.ShiftViewHolder> {
    private List<EventSlot> shifts;

    public ShiftListAdapter(List<EventSlot> shifts) {
        this.shifts = shifts;
    }

    @NonNull
    @Override
    public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shift, parent, false);
        return new ShiftViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
        EventSlot shift = shifts.get(position);
        holder.tvDate.setText(shift.getBegin_local().getDay());
        holder.tvStartTime.setText(shift.getBegin_local().getHours());
        holder.tvEndTime.setText(shift.getEnd_local().getHours());
    }

    @Override
    public int getItemCount() {
        return shifts.size();
    }

    public static class ShiftViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStartTime, tvEndTime;

        public ShiftViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.shiftDate);
            tvStartTime = itemView.findViewById(R.id.shiftStartTime);
            tvEndTime = itemView.findViewById(R.id.shiftEndTime);
        }
    }
}
