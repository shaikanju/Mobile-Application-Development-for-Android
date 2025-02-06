package com.anju.ctabustracker;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.anju.ctabustracker.databinding.ActivityPredictionsBinding;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PredictionsActivity extends AppCompatActivity {
    private ActivityPredictionsBinding binding;
    private List<Prediction> predictionsList;
    private RecyclerView recyclerView;
    private PredictionsAdapter predictionsAdapter;
    private static final String unityGameID = "234567";

    private static final String bannerPlacement = "Banner_Android";
    private BannerView.IListener bannerListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPredictionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        recyclerView = binding.rc1;  // Assuming you have a RecyclerView in your layout
        predictionsList = new ArrayList<>();
        bannerListener = new BannerViewListener(this);
        UnityAds.initialize(this, unityGameID, false,
                new UnityInitializationListener(this));

        // Get the data passed from StopsActivity
        String stopName = getIntent().getStringExtra("stopName");
        String routeNumber = getIntent().getStringExtra("routeNumber");
        String direction = getIntent().getStringExtra("direction");
        String routeName = getIntent().getStringExtra("routeName");
        String stpid = getIntent().getStringExtra("stopId");
        double stopLatitude = getIntent().getDoubleExtra("latitude",0.0);
        double stopLongitude = getIntent().getDoubleExtra("longitude",0.0);
        int color = getIntent().getIntExtra("color",0);
        String currentTime = getCurrentTime();
        binding.textView4.setText(stopName+" ("+direction+")\n"+currentTime);
        binding.textView4.setBackgroundColor(color);
        double luminance = ColorUtils.calculateLuminance(color); // Get luminance

        // Set text color based on luminance
        int textColor = (luminance < 0.25) ? Color.WHITE : Color.BLACK;
        binding.textView4.setTextColor(textColor);
        binding.textView3.setText("Route "+routeNumber+" - "+routeName);
        binding.imageView3.setImageResource(R.drawable.bus_icon);


        predictionsAdapter = new PredictionsAdapter(this, predictionsList, stopLatitude, stopLongitude,stopName,color);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(predictionsAdapter);
        fetchPredictions(routeNumber, stpid);
        binding.swiper.setOnRefreshListener(() -> {fetchPredictions(routeNumber, stpid);
        String currentTime1 = getCurrentTime();
            binding.textView4.setText(stopName+" ("+direction+")\n"+currentTime1);
        });

    }
    private void fetchPredictions(String routeNumber, String stpid) {
        RoutesVolley.fetchPredictions(this, routeNumber, stpid, new RoutesVolley.PredictionsCallback() {
            public void onSuccess(JSONObject response) throws JSONException {
                JSONObject bustimeResponse = response.getJSONObject("bustime-response");
                JSONArray predictionsArray = bustimeResponse.getJSONArray("prd");
                // Logs the entire JSON response
                Log.d("response", "Stops array: " + predictionsArray.toString());

                predictionsList.clear();

                for (int i = 0; i < predictionsArray.length(); i++) {
                    JSONObject predictionJson = predictionsArray.getJSONObject(i);
                    String vid = predictionJson.getString("vid");
                    String rtdir = predictionJson.getString("rtdir");
                    String des = predictionJson.getString("des");
                    String prdtime = predictionJson.getString("prdtm");
                    boolean dly = predictionJson.getBoolean("dly");
                    String duein = predictionJson.getString("prdctdn");
                    String localTime = convertToLocalTime(prdtime);

                    // Log the converted time
                    Log.d("Converted Time", "Bus " + vid + " Arrival Time: " + localTime);

                    // Add data to the list (Assuming you have a Prediction class)
                    predictionsList.add(new Prediction(vid, rtdir, des, localTime, duein, dly));

                }


                predictionsAdapter.notifyDataSetChanged();
                binding.swiper.setRefreshing(false);

            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(PredictionsActivity.this, "Error fetching stops: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                binding.swiper.setRefreshing(false);
            }

        });
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault()); // Use the device's local time zone
        return sdf.format(new Date());
    }
    private String convertToLocalTime(String prdTime) {
        try {
            // Input format from API (UTC assumed)
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyyMMdd HH:mm", Locale.getDefault());
            apiFormat.setTimeZone(TimeZone.getTimeZone("America/Chicago")); // Adjust if API uses a different timezone

            // Convert string to Date
            Date date = apiFormat.parse(prdTime);

            // Output format (local time, including seconds)
            SimpleDateFormat localFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            localFormat.setTimeZone(TimeZone.getDefault()); // Convert to device's local time zone

            return localFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid Time";
        }
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
}
