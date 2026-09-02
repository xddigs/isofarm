package com.isofarm.item;

import com.isofarm.data.Usables;
import com.isofarm.entity.Player;
import com.isofarm.wrld.GameMaster;

public abstract class Usable implements Craftable, Enchantable {
    private final Usables usablesID;
    private final byte id;
    private final int value;
    private String name;
    private Player player;

    public Usable(Usables usablesID) {
        this.usablesID = usablesID;
        this.id = usablesID.getId();
        this.value = usablesID.getValue();
    }

    public Usable(Usables usablesID, String name) {
        this(usablesID);
        this.name = name;
    }

    @Override
    public byte getId() {
        return id;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public Usables getUsablesID() {
        return usablesID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public abstract boolean use(GameMaster gameMaster, boolean isCtrlHeld);
    public abstract void update();
}
