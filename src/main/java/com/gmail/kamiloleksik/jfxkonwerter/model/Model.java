package com.gmail.kamiloleksik.jfxkonwerter.model;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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

import com.gmail.kamiloleksik.jfxkonwerter.model.DaoManager.DaoKey;
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
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model
{
	private DaoManager daoManager;
	private Preferences preferences;
	private Map<String, BigDecimal> updatedExchangeRates;
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

	public Model() throws SQLException, IOException
	{
		daoManager = new DaoManager();
		initializeRamDataStructures();
	}

	public String convertValue(String userInput) throws InvalidNumberFormatException, InvalidNumberBaseException
	{
		Converter converter;
		int numberOfDecimalPlaces = Integer.valueOf(getNumberOfDecimalPlaces());

		if (currentUnitsAreBasic())
		{
			BigDecimal firstUnitRatio = units.get(UnitKey.CURRENT_FIRST_UNIT).getUnitRatio();
			BigDecimal secondUnitRatio = units.get(UnitKey.CURRENT_SECOND_UNIT).getUnitRatio();

			converter = new BasicConverter(firstUnitRatio, secondUnitRatio);
		}
		else if (currentUnitsAreNumberBases())
		{
			BigInteger firstNumberBase = units.get(UnitKey.CURRENT_FIRST_UNIT).getUnitRatio().toBigInteger();
			BigInteger secondNumberBase = units.get(UnitKey.CURRENT_SECOND_UNIT).getUnitRatio().toBigInteger();

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

	private void initializeRamDataStructures() throws IOException
	{
		preferences = getPreferencesFromDB();
		appLanguages = getAppLanguagesFromDB(preferences.getAppLanguage().getAppLanguageId());
		unitsLanguages = getUnitsLanguagesFromDB(preferences.getUnitsLanguage().getUnitsLanguageId());
		appSkins = getAppSkinsFromDB(preferences.getAppSkin().getAppSkinId());
		numbersOfDecimalPlaces = getNumbersOfDecimalPlacesFromDB(
				preferences.getNumberOfDecimalPlaces().getNumberOfDecimalPlacesId());
		unitTypes = getUnitTypesFromDB(preferences.getUnitType().getUnitTypeId());
		int currentUnitTypeId = unitTypes.get(UnitTypeKey.CURRENT_UNIT_TYPE).getUnitTypeId();
		allUnits = getUnitsFromDB(currentUnitTypeId);
	}

	private Preferences getPreferencesFromDB() throws IOException
	{
		try (CloseableIterator<?> it = daoManager.get(DaoKey.PREFERENCES_DAO).closeableIterator())
		{
			return (Preferences) it.next();
		}
	}

	private List<AppLanguage> getAppLanguagesFromDB(int defaultAppLanguageId) throws IOException
	{
		try (CloseableIterator<?> it = daoManager.get(DaoKey.APP_LANGUAGE_DAO).closeableIterator())
		{
			ObservableList<String> nameList = FXCollections.observableArrayList();
			List<AppLanguage> languages = new ArrayList<AppLanguage>();
			AppLanguage lang;

			while (it.hasNext())
			{
				lang = (AppLanguage) it.next();

				languages.add(lang);
				nameList.add(lang.getAppLanguageName());

				if (lang.getAppLanguageId() == defaultAppLanguageId)
				{
					appLanguage = lang;
				}
			}

			Collections.sort(languages);
			Collections.sort(nameList, String.CASE_INSENSITIVE_ORDER);
			names.put(NamesKey.APP_LANGUAGES_NAMES, nameList);

			return languages;
		}
	}

	private List<UnitsLanguage> getUnitsLanguagesFromDB(int defaultUnitsLanguageId) throws IOException
	{
		try (CloseableIterator<?> it = daoManager.get(DaoKey.UNITS_LANGUAGE_DAO).closeableIterator())
		{
			ObservableList<String> nameList = FXCollections.observableArrayList();
			List<UnitsLanguage> languages = new ArrayList<UnitsLanguage>();
			UnitsLanguage lang;

			while (it.hasNext())
			{
				lang = (UnitsLanguage) it.next();

				languages.add(lang);
				nameList.add(lang.getUnitsLanguageName());

				if (lang.getUnitsLanguageId() == defaultUnitsLanguageId)
				{
					unitsLanguage = lang;
				}
			}

			Collections.sort(languages);
			Collections.sort(nameList, String.CASE_INSENSITIVE_ORDER);
			names.put(NamesKey.UNITS_LANGUAGES_NAMES, nameList);

			return languages;
		}
	}

	private List<AppSkin> getAppSkinsFromDB(int defaultAppSkinId) throws IOException
	{
		try (CloseableIterator<?> it = daoManager.get(DaoKey.APP_SKIN_DAO).closeableIterator())
		{
			ObservableList<String> nameList = FXCollections.observableArrayList();
			List<AppSkin> skins = new ArrayList<AppSkin>();
			AppSkin skin;

			while (it.hasNext())
			{
				skin = (AppSkin) it.next();

				skins.add(skin);
				nameList.add(skin.getAppSkinName());

				if (skin.getAppSkinId() == defaultAppSkinId)
				{
					appSkin = skin;
				}
			}

			Collections.sort(skins);
			Collections.sort(nameList, String.CASE_INSENSITIVE_ORDER);
			names.put(NamesKey.APP_SKINS_NAMES, nameList);

			return skins;
		}
	}

	private List<NumberOfDecimalPlaces> getNumbersOfDecimalPlacesFromDB(int defaultNumberOfDecimalPlacesId)
			throws IOException
	{
		try (CloseableIterator<?> it = daoManager.get(DaoKey.NUMBER_OF_DECIMAL_PLACES_DAO).closeableIterator())
		{
			ObservableList<String> nameList = FXCollections.observableArrayList();
			List<NumberOfDecimalPlaces> numsOfDecPlaces = new ArrayList<NumberOfDecimalPlaces>();
			NumberOfDecimalPlaces num;

			while (it.hasNext())
			{
				num = (NumberOfDecimalPlaces) it.next();

				numsOfDecPlaces.add(num);
				nameList.add(num.getNumberOfDecimalPlaces());

				if (num.getNumberOfDecimalPlacesId() == defaultNumberOfDecimalPlacesId)
				{
					numberOfDecimalPlaces = num;
				}
			}

			Collections.sort(numsOfDecPlaces);
			Collections.sort(nameList, (o1, o2) ->
			{
				return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
			});

			names.put(NamesKey.ALLOWED_NUMBERS_OF_DECIMAL_PLACES, nameList);

			return numsOfDecPlaces;
		}
	}

	private EnumMap<UnitTypeKey, UnitType> getUnitTypesFromDB(int defaultUnitTypeId) throws IOException
	{
		try (CloseableIterator<?> it = daoManager.get(DaoKey.UNIT_TYPE_DAO).closeableIterator())
		{
			ObservableList<String> nameList = FXCollections.observableArrayList();
			EnumMap<UnitTypeKey, UnitType> uTypes = new EnumMap<UnitTypeKey, UnitType>(UnitTypeKey.class);
			UnitType uType;

			while (it.hasNext())
			{
				uType = (UnitType) it.next();

				allUnitTypes.add(uType);
				nameList.add(uType.getUnitTypeName());

				if (uType.getUnitTypeId() == defaultUnitTypeId)
				{
					uTypes.put(UnitTypeKey.CURRENT_UNIT_TYPE, new UnitType(uType));
					uTypes.put(UnitTypeKey.DEFAULT_UNIT_TYPE, new UnitType(uType));
					defaultUnitTypeIndex = allUnitTypes.size() - 1;
					preferencesUnitTypeIndex = defaultUnitTypeIndex;
					currentUnitTypeClassifier = uType.getUnitTypeClassifier();
				}
			}

			Collections.sort(allUnitTypes);
			Collections.sort(nameList, String.CASE_INSENSITIVE_ORDER);
			names.put(NamesKey.ALL_UNIT_TYPE_NAMES, nameList);

			return uTypes;
		}
	}

	private List<Unit> getUnitsFromDB(int currentUnitTypeId) throws IOException
	{
		try (CloseableIterator<?> it = daoManager.get(DaoKey.UNIT_DAO).closeableIterator())
		{
			List<Unit> aUnits = new ArrayList<Unit>();
			names.put(NamesKey.MAIN_WINDOW_UNIT_NAMES, FXCollections.observableArrayList());
			names.put(NamesKey.PREFERENCES_UNIT_NAMES, FXCollections.observableArrayList());
			Unit unit;

			while (it.hasNext())
			{
				unit = (Unit) it.next();
				unit.setDisplayName(getDisplayName(unit.getUnitName(), unit.getUnitAbbreviation()));
				aUnits.add(unit);

				if (unit.getUnitType().getUnitTypeId() == currentUnitTypeId)
				{
					initializeStaticUnitObjects(unit);
				}
			}

			Collections.sort(names.get(NamesKey.MAIN_WINDOW_UNIT_NAMES), String.CASE_INSENSITIVE_ORDER);
			Collections.sort(names.get(NamesKey.PREFERENCES_UNIT_NAMES), String.CASE_INSENSITIVE_ORDER);
			Collections.sort(mainWindowUnits);
			Collections.sort(preferencesUnits);
			Collections.sort(aUnits);

			return aUnits;
		}
	}

	private void initializeStaticUnitObjects(Unit unit)
	{
		int defaultFirstUnitId = preferences.getFirstUnit().getUnitId();
		int defaultSecondUnitId = preferences.getSecondUnit().getUnitId();

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
		updatedExchangeRates = new HashMap<String, BigDecimal>();

		for (int tmp = 2; tmp < nList.getLength(); tmp++)
		{
			Node nNode = nList.item(tmp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element eElement = (Element) nNode;
				String currentCurrency = eElement.getAttribute("currency");
				BigDecimal currentRate = BigDecimal.ONE.divide(new BigDecimal(eElement.getAttribute("rate")), 300,
						RoundingMode.HALF_EVEN);

				updateExchangeRateSingleRowInDB(currentCurrency, currentRate);
				updatedExchangeRates.put(currentCurrency, currentRate);
			}
		}
	}

	private void updateExchangeRateSingleRowInDB(String abbreviation, BigDecimal rate) throws SQLException
	{
		@SuppressWarnings("unchecked")
		Dao<Unit, Integer> dao = (Dao<Unit, Integer>) daoManager.get(DaoKey.UNIT_DAO);
		UpdateBuilder<Unit, Integer> updateBuilder = dao.updateBuilder();

		updateBuilder.updateColumnValue("unitRatio", rate);
		updateBuilder.where().eq("unitAbbreviation", abbreviation);
		updateBuilder.update();
	}

	public void updateExchangeRatesInRam()
	{
		String currenFirstUnitAbbreviation = units.get(UnitKey.CURRENT_FIRST_UNIT).getUnitAbbreviation();
		String currentSecondUnitAbbreviation = units.get(UnitKey.CURRENT_SECOND_UNIT).getUnitAbbreviation();
		String unitAbbreviation;
		BigDecimal newValue;

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
		@SuppressWarnings("unchecked")
		Dao<Preferences, Integer> dao = (Dao<Preferences, Integer>) daoManager.get(DaoKey.PREFERENCES_DAO);

		try
		{
			dao.update(newPreferences);
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
			if (unit.getUnitType().getUnitTypeId() == typeId)
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

	public String getNumberOfDecimalPlaces()
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

	public void closeConnection()
	{
		daoManager.closeConnection();
	}
}