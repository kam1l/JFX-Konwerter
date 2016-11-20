package com.gmail.kamiloleksik.model.dto;

public class AppSkin
{
	private final int appSkinId;
	private final String appSkinName;
	private final String appSkinPath;

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
}
