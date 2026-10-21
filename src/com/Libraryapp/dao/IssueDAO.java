package com.Libraryapp.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.Libraryapp.model.IssueRecord;
import com.Libraryapp.util.DBConnection;

public class IssueDAO {

    private static final String BASE_SELECT =
            "SELECT i.*, b.title, u.name FROM issue_record i "
          + "JOIN book b ON i.book_id = b.book_id "
          + "JOIN user u ON i.user_id = u.user_id ";

    public int insertIssue(Connection con, IssueRecord r) throws SQLException {
        String sql = "INSERT INTO issue_record(book_id,user_id,issue_date,due_date,fine,status) VALUES(?,?,?,?,0,'ISSUED')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getBookId());
            ps.setInt(2, r.getUserId());
            ps.setDate(3, Date.valueOf(r.getIssueDate()));
            ps.setDate(4, Date.valueOf(r.getDueDate()));
            return ps.executeUpdate();
        }
    }

    public int markReturned(Connection con, int issueId, LocalDate returnDate, double fine) throws SQLException {
        String sql = "UPDATE issue_record SET return_date=?, fine=?, status='RETURNED' WHERE issue_id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(returnDate));
            ps.setDouble(2, fine);
            ps.setInt(3, issueId);
            return ps.executeUpdate();
        }
    }

    /** The not-yet-returned record of this member for this book, or null. */
    public IssueRecord findActiveIssue(int userId, int bookId) throws SQLException {
        List<IssueRecord> list = query(BASE_SELECT + "WHERE i.user_id=? AND i.book_id=? AND i.status='ISSUED'",
                userId, bookId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<IssueRecord> getIssuedByUser(int userId) throws SQLException {
        return query(BASE_SELECT + "WHERE i.user_id=? AND i.status='ISSUED'", userId);
    }

    public List<IssueRecord> getHistoryByUser(int userId) throws SQLException {
        return query(BASE_SELECT + "WHERE i.user_id=?", userId);
    }

    public List<IssueRecord> getAllTransactions() throws SQLException {
        return query(BASE_SELECT);
    }

    // ---------- helpers ----------
    /** varargs: any number of int parameters for the ? placeholders. */
    private List<IssueRecord> query(String sql, int... params) throws SQLException {
        List<IssueRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRecord(rs));
                }
            }
        }
        return list;
    }

    private IssueRecord mapRecord(ResultSet rs) throws SQLException {
        IssueRecord r = new IssueRecord();
        r.setIssueId(rs.getInt("issue_id"));
        r.setBookId(rs.getInt("book_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setIssueDate(rs.getDate("issue_date").toLocalDate());
        r.setDueDate(rs.getDate("due_date").toLocalDate());
        Date returned = rs.getDate("return_date");
        if (returned != null) {
            r.setReturnDate(returned.toLocalDate());
        }
        r.setFine(rs.getDouble("fine"));
        r.setStatus(rs.getString("status"));
        r.setBookTitle(rs.getString("title"));
        r.setUserName(rs.getString("name"));
        return r;
    }
}
