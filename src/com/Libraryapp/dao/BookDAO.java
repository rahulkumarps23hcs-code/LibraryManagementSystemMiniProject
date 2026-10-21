package com.Libraryapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.Libraryapp.model.Book;
import com.Libraryapp.util.DBConnection;

public class BookDAO {

    public int addBook(Book b) throws SQLException {
        String sql = "INSERT INTO book(title,author,category,isbn,quantity,available_quantity,active) VALUES(?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, b.getTitle());
            ps.setString(2, b.getAuthor());
            ps.setString(3, b.getCategory());
            ps.setString(4, b.getIsbn());
            ps.setInt(5, b.getQuantity());
            ps.setInt(6, b.getAvailableQuantity());
            ps.setBoolean(7, b.isActive());
            return ps.executeUpdate();
        }
    }

    public int updateBook(Book b) throws SQLException {
        String sql = "UPDATE book SET title=?, author=?, category=?, isbn=?, quantity=?, available_quantity=? WHERE book_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, b.getTitle());
            ps.setString(2, b.getAuthor());
            ps.setString(3, b.getCategory());
            ps.setString(4, b.getIsbn());
            ps.setInt(5, b.getQuantity());
            ps.setInt(6, b.getAvailableQuantity());
            ps.setInt(7, b.getBookId());
            return ps.executeUpdate();
        }
    }

    public int deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM book WHERE book_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate();
        }
    }

    public int setActive(int bookId, boolean active) throws SQLException {
        String sql = "UPDATE book SET active=? WHERE book_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, bookId);
            return ps.executeUpdate();
        }
    }

    /** Returns null when no book has this id. */
    public Book findById(int bookId) throws SQLException {
        String sql = "SELECT * FROM book WHERE book_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBook(rs) : null;
            }
        }
    }

    public List<Book> getAllBooks() throws SQLException {
        return query("SELECT * FROM book", null);
    }

    public List<Book> getAvailableBooks() throws SQLException {
        return query("SELECT * FROM book WHERE available_quantity > 0 AND active = TRUE", null);
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        return query("SELECT * FROM book WHERE title LIKE ? OR author LIKE ? OR category LIKE ? OR isbn LIKE ?", keyword);
    }

    /**
     * Used inside a transaction: the "available_quantity > 0" condition makes
     * sure two members can never take the last copy at the same time.
     * Returns 0 if no copy was available.
     */
    public int decreaseAvailable(Connection con, int bookId) throws SQLException {
        String sql = "UPDATE book SET available_quantity = available_quantity - 1 WHERE book_id=? AND available_quantity > 0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate();
        }
    }

    public int increaseAvailable(Connection con, int bookId) throws SQLException {
        String sql = "UPDATE book SET available_quantity = available_quantity + 1 WHERE book_id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate();
        }
    }

    // ---------- helpers ----------
    private List<Book> query(String sql, String keyword) throws SQLException {
        List<Book> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (keyword != null) {
                String like = "%" + keyword + "%";
                for (int i = 1; i <= 4; i++) {
                    ps.setString(i, like);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBook(rs));
                }
            }
        }
        return list;
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId(rs.getInt("book_id"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setCategory(rs.getString("category"));
        b.setIsbn(rs.getString("isbn"));
        b.setQuantity(rs.getInt("quantity"));
        b.setAvailableQuantity(rs.getInt("available_quantity"));
        b.setActive(rs.getBoolean("active"));
        return b;
    }
}
