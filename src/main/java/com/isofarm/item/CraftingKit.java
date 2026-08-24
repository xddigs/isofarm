package com.isofarm.item;

import com.isofarm.data.DataClass;
import com.isofarm.data.Tier;
import com.isofarm.data.ToolType;
import com.isofarm.wrld.GameMaster;

@DataClass
public class CraftingKit extends Tool {

    public CraftingKit(ToolType type, Tier tier) {
        super((byte) 0, tier.getName() + type.getName(), 50, type, tier,
                tier.getDurability() + type.getBaseDurability());
    }

    public CraftingKit() {
        this(ToolType.CRAFTING_KIT, Tier.LEATHER);
    }

    @Override
    public Item copy() {
        return new CraftingKit(ToolType.CRAFTING_KIT, Tier.LEATHER);
    }

    public boolean use(GameMaster gameMaster) {
        gameMaster.getGameUIService().addChatMessage("Crafting kit used!");
        return true;
    }
}
