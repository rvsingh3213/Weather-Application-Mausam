package com.example.mausam;

public class CurrentWeather {
    final String location;
    final int conditionId;
    final String weatherCondition;
    final double temperature;


    public CurrentWeather(final String location,
                          final int conditionId,
                          final String weatherCondition,
                          final double temperature)
    {
        this.location=location;
        this.conditionId=conditionId;
        this.weatherCondition=weatherCondition;
        this.temperature=temperature;
    }
    public int getTempFahrenheit(){
        //if want to get in Fahrenheit
       // return (int)(temperature*9/5-459.67);

        return (int)(temperature-273.15);
    }
}
