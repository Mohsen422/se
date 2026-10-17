package ap.projects.finalproject.manager;

import ap.projects.finalproject.model.Book;
import ap.projects.finalproject.model.BorrowRequest;
import ap.projects.finalproject.model.Student;
import ap.projects.finalproject.util.DatabaseHandler;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

public class RequestManager {
    private HashMap<String, BorrowRequest> requests;
    private DatabaseHandler db;

    public RequestManager(DatabaseHandler db) {
        this.requests = new HashMap<>();
        this.db = db;
    }

    public void loadFromDatabase(List<Student> students, List<Book> books) {
        // Rebuilds the requests; the student/book objects come from the already-loaded
        // lists so all layers share the same instances as before. The map key is the
        // database row id (as text), which is what the librarian types in the menu.
        try {
            for (Object[] row : db.query("SELECT id, student_id, book_id, borrow_date, to_date, status FROM borrow_requests")) {
                Student borrower = findStudentById(students, (Integer) row[1]);
                Book book = findBookById(books, (Integer) row[2]);

                BorrowRequest req = new BorrowRequest(borrower, book,
                        LocalDate.parse((String) row[3]), LocalDate.parse((String) row[4]), (String) row[5]);
                req.setId((Integer) row[0]);
                requests.put(String.valueOf(row[0]), req);
            }
        } catch (SQLException e) {
            System.out.println("Failed to load requests from the database.");
            e.printStackTrace();
        }
    }

    public void makeRequest(Book book, Student borrower) {
        BorrowRequest req = new BorrowRequest(borrower, book);
        try {
            int id = db.insert("INSERT INTO borrow_requests (student_id, book_id, borrow_date, to_date, status) VALUES (?,?,?,?,?)",
                    borrower.getId(), book.getId(), req.getBorrowDate().toString(), req.getToDate().toString(), req.getStatusName());
            req.setId(id);
            requests.put(String.valueOf(id), req);
        } catch (SQLException e) {
            System.out.println("Failed to save the request to the database.");
            e.printStackTrace();
        }
    }

    public HashMap<String, BorrowRequest> getRequests() {
        return requests;
    }

    public BorrowRequest getRequest(String ID) {
        return requests.get(ID);
    }

    public void setRequestStatus(String id, BorrowRequest req) {
        requests.replace(id, req);
        try {
            db.update("UPDATE borrow_requests SET status=? WHERE id=?", req.getStatusName(), req.getId());
        } catch (SQLException e) {
            System.out.println("Failed to update the request in the database.");
            e.printStackTrace();
        }
    }

    private Student findStudentById(List<Student> students, int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }

    private Book findBookById(List<Book> books, int id) {
        for (Book book : books) {
            if (book.getId() == id) {
                return book;
            }
        }
        return null;
    }
}
