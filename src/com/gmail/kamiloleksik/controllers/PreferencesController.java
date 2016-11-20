package com.gmail.kamiloleksik.controllers;

import java.net.URL;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gmail.kamiloleksik.model.Model;
import com.gmail.kamiloleksik.model.Model.NamesKey;
import com.gmail.kamiloleksik.model.Model.UnitKey;
import com.gmail.kamiloleksik.model.Model.UnitTypeKey;
import com.gmail.kamiloleksik.model.dto.Preferences;
import com.gmail.kamiloleksik.util.Message;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;

public class PreferencesController implements Initializable
{
	private Model model;
	private Message message = new Message();
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private Preferences currentPreferences;
	private EnumMap<IdKeys, Integer> currentIds = new EnumMap<IdKeys, Integer>(IdKeys.class);
	private EnumMap<IdKeys, Integer> selectedIds = new EnumMap<IdKeys, Integer>(IdKeys.class);

	@FXML
	private ComboBox<String> defaultNumberOfDecimalPlacesComboBox, defaultUnitTypeComboBox, defaultFirstUnitComboBox,
			defaultSecondUnitComboBox, defaultAppLanguageComboBox, defaultUnitsLanguageComboBox, defaultAppSkinComboBox;

	private int defaultNumberOfDecimalPlacesIndex, defaultUnitTypeIndex, defaultFirstUnitIndex, defaultSecondUnitIndex,
			defaultAppLanguageIndex, defaultUnitsLanguageIndex, defaultAppSkinIndex;

	private enum IdKeys
	{
		NUMBER_OF_DECIMAL_PLACES_ID, UNIT_TYPE_ID, FIRST_UNIT_ID, SECOND_UNIT_ID, APP_LANGUAGE_ID, UNITS_LANGUAGE_ID,
		APP_SKIN_ID
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		model = MainController.getModel();
		currentPreferences = model.getPreferences();

		ObservableList<String> numbers = model.getNames(NamesKey.ALLOWED_NUMBERS_OF_DECIMAL_PLACES);
		SingleSelectionModel<String> defaultNumberOfDecimalPlacesSel = defaultNumberOfDecimalPlacesComboBox
				.getSelectionModel();
		currentIds.put(IdKeys.NUMBER_OF_DECIMAL_PLACES_ID, currentPreferences.getDefaultNumberOfDecimalPlacesId());

		defaultNumberOfDecimalPlacesComboBox.setItems(numbers);
		defaultNumberOfDecimalPlacesSel.select(String.valueOf(model.getNumberOfDecimalPlaces()));

		ObservableList<String> unitTypeNames = model.getNames(NamesKey.ALL_UNIT_TYPE_NAMES);
		SingleSelectionModel<String> defaultUnitTypeSel = defaultUnitTypeComboBox.getSelectionModel();
		currentIds.put(IdKeys.UNIT_TYPE_ID, currentPreferences.getDefaultUnitTypeId());

		defaultUnitTypeComboBox.setItems(unitTypeNames);
		defaultUnitTypeSel.select(model.getUnitTypeName(UnitTypeKey.DEFAULT_UNIT_TYPE));

		ObservableList<String> unitNames = model.getNames(NamesKey.PREFERENCES_UNIT_NAMES);
		SingleSelectionModel<String> defaultFirstUnitSel = defaultFirstUnitComboBox.getSelectionModel();
		currentIds.put(IdKeys.FIRST_UNIT_ID, currentPreferences.getDefaultFirstUnitId());

		defaultFirstUnitComboBox.setItems(unitNames);
		defaultFirstUnitSel.select(model.getUnitDisplayName(UnitKey.DEFAULT_FIRST_UNIT));

		SingleSelectionModel<String> defaultSecondUnitSel = defaultSecondUnitComboBox.getSelectionModel();
		currentIds.put(IdKeys.SECOND_UNIT_ID, currentPreferences.getDefaultSecondUnitId());

		defaultSecondUnitComboBox.setItems(unitNames);
		defaultSecondUnitSel.select(model.getUnitDisplayName(UnitKey.DEFAULT_SECOND_UNIT));

		ObservableList<String> appLanguagesNames = model.getNames(NamesKey.APP_LANGUAGES_NAMES);
		SingleSelectionModel<String> defaultAppLanguageSel = defaultAppLanguageComboBox.getSelectionModel();
		currentIds.put(IdKeys.APP_LANGUAGE_ID, currentPreferences.getDefaultAppLanguageId());

