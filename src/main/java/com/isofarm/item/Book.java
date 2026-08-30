package com.isofarm.item;

import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;
import com.isofarm.service.BookService;
import com.isofarm.wrld.GameMaster;

import java.util.ArrayList;
import java.util.List;

public class Book extends Usable {
    private final List<Page> pages;
    private boolean isOpen = false;

    public Book() {
        super(Tier.NONE, MaterialID.BOOK);
        this.pages = new ArrayList<>();
    }

    @Override
    public void use(GameMaster gameMaster) {
        BookService.bs.add(this);
    }

    public void open() {
        isOpen = true;
    }

    public void close() {
        isOpen = false;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void addPage(Page page) {
        pages.add(page);
    }

    public Page getPage(int index) {
        return pages.get(index);
    }

    public void removePage(Page page) {
        pages.remove(page);
    }

    public void clearPages() {
        pages.clear();
    }

    public boolean isClosed() {
        return !isOpen;
    }

    @Override
    public void update() {

    }
}
