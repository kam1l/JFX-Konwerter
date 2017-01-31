package com.gmail.kamiloleksik.jfxkonwerter.model.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "UnitType")
public class UnitType implements Comparable<UnitType>
{
	@DatabaseField(generatedId = true)
	private int unitTypeId;
	@DatabaseField(canBeNull = false)
	private String unitTypeName;
	@DatabaseField(canBeNull = false)
	private String classifier;

	public UnitType()
	{
		// ORMLite needs a no-arg constructor
	}

	public UnitType(int unitTypeId, String unitTypeName, String classifier)
	{
		this.unitTypeId = unitTypeId;
		this.unitTypeName = unitTypeName;
		this.classifier = classifier;
	}

	public UnitType(UnitType another)
	{
		this.unitTypeId = another.unitTypeId;
		this.unitTypeName = another.unitTypeName;
		this.classifier = another.classifier;
	}

	public int getUnitTypeId()
	{
		return unitTypeId;
	}

	public String getUnitTypeName()
	{
		return unitTypeName;
	}

	public String getUnitTypeClassifier()
	{
		return classifier;
	}

	@Override
	public int compareTo(UnitType o)
	{
		return unitTypeName.compareToIgnoreCase(o.unitTypeName);
	}

	public void setUnitTypeName(String unitTypeName)
	{
		this.unitTypeName = unitTypeName;
	}
}
