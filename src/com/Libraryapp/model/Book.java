package com.Libraryapp.model;

import java.util.Objects;

public class Book implements Comparable<Book> {

    private int bookId;
    private String title;
    private String author;
    private String category;
    private String isbn;
    private int quantity;
    private int availableQuantity;
    private boolean active = true;

    public Book() {
    }

    public Book(String title, String author, String category, String isbn, int quantity) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.quantity = quantity;
        this.availableQuantity = quantity;
    }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public int compareTo(Book other) {
        return this.title.compareToIgnoreCase(other.title);
    }

 
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        return Objects.equals(isbn, ((Book) o).isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return String.format("%-5d %-28s %-18s %-12s %-14s %-7s %s",
                bookId, title, author, category, isbn,
                availableQuantity + "/" + quantity,
                active ? "Active" : "Inactive");
    }

    public static String header() {
        return String.format("%-5s %-28s %-18s %-12s %-14s %-7s %s",
                "ID", "TITLE", "AUTHOR", "CATEGORY", "ISBN", "AVAIL", "STATUS");
    }
}
