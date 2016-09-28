package application.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

import application.model.converter.BasicConverter;
import application.model.converter.Converter;
import application.model.converter.InvalidNumberBaseException;
import application.model.converter.InvalidNumberFormatException;
import application.model.converter.NumberBaseConverter;
import application.model.converter.TemperatureConverter;
import application.model.dto.Preferences;
import application.model.dto.Unit;
import application.model.dto.UnitType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model
{
	private Connection connection;
	private static Preferences preferences;
	private static String currentUnitTypeClassifier;

	private static List<UnitType> allUnitTypes = new ArrayList<UnitType>();
	private static List<Unit> allUnits = new ArrayList<Unit>();
	private static Map<String, Double> updatedExchangeRates;

	private static List<Unit> mainWindowUnits = new ArrayList<Unit>();
	private static List<Unit> preferencesUnits = new ArrayList<Unit>();

	private static Map<String, UnitType> unitTypes = new HashMap<String, UnitType>();
	private static Map<String, Unit> units = new HashMap<String, Unit>();
	private static Map<String, ObservableList<String>> names = new HashMap<String, ObservableList<String>>();

	static
	{
		names.put("mainWindowUnitNames", FXCollections.observableArrayList());
		names.put("preferencesUnitNames", FXCollections.observableArrayList());
	}

	public Model() throws SQLException
	{
		connection = SqliteConnection.Connector();

		if (connection == null)
		{
			throw new SQLException();
		}
	}

	public String convertValue(String userInput) throws InvalidNumberFormatException, InvalidNumberBaseException
	{
		Converter converter;
		int numberOfDecimalPlaces = getNumberOfDecimalPlaces();

		if (currentUnitsAreBasic())
		{
			double firstUnitRatio = units.get("currentFirstUnit").getUnitRatio();
			double secondUnitRatio = units.get("currentSecondUnit").getUnitRatio();

			converter = new BasicConverter(firstUnitRatio, secondUnitRatio);
		}
		else if (currentUnitsAreNumberBases())
		{
			int firstNumberBase = (int) units.get("currentFirstUnit").getUnitRatio();
			int secondNumberBase = (int) units.get("currentSecondUnit").getUnitRatio();

			converter = new NumberBaseConverter(firstNumberBase, secondNumberBase);
		}
		else
		{
			String firstScaleAbbreviation = getUnitAbbreviation("currentFirstUnit");
			String secondScaleAbbreviation = getUnitAbbreviation("currentSecondUnit");

			converter = new TemperatureConverter(firstScaleAbbreviation, secondScaleAbbreviation);
		}

		return converter.doValueConversion(userInput, numberOfDecimalPlaces);
	}

	private boolean currentUnitsAreBasic()
	{
		return currentUnitTypeClassifier.equals("basic");
	}

	private boolean currentUnitsAreNumberBases()
	{
		return currentUnitTypeClassifier.equals("number");
	}

	public void initializeRamDataStructures() throws SQLException
	{
		preferences = getPreferencesFromDB();
		unitTypes = getUnitTypesFromDB(preferences.getDefaultUnitTypeId());
		int currentUnitTypeId = unitTypes.get("currentUnitType").getUnitTypeId();
		allUnits = getUnitsFromDB(currentUnitTypeId);
	}

	private Preferences getPreferencesFromDB() throws SQLException
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement("select * from Preferences");
				ResultSet resultSet = preparedStatement.executeQuery())
		{
			resultSet.next();

			int preferencesId = resultSet.getInt("preferencesId");
			int numberOfDecimalPlaces = resultSet.getInt("numberOfDecimalPlaces");
			int defaultUnitTypeId = resultSet.getInt("defaultUnitTypeId");
			int defaultFirstUnitId = resultSet.getInt("defaultFirstUnitId");
			int defaultSecondUnitId = resultSet.getInt("defaultSecondUnitId");
			String defaultSkinName = resultSet.getString("defaultSkinName");

			return new Preferences(preferencesId, numberOfDecimalPlaces, defaultUnitTypeId, defaultFirstUnitId,
					defaultSecondUnitId, defaultSkinName);
		}
	}

	private Map<String, UnitType> getUnitTypesFromDB(int defaultUnitTypeId) throws SQLException
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("select * from UnitType order by unitTypeName asc");
				ResultSet resultSet = preparedStatement.executeQuery())
		{
			Map<String, UnitType> uTypes = new HashMap<String, UnitType>();
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
					uTypes.put("currentUnitType", new UnitType(unitType));
					uTypes.put("defaultUnitType", new UnitType(unitType));
					currentUnitTypeClassifier = unitType.getUnitTypeClassifier();
				}
			}

			names.put("allUnitTypeNames", nameList);

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

		addItemToUnitNames(unit, "mainWindowUnitNames");
		addItemToUnitNames(unit, "preferencesUnitNames");

		mainWindowUnits.add(unit);
		preferencesUnits.add(unit);

		int unitId = unit.getUnitId();

		if (unitId == defaultFirstUnitId)
		{
			setUnit(unit, "currentFirstUnit");
			setUnit(unit, "defaultFirstUnit");
		}
		if (unitId == defaultSecondUnitId)
		{
			setUnit(unit, "currentSecondUnit");
			setUnit(unit, "defaultSecondUnit");
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

				updateExRaSingleRowInDB(currentCurrency, currentRate);
				updatedExchangeRates.put(currentCurrency, currentRate);
			}
		}
	}

	private void updateExRaSingleRowInDB(String symbol, double rate) throws SQLException
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
		String currenFirstUnitAbbreviation = units.get("currentFirstUnit").getUnitAbbreviation();
		String currentSecondUnitAbbreviation = units.get("currentSecondUnit").getUnitAbbreviation();
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
			Unit unit = units.get("currentFirstUnit");
			unit.setUnitRatio(newValue);

			units.put("currentFirstUnit", unit);
		}
		if (updatedExchangeRates.get(currentSecondUnitAbbreviation) != null)
		{
			newValue = updatedExchangeRates.get(currentSecondUnitAbbreviation);
			Unit unit = units.get("currentSecondUnit");
			unit.setUnitRatio(newValue);

			units.put("currentSecondUnit", unit);
		}
	}

	public boolean updatePreferencesInDB(Preferences newPreferences)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"update Preferences set numberOfDecimalPlaces = ?, defaultUnitTypeId = ?, defaultFirstUnitId = ?, "
						+ "defaultSecondUnitId = ?, defaultSkinName = ? where preferencesId = ?"))
		{
			preparedStatement.setInt(1, newPreferences.getNumberOfDecimalPlaces());
			preparedStatement.setInt(2, newPreferences.getDefaultUnitTypeId());
			preparedStatement.setInt(3, newPreferences.getDefaultFirstUnitId());
			preparedStatement.setInt(4, newPreferences.getDefaultSecondUnitId());
			preparedStatement.setString(5, newPreferences.getDefaultSkinName());
			preparedStatement.setInt(6, newPreferences.getPreferencesId());

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
		names.put("preferencesUnitNames", FXCollections.observableArrayList());
		preferencesUnits.clear();

		UnitType unitType = allUnitTypes.get(index);
		int typeId = unitType.getUnitTypeId();

		preferencesUnits = getUnitLists("preferencesUnitNames", typeId);
	}

	public void changeMainWindowSetOfUnits(int index)
	{
		UnitType unitType = allUnitTypes.get(index);

		names.put("mainWindowUnitNames", FXCollections.observableArrayList());
		mainWindowUnits.clear();

		unitTypes.put("currentUnitType", new UnitType(unitType));
		currentUnitTypeClassifier = unitType.getUnitTypeClassifier();

		int currentUnitTypeId = unitTypes.get("currentUnitType").getUnitTypeId();

		mainWindowUnits = getUnitLists("mainWindowUnitNames", currentUnitTypeId);
	}

	private List<Unit> getUnitLists(String key, int typeId)
	{
		List<Unit> unitsL = new ArrayList<Unit>();

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

	private void addItemToUnitNames(Unit unit, String key)
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
		unitTypes.put("defaultUnitType", new UnitType(unitType));
	}

	public void setUnit(int index, String key)
	{
		Unit unit;

		if (key.matches("^default.+"))
		{
			unit = preferencesUnits.get(index);
		}
		else
		{
			unit = mainWindowUnits.get(index);
		}

		setUnit(unit, key);
	}

	private void setUnit(Unit unit, String key)
	{
		units.put(key, new Unit(unit));
	}

	public String getUnitDisplayName(String key)
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

	private String getUnitAbbreviation(String key)
	{
		return units.get(key).getUnitAbbreviation();
	}

	public static int getNumberOfDecimalPlaces()
	{
		return preferences.getNumberOfDecimalPlaces();
	}

	public void setPreferences(Preferences prefs)
	{
		preferences = prefs;
	}

	public static String getCurrentUnitTypeClassifier()
	{
		return currentUnitTypeClassifier;
	}

	public ObservableList<String> getNames(String key)
	{
		return names.get(key);
	}

	public String getUnitTypeName(String key)
	{
		return unitTypes.get(key).getUnitTypeName();
	}

	public Preferences getPreferences()
	{
		return preferences;
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