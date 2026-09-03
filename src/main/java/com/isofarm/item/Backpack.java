package com.isofarm.item;

import com.isofarm.data.Enchantment;
import com.isofarm.data.Usables;
import com.isofarm.entity.Player;
import com.isofarm.utils.Local;
import com.isofarm.wrld.GameMaster;

public class Backpack extends Usable implements Undroppable {

    public Backpack() {
        super(Usables.BACKPACK, Local.lang.t("item.usable.backpack"));
    }

    @Override
    public boolean use(GameMaster gameMaster,  boolean isCtrlHeld) {
        Player player = gameMaster.getPlayer();
        if (player == null) return false;
        setPlayer(player);

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

    @Override
    public void update() {}

    public void unequip() {
        if (getPlayer() != null && getPlayer().getInventory().hasBackpackEquipped()) {
            getPlayer().getInventory().unequipBackpack();
            GameMaster.game.getGameUIService().resetHotbarPosition();
        }
    }


    @Override
    public Item copy() {
        return new Backpack();
    }

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}