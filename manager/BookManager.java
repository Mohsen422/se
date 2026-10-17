package ap.projects.finalproject.manager;

import ap.projects.finalproject.model.Book;
import ap.projects.finalproject.model.BorrowedBook;
import ap.projects.finalproject.model.Librarian;
import ap.projects.finalproject.model.Student;
import ap.projects.finalproject.util.DatabaseHandler;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookManager {
    private List<Book> books;
    private List<BorrowedBook> borrowedBooks;
    private DatabaseHandler db;

    public BookManager(DatabaseHandler db) {
        this.books = new ArrayList<>();
        this.borrowedBooks = new ArrayList<>();
        this.db = db;
    }

    public void loadFromDatabase() {
        try {
            for (Object[] row : db.query("SELECT id, title, author, publication_date, isbn, pages, is_available FROM books")) {
                Book book = new Book((String) row[1], (String) row[2], (String) row[3], (String) row[4], (Integer) row[5]);
                book.setId((Integer) row[0]);
                book.setAvailable(((Integer) row[6]) == 1);
                books.add(book);
            }
        } catch (SQLException e) {
            System.out.println("Failed to load books from the database.");
            e.printStackTrace();
        }
    }

    public void loadBorrowedFromDatabase(List<Student> students, List<Librarian> librarians) {
        // Rebuilds the loan records; the student/book/librarian objects come from the
        // already-loaded lists so all layers share the same instances as before.
        try {
            for (Object[] row : db.query("SELECT id, book_id, student_id, librarian_id, borrow_date, due_date, return_date, is_late FROM borrowed_books")) {
                Book book = findBookById((Integer) row[1]);
                Student borrower = findStudentById(students, (Integer) row[2]);
                Librarian librarian = (row[3] == null) ? null : findLibrarianById(librarians, (Integer) row[3]);
                LocalDate returnDate = (row[6] == null) ? null : LocalDate.parse((String) row[6]);

                BorrowedBook borrowedBook = new BorrowedBook(book, borrower, librarian,
                        LocalDate.parse((String) row[4]), LocalDate.parse((String) row[5]), returnDate,
                        ((Integer) row[7]) == 1);
                borrowedBook.setId((Integer) row[0]);
                borrowedBooks.add(borrowedBook);
            }
        } catch (SQLException e) {
            System.out.println("Failed to load borrowed books from the database.");
            e.printStackTrace();
        }
    }

    public void addBook(String title, String author, String date, String isbn, long pages) {
        Book newBook = new Book(title, author, date, isbn, pages);
        try {
            int id = db.insert("INSERT INTO books (title, author, publication_date, isbn, pages, is_available) VALUES (?,?,?,?,?,1)",
                    title, author, date, isbn, pages);
            newBook.setId(id);
            books.add(newBook);
        } catch (SQLException e) {
            System.out.println("Failed to save the book to the database.");
            e.printStackTrace();
        }
    }

    public ArrayList<Book> searchBooks(String title, String author, String date) {
        ArrayList<Book> results = new ArrayList<>();

        for (Book book : this.books) {
            boolean containsTitle = !title.isEmpty() && book.getTitle().toLowerCase().contains(title.toLowerCase());
            boolean containsAuthor = !author.isEmpty() && book.getAuthor().toLowerCase().contains(author.toLowerCase());
            boolean containsDate = !date.isEmpty() && book.getPublicationDate().contains(date);

            if (containsAuthor || containsDate || containsTitle) {
                results.add(book);
            }
        }

        return results;
    }

    public Book getBook(String isbn) {
        for (Book book : this.books) {
            if (book.getIsbn().equals(isbn)) {
                return book;
            }
        }

        return null;
    }

    public void updateBook(Book old, Book newBook, boolean isAvailable) {
        Book oldtemp = this.books.stream()
                .filter(b -> b.equals(old))
                .findFirst().orElse(null);
        int index = books.indexOf(oldtemp);
        books.remove(index);
        newBook.setAvailable(isAvailable);
        books.add(newBook);

        try {
            db.update("UPDATE books SET title=?, author=?, publication_date=?, isbn=?, pages=?, is_available=? WHERE id=?",
                    newBook.getTitle(), newBook.getAuthor(), newBook.getPublicationDate(), newBook.getIsbn(),
                    newBook.getPages(), newBook.isAvailable() ? 1 : 0, newBook.getId());
        } catch (SQLException e) {
            System.out.println("Failed to update the book in the database.");
            e.printStackTrace();
        }
    }

    public boolean isbnExists(String isbn) {
        for (Book book : this.books) {
            if (book.getIsbn().equals(isbn)) {
                return true;
            }
        }

        return false;
    }

    public void addBorrowed(BorrowedBook borrowedBook) {
        try {
            int id = db.insert("INSERT INTO borrowed_books (book_id, student_id, librarian_id, borrow_date, due_date, return_date, is_late) VALUES (?,?,?,?,?,?,0)",
                    borrowedBook.getBook().getId(), borrowedBook.getBorrower().getId(),
                    (borrowedBook.getLibrarian() == null) ? null : borrowedBook.getLibrarian().getId(),
                    borrowedBook.getBorrowDate().toString(), borrowedBook.getDueDate().toString(),
                    (borrowedBook.getReturnDate() == null) ? null : borrowedBook.getReturnDate().toString());
            borrowedBook.setId(id);
            borrowedBooks.add(borrowedBook);
        } catch (SQLException e) {
            System.out.println("Failed to save the borrowed book to the database.");
            e.printStackTrace();
        }
    }

    public List<BorrowedBook> getBorrowedBooks() {
        return borrowedBooks;
    }

    public List<Book> getBooks() {
        return this.books;
    }

    public void returnBook(BorrowedBook borrowedBook) {
        int index = borrowedBooks.indexOf(borrowedBook);

        borrowedBook.setReturnDate(LocalDate.now());

        if (borrowedBook.getReturnDate().isAfter(borrowedBook.getDueDate())) {
            borrowedBook.setLate(true);
        }

        borrowedBooks.remove(index);
        borrowedBooks.add(borrowedBook);

        Book newBook = borrowedBook.getBook();

        updateBook(borrowedBook.getBook(), newBook, true);

        try {
            db.update("UPDATE borrowed_books SET return_date=?, is_late=? WHERE id=?",
                    borrowedBook.getReturnDate().toString(), borrowedBook.isLate() ? 1 : 0, borrowedBook.getId());
        } catch (SQLException e) {
            System.out.println("Failed to update the borrowed book in the database.");
            e.printStackTrace();
        }
    }

    private Book findBookById(int id) {
        for (Book book : this.books) {
            if (book.getId() == id) {
                return book;
            }
        }
        return null;
    }

    private Student findStudentById(List<Student> students, int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }

    private Librarian findLibrarianById(List<Librarian> librarians, int id) {
        for (Librarian librarian : librarians) {
            if (librarian.getId() == id) {
                return librarian;
            }
        }
        return null;
    }
}
