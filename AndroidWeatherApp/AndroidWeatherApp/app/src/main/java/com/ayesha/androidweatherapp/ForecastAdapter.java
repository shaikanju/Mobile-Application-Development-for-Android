package com.ayesha.androidweatherapp;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ayesha.androidweatherapp.databinding.ForecastActivityItemBinding;
import androidx.recyclerview.widget.RecyclerView;


import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {


    private List<ForecastDayItem> forecastList;
    private boolean isUnitsF;

    public ForecastAdapter(List<ForecastDayItem> forecastList) {
        this.forecastList = forecastList;

    }
    public void updateUnitsFlag(boolean isUnitsF) {
        this.isUnitsF = isUnitsF;
          // Refresh the data when the units change
    }

    @Override
    public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item layout using ViewBinding
        ForecastActivityItemBinding binding = ForecastActivityItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ForecastViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ForecastViewHolder holder, int position) {
        ForecastDayItem forecastDay = forecastList.get(position);
        // Bind your data using ViewBinding
        holder.bind(forecastDay);
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    public class ForecastViewHolder extends RecyclerView.ViewHolder {

        private ForecastActivityItemBinding binding;

        public ForecastViewHolder(ForecastActivityItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ForecastDayItem forecastDay) {
            double temperature = forecastDay.getTemp();
            String unit = isUnitsF ? "F" : "C"; // Use "F" or "C" based on the unit
            ColorMaker.setColorGradient(binding.getRoot(), temperature, unit);

            Log.d("temp","temp"+temperature);
            // Set the values to your views using the ViewBinding object
            Date date = new Date(forecastDay.getDatetimeEpoch() * 1000L); // Convert seconds to milliseconds

            // Format the date as "Saturday 10/12"
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE MM/dd", Locale.getDefault());
            String formattedDate = dateFormat.format(date);

            // Set the formatted date to DayandDate TextView
            binding.DayandDate.setText(formattedDate);
            binding.forecastDesc.setText(forecastDay.getDesc());
            if (isUnitsF) {
                // Fahrenheit format
                binding.forecastTemp.setText(String.format("%.1f°F / %.1f°F", forecastDay.getTempMax(), forecastDay.getTempMin()));
            } else {
                // Celsius format
                binding.forecastTemp.setText(String.format("%.1f°C / %.1f°C", forecastDay.getTempMax(), forecastDay.getTempMin()));
            }

            binding.precipitation.setText("("+String.format("%d%%", forecastDay.getPrecipProb()) +" precip.)");
            binding.Uvindex.setText(String.format("UV Index: %d", forecastDay.getUvIndex()));

            // Handle weather icon
            String iconName = forecastDay.getIcon().replace("-", "_");
            Log.d("icon", iconName);

            // Get the resource ID of the desired icon from R.drawable
            int iconID = getId(iconName, R.drawable.class);

            // Set the image resource for the weather icon
            if (iconID != 0) {
                binding.forecastIcon.setImageResource(iconID);
            } else {
                // Optional: Set a default icon in case the specified icon is not found
                binding.forecastIcon.setImageResource(R.mipmap.ic_launcher); // Replace with your default icon
            }

            // Set temperature for specific times (Morning, Afternoon, Evening, Night)
            if (isUnitsF) {
                // Fahrenheit format
                binding.Morning.setText(String.format("%.1f°F\nMorning", forecastDay.getMorningTemp()));
                binding.Afternoon.setText(String.format("%.1f°F\nAfternoon", forecastDay.getAfternoonTemp()));
                binding.Evening.setText(String.format("%.1f°F\nEvening", forecastDay.getEveningTemp()));
                binding.Night.setText(String.format("%.1f°F\nNight", forecastDay.getNightTemp()));
            } else {
                // Celsius format
                binding.Morning.setText(String.format("%.1f°C\nMorning", forecastDay.getMorningTemp()));
                binding.Afternoon.setText(String.format("%.1f°C\nAfternoon", forecastDay.getAfternoonTemp()));
                binding.Evening.setText(String.format("%.1f°C\nEvening", forecastDay.getEveningTemp()));
                binding.Night.setText(String.format("%.1f°C\nNight", forecastDay.getNightTemp()));
            }


            // If loading icons from the web, consider using a library like Glide or Picasso
            // Example with Glide:
            // Glide.with(context).load(forecastDay.getIconUrl()).into(binding.forecastIcon);
        }

        // Utility method to dynamically fetch resource ID from R.drawable
        private int getId(String resourceName, Class<?> c) {
            try {
                // Try to get the ID from the specified class (R.drawable in this case)
                Field field = c.getDeclaredField(resourceName);
                return field.getInt(null);
            } catch (Exception e) {
                e.printStackTrace();
                return 0; // Return 0 if the resource is not found
            }
        }


    }
}
