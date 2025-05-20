package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.DriverManager;

/**
 * Utility class to initialize the database schema.
 * This should be run once before the application starts.
 */
public class DatabaseInitializer {

    private static final String DB_NAME = "QuizDB";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String HOST = "localhost:3306";

    public static void initializeDatabase() {
        createDatabaseIfNotExists();

        // Then create tables using drop.sql script
        executeSqlScript();
    }

    private static void createDatabaseIfNotExists() {
        String url = "jdbc:mysql://" + HOST;

        try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            System.out.println("Creating database " + DB_NAME + " if not exists...");
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("Database created or already exists.");

        } catch (SQLException e) {
            System.err.println("Error creating database:");
            e.printStackTrace();
        }
    }

    private static void executeSqlScript() {
        String url = "jdbc:mysql://" + HOST + "/" + DB_NAME;

        try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            System.out.println("Executing SQL script to create tables...");
            String sqlFilePath = "src/main/resources/drop.sql";

            StringBuilder sqlScript = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(sqlFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sqlScript.append(line);
                    sqlScript.append("\n");
                }
            } catch (IOException e) {
                System.err.println("Error reading SQL file:");
                e.printStackTrace();
                return;
            }

            String[] sqlStatements = sqlScript.toString().split(";");
            for (String sqlStatement : sqlStatements) {
                if (!sqlStatement.trim().isEmpty()) {
                    stmt.executeUpdate(sqlStatement);
                }
            }

            System.out.println("Database tables created successfully.");

        } catch (SQLException e) {
            System.err.println("Error executing SQL script:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}