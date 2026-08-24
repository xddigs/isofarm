package com.isofarm.service;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.item.*;
import com.isofarm.wrld.GameMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class Library implements Service<GameMaster> {
    private static final Logger log = LoggerFactory.getLogger(Library.class);
    private static final String DEFAULT_ID = "main";

    public static void initItems(ItemRegistry itemR, Player player) {
        itemR.register(DEFAULT_ID + ".stick", () -> new Material(MaterialID.STICK));
        itemR.register(DEFAULT_ID + ".wood", () -> new Material(MaterialID.WOOD));
        itemR.register(DEFAULT_ID + ".stone", () -> new Material(MaterialID.STONE));

        itemR.register(DEFAULT_ID + ".backpack", Backpack::new);
        itemR.register(DEFAULT_ID + ".coin", Coin::new);
        itemR.register(DEFAULT_ID + ".wooden_hoe", () -> new Hoe());
        itemR.register(DEFAULT_ID + ".wooden_pickaxe", () -> new Pickaxe());
        itemR.register(DEFAULT_ID + ".wooden_shovel", () -> new Shovel());
        itemR.register(DEFAULT_ID + ".wooden_axe", () -> new Axe());
        itemR.register(DEFAULT_ID + ".wooden_sword", () -> new Sword());

        itemR.register(DEFAULT_ID + ".copper_backpack", () -> new Backpack(ToolType.BACKPACK, Tier.COPPER));
        itemR.register(DEFAULT_ID + ".copper_hoe", () -> new Hoe(Tier.COPPER));
        itemR.register(DEFAULT_ID + ".copper_pickaxe", () -> new Pickaxe(Tier.COPPER));
        itemR.register(DEFAULT_ID + ".copper_shovel", () -> new Shovel(Tier.COPPER));
        itemR.register(DEFAULT_ID + ".copper_axe", () -> new Axe(Tier.COPPER));
        itemR.register(DEFAULT_ID + ".copper_sword", () -> new Sword(Tier.COPPER));

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
        itemR.register(DEFAULT_ID + ".oak_leaves", () -> new Block(BlockData.OAK_LEAVES));
        itemR.register(DEFAULT_ID + ".snow", () -> new Block(BlockData.SNOW));

        itemR.register(MaterialID.WOOD, () -> new Block(BlockData.OAK_WOOD));
        itemR.register(MaterialID.STICK, () -> new Material(MaterialID.STICK));
        itemR.register(MaterialID.STONE, () -> new Block(BlockData.STONE));
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

            if (itemId.equals(DEFAULT_ID + ".coin")) {
                player.earn(amount);
            } else {
                player.add(item, amount);
            }
            log.info("Command add executed: {} x{}", itemId, amount);
            gameMaster.getToastService().success("You added " + amount + " " +
                    item.getName() + " to your inventory!");
        }));

        cr.register(new Command("/rain", new String[]{"start"}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }

            if (args.length < 1) {
                log.warn("Usage: /rain start/stop");
                return;
            }

            if (args[0].equals("start")) {
                if (args.length == 1) {
                    weatherService.setWeather(WeatherType.RAIN);
                    log.info("Command rain executed");
                    gameMaster.getToastService().success("You started the rain.");
                }
            } else if (args.length == 1 && args[0].equals("stop")) {
                weatherService.setWeather(WeatherType.CLEAR);
                log.info("Command rain executed");
                gameMaster.getToastService().success("You stopped the rain.");
                return;
            }
        }));

        cr.register(new Command("/time", new String[]{"set", "amount"}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }

            if (args.length < 2) {
                log.warn("Usage: /time set <amount>");
            }

            int amount = 0;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                log.warn("Invalid amount: {}", args[1]);
                return;
            }

            if (amount < 0 || amount > 24000) {
                log.warn("Amount must be between 0 and 24000.");
            }

            gameMaster.getTimeService().setTimeScale(amount / 24000.0f);
        }));

        cr.register(new Command("/gm", new String[]{"mode"}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }

            if (args.length < 1) {
                log.warn("Usage: /gm mode");
            }

            Gamemode targetMode = Gamemode.fromString(args[0]);
            if (targetMode == null) {
                log.warn("Invalid gamemode: {}", args[0]);
                return;
            }

            log.info("Command gm executed");
            gameMaster.getPlayer().setGamemode(targetMode);
            gameMaster.getToastService().success("You changed gamemode to " +
                    args[0].toLowerCase());
        }));

        cr.register(new Command("/gamerule", new String[]{"rule", "value"}, args -> {

            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }

            if (args.length == 0) {
                gameMaster.getToastService().info("Available gamerules:");
                GameRules.getRules().forEach((rule, value) ->
                        gameMaster.getToastService().info(rule + " = " + value));
                return;
            }

            String rule = args[0];

            if (!GameRules.exists(rule)) {
                log.warn("Unknown gamerule: {}", rule);
                gameMaster.getToastService().error("Unknown gamerule: " + rule);
                return;
            }

            if (args.length == 1) {
                Object value = GameRules.get(rule);
                log.info("Gamerule {} = {}", rule, value);
                gameMaster.getToastService().info(rule + " = " + value);
                return;
            }

            String valueString = args[1];
            Object currentValue = GameRules.get(rule);
            Object newValue;

            try {
                if (currentValue instanceof Boolean) {
                    if (!valueString.equalsIgnoreCase("true") &&
                            !valueString.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("Expected true or false");
                    }
                    newValue = Boolean.parseBoolean(valueString);
                } else if (currentValue instanceof Integer) {
                    newValue = Integer.parseInt(valueString);
                } else if (currentValue instanceof Float) {
                    newValue = Float.parseFloat(valueString);
                } else {
                    throw new IllegalArgumentException("Unsupported gamerule type");
                }

            } catch (IllegalArgumentException e) {
                log.warn("Invalid value for gamerule {}: {}", rule, valueString);
                gameMaster.getToastService().error("Invalid value for " + rule);
                return;
            }

            try {
                GameRules.set(rule, newValue);
                log.info("Gamerule changed: {} = {}", rule, newValue);
                gameMaster.getToastService().success("Gamerule " + rule + " was set to " + newValue);
            } catch (IllegalArgumentException e) {
                log.warn("Could not set gamerule {}: {}", rule, e.getMessage());
                gameMaster.getToastService().error(e.getMessage());
            }
        }));

        cr.register(new Command("/kill", new String[]{}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }
            player.setHitpoints(-player.getHitpoints());
            log.info("Command kill executed");
            gameMaster.getToastService().success("You killed yourself!");
        }));
    }
}
