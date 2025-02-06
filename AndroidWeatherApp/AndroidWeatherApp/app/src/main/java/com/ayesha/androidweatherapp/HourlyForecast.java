package com.ayesha.androidweatherapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HourlyForecast {
    private String datetime;
    private double temp;
    private String conditions;
    private long datetimeEpoch;
    private String icon;
    private String dayOfWeek;

    // Empty constructor
    public HourlyForecast() {}

    // Getters and Setters
    public String getDatetime() {
        return convertTo12HourFormat(datetime);
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
    public String getDatetimeEpoch() {
        return dayOfWeek; // Still return epoch if needed
    }

    public void setDatetimeEpoch(long datetimeEpoch) {
        this.datetimeEpoch = datetimeEpoch;
        this.dayOfWeek = convertEpochToDayOfWeek(datetimeEpoch); // Update dayOfWeek when epoch is set
    }
    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
    private String convertEpochToDayOfWeek(long epoch) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        Date date = new Date(epoch * 1000); // Convert epoch to Date
        String dayName = dayFormat.format(date);

        // Check if the date is today
        Calendar today = Calendar.getInstance();
        today.setTime(new Date()); // Set today's date
        Calendar targetDay = Calendar.getInstance();
        targetDay.setTime(date); // Set the target day

        if (today.get(Calendar.YEAR) == targetDay.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == targetDay.get(Calendar.DAY_OF_YEAR)) {
            return "Today"; // Return "Today" if the dates match
        } else {
            return dayName; // Otherwise return the day name
        }
    }

    private String convertTo12HourFormat(String time) {
        SimpleDateFormat twentyFourFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat twelveHourFormat = new SimpleDateFormat("hh:mm a");
        try {
            Date date = twentyFourFormat.parse(time);
            return twelveHourFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return time; // Return original time if parsing fails
        }
    }
}

