package ap.projects.finalproject.model;



import java.time.LocalDate;

public class BorrowedBook {
    private int id;
    private Book book;
    private Student borrower;
    private Librarian librarian;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean isLate;
//    LocalDate real_return_date;

    public BorrowedBook(Book book, Student borrower, Librarian librarian, LocalDate dueDate) {
        this.book = book;
        this.borrower = borrower;
        this.librarian = librarian;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusWeeks(2);
    }

    public BorrowedBook(BorrowRequest borrowRequest) {
        this.book = borrowRequest.getBook();
        this.borrower = borrowRequest.getBorrower();
        this.borrowDate = borrowRequest.getBorrowDate();
        this.dueDate = borrowRequest.getToDate();
    }

    public BorrowedBook(Book book, Student borrower, Librarian librarian,
                        LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate, boolean isLate) {
        // Rebuilds a loan loaded from the database.
        this.book = book;
        this.borrower = borrower;
        this.librarian = librarian;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.isLate = isLate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Student getBorrower() {
        return borrower;
    }

    public Librarian getLibrarian() {
        return librarian;
    }

    public boolean isLate() {
        return isLate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setLate(boolean late) {
        isLate = late;
    }

    public Book getBook() {
        return book;
    }

    @Override
    public String toString() {
        return  "dueDate=" + dueDate +
                ", borrowDate=" + borrowDate +
                ", book=" + book.getTitle();
    }
}
