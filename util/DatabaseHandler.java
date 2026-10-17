package ap.projects.finalproject.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    // Owns the SQLite connection and creates the tables on startup.
    private final Connection connection;

    public DatabaseHandler() {
        this("library.db");
    }

    public DatabaseHandler(String dbFileName) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            createTables(conn);
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
            e.printStackTrace();
        }
        this.connection = conn;
    }

    private void createTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                    "id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name        TEXT NOT NULL," +
                    "username    TEXT NOT NULL UNIQUE," +
                    "password    TEXT NOT NULL," +
                    "student_id  TEXT NOT NULL UNIQUE," +
                    "is_active   INTEGER NOT NULL DEFAULT 1)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS librarians (" +
                    "id                INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name              TEXT NOT NULL," +
                    "username          TEXT NOT NULL UNIQUE," +
                    "password          TEXT NOT NULL," +
                    "books_added       INTEGER NOT NULL DEFAULT 0," +
                    "requests_handled  INTEGER NOT NULL DEFAULT 0)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS books (" +
                    "id                INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title             TEXT NOT NULL," +
                    "author            TEXT NOT NULL," +
                    "publication_date  TEXT," +
                    "isbn              TEXT NOT NULL UNIQUE," +
                    "pages             INTEGER," +
                    "is_available      INTEGER NOT NULL DEFAULT 1)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS borrow_requests (" +
                    "id           INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "student_id   INTEGER NOT NULL REFERENCES students(id)," +
                    "book_id      INTEGER NOT NULL REFERENCES books(id)," +
                    "borrow_date  TEXT NOT NULL," +
                    "to_date      TEXT NOT NULL," +
                    "status       TEXT NOT NULL DEFAULT 'PENDING'" +
                    "             CHECK (status IN ('PENDING','APPROVED','REJECTED')))");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS borrowed_books (" +
                    "id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "book_id       INTEGER NOT NULL REFERENCES books(id)," +
                    "student_id    INTEGER NOT NULL REFERENCES students(id)," +
                    "librarian_id  INTEGER REFERENCES librarians(id)," +
                    "borrow_date   TEXT NOT NULL," +
                    "due_date      TEXT NOT NULL," +
                    "return_date   TEXT," +
                    "is_late       INTEGER NOT NULL DEFAULT 0)");

            // Each book can have at most one open loan (the DB counterpart of Book.isAvailable).
            st.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS idx_one_active_loan_per_book " +
                    "ON borrowed_books(book_id) WHERE return_date IS NULL");
        }
    }

    public int insert(String sql, Object... params) throws SQLException {
        // Runs an INSERT and returns the generated row id.
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public int update(String sql, Object... params) throws SQLException {
        // Runs an UPDATE/DELETE and returns the number of affected rows.
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        }
    }

    public List<Object[]> query(String sql, Object... params) throws SQLException {
        // Runs a SELECT and returns every row as an Object[] ordered like the columns.
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                int columns = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[columns];
                    for (int i = 0; i < columns; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