		defaultAppLanguageComboBox.setItems(appLanguagesNames);
		defaultAppLanguageSel.select(model.getAppLanguageName());

		ObservableList<String> unitsLanguagesNames = model.getNames(NamesKey.UNITS_LANGUAGES_NAMES);
		SingleSelectionModel<String> defaultUnitsLanguageSel = defaultUnitsLanguageComboBox.getSelectionModel();
		currentIds.put(IdKeys.UNITS_LANGUAGE_ID, currentPreferences.getDefaultUnitsLanguageId());

		defaultUnitsLanguageComboBox.setItems(unitsLanguagesNames);
		defaultUnitsLanguageSel.select(model.getUnitsLanguageName());

		ObservableList<String> appSkinNames = model.getNames(NamesKey.APP_SKINS_NAMES);
		SingleSelectionModel<String> defaultAppSkinSel = defaultAppSkinComboBox.getSelectionModel();
		currentIds.put(IdKeys.APP_SKIN_ID, currentPreferences.getDefaultAppSkinId());

		defaultAppSkinComboBox.setItems(appSkinNames);
		defaultAppSkinSel.select(model.getAppSkinName());
	}

	public void changeUnitSet(ActionEvent event)
	{
		defaultFirstUnitComboBox.getItems().clear();
		defaultSecondUnitComboBox.getItems().clear();

		SingleSelectionModel<String> defaultUnitTypeSel = defaultUnitTypeComboBox.getSelectionModel();
		int currentUnitTypeIndex = defaultUnitTypeSel.getSelectedIndex();

		model.setPreferencesUnitTypeIndex(currentUnitTypeIndex);
		model.changePreferencesSetOfUnits(currentUnitTypeIndex);

		ObservableList<String> unitNames = model.getNames(NamesKey.PREFERENCES_UNIT_NAMES);
		SingleSelectionModel<String> defaultFirstUnitSel = defaultFirstUnitComboBox.getSelectionModel();

		defaultFirstUnitComboBox.setItems(unitNames);
		defaultFirstUnitSel.select(0);

		SingleSelectionModel<String> defaultSecondUnitSel = defaultSecondUnitComboBox.getSelectionModel();

		defaultSecondUnitComboBox.setItems(unitNames);
		defaultSecondUnitSel.select(0);
	}

	public void savePreferences(ActionEvent event)
	{
		if (changesWereMade())
		{
			executor.execute(() ->
			{
				Preferences prefs = getNewValues();
				boolean taskSucceeded = model.updatePreferencesInDB(prefs);

				Platform.runLater(() ->
				{
					if (taskSucceeded)
					{
						updatePreferencesRamDataStructures(prefs);
					}
					else
					{
						message.showMessage(Message.ERROR_TITLE, Message.SAVING_PREFERENCES_ERROR_MESSAGE);
					}
				});
			});
		}

		executor.shutdown();
		MainController.getStage().close();
	}

	private boolean changesWereMade()
	{
		defaultNumberOfDecimalPlacesIndex = defaultNumberOfDecimalPlacesComboBox.getSelectionModel().getSelectedIndex();
		defaultUnitTypeIndex = defaultUnitTypeComboBox.getSelectionModel().getSelectedIndex();
		defaultFirstUnitIndex = defaultFirstUnitComboBox.getSelectionModel().getSelectedIndex();
		defaultSecondUnitIndex = defaultSecondUnitComboBox.getSelectionModel().getSelectedIndex();
		defaultAppLanguageIndex = defaultAppLanguageComboBox.getSelectionModel().getSelectedIndex();
		defaultUnitsLanguageIndex = defaultUnitsLanguageComboBox.getSelectionModel().getSelectedIndex();
		defaultAppSkinIndex = defaultAppSkinComboBox.getSelectionModel().getSelectedIndex();

		selectedIds.put(IdKeys.NUMBER_OF_DECIMAL_PLACES_ID,
				model.getNumbersOfDecimalPlaces(defaultNumberOfDecimalPlacesIndex).getNumberOfDecimalPlacesId());
		selectedIds.put(IdKeys.UNIT_TYPE_ID, model.getUnitType(defaultUnitTypeIndex).getUnitTypeId());
		selectedIds.put(IdKeys.FIRST_UNIT_ID, model.getPreferencesUnit(defaultFirstUnitIndex).getUnitId());
		selectedIds.put(IdKeys.SECOND_UNIT_ID, model.getPreferencesUnit(defaultSecondUnitIndex).getUnitId());
		selectedIds.put(IdKeys.APP_LANGUAGE_ID, model.getAppLanguages(defaultAppLanguageIndex).getAppLanguageId());
		selectedIds.put(IdKeys.UNITS_LANGUAGE_ID,
				model.getUnitsLanguages(defaultUnitsLanguageIndex).getUnitsLanguageId());
		selectedIds.put(IdKeys.APP_SKIN_ID, model.getAppSkins(defaultAppSkinIndex).getAppSkinId());

		return !Arrays.equals(currentIds.values().toArray(), selectedIds.values().toArray());
	}

