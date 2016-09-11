package application;

public class Preferences
{
	private int preferencesId;
	private int numberOfDecimalPlaces;
	private int defaultUnitTypeId;
	private int defaultFirstUnitId;
	private int defaultSecondUnitId;

	public int getPreferencesId()
	{
		return preferencesId;
	}

	public void setPreferencesId(int preferencesId)
	{
		this.preferencesId = preferencesId;
	}

	public int getNumberOfDecimalPlaces()
	{
		return numberOfDecimalPlaces;
	}

	public void setNumberOfDecimalPlaces(int numberOfDecimalPlaces)
	{
		this.numberOfDecimalPlaces = numberOfDecimalPlaces;
	}

	public int getDefaultUnitTypeId()
	{
		return defaultUnitTypeId;
	}

	public void setDefaultUnitTypeId(int defaultUnitTypeId)
	{
		this.defaultUnitTypeId = defaultUnitTypeId;
	}

	public int getDefaultFirstUnitId()
	{
		return defaultFirstUnitId;
	}

	public void setDefaultFirstUnitId(int defaultFirstUnitId)
	{
		this.defaultFirstUnitId = defaultFirstUnitId;
	}

	public int getDefaultSecondUnitId()
	{
		return defaultSecondUnitId;
	}

	public void setDefaultSecondUnitId(int defaultSecondUnitId)
	{
		this.defaultSecondUnitId = defaultSecondUnitId;
	}
}
