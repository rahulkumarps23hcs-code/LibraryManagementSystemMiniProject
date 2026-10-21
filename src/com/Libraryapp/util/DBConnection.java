
package com.Libraryapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static final String USERNAME =
        System.getenv("LIBRARY_DB_USERNAME");

    private static final String PASSWORD =
        System.getenv("LIBRARY_DB_PASSWORD");

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver jar not added to build path", e);
        }

        if (USERNAME == null || PASSWORD == null) {
            throw new SQLException(
                "Database credentials are missing. Set LIBRARY_DB_USERNAME and LIBRARY_DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
