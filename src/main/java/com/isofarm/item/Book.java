package com.isofarm.item;

import com.isofarm.data.*;
import com.isofarm.gui.BookUI;
import com.isofarm.input.Keyboard;
import com.isofarm.service.BookService;
import com.isofarm.service.SoundService;
import com.isofarm.wrld.GameMaster;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Book extends Usable {
    private final List<Page> pages;
    private int currentPage;
    private boolean isOpen = false;
    private final boolean hasContent;

    public Book(boolean hasContent) {
        super(Usables.BOOK, "Book");
        this.pages = new ArrayList<>();
        this.hasContent = hasContent;
    }

    public Book() {
        super(Usables.CRAFTING_BOOK, "Crafting Book");
        this.pages = new ArrayList<>();
        this.hasContent = true;
    }

    @Override
    public boolean use(GameMaster gameMaster) {
        if (!hasContent) return false;
        if (isOpen) {
            BookService.bs.close();
        } else {
            BookService.bs.open(this);
        }
        return true;
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
        if (Keyboard.isKeyPressed(GLFW_KEY_LEFT)) previousPage();
        if (Keyboard.isKeyPressed(GLFW_KEY_RIGHT)) nextPage();
    }

    public boolean hasContent() {
        return hasContent;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void nextPage() {
        if (hasNextPage()) {
            currentPage += 2;
            SoundService.fx.playUseSound(SoundGroup.BOOKS);
            BookUI.nextPage();
        }
    }

    public void previousPage() {
        if (hasPreviousPage()) {
            currentPage -= 2;
            SoundService.fx.playUseSound(SoundGroup.BOOKS);
            BookUI.previousPage();
        }
    }

    public void navigateTo(int targetPage) {
        if (targetPage < 0 || targetPage >= pages.size() || targetPage == currentPage) {
            return;
        }

        if (targetPage % 2 != 0) {
            targetPage--;
        }

        if (targetPage > currentPage) {
            currentPage = targetPage;
            SoundService.fx.playUseSound(SoundGroup.BOOKS);
            BookUI.nextPage();
        } else if (targetPage < currentPage) {
            currentPage = targetPage;
            SoundService.fx.playUseSound(SoundGroup.BOOKS);
            BookUI.previousPage();
        }
    }

    public boolean hasNextPage() {
        return currentPage + 2 < pages.size();
    }

    public boolean hasPreviousPage() {
        return currentPage - 2 >= 0;
    }

    @Override
    public Item copy() {
        return new Book(hasContent);
    }

    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }
}