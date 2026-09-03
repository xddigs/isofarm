package com.isofarm.data;

/**
 * Defines the levelable contract.
 */
public interface Levelable {
    /**
     * Returns the level.
     * @return the level
     */
    int getLevel();
    /**
     * Sets the level.
     * @param level the level value
     */
    void setLevel(int level);
    /**
     * Returns the experience.
     * @return the experience
     */
    int getExperience();
    /**
     * Sets the experience.
     * @param experience the experience value
     */
    void setExperience(int experience);
    /**
     * Returns the experience for next level.
     * @return the experience for next level
     */
    int getExperienceForNextLevel();
    /**
     * Sets the experience for next level.
     * @param experienceForNextLevel the experience for next level value
     */
    void setExperienceForNextLevel(int experienceForNextLevel);
    /**
     * Performs the calc next level operation.
     * @return the calc next level result
     */
    int calcNextLevel();
    /**
     * Performs the level up operation.
     */
    void levelUp();
    /**
     * Performs the gain operation.
     * @param experience the experience value
     */
    void gain(int experience);
}
