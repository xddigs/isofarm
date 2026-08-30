package com.isofarm.service;

import com.isofarm.gui.BookUI;
import com.isofarm.input.Mouse;
import com.isofarm.item.Book;

public class BookService implements Service<Book> {
    public static final BookService bs = new BookService();
    private Book openedBook;

    private BookService() {}

    public Book getOpenedBook() {
        return openedBook;
    }

    public boolean isOpen() {
        return openedBook != null;
    }

    public void open(Book book) {
        if (book == null || openedBook != null) {
            return;
        }

        openedBook = book;
        book.open();
        BookUI.open();
    }

    public void close() {
        if (openedBook == null) {
            return;
        }
        BookUI.close();
    }

    public void update() {
        if (openedBook != null && !BookUI.isAnimating()) {
            openedBook.update();
        }

        if (BookUI.isClosed()) {
            if (openedBook != null) {
                openedBook.close();
            }
            openedBook = null;
        }
    }
}