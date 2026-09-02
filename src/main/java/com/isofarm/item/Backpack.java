package com.isofarm.item;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.service.SoundService;
import com.isofarm.wrld.GameMaster;

public class Backpack extends Usable implements Undroppable {

    public Backpack() {
        super(Usables.BACKPACK, "Backpack");
    }

    @Override
    public boolean use(GameMaster gameMaster,  boolean isCtrlHeld) {
        Player player = gameMaster.getPlayer();
        if (player == null) return false;
        setPlayer(player);

        if (!player.getInventory().hasBackpackEquipped()) {
            SoundService.fx.playUseSound(SoundGroup.ITEMS);
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
            SoundService.fx.playUseSound(SoundGroup.ITEMS);
            getPlayer().getInventory().unequipBackpack();
            getPlayer().getGameMaster().getGameUIService().resetHotbarPosition();
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