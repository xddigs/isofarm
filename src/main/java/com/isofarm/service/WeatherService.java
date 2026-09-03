package com.isofarm.service;

import com.isofarm.data.Singleton;
import com.isofarm.data.WeatherType;
import com.isofarm.utils.K;

import java.util.Random;

@Singleton
public class WeatherService implements Service<WeatherType> {
    public static final WeatherService wes = new WeatherService();
    private final static Random random = new Random();
    private static WeatherType weather = WeatherType.CLEAR;

    private WeatherService() {}

    public static boolean isRaining() {
        return weather == WeatherType.RAIN ||
                weather == WeatherType.THUNDERSTORM;
    }

    public WeatherType getWeather() {
        return weather;
    }

    public void setWeather(WeatherType weather) {
        WeatherService.weather = weather;
    }

    public WeatherType nextWeather() {
        if (random.nextFloat() < K.World.WEATHER_CHANGE_PROBABILITY) {
            return WeatherType.values()[
                    (int) (Math.random() * WeatherType.values().length)];
        }
        return weather;
    }
}
