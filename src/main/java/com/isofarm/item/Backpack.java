package com.isofarm.item;

import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;
import com.isofarm.wrld.GameMaster;

public class Backpack extends Tool {

    public Backpack(ToolType type, Tier tier) {
        super((byte) 2, tier.getName() + " Backpack", 100, type, tier,
                tier.getDurability() + type.getBaseDurability());
    }

    public Backpack() {
        this(ToolType.BACKPACK, Tier.LEATHER);
    }

    @Override
    public Item copy() {
        return new Backpack(ToolType.BACKPACK, Tier.LEATHER);
    }

    public void use(GameMaster gameMaster) {
        setPlayer(gameMaster.getPlayer());
        if (!gameMaster.isChatOpen()) {
            gameMaster.toggleInventory();
        }
    }
}
