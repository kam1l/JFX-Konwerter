package com.gmail.kamiloleksik.jfxkonwerter.model.dto;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "UnitsLanguage")
public class UnitsLanguage implements Comparable<UnitsLanguage>
{
	@DatabaseField(generatedId = true)
	private int unitsLanguageId;
	@DatabaseField(canBeNull = false, unique = true)
	private String unitsLanguageName;

	public UnitsLanguage()
	{
		// ORMLite needs a no-arg constructor
	}

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

	@Override
	public int compareTo(UnitsLanguage o)
	{
		return unitsLanguageName.compareToIgnoreCase(o.unitsLanguageName);
	}
}
