package com.isofarm.service;

import com.isofarm.data.Season;
import com.isofarm.data.Singleton;
import com.isofarm.utils.Local;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides time service behavior.
 */
@Singleton
public class TimeService {
    public static final TimeService ts = new TimeService();
    private static final Logger log = LoggerFactory.getLogger(TimeService.class);
    private static final float REAL_SECONDS_PER_IN_GAME_MINUTE = 0.7f;
    private static final int MINUTES_PER_HOUR = 60;
    private static final int HOURS_PER_DAY = 24;
    private static final int DAYS_PER_SEASON = 28;
    private static final int STARTING_HOUR = 8;

    private Season currentSeason = Season.SPRING;
    private float secondAccumulator = 0.0f;
    private static int minute = 0;
    private static int hour = STARTING_HOUR;
    private int day = 1;
    private int year = 0;
    private float timeScale = 2.0f;

    /**
     * Creates a new {@code TimeService} instance.
     */
    private TimeService() {}

    /**
     * Updates the current state.
     * @param delta the delta value
     * @param weatherService the weather service value
     */
    public void update(float delta, WeatherService weatherService) {
        secondAccumulator += delta * timeScale;
        while (secondAccumulator >= REAL_SECONDS_PER_IN_GAME_MINUTE) {
            secondAccumulator -= REAL_SECONDS_PER_IN_GAME_MINUTE;
            advanceMinute(weatherService);
        }
    }

    /**
     * Returns the minute.
     * @return the minute
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Returns the hour.
     * @return the hour
     */
    public int getHour() {
        return hour;
    }

    /**
     * Returns the day.
     * @return the day
     */
    public int getDay() {
        return day;
    }

    /**
     * Returns the current season.
     * @return the current season
     */
    public Season getCurrentSeason() {
        return currentSeason;
    }

    /**
     * Returns the year.
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * Returns the formatted time.
     * @return the formatted time
     */
    public String getFormattedTime() {
        return Local.lang.f("time.formatted", year, currentSeason.getDisplayName(), day, hour, minute);
    }

    /**
     * Sets the time scale.
     * @param timeScale the time scale value
     */
    public void setTimeScale(float timeScale) {
        this.timeScale = Math.max(0.0f, timeScale);
    }

    /**
     * Returns the time scale.
     * @return the time scale
     */
    public float getTimeScale() {
        return timeScale;
    }

    /**
     * Adds the time scale.
     * @param delta the delta value
     */
    public void addTimeScale(float delta) {
        setTimeScale(getTimeScale() + delta);
    }

    /**
     * Performs the slow time scale operation.
     * @param delta the delta value
     */
    public void slowTimeScale(float delta) {
        setTimeScale(getTimeScale() * (1.0f - delta));
    }

    /**
     * Performs the advance minute operation.
     * @param weatherService the weather service value
     */
    private void advanceMinute(WeatherService weatherService) {
        minute++;
        if (minute >= MINUTES_PER_HOUR) {
            minute = 0;
            advanceHour(weatherService);
        }
    }

    /**
     * Performs the advance hour operation.
     * @param weatherService the weather service value
     */
    private void advanceHour(WeatherService weatherService) {
        hour++;
        weatherService.setWeather(weatherService.nextWeather());
        if (hour >= HOURS_PER_DAY) {
            advanceDay();
        }
    }

    /**
     * Performs the advance day operation.
     */
    public void advanceDay() {
        hour = STARTING_HOUR;
        minute = 0;
        secondAccumulator = 0.0f;
        day++;

        if (day > DAYS_PER_SEASON) {
            day = 1;
            advanceSeason();
        }

        log.info("New day started: Year {} {}, Day {}", year, currentSeason, day);
    }

    /**
     * Performs the advance season operation.
     */
    private void advanceSeason() {
        Season[] seasons = Season.values();
        int nextSeasonIndex = (currentSeason.ordinal() + 1) % seasons.length;
        currentSeason = seasons[nextSeasonIndex];

        if (nextSeasonIndex == 0) {
            year++;
            log.info("Happy New Year! Welcome to Year {}", year);
        }

        log.info("Season changed to {}", currentSeason);
    }

    /**
     * Returns the sky color.
     * @return the sky color
     */
    public static Vector3f getSkyColor() {
        float time = hour + minute / 60.0f;
        Vector3f cloudy = new Vector3f(0.48f, 0.48f, 0.48f);
        Vector3f night = new Vector3f(0.025f, 0.035f, 0.09f);
        Vector3f dawn = new Vector3f(0.85f, 0.40f, 0.22f);
        Vector3f day = new Vector3f(0.35f, 0.65f, 0.95f);
        Vector3f dusk = new Vector3f(0.70f, 0.25f, 0.30f);
        Vector3f result = new Vector3f();

        boolean isRaining = WeatherService.isRaining();
        if (isRaining) {
            float t = smoothStep(5.0f, 7.0f, time);
            cloudy.lerp(cloudy, t, result);
            return result;
        }

        if (time < 5.0f) {
            result.set(night);
        } else if (time < 7.0f) {
            float t = smoothStep(5.0f, 7.0f, time);
            night.lerp(dawn, t, result);
        } else if (time < 12.0f) {
            float t = smoothStep(7.0f, 12.0f, time);
            dawn.lerp(day, t, result);
        } else if (time < 17.0f) {
            float t = smoothStep(12.0f, 17.0f, time);
            day.lerp(day, t, result);
        } else if (time < 20.0f) {
            float t = smoothStep(17.0f, 20.0f, time);
            day.lerp(dusk, t, result);
        } else if (time < 22.0f) {
            float t = smoothStep(20.0f, 22.0f, time);
            dusk.lerp(night, t, result);
        } else {
            result.set(night);
        }

        return result;
    }

    /**
     * Performs the smooth step operation.
     * @param start the start value
     * @param end the end value
     * @param value the value value
     * @return the smooth step result
     */
    private static float smoothStep(float start, float end, float value) {
        float t = (value - start) / (end - start);
        t = Math.clamp(t, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }
}