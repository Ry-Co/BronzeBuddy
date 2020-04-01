package com.example.bronzebuddy.Objects;


import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class dayObject {
    String ISODate;
    //DateTime dateTime;
    LocalDate localDate;
    int weatherCode;
    double UVI;

    public dayObject(String isoDate, int wc, double uvi) {
        ISODate = isoDate;
        weatherCode = wc;
        UVI = uvi;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            DateTime dateTime = new DateTime(format.parse(isoDate));
            localDate = dateTime.toLocalDate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public dayObject(LocalDate ld, int wc, double uvi) {
        localDate = ld;
        weatherCode = wc;
        UVI = uvi;
    }

    public String getISODate() {
        return ISODate;
    }

    public void setISODate(String ISODate) {
        this.ISODate = ISODate;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }

    public double getUVI() {
        return UVI;
    }

    public void setUVI(double UVI) {
        this.UVI = UVI;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    @NonNull
    @Override
    public String toString() {
        return localDate.toString();
    }
}

