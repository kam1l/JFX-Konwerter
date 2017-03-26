package com.gmail.kamiloleksik.jfxkonwerter.model.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Preferences")
public class Preferences
{
	@DatabaseField(generatedId = true)
	private int preferencesId;
	@DatabaseField(canBeNull = false, foreign = true, columnName = "defaultNumberOfDecimalPlacesId")
	private NumberOfDecimalPlaces numberOfDecimalPlaces;
	@DatabaseField(canBeNull = false, foreign = true, columnName = "defaultUnitTypeId")
	private UnitType unitType;
	@DatabaseField(canBeNull = false, foreign = true, columnName = "defaultFirstUnitId")
	private Unit firstUnit;
	@DatabaseField(canBeNull = false, foreign = true, columnName = "defaultSecondUnitId")
	private Unit secondUnit;
	@DatabaseField(canBeNull = false, foreign = true, columnName = "defaultAppLanguageId")
	private AppLanguage appLanguage;
	@DatabaseField(canBeNull = false, foreign = true, columnName = "defaultUnitsLanguageId")
	private UnitsLanguage unitsLanguage;
	@DatabaseField(canBeNull = false, foreign = true, columnName = "defaultAppSkinId")
	private AppSkin appSkin;
	@DatabaseField(canBeNull = false)
	private boolean updateExchangeRatesOnStartup;
	@DatabaseField(canBeNull = false)
	private boolean checkForApplicationUpdatesOnStartup;
	@DatabaseField(canBeNull = false)
	private boolean logHistory;
	@DatabaseField(canBeNull = false)
	private boolean alwaysOnTop;

	public Preferences()
	{
		// ORMLite needs a no-arg constructor
	}

	public Preferences(int preferencesId, NumberOfDecimalPlaces numberOfDecimalPlaces, UnitType unitType,
			Unit firstUnit, Unit secondUnit, AppLanguage appLanguage, UnitsLanguage unitsLanguage, AppSkin appSkin,
			boolean updateExchangeRatesOnStartup, boolean checkForApplicationUpdatesOnStartup, boolean logHistory, 
			boolean alwaysOnTop)
	{
		this.preferencesId = preferencesId;
		this.numberOfDecimalPlaces = numberOfDecimalPlaces;
		this.unitType = unitType;
		this.firstUnit = firstUnit;
		this.secondUnit = secondUnit;
		this.appLanguage = appLanguage;
		this.unitsLanguage = unitsLanguage;
		this.appSkin = appSkin;
		this.updateExchangeRatesOnStartup = updateExchangeRatesOnStartup;
		this.checkForApplicationUpdatesOnStartup = checkForApplicationUpdatesOnStartup;
		this.logHistory = logHistory;
		this.alwaysOnTop = alwaysOnTop;
	}

	public Preferences(int preferencesId, int defaultNumberOfDecimalPlacesId, int defaultUnitTypeId,
			int defaultFirstUnitId, int defaultSecondUnitId, int defaultAppLanguageId, int defaultUnitsLanguageId,
			int defaultAppSkinId, boolean updateExchangeRatesOnStartup, boolean checkForApplicationUpdatesOnStartup,
			boolean logHistory, boolean alwaysOnTop)
	{
		this(preferencesId, new NumberOfDecimalPlaces(defaultNumberOfDecimalPlacesId, null),
				new UnitType(defaultUnitTypeId, null, null), new Unit(defaultFirstUnitId, null, null, null, null, null),
				new Unit(defaultSecondUnitId, null, null, null, null, null),
				new AppLanguage(defaultAppLanguageId, null), new UnitsLanguage(defaultUnitsLanguageId, null),
				new AppSkin(defaultAppSkinId, null, null), updateExchangeRatesOnStartup,
				checkForApplicationUpdatesOnStartup, logHistory, alwaysOnTop);
	}

	public int getPreferencesId()
	{
		return preferencesId;
	}

	public NumberOfDecimalPlaces getNumberOfDecimalPlaces()
	{
		return numberOfDecimalPlaces;
	}

	public void setNumberOfDecimalPlaces(NumberOfDecimalPlaces numberOfDecimalPlaces)
	{
		this.numberOfDecimalPlaces = numberOfDecimalPlaces;
	}

	public UnitType getUnitType()
	{
		return unitType;
	}

	public Unit getFirstUnit()
	{
		return firstUnit;
	}

	public Unit getSecondUnit()
	{
		return secondUnit;
	}

	public AppLanguage getAppLanguage()
	{
		return appLanguage;
	}

	public UnitsLanguage getUnitsLanguage()
	{
		return unitsLanguage;
	}

	public AppSkin getAppSkin()
	{
		return appSkin;
	}

	public boolean updateExchangeRatesOnStartup()
	{
		return updateExchangeRatesOnStartup;
	}

	public void setUpdateExchangeRatesOnStartup(boolean updateExchangeRatesOnStartup)
	{
		this.updateExchangeRatesOnStartup = updateExchangeRatesOnStartup;
	}

	public boolean checkForApplicationUpdatesOnStartup()
	{
		return checkForApplicationUpdatesOnStartup;
	}

	public void setCheckForApplicationUpdatesOnStartup(boolean checkForApplicationUpdatesOnStartup)
	{
		this.checkForApplicationUpdatesOnStartup = checkForApplicationUpdatesOnStartup;
	}

	public boolean logHistory()
	{
		return logHistory;
	}

	public void setLogHistory(boolean logHistory)
	{
		this.logHistory = logHistory;
	}

	public boolean alwaysOnTop()
	{
		return alwaysOnTop;
	}

	public void setAlwaysOnTop(boolean alwaysOnTop)
	{
		this.alwaysOnTop = alwaysOnTop;
	}
}
