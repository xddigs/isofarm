package com.isofarm.data;

public interface Levelable {
    int getLevel();
    void setLevel(int level);
    int getExperience();
    void setExperience(int experience);
    int getExperienceForNextLevel();
    void setExperienceForNextLevel(int experienceForNextLevel);
    int calcNextLevel();
    void levelUp();
    void gain(int experience);
}
