package com.isofarm.item;

import com.isofarm.data.*;
import com.isofarm.entity.Player;
import com.isofarm.gui.BookUI;
import com.isofarm.input.Keyboard;
import com.isofarm.service.BookService;
import com.isofarm.service.SoundService;
import com.isofarm.wrld.GameMaster;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Provides book behavior.
 */
public class Book extends Usable {
    private final List<Page> pages;
    private int currentPage;
    private boolean isOpen = false;
    private final boolean hasContent;

    /**
     * Creates a new {@code Book} instance.
     * @param hasContent the has content value
     */
    public Book(boolean hasContent) {
        super(Usables.BOOK, "Book");
        this.pages = new ArrayList<>();
        this.hasContent = hasContent;
    }

    /**
     * Creates a new {@code Book} instance.
     */
    public Book() {
        super(Usables.CRAFTING_BOOK, "Crafting Book");
        this.pages = new ArrayList<>();
        this.hasContent = true;
    }

    /**
     * Performs the use operation.
     * @param gameMaster the game master value
     * @param isCtrlHeld the is ctrl held value
     * @return the use result
     */
    @Override
    public boolean use(GameMaster gameMaster, boolean isCtrlHeld) {
        if (!hasContent) return false;
        if (isOpen) {
            BookService.bs.close();
        } else {
            BookService.bs.open(this);
        }
        return true;
    }

    /**
     * Updates the current state.
     */
    @Override
    public void update() {
        if (Keyboard.isKeyPressed(GLFW_KEY_LEFT)) previousPage();
        if (Keyboard.isKeyPressed(GLFW_KEY_RIGHT)) nextPage();
    }


    /**
     * Performs the copy operation.
     * @return the copy result
     */
    @Override
    public Item copy() {
        return new Book(hasContent);
    }

    /**
     * Performs the enchanting operation.
     * @param enchantment the enchantment value
     * @return the enchanting result
     */
    @Override
    public boolean enchanting(Enchantment enchantment) {
        return false;
    }

    /**
     * Performs the open operation.
     */
    public void open() {
        isOpen = true;
    }

    /**
     * Performs the close operation.
     */
    public void close() {
        isOpen = false;
    }

    /**
     * Returns the pages.
     * @return the pages
     */
    public List<Page> getPages() {
        return pages;
    }

    /**
     * Adds the page.
     * @param page the page value
     */
    public void addPage(Page page) {
        pages.add(page);
    }

    /**
     * Returns the page.
     * @param index the index value
     * @return the page
     */
    public Page getPage(int index) {
        return pages.get(index);
    }

    /**
     * Removes the page.
     * @param page the page value
     */
    public void removePage(Page page) {
        pages.remove(page);
    }

    /**
     * Clears the pages.
     */
    public void clearPages() {
        pages.clear();
    }

    /**
     * Checks whether the closed condition is met.
     * @return {@code true} if closed; otherwise {@code false}
     */
    public boolean isClosed() {
        return !isOpen;
    }

    /**
     * Checks whether the content condition is met.
     * @return {@code true} if content; otherwise {@code false}
     */
    public boolean hasContent() {
        return hasContent;
    }

    /**
     * Returns the current page.
     * @return the current page
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Performs the next page operation.
     */
    public void nextPage() {
        if (hasNextPage()) {
            currentPage += 2;
            SoundService.fx.playUseSound(SoundGroup.BOOKS);
            BookUI.bui.nextPage();
        }
    }

    /**
     * Performs the previous page operation.
     */
    public void previousPage() {
        if (hasPreviousPage()) {
            currentPage -= 2;
            SoundService.fx.playUseSound(SoundGroup.BOOKS);
            BookUI.bui.previousPage();
        }
    }

    /**
     * Performs the navigate to operation.
     * @param targetPage the target page value
     */
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
            BookUI.bui.nextPage();
        } else if (targetPage < currentPage) {
            currentPage = targetPage;
            SoundService.fx.playUseSound(SoundGroup.BOOKS);
            BookUI.bui.previousPage();
        }
    }

    /**
     * Checks whether the next page condition is met.
     * @return {@code true} if next page; otherwise {@code false}
     */
    public boolean hasNextPage() {
        return currentPage + 2 < pages.size();
    }

    /**
     * Checks whether the previous page condition is met.
     * @return {@code true} if previous page; otherwise {@code false}
     */
    public boolean hasPreviousPage() {
        return currentPage - 2 >= 0;
    }

    /**
     * Performs the reload operation.
     * @param player the player value
     */
    public void reload(Player player) {}
}