package DAO;

import questions.Question;
import user.User;
import quizz.*;
import user.UserAttemptResult;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final Connection jdbcConnection;


    /**
     * Constructor for UserDAO
     * @param connection The connection class. Required: already connected to database
     */
    public UserDAO(Connection connection) {
        this.jdbcConnection = connection;
    }


    /**
     * Adds new User into the database.
     * @param user User object to add to the database. Required: defined parameters except id
     * @throws SQLException If a database error occurs, or User class is not defined properly.
     */
    public void createUser(User user) throws SQLException {
        try{
            String query = "INSERT INTO users (username, hashedPassword, isAdmin, firstName, lastName) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertingStatement = jdbcConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            insertingStatement.setString(1, user.getUsername());
            insertingStatement.setString(2, user.getPassword());
            insertingStatement.setBoolean(3, user.hasAdminPrivileges());
            insertingStatement.setString(4, user.getFirstname());
            insertingStatement.setString(5, user.getLastname());

            insertingStatement.execute();

            ResultSet generatedKeys = insertingStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                user.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            throw new SQLException("Error creating user: " + e.getMessage());
        }


    }

    /**
     * Removes Existing User from the database.
     * @param userId The unique User ID generated by the DB.
     * @throws SQLException If a database error occurs.
     */
    public void removeUser(long userId) throws SQLException {
        try {
            String query = "DELETE FROM users WHERE id = ?";
            PreparedStatement deletingStatement = jdbcConnection.prepareStatement(query);
            deletingStatement.setLong(1, userId);
            deletingStatement.execute();
        } catch (SQLException e) {
            throw new SQLException("Error deleting user: " + e.getMessage());
        }
    }

    /**
     * Finds a user in the database by unique User ID.
     * SQL query uses 'users' table.
     * @param userId The unique User ID generated by the DB.
     * @return User object of the user. If no user was found, returns empty user.
     * @throws SQLException If a database error occurs.
     */
    public User getUser(long userId) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";
        PreparedStatement selectingStatement = jdbcConnection.prepareStatement(query);
        selectingStatement.setLong(1, userId);

        ResultSet resultSet = selectingStatement.executeQuery();
        if(resultSet.next())
            return extractUserFromResultSet(resultSet);
        else return new User();
    }

    /**
     * Finds a user in the database by unique Username.
     * SQL query uses 'users' table.
     * @param username a unique Username of the user.
     * @return User object of the user. If no user was found, returns empty user.
     * @throws SQLException If a database error occurs.
     */
    public User getUser(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?;";
        PreparedStatement selectingStatement = jdbcConnection.prepareStatement(query);
        selectingStatement.setString(1, username);

        ResultSet resultSet = selectingStatement.executeQuery();
        if(resultSet.next())
            return extractUserFromResultSet(resultSet);
        else
            return new User();
    }

    /**
     * Helper method Extracts User from ResultSet given by query.
     * @param resultSet The ResultSet of parameters for the User.
     *                  Required: resultSet must have the only 1 value,
     *                      and resultSet.next() should've been called before.
     *
     * @return User object from ResultSet
     * @throws SQLException If ResultSet is not called properly.
     */
    private User extractUserFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String username = resultSet.getString("username");
        String hashedPassword = resultSet.getString("hashedPassword");
        boolean hasAdminPrivileges = resultSet.getBoolean("isAdmin");
        String firstname = resultSet.getString("firstName");
        String lastname = resultSet.getString("lastName");

        return new User(id, username, hashedPassword, firstname, lastname, hasAdminPrivileges);
    }


    /**
     * Gives List of created quizzes by the User.
     * SQL query uses 'quizzes' table.
     * @param userId The User ID of the creator.
     * @return List of quizz-es in quizz object
     * @throws SQLException If a database error occurs.
     */
    public List<quizz> getCreatedQuizzes(long userId) throws SQLException {
        List<quizz> quizzes = new ArrayList<>();

        String query = "SELECT * FROM quizzes WHERE author = ?;";
        PreparedStatement userQuizzesStatement = jdbcConnection.prepareStatement(query);
        userQuizzesStatement.setLong(1, userId);
        ResultSet resultSet = userQuizzesStatement.executeQuery();

        QuizzDAO quizzDAO = new QuizzDAO(jdbcConnection);
        while(resultSet.next()) {
            long quizId = resultSet.getLong("id");
            quizzes.add(quizzDAO.getQuizzById(quizId));
        }

        return quizzes;
    }

    /**
     * Adds Attempt made by User after taking the quiz
     * SQL query uses 'quizHistory' table.
     * @param userAttemptResult Result of a user as UserAttemptResult object
     * @throws SQLException If a database error occurs.
     */
    public void addAttempt(UserAttemptResult userAttemptResult) throws SQLException {
        String query = "INSERT INTO quizHistory (quizId, userId, score, attemptTime) VALUES (?, ?, ?, ?);";

        PreparedStatement insertingStatement = jdbcConnection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
        insertingStatement.setLong(1, userAttemptResult.getQuizId());
        insertingStatement.setLong(2, userAttemptResult.getUserId());
        insertingStatement.setDouble(3, userAttemptResult.getScore());
        insertingStatement.setTimestamp(4, userAttemptResult.getTimeSpent());

        insertingStatement.execute();

        ResultSet generatedKeys = insertingStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            long id = generatedKeys.getLong(1);
            userAttemptResult.setId(id);
        } else throw new SQLException("Failed to retrieve the generated userAttempt ID");
    }

    /**
     * Gets List of Attempts made by The User.
     * SQL query uses 'quizHistory' Table.
     * @param userId the unique user ID generated by the DB.
     * @return Attempt Results of all quizzes of the user in the List of UserAttemptResult object
     * @throws SQLException If a database error occurs.
     */
    public List<UserAttemptResult> getAttempts(long userId) throws SQLException {
        List<UserAttemptResult> attempts = new ArrayList<>();

        String query = "SELECT * FROM quizHistory WHERE userId = ?;";
        PreparedStatement selectingStatement = jdbcConnection.prepareStatement(query);
        selectingStatement.setLong(1, userId);
        ResultSet resultSet = selectingStatement.executeQuery();

        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            long quizId = resultSet.getLong("quizId");
            long attemptingUserId = resultSet.getLong("userId");
            double score = resultSet.getDouble("score");
            Timestamp attemptTime = resultSet.getTimestamp("attemptTime");

            UserAttemptResult attemptingUser = new UserAttemptResult(id, quizId, attemptingUserId, score, attemptTime);
            attempts.add(attemptingUser);
        }

        return attempts;
    }

    /**
     * Modifies admin Privileges of the User
     * @param userId unique User ID by the DB.
     * @param admin new true or false value to define user admin state
     * @throws SQLException If a database error occurs. (Incorrect: connection, userId)
     */
    public void setAdminPrivileges(long userId, boolean admin) throws SQLException {
        String adminQuery = "UPDATE users SET isAdmin = ? WHERE id = ?;";

        PreparedStatement updateStatement = jdbcConnection.prepareStatement(adminQuery);
        updateStatement.setBoolean(1, admin);
        updateStatement.setLong(2, userId);

        updateStatement.execute();
    }
}
