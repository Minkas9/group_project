package repository;

import service.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseRepository {

    private final static String URl = "jdbc:postgresql://localhost:5432/class_project";
    private final static String USER = "postgres";
    private final static String PASSWORD = "mama123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URl, USER, PASSWORD);
    }

    public void executeSql(String sql) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            Logger.log(sql);
            statement.executeUpdate(sql);
            Logger.log("Query executed successfully.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
