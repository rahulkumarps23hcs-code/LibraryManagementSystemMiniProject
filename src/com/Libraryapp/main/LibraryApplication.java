package com.Libraryapp.main;

import java.util.List;
import java.util.Map;

import com.Libraryapp.exception.LibraryException;
import com.Libraryapp.model.Book;
import com.Libraryapp.model.IssueRecord;
import com.Libraryapp.model.Member;
import com.Libraryapp.model.User;
import com.Libraryapp.service.LibraryService;
import com.Libraryapp.service.LibraryServiceImpl;
import com.Libraryapp.util.InputUtil;

public class LibraryApplication {

    private static final LibraryService service = new LibraryServiceImpl();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = InputUtil.readInt("Enter choice: ");
            try {
                switch (choice) {
                case 1:
                    registerMember();
                    break;
                case 2:
                    memberMenu(login("MEMBER"));
                    break;
                case 3:
                    adminMenu(login("ADMIN"));
                    break;
                case 4:
                    System.out.println("Thank you for using the Library System. Bye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // Menus
    // ------------------------------------------------------------------
    private static void printMainMenu() {
        System.out.println("\n======================================");
        System.out.println("       LIBRARY MANAGEMENT SYSTEM");
        System.out.println("======================================");
        System.out.println("1. Member Registration");
        System.out.println("2. Member Login");
        System.out.println("3. Admin Login");
        System.out.println("4. Exit");
    }

    private static void memberMenu(User user) {
        user.showWelcome();
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n----------- MEMBER MENU -----------");
            System.out.println("1. Search Book");
            System.out.println("2. View Available Books");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. View Issued Books");
            System.out.println("6. View History");
            System.out.println("7. View Fine");
            System.out.println("8. Logout");
            int choice = InputUtil.readInt("Enter choice: ");
            try {
                switch (choice) {
                case 1:
                    printBooks(service.searchBooks(InputUtil.readString("Title / author / category / ISBN: ")));
                    break;
                case 2:
                    printBooks(service.viewAvailableBooks());
                    break;
                case 3:
                    service.issueBook(user.getUserId(), InputUtil.readInt("Book id to issue: "));
                    System.out.println("Book issued successfully. Please return within 14 days.");
                    break;
                case 4:
                    double fine = service.returnBook(user.getUserId(), InputUtil.readInt("Book id to return: "));
                    System.out.println("Book returned successfully."
                            + (fine > 0 ? " Late fine: Rs " + fine : " No fine."));
                    break;
                case 5:
                    printRecords(service.viewIssuedBooks(user.getUserId()));
                    break;
                case 6:
                    printRecords(service.viewHistory(user.getUserId()));
                    break;
                case 7:
                    System.out.println("Total fine: Rs " + service.viewTotalFine(user.getUserId()));
                    break;
                case 8:
                    System.out.println("Logged out.");
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void adminMenu(User admin) {
        admin.showWelcome();
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n----------- ADMIN MENU -----------");
            System.out.println("1. Add Book");
            System.out.println("2. Update Book");
            System.out.println("3. Delete Book");
            System.out.println("4. Activate / Deactivate Book");
            System.out.println("5. View / Search Books (sorted)");
            System.out.println("6. View Books By Category");
            System.out.println("7. View All Members");
            System.out.println("8. Search Member");
            System.out.println("9. View Issued Books");
            System.out.println("10. View All Transactions");
            System.out.println("11. View Overdue Books");
            System.out.println("12. Category Report");
            System.out.println("13. Logout");
            int choice = InputUtil.readInt("Enter choice: ");
            try {
                switch (choice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    updateBook();
                    break;
                case 3:
                    service.deleteBook(InputUtil.readInt("Book id to delete: "));
                    System.out.println("Book deleted.");
                    break;
                case 4:
                    toggleBook();
                    break;
                case 5:
                    viewBooks();
                    break;
                case 6:
                    printBooksByCategory(service.viewBooksByCategory());
                    break;
                case 7:
                    printUsers(service.viewAllMembers());
                    break;
                case 8:
                    printUsers(service.searchMembers(InputUtil.readString("Name / email / mobile: ")));
                    break;
                case 9:
                    printRecords(service.viewAllIssuedBooks());
                    break;
                case 10:
                    printRecords(service.viewAllTransactions());
                    break;
                case 11:
                    printRecords(service.viewOverdueRecords());
                    break;
                case 12:
                    service.viewCategoryReport().forEach(
                            (category, count) -> System.out.println(category + " -> " + count + " title(s)"));
                    break;
                case 13:
                    System.out.println("Logged out.");
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // Actions that need several inputs
    // ------------------------------------------------------------------
    private static void registerMember() throws LibraryException {
        Member m = new Member();
        m.setName(InputUtil.readString("Name: "));
        m.setEmail(InputUtil.readString("Email: "));
        m.setMobile(InputUtil.readString("Mobile (10 digits): "));
        m.setAddress(InputUtil.readString("Address: "));
        m.setPassword(InputUtil.readString("Password (min 6 chars): "));
        service.registerMember(m);
        System.out.println("Registration successful! You can login now.");
    }

    private static User login(String role) throws LibraryException {
        String email = InputUtil.readString("Email: ");
        String password = InputUtil.readString("Password: ");
        return service.login(email, password, role);
    }

    private static void addBook() throws LibraryException {
        Book b = new Book();
        b.setTitle(InputUtil.readString("Title: "));
        b.setAuthor(InputUtil.readString("Author: "));
        b.setCategory(InputUtil.readString("Category: "));
        b.setIsbn(InputUtil.readString("ISBN: "));
        b.setQuantity(InputUtil.readInt("Quantity: "));
        service.addBook(b);
        System.out.println("Book added successfully.");
    }

    /** Press Enter to keep the old value. */
    private static void updateBook() throws LibraryException {
        Book b = service.findBook(InputUtil.readInt("Book id to update: "));
        System.out.println("Leave blank to keep the current value.");

        String title = InputUtil.readString("Title [" + b.getTitle() + "]: ");
        String author = InputUtil.readString("Author [" + b.getAuthor() + "]: ");
        String category = InputUtil.readString("Category [" + b.getCategory() + "]: ");
        String isbn = InputUtil.readString("ISBN [" + b.getIsbn() + "]: ");
        String qty = InputUtil.readString("Quantity [" + b.getQuantity() + "]: ");

        if (!title.isEmpty()) b.setTitle(title);
        if (!author.isEmpty()) b.setAuthor(author);
        if (!category.isEmpty()) b.setCategory(category);
        if (!isbn.isEmpty()) b.setIsbn(isbn);
        if (!qty.isEmpty()) {
            try {
                b.setQuantity(Integer.parseInt(qty));
            } catch (NumberFormatException e) {
                System.out.println("Invalid quantity, keeping old value.");
            }
        }
        service.updateBook(b);
        System.out.println("Book updated.");
    }

    private static void toggleBook() throws LibraryException {
        int id = InputUtil.readInt("Book id: ");
        String choice = InputUtil.readString("Type A to activate or D to deactivate: ");
        service.setBookActive(id, choice.equalsIgnoreCase("A"));
        System.out.println("Book status changed.");
    }

    private static void viewBooks() throws LibraryException {
        String keyword = InputUtil.readString("Search keyword (Enter = show all): ");
        if (!keyword.isEmpty()) {
            printBooks(service.searchBooks(keyword));
            return;
        }
        System.out.println("Sort by: 1.Title  2.Author  3.Category  4.Most available");
        int s = InputUtil.readInt("Choice: ");
        String sortBy = s == 2 ? "author" : s == 3 ? "category" : s == 4 ? "available" : "title";
        printBooks(service.viewAllBooksSorted(sortBy));
    }

    // ------------------------------------------------------------------
    // Printing helpers (method references + forEach)
    // ------------------------------------------------------------------
    private static void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.println(Book.header());
        books.forEach(System.out::println);
    }

    private static void printBooksByCategory(Map<String, List<Book>> map) {
        if (map.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        map.forEach((category, books) -> {
            System.out.println("\n== " + category.toUpperCase() + " ==");
            books.forEach(b -> System.out.println("  " + b.getBookId() + ". " + b.getTitle()
                    + " (" + b.getAuthor() + ") - " + b.getAvailableQuantity() + " available"));
        });
    }

    private static void printUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No members found.");
            return;
        }
        System.out.println(User.header());
        users.forEach(System.out::println);
    }

    private static void printRecords(List<IssueRecord> records) {
        if (records.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        System.out.println(IssueRecord.header());
        records.forEach(System.out::println);
    }
}
