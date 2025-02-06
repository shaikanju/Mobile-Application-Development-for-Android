package com.anju.ctabustracker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PredictionsAdapter extends RecyclerView.Adapter<PredictionsAdapter.PredictionViewHolder> {

    private List<Prediction> predictions;
    private Context context;
    private double stopLatitude;
    private double stopLongitude;
    private String stopName;
    private int  color;



    public PredictionsAdapter( Context context, List<Prediction> predictions, double stopLatitude, double stopLongitude,String stopName,int color) {
        this.predictions=predictions;
        this.context=context;
        this.stopLatitude=stopLatitude;
        this.stopLongitude=stopLongitude;
        this.stopName=stopName;
        this.color = color;

    }

    @NonNull
    @Override
    public PredictionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prediction_item, parent, false);
        return new PredictionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PredictionViewHolder holder, int position) {
        Prediction prediction = predictions.get(position);
        holder.busnumber.setText("Bus #"+prediction.getVehicleId());
        holder.duein.setText("Due in "+prediction.getCountdown()+" at");
        holder.directiontodestination.setText(prediction.getRouteDir()+" to "+prediction.getDestination());
        holder.arrivaltime.setText(prediction.getPredictedTime());
holder.itemView.setBackgroundColor(color);
        double luminance = ColorUtils.calculateLuminance(color); // Get luminance

        // Set text color based on luminance
        int textColor = (luminance < 0.25) ? Color.WHITE : Color.BLACK;
        holder.busnumber.setTextColor(textColor);
        holder.duein.setTextColor(textColor);
        holder.directiontodestination.setTextColor(textColor);
        holder.arrivaltime.setTextColor(textColor);
        holder.itemView.setOnClickListener(v -> {
            String vehicleId = prediction.getVehicleId();
            RoutesVolley.fetchVehicles(context, vehicleId, new RoutesVolley.VehiclesCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        JSONObject bustimeResponse = response.getJSONObject("bustime-response");
                        JSONArray vehicleArray = bustimeResponse.getJSONArray("vehicle");

                        if (vehicleArray.length() > 0) {
                            JSONObject vehicleObject = vehicleArray.getJSONObject(0);
                            double vehicleLatitude = vehicleObject.getDouble("lat");
                            double vehicleLongitude = vehicleObject.getDouble("lon");
                           // Example timestamp from API
                            float[] results = new float[1];


                            // Calculate distance
                              Location.distanceBetween(stopLatitude, stopLongitude, vehicleLatitude, vehicleLongitude,results);
                              Log.d("coords","lat"+stopLatitude+" "+stopLongitude+" "+ vehicleLatitude+" "+vehicleLongitude);
                              Log.d("distance", "distance"+results[0]);
                            float distance = results[0];

                            // Show dialog with distance and time
                            showBusInfoDialog(vehicleLatitude, vehicleLongitude, distance, prediction.getCountdown(),prediction.getVehicleId(),stopName);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(context, "Error parsing vehicle data", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(context, "Error fetching vehicle data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }
    private void showBusInfoDialog(double vehicleLat, double vehicleLon, double distance, String due,String id,String stopName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.splash_logo);

        builder.setTitle("Bus #"+id);

        // Message to show distance and time
        String message = "Bus #" + id+" is "+ String.format("%.2f", distance) + " m (" +due+" min) away from the "+ stopName+" stop";


        builder.setMessage(message);

        // Show on Map Button
        builder.setPositiveButton("Show on Map", (dialog, which) -> openGoogleMaps(vehicleLat, vehicleLon));

        // OK Button (just close dialog)
        builder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
//    private void openGoogleMaps(double lat, double lon) {
//        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lon + "?q=" + lat + "," + lon + "(Bus Location)");
//        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//        mapIntent.setPackage("com.google.android.apps.maps");
//
//        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
//            context.startActivity(mapIntent);
//        } else {
//            Toast.makeText(context, "Google Maps not installed", Toast.LENGTH_SHORT).show();
//        }
//    }
private void openGoogleMaps(double lat, double lon) {
    Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lon + "?q=" + lat + "," + lon + "(Bus Location)");
    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
    mapIntent.setPackage("com.google.android.apps.maps");

    // Check if Google Maps is installed
    if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
        context.startActivity(mapIntent);
    } else {
        // Open maps in a browser if Google Maps is not installed
        Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
        context.startActivity(webIntent);
    }
}



    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }


    @Override
    public int getItemCount() {
        return predictions.size();
    }

    public static class PredictionViewHolder extends RecyclerView.ViewHolder {

        TextView  busnumber,duein,directiontodestination,arrivaltime;

        public PredictionViewHolder(@NonNull View itemView) {
            super(itemView);
            busnumber = itemView.findViewById(R.id.busnumber);
            duein= itemView.findViewById(R.id.duein);
            directiontodestination= itemView.findViewById(R.id.directiontodestination);
            arrivaltime= itemView.findViewById(R.id.arrivaltime);
        }
    }
}
