package com.sfarm4j.data;

import com.sfarm4j.service.TimeService;
import com.sfarm4j.utils.K;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DataClass
public class Shop {
    private static final Logger log = LoggerFactory.getLogger(Shop.class);
    private final String owner;
    private final Inventory stock;
    private int money;

    public Shop() {
        this.owner = getRandomName();
        this.stock = new Inventory();
        this.money = K.World.SHOP_STARTING_CREDIT;
        setUpStock();
    }

    public String getOwner() {
        return owner;
    }

    public Inventory getStock() {
        return stock;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public void earn(int amount) {
        this.money += amount;
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
        if (!hasMoney()) return;
        if (money < item.getValue() * amount) {
            log.warn("Not enough money to buy x{} of {}", amount, item.getName());
            return;
        }
        stock.add(item, amount);
        earn(-amount);
        log.info("Bought x{} of {} from player", amount, item.getName());
    }

    public void clear() {
        stock.clear();
        log.info("Cleared shop stock");
    }

    public boolean hasMoney() {
        return money > 0;
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

    private void setUpStock() {
        clear();
        add(new Seed(CropType.CARROT), 64);
        add(new Seed(CropType.POTATO), 16);
    }

    public void reset() {
        this.money = K.World.SHOP_STARTING_CREDIT;
        setUpStock();
    }

    private String getRandomName() {
        String[] names = {"John", "Michael", "Jeffrey", "Hugh",
                          "Angela", "Malenia", "Carol", "Ashley"};
        return names[(int) (Math.random() * names.length)];
    }
}
