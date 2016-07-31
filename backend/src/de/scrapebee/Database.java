package de.scrapebee;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Datenbank Klasse
 *
 */
public class Database {
	private static Connection connect = null;
	private final static String host = "127.0.0.1";
	public final static String DB_LANGUAGE = "language";
	public final static String DB_ARTICLES = "articles";
	private final static String user = "root";
	private final static String pass = "";

	/**
	 * Stellt eine DB Verbindung her
	 * @param database Datenbank Name
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void connect(String database) throws SQLException,
			ClassNotFoundException {

		// prüfen ob Verbindung bereits besteht
		if (connect != null) {
			if (!connect.isClosed()) {
				return;
			}
		}

		try {
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			connect = DriverManager.getConnection("jdbc:mysql://" + host + "/"
					+ database + "?" + "user=" + user + "&password=" + pass);

		} catch (SQLException e) {
			if (connect != null)
				connect.close();
			throw e;
		}
	}

	/**
	 * Setzt ein SQL Statement ab und liefert das Ergebnis aus DB
	 * @param sql Statement
	 * @return Ergebnis
	 * @throws SQLException
	 */
	public static ResultSet read(String sql) throws SQLException {
		Statement statement = connect.createStatement();
		// Result set get the result of the SQL query
		ResultSet resultSet = statement.executeQuery(sql);
		return resultSet;
	}

	/**
	 * Aktualisiert einen DB Eintrag mittels SQL Statement
	 * @param sql Statement
	 * @throws SQLException
	 */
	public static void update(String sql) throws SQLException {
		// Statements allow to issue SQL queries to the database
		Statement statement = connect.createStatement();

		statement.executeUpdate(sql);

	}

	/**
	 * Fügt eine Zeile zu einer Tabelle hinzu mittels SQL Statement
	 * @param sql Statement
	 * @throws SQLException
	 */
	public static void insert(String sql) throws SQLException {
		// Statements allow to issue SQL queries to the database
		Statement statement = connect.createStatement();

		statement.executeUpdate(sql);

	}
}
