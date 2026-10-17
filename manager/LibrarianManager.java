package ap.projects.finalproject.manager;

import ap.projects.finalproject.model.Librarian;
import ap.projects.finalproject.model.User;
import ap.projects.finalproject.util.DatabaseHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibrarianManager implements Update {
    private List<Librarian> librarians;
    private DatabaseHandler db;

    public LibrarianManager(DatabaseHandler db) {
        this.librarians = new ArrayList<>();
        this.db = db;
    }

    public void loadFromDatabase() {
        try {
            for (Object[] row : db.query("SELECT id, name, username, password, books_added, requests_handled FROM librarians")) {
                Librarian librarian = new Librarian((String) row[1], (String) row[2], (String) row[3]);
                librarian.setId((Integer) row[0]);
                librarian.setBooksAdded((Integer) row[4]);
                librarian.setRequestsHandled((Integer) row[5]);
                librarians.add(librarian);
            }
        } catch (SQLException e) {
            System.out.println("Failed to load librarians from the database.");
            e.printStackTrace();
        }
    }

    public void addLibrarian(String name, String username, String password) {
        // Logic for adding a new librarian. Checks if the provided username is already in use.
        if (isUsernameTaken(username)) {
            System.out.println("This username already exists. Please choose a different username.");
            return;
        } else {
            Librarian newLibrarian = new Librarian(name, username, password);
            try {
                int id = db.insert("INSERT INTO librarians (name, username, password) VALUES (?,?,?)",
                        name, username, password);
                newLibrarian.setId(id);
                librarians.add(newLibrarian);
                System.out.println("Successfully added " + newLibrarian + '!');
            } catch (SQLException e) {
                System.out.println("Failed to save the librarian to the database.");
                e.printStackTrace();
            }
        }
    }

    public Librarian authenticateLibrarian(String username, String password) {
        // Logic for librarian login. Checks if the provided info match any librarian in the list.
        return librarians.stream()
                .filter(s -> s.getUsername().equals(username) && s.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void update(User Old, User New) {
        librarians.remove((Librarian) Old);
        librarians.add((Librarian) New);

        Librarian librarian = (Librarian) New;
        try {
            db.update("UPDATE librarians SET name=?, username=?, password=?, books_added=?, requests_handled=? WHERE id=?",
                    librarian.getName(), librarian.getUsername(), librarian.getPassword(),
                    librarian.getBooksAdded(), librarian.getRequestsHandled(), librarian.getId());
        } catch (SQLException e) {
            System.out.println("Failed to update the librarian in the database.");
            e.printStackTrace();
        }
    }

    public List<Librarian> getLibrarians() {
        return librarians;
    }

    private boolean isUsernameTaken(String username) {
        return librarians.stream().anyMatch(s -> s.getUsername().equals(username));
    }

}
