package com.isofarm.graphics;

import com.isofarm.data.Hit;
import com.isofarm.entity.Moon;
import com.isofarm.entity.Sun;
import org.joml.Vector3f;

public class CelestialLighting {
    private final Sun sun;
    private final Moon moon;

    private final Vector3f direction;
    private final Vector3f color;

    private float intensity;
    private float ambientIntensity;

    public CelestialLighting(Sun sun, Moon moon) {
        this.sun = sun;
        this.moon = moon;
        this.direction = new Vector3f();
        this.color = new Vector3f();
    }

    public void update(Hit hoveredCell, float timeOfDay) {
        sun.update(hoveredCell, timeOfDay);
        moon.update(hoveredCell, timeOfDay);

        float sunIntensity = sun.getIntensity();
        float moonIntensity = moon.getIntensity();

        if (sunIntensity > moonIntensity) {
            direction.set(sun.getDirection());
            color.set(sun.getColor());
            intensity = sunIntensity;
        } else {
            direction.set(moon.getDirection());
            color.set(moon.getColor());
            intensity = moonIntensity;
        }

        float daylight = Math.clamp(sunIntensity, 0.0f, 1.0f);
        ambientIntensity = 0.05f + daylight * 0.20f;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getColor() {
        return color;
    }

    public float getIntensity() {
        return intensity;
    }

    public float getAmbientIntensity() {
        return ambientIntensity;
    }

    public Sun getSun() {
        return sun;
    }

    public Moon getMoon() {
        return moon;
    }
}