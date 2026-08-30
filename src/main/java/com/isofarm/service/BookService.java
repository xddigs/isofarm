package com.isofarm.service;
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
        if (book == null) {
            return;
        }

        if (openedBook != null) {
            openedBook.close();
        }

        openedBook = book;
        openedBook.open();
    }

    public void close() {
        if (openedBook == null) {
            return;
        }

        openedBook.close();
        openedBook = null;
    }

    public void update() {
        if (openedBook != null) {
            openedBook.update();
        }
    }
}