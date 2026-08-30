package com.isofarm.item;

import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;
import com.isofarm.wrld.GameMaster;

public abstract class Usable extends Material {

    public Usable(Tier tier, MaterialID materialID) {
        super(tier, materialID);
    }

    public abstract void use(GameMaster gameMaster);
    public abstract void update();
}
