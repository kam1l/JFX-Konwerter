package com.gmail.kamiloleksik.jfxkonwerter.model.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Unit_en")
public class Unit_en implements Comparable<Unit_en>
{
	@DatabaseField(generatedId = true)
	private int unitId;
	@DatabaseField(canBeNull = false)
	private String unitName;

	public Unit_en()
	{
		// ORMLite needs a no-arg constructor
	}

	public Unit_en(int unitId, String unitName)
	{
		this.unitId = unitId;
		this.unitName = unitName;
	}

	public Unit_en(Unit_en another)
	{
		this.unitId = another.unitId;
		this.unitName = another.unitName;
	}

	public int getUnitId()
	{
		return unitId;
	}

	public String getUnitName()
	{
		return unitName;
	}

	@Override
	public int compareTo(Unit_en o)
	{
		return unitName.compareToIgnoreCase(o.unitName);
	}
}
