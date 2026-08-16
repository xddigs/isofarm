package com.tilled.service;

import com.tilled.data.*;
import com.tilled.wrld.GameMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class Library implements Service<GameMaster> {
    private static final Logger log = LoggerFactory.getLogger(Library.class);
    private static final String DEFAULT_ID = "tilled";

    public static void initItems(ItemRegistry itemR, Player player) {
        itemR.register(DEFAULT_ID + ":coin", Coin::new);
        itemR.register(DEFAULT_ID + ":wheat_seed", Seed::new);
        itemR.register(DEFAULT_ID + ":carrot_seed", () -> new Seed(CropType.CARROT));
        itemR.register(DEFAULT_ID + ":potato_seed", () -> new Seed(CropType.POTATO));
        itemR.register(DEFAULT_ID + ":beetroot_seed", () -> new Seed(CropType.BEETROOT));
        itemR.register(DEFAULT_ID + ":dirt", () -> new Block(BlockData.DIRT));
        itemR.register(DEFAULT_ID + ":tilled_dirt", () -> new Block(BlockData.TILLED_DIRT));
        itemR.register(DEFAULT_ID + ":grass", () -> new Block(BlockData.GRASS));
        itemR.register(DEFAULT_ID + ":stone", () -> new Block(BlockData.STONE));
        itemR.register(DEFAULT_ID + ":dispenser", () -> new Block(BlockData.DISPENSER));
    }

    public static void initCommands(float delta, CommandRegistry cr, ItemRegistry ir,
                                    WeatherService weatherService, Player player) {
        cr.register(new Command("add", new String[]{"item", "amount"}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }

            if (args.length < 2) {
                log.warn("Usage: add <namespace:item> <amount>");
                return;
            }

            String itemId = args[0];
            if (itemId.isEmpty()) {
                log.warn("Item ID cannot be empty.");
                return;
            }

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

            if (itemId.equals(DEFAULT_ID + ":coin")) {
                player.earn(amount);
            } else {
                player.add(item, amount);
            }
            log.info("Command add executed: {} x{}", itemId, amount);
        }));

        cr.register(new Command("rain", new String[]{"start", "amount"}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }

            if (args.length < 1) {
                log.warn("Usage: rain start/stop <amount>");
                return;
            }

            int amount = 0;
            if (args.length == 2 && args[0].equals("start")) {
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
            } else if (args.length == 1 && args[0].equals("stop")) {
                weatherService.setWeather(WeatherType.CLEAR);
                log.info("Command rain executed");
                return;
            }

            weatherService.setWeather(delta, WeatherType.RAIN, true, amount);
            log.info("Command rain executed");

        }));
    }
}
