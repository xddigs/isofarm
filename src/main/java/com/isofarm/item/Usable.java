package com.isofarm.item;

import com.isofarm.data.Usables;
import com.isofarm.entity.Player;
import com.isofarm.utils.Local;
import com.isofarm.wrld.GameMaster;

/**
 * Provides usable behavior.
 */
public abstract class Usable implements Craftable,
        Enchantable {
    private final Usables usablesID;
    private final byte id;
    private final int value;
    private String name;
    private Player player;

    /**
     * Creates a new {@code Usable} instance.
     * @param usablesID the usables id value
     */
    public Usable(Usables usablesID) {
        this.usablesID = usablesID;
        this.id = usablesID.getId();
        this.value = usablesID.getValue();
    }

    /**
     * Creates a new {@code Usable} instance.
     * @param usablesID the usables id value
     * @param name the name value
     */
    public Usable(Usables usablesID, String name) {
        this(usablesID);
        this.name = name;
    }

    /**
     * Returns the id.
     * @return the id
     */
    @Override
    public byte getId() {
        return id;
    }

    /**
     * Returns the value.
     * @return the value
     */
    @Override
    public int getValue() {
        return value;
    }

    /**
     * Returns the name.
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the display name.
     * @return the display name
     */
    public String getDisplayName() {
        return Local.lang.t("item.usable." + usablesID.name().toLowerCase());
    }

    /**
     * Returns the usables id.
     * @return the usables id
     */
    public Usables getUsablesID() {
        return usablesID;
    }

    /**
     * Sets the name.
     * @param name the name value
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the player.
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Sets the player.
     * @param player the player value
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Performs the use operation.
     * @param gameMaster the game master value
     * @param isCtrlHeld the is ctrl held value
     * @return the use result
     */
    public abstract boolean use(GameMaster gameMaster, boolean isCtrlHeld);
    /**
     * Updates the current state.
     */
    public abstract void update();
}
