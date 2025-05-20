package database;

import java.io.*;
import java.sql.*;

public class DBConnection {
    private static final String user = "user";
    private static final String password = "password";
    private static final String db_name = "QuizDB";
    private static final String drop_sql_path = "drop.sql";
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/" + db_name, user, password);
    }

    public static void resetTables() throws IOException, SQLException, ClassNotFoundException {
        StringBuilder sb = new StringBuilder();

        //read SQL file content
        try(BufferedReader reader = new BufferedReader(new FileReader(drop_sql_path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        //execute SQL statements
        try(Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            String[] statements = sb.toString().split(";");
            for (String stmt : statements) {
                if (!stmt.trim().isEmpty()) statement.executeUpdate(stmt.trim());
            }
        }
    }
}