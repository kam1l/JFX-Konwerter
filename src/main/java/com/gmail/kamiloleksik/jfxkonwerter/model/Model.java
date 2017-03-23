package com.gmail.kamiloleksik.jfxkonwerter.model;

import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.DaoKey.*;
import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.NamesKey.*;
import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.UnitKey.*;
import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.UnitTypeKey.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.gmail.kamiloleksik.jfxkonwerter.model.converter.*;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.*;
import com.gmail.kamiloleksik.jfxkonwerter.model.entity.*;
import com.gmail.kamiloleksik.jfxkonwerter.util.ExchangeRatesDownloader;
import com.gmail.kamiloleksik.jfxkonwerter.util.NumberBaseComparator;
import com.gmail.kamiloleksik.jfxkonwerter.util.NumberBaseNameComparator;
import com.gmail.kamiloleksik.jfxkonwerter.util.keys.DaoKey;
import com.gmail.kamiloleksik.jfxkonwerter.util.keys.NamesKey;
import com.gmail.kamiloleksik.jfxkonwerter.util.keys.UnitKey;
import com.gmail.kamiloleksik.jfxkonwerter.util.keys.UnitTypeKey;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@Service
public class Model
{
	private Map<DaoKey, Dao<?, ?>> daos;
	private Preferences preferences;
	private String currentUnitTypeClassifier;
	private int defaultUnitTypeIndex, preferencesUnitTypeIndex;

	private AppLanguage appLanguage;
	private UnitsLanguage unitsLanguage;
	private AppSkin appSkin;
	private NumberOfDecimalPlaces numberOfDecimalPlaces;

	@Autowired
	private List<UnitType> allUnitTypes;
	@Autowired
	private List<Unit> allUnits;
	@Autowired
	private List<Unit> mainWindowUnits;
	@Autowired
	private List<Unit> preferencesUnits;

	@Autowired
	private List<AppLanguage> appLanguages;
	@Autowired
	private List<UnitsLanguage> unitsLanguages;
	@Autowired
	private List<AppSkin> appSkins;
	@Autowired
	private List<NumberOfDecimalPlaces> numbersOfDecimalPlaces;

	@Autowired
	private Map<UnitTypeKey, UnitType> unitTypes;
	@Autowired
	private Map<UnitKey, Unit> units;
	@Autowired
	private Map<NamesKey, ObservableList<String>> names;

	@Autowired
	public Model(Map<DaoKey, Dao<?, ?>> daos)
	{
		this.daos = daos;
	}

	public String convertValue(String userInput) throws InvalidNumberFormatException, InvalidNumberBaseException
	{
		Converter converter;
		int numberOfDecimalPlaces = Integer.valueOf(getNumberOfDecimalPlaces());

		if (currentUnitsAreBasic())
		{
			BigDecimal firstUnitRatio = units.get(CURRENT_FIRST_UNIT).getUnitRatio();
			BigDecimal secondUnitRatio = units.get(CURRENT_SECOND_UNIT).getUnitRatio();

			converter = new BasicConverter(firstUnitRatio, secondUnitRatio);
		}
		else if (currentUnitsAreNumberBases())
		{
			BigInteger firstNumberBase = units.get(CURRENT_FIRST_UNIT).getUnitRatio().toBigInteger();
			BigInteger secondNumberBase = units.get(CURRENT_SECOND_UNIT).getUnitRatio().toBigInteger();

			converter = new NumberBaseConverter(firstNumberBase, secondNumberBase);
		}
		else
		{
			int firstScaleCode = units.get(CURRENT_FIRST_UNIT).getUnitRatio().intValue();
			int secondScaleCode = units.get(CURRENT_SECOND_UNIT).getUnitRatio().intValue();

			converter = new TemperatureConverter(firstScaleCode, secondScaleCode);
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

	public void initializeRamDataStructures() throws IOException
	{
		preferences = getPreferencesFromDB();
		appLanguages = getAppLanguagesFromDB(preferences.getAppLanguage().getAppLanguageId());
		unitsLanguages = getUnitsLanguagesFromDB(preferences.getUnitsLanguage().getUnitsLanguageId());
		appSkins = getAppSkinsFromDB(preferences.getAppSkin().getAppSkinId());
		numbersOfDecimalPlaces = getNumbersOfDecimalPlacesFromDB(
				preferences.getNumberOfDecimalPlaces().getNumberOfDecimalPlacesId());
		unitTypes = getUnitTypesFromDB(preferences.getUnitType().getUnitTypeId());
		int currentUnitTypeId = unitTypes.get(CURRENT_UNIT_TYPE).getUnitTypeId();
		allUnits = getUnitsFromDB(currentUnitTypeId);
	}

	private Preferences getPreferencesFromDB() throws IOException
	{
		try (CloseableIterator<?> it = daos.get(PREFERENCES_DAO).closeableIterator())
		{
			return (Preferences) it.next();
		}
	}

	private List<AppLanguage> getAppLanguagesFromDB(int defaultAppLanguageId) throws IOException
	{
		try (CloseableIterator<?> it = daos.get(APP_LANGUAGE_DAO).closeableIterator())
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
			names.put(APP_LANGUAGES_NAMES, nameList);

			return languages;
		}
	}

	private List<UnitsLanguage> getUnitsLanguagesFromDB(int defaultUnitsLanguageId) throws IOException
	{
		try (CloseableIterator<?> it = daos.get(UNITS_LANGUAGE_DAO).closeableIterator())
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
			names.put(UNITS_LANGUAGES_NAMES, nameList);

			return languages;
		}
	}

