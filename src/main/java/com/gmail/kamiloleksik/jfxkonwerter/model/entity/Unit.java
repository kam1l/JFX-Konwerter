package com.gmail.kamiloleksik.jfxkonwerter.model.entity;

import java.math.BigDecimal;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Unit")
public class Unit implements Comparable<Unit>
{
	@DatabaseField(generatedId = true)
	private int unitId;
	@DatabaseField(canBeNull = false)
	private String unitName;
	@DatabaseField(unique = true)
	private String unitAbbreviation;
	@DatabaseField(persisted = false)
	private String unitDisplayName;
	@DatabaseField(canBeNull = false)
	private BigDecimal unitRatio;
	@DatabaseField(canBeNull = false, foreign = true, columnName = "unitType_unitTypeId")
	private UnitType unitType;

	public Unit()
	{
		// ORMLite needs a no-arg constructor
	}

	public Unit(int unitId, String unitName, String unitAbbreviation, String unitDisplayName, BigDecimal unitRatio,
			UnitType unitType)
	{
		this.unitId = unitId;
		this.unitName = unitName;
		this.unitAbbreviation = unitAbbreviation;
		this.unitDisplayName = unitDisplayName;
		this.unitRatio = unitRatio;
		this.unitType = unitType;
	}

	public Unit(Unit another)
	{
		this.unitId = another.unitId;
		this.unitName = another.unitName;
		this.unitAbbreviation = another.unitAbbreviation;
		this.unitDisplayName = another.unitDisplayName;
		this.unitRatio = another.unitRatio;
		this.unitType = another.unitType;
	}

	public void setUnitRatio(BigDecimal unitRatio)
	{
		this.unitRatio = unitRatio;
	}

	public int getUnitId()
	{
		return unitId;
	}

	public String getUnitName()
	{
		return unitName;
	}

	public String getUnitAbbreviation()
	{
		return unitAbbreviation;
	}

	public String getUnitDisplayName()
	{
		return unitDisplayName;
	}

	public BigDecimal getUnitRatio()
	{
		return unitRatio;
	}

	public BigDecimal getNumberBase()
	{
		return unitRatio;
	}

	public boolean isCurrency()
	{
		return unitType.getUnitTypeId() == 7 ? true : false;
	}

	public UnitType getUnitType()
	{
		return unitType;
	}

	public void setDisplayName(String unitDisplayName)
	{
		this.unitDisplayName = unitDisplayName;
	}

	@Override
	public int compareTo(Unit o)
	{
		return unitName.compareToIgnoreCase(o.unitName);
	}
}
