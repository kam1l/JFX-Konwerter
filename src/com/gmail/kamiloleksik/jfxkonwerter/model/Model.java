package com.gmail.kamiloleksik.jfxkonwerter.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gmail.kamiloleksik.jfxkonwerter.model.converter.BasicConverter;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.Converter;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.InputValue;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.NumberBaseConverter;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.TemperatureConverter;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberBaseException;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberFormatException;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.AppLanguage;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.AppSkin;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.NumberOfDecimalPlaces;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.Preferences;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.Unit;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.UnitType;
import com.gmail.kamiloleksik.jfxkonwerter.model.dto.UnitsLanguage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model
{
	private Connection connection;
	private Preferences preferences;
	private Map<String, Double> updatedExchangeRates;
	private String currentUnitTypeClassifier;
	private int defaultUnitTypeIndex, preferencesUnitTypeIndex;

	private List<UnitType> allUnitTypes = new ArrayList<UnitType>();
	private List<Unit> allUnits = new ArrayList<Unit>();
	private List<Unit> mainWindowUnits = new ArrayList<Unit>();
	private List<Unit> preferencesUnits = new ArrayList<Unit>();

	private List<AppLanguage> appLanguages = new ArrayList<AppLanguage>();
	private List<UnitsLanguage> unitsLanguages = new ArrayList<UnitsLanguage>();
	private List<AppSkin> appSkins = new ArrayList<AppSkin>();
	private List<NumberOfDecimalPlaces> numbersOfDecimalPlaces = new ArrayList<NumberOfDecimalPlaces>();

	private AppLanguage appLanguage;
	private UnitsLanguage unitsLanguage;
	private AppSkin appSkin;
	private NumberOfDecimalPlaces numberOfDecimalPlaces;

	private EnumMap<UnitTypeKey, UnitType> unitTypes = new EnumMap<UnitTypeKey, UnitType>(UnitTypeKey.class);
	private EnumMap<UnitKey, Unit> units = new EnumMap<UnitKey, Unit>(UnitKey.class);
	private EnumMap<NamesKey, ObservableList<String>> names = new EnumMap<NamesKey, ObservableList<String>>(
			NamesKey.class);

	public enum UnitTypeKey
	{
		CURRENT_UNIT_TYPE, DEFAULT_UNIT_TYPE
	}

	public enum UnitKey
	{
		CURRENT_FIRST_UNIT, CURRENT_SECOND_UNIT, DEFAULT_FIRST_UNIT, DEFAULT_SECOND_UNIT
	}

	public enum NamesKey
	{
		MAIN_WINDOW_UNIT_NAMES, PREFERENCES_UNIT_NAMES, ALL_UNIT_TYPE_NAMES, APP_LANGUAGES_NAMES, UNITS_LANGUAGES_NAMES,
		APP_SKINS_NAMES, ALLOWED_NUMBERS_OF_DECIMAL_PLACES
	}

	public Model() throws SQLException
	{
		connection = SqliteConnection.Connector();

		if (connection == null)
		{
			throw new SQLException();
		}

		initializeRamDataStructures();
	}

	public String convertValue(String userInput) throws InvalidNumberFormatException, InvalidNumberBaseException
	{
		Converter converter;
		int numberOfDecimalPlaces = getNumberOfDecimalPlaces();

		if (currentUnitsAreBasic())
		{
			String firstUnitRatio = String.valueOf(units.get(UnitKey.CURRENT_FIRST_UNIT).getUnitRatio());
			String secondUnitRatio = String.valueOf(units.get(UnitKey.CURRENT_SECOND_UNIT).getUnitRatio());

			converter = new BasicConverter(firstUnitRatio, secondUnitRatio);
		}
		else if (currentUnitsAreNumberBases())
		{
			String firstNumberBase = String.valueOf((int) units.get(UnitKey.CURRENT_FIRST_UNIT).getUnitRatio());
			String secondNumberBase = String.valueOf((int) units.get(UnitKey.CURRENT_SECOND_UNIT).getUnitRatio());

			converter = new NumberBaseConverter(firstNumberBase, secondNumberBase);
		}
		else
		{
			String firstScaleAbbreviation = getUnitAbbreviation(UnitKey.CURRENT_FIRST_UNIT);
			String secondScaleAbbreviation = getUnitAbbreviation(UnitKey.CURRENT_SECOND_UNIT);

			converter = new TemperatureConverter(firstScaleAbbreviation, secondScaleAbbreviation);
		}

		InputValue<?> value = converter.preprocessUserInput(userInput);

		return converter.doValueConversion(value, numberOfDecimalPlaces);
	}

	private boolean currentUnitsAreBasic()
	{
		return currentUnitTypeClassifier.equals("basic");
	}

	public boolean currentUnitsAreNumberBases()
	{
		return currentUnitTypeClassifier.equals("number");
	}

	private void initializeRamDataStructures() throws SQLException
	{
		preferences = getPreferencesFromDB();
		appLanguages = getAppLanguagesFromDB(preferences.getDefaultAppLanguageId());
		unitsLanguages = getUnitsLanguagesFromDB(preferences.getDefaultUnitsLanguageId());
		appSkins = getAppSkinsFromDB(preferences.getDefaultAppSkinId());
		numbersOfDecimalPlaces = getNumbersOfDecimalPlacesFromDB(preferences.getDefaultNumberOfDecimalPlacesId());
		unitTypes = getUnitTypesFromDB(preferences.getDefaultUnitTypeId());
		int currentUnitTypeId = unitTypes.get(UnitTypeKey.CURRENT_UNIT_TYPE).getUnitTypeId();
		allUnits = getUnitsFromDB(currentUnitTypeId);
	}

	private Preferences getPreferencesFromDB() throws SQLException
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement("select * from Preferences");
				ResultSet resultSet = preparedStatement.executeQuery())
		{
			resultSet.next();

			int preferencesId = resultSet.getInt("preferencesId");
			int defaultNumberOfDecimalPlacesId = resultSet.getInt("defaultNumberOfDecimalPlacesId");
			int defaultUnitTypeId = resultSet.getInt("defaultUnitTypeId");
			int defaultFirstUnitId = resultSet.getInt("defaultFirstUnitId");
			int defaultSecondUnitId = resultSet.getInt("defaultSecondUnitId");
			int defaultAppLanguageId = resultSet.getInt("defaultAppLanguageId");
			int defaultUnitsLanguageId = resultSet.getInt("defaultUnitsLanguageId");
			int defaultAppSkinId = resultSet.getInt("defaultAppSkinId");

			return new Preferences(preferencesId, defaultNumberOfDecimalPlacesId, defaultUnitTypeId, defaultFirstUnitId,
					defaultSecondUnitId, defaultAppLanguageId, defaultUnitsLanguageId, defaultAppSkinId);
		}
	}

	private List<AppLanguage> getAppLanguagesFromDB(int defaultAppLanguageId) throws SQLException
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("select * from AppLanguage order by appLanguageName asc");
				ResultSet resultSet = preparedStatement.executeQuery())
		{
			ObservableList<String> nameList = FXCollections.observableArrayList();
			List<AppLanguage> aLanguages = new ArrayList<AppLanguage>();

			while (resultSet.next())
			{
				int appLanguageId = resultSet.getInt("appLanguageId");
				String appLanguageName = resultSet.getString("appLanguageName");
				AppLanguage appLanguage = new AppLanguage(appLanguageId, appLanguageName);

				aLanguages.add(appLanguage);
				nameList.add(appLanguageName);

				if (appLanguageId == defaultAppLanguageId)
				{
					this.appLanguage = appLanguage;
				}
			}

			names.put(NamesKey.APP_LANGUAGES_NAMES, nameList);

			return aLanguages;
		}
	}

	private List<UnitsLanguage> getUnitsLanguagesFromDB(int defaultUnitsLanguageId) throws SQLException
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("select * from UnitsLanguage order by unitsLanguageName asc");
				ResultSet resultSet = preparedStatement.executeQuery())
		{
			ObservableList<String> nameList = FXCollections.observableArrayList();
			List<UnitsLanguage> uLanguages = new ArrayList<UnitsLanguage>();

			while (resultSet.next())
			{
				int unitsLanguageId = resultSet.getInt("unitsLanguageId");
				String unitsLanguageName = resultSet.getString("unitsLanguageName");
				UnitsLanguage unitsLanguage = new UnitsLanguage(unitsLanguageId, unitsLanguageName);

				uLanguages.add(unitsLanguage);
				nameList.add(unitsLanguageName);

				if (unitsLanguageId == defaultUnitsLanguageId)
				{
					this.unitsLanguage = unitsLanguage;
				}
			}

			names.put(NamesKey.UNITS_LANGUAGES_NAMES, nameList);

			return uLanguages;
		}
	}

	private List<AppSkin> getAppSkinsFromDB(int defaultAppSkinId) throws SQLException
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("select * from AppSkin order by appSkinName asc");
				ResultSet resultSet = preparedStatement.executeQuery())
		{
			ObservableList<String> nameList = FXCollections.observableArrayList();
			List<AppSkin> aSkins = new ArrayList<AppSkin>();

			while (resultSet.next())
			{
				int appSkinId = resultSet.getInt("appSkinId");
				String appSkinName = resultSet.getString("appSkinName");
				String appSkinPath = resultSet.getString("appSkinPath");
				AppSkin appSkin = new AppSkin(appSkinId, appSkinName, appSkinPath);

				aSkins.add(appSkin);
				nameList.add(appSkinName);

				if (appSkinId == defaultAppSkinId)
				{
					this.appSkin = appSkin;
				}
			}

			names.put(NamesKey.APP_SKINS_NAMES, nameList);

			return aSkins;
		}
	}

	private List<NumberOfDecimalPlaces> getNumbersOfDecimalPlacesFromDB(int defaultNumberOfDecimalPlacesId)
			throws SQLException
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("select * from NumberOfDecimalPlaces order by numberOfDecimalPlaces asc");
				ResultSet resultSet = preparedStatement.executeQuery())
		{
			ObservableList<String> nameList = FXCollections.observableArrayList();
			List<NumberOfDecimalPlaces> numsOfDecPlaces = new ArrayList<NumberOfDecimalPlaces>();

			while (resultSet.next())
			{
				int numberOfDecimalPlacesId = resultSet.getInt("numberOfDecimalPlacesId");
				int numberOfDecimalPlaces = resultSet.getInt("numberOfDecimalPlaces");
				NumberOfDecimalPlaces numberOfDecimalPlacesObject = new NumberOfDecimalPlaces(numberOfDecimalPlacesId,
						numberOfDecimalPlaces);

				numsOfDecPlaces.add(numberOfDecimalPlacesObject);
				nameList.add(String.valueOf(numberOfDecimalPlaces));

				if (numberOfDecimalPlacesId == defaultNumberOfDecimalPlacesId)
				{
					this.numberOfDecimalPlaces = numberOfDecimalPlacesObject;
				}
			}

			names.put(NamesKey.ALLOWED_NUMBERS_OF_DECIMAL_PLACES, nameList);

			return numsOfDecPlaces;
		}
	}

	private EnumMap<UnitTypeKey, UnitType> getUnitTypesFromDB(int defaultUnitTypeId) throws SQLException
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("select * from UnitType order by unitTypeName asc");
				ResultSet resultSet = preparedStatement.executeQuery())
		{
			EnumMap<UnitTypeKey, UnitType> uTypes = new EnumMap<UnitTypeKey, UnitType>(UnitTypeKey.class);
			ObservableList<String> nameList = FXCollections.observableArrayList();

			while (resultSet.next())
			{
				int unitTypeId = resultSet.getInt("unitTypeId");
				String unitTypeName = resultSet.getString("unitTypeName");
				String classifier = resultSet.getString("classifier");
				UnitType unitType = new UnitType(unitTypeId, unitTypeName, classifier);

				allUnitTypes.add(unitType);
				nameList.add(unitTypeName);

				if (unitTypeId == defaultUnitTypeId)
				{
					uTypes.put(UnitTypeKey.CURRENT_UNIT_TYPE, new UnitType(unitType));
					uTypes.put(UnitTypeKey.DEFAULT_UNIT_TYPE, new UnitType(unitType));
					defaultUnitTypeIndex = allUnitTypes.size() - 1;
					preferencesUnitTypeIndex = defaultUnitTypeIndex;
					currentUnitTypeClassifier = unitType.getUnitTypeClassifier();
				}
			}

			names.put(NamesKey.ALL_UNIT_TYPE_NAMES, nameList);

			return uTypes;
		}
	}

	private List<Unit> getUnitsFromDB(int currentUnitTypeId) throws SQLException
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("select * from Unit order by unitName asc");
				ResultSet resultSet = preparedStatement.executeQuery())
		{
			List<Unit> aUnits = new ArrayList<Unit>();
			names.put(NamesKey.MAIN_WINDOW_UNIT_NAMES, FXCollections.observableArrayList());
			names.put(NamesKey.PREFERENCES_UNIT_NAMES, FXCollections.observableArrayList());

			while (resultSet.next())
			{
				int unitId = resultSet.getInt("unitId");
				String unitName = resultSet.getString("unitName");
				String unitAbbreviation = resultSet.getString("unitAbbreviation");
				String displayName = getDisplayName(unitName, unitAbbreviation);
				double unitRatio = resultSet.getDouble("unitRatio");
				int unitType_unitTypeId = resultSet.getInt("unitType_unitTypeId");
				Unit unit = new Unit(unitId, unitName, unitAbbreviation, displayName, unitRatio, unitType_unitTypeId);

				aUnits.add(unit);

				if (unitType_unitTypeId == currentUnitTypeId)
				{
					initializeStaticUnitObjects(unit);
				}
			}

			return aUnits;
		}
	}

	private void initializeStaticUnitObjects(Unit unit)
	{
		int defaultFirstUnitId = preferences.getDefaultFirstUnitId();
		int defaultSecondUnitId = preferences.getDefaultSecondUnitId();

		addItemToUnitNames(unit, NamesKey.MAIN_WINDOW_UNIT_NAMES);
		addItemToUnitNames(unit, NamesKey.PREFERENCES_UNIT_NAMES);

		mainWindowUnits.add(unit);
		preferencesUnits.add(unit);

		int unitId = unit.getUnitId();

		if (unitId == defaultFirstUnitId)
		{
			setUnit(unit, UnitKey.CURRENT_FIRST_UNIT);
			setUnit(unit, UnitKey.DEFAULT_FIRST_UNIT);
		}
		if (unitId == defaultSecondUnitId)
		{
			setUnit(unit, UnitKey.CURRENT_SECOND_UNIT);
			setUnit(unit, UnitKey.DEFAULT_SECOND_UNIT);
		}
	}

	public boolean updateExchangeRates()
	{
		NodeList nList;

		try
		{
			nList = getExchangeRatesFromServer();
			updateExchangeRatesInDB(nList);

			return true;
		}
		catch (ParserConfigurationException | SAXException | IOException | SQLException e)
		{
			return false;
		}
	}

	private NodeList getExchangeRatesFromServer()
			throws ParserConfigurationException, MalformedURLException, SAXException, IOException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new URL("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml").openStream());
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("Cube");

		return nList;
	}

	private void updateExchangeRatesInDB(NodeList nList) throws SQLException
	{
		updatedExchangeRates = new HashMap<String, Double>();

		for (int tmp = 2; tmp < nList.getLength(); tmp++)
		{
			Node nNode = nList.item(tmp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element eElement = (Element) nNode;
				String currentCurrency = eElement.getAttribute("currency");
				double currentRate = 1.0 / Double.parseDouble(eElement.getAttribute("rate"));

				updateExchangeRateSingleRowInDB(currentCurrency, currentRate);
				updatedExchangeRates.put(currentCurrency, currentRate);
			}
		}
	}

	private void updateExchangeRateSingleRowInDB(String symbol, double rate) throws SQLException
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("update Unit set unitRatio = ? where unitAbbreviation = ?"))
		{
			preparedStatement.setDouble(1, rate);
			preparedStatement.setString(2, symbol);

			preparedStatement.executeUpdate();
		}
	}

	public void updateExchangeRatesInRam()
	{
		String currenFirstUnitAbbreviation = units.get(UnitKey.CURRENT_FIRST_UNIT).getUnitAbbreviation();
		String currentSecondUnitAbbreviation = units.get(UnitKey.CURRENT_SECOND_UNIT).getUnitAbbreviation();
		String unitAbbreviation;
		double newValue;

		for (Unit unit : allUnits)
		{
			unitAbbreviation = unit.getUnitAbbreviation();

			if (updatedExchangeRates.get(unitAbbreviation) != null)
			{
				newValue = updatedExchangeRates.get(unitAbbreviation);
				unit.setUnitRatio(newValue);
			}
		}

		if (updatedExchangeRates.get(currenFirstUnitAbbreviation) != null)
		{
			newValue = updatedExchangeRates.get(currenFirstUnitAbbreviation);
			Unit unit = units.get(UnitKey.CURRENT_FIRST_UNIT);
			unit.setUnitRatio(newValue);

			units.put(UnitKey.CURRENT_FIRST_UNIT, unit);
		}
		if (updatedExchangeRates.get(currentSecondUnitAbbreviation) != null)
		{
			newValue = updatedExchangeRates.get(currentSecondUnitAbbreviation);
			Unit unit = units.get(UnitKey.CURRENT_SECOND_UNIT);
			unit.setUnitRatio(newValue);

			units.put(UnitKey.CURRENT_SECOND_UNIT, unit);
		}
	}

	public boolean updatePreferencesInDB(Preferences newPreferences)
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("update Preferences set defaultNumberOfDecimalPlacesId = ?, defaultUnitTypeId = ?, "
						+ "defaultFirstUnitId = ?, defaultSecondUnitId = ?, defaultAppLanguageId = ?, "
						+ "defaultUnitsLanguageId = ?, defaultAppSkinId = ? where preferencesId = ?"))
		{
			preparedStatement.setInt(1, newPreferences.getDefaultNumberOfDecimalPlacesId());
			preparedStatement.setInt(2, newPreferences.getDefaultUnitTypeId());
			preparedStatement.setInt(3, newPreferences.getDefaultFirstUnitId());
			preparedStatement.setInt(4, newPreferences.getDefaultSecondUnitId());
			preparedStatement.setInt(5, newPreferences.getDefaultAppLanguageId());
			preparedStatement.setInt(6, newPreferences.getDefaultUnitsLanguageId());
			preparedStatement.setInt(7, newPreferences.getDefaultAppSkinId());
			preparedStatement.setInt(8, newPreferences.getPreferencesId());

			preparedStatement.executeUpdate();

			return true;
		}
		catch (SQLException e)
		{
			return false;
		}
	}

	public void changePreferencesSetOfUnits(int index)
	{
		preferencesUnits.clear();

		UnitType unitType = allUnitTypes.get(index);
		int typeId = unitType.getUnitTypeId();

		preferencesUnits = getUnitLists(NamesKey.PREFERENCES_UNIT_NAMES, typeId);
	}

	public void changeMainWindowSetOfUnits(int index)
	{
		UnitType unitType = allUnitTypes.get(index);

		mainWindowUnits.clear();
		unitTypes.put(UnitTypeKey.CURRENT_UNIT_TYPE, new UnitType(unitType));
		currentUnitTypeClassifier = unitType.getUnitTypeClassifier();

		int currentUnitTypeId = unitTypes.get(UnitTypeKey.CURRENT_UNIT_TYPE).getUnitTypeId();

		mainWindowUnits = getUnitLists(NamesKey.MAIN_WINDOW_UNIT_NAMES, currentUnitTypeId);
	}

	private List<Unit> getUnitLists(NamesKey key, int typeId)
	{
		List<Unit> unitsL = new ArrayList<Unit>();
		names.put(key, FXCollections.observableArrayList());

		for (Unit unit : allUnits)
		{
			if (unit.getUnitType_unitTypeId() == typeId)
			{
				addItemToUnitNames(unit, key);
				unitsL.add(unit);
			}
		}

		return unitsL;
	}

	private void addItemToUnitNames(Unit unit, NamesKey key)
	{
		String displayName = unit.getUnitDisplayName();
		ObservableList<String> namList = names.get(key);
		namList.add(displayName);

		names.put(key, namList);
	}

	private String getDisplayName(String unitName, String unitAbbreviation)
	{
		if (unitAbbreviation != null)
		{
			return unitName + " [" + unitAbbreviation + "]";
		}
		else
		{
			return unitName;
		}
	}

	public void setDefaultUnitType(int index)
	{
		UnitType unitType = allUnitTypes.get(index);
		unitTypes.put(UnitTypeKey.DEFAULT_UNIT_TYPE, new UnitType(unitType));
	}

	public void setUnit(int index, UnitKey key)
	{
		Unit unit;

		if (key.equals(UnitKey.DEFAULT_FIRST_UNIT) || key.equals(UnitKey.DEFAULT_SECOND_UNIT))
		{
			unit = preferencesUnits.get(index);
		}
		else
		{
			unit = mainWindowUnits.get(index);
		}

		setUnit(unit, key);
	}

	private void setUnit(Unit unit, UnitKey key)
	{
		units.put(key, new Unit(unit));
	}

	public void setNumberOfDecimalPlaces(int index)
	{
		numberOfDecimalPlaces = numbersOfDecimalPlaces.get(index);
	}

	public void setAppLanguage(int index)
	{
		appLanguage = appLanguages.get(index);
	}

	public void setUnitsLanguage(int index)
	{
		unitsLanguage = unitsLanguages.get(index);
	}

	public void setAppSkin(int index)
	{
		appSkin = appSkins.get(index);
	}

	public String getUnitDisplayName(UnitKey key)
	{
		return units.get(key).getUnitDisplayName();
	}

	public UnitType getUnitType(int index)
	{
		return allUnitTypes.get(index);
	}

	public Unit getPreferencesUnit(int index)
	{
		return preferencesUnits.get(index);
	}

	public int getPreferencesId()
	{
		return preferences.getPreferencesId();
	}

	private String getUnitAbbreviation(UnitKey key)
	{
		return units.get(key).getUnitAbbreviation();
	}

	public int getNumberOfDecimalPlaces()
	{
		return numberOfDecimalPlaces.getNumberOfDecimalPlaces();
	}

	public String getAppSkinName()
	{
		return appSkin.getAppSkinName();
	}

	public String getAppSkinPath()
	{
		return appSkin.getAppSkinPath();
	}

	public String getAppLanguageName()
	{
		return appLanguage.getAppLanguageName();
	}

	public String getUnitsLanguageName()
	{
		return unitsLanguage.getUnitsLanguageName();
	}

	public void setPreferences(Preferences prefs)
	{
		preferences = prefs;
	}

	public Preferences getPreferences()
	{
		return preferences;
	}

	public int getDefaultUnitTypeIndex()
	{
		return defaultUnitTypeIndex;
	}

	public void setDefaultUnitTypeIndex(int defaultUnitTypeIndex)
	{
		this.defaultUnitTypeIndex = defaultUnitTypeIndex;
	}

	public int getPreferencesUnitTypeIndex()
	{
		return preferencesUnitTypeIndex;
	}

	public void setPreferencesUnitTypeIndex(int preferencesUnitTypeIndex)
	{
		this.preferencesUnitTypeIndex = preferencesUnitTypeIndex;
	}

	public AppLanguage getAppLanguages(int index)
	{
		return appLanguages.get(index);
	}

	public UnitsLanguage getUnitsLanguages(int index)
	{
		return unitsLanguages.get(index);
	}

	public AppSkin getAppSkins(int index)
	{
		return appSkins.get(index);
	}

	public NumberOfDecimalPlaces getNumbersOfDecimalPlaces(int index)
	{
		return numbersOfDecimalPlaces.get(index);
	}

	public ObservableList<String> getNames(NamesKey key)
	{
		return names.get(key);
	}

	public String getUnitTypeName(UnitTypeKey key)
	{
		return unitTypes.get(key).getUnitTypeName();
	}

	public boolean dbIsConnected()
	{
		try
		{
			return !connection.isClosed();
		}
		catch (SQLException e)
		{
			return false;
		}
	}
}