package ap.projects.finalproject.model;

public class Student extends User {
    private int id;
    private String studentId;
    boolean isActive;

    public Student(String name, String username, String password, String studentId) {
        super(name, username, password);
        this.studentId = studentId;
        this.isActive = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    @Override
    public String toString() {
        return "Name: " + name +
                " | Student ID: " + studentId +
                " | Username: " + username +
                " | isActive: " + isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
