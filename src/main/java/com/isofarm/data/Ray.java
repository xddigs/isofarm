package com.isofarm.data;

import org.joml.Vector3f;

/**
 * Stores ray data.
 */
@DataClass
public record Ray(Vector3f origin, Vector3f direction) {}
