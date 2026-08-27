package com.isofarm.service;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.item.*;
import com.isofarm.utils.ToastFactory;
import com.isofarm.wrld.GameMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

@SuppressWarnings("all")
public class Library implements Service<GameMaster> {
    public static final String DEFAULT_ID = "isofarm";
    public static final char SEPARATOR = ':';
    private static final Logger log = LoggerFactory.getLogger(Library.class);

    public static void initItems(ItemRegistry itemR, Player player) {
        for (MaterialID material : MaterialID.values()) {
            if (material.equals(MaterialID.INGOT) || material.equals(MaterialID.RAW_ORE)) continue;
            registerDefault(itemR, () -> new Material(Tier.NONE, material));
        }

        Tier.forEach(tier -> {
            if (tier.isInvalidTier()) return;
            registerDefault(itemR, () -> new MiningComponent(tier, MaterialID.RAW_ORE));
            registerDefault(itemR, () -> new MiningComponent(tier, MaterialID.INGOT));
        });

        Tier.forEach(tier -> {
            if (tier.equals(Tier.NONE) || tier.equals(Tier.WOOD)) return;
            registerDefault(itemR, () -> new CraftingKit(ToolType.CRAFTING_KIT, tier));
            registerDefault(itemR, () -> new Backpack(ToolType.BACKPACK, tier));
        });

        Tier.forEach(tier -> {
            if (tier.equals(Tier.NONE) || tier.equals(Tier.LEATHER)) return;
            registerDefault(itemR, () -> new Bucket(BlockData.AIR, tier));
            registerDefault(itemR, () -> new Hoe(tier));
            registerDefault(itemR, () -> new Pickaxe(tier));
            registerDefault(itemR, () -> new Shovel(tier));
            registerDefault(itemR, () -> new Axe(tier));
            registerDefault(itemR, () -> new Sword(tier));
        });

        for (CropType type : new CropType[]{
                CropType.WHEAT, CropType.CARROT,
                CropType.POTATO, CropType.BEETROOT}) {
            registerDefault(itemR, () -> new Seed(type));
            registerDefault(itemR, () -> new Produce(type));
        }

        for (BlockData block : BlockData.values()) {
            if (block.getId() > 0) {
                registerDefault(itemR, () -> new Block(block));
            }
        }
    }

    private static void registerDefault(ItemRegistry itemR, Supplier<Item> supplier) {
        Item item = supplier.get();
        itemR.register(getFormattedName(DEFAULT_ID, String.valueOf(SEPARATOR), item.getName().trim()), supplier);
    }

    public static void initCommands(float delta, GameMaster gameMaster) {
        Player player = gameMaster.getPlayer();
        CommandRegistry cr = gameMaster.getCommandRegistry();
        ItemRegistry ir = gameMaster.getItemRegistry();
        WeatherService weatherService = gameMaster.getWeatherService();

        cr.register(new Command("/add", new CommandArgument[]{dynamic("item", ir::getIds), new CommandArgument("amount")}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }
            if (args.length < 2) {
                log.warn("Usage: /add <namespace:item> <amount>");
                return;
            }
            String itemId = args[0];
            if (itemId == null || itemId.isBlank()) {
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
            if (itemId.equals(null)) {
                player.earn(amount);
            } else if (!player.hasSpace()) {
                player.addToBackpack(item, amount);
            } else if (player.hasSpace()){
                player.add(item, amount);
            }
            log.info("Command add executed: {} x{}", itemId, amount);
            ToastFactory.success("You added " + amount + " " + item.getName() + " to your inventory!");
        }));

