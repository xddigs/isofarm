package com.sfarm4j.service;

import com.sfarm4j.data.*;
import com.sfarm4j.wrld.GameMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class InitService implements Service<GameMaster> {
    private static final Logger log = LoggerFactory.getLogger(InitService.class);

    public static void initItems(ItemRegistry itemR) {
        itemR.register("farm:wheat_seed", Seed::new);
        itemR.register("farm:carrot_seed", () -> new Seed(CropType.CARROT));
        itemR.register("farm:potato_seed", () -> new Seed(CropType.POTATO));
        itemR.register("farm:beetroot", () -> new Seed(CropType.BEETROOT));
        itemR.register("farm:dirt", () -> new Block(BlockData.DIRT));
        itemR.register("farm:tilled_dirt", () -> new Block(BlockData.TILLED_DIRT));
        itemR.register("farm:grass", () ->  new Block(BlockData.GRASS));
        itemR.register("farm:stone", () -> new Block(BlockData.STONE));
        itemR.register("farm:dispenser", () -> new Block(BlockData.DISPENSER));
    }

    public static void initCommands(CommandRegistry cr,
                                    ItemRegistry ir,
                                    Player player) {
        cr.register(new Command("add",
                new String[]{"item", "amount"},
                args -> {
                    if (player == null) {
                        log.warn("Cannot execute command: player does not exist.");
                        return;
                    }

                    if (args.length < 2) {
                        log.warn("Usage: /add <namespace:item> <amount>");
                        return;
                    }

                    String itemId = args[0];
                    int amount;

                    try {
                        amount = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid amount: {}", args[1]);
                        return;
                    }

                    if (amount <= 0) {
                        log.warn("Amount must be greater than zero.");
                        return;
                    }

                    Item item = ir.create(itemId);

                    if (item == null) {
                        log.warn("Unknown item: {}", itemId);
                        return;
                    }

                    player.add(item, amount);
                    log.info("Command /add executed: {} x{}", itemId, amount);
                }
        ));
    }
}
