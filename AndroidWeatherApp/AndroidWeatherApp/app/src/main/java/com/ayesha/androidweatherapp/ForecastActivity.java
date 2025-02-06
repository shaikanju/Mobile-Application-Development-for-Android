package com.ayesha.androidweatherapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ayesha.androidweatherapp.databinding.ForecastActivityBinding;
import com.android.volley.VolleyError;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ForecastActivity extends AppCompatActivity {

    private ForecastActivityBinding binding;  // Declare binding object
    private RecyclerView recyclerView;
    private ForecastAdapter adapter;
    private List<ForecastDayItem> forecastList;
    private String address;
    private String cityName;
    private boolean isUnitsF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the binding layout
        binding = ForecastActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());  // Set the root view using binding
        recyclerView = binding.recyclerView;
        forecastList = new ArrayList<>();
        adapter = new ForecastAdapter(forecastList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        cityName = getIntent().getStringExtra("cityName");
        binding.textView.setText(cityName +" 15-Day Forecast");



        // Retrieve the resolvedAddress passed from MainActivity
        String resolvedAddress = getIntent().getStringExtra("resolvedAddress");
        // Retrieve the isUnitsF flag passed from MainActivity
         isUnitsF = getIntent().getBooleanExtra("isUnitsF", true);  // Default to true (Fahrenheit)
        adapter.updateUnitsFlag(isUnitsF);


        // Check if resolvedAddress is null or not
        if (resolvedAddress != null) {
            Log.d("ForecastActivity", "Resolved Address: " + resolvedAddress);

            // Construct the API URL dynamically based on the type of address
            String apiUrl = getApiUrl(resolvedAddress);

            // Fetch the weather data using the constructed URL
            fetchWeatherData(apiUrl);
        }
    }

    private boolean isLatLong(String address) {
        // Check if the address matches a latitude,longitude pattern
        return address.matches("^-?\\d{1,3}\\.\\d+,\\s*-?\\d{1,3}\\.\\d+$");
    }

    private String getApiUrl(String resolvedAddress) {
        // If the address is a latitude/longitude pair, construct the API URL accordingly
        if (isLatLong(resolvedAddress)) {
            String[] parts = resolvedAddress.split(",");
            double latitude = Double.parseDouble(parts[0].trim());
            double longitude = Double.parseDouble(parts[1].trim());
            return "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" +
                    latitude + "," + longitude + "?key=EC6MLZ4WBZNZVYDJ942YQPYMQ"; // Replace with your API key
        } else {
            // If the address is a city, state, country, construct the API URL accordingly
            String[] addressParts = resolvedAddress.split(",");
            String cityState = addressParts[0].trim() + "," + addressParts[1].trim();
            return "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" +
                    cityState + "?key=EC6MLZ4WBZNZVYDJ942YQPYMQ"; // Replace with your API key
        }
    }

    private void fetchWeatherData(String apiUrl) {
        // Example: Fetch forecast data using the dynamic API URL
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Parse the response and set the data to forecast day items
                        Log.d("ForecastData", response.toString());
                        // Example of handling the response to populate your forecast data
                        try {
                            JSONArray forecastDays = response.getJSONArray("days");

                            for (int i = 0; i < forecastDays.length(); i++) {
                                JSONObject day = forecastDays.getJSONObject(i);
                                ForecastDayItem forecastDay = new ForecastDayItem();
                                forecastDay.setDatetimeEpoch(day.getLong("datetimeEpoch"));
                                double temp = day.getDouble("temp");
                                if (!isUnitsF) {
                                    // Convert Fahrenheit to Celsius
                                    temp = (temp - 32) * 5 / 9;
                                }
                                forecastDay.setTemp(temp);
                                forecastDay.setDesc(day.getString("description"));
                                double tempMax = day.getDouble("tempmax");
                                if (!isUnitsF) {
                                    tempMax = (tempMax - 32) * 5 / 9; // Convert if needed
                                }
                                forecastDay.setTempMax(tempMax);
                                double tempMin = day.getDouble("tempmin");
                                if (!isUnitsF) {
                                    tempMin = (tempMin - 32) * 5 / 9; // Convert if needed
                                }
                                forecastDay.setTempMin(tempMin);

                                forecastDay.setPrecipProb(day.getInt("precipprob"));
                                forecastDay.setUvIndex(day.getInt("uvindex"));
                                forecastDay.setIcon(day.getString("icon"));
                                JSONArray hoursArray = day.getJSONArray("hours");
                                for (int j = 0; j < hoursArray.length(); j++) {
                                    JSONObject hourData = hoursArray.getJSONObject(j);
                                    String datetime = hourData.getString("datetime"); // Extract datetime

                                    // Set the temperature based on specific times
                                    double hourlyTemp = hourData.getDouble("temp");
                                    if (!isUnitsF) {
                                        // Convert hourly temp if necessary
                                        hourlyTemp = (hourlyTemp - 32) * 5 / 9;  // Convert Fahrenheit to Celsius
                                    }

                                    if (datetime.equals("08:00:00")) {
                                        forecastDay.setMorningTemp(hourlyTemp); // Set morning temperature
                                        Log.d("ForecastActivity", "Morning time: " + datetime + ", Temp: " + hourlyTemp);
                                    } else if (datetime.equals("13:00:00")) {
                                        forecastDay.setAfternoonTemp(hourlyTemp); // Set afternoon temperature
                                        Log.d("ForecastActivity", "Afternoon time: " + datetime + ", Temp: " + hourlyTemp);
                                    } else if (datetime.equals("17:00:00")) {
                                        forecastDay.setEveningTemp(hourlyTemp); // Set evening temperature
                                        Log.d("ForecastActivity", "Evening time: " + datetime + ", Temp: " + hourlyTemp);
                                    } else if (datetime.equals("23:00:00")) {
                                        forecastDay.setNightTemp(hourlyTemp); // Set night temperature
                                        Log.d("ForecastActivity", "Night time: " + datetime + ", Temp: " + hourlyTemp);
                                    }
//                                    if (datetime.equals("08:00:00")) {
//                                        forecastDay.setMorningTemp(hourData.getDouble("temp")); // Set morning temperature
//                                        Log.d("ForecastActivity", "Morning time: " + datetime + ", Temp: " + hourData.getDouble("temp"));
//                                    } else if (datetime.equals("13:00:00")) {
//                                        forecastDay.setAfternoonTemp(hourData.getDouble("temp")); // Set afternoon temperature
//                                        Log.d("ForecastActivity", "Afternoon time: " + datetime + ", Temp: " + hourData.getDouble("temp"));
//                                    } else if (datetime.equals("17:00:00")) {
//                                        forecastDay.setEveningTemp(hourData.getDouble("temp")); // Set evening temperature
//                                        Log.d("ForecastActivity", "Evening time: " + datetime + ", Temp: " + hourData.getDouble("temp"));
//                                    } else if (datetime.equals("23:00:00")) {
//                                        forecastDay.setNightTemp(hourData.getDouble("temp")); // Set night temperature
//                                        Log.d("ForecastActivity", "Night time: " + datetime + ", Temp: " + hourData.getDouble("temp"));
//                                    }
                                }


                                // Add more fields as needed
                                forecastList.add(forecastDay);

                            }
                            adapter.notifyDataSetChanged();
                            // You can now display or store the forecastList as needed
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ForecastActivity.this, "Error parsing forecast data", Toast.LENGTH_SHORT).show();
                            Log.e("ForecastActivity", "Error parsing forecast data", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ForecastActivity.this, "Error fetching forecast data", Toast.LENGTH_SHORT).show();
                        Log.e("VolleyError", "Error: " + error.toString());
                    }
                });

        queue.add(jsonObjectRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure to clear the binding reference to avoid memory leaks
        binding = null;
    }
}

