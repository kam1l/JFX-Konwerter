package com.gmail.kamiloleksik.jfxkonwerter.model.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "UnitType_en")
public class UnitType_en implements Comparable<UnitType_en>
{
	@DatabaseField(generatedId = true)
	private int unitTypeId;
	@DatabaseField(canBeNull = false)
	private String unitTypeName;

	public UnitType_en()
	{
		// ORMLite needs a no-arg constructor
	}

	public UnitType_en(int unitTypeId, String unitTypeName)
	{
		this.unitTypeId = unitTypeId;
		this.unitTypeName = unitTypeName;
	}

	public UnitType_en(UnitType_en another)
	{
		this.unitTypeId = another.unitTypeId;
		this.unitTypeName = another.unitTypeName;
	}

	public int getUnitTypeId()
	{
		return unitTypeId;
	}

	public String getUnitTypeName()
	{
		return unitTypeName;
	}

	@Override
	public int compareTo(UnitType_en o)
	{
		return unitTypeName.compareToIgnoreCase(o.unitTypeName);
	}
}
