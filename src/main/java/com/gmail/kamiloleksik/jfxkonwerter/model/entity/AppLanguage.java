package com.gmail.kamiloleksik.jfxkonwerter.model.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "AppLanguage")
public class AppLanguage implements Comparable<AppLanguage>
{
	@DatabaseField(generatedId = true)
	private int appLanguageId;
	@DatabaseField(canBeNull = false, unique = true)
	private String appLanguageName;

	public AppLanguage()
	{
		// ORMLite needs a no-arg constructor
	}

	public AppLanguage(int appLanguageId, String appLanguageName)
	{
		this.appLanguageId = appLanguageId;
		this.appLanguageName = appLanguageName;
	}

	public int getAppLanguageId()
	{
		return appLanguageId;
	}

	public String getAppLanguageName()
	{
		return appLanguageName;
	}

	@Override
	public int compareTo(AppLanguage o)
	{
		return appLanguageName.compareToIgnoreCase(o.appLanguageName);
	}
}
