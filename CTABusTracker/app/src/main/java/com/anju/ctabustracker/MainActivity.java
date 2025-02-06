package com.anju.ctabustracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import com.anju.ctabustracker.databinding.ActivityMainBinding;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;



public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private boolean keepOn = true;
    private static final long minSplashTime = 2000;
    private long startTime;
    private List<Routes> routeList = new ArrayList<>(); // Store routes
    private List<Routes> filteredRoutes = new ArrayList<>();
    private RoutesAdapter routesAdapter;
    private static final String unityGameID = "4921141";
    private static final boolean testMode = true;
    private static final String bannerPlacement = "Banner_Android";
    private BannerView.IListener bannerListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this)
                .setKeepOnScreenCondition(
                        new SplashScreen.KeepOnScreenCondition() {
                            @Override
                            public boolean shouldKeepOnScreen() {

                                return keepOn||(System.currentTimeMillis() - startTime <= minSplashTime);
                            }
                        }
                );
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setupRecyclerView();

        startTime = System.currentTimeMillis();
        if (!isNetworkAvailable()) {
            showNetworkErrorDialog();
        } else {
            // Network is available, check location
            requestLocationPermission();
        }



        setContentView(binding.getRoot());
        // Check network connectivity before requesting location permission

        binding.imageView.setImageResource(R.drawable.bus_icon);
        binding.imageView2.setImageResource((R.drawable.info_icon));
        binding.imageView2.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

            // Set title
            builder.setTitle("Bus Tracker - CTA");

            // Set message with clickable link
            SpannableString message = new SpannableString("CTA Bus Tracker data provided by Chicago Transit Authority\n\nhttps://www.transitchicago.com/developers/bustracker/");
            Linkify.addLinks(message, Linkify.WEB_URLS);

            // Set icon (CTA logo)
            builder.setIcon(R.drawable.splash_logo);

            builder.setMessage(message);
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            // Make the link clickable inside the dialog
            ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        });



        binding.textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable editable){
                    filterRoutes(editable.toString());
                }
        });

        bannerListener = new BannerViewListener(this);
        UnityAds.initialize(this, unityGameID, false,
                new UnityInitializationListener(this));
    }




    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Show a dialog if no network is available
    private void showNetworkErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("BUS Tracker CTA")
                .setMessage("Unable to contact Bus Tracker API due to network problem.Please check your network connection.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); // Close the app after clicking OK
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Check if user denied before and show rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRationaleDialog();
            } else {
                // First-time request
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }}
//        } else {
//
//            // Permission is already granted, continue with the app
//            RoutesVolley.downloadRoutes(this);
//        }
//    }
            else {
                // Permission is already granted, check if location services are enabled
                if (!isLocationEnabled()) {
                    showLocationEnabledDialog();
                } else {
                    // Check if location can be retrieved
                    if (getLastKnownLocation() == null) {
                        // No location available (e.g., emulator with no location set)
                        showLocationErrorDialog();
                    } else {
                        RoutesVolley routesVolley = new RoutesVolley(this);
                        // Location is available, continue with the app
                        routesVolley.downloadRoutes(this);
                    }
                }
            }
        }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // Method to get the last known location
    private Location getLastKnownLocation() {
        // Check if the location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // If permission is not granted, return null or handle the case as needed
            return null;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = null;

        if (locationManager != null) {
            try {
                // Get last known location from GPS and Network providers
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation == null) {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            } catch (SecurityException e) {
                // Handle the case where permission was denied after requesting
                e.printStackTrace();
                return null;
            }
        }

        return lastKnownLocation;
    }

    private void showLocationErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("BUS Tracker CTA")
                .setMessage("Unable to determine device location. Please set location and restart the app.")
                .setPositiveButton("OK", (dialog, which) -> {
                    finish(); // Close the app if location is not available
                })
                .setCancelable(false)
                .show();
    }

    // Method to show dialog if location services are not enabled
    private void showLocationEnabledDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Services Disabled")
                .setMessage("Please enable location services to proceed with the app.")
                .setPositiveButton("Enable Location", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent); // Open location settings
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    finish(); // Close the app if location services are not enabled
                })
                .setCancelable(false)
                .show();
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.splash_logo)
                .setTitle("Fine Accuracy Needed")
                .setMessage("This app needs fine Accuracy permission in order to determine the closest stops bus to your location.It will not function properly without it. Will you allow it?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("No thanks", (dialog, which) -> showMandatoryDialog())
                .setCancelable(false)
                .show();
    }
    private void showMandatoryDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.splash_logo)
                .setTitle("Fine Accuracy Needed")
                .setMessage("This app needs fine Accuracy permission in order to determine the closest stops bus to your location.It will not function properly without it.Please start the application again and allow this permission")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Close the app
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                RoutesVolley routesVolley = new RoutesVolley(this);
                routesVolley.downloadRoutes(this);;
            } else {
                // Permission denied, show rationale
                showRationaleDialog();
            }
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


        Log.d("log","log"+Toast.LENGTH_LONG);
    }

    private void setupRecyclerView() {
        routesAdapter = new RoutesAdapter(filteredRoutes,this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(routesAdapter);
    }
    public void acceptRoutes(List<Routes> routes) {
        keepOn = false;
        Log.d(TAG, "Route Data Download Completed");

        // Update dataset and notify adapter
        routeList.clear();
        routeList.addAll(routes);
        filteredRoutes.clear();
        filteredRoutes.addAll(routeList);
        routesAdapter.notifyDataSetChanged();


    }
    private void filterRoutes(String query) {
        Log.d("onceagaib","query:"+query);
        filteredRoutes.clear();
Log.d("hey there", "i changed"+filteredRoutes.size());
        if (query.isEmpty()) {
            filteredRoutes .addAll(routeList); // If query is empty, show all routes
        } else {
            // Filter based on route number or name
            for (Routes route : routeList) {
                if (
                        route.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredRoutes.add(route);
                }
            }
            Log.d("hey there", "i changed"+filteredRoutes.size());
            Log.d("hey there", "i changed"+filteredRoutes);




        }
        routesAdapter.notifyDataSetChanged();
        // Notify adapter that the data set has changed

    }
    public void acceptFail(String msg) {
        keepOn = false;
        Log.d(TAG, "Request failed: " + msg);
        binding.textView.setText(msg); // Display the error message
          // Remove the splash screen
    }
}