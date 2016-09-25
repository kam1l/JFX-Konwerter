package application.service;

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

import application.dao.Preferences;
import application.dao.SqliteConnection;
import application.dao.Unit;
import application.dao.UnitType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model
{
	private Connection connection;
	private Message message = new Message();

	private static List<UnitType> allUnitTypes = new ArrayList<UnitType>();
	private static List<Unit> allUnits = new ArrayList<Unit>();
	private static Map<String, Double> updatedExchangeRates;

	private static List<Unit> currentMainWindowSetOfUnits = new ArrayList<Unit>();
	private static List<Unit> currentPreferencesSetOfUnits = new ArrayList<Unit>();

	private static ObservableList<String> setOfAllUnitTypeNames = FXCollections.observableArrayList();
	private static ObservableList<String> currentMainWindowSetOfUnitNames = FXCollections.observableArrayList();
	private static ObservableList<String> currentPreferencesSetOfUnitNames = FXCollections.observableArrayList();

	private static UnitType currentUnitType, defaultUnitType;

	private static Unit currentFirstUnit, currentSecondUnit, defaultFirstUnit, defaultSecondUnit;

	private static Preferences preferences;

	private static String currentUnitTypeClassifier;

	public Model()
	{
		connection = SqliteConnection.Connector();

		if (connection == null)
		{
			message.showMessage(Message.ERROR_TITLE, Message.CRITICAL_ERROR_MESSAGE);

			Platform.exit();
			System.exit(-1);
		}
	}

	public String convertValue(String userInput)
	{
		Converter converter;

		if (currentUnitsAreBasic())
		{
			double firstUnitRatio = currentFirstUnit.getUnitRatio();
			double secondUnitRatio = currentSecondUnit.getUnitRatio();

			converter = new BasicConverter(userInput, firstUnitRatio, secondUnitRatio);
		}
		else if (currentUnitsAreNumberBases())
		{
			int firstNumberBase = (int) currentFirstUnit.getUnitRatio();
			int secondNumberBase = (int) currentSecondUnit.getUnitRatio();

			converter = new NumberBaseConverter(userInput, firstNumberBase, secondNumberBase);
		}
		else
		{
			String firstScaleAbbreviation = getCurrentFirstUnitAbbreviation();
			String secondScaleAbbreviation = getCurrentSecondUnitAbbreviation();

			converter = new TemperatureConverter(userInput, firstScaleAbbreviation, secondScaleAbbreviation);
		}

		return converter.doValueConvesion();
	}

	private boolean currentUnitsAreBasic()
	{
		return currentUnitTypeClassifier.equals("basic");
	}

	private boolean currentUnitsAreNumberBases()
	{
		return currentUnitTypeClassifier.equals("number");
	}

	public void initializeRamDataStructures()
	{
		getPreferencesFromDB();
		getUnitTypesFromDB();
		getUnitsFromDB();
	}

	private void getPreferencesFromDB()
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

			preferences = new Preferences(preferencesId, numberOfDecimalPlaces, defaultUnitTypeId, defaultFirstUnitId,
					defaultSecondUnitId, defaultSkinName);
		}
		catch (SQLException e)
		{
			showCriticalErrorMessageAndExitApp();
		}
	}

	private void getUnitTypesFromDB()
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("select * from UnitType order by unitTypeName asc");
				ResultSet resultSet = preparedStatement.executeQuery())
		{
			int defaultUnitTypeId = preferences.getDefaultUnitTypeId();

			while (resultSet.next())
			{
				int unitTypeId = resultSet.getInt("unitTypeId");
				String unitTypeName = resultSet.getString("unitTypeName");
				String classifier = resultSet.getString("classifier");
				UnitType unitType = new UnitType(unitTypeId, unitTypeName, classifier);

				allUnitTypes.add(unitType);
				setOfAllUnitTypeNames.add(unitTypeName);

				if (unitTypeId == defaultUnitTypeId)
				{
					initializeUnitTypeClassObjects(unitType);
				}
			}
		}
		catch (SQLException e)
		{
			showCriticalErrorMessageAndExitApp();
		}
	}

	private void getUnitsFromDB()
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("select * from Unit order by unitName asc");
				ResultSet resultSet = preparedStatement.executeQuery())
		{
			while (resultSet.next())
			{
				int unitId = resultSet.getInt("unitId");
				String unitName = resultSet.getString("unitName");
				String unitAbbreviation = resultSet.getString("unitAbbreviation");
				String displayName = getDisplayName(unitName, unitAbbreviation);
				double unitRatio = resultSet.getDouble("unitRatio");
				int unitType_unitTypeId = resultSet.getInt("unitType_unitTypeId");
				Unit unit = new Unit(unitId, unitName, unitAbbreviation, displayName, unitRatio, unitType_unitTypeId);

				allUnits.add(unit);

				if (unitType_unitTypeId == currentUnitType.getUnitTypeId())
				{
					initializeUnitClassObjects(unit);
				}
			}
		}
		catch (SQLException e)
		{
			showCriticalErrorMessageAndExitApp();
		}
	}

	private void initializeUnitTypeClassObjects(UnitType unitType)
	{
		currentUnitType = new UnitType(unitType);
		defaultUnitType = new UnitType(unitType);
		currentUnitTypeClassifier = unitType.getUnitTypeClassifier();
	}

	private void initializeUnitClassObjects(Unit unit)
	{
		int defaultFirstUnitId = preferences.getDefaultFirstUnitId();
		int defaultSecondUnitId = preferences.getDefaultSecondUnitId();

		addNameToCurrentMainWindowSetOfUnitNames(unit);
		addNameToCurrentPreferencesSetOfUnitNames(unit);
		currentMainWindowSetOfUnits.add(unit);
		currentPreferencesSetOfUnits.add(unit);

		int unitId = unit.getUnitId();

		if (unitId == defaultFirstUnitId)
		{
			setCurrentFirstUnit(unit);
			setDefaultFirstUnit(unit);
		}
		if (unitId == defaultSecondUnitId)
		{
			setCurrentSecondUnit(unit);
			setDefaultSecondUnit(unit);
		}
	}

	private void showCriticalErrorMessageAndExitApp()
	{
		message.showMessage(Message.ERROR_TITLE, Message.CRITICAL_ERROR_MESSAGE);

		Platform.exit();
		System.exit(-1);
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
		String currenFirstUnitAbbreviation = currentFirstUnit.getUnitAbbreviation();
		String currentSecondUnitAbbreviation = currentSecondUnit.getUnitAbbreviation();
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
			currentFirstUnit.setUnitRatio(newValue);
		}
		if (updatedExchangeRates.get(currentSecondUnitAbbreviation) != null)
		{
			newValue = updatedExchangeRates.get(currentSecondUnitAbbreviation);
			currentSecondUnit.setUnitRatio(newValue);
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

	public void changeCurrentPreferencesSetOfUnits(int index)
	{
		currentPreferencesSetOfUnitNames.clear();
		currentPreferencesSetOfUnits.clear();

		UnitType unitType = allUnitTypes.get(index);
		int typeId = unitType.getUnitTypeId();

		for (Unit unit : allUnits)
		{
			if (unit.getUnitType_unitTypeId() == typeId)
			{
				addNameToCurrentPreferencesSetOfUnitNames(unit);
				currentPreferencesSetOfUnits.add(unit);
			}
		}
	}

	public void changeCurrentMainWindowSetOfUnits(int index)
	{
		UnitType unitType = allUnitTypes.get(index);

		currentUnitType = new UnitType(unitType);
		currentUnitTypeClassifier = unitType.getUnitTypeClassifier();
		currentMainWindowSetOfUnitNames.clear();
		currentMainWindowSetOfUnits.clear();

		int currentUnitTypeId = currentUnitType.getUnitTypeId();

		for (Unit unit : allUnits)
		{
			if (unit.getUnitType_unitTypeId() == currentUnitTypeId)
			{
				addNameToCurrentMainWindowSetOfUnitNames(unit);
				currentMainWindowSetOfUnits.add(unit);
			}
		}
	}

	public void addNameToCurrentPreferencesSetOfUnitNames(Unit unit)
	{
		String displayName = unit.getUnitDisplayName();

		currentPreferencesSetOfUnitNames.add(displayName);
	}

	public void addNameToCurrentMainWindowSetOfUnitNames(Unit unit)
	{
		String displayName = unit.getUnitDisplayName();

		currentMainWindowSetOfUnitNames.add(displayName);
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
		defaultUnitType = new UnitType(unitType);
	}

	public void changeFirstCurrentUnit(int index)
	{
		if (index >= 0)
		{
			Unit unit = currentMainWindowSetOfUnits.get(index);
			setCurrentFirstUnit(unit);
		}
	}

	public void changeSecondCurrentUnit(int index)
	{
		if (index >= 0)
		{
			Unit unit = currentMainWindowSetOfUnits.get(index);
			setCurrentSecondUnit(unit);
		}
	}

	public void setDefaultFirstUnit(int index)
	{
		Unit unit = currentPreferencesSetOfUnits.get(index);
		setDefaultFirstUnit(unit);
	}

	public void setDefaultSecondUnit(int index)
	{
		Unit unit = currentPreferencesSetOfUnits.get(index);
		setDefaultSecondUnit(unit);
	}

	private void setDefaultFirstUnit(Unit unit)
	{
		defaultFirstUnit = new Unit(unit);
	}

	private void setDefaultSecondUnit(Unit unit)
	{
		defaultSecondUnit = new Unit(unit);
	}

	private void setCurrentFirstUnit(Unit unit)
	{
		currentFirstUnit = new Unit(unit);
	}

	private void setCurrentSecondUnit(Unit unit)
	{
		currentSecondUnit = new Unit(unit);
	}

	public static String getDefaultFirstUnitDisplayName()
	{
		return defaultFirstUnit.getUnitDisplayName();
	}

	public static String getDefaultSecondUnitDisplayName()
	{
		return defaultSecondUnit.getUnitDisplayName();
	}

	public static String getFirstCurrentUnitDisplayName()
	{
		return currentFirstUnit.getUnitDisplayName();
	}

	public static String getSecondCurrentUnitDisplayName()
	{
		return currentSecondUnit.getUnitDisplayName();
	}

	public static UnitType getUnitType(int index)
	{
		return allUnitTypes.get(index);
	}

	public static Unit getPreferencesUnit(int index)
	{
		return currentPreferencesSetOfUnits.get(index);
	}

	public static int getPreferencesId()
	{
		return preferences.getPreferencesId();
	}

	private static String getCurrentFirstUnitAbbreviation()
	{
		return currentFirstUnit.getUnitAbbreviation();
	}

	private static String getCurrentSecondUnitAbbreviation()
	{
		return currentSecondUnit.getUnitAbbreviation();
	}

	public static int getNumberOfDecimalPlaces()
	{
		return preferences.getNumberOfDecimalPlaces();
	}

	public static void setPreferences(Preferences prefs)
	{
		preferences = prefs;
	}

	public static String getCurrentUnitTypeClassifier()
	{
		return currentUnitTypeClassifier;
	}

	public ObservableList<String> getAllUnitTypeNames()
	{
		return setOfAllUnitTypeNames;
	}

	public String getDefaultUnitTypeName()
	{
		return defaultUnitType.getUnitTypeName();
	}

	public static String getCurrentUnitTypeName()
	{
		return currentUnitType.getUnitTypeName();
	}

	public static Preferences getPreferences()
	{
		return preferences;
	}

	public ObservableList<String> getCurrentMainWindowSetOfUnitNames()
	{
		return currentMainWindowSetOfUnitNames;
	}

	public ObservableList<String> getCurrentPreferencesSetOfUnitNames()
	{
		return currentPreferencesSetOfUnitNames;
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