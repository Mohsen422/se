package ap.projects.finalproject;

import ap.projects.finalproject.manager.BookManager;
import ap.projects.finalproject.manager.LibrarianManager;
import ap.projects.finalproject.manager.StudentManager;
import ap.projects.finalproject.manager.RequestManager;
import ap.projects.finalproject.menu.MenuHandler;
import ap.projects.finalproject.model.*;
import ap.projects.finalproject.util.IdGen;
import ap.projects.finalproject.util.DatabaseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// LibrarySystem.java
public class LibrarySystem {
    private IdGen gen = new IdGen();

    private static Director director;

    private StudentManager studentManager;
    private LibrarianManager librarianManager;
    private BookManager bookManager;
    private RequestManager requestManager;

    private MenuHandler menuHandler;
    private DatabaseHandler database;

    public LibrarySystem() {
        this.director = new Director("Amir Sultani", "1234");

        this.database = new DatabaseHandler();

        this.studentManager = new StudentManager(database);
        this.studentManager.loadFromDatabase();

        this.librarianManager = new LibrarianManager(database);
        this.librarianManager.loadFromDatabase();

        this.bookManager = new BookManager(database);
        this.bookManager.loadFromDatabase();
        this.bookManager.loadBorrowedFromDatabase(studentManager.getStudents(), librarianManager.getLibrarians());

        this.requestManager = new RequestManager(database);
        this.requestManager.loadFromDatabase(studentManager.getStudents(), bookManager.getBooks());

        this.menuHandler = new MenuHandler(this);
    }

    public static Director getDirector() {
        return director;
    }

    public void registerStudent(String name, String studentId, String username, String password) {
        studentManager.registerStudent(name, studentId, username, password);
    }

    public Student authenticateStudent(String username, String password) {
        return studentManager.authenticateStudent(username, password);
    }

    public void editStudentInformation(Student student) {
        System.out.println("Not implemented.");
    }

    public void borrowBook(Student student) {
        System.out.println("Not implemented.");
    }

    public void displayAvailableBooks() {
        System.out.println("Not implemented.");
    }

    public int getStudentCount() {
        return this.studentManager.getStudentCount();
    }

    public Director authenticateDirector(String password) {
        return director.authenticate(password);
    }

    public void addLibrarian(String name, String username, String password) {
        librarianManager.addLibrarian(name, username, password);
    }

    public ArrayList<Book> searchBooks(String title, String author, String date) {
        return bookManager.searchBooks(title, author, date);
    }

    public User authenticateLibrarian(String username, String password) {
        return librarianManager.authenticateLibrarian(username, password);
    }

    public void start() {
        menuHandler.displayMainMenu();
    }

    public void save() {
        // Every change is written to the database as it happens, so shutting down
        // only needs to close the connection.
        database.close();
    }

    public void updateLibrarian(User old_librarian, User new_librarian) {
        librarianManager.update((Librarian) old_librarian, (Librarian) new_librarian);
    }

    public void addBook(String title, String author, String date, long pages) {
        String isbn;
        do {
            isbn = gen.generateID().toString();
        } while (bookManager.isbnExists(isbn));

        bookManager.addBook(title, author, date, isbn, pages);
    }

    public Book getBook(String isbn) {
        return bookManager.getBook(isbn);
    }

    public void makeRequest(Book book, User currentUser) {
        requestManager.makeRequest(book, (Student) currentUser);
    }

    public HashMap<String, BorrowRequest> getRequests() {
        return requestManager.getRequests();
    }

    public BorrowRequest getRequest(String ID) {
        return requestManager.getRequest(ID);
    }

    public void changeRequestStatus(int input, String id, BorrowRequest req) {
        requestManager.setRequestStatus(id, req);
        if (input == 1) {

            Book book = req.getBook();
            updateBook(req.getBook(), book, false);

            BorrowedBook borrowedBook = new BorrowedBook(req);
            bookManager.addBorrowed(borrowedBook);
        }
    }

    public void updateBook(Book oldBook, Book newBook, boolean isAvailable) {
        bookManager.updateBook(oldBook, newBook, isAvailable);
    }

    public List<Book> getBooks() {
        return bookManager.getBooks();
    }

    public List<Student> getStudents() {
        return studentManager.getStudents();
    }

    public List<BorrowedBook> getBorrowedBooks() {
        return bookManager.getBorrowedBooks();
    }

    public void studentActivation(String username, boolean state) {
        Student student = studentManager.getStudent(username);
        student.setActive(state);

        studentManager.update(studentManager.getStudent(username), student);
    }

    public void returnBook(BorrowedBook borrowedBook) {
        bookManager.returnBook(borrowedBook);
    }
}
