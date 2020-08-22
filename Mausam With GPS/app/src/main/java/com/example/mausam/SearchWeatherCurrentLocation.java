package com.example.mausam;

import android.app.Activity;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchWeatherCurrentLocation {

    private static final String TAG= CurrentWeatherService.class.getSimpleName();

    //PUT API URL
    private static final String URL="https://api.openweathermap.org/data/2.5/weather";

    // I have Used OpenWeathermap.com for weather services
    //this will be used to cancelOperations
    //belong to android volley
    private static final String  CURRENT_WEATHER_TAG="CURRENT_WEATHER";
    //Put your API KEY
    private static final String API_KEY="c9476d2242da55eb96ec06f9cba4784d";

    private RequestQueue queue;

    public SearchWeatherCurrentLocation(@NonNull Activity activity)
    {
        queue= Volley.newRequestQueue(activity.getApplicationContext());
    }

    public interface CurrentWeatherCallbackGPS{
        @MainThread
        void onCurrentWeather(@NonNull final CurrentWeather currentWeather);

        @MainThread
        void onError(@Nullable Exception exception);
    }

    public void getCurrentWeather(double lat,double lon,@NonNull final CurrentWeatherCallbackGPS callback)
    {
        final String url=String.format("%s?lat=%s&lon=%s&appId=%s",URL,lat,lon,API_KEY);
//
//  api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={your api key}
        //now we will create StringRequest

        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            final JSONObject currentWeatherJSONObject = new JSONObject(response);
                            final JSONArray weather = currentWeatherJSONObject.getJSONArray("weather");
                            final JSONObject weatherCondition = weather.getJSONObject(0);
                            final String locationName = currentWeatherJSONObject.getString("name");
                            final int conditionId = weatherCondition.getInt("id");
                            final String conditionName = weatherCondition.getString("main");
                            final double tempKelvin = currentWeatherJSONObject.getJSONObject("main").getDouble("temp");

                            final CurrentWeather currentWeather = new CurrentWeather(locationName, conditionId, conditionName, tempKelvin);
                            callback.onCurrentWeather(currentWeather);
                        } catch (JSONException e) {
                            callback.onError(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        } );  //StringRequest Method

        stringRequest.setTag(CURRENT_WEATHER_TAG);
        queue.add(stringRequest);

    }  //getCurentWeather() Method Ends


    public void cancel()
    {
        queue.cancelAll(CURRENT_WEATHER_TAG);
    }


}
