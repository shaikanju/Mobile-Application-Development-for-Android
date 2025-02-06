package com.anju.ctabustracker;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RoutesVolley {
    private Context context;

    private static final String PREFS_NAME = "RouteCache";
    private static final String ROUTE_DATA_KEY = "cached_routes";
    private static final String ROUTE_TIMESTAMP_KEY = "route_timestamp";
    private static final String DREFS_NAME = "DirectionsCache";
    private static final String DIRECTION_DATA_KEY = "cached_directions";
    private static final String DIRECTION_TIMESTAMP_KEY = "direction_timestamp";
    private static final String SREFS_NAME = "StopsCache";
    private static final String STOP_DATA_KEY = "cached_stops";
    private static final String STOP_TIMESTAMP_KEY = "stop_timestamp";
    public RoutesVolley(Context context) {
        this.context = context;
    }
    private static void cacheRoutes(String routeData,Context context) {

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ROUTE_DATA_KEY, routeData);
        editor.putLong(ROUTE_TIMESTAMP_KEY, System.currentTimeMillis()); // Save current timestamp
        editor.apply();
    }
    private static void cacheDirections(List<String> directionsList, String routeNumber, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(DREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Convert List<String> to a JSON string
        JSONArray jsonArray = new JSONArray(directionsList);
        String directionData = jsonArray.toString();  // Convert the List to a JSON string

        // Store the directions with the routeNumber as part of the key
        editor.putString(routeNumber + DIRECTION_DATA_KEY, directionData);
        editor.putLong(routeNumber + DIRECTION_TIMESTAMP_KEY, System.currentTimeMillis()); // Save current timestamp
        editor.apply();
    }


    private  static String getCachedRoutes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long cachedTime = prefs.getLong(ROUTE_TIMESTAMP_KEY, 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - cachedTime < 86400000) { // 24 hours in milliseconds
            return prefs.getString(ROUTE_DATA_KEY, null);
        } else {
            // Cache expired, remove data
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(ROUTE_DATA_KEY);
            editor.remove(ROUTE_TIMESTAMP_KEY);
            editor.apply();
        }
        return null;
    }
    private static List<String> getCachedDirections(String routeNumber, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(DREFS_NAME, MODE_PRIVATE);
        long cachedTime = prefs.getLong(routeNumber + DIRECTION_TIMESTAMP_KEY, 0);
        long currentTime = System.currentTimeMillis();

        // Check if the cache is still valid (within 24 hours)
        if (currentTime - cachedTime < 86400000) { // 24 hours in milliseconds
            String directionData = prefs.getString(routeNumber + DIRECTION_DATA_KEY, null);

            if (directionData != null) {
                try {
                    // Convert the JSON string back to a List<String>
                    JSONArray jsonArray = new JSONArray(directionData);
                    List<String> directionsList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        directionsList.add(jsonArray.getString(i));
                    }
                    return directionsList;
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing cached directions JSON", e);
                }
            }
        } else {
            // Cache expired, remove data for the specific route
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(routeNumber + DIRECTION_TIMESTAMP_KEY);
            editor.remove(routeNumber + DIRECTION_DATA_KEY);  // Remove specific route data
            editor.apply();
        }
        return null;
    }

    private static void cacheStops(JSONObject stopData, String routeNumber, String direction,Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Convert the JSONObject to a string and store it
        String stopDataString = stopData.toString();  // Converts JSONObject to String
        editor.putString(routeNumber+direction+STOP_DATA_KEY, stopDataString);
        editor.putLong(routeNumber+direction+STOP_TIMESTAMP_KEY, System.currentTimeMillis()); // Save current timestamp
        editor.apply();
    }

    private static JSONObject getCachedStops(String routeNumber, String direction,Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SREFS_NAME, MODE_PRIVATE);
        long cachedTime = prefs.getLong(routeNumber+direction+STOP_TIMESTAMP_KEY, 0);
        long currentTime = System.currentTimeMillis();

        // Check if the cache is still valid (within 24 hours)
        if (currentTime - cachedTime < 86400000) { // 24 hours in milliseconds
            String stopDataString = prefs.getString(routeNumber + direction +STOP_DATA_KEY, null);

            if (stopDataString != null) {
                try {

                    return new JSONObject(stopDataString);
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing cached directions JSON", e);
                }
            }
        } else {
            // Cache expired, remove data for the specific route
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(routeNumber + STOP_TIMESTAMP_KEY);
            editor.remove(routeNumber + STOP_DATA_KEY);  // Remove specific route data
            editor.apply();
        }
        return null;
    }





    private static final String vehiclesUrl =
            "https://www.ctabustracker.com/bustime/api/v2/getroutes";
    private static final String TAG = "RoutesVolley";
    public interface DirectionsCallback {
        void onSuccess(List<String> directions);
        void onError(VolleyError error);
    }
    public interface StopsCallback {
        void onSuccess(JSONObject response) throws JSONException;
        void onError(Exception error);
    }
    public interface PredictionsCallback {
        void onSuccess(JSONObject response) throws JSONException;
        void onError(Exception error);
    }
    public interface VehiclesCallback {
        void onSuccess(JSONObject response) throws JSONException;
        void onError(Exception error);
    }

    public static void downloadRoutes(MainActivity mainActivityIn) {
        String cachedRoutes = getCachedRoutes(mainActivityIn);

        if (cachedRoutes != null) {
            try{
            Log.d("Routes", "Using cached route data");
            Log.d("response","response"+cachedRoutes.toString());
            handleSuccess(cachedRoutes,mainActivityIn);}
            catch (JSONException e) { // Catch and handle JSON parsing errors
                Log.e("Routes", "Error parsing cached route data", e);
            }
            // Use cachedRoutes (convert JSON string to object if needed)
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(mainActivityIn);

        Uri.Builder buildURL = Uri.parse(vehiclesUrl).buildUpon();
        buildURL.appendQueryParameter("key", "cxWRbqrwX99a2RUK9RJrCNkYN");
        buildURL.appendQueryParameter("format", "json");
        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONObject> listener = response -> {
            try {
                Log.d("response","response"+response.toString());
                cacheRoutes(response.toString(),mainActivityIn);
                handleSuccess(response.toString(), mainActivityIn);
            } catch (JSONException e) {
                Log.d(TAG, "downloadRoutes: " + e.getMessage());
            }
        };

        Response.ErrorListener error = error1 -> handleFail(error1, mainActivityIn);

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }


    private static void handleSuccess(String responseText,
                                      MainActivity mainActivity) throws JSONException {
        JSONObject response = new JSONObject(responseText);

        JSONObject jsonObject = response.getJSONObject("bustime-response");
        JSONArray routes = jsonObject.getJSONArray("routes");
        List<Routes> routesList = new ArrayList<>();
        for (int i = 0; i < routes.length(); i++) {
            JSONObject route = routes.getJSONObject(i);
            String rNum = route.getString("rt");
            String rName = route.getString("rtnm");
            String rColor = route.getString("rtclr");
            routesList.add(new Routes(rNum, rName, rColor));


            // Here I would make an object to example purposes I am not.
        }


        mainActivity.runOnUiThread(() -> mainActivity.acceptRoutes(routesList));
    }

    private static void handleFail(VolleyError ve, MainActivity mainActivity) {
        Log.d(TAG, "handleFail: " + ve.getMessage());
        mainActivity.runOnUiThread(() -> mainActivity.acceptFail(ve.getClass().getSimpleName()));
    }

    public static void fetchRouteDirections(Context context, String routeNumber, DirectionsCallback callback) {
        List<String> cachedDirections = getCachedDirections(routeNumber,context);

        if (cachedDirections != null) {

                Log.d("Directions", "Using cached direction data");
                Log.d("response","response"+cachedDirections.toString());
                callback.onSuccess(cachedDirections);

            // Use cachedRoutes (convert JSON string to object if needed)
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(context);

        Uri.Builder buildURL = Uri.parse("https://www.ctabustracker.com/bustime/api/v2/getdirections").buildUpon();
        buildURL.appendQueryParameter("key", "cxWRbqrwX99a2RUK9RJrCNkYN");
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("rt", routeNumber);
        String urlToUse = buildURL.build().toString();

        Log.d(TAG, "Fetching directions for route: " + routeNumber);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlToUse, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Directions Response: " + response.toString());
                        try {
                            List<String> directionsList = new ArrayList<>();
                            JSONArray directionsArray = response.getJSONObject("bustime-response").getJSONArray("directions");

                            for (int i = 0; i < directionsArray.length(); i++) {
                                JSONObject direction = directionsArray.getJSONObject(i);
                                directionsList.add(direction.getString("dir"));
                            }
                            cacheDirections(directionsList,routeNumber,context);
                            callback.onSuccess(directionsList);

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing directions JSON: " + e.getMessage());
                            callback.onError(new VolleyError("Failed to parse directions"));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching directions: " + error.getMessage());
                        callback.onError(error);
                    }
                });

        queue.add(request);
    }

    public static void fetchRouteStops(Context context, String routeNumber, String direction, StopsCallback callback) {
        JSONObject cachedStops = getCachedStops(routeNumber,direction,context);

        if (cachedStops != null) {
            try{
                Log.d("stops", "Using cached stop data");
                Log.d("stops","response"+cachedStops.toString());
                callback.onSuccess(cachedStops);}
            catch (JSONException e) { // Catch and handle JSON parsing errors
                Log.e("Routes", "Error parsing cached route data", e);
            }
            // Use cachedRoutes (convert JSON string to object if needed)
            return;
        }


        RequestQueue queue = Volley.newRequestQueue(context);

        // Build the URL to fetch the stops
        Uri.Builder buildURL = Uri.parse("https://www.ctabustracker.com/bustime/api/v2/getstops").buildUpon();
        buildURL.appendQueryParameter("key", "cxWRbqrwX99a2RUK9RJrCNkYN");
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("rt", routeNumber);
        buildURL.appendQueryParameter("dir", direction);  // Pass direction to the request
        String urlToUse = buildURL.build().toString();

        // Create a listener for the response
        Response.Listener<JSONObject> listener = response -> {
            try {
                Log.d("stops","stops"+response);
                cacheStops(response,routeNumber,direction,context);
                // Pass the response to the callback
                callback.onSuccess(response);
            } catch (JSONException e) {
                Log.d(TAG, "fetchRouteStops: " + e.getMessage());
                callback.onError(e);
            }
        };

        // Create an error listener
        Response.ErrorListener error = error1 -> {
            Log.d(TAG, "fetchRouteStops Error: " + error1.getMessage());
            callback.onError(error1);
        };

        // Request the stops data from the API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlToUse,
                null, listener, error);

        // Add the request to the queue
        queue.add(jsonObjectRequest);
    }
    public static void fetchPredictions(Context context, String routeNumber, String stpid, PredictionsCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        // Build the URL to fetch the stops
        Uri.Builder buildURL = Uri.parse("https://www.ctabustracker.com/bustime/api/v2/getpredictions").buildUpon();
        buildURL.appendQueryParameter("key", "cxWRbqrwX99a2RUK9RJrCNkYN");
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("rt", routeNumber);
        buildURL.appendQueryParameter("stpid", stpid);  // Pass direction to the request
        String urlToUse = buildURL.build().toString();

        // Create a listener for the response
        Response.Listener<JSONObject> listener = response -> {
            try {
                // Pass the response to the callback
                callback.onSuccess(response);
            } catch (JSONException e) {
                Log.d(TAG, "fetchPredictions: " + e.getMessage());
                callback.onError(e);
            }
        };

        // Create an error listener
        Response.ErrorListener error = error1 -> {
            Log.d(TAG, "fetchPredictions Error: " + error1.getMessage());
            callback.onError(error1);
        };

        // Request the stops data from the API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlToUse,
                null, listener, error);

        // Add the request to the queue
        queue.add(jsonObjectRequest);
    }
    public static void fetchVehicles(Context context, String vid, VehiclesCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        // Build the URL to fetch the stops
        Uri.Builder buildURL = Uri.parse("https://www.ctabustracker.com/bustime/api/v2/getvehicles").buildUpon();
        buildURL.appendQueryParameter("key", "cxWRbqrwX99a2RUK9RJrCNkYN");
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("vid", vid);
        // Pass direction to the request
        String urlToUse = buildURL.build().toString();

        // Create a listener for the response
        Response.Listener<JSONObject> listener = response -> {
            try {
                // Pass the response to the callback
                callback.onSuccess(response);
            } catch (JSONException e) {
                Log.d(TAG, "fetchPredictions: " + e.getMessage());
                callback.onError(e);
            }
        };

        // Create an error listener
        Response.ErrorListener error = error1 -> {
            Log.d(TAG, "fetchPredictions Error: " + error1.getMessage());
            callback.onError(error1);
        };

        // Request the stops data from the API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlToUse,
                null, listener, error);

        // Add the request to the queue
        queue.add(jsonObjectRequest);
    }


}

