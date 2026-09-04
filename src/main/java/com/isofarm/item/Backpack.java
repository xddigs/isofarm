package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Usables;
import com.isofarm.entity.Player;
import com.isofarm.utils.Local;
import com.isofarm.wrld.GameMaster;

/**
 * Provides backpack behavior.
 */
public class Backpack extends Usable implements Undroppable {

    /**
     * Creates a new {@code Backpack} instance.
     */
    public Backpack() {
        super(Usables.BACKPACK, Local.lang.t("item.usable.backpack"));
    }

    /**
     * Performs the use operation.
     * @param gameMaster the game master value
     * @param isCtrlHeld the is ctrl held value
     * @return the use result
     */
    @Override
    public boolean use(GameMaster gameMaster,  boolean isCtrlHeld) {
        Player player = Player.plyr;
        if (!player.getInventory().hasBackpackEquipped()) {
            player.getInventory().equipBackpack(this);
            gameMaster.getGameUIService().resetHotbarPosition();
            return true;
        }

        if (!gameMaster.isChatOpen()) {
            gameMaster.toggleInventory();
        }

        return false;
    }

    /**
     * Updates the current state.
     */
    @Override
    public void update() {}

    /**
     * Performs the unequip operation.
     */
    public void unequip() {
        if (Player.plyr.getInventory().hasBackpackEquipped()) {
            Player.plyr.getInventory().unequipBackpack();
            GameMaster.game.getGameUIService().resetHotbarPosition();
        }
    }


    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return new Backpack();
    }

    /**
     * Performs the enchanting operation.
     * @param enchantment the enchantment value
     * @return the enchanting result
     */
    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}
