package com.tilled.data;

import com.tilled.entity.Entity;
import org.joml.Vector3f;

@DataClass
public class ItemWorld extends Entity {
    private final Item item;
    private int amount;
    private Vector3f position;
    private Vector3f velocity;

    public ItemWorld(Item item, int amount, Vector3f position) {
        super(item.getName());
        this.item = item;
        this.amount = amount;
        this.position = position;
    }

    @Override
    public void update(float delta) {

    }

    public Item getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
    }

    public void setVelocity(float x, float y, float z) {
        this.velocity.set(x, y, z);
    }

    public void addVelocity(float x, float y, float z) {
        this.velocity.add(x, y, z);
    }
}
