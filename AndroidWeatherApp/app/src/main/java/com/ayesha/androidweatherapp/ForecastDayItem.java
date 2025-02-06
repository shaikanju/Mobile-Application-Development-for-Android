package com.ayesha.androidweatherapp;

public class ForecastDayItem {
    private long datetimeEpoch;
    private double tempMax;
    private double tempMin;
    private int precipProb;
    private int uvIndex;
    private String icon;
    private String desc;
    private String city;
    private double morningTemp;
    private double afternoonTemp;
    private double eveningTemp;
    private double nightTemp;
    private double temp;

    // Getter and Setter for datetimeEpoch
    public long getDatetimeEpoch() {
        return datetimeEpoch;
    }

    public void setDatetimeEpoch(long datetimeEpoch) {
        this.datetimeEpoch = datetimeEpoch;
    }
    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    // Getter and Setter for tempMax
    public double getTempMax() {
        return tempMax;
    }

    public void setTempMax(double tempMax) {
        this.tempMax = tempMax;
    }

    // Getter and Setter for tempMin
    public double getTempMin() {
        return tempMin;
    }

    public void setTempMin(double tempMin) {
        this.tempMin = tempMin;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }



    // Getter and Setter for precipProb
    public int getPrecipProb() {
        return precipProb;
    }

    public void setPrecipProb(int precipProb) {
        this.precipProb = precipProb;
    }

    // Getter and Setter for uvIndex
    public int getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(int uvIndex) {
        this.uvIndex = uvIndex;
    }

    // Getter and Setter for icon
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    // Getter and Setter for morningTemp
    public double getMorningTemp() {
        return morningTemp;
    }

    public void setMorningTemp(double morningTemp) {
        this.morningTemp = morningTemp;
    }

    // Getter and Setter for afternoonTemp
    public double getAfternoonTemp() {
        return afternoonTemp;
    }

    public void setAfternoonTemp(double afternoonTemp) {
        this.afternoonTemp = afternoonTemp;
    }

    // Getter and Setter for eveningTemp
    public double getEveningTemp() {
        return eveningTemp;
    }

    public void setEveningTemp(double eveningTemp) {
        this.eveningTemp = eveningTemp;
    }

    // Getter and Setter for nightTemp
    public double getNightTemp() {
        return nightTemp;
    }

    public void setNightTemp(double nightTemp) {
        this.nightTemp = nightTemp;
    }
}
