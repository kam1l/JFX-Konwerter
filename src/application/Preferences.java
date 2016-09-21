package application;

public class Preferences
{
	private final int preferencesId;
	private final int numberOfDecimalPlaces;
	private final int defaultUnitTypeId;
	private final int defaultFirstUnitId;
	private final int defaultSecondUnitId;
	private final String defaultSkinName;

	public Preferences(int preferencesId, int numberOfDecimalPlaces, int defaultUnitTypeId, int defaultFirstUnitId,
			int defaultSecondUnitId, String defaultSkinName)
	{
		this.preferencesId = preferencesId;
		this.numberOfDecimalPlaces = numberOfDecimalPlaces;
		this.defaultUnitTypeId = defaultUnitTypeId;
		this.defaultFirstUnitId = defaultFirstUnitId;
		this.defaultSecondUnitId = defaultSecondUnitId;
		this.defaultSkinName = defaultSkinName;
	}

	public int getPreferencesId()
	{
		return preferencesId;
	}

	public int getNumberOfDecimalPlaces()
	{
		return numberOfDecimalPlaces;
	}

	public int getDefaultUnitTypeId()
	{
		return defaultUnitTypeId;
	}

	public int getDefaultFirstUnitId()
	{
		return defaultFirstUnitId;
	}

	public int getDefaultSecondUnitId()
	{
		return defaultSecondUnitId;
	}

	public String getDefaultSkinName()
	{
		return defaultSkinName;
	}
}
