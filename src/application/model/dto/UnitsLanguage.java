package application.model.dto;

public class UnitsLanguage
{
	private final int unitsLanguageId;
	private final String unitsLanguageName;

	public UnitsLanguage(int unitsLanguageId, String unitsLanguageName)
	{
		this.unitsLanguageId = unitsLanguageId;
		this.unitsLanguageName = unitsLanguageName;
	}

	public int getUnitsLanguageId()
	{
		return unitsLanguageId;
	}

	public String getUnitsLanguageName()
	{
		return unitsLanguageName;
	}
}
