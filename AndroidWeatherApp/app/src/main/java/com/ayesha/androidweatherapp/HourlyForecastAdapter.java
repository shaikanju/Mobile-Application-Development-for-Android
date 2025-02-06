package com.ayesha.androidweatherapp;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ayesha.androidweatherapp.databinding.HourlyItemBinding; // Use the correct generated binding class

import java.lang.reflect.Field;
import java.util.List;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.HourlyForecastViewHolder> {

    private List<HourlyForecast> hourlyForecasts;
    private static boolean isUnitsF=true;

    public HourlyForecastAdapter(List<HourlyForecast> hourlyForecasts) {
        this.hourlyForecasts = hourlyForecasts;

    }
    public void updateUnitsFlag(boolean isUnitsF) {
        this.isUnitsF = isUnitsF;
        notifyDataSetChanged();  // Notify that data has changed
    }

    @NonNull
    @Override
    public HourlyForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using View Binding
        HourlyItemBinding binding = HourlyItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HourlyForecastViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyForecastViewHolder holder, int position) {
        HourlyForecast forecast = hourlyForecasts.get(position);
        holder.bind(forecast); // Bind the forecast data to the holder
    }

    @Override
    public int getItemCount() {
        return hourlyForecasts.size();
    }

    public void updateHourlyForecasts(List<HourlyForecast> newForecasts) {
        this.hourlyForecasts.clear();
        this.hourlyForecasts.addAll(newForecasts);
        notifyDataSetChanged();
    }

    // ViewHolder class using View Binding
    public static class HourlyForecastViewHolder extends RecyclerView.ViewHolder {
        private final HourlyItemBinding binding; // Binding object

        public HourlyForecastViewHolder(HourlyItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HourlyForecast forecast) {

            binding.dayName.setText(forecast.getDatetimeEpoch()); // Set day name
            binding.time.setText(forecast.getDatetime()); // Set time
            binding.TemperatureHourly.setText(String.format("%.1f %s", forecast.getTemp(), isUnitsF ? "°F" : "°C")); // Set temperature
            binding.description.setText(forecast.getConditions());
            String iconName = forecast.getIcon().replace("-", "_"); // Format icon name
            int iconID = getId(iconName, R.drawable.class); // Method to get drawable resource ID
            if (iconID != 0) {
                binding.weatherIcon.setImageResource(iconID); // Set weather icon
            } else {
                binding.weatherIcon.setImageResource(R.mipmap.ic_launcher); // Default icon if not found
            }
        }
        public static int getId(String resourceName, Class<?> c) {
            try {
                Field idField = c.getDeclaredField(resourceName.replace("-", "_"));
                return idField.getInt(idField);
            } catch (Exception e) {
                return 0; // Handle resource not found
            }
        }
    }
}
