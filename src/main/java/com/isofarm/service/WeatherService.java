package com.isofarm.service;

import com.isofarm.data.WeatherType;
import com.isofarm.graphics.Camera;
import com.isofarm.graphics.RainEngine;
import com.isofarm.utils.K;
import com.isofarm.wrld.World;

import java.util.Random;

public class WeatherService implements Service<WeatherType> {
    private final static Random random = new Random();
    private final RainEngine rainEngine;
    private final Camera camera;
    private static WeatherType weather;

    public WeatherService(RainEngine rainEngine, Camera camera) {
        this.rainEngine = rainEngine;
        this.camera = camera;
        weather = WeatherType.CLEAR;
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

    public static boolean isRaining() {
        return weather == WeatherType.RAIN ||
                weather == WeatherType.HEAVY_STORM;
    }
}
