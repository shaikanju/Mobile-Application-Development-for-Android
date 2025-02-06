package com.anju.ctabustracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StopAdapter extends RecyclerView.Adapter<StopAdapter.StopViewHolder> {

    private static final String AREFS_NAME = "alerts";
    private List<Stop> stops;
    private Context context;
    private String routeNumber;
    private String selectedDirection;
    private String routeName;
    private int color;

    public StopAdapter(Context context, List<Stop> stopList, String routeNumber, String selectedDirection,String routeName,int color) {
        this.context = context;
        this.stops = stopList;
        this.routeNumber = routeNumber;
        this.selectedDirection = selectedDirection;
        this. routeName = routeName;
        this.color= color;
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stop, parent, false);
        return new StopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        Stop stop = stops.get(position);
        holder.stopNameTextView.setText(stop.getStopName());
        holder.itemView.setBackgroundColor(color);
        double luminance = ColorUtils.calculateLuminance(color); // Get luminance

        // Set text color based on luminance
        int textColor = (luminance < 0.25) ? Color.WHITE : Color.BLACK;
        holder.stopNameTextView.setTextColor(textColor);
        holder.stopdistance.setTextColor(textColor);
        holder.stopdistance.setText(String.format("%.0f m ", stop.getDistance())+stop.getDirectionfrom()+" of your location");
        holder.itemView.setOnClickListener(v -> {
            fetchServiceAlerts(context, routeNumber, selectedDirection, () -> {
                // After alerts are shown, proceed to PredictionsActivity
                Intent intent = new Intent(context, PredictionsActivity.class);
                intent.putExtra("stopName", stop.getStopName());
                intent.putExtra("routeNumber", routeNumber);  // Send the route number
                intent.putExtra("direction", selectedDirection);  // Send the direction
                intent.putExtra("routeName", routeName);
                intent.putExtra("stopId", stop.getStopId());
                intent.putExtra("latitude", stop.getLatitude());
                intent.putExtra("longitude", stop.getLongitude());
                intent.putExtra("color",color);

                context.startActivity(intent);
            });
        });

    }
    private void fetchServiceAlerts(Context context, String routeNumber, String direction, Runnable onComplete) {
        String url = "https://www.transitchicago.com/api/1.0/alerts.aspx?routeid=" + routeNumber + "&activeonly=true&outputType=JSON";

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray alertsArray = response.getJSONObject("CTAAlerts").getJSONArray("Alert");
                        List<JSONObject> newAlerts = filterNewAlerts(context, routeNumber, alertsArray);

                        if (!newAlerts.isEmpty()) {
                            showAlertsSequentially(context, routeNumber, newAlerts, onComplete);
                        } else {
                            onComplete.run(); // No alerts, proceed immediately
                        }

                    } catch (JSONException e) {
                        Log.e("Alerts", "Error parsing alerts JSON", e);
                        onComplete.run(); // Proceed even if there's an error
                    }
                },
                error -> {
                    Log.e("Alerts", "Error fetching alerts: " + error.getMessage());
                    onComplete.run(); // Proceed even if there's an error
                });

        queue.add(request);
    }
    private void showAlertsSequentially(Context context, String routeNumber, List<JSONObject> alerts, Runnable onComplete) {
        if (alerts.isEmpty()) {
            onComplete.run(); // No alerts to show, proceed
            return;
        }

        showAlertDialog(context, routeNumber, alerts, 0, onComplete);
    }

    private void showAlertDialog(Context context, String routeNumber, List<JSONObject> alerts, int index, Runnable onComplete) {
        if (index >= alerts.size()) {
            onComplete.run(); // All alerts shown, proceed
            return;
        }

        try {
            JSONObject alert = alerts.get(index);
            String alertId = alert.getString("AlertId");
            String headline = alert.getString("Headline");
            String description = alert.getString("ShortDescription");

            new AlertDialog.Builder(context)
                    .setTitle(headline)
                    .setMessage(description)
                    .setPositiveButton("OK", (dialog, which) -> {
                        saveDisplayedAlert(context, routeNumber, alertId);
                        showAlertDialog(context, routeNumber, alerts, index + 1, onComplete); // Show next alert
                    })
                    .setCancelable(false)
                    .show();

        } catch (JSONException e) {
            Log.e("Alerts", "Error displaying alert dialog", e);
            showAlertDialog(context, routeNumber, alerts, index + 1, onComplete); // Skip error and show next
        }
    }

    private static List<JSONObject> filterNewAlerts(Context context, String routeNumber, JSONArray alertsArray) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences(AREFS_NAME, Context.MODE_PRIVATE);
        List<JSONObject> newAlerts = new ArrayList<>();

        for (int i = 0; i < alertsArray.length(); i++) {
            JSONObject alert = alertsArray.getJSONObject(i);
            String alertId = alert.getString("AlertId");

            // Check if "routeNumber_AlertId" exists in SharedPreferences
            if (!prefs.contains(routeNumber + "_" + alertId)) {
                newAlerts.add(alert);
            }
        }

        return newAlerts;
    }
    private static void saveDisplayedAlert(Context context, String routeNumber, String alertId) {
        SharedPreferences prefs = context.getSharedPreferences(AREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Store using "routeNumber_AlertId" as key
        editor.putBoolean(routeNumber + "_" + alertId, true);
        editor.apply();
    }


    @Override
    public int getItemCount() {
        return stops.size();
    }

    public static class StopViewHolder extends RecyclerView.ViewHolder {

        TextView stopNameTextView,stopdistance;

        public StopViewHolder(@NonNull View itemView) {
            super(itemView);
            stopNameTextView = itemView.findViewById(R.id.textView5);
            stopdistance= itemView.findViewById(R.id.textView6);
        }
    }
}
