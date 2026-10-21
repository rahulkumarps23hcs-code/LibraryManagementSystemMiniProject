package com.Libraryapp.model;

import java.time.LocalDate;

public class IssueRecord {

    private int issueId;
    private int bookId;
    private int userId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;   
    private double fine;
    private String status = "ISSUED";   
    
    private String bookTitle;
    private String userName;

    public IssueRecord() {
    }

    public IssueRecord(int userId, int bookId, LocalDate issueDate, LocalDate dueDate) {
        this.userId = userId;
        this.bookId = bookId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    public int getIssueId() { return issueId; }
    public void setIssueId(int issueId) { this.issueId = issueId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public double getFine() { return fine; }
    public void setFine(double fine) { this.fine = fine; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public boolean isReturned() {
        return "RETURNED".equals(status);
    }

    @Override
    public String toString() {
        return String.format("%-6d %-24s %-14s %-12s %-12s %-12s %-7.2f %s",
                issueId, bookTitle, userName, issueDate, dueDate,
                returnDate == null ? "-" : returnDate.toString(), fine, status);
    }

    public static String header() {
        return String.format("%-6s %-24s %-14s %-12s %-12s %-12s %-7s %s",
                "ISSUE", "BOOK", "MEMBER", "ISSUED ON", "DUE ON", "RETURNED", "FINE", "STATUS");
    }
}
