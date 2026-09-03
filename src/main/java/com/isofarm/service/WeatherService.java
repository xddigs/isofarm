package com.isofarm.service;

import com.isofarm.data.Singleton;
import com.isofarm.data.WeatherType;
import com.isofarm.utils.K;

import java.util.Random;

/**
 * Provides weather service behavior.
 */
@Singleton
public class WeatherService implements Service<WeatherType> {
    public static final WeatherService wes = new WeatherService();
    private final static Random random = new Random();
    private static WeatherType weather = WeatherType.CLEAR;

    /**
     * Creates a new {@code WeatherService} instance.
     */
    private WeatherService() {}

    /**
     * Checks whether the raining condition is met.
     * @return {@code true} if raining; otherwise {@code false}
     */
    public static boolean isRaining() {
        return weather == WeatherType.RAIN ||
                weather == WeatherType.THUNDERSTORM;
    }

    /**
     * Returns the weather.
     * @return the weather
     */
    public WeatherType getWeather() {
        return weather;
    }

    /**
     * Sets the weather.
     * @param weather the weather value
     */
    public void setWeather(WeatherType weather) {
        WeatherService.weather = weather;
    }

    /**
     * Performs the next weather operation.
     * @return the next weather result
     */
    public WeatherType nextWeather() {
        if (random.nextFloat() < K.World.WEATHER_CHANGE_PROBABILITY) {
            return WeatherType.values()[
                    (int) (Math.random() * WeatherType.values().length)];
        }
        return weather;
    }
}
