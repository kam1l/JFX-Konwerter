package com.gmail.kamiloleksik.jfxkonwerter.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.gmail.kamiloleksik.jfxkonwerter.model.Model;
import com.gmail.kamiloleksik.jfxkonwerter.model.entity.Preferences;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class PreferencesUtil
{
	public static void exportToFile(Preferences preferences) throws IOException
	{
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("Preferences Files", "*.dat"));
		File file = fc.showSaveDialog(null);

		if (file != null)
		{
			try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
			{
				dos.writeInt(preferences.getPreferencesId());
				dos.writeInt(preferences.getNumberOfDecimalPlaces().getNumberOfDecimalPlacesId());
				dos.writeInt(preferences.getUnitType().getUnitTypeId());
				dos.writeInt(preferences.getFirstUnit().getUnitId());
				dos.writeInt(preferences.getSecondUnit().getUnitId());
				dos.writeInt(preferences.getAppSkin().getAppSkinId());
				dos.writeInt(preferences.getAppLanguage().getAppLanguageId());
				dos.writeInt(preferences.getUnitsLanguage().getUnitsLanguageId());
				dos.writeBoolean(preferences.getUpdateExchangeRatesOnStartup());
				dos.writeBoolean(preferences.getCheckForApplicationUpdatesOnStartup());
				dos.writeBoolean(preferences.getLogHistory());
			}
		}
	}

	public static Preferences importFromFile() throws FileNotFoundException, IOException
	{
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("Preferences Files", "*.dat"));
		File file = fc.showOpenDialog(null);

		if (file != null)
		{
			try (DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(file))))
			{
				int preferencesId = is.readInt();
				int numberOfDecimalPlacesId = is.readInt();
				int unitTypeId = is.readInt();
				int firstUnitId = is.readInt();
				int secondUnitId = is.readInt();
				int appSkinId = is.readInt();
				int appLanguageId = is.readInt();
				int unitsLanguageId = is.readInt();
				boolean updateExchangeRatesOnStartup = is.readBoolean();
				boolean checkForApplicationUpdatesOnStartup = is.readBoolean();
				boolean logHistory = is.readBoolean();

				return new Preferences(preferencesId, numberOfDecimalPlacesId, unitTypeId, firstUnitId, secondUnitId,
						appLanguageId, unitsLanguageId, appSkinId, updateExchangeRatesOnStartup,
						checkForApplicationUpdatesOnStartup, logHistory);
			}
		}

		return null;
	}

	public static void validate(Preferences prefs, Model model) throws IOException
	{
		if (prefs.getPreferencesId() != model.getPreferencesId()
				|| !model.numberOfDecimalPlacesExistsInDB(prefs.getNumberOfDecimalPlaces().getNumberOfDecimalPlacesId())
				|| !model.unitTypeExistsInDB(prefs.getUnitType().getUnitTypeId())
				|| !model.unitExistsInDB(prefs.getFirstUnit().getUnitId())
				|| !model.unitExistsInDB(prefs.getSecondUnit().getUnitId())
				|| !model.appSkinExistsInDB(prefs.getAppSkin().getAppSkinId())
				|| !model.appLanguageExistsInDB(prefs.getAppLanguage().getAppLanguageId())
				|| !model.appUnitsLanguageExistsInDB(prefs.getUnitsLanguage().getUnitsLanguageId()))
		{
			throw new IOException();
		}
	}

	public static boolean preferencesAreDifferent(Preferences prefs, Preferences newPrefs)
	{
		return newPrefs.getNumberOfDecimalPlaces().getNumberOfDecimalPlacesId() != prefs.getNumberOfDecimalPlaces()
				.getNumberOfDecimalPlacesId()
				|| newPrefs.getUnitType().getUnitTypeId() != prefs.getUnitType().getUnitTypeId()
				|| newPrefs.getFirstUnit().getUnitId() != prefs.getFirstUnit().getUnitId()
				|| newPrefs.getSecondUnit().getUnitId() != prefs.getSecondUnit().getUnitId()
				|| newPrefs.getAppSkin().getAppSkinId() != prefs.getAppSkin().getAppSkinId()
				|| newPrefs.getAppLanguage().getAppLanguageId() != prefs.getAppLanguage().getAppLanguageId()
				|| newPrefs.getUnitsLanguage().getUnitsLanguageId() != prefs.getUnitsLanguage().getUnitsLanguageId()
				|| newPrefs.getUpdateExchangeRatesOnStartup() != prefs.getUpdateExchangeRatesOnStartup()
				|| newPrefs.getCheckForApplicationUpdatesOnStartup() != prefs.getCheckForApplicationUpdatesOnStartup()
				|| newPrefs.getLogHistory() != prefs.getLogHistory();
	}
}
