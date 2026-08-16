package com.tilled.service;

import com.tilled.data.WeatherType;
import com.tilled.graphics.Camera;
import com.tilled.graphics.RainEngine;
import com.tilled.utils.K;

import java.util.Random;

public class WeatherService implements Service<WeatherType> {
    private final static Random random = new Random();
    private final RainEngine rainEngine;
    private final Camera camera;
    private WeatherType weather;

    public WeatherService(RainEngine rainEngine, Camera camera) {
        this.rainEngine = rainEngine;
        this.camera = camera;
        this.weather = WeatherType.CLEAR;
    }

    public void setWeather(float delta, WeatherType weather,
                           boolean isRaining, int amount) {
        this.weather = weather;
        if (isRaining) {
            rainEngine.update(delta, camera.getPosition(), amount);
        }
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
