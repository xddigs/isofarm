package com.isofarm.service;

import com.isofarm.data.Singleton;
import com.isofarm.graphics.ResourceManager;
import com.isofarm.gui.BookUI;
import com.isofarm.item.Book;

@Singleton
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
        BookUI.bui.open();
    }

    public void close() {
        if (openedBook == null) {
            return;
        }
        BookUI.bui.close();
    }

    public void update() {
        if (openedBook != null && !BookUI.bui.isAnimating()) {
            openedBook.update();
            BookUI.bui.update(openedBook,
                    ResourceManager.rem.getBookAnimationSheet());
        }

        if (BookUI.bui.isClosed()) {
            if (openedBook != null) {
                openedBook.close();
            }
            openedBook = null;
        }
    }
}