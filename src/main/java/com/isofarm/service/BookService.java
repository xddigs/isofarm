package com.isofarm.service;

import com.isofarm.item.Book;

import java.util.LinkedList;
import java.util.List;

public class BookService implements Service<Book> {
    public static final BookService bs = new BookService();
    private final List<Book> books = new LinkedList<>();

    private BookService() {}

    public List<Book> getBooks() {
        return books;
    }

    public void add(Book book) {
        books.add(book);
    }

    public Book get(int id) {
        return books.get(id);
    }

    public void remove(Book book) {
        books.remove(book);
    }

    public void update() {
        books.forEach(Book::update);
        books.removeIf(Book::isClosed);
    }

    public void render() {
    }
}
