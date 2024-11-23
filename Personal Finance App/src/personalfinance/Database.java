package personalfinance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/finance"; // Update with your database details
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = "719483raw"; // Replace with your MySQL password

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Database connection error: " + e.getMessage());
        }
    }
}

