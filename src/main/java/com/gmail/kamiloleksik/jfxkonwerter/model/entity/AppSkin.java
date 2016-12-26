package com.gmail.kamiloleksik.jfxkonwerter.model.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "AppSkin")
public class AppSkin implements Comparable<AppSkin>
{
	@DatabaseField(generatedId = true)
	private int appSkinId;
	@DatabaseField(canBeNull = false, unique = true)
	private String appSkinName;
	@DatabaseField(canBeNull = false)
	private String appSkinPath;

	public AppSkin()
	{
		// ORMLite needs a no-arg constructor
	}

	public AppSkin(int appSkinId, String appSkinName, String appSkinPath)
	{
		this.appSkinId = appSkinId;
		this.appSkinName = appSkinName;
		this.appSkinPath = appSkinPath;
	}

	public int getAppSkinId()
	{
		return appSkinId;
	}

	public String getAppSkinName()
	{
		return appSkinName;
	}

	public String getAppSkinPath()
	{
		return appSkinPath;
	}

	@Override
	public int compareTo(AppSkin o)
	{
		return appSkinName.compareToIgnoreCase(o.appSkinName);
	}
}