        cr.register(new Command("/rain", new CommandArgument[]{literal("action", "start", "stop")}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }
            if (args.length < 1) {
                log.warn("Usage: /rain <action>");
                return;
            }
            switch (args[0].toLowerCase()) {
                case "start" -> {
                    weatherService.setWeather(WeatherType.RAIN);
                    log.info("Command rain executed");
                    ToastFactory.success("You started the rain.");
                }
                case "stop" -> {
                    weatherService.setWeather(WeatherType.CLEAR);
                    log.info("Command rain executed");
                    ToastFactory.success("You stopped the rain.");
                }
                default -> log.warn("Unknown rain action: {}", args[0]);
            }
        }));

        cr.register(new Command("/time", new CommandArgument[]{literal("action", "set"), new CommandArgument("amount")}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }
            if (args.length < 2) {
                log.warn("Usage: /time set <amount>");
                return;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                log.warn("Invalid amount: {}", args[1]);
                return;
            }
            if (amount < 0 || amount > 24000) {
                log.warn("Amount must be between 0 and 24000.");
                return;
            }
            gameMaster.getTimeService().setTimeScale(amount / 24000.0f);
        }));

        cr.register(new Command("/gm", new CommandArgument[]{dynamic("mode", () -> Arrays.stream(Gamemode.values()).map(Enum::name).toList())}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }
            if (args.length < 1) {
                log.warn("Usage: /gm <mode>");
                return;
            }
            Gamemode targetMode = Gamemode.fromString(args[0]);
            if (targetMode == null) {
                log.warn("Invalid gamemode: {}", args[0]);
                return;
            }
            player.setGamemode(targetMode);
            if (targetMode.isNoClip()) gameMaster.toggleHUD();
            log.info("Command gamemode executed: {}", targetMode);
            ToastFactory.success("You changed gamemode to " + targetMode.name().toLowerCase());
        }));

        cr.register(new Command("/gamerule", new CommandArgument[]{dynamic("rule", () -> GameRules.getRules().keySet()), new CommandArgument("value")}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }
            if (args.length == 0) {
                ToastFactory.info("Available gamerules:");
                GameRules.getRules().forEach((rule, value) -> ToastFactory.info(rule + " = " + value));
                return;
            }
            String rule = args[0];
            if (!GameRules.exists(rule)) {
                log.warn("Unknown gamerule: {}", rule);
                ToastFactory.error("Unknown gamerule: " + rule);
                return;
            }
            if (args.length == 1) {
                Object value = GameRules.get(rule);
                log.info("Gamerule {} = {}", rule, value);
                ToastFactory.info(rule + " = " + value);
                return;
            }
            String valueString = args[1];
            Object currentValue = GameRules.get(rule);
            Object newValue;
            try {
                if (currentValue instanceof Boolean) {
                    if (!valueString.equalsIgnoreCase("true") && !valueString.equalsIgnoreCase("false")) {
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
                ToastFactory.error("Invalid value for " + rule);
                return;
            }
            try {
                GameRules.set(rule, newValue);
                log.info("Gamerule changed: {} = {}", rule, newValue);
                ToastFactory.success("Gamerule " + rule + " was set to " + newValue);
            } catch (IllegalArgumentException e) {
                log.warn("Could not set gamerule {}: {}", rule, e.getMessage());
                ToastFactory.error(e.getMessage());
            }
        }));

        cr.register(new Command("/kill", new CommandArgument[]{}, args -> {
            if (player == null) {
                log.warn("Cannot execute command: player does not exist.");
                return;
            }
            player.setHitpoints(-player.getHitpoints());
            log.info("Command kill executed");
            ToastFactory.success("You killed yourself!");
        }));
    }

    private static CommandArgument literal(String name, String... values) {
        return CommandArgument.of(name, (text, cursorPosition) -> {
            String prefix = text == null ? "" : text.substring(0, Math.min(cursorPosition, text.length()));
            return Arrays.stream(values).filter(value -> value.regionMatches(true, 0, prefix, 0, prefix.length())).sorted(String.CASE_INSENSITIVE_ORDER).toList();
        });
    }

    private static CommandArgument dynamic(String name, Supplier<Collection<String>> supplier) {
        return CommandArgument.of(name, (text, cursorPosition) -> {
            String prefix = text == null ? "" : text.substring(0, Math.min(cursorPosition, text.length()));
            Collection<String> values = supplier.get();
            if (values == null) {
                return java.util.List.of();
            }
            return values.stream().filter(value -> value != null).filter(value -> value.regionMatches(true, 0, prefix, 0, prefix.length())).sorted(String.CASE_INSENSITIVE_ORDER).toList();
        });
    }

    public static String getFormattedName(String... name) {
        StringBuilder builder = new StringBuilder();
        for (String s : name) {
            builder.append(s);
        }
        return builder.toString().trim()
                .replaceAll(" ", "_").toLowerCase();
    }
}