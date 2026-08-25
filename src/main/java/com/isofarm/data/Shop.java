package com.isofarm.data;

import com.isofarm.entity.Player;
import com.isofarm.item.*;
import com.isofarm.service.TimeService;
import com.isofarm.utils.K;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DataClass
public class Shop {
    private static final Logger log = LoggerFactory.getLogger(Shop.class);
    private final String owner;
    private final Inventory stock;
    private final Purse purse;
    private Player player;

    public Shop() {
        this.owner = getRandomName();
        this.stock = new Inventory();
        this.purse = new Purse();
        setUpStock();
        this.purse.add(K.World.STARTING_COINS);
    }

    private void setUpStock() {
        clear();
        add(new Seed(CropType.CARROT), 64);
        add(new Seed(CropType.POTATO), 16);
        add(new Seed(CropType.BEETROOT), 16);
        add(new Block(BlockData.TILLED_DIRT), 2);
        add(new Block(BlockData.GRASS), 8);
        add(new Hoe(), 1);
    }

    public String getOwner() {
        return owner;
    }

    public Inventory getStock() {
        return stock;
    }

    public Purse getPurse() {
        return purse;
    }

    public int purse() {
        return purse.getBalance();
    }

    public void earn(int amount) {
        purse.add(amount);
    }

    public void update(TimeService timeService) {
        if (timeService.getDay() % 10 == 0) {
            reset();
        }
    }

    public void add(Item item, int amount) {
        if (amount <= 0) return;
        stock.add(item, amount);
        log.info("Added x{} of {} to shop stock", amount, item.getName());
    }

    public void sell(Item item, int amount) {
        if (amount <= 0) return;
        stock.remove(item, amount);
        earn(amount);
        log.info("Sold x{} of {} to player", amount, item.getName());
    }

    public void buy(Item item, int amount) {
        int totalPrice = item.getValue() * amount;
        if (!hasMoney() || purse() < totalPrice) {
            log.warn("Not enough money to buy x{} of {}", amount, item.getName());
            return;
        }
        stock.add(item, amount);
        purse.remove(totalPrice);
        log.info("Bought x{} of {} from player", amount, item.getName());
    }

    public void clear() {
        stock.clear();
        log.info("Cleared shop stock");
    }

    public boolean hasMoney() {
        return purse() > 0;
    }

    public boolean isEmpty() {
        return stock.isEmpty();
    }

    public int size() {
        return stock.size();
    }

    public Item get(int index) {
        return stock.get(index);
    }

    public int getAmount(Item item) {
        return stock.getAmount(item);
    }

    public void reset() {
        setUpStock();
        this.purse.add(K.World.STARTING_COINS);
    }

    private String getRandomName() {
        String[] names = {"John", "Michael", "Jeffrey", "Hugh",
                          "Angela", "Malenia", "Carol", "Ashley"};
        return names[(int) (Math.random() * names.length)];
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
