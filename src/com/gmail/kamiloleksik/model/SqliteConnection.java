package com.gmail.kamiloleksik.model;

import java.sql.Connection;
import java.sql.DriverManager;

public class SqliteConnection
{
	public static Connection Connector()
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:resource.db");
			return conn;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
