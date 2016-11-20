package com.gmail.kamiloleksik.model.dto;

public class UnitType
{
	private final int unitTypeId;
	private final String unitTypeName;
	private final String classifier;

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
}
