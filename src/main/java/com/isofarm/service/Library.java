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
    public static final String DEFAULT_ID = "iso";
    public static final char SEPARATOR = ':';

    public static void initItems(ItemRegistry itemR, Player player) {
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Material(MaterialID.STICK).getName()), () -> new Material(MaterialID.STICK));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Material(MaterialID.WOOD).getName()), () -> new Material(MaterialID.WOOD));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Material(MaterialID.STONE).getName()), () -> new Material(MaterialID.STONE));

        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Backpack().getName()), Backpack::new);
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Coin().getName()), Coin::new);

        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Hoe().getName()), Hoe::new);
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Pickaxe().getName()), Pickaxe::new);
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Shovel().getName()), Shovel::new);
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Axe().getName()), Axe::new);
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Sword().getName()), Sword::new);


        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Backpack(ToolType.BACKPACK, Tier.COPPER).getName()), () -> new Backpack(ToolType.BACKPACK, Tier.COPPER));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Hoe(Tier.COPPER).getName()), () -> new Hoe(Tier.COPPER));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Pickaxe(Tier.COPPER).getName()), () -> new Pickaxe(Tier.COPPER));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Shovel(Tier.COPPER).getName()), () -> new Shovel(Tier.COPPER));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Axe(Tier.COPPER).getName()), () -> new Axe(Tier.COPPER));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Sword(Tier.COPPER).getName()), () -> new Sword(Tier.COPPER));


        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Seed().getName()), Seed::new);
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Seed(CropType.CARROT).getName()), () -> new Seed(CropType.CARROT));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Seed(CropType.POTATO).getName()), () -> new Seed(CropType.POTATO));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Seed(CropType.BEETROOT).getName()), () -> new Seed(CropType.BEETROOT));


        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Produce(CropType.WHEAT).getName()), () -> new Produce(CropType.WHEAT));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Produce(CropType.CARROT).getName()), () -> new Produce(CropType.CARROT));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Produce(CropType.POTATO).getName()), () -> new Produce(CropType.POTATO));
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Produce(CropType.BEETROOT).getName()), () -> new Produce(CropType.BEETROOT));


        for (BlockData block : BlockData.values()) {
            if (block == BlockData.AIR ||
                    block == BlockData.WATER ||
                    block == BlockData.CROP) {
                continue;
            }

            itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), new Block(block).getName()), () -> new Block(block));
        }


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

            if (itemId.equals(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR),
                    ToolType.COIN.getName()))) {
                player.earn(amount);
            } else {
                if (player.canAdd()) {
                    player.addToBackpack(item, amount);
                } else {
                    player.add(item, amount);
                }
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

    public static String getFormattedName(String... name) {
        StringBuilder builder = new StringBuilder();
        for (String s : name) {
            builder.append(s);
            builder.append(" ");
        }
        return builder.toString().trim().toLowerCase();
    }
}
