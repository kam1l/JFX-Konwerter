package com.gmail.kamiloleksik.jfxkonwerter.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "NumberOfDecimalPlaces")
public class NumberOfDecimalPlaces implements Comparable<NumberOfDecimalPlaces>
{
	@DatabaseField(generatedId = true)
	private int numberOfDecimalPlacesId;
	@DatabaseField(canBeNull = false, unique = true)
	private String numberOfDecimalPlaces;

	public NumberOfDecimalPlaces()
	{
		// ORMLite needs a no-arg constructor
	}

	public NumberOfDecimalPlaces(int numberOfDecimalPlacesId, String numberOfDecimalPlaces)
	{
		this.numberOfDecimalPlacesId = numberOfDecimalPlacesId;
		this.numberOfDecimalPlaces = numberOfDecimalPlaces;
	}

	public int getNumberOfDecimalPlacesId()
	{
		return numberOfDecimalPlacesId;
	}

	public String getNumberOfDecimalPlaces()
	{
		return numberOfDecimalPlaces;
	}

	@Override
	public int compareTo(NumberOfDecimalPlaces o)
	{
		return Integer.valueOf(numberOfDecimalPlaces).compareTo(Integer.valueOf(o.numberOfDecimalPlaces));
	}
}
