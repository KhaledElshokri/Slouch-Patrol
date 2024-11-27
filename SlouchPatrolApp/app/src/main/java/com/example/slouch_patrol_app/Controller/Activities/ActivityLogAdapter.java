package com.example.slouch_patrol_app.Controller.Activities;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.slouch_patrol_app.R;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityLogAdapter extends RecyclerView.Adapter<ActivityLogAdapter.ActivityLogViewHolder>{
    private List<String> activityLogList;
    private Context context;

    public ActivityLogAdapter(List<String> activityLogList, Context context){
        this.activityLogList = activityLogList;
        this.context = context;

    }

    @NonNull
    @Override
    public ActivityLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activitylog_recycler_item, parent, false);
        return new ActivityLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityLogViewHolder holder, int position){
        String activityLog = activityLogList.get(position);

        // Parse and update the proper text views
        try {
            JSONObject jsonObject = new JSONObject(activityLog);

            String name = jsonObject.optString("sessionName", "Unknown Session"); // Default to "Unknown Session" if not present
                // TODO: change this from sessionType to timestamp
            String time = jsonObject.optString("sessionType", "Unknown Type"); // Default to "Unknown Type" if not present
            String data = jsonObject.optString("postureScores", "Unknown Data");

            holder.nameTextView.setText(name);
            holder.timeTextView.setText(time);

            // TODO: Add an onClick listener here
                // not sure if it should be in the try or below it
            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(context, "Session clicked: " + name, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, SessionDataActivity.class);
                intent.putExtra("logID", position);
                intent.putExtra("sessionName", name);
                intent.putExtra("postureScores", data);

                context.startActivity(intent);
            });

        } catch (JSONException e) {
            e.printStackTrace();
            holder.nameTextView.setText("Error parsing data");
            holder.timeTextView.setText("");
        }

    }

    @Override
    public int getItemCount(){
        return activityLogList.size();
    }

    public static class ActivityLogViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, timeTextView;

        public ActivityLogViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }

}

