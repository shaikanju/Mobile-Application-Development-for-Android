package com.ayesha.androidweatherapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.ayesha.androidweatherapp.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private ActivityMainBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    private HourlyForecastAdapter hourlyForecastAdapter; // Assume this is your RecyclerView adapter
    private List<HourlyForecast> hourlyForecasts;
    private TreeMap<String, Double> tempPoints;
    private String resolvedAddress= "";
    private String currentLocationCityName= "";
    private String cityName="";
    private boolean isUnitsF = true;
    private String location="";
    private String conditions ="";
    private int humidity;
    private int windDir;
    private int windSpeed;
    private int uvIndex;
    private String formattedSunriseTime="";
    private String formattedSunsetTime="";
    private double visibility;
    private double temperature;
    private int feelsLike;
private String postalCode="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tempPoints = new TreeMap<>();
        checkInternetAndShowAlert();

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        hourlyForecasts = new ArrayList<>();
        hourlyForecastAdapter = new HourlyForecastAdapter(hourlyForecasts);

        // Set up RecyclerView using binding
        binding.recyclerViewHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewHourly.setAdapter(hourlyForecastAdapter);


        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocationAndWeatherData();
        }
        //    fetchLocationAndWeatherData()
        binding.icon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   fetchLocationAndWeatherData();
            }
        });

        binding.icon5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInternetAndShowAlert();
                // Check if resolvedAddress is available
                if (!resolvedAddress.isEmpty()) {
                    // Create an Intent to send resolvedAddress to ForecastActivity
                    Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
                    intent.putExtra("resolvedAddress", resolvedAddress);  // Pass the resolvedAddress as an extra
                    intent.putExtra("cityName", cityName);
                    intent.putExtra("isUnitsF", isUnitsF);

                    // Start ForecastActivity
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Resolved Address not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.icon3.setOnClickListener(v -> {
            // Define the subject and the message you want to share
            String subject = "Weather for" +cityName+"("+postalCode+")";
            String message = "Weather for " +cityName+" ("+postalCode+"):\n"+
                    "Forecast: "+conditions+"\n" +

                    "Now: "+temperature+ "°F "+conditions+" (Feels like: " +feelsLike+"°F\n" +
                    "Humidity: " + humidity+"%\n"+
                    "Winds: " + getDirection(windDir) + " at " + windSpeed + " mph"+"\n"+
                    "UV Index: " + uvIndex+"\n"+
                    "Sunrise: " + formattedSunriseTime+"\n"+
                    "Sunset: " + formattedSunsetTime+"\n"+
                    "Visibility: "+visibility+"mi";

            // Create an Intent for sharing
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain"); // Set the type of content to share (plain text)

            // Add the subject and message to the intent
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);

            // Start the share activity (this will open a chooser for available sharing options)
            startActivity(Intent.createChooser(shareIntent, "Share Weather Information"));
        });

        binding.icon4.setImageResource(R.drawable.units_f);

        // Set an OnClickListener using binding
        binding.icon4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInternetAndShowAlert();
                toggleImage();
            }
        });
        binding.icon6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInternetAndShowAlert();
                showLocationInputDialog();
            }
        });
        binding.icon1.setOnClickListener(v -> {
            checkInternetAndShowAlert();
            // Replace this with the actual city name you're working with
            openGoogleMapsWithCity(cityName);
        });


    }
    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    // Method to check internet and show alert if no connection
    private void checkInternetAndShowAlert() {
        if (!isInternetAvailable()) {
            new AlertDialog.Builder(this)
                    .setTitle("No Internet Connection")
                    .setMessage("This app requires an internet connection to function properly. Please check your connection and try again.")
                    .setIcon(R.drawable.alert)  // Optional icon
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
    private void openGoogleMapsWithCity(String cityName)
        {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(cityName)));
            intent.setPackage("com.google.android.apps.maps");  // Explicitly set the Google Maps package
            startActivity(intent);  // Open the map with the location
        }



    private void showLocationInputDialog() {
        // Create an EditText to allow user input
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT); // Allow text input (city, state/country)

        // Create the AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Enter Location")
                .setMessage("For US locations, enter City, State (e.g., Chicago, IL). For international locations, enter City, Country (e.g., Paris, France).")
                .setView(input)  // Set the EditText as the view in the dialog
                .setPositiveButton("OK", (dialog, which) -> {
                    location = input.getText().toString().trim();
                    if (!location.isEmpty()) {
                        // Handle the location input (e.g., you could use it to fetch weather or geolocation)
                        boolean locationResolved = processLocation(location);
                        if (!locationResolved) {
                            // If the location cannot be resolved, show an error dialog
                            showLocationErrorDialog();
                        }
                    }
                     else {
                        // If no input, show a message
                        android.widget.Toast.makeText(MainActivity.this, "Please enter a location", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showLocationErrorDialog() {
        // Create an AlertDialog to show error message with an image
//        android.widget.ImageView imageView = new android.widget.ImageView(this);
//        imageView.setImageResource(R.drawable.alert); // Assuming alert.png is in the drawable folder

        new AlertDialog.Builder(this)
                .setTitle("Location Not Resolved")
                .setMessage("The specified location cannot be resolved. Please try a different location.")
                .setIcon(R.drawable.alert)  // Add the ImageView with the alert icon
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean processLocation(String location) {
        checkInternetAndShowAlert();
        // Split the location into city and state/country
        String[] locationParts = location.split(",");
        if (locationParts.length == 2) {
            String city = locationParts[0].trim();
            String region = locationParts[1].trim();

            // Use Geocoder to get latitude and longitude for the location
            getCoordinatesFromLocation(city, region);
            return true;
        } else {
            android.widget.Toast.makeText(this, "Invalid location format. Please use 'City, State' or 'City, Country'.", android.widget.Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    private void getCoordinatesFromLocation(String city, String region){
        checkInternetAndShowAlert();

        hourlyForecasts.clear();

// Optionally, reinitialize the list (though this is not required if you only want to clear it)

        String apiKey = "EC6MLZ4WBZNZVYDJ942YQPYMQ"; // Replace with your actual API key
        String apiUrl = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" +
                city + "," + region + "?key=" + apiKey;


        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Clear the existing list



                        try {
                            // Extract resolvedAddress from the root level of the response
                            resolvedAddress = response.getString("resolvedAddress");
                            Log.d("ResolvedAddress", resolvedAddress);

                            if (isLatLong(resolvedAddress)) {
                                cityName = reverseGeocode(MainActivity.this, resolvedAddress);
                                Log.d("CityName", "City Name: " + cityName);
                            } else {
                                // If already in "City, State, Country" format, extract the city
                                cityName = extractCityName(resolvedAddress);

                            }


                            Log.d("CityName", "City Name: " + cityName);




                            // Extract currentConditions from the response
                            JSONObject currentConditions = response.getJSONObject("currentConditions");

                            // Parse temperature and other weather details
                             temperature = currentConditions.getDouble("temp");
                            if (!isUnitsF) {
                                // Convert to Celsius if isUnitsF is false
                                temperature = (temperature - 32) * 5 / 9;
                            }
                            Log.d("WeatherApp", "Current Temperature: " + temperature);

                            // Set the background gradient based on temperature
                            ColorMaker.setColorGradient(binding.getRoot(), temperature, isUnitsF ? "F" : "C");

                            // Parse additional weather data
                             feelsLike = currentConditions.getInt("feelslike");
                            if (!isUnitsF) {
                                feelsLike = (int) ((feelsLike - 32) * 5 / 9);
                            }
                             humidity = currentConditions.getInt("humidity");
                             uvIndex = currentConditions.getInt("uvindex");

                             conditions = currentConditions.getString("conditions");
                            long currenttimeepoch = currentConditions.getLong("datetimeEpoch");
                            long currenttimeepoch1 = currenttimeepoch * 1000;
                            Date date1 = new Date(currenttimeepoch1);
                            SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEE MMM dd hh:mm a");


                            String formattedDate1 = dateFormat1.format(date1);
                            int cloudcover = currentConditions.getInt("cloudcover");
                             windDir = currentConditions.getInt("winddir");
                             windSpeed = currentConditions.getInt("windspeed");
                            int windgust = currentConditions.getInt("windgust");
                            Log.d("WindInfo", "Wind Direction: " + windDir);
                            Log.d("WindInfo", "Wind Speed: " + windSpeed);
                            Log.d("WindInfo", "Wind Gust: " + windgust);
                             visibility = currentConditions.getDouble("visibility");
                            long sunrisetimeepoch = currentConditions.getLong("sunriseEpoch");
                            long sunriseTimeEpoch = currentConditions.getLong("sunriseEpoch");
                            Log.d("heysunrise", "Sunrise Epoch Time: " + sunriseTimeEpoch);

                            Date sunriseDate = new Date(sunrisetimeepoch * 1000);

// Format the Date object to "h:mm a" (e.g., "7:00 AM")
                            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
                             formattedSunriseTime = timeFormat.format(sunriseDate);

                            long sunsetimeepoch = currentConditions.getLong("sunsetEpoch");
                            Date sunsetDate = new Date(sunsetimeepoch * 1000);

// Format the Date object to "h:mm a" (e.g., "7:00 AM")
                            SimpleDateFormat timeFormat2 = new SimpleDateFormat("h:mm a", Locale.US);
                             formattedSunsetTime = timeFormat2.format(sunsetDate);

                            Log.d("Visibility", "Current visibility: " + visibility);

                            // Update UI elements with the parsed data
                            binding.resolvedAdress.setText(getCityName(cityName)+", "+formattedDate1);
                            binding.Temperature.setText(String.format("%.1f°%s", temperature, isUnitsF ? "F" : "C"));
                            binding.feelslike.setText("Feels like: " + feelsLike + (isUnitsF ? " °F" : " °C"));

                            binding.Humidity.setText("Humidity: " + humidity + "%");
                            binding.UVindex.setText("UV Index: " + uvIndex);
                            binding.Weatherdescriptionr.setText(conditions + " (" + cloudcover + "% clouds)");
                            binding.Winddirection.setText("Winds: " + getDirection(windDir) + " at " + windSpeed + " mph gusting to " + windgust);
                            binding.Visibility.setText("Visibility: " + visibility + " miles");
                            binding.Sunrise.setText("Sunrise: "+ formattedSunriseTime );
                            binding.Sunset.setText("Sunset: "+ formattedSunsetTime );

                            // Get the appropriate weather icon
                            // Convert the weather conditions to lowercase and replace dashes with underscores
                            String iconName = currentConditions.getString("icon").replace("-", "_");
                            Log.d("icon", iconName);
// Get the resource ID of the desired icon
                            int iconID = getId(iconName, R.drawable.class);

// Set the image resource for the weather icon; use a default icon if not found
                            if (iconID != 0) {
                                binding.Weathericon.setImageResource(iconID);
                            } else {
                                // Optional: Set a default icon in case the specified icon is not found
                                binding.Weathericon.setImageResource(R.mipmap.ic_launcher); // Replace with your default icon if necessary
                            }
                            JSONArray daysArray = response.getJSONArray("days");
                            if (daysArray.length() > 0) {
                                for (int j = 0; j < 3; j++){
                                    JSONObject dayForecast = daysArray.getJSONObject(j); // Assuming you want the first day's data
                                    JSONArray hoursArray = dayForecast.getJSONArray("hours");

                                    // List to hold hourly forecasts


                                    // Loop through the hours and create HourlyForecast objects
                                    for (int i = 0; i < hoursArray.length(); i++) {
                                        JSONObject hourData = hoursArray.getJSONObject(i);
                                        long datetimeEpoch = hourData.getLong("datetimeEpoch");

                                        if (datetimeEpoch > currenttimeepoch) {
                                            // Extract required fields
                                            String datetime = hourData.getString("datetime"); // Hour time
                                            // Epoch time
                                            double temp = hourData.getDouble("temp");
                                            if (!isUnitsF) {
                                                temp = (temp - 32) * 5 / 9;
                                            }
                                            String hourlyconditions = hourData.getString("conditions"); // Conditions
                                            String icon = hourData.getString("icon"); // Icon

                                            // Create HourlyForecast object
                                            HourlyForecast hourlyForecast = new HourlyForecast();
                                            hourlyForecast.setDatetime(datetime);
                                            hourlyForecast.setDatetimeEpoch(datetimeEpoch);
                                            hourlyForecast.setTemp(temp);
                                            hourlyForecast.setConditions(hourlyconditions);
                                            hourlyForecast.setIcon(icon); // Assuming you want to store the icon name as well

                                            // Add to the list
                                            hourlyForecasts.add(hourlyForecast);
                                        }
                                    }
                                    hourlyForecastAdapter.notifyDataSetChanged();
                                }
                            }

                            JSONArray dayArray = response.getJSONArray("days");
                            if (dayArray.length() > 0){
                                JSONObject dayForecast = dayArray.getJSONObject(0); // Assuming you want the first day's data
                                JSONArray hoursArray = dayForecast.getJSONArray("hours");
                                for (int i = 0; i < hoursArray.length(); i++) {
                                    JSONObject hourData = hoursArray.getJSONObject(i);

                                    // Extract required fields
                                    String datetime = hourData.getString("datetime"); // Hour time
                                    // Epoch time
                                    double temp = hourData.getDouble("temp");
                                    if (!isUnitsF) {
                                        temp = (temp - 32) * 5 / 9;
                                    } // Temperature
                                    tempPoints.put(datetime, temp);
                                }
                                Log.d("TemperatureData", "Temperature Points: " + tempPoints.toString());
                                ChartMaker chartMaker = new ChartMaker(MainActivity.this, binding);
                                chartMaker.makeChart(tempPoints, System.currentTimeMillis());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error fetching weather data", Toast.LENGTH_SHORT).show();
                        Log.e("VolleyError", "Error: " + error.toString());
                    }
                });

        queue.add(jsonObjectRequest);
    }


    private void toggleImage() {
        if (isUnitsF) {
            binding.icon4.setImageResource(R.drawable.units_c);
        } else {
            binding.icon4.setImageResource(R.drawable.units_f);
        }
        isUnitsF = !isUnitsF;
Log.d("name:","currentlocationcn"+currentLocationCityName+"cityname:"+cityName);
        if(currentLocationCityName.equals(cityName))
        { fetchLocationAndWeatherData();}
        else
            processLocation(location);
        hourlyForecastAdapter.updateUnitsFlag(isUnitsF);

    }
    public void displayChartTemp(float time, float tempVal) {
        SimpleDateFormat sdf =
                new SimpleDateFormat("h a", Locale.US);
        Date d = new Date((long) time);
        binding.chartTemp.setText(
                String.format(Locale.getDefault(),
                        "%s, %.0f°",
                        sdf.format(d), tempVal));
        binding.chartTemp.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                runOnUiThread(() -> binding.chartTemp.setVisibility(View.GONE));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void fetchLocationAndWeatherData() {

        checkInternetAndShowAlert();
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                // Get the last known location
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                // Fetch weather data using the obtained coordinates
                                fetchWeatherData(latitude, longitude);
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error fetching location", Toast.LENGTH_SHORT).show());
            } catch (SecurityException e) {
                // Catch any unexpected SecurityException
                e.printStackTrace();
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Request permission if not already granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    private void fetchWeatherData(double latitude, double longitude) {
        checkInternetAndShowAlert();
        hourlyForecasts.clear();

// Optionally, reinitialize the list (though this is not required if you only want to clear it)

        String apiKey = "EC6MLZ4WBZNZVYDJ942YQPYMQ"; // Replace with your actual API key
        String apiUrl = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" +
                latitude + "," + longitude + "?key=" + apiKey;


        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Clear the existing list



                        try {
                            // Extract resolvedAddress from the root level of the response
                            resolvedAddress = response.getString("resolvedAddress");
                            Log.d("ResolvedAddress", resolvedAddress);

                            if (isLatLong(resolvedAddress)) {
                                cityName = reverseGeocode(MainActivity.this, resolvedAddress);
                                Log.d("CityName", "City Name: " + cityName);
                            } else {
                                // If already in "City, State, Country" format, extract the city
                                cityName = extractCityName(resolvedAddress);

                            }
                            currentLocationCityName=cityName;

                            Log.d("CityName", "City Name: " + cityName);




                            // Extract currentConditions from the response
                            JSONObject currentConditions = response.getJSONObject("currentConditions");

                            // Parse temperature and other weather details
                             temperature = currentConditions.getDouble("temp");
                            if (!isUnitsF) {
                                // Convert to Celsius if isUnitsF is false
                                temperature = (temperature - 32) * 5 / 9;
                            }
                            Log.d("WeatherApp", "Current Temperature: " + temperature);

                            // Set the background gradient based on temperature
                            ColorMaker.setColorGradient(binding.getRoot(), temperature, isUnitsF ? "F" : "C");

                            // Parse additional weather data
                             feelsLike = currentConditions.getInt("feelslike");
                            if (!isUnitsF) {
                                feelsLike = (int) ((feelsLike - 32) * 5 / 9);
                            }
                            humidity = currentConditions.getInt("humidity");
                             uvIndex = currentConditions.getInt("uvindex");

                             conditions = currentConditions.getString("conditions");
                            long currenttimeepoch = currentConditions.getLong("datetimeEpoch");
                             long currenttimeepoch1 = currenttimeepoch * 1000;
                            Date date1 = new Date(currenttimeepoch1);
                            SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEE MMM dd hh:mm a");


                            String formattedDate1 = dateFormat1.format(date1);
                            int cloudcover = currentConditions.getInt("cloudcover");
                             windDir = currentConditions.getInt("winddir");
                             windSpeed = currentConditions.getInt("windspeed");
                            int windgust = currentConditions.getInt("windgust");
                            Log.d("WindInfo", "Wind Direction: " + windDir);
                            Log.d("WindInfo", "Wind Speed: " + windSpeed);
                            Log.d("WindInfo", "Wind Gust: " + windgust);
                             visibility = currentConditions.getDouble("visibility");
                            long sunrisetimeepoch = currentConditions.getLong("sunriseEpoch");
                            long sunriseTimeEpoch = currentConditions.getLong("sunriseEpoch");
                            Log.d("heysunrise", "Sunrise Epoch Time: " + sunriseTimeEpoch);

                            Date sunriseDate = new Date(sunrisetimeepoch * 1000);

// Format the Date object to "h:mm a" (e.g., "7:00 AM")
                            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
                             formattedSunriseTime = timeFormat.format(sunriseDate);

                            long sunsetimeepoch = currentConditions.getLong("sunsetEpoch");
                            Date sunsetDate = new Date(sunsetimeepoch * 1000);

// Format the Date object to "h:mm a" (e.g., "7:00 AM")
                            SimpleDateFormat timeFormat2 = new SimpleDateFormat("h:mm a", Locale.US);
                             formattedSunsetTime = timeFormat2.format(sunsetDate);

                            Log.d("Visibility", "Current visibility: " + visibility);

                            // Update UI elements with the parsed data
                            binding.resolvedAdress.setText(getCityName(cityName)+", "+formattedDate1);
                            binding.Temperature.setText(String.format("%.1f°%s", temperature, isUnitsF ? "F" : "C"));
                            binding.feelslike.setText("Feels like: " + feelsLike + (isUnitsF ? " °F" : " °C"));

                            binding.Humidity.setText("Humidity: " + humidity + "%");
                            binding.UVindex.setText("UV Index: " + uvIndex);
                            binding.Weatherdescriptionr.setText(conditions + " (" + cloudcover + "% clouds)");
                            binding.Winddirection.setText("Winds: " + getDirection(windDir) + " at " + windSpeed + " mph gusting to " + windgust);
                            binding.Visibility.setText("Visibility: " + visibility + " miles");
                            binding.Sunrise.setText("Sunrise: "+ formattedSunriseTime );
                            binding.Sunset.setText("Sunset: "+ formattedSunsetTime );

                            // Get the appropriate weather icon
                            // Convert the weather conditions to lowercase and replace dashes with underscores
                            String iconName = currentConditions.getString("icon").replace("-", "_");
                            Log.d("icon", iconName);
// Get the resource ID of the desired icon
                            int iconID = getId(iconName, R.drawable.class);

// Set the image resource for the weather icon; use a default icon if not found
                            if (iconID != 0) {
                                binding.Weathericon.setImageResource(iconID);
                            } else {
                                // Optional: Set a default icon in case the specified icon is not found
                                binding.Weathericon.setImageResource(R.mipmap.ic_launcher); // Replace with your default icon if necessary
                            }
                            JSONArray daysArray = response.getJSONArray("days");
                            if (daysArray.length() > 0) {
                                for (int j = 0; j < 3; j++){
                                JSONObject dayForecast = daysArray.getJSONObject(j); // Assuming you want the first day's data
                                JSONArray hoursArray = dayForecast.getJSONArray("hours");

                                // List to hold hourly forecasts


                                // Loop through the hours and create HourlyForecast objects
                                for (int i = 0; i < hoursArray.length(); i++) {
                                    JSONObject hourData = hoursArray.getJSONObject(i);
                                    long datetimeEpoch = hourData.getLong("datetimeEpoch");

                                    if (datetimeEpoch > currenttimeepoch) {
                                        // Extract required fields
                                        String datetime = hourData.getString("datetime"); // Hour time
                                        // Epoch time
                                        double temp = hourData.getDouble("temp");
                                        if (!isUnitsF) {
                                            temp = (temp - 32) * 5 / 9;
                                        }
                                        String hourlyconditions = hourData.getString("conditions"); // Conditions
                                        String icon = hourData.getString("icon"); // Icon

                                        // Create HourlyForecast object
                                        HourlyForecast hourlyForecast = new HourlyForecast();
                                        hourlyForecast.setDatetime(datetime);
                                        hourlyForecast.setDatetimeEpoch(datetimeEpoch);
                                        hourlyForecast.setTemp(temp);
                                        hourlyForecast.setConditions(hourlyconditions);
                                        hourlyForecast.setIcon(icon); // Assuming you want to store the icon name as well

                                        // Add to the list
                                        hourlyForecasts.add(hourlyForecast);
                                    }
                                }
                                hourlyForecastAdapter.notifyDataSetChanged();
                            }
                        }

                            JSONArray dayArray = response.getJSONArray("days");
                            if (dayArray.length() > 0){
                                JSONObject dayForecast = dayArray.getJSONObject(0); // Assuming you want the first day's data
                                JSONArray hoursArray = dayForecast.getJSONArray("hours");
                                for (int i = 0; i < hoursArray.length(); i++) {
                                    JSONObject hourData = hoursArray.getJSONObject(i);

                                        // Extract required fields
                                        String datetime = hourData.getString("datetime"); // Hour time
                                        // Epoch time
                                    double temp = hourData.getDouble("temp");
                                    if (!isUnitsF) {
                                        temp = (temp - 32) * 5 / 9;
                                    } // Temperature
                                    tempPoints.put(datetime, temp);
                                }
                                Log.d("TemperatureData", "Temperature Points: " + tempPoints.toString());
                                ChartMaker chartMaker = new ChartMaker(MainActivity.this, binding);
                                chartMaker.makeChart(tempPoints, System.currentTimeMillis());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error fetching weather data", Toast.LENGTH_SHORT).show();
                        Log.e("VolleyError", "Error: " + error.toString());
                    }
                });

        queue.add(jsonObjectRequest);
    }


    private boolean isLatLong(String address) {
        // Check if the address matches a latitude,longitude pattern
        return address.matches("^-?\\d{1,3}\\.\\d+,\\s*-?\\d{1,3}\\.\\d+$");
    }

    private String reverseGeocode(Context context, String latLong) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String[] latLongArr = latLong.split(",");
        double latitude = Double.parseDouble(latLongArr[0].trim());
        double longitude = Double.parseDouble(latLongArr[1].trim());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                postalCode =addresses.get(0).getPostalCode();
                return addresses.get(0).getLocality(); // Get city name
            }
        } catch (IOException e) {
            Log.e("ReverseGeocodeError", "Error in reverse geocoding", e);
        }

        return null; // Return null if reverse geocoding fails
    }

    private String extractCityName(String address) {
        // Extract city name from "City, State, Country" format
        String[] parts = address.split(",");
        return parts[0].trim(); // Return the first part as city name
    }
    public static int getId(String resourceName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resourceName.replace("-", "_"));
            return idField.getInt(idField);
        } catch (Exception e) {
            return 0; // Handle resource not found
        }
    }
    private String getCityName(String resolvedAddress) {
        return resolvedAddress.split(",")[0]; // Extract city name
    }

    private String getDirection(double degrees) {
        if (degrees >= 337.5 || degrees < 22.5) return "N";
        if (degrees >= 22.5 && degrees < 67.5) return "NE";
        if (degrees >= 67.5 && degrees < 112.5) return "E";
        if (degrees >= 112.5 && degrees < 157.5) return "SE";
        if (degrees >= 157.5 && degrees < 202.5) return "S";
        if (degrees >= 202.5 && degrees < 247.5) return "SW";
        if (degrees >= 247.5 && degrees < 292.5) return "W";
        if (degrees >= 292.5 && degrees < 337.5) return "NW";
        return "X"; // Default case
    }
    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndWeatherData();
            } else {
                Toast.makeText(this, "Location permission is needed to fetch weather data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
