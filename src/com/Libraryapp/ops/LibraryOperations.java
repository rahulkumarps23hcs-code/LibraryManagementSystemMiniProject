package com.Libraryapp.ops;

import java.util.List;

import com.Libraryapp.exception.LibraryException;
import com.Libraryapp.model.Book;

public interface LibraryOperations {

    List<Book> searchBooks(String keyword) throws LibraryException;

    List<Book> viewAvailableBooks() throws LibraryException;

    List<Book> viewAllBooksSorted(String sortBy) throws LibraryException;
}
