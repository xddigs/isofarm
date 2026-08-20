package com.soilcraft.data;

import org.joml.Vector3f;

@DataClass
public record Ray(Vector3f origin, Vector3f direction) {}
