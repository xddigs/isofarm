package com.isofarm.service;

import com.isofarm.data.Singleton;
import com.isofarm.gui.BookUI;
import com.isofarm.item.Book;

/**
 * Provides book service behavior.
 */
@Singleton
public class BookService implements Service<Book> {
    public static final BookService bs = new BookService();
    private Book openedBook;

    /**
     * Creates a new {@code BookService} instance.
     */
    private BookService() {}

    /**
     * Returns the opened book.
     * @return the opened book
     */
    public Book getOpenedBook() {
        return openedBook;
    }

    /**
     * Checks whether the open condition is met.
     * @return {@code true} if open; otherwise {@code false}
     */
    public boolean isOpen() {
        return openedBook != null;
    }

    /**
     * Performs the open operation.
     * @param book the book value
     */
    public void open(Book book) {
        if (book == null || openedBook != null) {
            return;
        }

        openedBook = book;
        book.open();
        BookUI.bui.open();
    }

    /**
     * Performs the close operation.
     */
    public void close() {
        if (openedBook == null) {
            return;
        }
        BookUI.bui.close();
    }

    /**
     * Updates the current state.
     */
    public void update() {
        if (openedBook != null && !BookUI.bui.isAnimating()) {
            openedBook.update();
        }

        if (BookUI.bui.isClosed()) {
            if (openedBook != null) {
                openedBook.close();
            }
            openedBook = null;
        }
    }
}