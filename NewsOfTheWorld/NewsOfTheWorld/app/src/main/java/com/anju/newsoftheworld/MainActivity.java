package com.anju.newsoftheworld;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anju.newsoftheworld.databinding.ActivityMainBinding;
import com.anju.newsoftheworld.databinding.DrawerItemBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter<String> arrayAdapter;
    private Set<String> uniqueCategories = new HashSet<>();
    private final ArrayList<NewsArticle> currentArticleList = new ArrayList<>();
    private JSONObject countryMapping;
    private JSONObject languageMapping;
    private Set<String> uniqueCountries = new HashSet<>();
    private Set<String> uniqueLanguages = new HashSet<>();
    private NewsSourceAdapter newsSourceAdapter;
    HashMap<String, NewsSource> fullSourceList = new LinkedHashMap<>();
    HashMap<String, NewsSource> currentSourceList = new LinkedHashMap<>();
    ArrayList<String> namesList=new ArrayList<>() ;
    private String selectedCategory1;
    private String selectedCountry1;
    private String selectedLanguage1;
    int sourceListSize;
    private final Set<Integer> usedColors = new HashSet<>();

    private final Map<String, Integer> categoryColorMap = new HashMap<>();
//    private  ArrayList<String> fullSourceList = new ArrayList<>();ArrayList
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.parseColor("#FF0D47A1")); // Replace with your desired color
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Assuming you have a binding object, e.g., ActivityMainBinding binding
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            // Get system insets (status bar, navigation bar, etc.)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Adjust the padding of the view based on the system bars
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            // Return the insets so that the system can continue handling them
            return insets;
        });

        // Assuming you have a binding object, e.g., `ActivityMainBinding binding`


        countryMapping = loadJSONFromRaw(R.raw.country_codes);
        languageMapping = loadJSONFromRaw(R.raw.language_codes);

         sourceListSize = currentSourceList.size();
        binding.toolbar.setTitle("News of the World (" + sourceListSize + ")");
        binding.toolbar.setTitleTextColor(Color.WHITE);
        binding.toolbar.setTitleTextColor(Color.WHITE);


        Objects.requireNonNull(binding.toolbar.getOverflowIcon()).setTint(Color.WHITE);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                binding.main,         /* DrawerLayout object */
                binding.toolbar,
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

