package com.gmail.kamiloleksik.jfxkonwerter.model.dto;

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

	public Preferences()
	{
		// ORMLite needs a no-arg constructor
	}

	public Preferences(int preferencesId, NumberOfDecimalPlaces numberOfDecimalPlaces, UnitType unitType,
			Unit firstUnit, Unit secondUnit, AppLanguage appLanguage, UnitsLanguage unitsLanguage, AppSkin appSkin)
	{
		this.preferencesId = preferencesId;
		this.numberOfDecimalPlaces = numberOfDecimalPlaces;
		this.unitType = unitType;
		this.firstUnit = firstUnit;
		this.secondUnit = secondUnit;
		this.appLanguage = appLanguage;
		this.unitsLanguage = unitsLanguage;
		this.appSkin = appSkin;
	}

	public Preferences(int preferencesId, int defaultNumberOfDecimalPlacesId, int defaultUnitTypeId,
			int defaultFirstUnitId, int defaultSecondUnitId, int defaultAppLanguageId, int defaultUnitsLanguageId,
			int defaultAppSkinId)
	{
		this.preferencesId = preferencesId;
		numberOfDecimalPlaces = new NumberOfDecimalPlaces(defaultNumberOfDecimalPlacesId, null);
		unitType = new UnitType(defaultUnitTypeId, null, null);
		firstUnit = new Unit(defaultFirstUnitId, null, null, null, null, null);
		secondUnit = new Unit(defaultSecondUnitId, null, null, null, null, null);
		appLanguage = new AppLanguage(defaultAppLanguageId, null);
		unitsLanguage = new UnitsLanguage(defaultUnitsLanguageId, null);
		appSkin = new AppSkin(defaultAppSkinId, null, null);
	}

	public int getPreferencesId()
	{
		return preferencesId;
	}

	public NumberOfDecimalPlaces getNumberOfDecimalPlaces()
	{
		return numberOfDecimalPlaces;
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
}
