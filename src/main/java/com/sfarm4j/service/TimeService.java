package com.sfarm4j.service;

import com.sfarm4j.data.Season;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeService {
    private static final Logger log = LoggerFactory.getLogger(TimeService.class);

    private static final float REAL_SECONDS_PER_IN_GAME_MINUTE = 0.7f;
    private static final int MINUTES_PER_HOUR = 60;
    private static final int HOURS_PER_DAY = 24;
    private static final int DAYS_PER_SEASON = 28;
    private static final int STARTING_HOUR = 6;

    private final CropService cropService;

    private float secondAccumulator = 0.0f;
    private int minute = 0;
    private int hour = STARTING_HOUR;
    private int day = 1;
    private Season currentSeason = Season.SPRING;
    private int year = 1;

    public TimeService(CropService cropService) {
        this.cropService = cropService;
        log.info("TimeService initialized. Starting at Year {} {}, Day {} - {}:00", 
                year, currentSeason, day, hour);
    }

    public void update(float delta) {
        secondAccumulator += delta;
        while (secondAccumulator >= REAL_SECONDS_PER_IN_GAME_MINUTE) {
            secondAccumulator -= REAL_SECONDS_PER_IN_GAME_MINUTE;
            advanceMinute();
        }
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
        cropService.process(currentSeason);
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
}