	private List<AppSkin> getAppSkinsFromDB(int defaultAppSkinId) throws IOException
	{
		try (CloseableIterator<?> it = daos.get(APP_SKIN_DAO).closeableIterator())
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
			names.put(APP_SKINS_NAMES, nameList);

			return skins;
		}
	}

	private List<NumberOfDecimalPlaces> getNumbersOfDecimalPlacesFromDB(int defaultNumberOfDecimalPlacesId)
			throws IOException
	{
		try (CloseableIterator<?> it = daos.get(NUMBER_OF_DECIMAL_PLACES_DAO).closeableIterator())
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

			names.put(ALLOWED_NUMBERS_OF_DECIMAL_PLACES, nameList);

			return numsOfDecPlaces;
		}
	}

	private EnumMap<UnitTypeKey, UnitType> getUnitTypesFromDB(int defaultUnitTypeId) throws IOException
	{
		try (CloseableIterator<?> it = daos.get(UNIT_TYPE_DAO).closeableIterator())
		{
			ObservableList<String> nameList = FXCollections.observableArrayList();
			EnumMap<UnitTypeKey, UnitType> uTypes = new EnumMap<UnitTypeKey, UnitType>(UnitTypeKey.class);
			String unitTypeName;
			UnitType uType;
			Object uTypeLang;
			CloseableIterator<?> itLang;

			if (unitsLanguage.getUnitsLanguageName().equals("Polski"))
			{
				itLang = daos.get(UNIT_TYPE_DAO).closeableIterator();
			}
			else
			{
				itLang = daos.get(UNIT_TYPE_EN_DAO).closeableIterator();
			}

			while (it.hasNext())
			{
				uType = (UnitType) it.next();
				uTypeLang = itLang.next();

				if (uTypeLang instanceof UnitType)
				{
					unitTypeName = ((UnitType) uTypeLang).getUnitTypeName();
					nameList.add(unitTypeName);
				}
				else
				{
					unitTypeName = ((UnitType_en) uTypeLang).getUnitTypeName();
					nameList.add(unitTypeName);
				}

				uType.setUnitTypeName(unitTypeName);
				allUnitTypes.add(uType);

				if (uType.getUnitTypeId() == defaultUnitTypeId)
				{
					uTypes.put(CURRENT_UNIT_TYPE, new UnitType(uType));
					uTypes.put(DEFAULT_UNIT_TYPE, new UnitType(uType));
					defaultUnitTypeIndex = allUnitTypes.size() - 1;
					preferencesUnitTypeIndex = defaultUnitTypeIndex;
					currentUnitTypeClassifier = uType.getUnitTypeClassifier();
				}
			}

			Collections.sort(allUnitTypes);
			Collections.sort(nameList, String.CASE_INSENSITIVE_ORDER);
			names.put(ALL_UNIT_TYPE_NAMES, nameList);

			return uTypes;
		}
	}

	private List<Unit> getUnitsFromDB(int currentUnitTypeId) throws IOException
	{
		try (CloseableIterator<?> it = daos.get(UNIT_DAO).closeableIterator())
		{
			List<Unit> aUnits = new ArrayList<Unit>();
			names.put(MAIN_WINDOW_UNIT_NAMES, FXCollections.observableArrayList());
			names.put(PREFERENCES_UNIT_NAMES, FXCollections.observableArrayList());
			Unit unit;
			Object uLang;
			CloseableIterator<?> itLang;

			if (unitsLanguage.getUnitsLanguageName().equals("Polski"))
			{
				itLang = daos.get(UNIT_DAO).closeableIterator();
			}
			else
			{
				itLang = daos.get(UNIT_EN_DAO).closeableIterator();
			}

			while (it.hasNext())
			{
				unit = (Unit) it.next();
				uLang = itLang.next();

				if (uLang instanceof Unit_en)
				{
					unit.setName(((Unit_en) uLang).getUnitName());
				}

				unit.setDisplayName(getDisplayName(unit.getUnitName(), unit.getUnitAbbreviation()));
				aUnits.add(unit);

				if (unit.getUnitType().getUnitTypeId() == currentUnitTypeId)
				{
					initializeStaticUnitObjects(unit);
				}
			}

			if (currentUnitsAreNumberBases())
			{
				Comparator<String> numberBaseNameComparator = new NumberBaseNameComparator();
				Comparator<Unit> numberBaseComparator = new NumberBaseComparator();

				Collections.sort(names.get(MAIN_WINDOW_UNIT_NAMES), numberBaseNameComparator);
				Collections.sort(names.get(PREFERENCES_UNIT_NAMES), numberBaseNameComparator);
				Collections.sort(mainWindowUnits, numberBaseComparator);
				Collections.sort(preferencesUnits, numberBaseComparator);
			}
			else
			{
				Collections.sort(names.get(MAIN_WINDOW_UNIT_NAMES), String.CASE_INSENSITIVE_ORDER);
				Collections.sort(names.get(PREFERENCES_UNIT_NAMES), String.CASE_INSENSITIVE_ORDER);
				Collections.sort(mainWindowUnits);
				Collections.sort(preferencesUnits);
			}

			return aUnits;
		}
	}

	private void initializeStaticUnitObjects(Unit unit)
	{
		int defaultFirstUnitId = preferences.getFirstUnit().getUnitId();
		int defaultSecondUnitId = preferences.getSecondUnit().getUnitId();

		addItemToUnitNames(unit, MAIN_WINDOW_UNIT_NAMES);
		addItemToUnitNames(unit, PREFERENCES_UNIT_NAMES);

		mainWindowUnits.add(unit);
		preferencesUnits.add(unit);

		int unitId = unit.getUnitId();

		if (unitId == defaultFirstUnitId)
		{
			setUnit(unit, CURRENT_FIRST_UNIT);
			setUnit(unit, DEFAULT_FIRST_UNIT);
		}
		if (unitId == defaultSecondUnitId)
		{
			setUnit(unit, CURRENT_SECOND_UNIT);
			setUnit(unit, DEFAULT_SECOND_UNIT);
		}
	}

	public void updateUnitsLanguage()
	{
		updateUnitTypeLanguage();
		updateAllUnitsListLanguage();
		updateMainWindowUnitsLanguage();
		updatePreferencesUnitsLanguage();
	}

	@SuppressWarnings("unchecked")
	private void updatePreferencesUnitsLanguage()
	{
		ObservableList<String> nameList = FXCollections.observableArrayList();
		String unitName = null;

		for (Unit unit : preferencesUnits)
		{
			if (unitsLanguage.getUnitsLanguageName().equals("Polski"))
			{
				try
				{
					unitName = ((Dao<Unit, Integer>) daos.get(UNIT_DAO)).queryForId(unit.getUnitId()).getUnitName();

				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				try
				{
					unitName = ((Dao<Unit_en, Integer>) daos.get(UNIT_EN_DAO)).queryForId(unit.getUnitId())
							.getUnitName();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}

			unit.setName(unitName);
			unit.setDisplayName(getDisplayName(unitName, unit.getUnitAbbreviation()));
			nameList.add(unit.getUnitDisplayName());
		}

		names.put(PREFERENCES_UNIT_NAMES, nameList);
	}

	@SuppressWarnings("unchecked")
	private void updateMainWindowUnitsLanguage()
	{
		ObservableList<String> nameList = FXCollections.observableArrayList();
		String unitName = null;

		for (Unit unit : mainWindowUnits)
		{
			if (unitsLanguage.getUnitsLanguageName().equals("Polski"))
			{
				try
				{
					unitName = ((Dao<Unit, Integer>) daos.get(UNIT_DAO)).queryForId(unit.getUnitId()).getUnitName();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				try
				{
					unitName = ((Dao<Unit_en, Integer>) daos.get(UNIT_EN_DAO)).queryForId(unit.getUnitId())
							.getUnitName();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}

			unit.setName(unitName);
			unit.setDisplayName(getDisplayName(unitName, unit.getUnitAbbreviation()));
			nameList.add(unit.getUnitDisplayName());
		}

		names.put(MAIN_WINDOW_UNIT_NAMES, nameList);
	}

	@SuppressWarnings("unchecked")
	private void updateAllUnitsListLanguage()
	{
		String unitName = null;

		for (Unit unit : allUnits)
		{
			if (unitsLanguage.getUnitsLanguageName().equals("Polski"))
			{
				try
				{
					unitName = ((Dao<Unit, Integer>) daos.get(UNIT_DAO)).queryForId(unit.getUnitId()).getUnitName();

				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				try
				{
					unitName = ((Dao<Unit_en, Integer>) daos.get(UNIT_EN_DAO)).queryForId(unit.getUnitId())
							.getUnitName();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}

			unit.setName(unitName);
			unit.setDisplayName(getDisplayName(unitName, unit.getUnitAbbreviation()));
			Unit u = units.get(CURRENT_FIRST_UNIT);

			if (u.getUnitId() == unit.getUnitId())
			{
				u.setName(unitName);
				u.setDisplayName(getDisplayName(unitName, u.getUnitAbbreviation()));
			}

			u = units.get(CURRENT_SECOND_UNIT);

			if (u.getUnitId() == unit.getUnitId())
			{
				u.setName(unitName);
				u.setDisplayName(getDisplayName(unitName, u.getUnitAbbreviation()));
			}

			u = units.get(DEFAULT_FIRST_UNIT);

			if (u.getUnitId() == unit.getUnitId())
			{
				u.setName(unitName);
				u.setDisplayName(getDisplayName(unitName, u.getUnitAbbreviation()));
			}

			u = units.get(DEFAULT_SECOND_UNIT);

			if (u.getUnitId() == unit.getUnitId())
			{
				u.setName(unitName);
				u.setDisplayName(getDisplayName(unitName, u.getUnitAbbreviation()));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void updateUnitTypeLanguage()
	{
		ObservableList<String> nameList = FXCollections.observableArrayList();
		String unitTypeName = null;

		for (UnitType type : allUnitTypes)
		{
			if (unitsLanguage.getUnitsLanguageName().equals("Polski"))
			{
				try
				{
					unitTypeName = ((Dao<UnitType, Integer>) daos.get(UNIT_TYPE_DAO)).queryForId(type.getUnitTypeId())
							.getUnitTypeName();

				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				try
				{
					unitTypeName = ((Dao<UnitType_en, Integer>) daos.get(UNIT_TYPE_EN_DAO))
							.queryForId(type.getUnitTypeId()).getUnitTypeName();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}

			type.setUnitTypeName(unitTypeName);

			if (unitTypes.get(CURRENT_UNIT_TYPE).getUnitTypeId() == type.getUnitTypeId())
			{
				unitTypes.get(CURRENT_UNIT_TYPE).setUnitTypeName(unitTypeName);
			}

			if (unitTypes.get(DEFAULT_UNIT_TYPE).getUnitTypeId() == type.getUnitTypeId())
			{
				unitTypes.get(DEFAULT_UNIT_TYPE).setUnitTypeName(unitTypeName);
			}

			nameList.add(unitTypeName);
		}

		names.put(ALL_UNIT_TYPE_NAMES, nameList);
	}

	public boolean updateExchangeRates()
	{
		Map<String, BigDecimal> downloadedExchangeRates;

		try
		{
			downloadedExchangeRates = ExchangeRatesDownloader.getExchangeRatesFromServer();
			updateExchangeRatesInDB(downloadedExchangeRates);
		}
		catch (ParserConfigurationException | SAXException | IOException | SQLException e)
		{
			return false;
		}

		updateExchangeRatesInRam(downloadedExchangeRates);

		return true;
	}

	private void updateExchangeRatesInDB(Map<String, BigDecimal> downloadedExchangeRates) throws SQLException
	{
		for (String currency : downloadedExchangeRates.keySet())
		{
			updateExchangeRateSingleRowInDB(currency, downloadedExchangeRates.get(currency));
		}
	}

	private void updateExchangeRateSingleRowInDB(String abbreviation, BigDecimal rate) throws SQLException
	{
		@SuppressWarnings("unchecked")
		Dao<Unit, Integer> dao = (Dao<Unit, Integer>) daos.get(UNIT_DAO);
		UpdateBuilder<Unit, Integer> updateBuilder = dao.updateBuilder();

		updateBuilder.updateColumnValue("unitRatio", rate);
		updateBuilder.where().eq("unitAbbreviation", abbreviation);
		updateBuilder.update();
	}

	private void updateExchangeRatesInRam(Map<String, BigDecimal> downloadedExchangeRates)
	{
		String currenFirstUnitAbbreviation = units.get(CURRENT_FIRST_UNIT).getUnitAbbreviation();
		String currentSecondUnitAbbreviation = units.get(CURRENT_SECOND_UNIT).getUnitAbbreviation();
		String unitAbbreviation;
		BigDecimal newValue;

		for (Unit unit : allUnits)
		{
			unitAbbreviation = unit.getUnitAbbreviation();

			if (downloadedExchangeRates.get(unitAbbreviation) != null)
			{
				newValue = downloadedExchangeRates.get(unitAbbreviation);
				unit.setUnitRatio(newValue);
			}
		}

		if (downloadedExchangeRates.get(currenFirstUnitAbbreviation) != null)
		{
			newValue = downloadedExchangeRates.get(currenFirstUnitAbbreviation);
			Unit unit = units.get(CURRENT_FIRST_UNIT);
			unit.setUnitRatio(newValue);

			units.put(UnitKey.CURRENT_FIRST_UNIT, unit);
		}
		if (downloadedExchangeRates.get(currentSecondUnitAbbreviation) != null)
		{
			newValue = downloadedExchangeRates.get(currentSecondUnitAbbreviation);
			Unit unit = units.get(CURRENT_SECOND_UNIT);
			unit.setUnitRatio(newValue);

			units.put(CURRENT_SECOND_UNIT, unit);
		}
	}

	public boolean updatePreferencesInDB(Preferences newPreferences)
	{
		@SuppressWarnings("unchecked")
		Dao<Preferences, Integer> dao = (Dao<Preferences, Integer>) daos.get(PREFERENCES_DAO);

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

		preferencesUnits = getUnitLists(PREFERENCES_UNIT_NAMES, typeId);
	}

	public void changeMainWindowSetOfUnits(int index)
	{
		UnitType unitType = allUnitTypes.get(index);

		mainWindowUnits.clear();
		unitTypes.put(CURRENT_UNIT_TYPE, new UnitType(unitType));
		currentUnitTypeClassifier = unitType.getUnitTypeClassifier();

		int currentUnitTypeId = unitTypes.get(CURRENT_UNIT_TYPE).getUnitTypeId();

		mainWindowUnits = getUnitLists(MAIN_WINDOW_UNIT_NAMES, currentUnitTypeId);
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

		if (currentUnitsAreNumberBases())
		{
			Comparator<String> numberBaseNameComparator = new NumberBaseNameComparator();
			Comparator<Unit> numberBaseComparator = new NumberBaseComparator();

			Collections.sort(names.get(key), numberBaseNameComparator);
			Collections.sort(unitsL, numberBaseComparator);
		}
		else
		{
			Collections.sort(names.get(key), String.CASE_INSENSITIVE_ORDER);
			Collections.sort(unitsL);
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
		unitTypes.put(DEFAULT_UNIT_TYPE, new UnitType(unitType));
	}

	public int getUnitTypeIndex(int id)
	{
		for (int i = 0; i < allUnitTypes.size(); i++)
		{
			if (allUnitTypes.get(i).getUnitTypeId() == id)
			{
				return i;
			}
		}

		return 0;
	}

	@SuppressWarnings("unchecked")
	public void changeDefaultNumberOfDecimalPlaces(String numOfDecPlacesString) throws SQLException
	{
		numberOfDecimalPlaces = ((Dao<NumberOfDecimalPlaces, Integer>) daos.get(NUMBER_OF_DECIMAL_PLACES_DAO))
				.queryForEq("numberOfDecimalPlaces", numOfDecPlacesString).get(0);
		preferences.setNumberOfDecimalPlaces(numberOfDecimalPlaces);
		((Dao<Preferences, Integer>) daos.get(PREFERENCES_DAO)).update(preferences);
	}

	public void setUnit(int index, UnitKey key)
	{
		Unit unit;

		if (key.equals(DEFAULT_FIRST_UNIT) || key.equals(DEFAULT_SECOND_UNIT))
		{
			unit = preferencesUnits.get(index);
		}
		else
		{
			unit = mainWindowUnits.get(index);
		}

		setUnit(unit, key);
	}

	public int getPreferencesUnitIndex(int id)
	{
		for (int i = 0; i < preferencesUnits.size(); i++)
		{
			if (preferencesUnits.get(i).getUnitId() == id)
			{
				return i;
			}
		}

		return 0;
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

	public Unit getUnit(UnitKey key)
	{
		return units.get(key);
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
		if (appLanguage == null)
		{
			try (CloseableIterator<?> it = daos.get(PREFERENCES_DAO).closeableIterator())
			{
				Preferences prefs = (Preferences) it.next();
				if (prefs.getAppLanguage().getAppLanguageId() == 1)
				{
					return "English";
				}
				else
				{
					return "Polski";
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
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

	public boolean numberOfDecimalPlacesExistsInDB(int numberOfDecimalPlacesId)
	{
		@SuppressWarnings("unchecked")
		Dao<NumberOfDecimalPlaces, Integer> dao = (Dao<NumberOfDecimalPlaces, Integer>) daos
				.get(NUMBER_OF_DECIMAL_PLACES_DAO);
		return idExistInDB(numberOfDecimalPlacesId, dao);
	}

	public boolean unitTypeExistsInDB(int unitTypeId)
	{
		@SuppressWarnings("unchecked")
		Dao<UnitType, Integer> dao = (Dao<UnitType, Integer>) daos.get(UNIT_TYPE_DAO);
		return idExistInDB(unitTypeId, dao);
	}

	public boolean unitExistsInDB(int unitId)
	{
		@SuppressWarnings("unchecked")
		Dao<Unit, Integer> dao = (Dao<Unit, Integer>) daos.get(UNIT_DAO);
		return idExistInDB(unitId, dao);
	}

	public boolean appSkinExistsInDB(int appSkinId)
	{
		@SuppressWarnings("unchecked")
		Dao<AppSkin, Integer> dao = (Dao<AppSkin, Integer>) daos.get(APP_SKIN_DAO);
		return idExistInDB(appSkinId, dao);
	}

	public boolean appLanguageExistsInDB(int appLanguageId)
	{
		@SuppressWarnings("unchecked")
		Dao<AppLanguage, Integer> dao = (Dao<AppLanguage, Integer>) daos.get(APP_LANGUAGE_DAO);
		return idExistInDB(appLanguageId, dao);
	}

	public boolean appUnitsLanguageExistsInDB(int unitsLanguageId)
	{
		@SuppressWarnings("unchecked")
		Dao<UnitsLanguage, Integer> dao = (Dao<UnitsLanguage, Integer>) daos.get(UNITS_LANGUAGE_DAO);
		return idExistInDB(unitsLanguageId, dao);
	}

	private boolean idExistInDB(int id, Dao<?, Integer> dao)
	{
		Object object = null;

		try
		{
			object = dao.queryForId(id);
		}
		catch (SQLException e)
		{
			return false;
		}

		return object == null ? false : true;
	}

	public int getNumberOfDecimalPlacesIndex(int id)
	{
		for (int i = 0; i < numbersOfDecimalPlaces.size(); i++)
		{
			if (numbersOfDecimalPlaces.get(i).getNumberOfDecimalPlacesId() == id)
			{
				return i;
			}
		}

		return 0;
	}

	public int getAppLanguageIndex(int id)
	{
		for (int i = 0; i < appLanguages.size(); i++)
		{
			if (appLanguages.get(i).getAppLanguageId() == id)
			{
				return i;
			}
		}

		return 0;
	}

	public int getUnitsLanguageIndex(int id)
	{
		for (int i = 0; i < unitsLanguages.size(); i++)
		{
			if (unitsLanguages.get(i).getUnitsLanguageId() == id)
			{
				return i;
			}
		}

		return 0;
	}

	public int getAppSkinIndex(int id)
	{
		for (int i = 0; i < appSkins.size(); i++)
		{
			if (appSkins.get(i).getAppSkinId() == id)
			{
				return i;
			}
		}

		return 0;
	}

	public boolean currentUnitsAreCurrencies()
	{
		return units.get(CURRENT_FIRST_UNIT).getUnitType().getUnitTypeId() == 7;
	}
}