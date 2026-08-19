package com.tilled.service;

import com.tilled.data.*;
import com.tilled.wrld.GameMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class Library implements Service<GameMaster> {
    private static final Logger log = LoggerFactory.getLogger(Library.class);
    private static final String DEFAULT_ID = "main";

    public static void initItems(ItemRegistry itemR, Player player) {
        itemR.register(DEFAULT_ID + ".coin", Coin::new);
        itemR.register(DEFAULT_ID + ".can", WateringCan::new);
        itemR.register(DEFAULT_ID + ".hoe", Hoe::new);
        itemR.register(DEFAULT_ID + ".wheat_seed", Seed::new);
        itemR.register(DEFAULT_ID + ".carrot_seed", () -> new Seed(CropType.CARROT));
        itemR.register(DEFAULT_ID + ".potato_seed", () -> new Seed(CropType.POTATO));
        itemR.register(DEFAULT_ID + ".beetroot_seed", () -> new Seed(CropType.BEETROOT));
        itemR.register(DEFAULT_ID + ".wheat", () -> new Produce(CropType.WHEAT));
        itemR.register(DEFAULT_ID + ".carrot", () -> new Produce(CropType.CARROT));
        itemR.register(DEFAULT_ID + ".potato", () -> new Produce(CropType.POTATO));
        itemR.register(DEFAULT_ID + ".beetroot", () -> new Produce(CropType.BEETROOT));
        itemR.register(DEFAULT_ID + ".dirt", () -> new Block(BlockData.DIRT));
        itemR.register(DEFAULT_ID + ".tilled_dirt", () -> new Block(BlockData.TILLED_DIRT));
        itemR.register(DEFAULT_ID + ".grass", () -> new Block(BlockData.GRASS));
        itemR.register(DEFAULT_ID + ".stone", () -> new Block(BlockData.STONE));
        itemR.register(DEFAULT_ID + ".voidstone", () -> new Block(BlockData.VOIDSTONE));
        itemR.register(DEFAULT_ID + ".glass", () -> new Block(BlockData.GLASS));
        itemR.register(DEFAULT_ID + ".oak_log", () -> new Block(BlockData.OAK_LOG));
        itemR.register(DEFAULT_ID + ".oak_wood", () -> new Block(BlockData.OAK_WOOD));
        itemR.register(DEFAULT_ID + ".oak_leaves", () -> new Block(BlockData.LEAVES));
    }

    public static void initCommands(float delta, GameMaster gameMaster) {
        Player player = gameMaster.getPlayer();
        CommandRegistry cr = gameMaster.getCommandRegistry();
        ItemRegistry ir = gameMaster.getItemRegistry();
        WeatherService weatherService = gameMaster.getWeatherService();

        cr.register(new Command("/add", new String[]{"item", "amount"}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }

            if (args.length < 2) {
                log.warn("Usage: /add <namespace:item> <amount>");
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
            gameMaster.getToastService().success("You added " + amount + " " +
                    item.getName() + " to your inventory!");
        }));

        cr.register(new Command("/rain", new String[]{"start", "amount"}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }

            if (args.length < 1) {
                log.warn("Usage: /rain start/stop <amount>");
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
                gameMaster.getToastService().success("You stopped the rain.");
                return;
            }

            weatherService.setWeather(delta, WeatherType.RAIN, true, amount);
            log.info("Command rain executed");
            gameMaster.getToastService().success("You started the rain.");
        }));
    }
}
