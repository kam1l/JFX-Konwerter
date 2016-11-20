package com.gmail.kamiloleksik.jfxkonwerter.model.dto;

public class NumberOfDecimalPlaces
{
	private final int numberOfDecimalPlacesId;
	private final int numberOfDecimalPlaces;

	public NumberOfDecimalPlaces(int numberOfDecimalPlacesId, int numberOfDecimalPlaces)
	{
		this.numberOfDecimalPlacesId = numberOfDecimalPlacesId;
		this.numberOfDecimalPlaces = numberOfDecimalPlaces;
	}

	public int getNumberOfDecimalPlacesId()
	{
		return numberOfDecimalPlacesId;
	}

	public int getNumberOfDecimalPlaces()
	{
		return numberOfDecimalPlaces;
	}
}
