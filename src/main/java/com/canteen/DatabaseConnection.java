package com.canteen;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:canteen.db";

    public static Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(URL);

        // Enable foreign key checking
        con.createStatement().execute("PRAGMA foreign_keys = ON");

        return con;
    }
}