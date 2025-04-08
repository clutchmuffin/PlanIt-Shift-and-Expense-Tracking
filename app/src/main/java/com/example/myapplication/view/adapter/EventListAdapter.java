package com.example.myapplication.view.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.controller.JobDetailActivity;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.NotificationSender;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {
    private final List<CalendarEvent> events;
    private final Job job;
    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public EventListAdapter(List<CalendarEvent> events, Job job, Context context) {
        this.events = events != null ? events : new ArrayList<>();
        this.job = job;
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CalendarEvent event = events.get(position);
        holder.tvDateRange.setText(event.getBegin_date().substring(5) + " to " + event.getEnd_date().substring(5));
        holder.tvName.setText(event.getName());
        holder.tvTimeRange.setText(event.getBegin_time().substring(0,5) + " - " + event.getEnd_time().substring(0,5));
        holder.tvRepeatType.setText(event.getRepeated().toString());
        holder.tvNetPay.setText("$" + event.calculatePay());

        holder.eventDelete.setOnClickListener(
                v -> {
                    if (job.getJobId() != null) {

                        // Query for the event document ID based on event data
                        db.collection("Jobs").document(job.getJobId())
                                .collection("Events")
                                .whereEqualTo("begin_date", event.getBegin_date())
                                .whereEqualTo("begin_time", event.getBegin_time())
                                .whereEqualTo("end_date", event.getEnd_date())
                                .whereEqualTo("end_time", event.getEnd_time())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {

                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        // Get the document ID of the first matching event
                                        String eventDocId = queryDocumentSnapshots.getDocuments().get(0).getId();

                                        // Delete the event from Firestore
                                        db.collection("Jobs").document(job.getJobId())
                                                .collection("Events")
                                                .document(eventDocId)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {

                                                    // Delete from local list and update RecyclerView
                                                    cancelNotification(job.getEvents().remove(position));
                                                    notifyItemRemoved(position);
                                                    Log.d("JobDetailActivity", "Event successfully deleted from Firestore");
                                                })
                                                .addOnFailureListener(e -> Log.e("JobDetailActivity", "Error deleting event from Firestore", e));
                                    } else {
                                        Log.e("JobDetailActivity", "Could not find event document to delete");
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("JobDetailActivity", "Error querying for event to delete", e));
                    }
                });

        holder.eventEdit.setOnClickListener(
                v -> {
                    if (context instanceof JobDetailActivity) {
                        ((JobDetailActivity) context).showEditEventDialog(event, position);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private void cancelNotification(CalendarEvent event){
        NotificationSender notifSender = new NotificationSender(context);
        notifSender.cancelNotification(event);
        notifSender.updateWeeklyNotif();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateRange, tvName, tvTimeRange, tvRepeatType, tvNetPay;
        ImageButton eventDelete, eventEdit;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateRange = itemView.findViewById(R.id.eventDateRange);
            tvName = itemView.findViewById(R.id.eventName);
            tvTimeRange = itemView.findViewById(R.id.eventTimeRange);
            tvRepeatType = itemView.findViewById(R.id.eventRepeatInfo);
            tvNetPay = itemView.findViewById(R.id.netPay);
            eventDelete = itemView.findViewById(R.id.eventDeleteBtn);
            eventEdit = itemView.findViewById(R.id.eventEditBtn);
        }
    }
}
