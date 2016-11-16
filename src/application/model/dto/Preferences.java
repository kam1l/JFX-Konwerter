package application.model.dto;

public class Preferences
{
	private final int preferencesId;
	private final int defaultNumberOfDecimalPlacesId;
	private final int defaultUnitTypeId;
	private final int defaultFirstUnitId;
	private final int defaultSecondUnitId;
	private final int defaultAppLanguageId;
	private final int defaultUnitsLanguageId;
	private final int defaultAppSkinId;

	public Preferences(int preferencesId, int defaultNumberOfDecimalPlacesId, int defaultUnitTypeId,
			int defaultFirstUnitId, int defaultSecondUnitId, int defaultAppLanguageId, int defaultUnitsLanguageId,
			int defaultAppSkinId)
	{
		this.preferencesId = preferencesId;
		this.defaultNumberOfDecimalPlacesId = defaultNumberOfDecimalPlacesId;
		this.defaultUnitTypeId = defaultUnitTypeId;
		this.defaultFirstUnitId = defaultFirstUnitId;
		this.defaultSecondUnitId = defaultSecondUnitId;
		this.defaultAppLanguageId = defaultAppLanguageId;
		this.defaultUnitsLanguageId = defaultUnitsLanguageId;
		this.defaultAppSkinId = defaultAppSkinId;
	}

	public int getPreferencesId()
	{
		return preferencesId;
	}

	public int getDefaultNumberOfDecimalPlacesId()
	{
		return defaultNumberOfDecimalPlacesId;
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

	public int getDefaultAppSkinId()
	{
		return defaultAppSkinId;
	}

	public int getDefaultAppLanguageId()
	{
		return defaultAppLanguageId;
	}

	public int getDefaultUnitsLanguageId()
	{
		return defaultUnitsLanguageId;
	}
}
