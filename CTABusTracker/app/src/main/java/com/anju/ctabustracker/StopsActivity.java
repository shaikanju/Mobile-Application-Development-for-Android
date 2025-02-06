package com.anju.ctabustracker;




import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import android.Manifest;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anju.ctabustracker.databinding.ActivityStopsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StopsActivity extends AppCompatActivity {
    private ActivityStopsBinding binding;
    private RecyclerView recyclerView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private StopAdapter stopsAdapter;
    private List<Stop> stopList;
    private FusedLocationProviderClient fusedLocationClient;
    private static final String unityGameID = "9876543";

    private static final String bannerPlacement = "Banner_Android";
    private BannerView.IListener bannerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        binding = ActivityStopsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recyclerView = binding.rc1;  // Assuming you have a RecyclerView in your layout
        stopList = new ArrayList<>();  // Initialize the list to prevent NullPointerException

        // Initialize the adapter and set it to the RecyclerView

        // Retrieve data passed through the intent

        String routeNumber = getIntent().getStringExtra("routeNumber");
        String selectedDirection = getIntent().getStringExtra("direction");
        String routeName = getIntent().getStringExtra("routeName");
        int color = getIntent().getIntExtra("color",0);
        stopsAdapter = new StopAdapter(this, stopList, routeNumber, selectedDirection, routeName,color);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(stopsAdapter);
        binding.textView3.setText("Route "+routeNumber+" - "+routeName);
        binding.imageView3.setImageResource(R.drawable.bus_icon);
        binding.textView4.setText(selectedDirection+" Stops");
        double luminance = ColorUtils.calculateLuminance(color); // Get luminance

        // Set text color based on luminance
        int textColor = (luminance < 0.25) ? Color.WHITE : Color.BLACK;
        binding.textView4.setTextColor(textColor);
        binding.textView4.setBackgroundColor(color);

        if (routeNumber != null && selectedDirection != null) {
            getUserLocation(routeNumber, selectedDirection);  // Call the method to get the user's location
        } else {
            Toast.makeText(this, "Route number or direction not passed.", Toast.LENGTH_SHORT).show();
        }
        bannerListener = new BannerViewListener(this);
        UnityAds.initialize(this, unityGameID, false,
                new UnityInitializationListener(this));

    }
    public void showBanner() {

        BannerView bottomBanner = new BannerView(
                this, bannerPlacement, UnityBannerSize.getDynamicSize(this));
        bottomBanner.setListener(bannerListener);

        binding.layout.addView(bottomBanner);
        bottomBanner.load();
    }
    public void initFailed(String s) {


    }

    public void loadFailed(String s) {


    }

    private void getUserLocation(String routeNumber, String selectedDirection) {
        // Double-check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // If permissions are not granted, request them
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return; // Exit the method, will try again after permission result
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double userLatitude = location.getLatitude();
                            double userLongitude = location.getLongitude();
                            Log.d("location","user"+userLongitude+" "+userLatitude);
                            fetchStops(routeNumber, selectedDirection, userLatitude, userLongitude);
                        } else {
                            Toast.makeText(StopsActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permission granted, get user location
                String selectedDirection = getIntent().getStringExtra("direction");
                String routeNumber = getIntent().getStringExtra("routeNumber");
                getUserLocation(routeNumber, selectedDirection);
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchStops(String routeNumber, String selectedDirection, double userLatitude, double userLongitude) {
        RoutesVolley.fetchRouteStops(this, routeNumber, selectedDirection, new RoutesVolley.StopsCallback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                JSONObject bustimeResponse = response.getJSONObject("bustime-response");
                JSONArray stopsArray = bustimeResponse.getJSONArray("stops");
                Log.d("response", "Full response: " + response.toString());  // Logs the entire JSON response
                Log.d("response", "Stops array: " + stopsArray.toString());
                stopList.clear();

                for (int i = (stopsArray.length()-1); i >=0; i--) {
                    JSONObject stopJson = stopsArray.getJSONObject(i);
                    String stopId = stopJson.getString("stpid");
                    String stopName = stopJson.getString("stpnm");
                    double latitude = stopJson.getDouble("lat");
                    double longitude = stopJson.getDouble("lon");

                    String distanceStr = calculateDistanceAndDirection(userLatitude, userLongitude, latitude, longitude);
                    String distanceSubstring = distanceStr.split(",")[0]; // Get "Distance: X meters"
                    String directionfrom = distanceStr.split(",")[1];

                    double distance = Double.parseDouble(distanceSubstring.replace("Distance: ", "").replace(" meters", ""));
                    distance=Math.round(distance);

                    if (distance <= 1000) { // Filter stops within 1000 meters
                        stopList.add(new Stop(stopId, stopName, latitude, longitude, distance, directionfrom));
                    }
                }
                stopList.sort(Comparator.comparingDouble(Stop::getDistance));

                Log.d("hey there","the array"+stopList.size());

                stopsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(StopsActivity.this, "Error fetching stops: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String calculateDistanceAndDirection(double userLat, double userLon, double stopLat, double stopLon) {
        float[] results = new float[1];
        Location.distanceBetween(userLat, userLon, stopLat, stopLon, results);
        float distance = results[0]; // Distance in meters

        Location userLocation = new Location("user");
        userLocation.setLatitude(userLat);
        userLocation.setLongitude(userLon);

        Location stopLocation = new Location("stop");
        stopLocation.setLatitude(stopLat);
        stopLocation.setLongitude(stopLon);

        float bearing = userLocation.bearingTo(stopLocation); // Bearing in degrees

        // Convert bearing to a direction (simplified)
        String direction = getDirectionFromBearing(bearing);

        return "Distance: " + distance + " meters,"+direction;
    }

    private String getDirectionFromBearing(float bearing) {
        if (bearing >= 337.5 || bearing < 22.5) {
            return "North";
        } else if (bearing >= 22.5 && bearing < 67.5) {
            return "Northeast";
        } else if (bearing >= 67.5 && bearing < 112.5) {
            return "East";
        } else if (bearing >= 112.5 && bearing < 157.5) {
            return "Southeast";
        } else if (bearing >= 157.5 && bearing < 202.5) {
            return "South";
        } else if (bearing >= 202.5 && bearing < 247.5) {
            return "Southwest";
        } else if (bearing >= 247.5 && bearing < 292.5) {
            return "West";
        } else {
            return "Northwest";
        }
    }

}



