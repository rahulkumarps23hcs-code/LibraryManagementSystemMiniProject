package com.Libraryapp.service;

import java.util.List;
import java.util.Map;

import com.Libraryapp.exception.LibraryException;
import com.Libraryapp.model.Book;
import com.Libraryapp.model.IssueRecord;
import com.Libraryapp.model.Member;
import com.Libraryapp.model.User;
import com.Libraryapp.ops.LibraryOperations;

public interface LibraryService extends LibraryOperations {

    void registerMember(Member member) throws LibraryException;

    User login(String email, String password, String role) throws LibraryException;

   
    void issueBook(int userId, int bookId) throws LibraryException;

    double returnBook(int userId, int bookId) throws LibraryException;

    List<IssueRecord> viewIssuedBooks(int userId) throws LibraryException;

    List<IssueRecord> viewHistory(int userId) throws LibraryException;

    double viewTotalFine(int userId) throws LibraryException;

    void addBook(Book book) throws LibraryException;

    void updateBook(Book book) throws LibraryException;

    void deleteBook(int bookId) throws LibraryException;

    void setBookActive(int bookId, boolean active) throws LibraryException;

    Book findBook(int bookId) throws LibraryException;

    List<User> viewAllMembers() throws LibraryException;

    List<User> searchMembers(String keyword) throws LibraryException;

    List<IssueRecord> viewAllIssuedBooks() throws LibraryException;

    List<IssueRecord> viewAllTransactions() throws LibraryException;

    List<IssueRecord> viewOverdueRecords() throws LibraryException;

    Map<String, List<Book>> viewBooksByCategory() throws LibraryException;

    Map<String, Long> viewCategoryReport() throws LibraryException;
}