	private Preferences getNewValues()
	{
		return new Preferences(currentPreferences.getPreferencesId(),
				selectedIds.get(IdKeys.NUMBER_OF_DECIMAL_PLACES_ID), selectedIds.get(IdKeys.UNIT_TYPE_ID),
				selectedIds.get(IdKeys.FIRST_UNIT_ID), selectedIds.get(IdKeys.SECOND_UNIT_ID),
				selectedIds.get(IdKeys.APP_LANGUAGE_ID), selectedIds.get(IdKeys.UNITS_LANGUAGE_ID),
				selectedIds.get(IdKeys.APP_SKIN_ID));
	}

	private void updatePreferencesRamDataStructures(Preferences newPreferences)
	{
		model.setPreferences(newPreferences);

		if (defaultUnitTypeWasChanged())
		{
			model.setDefaultUnitTypeIndex(defaultUnitTypeIndex);
			model.setDefaultUnitType(defaultUnitTypeIndex);
		}

		if (defaultFirstUnitWasChanged())
		{
			model.setUnit(defaultFirstUnitIndex, UnitKey.DEFAULT_FIRST_UNIT);
		}

		if (defaultSecondUnitWasChanged())
		{
			model.setUnit(defaultSecondUnitIndex, UnitKey.DEFAULT_SECOND_UNIT);
		}

		if (numberOfDecimalPlacesWasChanged())
		{
			model.setNumberOfDecimalPlaces(defaultNumberOfDecimalPlacesIndex);
			MainController.numberOfDecimalPlacesWasChanged.setValue(true);
		}

		if (defaultAppLanguageWasChanged())
		{
			model.setAppLanguage(defaultAppLanguageIndex);
			MainController.defaultAppLanguageWasChanged.setValue(true);
		}

		if (defaultUnitsLanguageWasChanged())
		{
			model.setUnitsLanguage(defaultUnitsLanguageIndex);
			MainController.defaultUnitsLanguageWasChanged.setValue(true);
		}

		if (defaultAppSkinWasChanged())
		{
			model.setAppSkin(defaultAppSkinIndex);
			MainController.defaultSkinNameWasChanged.setValue(true);
		}
	}

	private boolean defaultUnitTypeWasChanged()
	{
		return currentIds.get(IdKeys.UNIT_TYPE_ID) != selectedIds.get(IdKeys.UNIT_TYPE_ID);
	}

	private boolean defaultFirstUnitWasChanged()
	{
		return currentIds.get(IdKeys.FIRST_UNIT_ID) != selectedIds.get(IdKeys.FIRST_UNIT_ID);
	}

	private boolean defaultSecondUnitWasChanged()
	{
		return currentIds.get(IdKeys.SECOND_UNIT_ID) != selectedIds.get(IdKeys.SECOND_UNIT_ID);
	}

	private Boolean numberOfDecimalPlacesWasChanged()
	{
		return currentIds.get(IdKeys.NUMBER_OF_DECIMAL_PLACES_ID) != selectedIds
				.get(IdKeys.NUMBER_OF_DECIMAL_PLACES_ID);
	}

	private Boolean defaultAppLanguageWasChanged()
	{
		return currentIds.get(IdKeys.APP_LANGUAGE_ID) != selectedIds.get(IdKeys.APP_LANGUAGE_ID);
	}

	private Boolean defaultUnitsLanguageWasChanged()
	{
		return currentIds.get(IdKeys.UNITS_LANGUAGE_ID) != selectedIds.get(IdKeys.UNITS_LANGUAGE_ID);
	}

	private Boolean defaultAppSkinWasChanged()
	{
		return currentIds.get(IdKeys.APP_SKIN_ID) != selectedIds.get(IdKeys.APP_SKIN_ID);
	}
}