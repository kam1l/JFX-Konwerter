package com.gmail.kamiloleksik.jfxkonwerter.model;

import static com.j256.ormlite.dao.DaoManager.createDao;

import java.sql.SQLException;
import java.util.EnumMap;

import com.gmail.kamiloleksik.jfxkonwerter.model.dto.AppLanguage;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.AppSkin;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.NumberOfDecimalPlaces;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.Preferences;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.Unit;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.UnitType;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.UnitsLanguage;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

public class DaoManager
{
	private ConnectionSource connection;
	private EnumMap<DaoKey, Dao<?, ?>> daos;

	private class SqliteConnection
	{
		public JdbcConnectionSource Connector() throws SQLException
		{
			return new JdbcConnectionSource("jdbc:sqlite:resource.db");
		}
	}

	public enum DaoKey
	{
		UNIT_TYPE_DAO, UNIT_DAO, UNITS_LANGUAGE_DAO, APP_LANGUAGE_DAO, PREFERENCES_DAO, NUMBER_OF_DECIMAL_PLACES_DAO,
		APP_SKIN_DAO
	}

	public DaoManager() throws SQLException
	{
		connection = new SqliteConnection().Connector();
		daos = new EnumMap<DaoKey, Dao<?, ?>>(DaoKey.class);

		daos.put(DaoKey.UNIT_TYPE_DAO, createDao(connection, UnitType.class));
		daos.put(DaoKey.UNIT_DAO, createDao(connection, Unit.class));
		daos.put(DaoKey.UNITS_LANGUAGE_DAO, createDao(connection, UnitsLanguage.class));
		daos.put(DaoKey.APP_LANGUAGE_DAO, createDao(connection, AppLanguage.class));
		daos.put(DaoKey.PREFERENCES_DAO, createDao(connection, Preferences.class));
		daos.put(DaoKey.NUMBER_OF_DECIMAL_PLACES_DAO, createDao(connection, NumberOfDecimalPlaces.class));
		daos.put(DaoKey.APP_SKIN_DAO, createDao(connection, AppSkin.class));
	}

	public Dao<?, ?> get(DaoKey key)
	{
		return daos.get(key);
	}

	public void closeConnection()
	{
		connection.closeQuietly();
	}
}
