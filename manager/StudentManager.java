package ap.projects.finalproject.manager;

import ap.projects.finalproject.model.Student;
import ap.projects.finalproject.model.User;
import ap.projects.finalproject.util.DatabaseHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentManager implements Update {
    private List<Student> students;
    private DatabaseHandler db;

    public StudentManager(DatabaseHandler db) {
        this.students = new ArrayList<>();
        this.db = db;
    }

    public void loadFromDatabase() {
        try {
            for (Object[] row : db.query("SELECT id, name, username, password, student_id, is_active FROM students")) {
                Student student = new Student((String) row[1], (String) row[2], (String) row[3], (String) row[4]);
                student.setId((Integer) row[0]);
                student.setActive(((Integer) row[5]) == 1);
                students.add(student);
            }
        } catch (SQLException e) {
            System.out.println("Failed to load students from the database.");
            e.printStackTrace();
        }
    }

    public void registerStudent(String name, String studentId, String username, String password) {
        // Logic for registering a new student. Checks the student list to see if the provided username is already in use.
        if (isUsernameTaken(username)) {
            System.out.println("This username already exists. Please choose a different username.");
            return;
        }

        Student newStudent = new Student(name, username, password, studentId);
        try {
            int id = db.insert("INSERT INTO students (name, username, password, student_id, is_active) VALUES (?,?,?,?,1)",
                    name, username, password, studentId);
            newStudent.setId(id);
            students.add(newStudent);
            System.out.println("Student registration completed successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to save the student to the database.");
            e.printStackTrace();
        }
    }

    public Student authenticateStudent(String username, String password) {
        // Logic for student login. Checks if the provided info match any student in the list.
        return students.stream()
                .filter(s -> s.getUsername().equals(username) && s.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    private boolean isUsernameTaken(String username) {
        return students.stream().anyMatch(s -> s.getUsername().equals(username));
    }

    public int getStudentCount() {
        return students.size();
    }

    public List<Student> getStudents() {
        return students;
    }

    public Student getStudent(String username) {
        for (Student student : this.students) {
            if (student.getUsername().equals(username)) {
                return student;
            }
        }

        return null;
    }

    @Override
    public void update(User Old, User New) {
        students.remove((Student) Old);
        students.add((Student) New);

        Student student = (Student) New;
        try {
            db.update("UPDATE students SET name=?, username=?, password=?, student_id=?, is_active=? WHERE id=?",
                    student.getName(), student.getUsername(), student.getPassword(), student.getStudentId(),
                    student.isActive() ? 1 : 0, student.getId());
        } catch (SQLException e) {
            System.out.println("Failed to update the student in the database.");
            e.printStackTrace();
        }
    }

}
