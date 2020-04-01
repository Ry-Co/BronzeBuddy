package com.example.bronzebuddy.Workers;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.example.bronzebuddy.Objects.dayObject;
import com.example.bronzebuddy.R;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

//800 - 803 are the only weather codes we want, [Clear skies - broken clouds]
//THIS CLASS WILL RETRIEVE CURRENT LOCAL WEATHER AND UV
//IN FUTURE WE GRAB 5DAY FORECAST AND SET REMINDERS FOR LATER IN THE WEEK
public class WeatherWorker extends AsyncTask<Void, Void, String> {
    private static final String TAG = WeatherWorker.class.getSimpleName();
    Context mContext;
    double mLat, mLon;
    URL API_CURRENT_WEATHER, API_CURRENT_UV, API_FORECAST_WEATHER, API_FORECAST_UV;
    JSONObject currentWeatherJSON, currentUVJSON, forecastWeatherJSON;
    JSONArray forecastUVJSON;
    String API_KEY;
    ArrayList<URL> apiURLs = new ArrayList<>();
    private ForecastListener listener;

    public WeatherWorker(Context context, Location location, ForecastListener l) {
        mContext = context;
        API_KEY = mContext.getResources().getString(R.string.openWeatherAPIkey);
        mLat = location.getLatitude();
        mLon = location.getLongitude();
        try {
            API_CURRENT_WEATHER = new URL("http://api.openweathermap.org/data/2.5/weather?appid=" + API_KEY + "&lat=" + mLat + "&lon=" + mLon);
            API_CURRENT_UV = new URL("http://api.openweathermap.org/data/2.5/uvi?appid=" + API_KEY + "&lat=" + mLat + "&lon=" + mLon);
            API_FORECAST_WEATHER = new URL("http://api.openweathermap.org/data/2.5/forecast?appid=" + API_KEY + "&lat=" + mLat + "&lon=" + mLon);
            API_FORECAST_UV = new URL("http://api.openweathermap.org/data/2.5/uvi/forecast?appid=" + API_KEY + "&lat=" + mLat + "&lon=" + mLon + "&cnt=" + 5); // 5 so that we match length of UV and Weather forecasts
            apiURLs.add(API_CURRENT_WEATHER);
            apiURLs.add(API_CURRENT_UV);
            apiURLs.add(API_FORECAST_WEATHER);
            apiURLs.add(API_FORECAST_UV);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.listener = l;
        Log.i(TAG, "WeatherWorker:SUCCESS:Worker built");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        for (URL url : apiURLs) {
            if (url.equals(API_CURRENT_UV)) {
                currentUVJSON = getJSON(callURL(url));
            } else if (url.equals(API_FORECAST_UV)) {
                //for some reason UV forecasts are JSONArrays and not JSONObjects
                try {
                    forecastUVJSON = new JSONArray(callURL(url));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (url.equals(API_CURRENT_WEATHER)) {
                currentWeatherJSON = getJSON(callURL(url));
            } else if (url.equals(API_FORECAST_WEATHER)) {
                forecastWeatherJSON = getJSON(callURL(url));
            }
        }
        parseJSONObjects();
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s == null) {
            //log an error and maybe retry
        }
        super.onPostExecute(s);
    }

    public String callURL(URL url) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
                Log.i(TAG, "WeatherWorker:SUCCESS:"+url);
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    public JSONObject getJSON(String data) {
        try {
            JSONObject json = new JSONObject(data);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void parseJSONObjects() {
        ArrayList<dayObject> forecast = new ArrayList<>();
        HashMap<LocalDate, Integer> daysFinal = new HashMap<>();
        forecast.add(getToday());
        daysFinal = getDays();
        forecast.addAll(getForecast(daysFinal));
        listener.onForecastReady(forecast);
        Log.i(TAG, "WeatherWorker:SUCCESS:Forecast finished");
        //we now have a complete forecast, today +5

    }

    public dayObject getToday() {
        SimpleDateFormat formatUV = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            int wc = currentWeatherJSON.getJSONArray("weather").getJSONObject(0).getInt("id");
            double uvi = currentUVJSON.getDouble("value");
            DateTime dateTime = new DateTime(formatUV.parse(currentUVJSON.getString("date_iso")));
            LocalDate ld = dateTime.toLocalDate();
            return new dayObject(ld, wc, uvi);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<LocalDate, Integer> getDays() {
        HashMap<LocalDate, ArrayList<Integer>> days = new HashMap<>();
        HashMap<LocalDate, Integer> daysFinal = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        try {
            int l = forecastWeatherJSON.getJSONArray("list").length();
            for (int i = 0; i < l; i++) {
                JSONObject jsonObject = forecastWeatherJSON.getJSONArray("list").getJSONObject(i);
                DateTime dT = new DateTime(format.parse(jsonObject.getString("dt_txt")));
                if (!days.containsKey(dT.toLocalDate())) {
                    ArrayList<Integer> aRayList = new ArrayList<>();
                    aRayList.add(jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id"));
                    days.put(dT.toLocalDate(), aRayList);
                } else {
                    ArrayList<Integer> tempList = days.get(dT.toLocalDate());
                    tempList.add(jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id"));
                    days.put(dT.toLocalDate(), tempList);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (LocalDate key : days.keySet()) {
            ArrayList<Integer> weatherCodes = days.get(key);
            Iterator<Integer> iterator = weatherCodes.iterator();
            double sum = 0;
            while (iterator.hasNext()) {
                sum += iterator.next();
            }
            double average = sum / weatherCodes.size();
            int finalWeatherCode = (int) Math.round(average);
            daysFinal.put(key, finalWeatherCode);
        }
        return daysFinal;
    }

    public ArrayList<dayObject> getForecast(HashMap<LocalDate, Integer> daysFinal) {
        SimpleDateFormat formatUV = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        ArrayList<dayObject> forecast = new ArrayList<>();
        int length = forecastUVJSON.length();
        for (int i = 0; i < length; i++) {
            try {
                JSONObject jsonObject = forecastUVJSON.getJSONObject(i);
                DateTime dateTime = new DateTime(formatUV.parse(jsonObject.getString("date_iso")));
                double uvi = jsonObject.getDouble("value");
                if (daysFinal.containsKey(dateTime.toLocalDate())) {
                    dayObject day = new dayObject(dateTime.toLocalDate(), daysFinal.get(dateTime.toLocalDate()), uvi);
                    forecast.add(day);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return forecast;
        //forecast now contains an array of days with weather code and uvi
    }

    public interface ForecastListener {
        void onForecastReady(ArrayList<dayObject> fc);
    }
}

