package com.tilled.service;

import com.tilled.data.WeatherType;
import com.tilled.utils.K;

import java.util.Random;

public class WeatherService implements Service<WeatherType> {
    private final static Random random = new Random();
    private WeatherType weather;

    public WeatherService() {
        this.weather = WeatherType.RAIN;
    }

    public void setWeather(WeatherType weather) {
        this.weather = weather;
    }

    public WeatherType getWeather() {
        return weather;
    }

    public WeatherType nextWeather() {
        if (random.nextFloat() < K.World.WEATHER_CHANGE_PROBABILITY) {
            return WeatherType.values()[
                    (int) (Math.random() * WeatherType.values().length)];
        }
        return weather;
    }

    public boolean isRaining() {
        return weather == WeatherType.RAIN ||
               weather == WeatherType.HEAVY_STORM;
    }
}