//        mDrawerToggle.getDrawerArrowDrawable().setColor(Color.WHITE);  // Make he toggle same color as other toolbar content
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));
        binding.main.addDrawerListener(mDrawerToggle);

        // Sync the toggle state
        mDrawerToggle.syncState();
        binding.leftDrawer.setOnItemClickListener((parent, view, position, id) -> {
            // Get the news source ID directly from the selected item
            String selectedSource = parent.getItemAtPosition(position).toString(); // Convert to lower case to match the API
//

            // Now, retrieve the URL or any other information from the LinkedHashMap using the selectedSourceId (the key)
            Iterator<Map.Entry<String, NewsSource>> iterator = currentSourceList.entrySet().iterator();

// Loop through the map and get the key at the desired position
            String selectedSourceId = null;
            for (int i = 0; iterator.hasNext(); i++) {
                Map.Entry<String, NewsSource> entry = iterator.next();
                if (i == position) {
                    selectedSourceId = entry.getKey(); // The ID is the key in LinkedHashMap
                    break;
                }
            }

// Log the selected ID
            if (selectedSourceId != null) {
                Log.d("Selected Source ID", "ID at position " + position + ": " + selectedSourceId);
            } else {
                Log.d("Selected Source ID", "No ID found at position " + position);
            }
            // If you need to fetch the articles, use the retrieved URL
            if (selectedSourceId != null) {
                fetchNewsArticles(selectedSourceId,selectedSource); // Pass the URL to fetch articles
            }

            // Close the drawer
            binding.main.closeDrawer(binding.cLayout);
        });
        // Load the data
        if (namesList.isEmpty()) {
           fetchNewsSources();
        }
        newsSourceAdapter = new NewsSourceAdapter(this, currentArticleList);
        binding.viewPager2.setAdapter(newsSourceAdapter);

    }
    private void updateToolbarTitle() {
        int sourceListSize = currentSourceList.size();
        binding.toolbar.setTitle("News of the World (" + sourceListSize + ")");
        binding.toolbar.setTitleTextColor(Color.WHITE);
    }
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        Log.d("Network Status", "Connected: " + isConnected); // Log the connection status
        return isConnected;
    }

    public void fetchNewsArticles(String source,String Name) {
        currentArticleList.clear();
        // Update the URL dynamically based on the selected news source
        String url = "https://newsapi.org/v2/top-headlines?sources=" + source;
        Log.d("Volley", "Request being sent: " + url);


        // Make the Volley request
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("Response", "Full Response: " + response.toString());
                        // Handle the response and parse the articles
                        JSONArray articlesArray = response.getJSONArray("articles");

                        int limit = Math.min(articlesArray.length(), 10);
                        // Loop through each article in the JSON response
                        for (int i = 0; i <limit; i++) {
                            JSONObject articleObject = articlesArray.getJSONObject(i);

                            // Extract the details for each article
                            String author = articleObject.getString("author");
                            String title = articleObject.getString("title");
                            String description = articleObject.getString("description");
                            String url2 = articleObject.getString("url");
                            Log.d("imageurl",url2);
                            String imageUrl = articleObject.getString("urlToImage");
                            String publishedAt = articleObject.getString("publishedAt");

                            // Extract the source object and get its details


                            // Create a NewsArticle object with the extracted data
                            NewsArticle article = new NewsArticle(author, title, description, url2, imageUrl, publishedAt,source);
                            currentArticleList.add(article);


                        }// Update the UI with the fetched articles
                        updateUIWithArticles(currentArticleList,Name);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Handle error if response format is not as expected
                        updateUIWithArticles(null,null);
                    }
                },
                error -> {
                    // Handle error response
                    updateUIWithArticles(null,null);
                    Log.e("MainActivity", "Error fetching news articles: ", error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Add the necessary headers for the request
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "News-App");
                headers.put("X-Api-Key", "09688d2f5d434b43bdb940055ff177e7"); // Use your actual API key
                return headers;
            }
        };

        // Add the request to the Volley request queue
        queue.add(jsonObjectRequest);
    }

    public void updateUIWithArticles(ArrayList<NewsArticle> articles,String Name) {
        binding.viewPager2.setBackground(null);
        if (articles != null) {

            newsSourceAdapter.notifyItemRangeChanged(0, currentArticleList.size());
            binding.viewPager2.setCurrentItem(0);
            Log.d("afterselection", "currentArticleList contains " + currentArticleList.size() + " items during initialization.");
            binding.main.closeDrawer(binding.cLayout);
            binding.toolbar.setTitle(Name);
            // Set the new list of articles to your adapter and notify the adapter to refresh the view
            // Assuming you use RecyclerView

        } else {
            Toast.makeText(this, "Failed to fetch news articles.", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchNewsSources() {
        if (!isConnected()) {
            Log.e("Network Error", "No internet connection");
            Toast.makeText(this, "No internet connection. Please check your network and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://newsapi.org/v2/sources";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("URL", "Fetching from: " + url);


                        // Parse the JSON response
                        JSONArray sources = response.getJSONArray("sources");

                        // Clear the existing lists before adding new data
                        fullSourceList.clear();
                        currentSourceList.clear();

                        for (int i = 0; i < sources.length(); i++) {
                            JSONObject source = sources.getJSONObject(i);

                            // Extract relevant fields
                            String id = source.getString("id");
                            String name = source.getString("name");
                            String category = source.getString("category");
                            String languageCode = source.getString("language");
                            String countryCode = source.getString("country");

                            // Decode country and language codes
                            String country = resolveMapping(countryCode, countryMapping, "countries");


                            String language = resolveMapping(languageCode, languageMapping, "languages");

                            // Create a NewsSource object
                            NewsSource newsSource = new NewsSource(id, name, category, language, country);

                            // Add to the full list
//                            fullSourceList.add(newsSource.getName());
                            fullSourceList.put(id,newsSource);

                            uniqueCategories.add(category);
                            uniqueCountries.add(country);
                            uniqueLanguages.add(language);
                        }

                        invalidateOptionsMenu();
                        updateDrawerWithSources(fullSourceList);

                    } catch (JSONException e) {
                        Log.e("JSON Parsing", "Error parsing JSON response: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Handle errors
                    Log.e("NewsAPI", "Error fetching sources: " + error.getMessage());
                    Log.e("NewsAPI", "Error details: " + error.toString());
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "News-App");
                headers.put("X-Api-Key", "09688d2f5d434b43bdb940055ff177e7");  // Use your actual API key
                return headers;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

private void updateDrawerWithSources(HashMap<String, NewsSource> sourceNames) {
    // Clear the current data in the adapter
//   currentSourceList.clear();

    // Add the unique source names to the list
    if(currentSourceList.isEmpty())
    {currentSourceList.putAll(sourceNames);}
else
    // Prepare the names list and colors
    namesList.clear(); // Clear the list to start fresh
    ArrayList<Integer> colorList = new ArrayList<>(); // List to store colors

    for (Map.Entry<String, NewsSource> entry : currentSourceList.entrySet()) {
        NewsSource newsSource = entry.getValue();
        String name = newsSource.getName();
        String category = newsSource.getCategory();

        // Get the category color from the hashmap or generate a new one
        int color = getCategoryColor(category); // Method to fetch the color based on category

        // Add the name and color to the lists
        namesList.add(name);
        colorList.add(color);
    }

    // Set the adapter
    arrayAdapter = new ArrayAdapter<String>(this, R.layout.drawer_item, namesList) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Use ViewBinding to inflate the layout and access views safely
            if (convertView == null) {
                // Inflate the layout with view binding
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_item, parent, false);
            }

            // Use view binding to get the TextView (replace with your actual view binding reference)
            DrawerItemBinding itemBinding = DrawerItemBinding.bind(convertView); // Assuming `drawer_item.xml` is bound to `DrawerItemBinding`

            // Get the name and color for this position
            String name = namesList.get(position);
            int color = colorList.get(position);

            // Set the text and color dynamically using ViewBinding
            itemBinding.drawerItemText.setText(name); // Assuming drawer_item_text is the TextView in your layout
            itemBinding.drawerItemText.setTextColor(color); // Set the color dynamically

            return convertView;
        }
    };

    // Attach the adapter to the drawer using view binding
    binding.leftDrawer.setAdapter(arrayAdapter);  // Assuming 'leftDrawer' is your ListView from the binding
    arrayAdapter.notifyDataSetChanged();
    updateToolbarTitle();

}



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("MainActivity", "onCreateOptionsMenu is running");
        Log.d("MainActivity", "Unique Categories: " + uniqueCategories.size());


        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        // Find the "Topics" menu item
        MenuItem topicsMenuItem = menu.findItem(R.id.action_topics);

        // Get the submenu associated with the "Topics" menu item
        SubMenu subMenu = topicsMenuItem.getSubMenu();

        MenuItem countriesMenuItem = menu.findItem(R.id.action_countries);
        SubMenu countryMenu = countriesMenuItem.getSubMenu();
// Use an iterator to loop through the keys of the JSONObject for languages
        MenuItem languagesMenuItem = menu.findItem(R.id.action_languages);
        SubMenu languageMenu = languagesMenuItem.getSubMenu();

        MenuItem clearAll = menu.findItem(R.id.action_clear_all);
        clearAll.setOnMenuItemClickListener(item -> {
            // When "Clear All" is clicked, reset the current source list to the full source list
            currentSourceList.clear();  // Clear the existing data
            currentSourceList.putAll(fullSourceList);  // Add all entries from the fullSourceList
            // Optionally, log to verify the reset
            Log.d("ClearAll", "currentSourceList reset to fullSourceList");
            updateDrawerWithSources(currentSourceList);// Notify the adapter to refresh the UI
            return true;
        });

        MenuItem allMenuItem = subMenu.add("All");
        allMenuItem.setOnMenuItemClickListener(item -> {
            // Handle "All" click - display all categories
            updateCurrentSourceList("All", null, null);  // No category filter, show everything
            return true;
        });
        MenuItem allMenuItem2 = countryMenu.add("All");
        allMenuItem2.setOnMenuItemClickListener(item -> {
            // Handle "All" click - display all categories
            updateCurrentSourceList(null, "All", null);  // No category filter, show everything
            return true;
        });
        MenuItem allMenuItem3 = languageMenu.add("All");
        allMenuItem3.setOnMenuItemClickListener(item -> {
            // Handle "All" click - display all categories
            updateCurrentSourceList(null, null, "All");  // No category filter, show everything
            return true;
        });


        // Add unique categories to the submenu
        for (final String category : uniqueCategories) {
            int color = getCategoryColor(category);
            MenuItem menuItem = subMenu.add(category);
            menuItem.setOnMenuItemClickListener(item -> {

                updateCurrentSourceList(category, null, null);
                return true;
            });

            SpannableString coloredTitle = new SpannableString(menuItem.getTitle());
            coloredTitle.setSpan(new ForegroundColorSpan(color), 0, coloredTitle.length(), 0);
            menuItem.setTitle(coloredTitle);
        }

        for (final String category : uniqueCountries) {
            countryMenu.add(category).setOnMenuItemClickListener(item -> {
                // Handle submenu item clicks here
                updateCurrentSourceList(null,category,null);
                return true;
            });
        }
        for (final String category : uniqueLanguages) {
            languageMenu.add(category).setOnMenuItemClickListener(item -> {
                // Handle submenu item clicks here
                updateCurrentSourceList(null,null,category);

                return true;
            });
        }



        return true;
    }
    private int getCategoryColor(String category) {
        // Check if the category already has a color assigned
        if (categoryColorMap.containsKey(category)) {
            return categoryColorMap.get(category);
        }

        // Generate a new unique color
        int newColor;
        do {
            Random random = new Random();
            newColor = Color.rgb(
                    300 + random.nextInt(206), // Red (50-255)
                    300+ random.nextInt(206), // Green (50-255)
                    300 + random.nextInt(206)  // Blue (50-255)
            );
        } while (usedColors.contains(newColor)); // Keep generating until it's unique

        // Store the new color
        usedColors.add(newColor);
        categoryColorMap.put(category, newColor);

        return newColor;
    }
    public void updateCurrentSourceList(String selectedCategory, String selectedCountry, String selectedLanguage) {
        // Reset currentSourceList to fullSourceList

selectedCategory1=selectedCategory;
        selectedCountry1=selectedCountry;
        selectedLanguage1=selectedLanguage;

        // Apply filters based on category, country, and language (if not empty or null)
        if (selectedCategory != null && !selectedCategory.isEmpty()) {

            if(selectedCategory=="All")
            {
                currentSourceList.clear();

// Iterate through the full source list (assuming fullSourceList contains all sources)
                for (Map.Entry<String, NewsSource> entry : fullSourceList.entrySet()) {
                    NewsSource newsSource = entry.getValue();

                    // Check if the category of the current news source is in the uniqueCategories set
                    if (uniqueCategories.contains(newsSource.getCategory())) {
                        // Add this news source to currentSourceList if the category matches
                        currentSourceList.put(entry.getKey(), newsSource);
                    }
                }

// Log the updated currentSourceList for verification

            }
            else
            {  currentSourceList.entrySet().removeIf(entry -> !entry.getValue().getCategory().equalsIgnoreCase(selectedCategory));}
        }

        if (selectedCountry != null && !selectedCountry.isEmpty()) {
            if(selectedCountry=="All")
            {
                currentSourceList.clear();

// Iterate through the full source list (assuming fullSourceList contains all sources)
                for (Map.Entry<String, NewsSource> entry : fullSourceList.entrySet()) {
                    NewsSource newsSource = entry.getValue();

                    // Check if the category of the current news source is in the uniqueCategories set
                    if (uniqueCountries.contains(newsSource.getCountry())) {
                        // Add this news source to currentSourceList if the category matches
                        currentSourceList.put(entry.getKey(), newsSource);
                    }
                }
                for (Map.Entry<String, NewsSource> entry : currentSourceList.entrySet()) {
                    Log.d("CurrentSourceList", "Key: " + entry.getKey() + ", NewsSource: " + entry.getValue());
                }

// Log the updated currentSourceList for verification

            }
else
            { currentSourceList.entrySet().removeIf(entry -> !entry.getValue().getCountry().equalsIgnoreCase(selectedCountry));}
        }

        if (selectedLanguage != null && !selectedLanguage.isEmpty()) {
            if(selectedLanguage=="All")
            {
                currentSourceList.clear();

// Iterate through the full source list (assuming fullSourceList contains all sources)
                for (Map.Entry<String, NewsSource> entry : fullSourceList.entrySet()) {
                    NewsSource newsSource = entry.getValue();

                    // Check if the category of the current news source is in the uniqueCategories set
                    if (uniqueLanguages.contains(newsSource.getLanguage())) {
                        // Add this news source to currentSourceList if the category matches
                        currentSourceList.put(entry.getKey(), newsSource);
                    }
                }
                for (Map.Entry<String, NewsSource> entry : currentSourceList.entrySet()) {
                    Log.d("CurrentSourceList", "Key: " + entry.getKey() + ", NewsSource: " + entry.getValue());
                }
// Log the updated currentSourceList for verification

            }
            else{
            currentSourceList.entrySet().removeIf(entry -> !entry.getValue().getLanguage().equalsIgnoreCase(selectedLanguage));}
        }

        // Update the UI with the filtered list (e.g., update the drawer)
       updateDrawerWithSources(currentSourceList);

    }


    private JSONObject loadJSONFromRaw(int resourceId) {
        StringBuilder jsonString = new StringBuilder();
        try {
            InputStream inputStream = getResources().openRawResource(resourceId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();
            return new JSONObject(jsonString.toString());
        } catch (Exception e) {
            Log.e("JSON Parsing", "Error reading local JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String resolveMapping(String code, JSONObject mapping, String key) {
        try {
            if (mapping != null && mapping.has(key)) {
                JSONArray items = mapping.getJSONArray(key);
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    if (item.getString("code").equalsIgnoreCase(code)) {
                        return item.getString("name");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Fallback to returning the code if no match is found
        return code;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the selected category, country, and language (if applicable)
        outState.putString("selectedCategory1", selectedCategory1);
        outState.putString("selectedCountry1", selectedCountry1);
        outState.putString("selectedLanguage1", selectedLanguage1);

        // Save the current source list and articles
        outState.putSerializable("currentSourceList", currentSourceList);
        outState.putSerializable("currentArticleList", new ArrayList<>(currentArticleList));

        Log.d("MainActivity", "State saved in onSaveInstanceState.");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the selected category, country, and language
        selectedCategory1 = savedInstanceState.getString("selectedCategory1");
        selectedCountry1 = savedInstanceState.getString("selectedCountry1");
        selectedLanguage1 = savedInstanceState.getString("selectedLanguage1");

        // Restore the current source list and articles
        currentSourceList = (HashMap<String, NewsSource>) savedInstanceState.getSerializable("currentSourceList");
        currentArticleList.clear();
        currentArticleList.addAll((ArrayList<NewsArticle>) savedInstanceState.getSerializable("currentArticleList"));

        // Update the UI with the restored data
        updateDrawerWithSources(currentSourceList);
        newsSourceAdapter.notifyDataSetChanged();
        updateToolbarTitle();

        Log.d("MainActivity", "State restored in onRestoreInstanceState.");
    }
}