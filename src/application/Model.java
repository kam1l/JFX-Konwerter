package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Model
{
	private Connection connection;

	private static List<UnitType> allUnitTypes = new ArrayList<UnitType>();
	private static List<Unit> allUnits = new ArrayList<Unit>();

	private static List<Unit> currentMainWindowSetOfUnits = new ArrayList<Unit>();
	private static List<Unit> currentPreferencesSetOfUnits = new ArrayList<Unit>();

	private static ObservableList<String> setOfUnitTypeNames = FXCollections.observableArrayList();
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
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(Message.ERROR_TITLE);
			alert.setHeaderText(null);
			alert.setContentText(Message.CRITICAL_ERROR_MESSAGE);

			alert.showAndWait();
			Platform.exit();
			System.exit(-1);
		}
	}

	public String convertValue(Value value)
	{
		if (currentUnitTypeClassifier.equals("simple") && value.isValidDoubleValue())
		{
			return doSimpleConversion(value.getDoubleValue());
		}
		else if (currentUnitTypeClassifier.equals("numbers") && value.isValidStringValue())
		{
			return doNumberBaseConversion(value);
		}
		else if (currentUnitTypeClassifier.equals("temperature") && value.isValidDoubleValue())
		{
			return doTemperatureConversion(value.getDoubleValue());
		}
		else
		{
			return Message.INVALID_NUMBER_FORMAT_MESSAGE;
		}
	}

	private String doSimpleConversion(double value)
	{
		double currentFirstUnitRatio = currentFirstUnit.getUnitRatio();
		double currentSecondUnitRatio = currentSecondUnit.getUnitRatio();
		double result = value * currentFirstUnitRatio / currentSecondUnitRatio;
		String formattingString = getFormattingString();
		String resultString = String.format(Locale.ROOT, formattingString, result);

		return Value.removeNegativeAndTrailingZeros(resultString);
	}

	private String doTemperatureConversion(double value)
	{
		String firstUnitAbbreviation = Model.getCurrentFirstUnitAbbreviation();
		String secondUnitAbbreviation = Model.getCurrentSecondUnitAbbreviation();
		String resultString;
		String formattingString = getFormattingString();
		double result;

		if (firstUnitAbbreviation.matches("°C"))
		{
			result = convertFromCelsiusToOther(value, secondUnitAbbreviation);
		}
		else
		{
			result = convertFromOtherToOther(value, firstUnitAbbreviation, secondUnitAbbreviation);
		}

		resultString = String.format(Locale.ROOT, formattingString, result);

		return Value.removeNegativeAndTrailingZeros(resultString);
	}

	private double convertFromOtherToOther(double value, String firstUnitAbbreviation, String secondUnitAbbreviation)
	{
		if (firstUnitAbbreviation.matches(secondUnitAbbreviation))
		{
			return value;
		}

		double result;

		switch (firstUnitAbbreviation)
		{
		case "°F":
			result = (value - 32) / 1.8;
			break;

		case "°K":
			result = value - 273.15;
			break;

		case "°R":
			result = value / 1.8 - 273.15;
			break;

		case "°Ré":
			result = 1.25 * value;
			break;

		case "°Rø":
			result = (value - 7.5) * 40 / 21;
			break;

		case "°D":
			result = 100 - value * 2 / 3;
			break;

		case "°N":
			result = value / 0.33;
			break;

		default:
			result = 0;
		}

		if (secondUnitAbbreviation.matches("°C"))
		{
			return result;
		}
		else
		{
			return convertFromCelsiusToOther(result, secondUnitAbbreviation);
		}
	}

	private double convertFromCelsiusToOther(double value, String secondUnitAbbreviation)
	{
		switch (secondUnitAbbreviation)
		{
		case "°C":
			return value;

		case "°F":
			return value * 1.8 + 32;

		case "°K":
			return value + 273.15;

		case "°R":
			return (value + 273.15) * 1.8;

		case "°Ré":
			return value / 0.8;

		case "°Rø":
			return value * 21 / 40 + 7.5;

		case "°D":
			return (100 - value) * 3 / 2;

		case "°N":
			return value * 0.33;

		default:
			return 0;
		}
	}

	private String doNumberBaseConversion(Value value)
	{
		value.prepareInputValue();

		if (value.hasValidCharacters() == true)
		{
			String result;
			long intPart = value.getIntPart();

			if (value.isDecimalFraction() == true)
			{
				double decPart = value.getDecPart();
				result = convertIntPart(intPart) + "." + convertDecPart(decPart);
			}
			else
			{
				result = convertIntPart(intPart);
			}

			result = value.isNegative() ? "-" + result : result;

			return Value.removeNegativeAndTrailingZeros(result);
		}
		else
		{
			int currentFirstUnitRatio = (int) currentFirstUnit.getUnitRatio();

			return Message.INVALID_NUMBER_BASE_MESSAGE + currentFirstUnitRatio + ".";
		}
	}

	private String convertIntPart(long integerPart)
	{
		long numberBase = (long) currentSecondUnit.getUnitRatio();
		long reminder;
		String result = new String("");

		while (integerPart > 0)
		{
			reminder = integerPart % numberBase;
			result = convertLongToCharacter(reminder) + result;
			integerPart /= numberBase;
		}

		return result.isEmpty() ? "0" : result;
	}

	private String convertDecPart(double decimalFractionPart)
	{
		long intP;
		int counter = 0;
		double numberBase = currentSecondUnit.getUnitRatio();
		int numberOfDecimalPlaces = preferences.getNumberOfDecimalPlaces();
		String result = "";

		do
		{
			decimalFractionPart *= numberBase;
			intP = (long) decimalFractionPart;
			decimalFractionPart -= intP;

			result += convertLongToCharacter(intP);

			counter++;
		}
		while ((decimalFractionPart != 0) && (counter < numberOfDecimalPlaces));

		return result;
	}

	private static char convertLongToCharacter(long value)
	{
		if (value < 10)
		{
			return (char) (value + 48);
		}
		else
		{
			return (char) (value + 55);
		}
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

			preferences = new Preferences();
			preferences.setPreferencesId(preferencesId);
			preferences.setNumberOfDecimalPlaces(numberOfDecimalPlaces);
			preferences.setDefaultUnitTypeId(defaultUnitTypeId);
			preferences.setDefaultFirstUnitId(defaultFirstUnitId);
			preferences.setDefaultSecondUnitId(defaultSecondUnitId);
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
				UnitType unitType = new UnitType();
				int unitTypeId = resultSet.getInt("unitTypeId");
				String unitTypeName = resultSet.getString("unitTypeName");
				String classifier = resultSet.getString("classifier");

				unitType.setUnitTypeId(unitTypeId);
				unitType.setUnitTypeName(unitTypeName);
				unitType.setUnitTypeClassifier(classifier);

				allUnitTypes.add(unitType);
				setOfUnitTypeNames.add(unitTypeName);

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
				Unit unit = new Unit();
				int unitId = resultSet.getInt("unitId");
				String unitName = resultSet.getString("unitName");
				String unitAbbreviation = resultSet.getString("unitAbbreviation");
				String displayName = getDisplayName(unitName, unitAbbreviation);
				double unitRatio = resultSet.getDouble("unitRatio");
				int unitType_unitTypeId = resultSet.getInt("unitType_unitTypeId");

				unit.setUnitId(unitId);
				unit.setUnitName(unitName);
				unit.setUnitAbbreviation(unitAbbreviation);
				unit.setUnitDisplayName(displayName);
				unit.setUnitRatio(unitRatio);
				unit.setUnitType_unitTypeId(unitType_unitTypeId);

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
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(Message.ERROR_TITLE);
		alert.setHeaderText(null);
		alert.setContentText(Message.CRITICAL_ERROR_MESSAGE);

		alert.showAndWait();

		Platform.exit();
		Platform.exit();
		System.exit(-1);
	}

	public boolean updateRateInDB(String symbol, double rate)
	{
		try (PreparedStatement preparedStatement = connection
				.prepareStatement("update Unit set unitRatio = ? where unitAbbreviation = ?"))
		{
			preparedStatement.setDouble(1, rate);
			preparedStatement.setString(2, symbol);

			preparedStatement.executeUpdate();

			return true;
		}
		catch (SQLException e)
		{
			return false;
		}
	}

	public boolean updatePreferencesInDB(Preferences newPreferences)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"update Preferences set numberOfDecimalPlaces = ?, defaultUnitTypeId = ?, defaultFirstUnitId = ?, "
						+ "defaultSecondUnitId = ? where preferencesId = ?"))
		{
			preparedStatement.setInt(1, newPreferences.getNumberOfDecimalPlaces());
			preparedStatement.setInt(2, newPreferences.getDefaultUnitTypeId());
			preparedStatement.setInt(3, newPreferences.getDefaultFirstUnitId());
			preparedStatement.setInt(4, newPreferences.getDefaultSecondUnitId());
			preparedStatement.setInt(5, newPreferences.getPreferencesId());

			preparedStatement.executeUpdate();

			return true;
		}
		catch (SQLException e)
		{
			return false;
		}
	}

	public void updateRamDataStructures(Map<String, Double> updatedRates)
	{
		String currenFirstUnitAbbreviation = currentFirstUnit.getUnitAbbreviation();
		String currentSecondUnitAbbreviation = currentSecondUnit.getUnitAbbreviation();
		String unitAbbreviation;
		double newValue;

		for (Unit unit : allUnits)
		{
			unitAbbreviation = unit.getUnitAbbreviation();

			if (updatedRates.get(unitAbbreviation) != null)
			{
				newValue = updatedRates.get(unitAbbreviation);
				unit.setUnitRatio(newValue);
			}
		}

		if (updatedRates.get(currenFirstUnitAbbreviation) != null)
		{
			newValue = updatedRates.get(currenFirstUnitAbbreviation);
			currentFirstUnit.setUnitRatio(newValue);
		}
		if (updatedRates.get(currentSecondUnitAbbreviation) != null)
		{
			newValue = updatedRates.get(currentSecondUnitAbbreviation);
			currentSecondUnit.setUnitRatio(newValue);
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

	public static Unit getCurrentFirstUnit()
	{
		return currentFirstUnit;
	}

	public static int getNumberOfDecimalPlaces()
	{
		return preferences.getNumberOfDecimalPlaces();
	}

	public void setNumberOfDecimalPlaces(int newNumberOfDecPlaces)
	{
		preferences.setNumberOfDecimalPlaces(newNumberOfDecPlaces);
	}

	public String getFormattingString()
	{
		int numOfDecimalPlaces = Model.getNumberOfDecimalPlaces();

		return "%1$." + numOfDecimalPlaces + "f";
	}

	public static String getCurrentUnitTypeClassifier()
	{
		return currentUnitTypeClassifier;
	}

	public static void setCurrentUnitTypeClassifier(String currentUnitTypeClassfier)
	{
		Model.currentUnitTypeClassifier = currentUnitTypeClassfier;
	}

	public boolean isDbConnected()
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

	public ObservableList<String> getAllUnitTypeNames()
	{
		return setOfUnitTypeNames;
	}

	public String getDefaultUnitTypeName()
	{
		return defaultUnitType.getUnitTypeName();
	}

	public static String getCurrentUnitTypeName()
	{
		return currentUnitType.getUnitTypeName();
	}

	public ObservableList<String> getCurrentMainWindowSetOfUnitNames()
	{
		return currentMainWindowSetOfUnitNames;
	}

	public ObservableList<String> getCurrentPreferencesSetOfUnitNames()
	{
		return currentPreferencesSetOfUnitNames;
	}
}