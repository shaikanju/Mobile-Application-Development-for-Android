package com.ayesha.androidweatherapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import java.util.Locale;

public class ColorMaker {

    // Method to set gradient color based on temperature
    public static void setColorGradient(View view, double tempIn, String unitLetter) {
        double temp = tempIn;

        // Convert Celsius to Fahrenheit if needed
        if (unitLetter.equalsIgnoreCase("C")) {
            temp = (tempIn * 9 / 5) + 32; // Convert Celsius to Fahrenheit
        }

        int[] colors = getTemperatureColor((int) temp);

        // Generate colors for the gradient
        String startColorString = String.format(Locale.getDefault(), "#FF%02x%02x%02x", colors[0], colors[1], colors[2]);
        int startColor = Color.parseColor(startColorString);

        String endColorString = String.format(Locale.getDefault(), "#99%02x%02x%02x", colors[0], colors[1], colors[2]);
        int endColor = Color.parseColor(endColorString);

        // Create the gradient drawable and set it as the background
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{startColor, endColor});
        gradientDrawable.setCornerRadius(0f);

        view.setBackground(gradientDrawable);
    }

    // Helper method to get RGB values based on temperature ranges
    private static int[] getTemperatureColor(int temperature) {
        int[] rgb = new int[3];

        if (temperature < 40) { // Cool temperatures
            rgb[0] = 0; // Red
            rgb[1] = 0; // Green
            rgb[2] = 40 + temperature * 3; // Blue (darker at lower temps)
        } else if (temperature <= 81) { // Moderate temperatures
            rgb[1] = (int) (temperature * 1.5); // Green
            rgb[0] = rgb[1] / 2; // Red
            rgb[2] = rgb[0] / 2; // Blue
        } else { // Warm temperatures
            rgb[0] = 40 + temperature; // Red
            rgb[1] = 0; // Green
            rgb[2] = 0; // Blue
        }

        return rgb;
    }
}
