package com.gmail.kamiloleksik.jfxkonwerter.model.dto;

public class AppLanguage
{
	private final int appLanguageId;
	private final String appLanguageName;

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
}
