package com.Libraryapp.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.Libraryapp.dao.BookDAO;
import com.Libraryapp.dao.IssueDAO;
import com.Libraryapp.dao.UserDAO;
import com.Libraryapp.exception.BookNotAvailableException;
import com.Libraryapp.exception.BookNotFoundException;
import com.Libraryapp.exception.InvalidInputException;
import com.Libraryapp.exception.LibraryException;
import com.Libraryapp.model.Book;
import com.Libraryapp.model.IssueRecord;
import com.Libraryapp.model.Member;
import com.Libraryapp.model.User;
import com.Libraryapp.util.ValidationUtil;

public class LibraryServiceImpl implements LibraryService {

    private static final int LOAN_DAYS = 14;
    private static final double FINE_PER_DAY = 5.0;

    // Java 9 factory methods: immutable Set and Map
    private static final Set<String> VALID_ROLES = Set.of("MEMBER", "ADMIN");

    private static final Map<String, Comparator<Book>> SORTERS = Map.of(
            "title", Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER),
            "author", Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER),
            "category", Comparator.comparing(Book::getCategory, String.CASE_INSENSITIVE_ORDER)
                                  .thenComparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER),
            "available", Comparator.comparingInt(Book::getAvailableQuantity).reversed());

    private final UserDAO userDAO = new UserDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final IssueDAO issueDAO = new IssueDAO();

    // ------------------------------------------------------------------
    // Functional interfaces + generics: wrap every DAO call so SQLException
    // becomes a friendly LibraryException (no try/catch repeated everywhere).
    // ------------------------------------------------------------------
    @FunctionalInterface
    private interface DbAction<T> {
        T run() throws SQLException;
    }

    @FunctionalInterface
    private interface TxAction {
        void run(Connection con) throws Exception;
    }

    private <T> T db(DbAction<T> action) throws LibraryException {
        try {
            return action.run();
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    private LibraryException translate(SQLException e) {
        switch (e.getErrorCode()) {
        case 1062:
            return new LibraryException("Duplicate value: email or ISBN already exists.");
        case 1451:
            return new LibraryException("Cannot delete: this record has issue history. Deactivate it instead.");
        default:
            return new LibraryException("Database error: " + e.getMessage());
        }
    }

    /** Runs the action as ONE transaction: commit if all OK, rollback on any error. */
    private void inTransaction(TxAction action) throws LibraryException {
        try (Connection con = com.Libraryapp.util.DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                action.run(con);
                con.commit();
            } catch (LibraryException e) {
                con.rollback();
                throw e;
            } catch (Exception e) {
                con.rollback();
                throw new LibraryException("Transaction failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    // ------------------------------------------------------------------
    // Account
    // ------------------------------------------------------------------
    @Override
    public void registerMember(Member m) throws LibraryException {
        if (!ValidationUtil.IS_NOT_EMPTY.test(m.getName()))
            throw new InvalidInputException("Name cannot be empty.");
        if (!ValidationUtil.IS_VALID_EMAIL.test(m.getEmail()))
            throw new InvalidInputException("Invalid email format.");
        if (!ValidationUtil.IS_VALID_MOBILE.test(m.getMobile()))
            throw new InvalidInputException("Mobile must be exactly 10 digits.");
        if (!ValidationUtil.IS_NOT_EMPTY.test(m.getAddress()))
            throw new InvalidInputException("Address cannot be empty.");
        if (!ValidationUtil.IS_VALID_PASSWORD.test(m.getPassword()))
            throw new InvalidInputException("Password must be at least 6 characters.");

        if (db(() -> userDAO.emailExists(m.getEmail())))
            throw new InvalidInputException("This email is already registered.");

        db(() -> userDAO.registerUser(m));
    }

    @Override
    public User login(String email, String password, String role) throws LibraryException {
        if (!VALID_ROLES.contains(role))
            throw new InvalidInputException("Invalid role.");
        if (!ValidationUtil.IS_NOT_EMPTY.test(email) || !ValidationUtil.IS_NOT_EMPTY.test(password))
            throw new InvalidInputException("Email and password are required.");

        User user = db(() -> userDAO.login(email, password, role));
        if (user == null)
            throw new InvalidInputException("Invalid email or password.");
        return user;
    }

    // ------------------------------------------------------------------
    // Books: search / view / sort
    // ------------------------------------------------------------------
    @Override
    public List<Book> searchBooks(String keyword) throws LibraryException {
        if (!ValidationUtil.IS_NOT_EMPTY.test(keyword))
            throw new InvalidInputException("Search keyword cannot be empty.");
        return sort(db(() -> bookDAO.searchBooks(keyword)), "title");
    }

    @Override
    public List<Book> viewAvailableBooks() throws LibraryException {
        return sort(db(() -> bookDAO.getAvailableBooks()), "title");
    }

    @Override
    public List<Book> viewAllBooksSorted(String sortBy) throws LibraryException {
        return sort(db(() -> bookDAO.getAllBooks()), sortBy);
    }

    /** Stream + Comparator picked from the map (falls back to title). */
    private List<Book> sort(List<Book> books, String sortBy) {
        Comparator<Book> comparator = SORTERS.getOrDefault(sortBy, SORTERS.get("title"));
        return books.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
    }

    @Override
    public Book findBook(int bookId) throws LibraryException {
        return Optional.ofNullable(db(() -> bookDAO.findById(bookId)))
                       .orElseThrow(() -> new BookNotFoundException("No book found with id " + bookId));
    }

    // ------------------------------------------------------------------
    // Issue / Return (transactions)
    // ------------------------------------------------------------------
    @Override
    public void issueBook(int userId, int bookId) throws LibraryException {
        Book book = findBook(bookId);

        if (!book.isActive() || book.getAvailableQuantity() <= 0)
            throw new BookNotAvailableException("No copy of '" + book.getTitle() + "' is available.");

        if (db(() -> issueDAO.findActiveIssue(userId, bookId)) != null)
            throw new LibraryException("You have already issued this book.");

        LocalDate today = LocalDate.now();
        IssueRecord record = new IssueRecord(userId, bookId, today, today.plusDays(LOAN_DAYS));

        inTransaction(con -> {
            if (bookDAO.decreaseAvailable(con, bookId) == 0)
                throw new BookNotAvailableException("Last copy was just taken by someone else.");
            issueDAO.insertIssue(con, record);
        });
    }

    @Override
    public double returnBook(int userId, int bookId) throws LibraryException {
        IssueRecord record = Optional.ofNullable(db(() -> issueDAO.findActiveIssue(userId, bookId)))
                .orElseThrow(() -> new LibraryException("You have not issued this book."));

        LocalDate today = LocalDate.now();
        double fine = calculateFine(record.getDueDate(), today);

        inTransaction(con -> {
            issueDAO.markReturned(con, record.getIssueId(), today, fine);
            bookDAO.increaseAvailable(con, bookId);
        });
        return fine;
    }

    private double calculateFine(LocalDate dueDate, LocalDate returnedOn) {
        long lateDays = ChronoUnit.DAYS.between(dueDate, returnedOn);
        return lateDays > 0 ? lateDays * FINE_PER_DAY : 0.0;
    }

    // ------------------------------------------------------------------
    // Member views
    // ------------------------------------------------------------------
    @Override
    public List<IssueRecord> viewIssuedBooks(int userId) throws LibraryException {
        List<IssueRecord> list = db(() -> issueDAO.getIssuedByUser(userId));
        // show the running fine for books that are already overdue
        list.forEach(r -> r.setFine(calculateFine(r.getDueDate(), LocalDate.now())));
        return list.stream()
                   .sorted(Comparator.comparing(IssueRecord::getDueDate))
                   .collect(Collectors.toList());
    }

    @Override
    public List<IssueRecord> viewHistory(int userId) throws LibraryException {
        return newestFirst(db(() -> issueDAO.getHistoryByUser(userId)));
    }

    @Override
    public double viewTotalFine(int userId) throws LibraryException {
        return db(() -> issueDAO.getHistoryByUser(userId)).stream()
                .mapToDouble(r -> r.isReturned()
                        ? r.getFine()
                        : calculateFine(r.getDueDate(), LocalDate.now()))
                .sum();
    }

    private List<IssueRecord> newestFirst(List<IssueRecord> list) {
        return list.stream()
                   .sorted(Comparator.comparing(IssueRecord::getIssueDate)
                                     .thenComparingInt(IssueRecord::getIssueId)
                                     .reversed())
                   .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // Admin: book management
    // ------------------------------------------------------------------
    @Override
    public void addBook(Book b) throws LibraryException {
        validateBook(b);
        b.setAvailableQuantity(b.getQuantity());
        b.setActive(true);
        db(() -> bookDAO.addBook(b));
    }

    @Override
    public void updateBook(Book b) throws LibraryException {
        validateBook(b);
        Book old = findBook(b.getBookId());

        // keep quantity and available copies consistent with copies currently issued
        int issuedNow = old.getQuantity() - old.getAvailableQuantity();
        if (b.getQuantity() < issuedNow)
            throw new InvalidInputException("Quantity cannot be less than copies currently issued (" + issuedNow + ").");
        b.setAvailableQuantity(b.getQuantity() - issuedNow);

        db(() -> bookDAO.updateBook(b));
    }

    @Override
    public void deleteBook(int bookId) throws LibraryException {
        findBook(bookId);   // throws BookNotFoundException if missing
        db(() -> bookDAO.deleteBook(bookId));
    }

    @Override
    public void setBookActive(int bookId, boolean active) throws LibraryException {
        findBook(bookId);
        db(() -> bookDAO.setActive(bookId, active));
    }

    private void validateBook(Book b) throws InvalidInputException {
        if (!ValidationUtil.IS_NOT_EMPTY.test(b.getTitle()))
            throw new InvalidInputException("Title cannot be empty.");
        if (!ValidationUtil.IS_NOT_EMPTY.test(b.getAuthor()))
            throw new InvalidInputException("Author cannot be empty.");
        if (!ValidationUtil.IS_NOT_EMPTY.test(b.getIsbn()))
            throw new InvalidInputException("ISBN cannot be empty.");
        if (!ValidationUtil.IS_NOT_EMPTY.test(b.getCategory()))
            b.setCategory("General");
        if (b.getQuantity() <= 0)
            throw new InvalidInputException("Quantity must be greater than 0.");
    }

    // ------------------------------------------------------------------
    // Admin: members / transactions / reports
    // ------------------------------------------------------------------
    @Override
    public List<User> viewAllMembers() throws LibraryException {
        return db(() -> userDAO.getAllMembers()).stream()
                .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> searchMembers(String keyword) throws LibraryException {
        if (!ValidationUtil.IS_NOT_EMPTY.test(keyword))
            throw new InvalidInputException("Search keyword cannot be empty.");
        return db(() -> userDAO.searchMembers(keyword));
    }

    @Override
    public List<IssueRecord> viewAllTransactions() throws LibraryException {
        return newestFirst(db(() -> issueDAO.getAllTransactions()));
    }

    @Override
    public List<IssueRecord> viewAllIssuedBooks() throws LibraryException {
        return viewAllTransactions().stream()
                .filter(r -> !r.isReturned())
                .collect(Collectors.toList());
    }

    @Override
    public List<IssueRecord> viewOverdueRecords() throws LibraryException {
        LocalDate today = LocalDate.now();
        return viewAllIssuedBooks().stream()
                .filter(r -> r.getDueDate().isBefore(today))
                .peek(r -> r.setFine(calculateFine(r.getDueDate(), today)))
                .sorted(Comparator.comparing(IssueRecord::getDueDate))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<Book>> viewBooksByCategory() throws LibraryException {
        return sort(db(() -> bookDAO.getAllBooks()), "title").stream()
                .collect(Collectors.groupingBy(Book::getCategory, TreeMap::new, Collectors.toList()));
    }

    /** category -> number of titles, using groupingBy + counting. */
    @Override
    public Map<String, Long> viewCategoryReport() throws LibraryException {
        return db(() -> bookDAO.getAllBooks()).stream()
                .collect(Collectors.groupingBy(Book::getCategory, TreeMap::new, Collectors.counting()));
    }
}
