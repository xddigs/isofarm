package com.isofarm.data;

import com.isofarm.entity.Player;
import com.isofarm.wrld.GameMaster;

public interface PlayerState {
    void input(Player player, GameMaster gameMaster);
    void update(Player player, float delta);
    void enter(Player player);
    void exit(Player player);
}