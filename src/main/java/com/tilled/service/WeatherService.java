package com.tilled.service;

import com.tilled.data.WeatherType;
import com.tilled.utils.K;

import java.util.Random;

public class WeatherService implements Service<WeatherType> {
    private final static Random random = new Random();
    private WeatherType weather;

    public WeatherService() {
        this.weather = WeatherType.CLEAR;
    }

    public void setWeather(WeatherType weather) {
        this.weather = weather;
    }

    public WeatherType getWeather() {
        return weather;
    }

    public WeatherType nextWeather() {
        return WeatherType.values()[
                (int) (Math.random() * WeatherType.values().length)];
    }

    public void update() {
        if (random.nextFloat() < K.World.WEATHER_CHANGE_PROBABILITY) {
            setWeather(nextWeather());
        }
    }
}
