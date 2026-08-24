package com.isofarm.item;

import com.isofarm.data.SoundGroup;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;
import com.isofarm.entity.Player;
import com.isofarm.wrld.GameMaster;

public class Backpack extends Tool {

    public Backpack(ToolType type, Tier tier) {
        super((byte) 1, tier.getName() + " Backpack", 100, type, tier,
                tier.getDurability() + type.getBaseDurability());
    }

    public Backpack() {
        this(ToolType.BACKPACK, Tier.LEATHER);
    }

    @Override
    public Item copy() {
        return new Backpack(ToolType.BACKPACK, Tier.LEATHER);
    }

    public boolean use(GameMaster gameMaster) {
        Player player = gameMaster.getPlayer();
        if (player == null) return false;

        setPlayer(player);

        if (!player.getInventory().hasBackpackEquipped()) {
            gameMaster.getSoundService().playUseSound(SoundGroup.ITEMS);
            player.getInventory().equipBackpack(this);
            gameMaster.getGameUIService().resetHotbarPosition();
            return true;
        }

        if (!gameMaster.isChatOpen()) {
            gameMaster.toggleInventory();
        }

        return false;
    }

    public void unequip(GameMaster gameMaster) {
        if (getPlayer() != null && getPlayer().getInventory().hasBackpackEquipped()) {
            gameMaster.getSoundService().playUseSound(SoundGroup.ITEMS);
            getPlayer().getInventory().unequipBackpack();
            getPlayer().getGameMaster().getGameUIService().resetHotbarPosition();
        }
    }
}