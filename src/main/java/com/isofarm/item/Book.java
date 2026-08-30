package com.isofarm.item;

import com.isofarm.data.MaterialID;
import com.isofarm.data.Tier;
import com.isofarm.service.BookService;
import com.isofarm.wrld.GameMaster;

import java.util.ArrayList;
import java.util.List;

public class Book extends Usable {
    private final List<Page> pages;
    private int currentPage;
    private boolean isOpen = false;
    private final boolean hasContent;

    public Book(boolean hasContent) {
        super(Tier.NONE, MaterialID.BOOK);
        this.pages = new ArrayList<>();
        this.hasContent = hasContent;
    }

    public Book(Tier tier, MaterialID materialID, boolean hasContent) {
        super(tier, materialID);
        this.hasContent = hasContent;
        this.pages = new ArrayList<>();
    }

    @Override
    public void use(GameMaster gameMaster) {
        if (!hasContent) return;
        if (isOpen) {
            BookService.bs.close();
        } else {
            BookService.bs.open(this);
        }
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
    public void update() {}

    public boolean hasContent() {
        return hasContent;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void nextPage() {
        if (currentPage + 1 < pages.size()) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        }
    }

    public boolean hasNextPage() {
        return currentPage + 1 < pages.size();
    }

    public boolean hasPreviousPage() {
        return currentPage > 0;
    }
}
