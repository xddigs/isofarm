package com.tilled.service;

import com.tilled.data.Season;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeService {
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
    private float timeScale = 5.0f;

    public TimeService() {
        log.info("TimeService initialized. Starting at Year {} {}, Day {} - {}:00",
                year, currentSeason, day, hour);
    }

    public void update(float delta) {
        secondAccumulator += delta * timeScale;
        while (secondAccumulator >= REAL_SECONDS_PER_IN_GAME_MINUTE) {
            secondAccumulator -= REAL_SECONDS_PER_IN_GAME_MINUTE;
            advanceMinute();
        }
    }

    public int getMinute() {
        return minute;
    }

    public int getHour() {
        return hour;
    }

    public int getDay() {
        return day;
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public int getYear() {
        return year;
    }

    public String getFormattedTime() {
        return String.format("Year %d %s, Day %02d | %02d:%02d", 
                year, currentSeason, day, hour, minute);
    }

    public void setTimeScale(float timeScale) {
        this.timeScale = Math.max(0.0f, timeScale);
    }

    public float getTimeScale() {
        return timeScale;
    }

    public void addTimeScale(float delta) {
        setTimeScale(getTimeScale() + delta);
    }

    public void slowTimeScale(float delta) {
        setTimeScale(getTimeScale() * (1.0f - delta));
    }

    private void advanceMinute() {
        minute++;
        if (minute >= MINUTES_PER_HOUR) {
            minute = 0;
            advanceHour();
        }
    }

    private void advanceHour() {
        hour++;
        if (hour >= HOURS_PER_DAY) {
            advanceDay();
        }
    }

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

    public static Vector3f getSkyColor() {
        float timeInHours = hour + (minute / 60.0f);
        Vector3f night  = new Vector3f(0.05f, 0.05f, 0.12f);
        Vector3f dawn   = new Vector3f(0.85f, 0.45f, 0.30f);
        Vector3f day    = new Vector3f(0.35f, 0.65f, 0.95f);
        Vector3f dusk   = new Vector3f(0.75f, 0.30f, 0.35f);
        Vector3f result = new Vector3f();

        if (timeInHours >= 0.0f && timeInHours < 5.0f) {
            result.set(night);
        } else if (timeInHours >= 5.0f && timeInHours < 7.0f) {
            float t = (timeInHours - 5.0f) / 2.0f;
            night.lerp(dawn, t, result);
        } else if (timeInHours >= 7.0f && timeInHours < 12.0f) {
            float t = (timeInHours - 7.0f) / 5.0f;
            dawn.lerp(day, t, result);
        } else if (timeInHours >= 12.0f && timeInHours < 17.0f) {
            result.set(day);
        } else if (timeInHours >= 17.0f && timeInHours < 20.0f) {
            float t = (timeInHours - 17.0f) / 3.0f;
            day.lerp(dusk, t, result);
        } else if (timeInHours >= 20.0f && timeInHours < 22.0f) {
            float t = (timeInHours - 20.0f) / 2.0f;
            dusk.lerp(night, t, result);
        } else {
            result.set(night);
        }

        return result;
    }

    public static Vector3f getSunLightColor() {
        float timeInHours = hour + (minute / 60.0f);

        Vector3f nightColor = new Vector3f(0.4f, 0.5f, 0.7f);
        Vector3f dawnColor  = new Vector3f(1.0f, 0.6f, 0.3f);
        Vector3f dayColor   = new Vector3f(1.0f, 0.98f, 0.9f);

        Vector3f result = new Vector3f();

        if (timeInHours >= 6.0f && timeInHours < 8.0f) {
            float t = (timeInHours - 6.0f) / 2.0f;
            dawnColor.lerp(dayColor, t, result);
        } else if (timeInHours >= 8.0f && timeInHours < 18.0f) {
            result.set(dayColor);
        } else if (timeInHours >= 18.0f && timeInHours < 20.0f) {
            float t = (timeInHours - 18.0f) / 2.0f;
            dayColor.lerp(dawnColor, t, result);
        } else if (timeInHours >= 20.0f && timeInHours < 22.0f) {
            float t = (timeInHours - 20.0f) / 2.0f;
            dawnColor.lerp(nightColor, t, result);
        } else {
            result.set(nightColor);
        }

        return result;
    }

    public static float getSunIntensity() {
        float timeInHours = hour + (minute / 60.0f);

        if (timeInHours >= 7.0f && timeInHours < 19.0f) {
            return 1.0f;
        } else if (timeInHours >= 19.0f && timeInHours < 22.0f) {
            float t = (timeInHours - 19.0f) / 3.0f;
            return 1.0f - (t * 0.8f);
        } else if (timeInHours >= 5.0f && timeInHours < 7.0f) {
            float t = (timeInHours - 5.0f) / 2.0f;
            return 0.2f + (t * 0.8f);
        } else {
            return 0.2f;
        }
    }
}