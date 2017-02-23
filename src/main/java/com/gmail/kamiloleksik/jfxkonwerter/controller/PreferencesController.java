package com.gmail.kamiloleksik.jfxkonwerter.controller;

import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.IdKey.*;
import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.NamesKey.*;
import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.UnitKey.*;
import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.UnitTypeKey.*;

import java.net.URL;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gmail.kamiloleksik.jfxkonwerter.model.Model;
import com.gmail.kamiloleksik.jfxkonwerter.model.entity.Preferences;
import com.gmail.kamiloleksik.jfxkonwerter.util.Message;
import com.gmail.kamiloleksik.jfxkonwerter.util.keys.IdKey;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;

public class PreferencesController implements Initializable
{
	private Model model;
	private ResourceBundle resourceBundle;
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private Preferences currentPreferences;
	private Map<IdKey, Integer> currentIds = new EnumMap<IdKey, Integer>(IdKey.class);
	private Map<IdKey, Integer> selectedIds = new EnumMap<IdKey, Integer>(IdKey.class);

	@FXML
	private ComboBox<String> defaultNumberOfDecimalPlacesComboBox, defaultUnitTypeComboBox, defaultFirstUnitComboBox,
			defaultSecondUnitComboBox, defaultAppLanguageComboBox, defaultUnitsLanguageComboBox, defaultAppSkinComboBox;

	@FXML
	private CheckBox applicationUpdateCheckBox, exchangeRatesUpdateCheckbox, logHistoryCheckbox;

	private int defaultNumberOfDecimalPlacesIndex, defaultUnitTypeIndex, defaultFirstUnitIndex, defaultSecondUnitIndex,
			defaultAppLanguageIndex, defaultUnitsLanguageIndex, defaultAppSkinIndex;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.resourceBundle = resourceBundle;
		model = (Model) resourceBundle.getObject("model");
		currentPreferences = model.getPreferences();

		ObservableList<String> numbers = model.getNames(ALLOWED_NUMBERS_OF_DECIMAL_PLACES);
		SingleSelectionModel<String> defaultNumberOfDecimalPlacesSel = defaultNumberOfDecimalPlacesComboBox
				.getSelectionModel();
		currentIds.put(NUMBER_OF_DECIMAL_PLACES_ID,
				currentPreferences.getNumberOfDecimalPlaces().getNumberOfDecimalPlacesId());

		defaultNumberOfDecimalPlacesComboBox.setItems(numbers);
		defaultNumberOfDecimalPlacesSel.select(model.getNumberOfDecimalPlaces());

		ObservableList<String> unitTypeNames = model.getNames(ALL_UNIT_TYPE_NAMES);
		SingleSelectionModel<String> defaultUnitTypeSel = defaultUnitTypeComboBox.getSelectionModel();
		currentIds.put(UNIT_TYPE_ID, currentPreferences.getUnitType().getUnitTypeId());

		defaultUnitTypeComboBox.setItems(unitTypeNames);
		defaultUnitTypeSel.select(model.getUnitTypeName(DEFAULT_UNIT_TYPE));

		ObservableList<String> unitNames = model.getNames(PREFERENCES_UNIT_NAMES);
		SingleSelectionModel<String> defaultFirstUnitSel = defaultFirstUnitComboBox.getSelectionModel();
		currentIds.put(FIRST_UNIT_ID, currentPreferences.getFirstUnit().getUnitId());

		defaultFirstUnitComboBox.setItems(unitNames);
		defaultFirstUnitSel.select(model.getUnitDisplayName(DEFAULT_FIRST_UNIT));

		SingleSelectionModel<String> defaultSecondUnitSel = defaultSecondUnitComboBox.getSelectionModel();
		currentIds.put(SECOND_UNIT_ID, currentPreferences.getSecondUnit().getUnitId());

		defaultSecondUnitComboBox.setItems(unitNames);
		defaultSecondUnitSel.select(model.getUnitDisplayName(DEFAULT_SECOND_UNIT));

		ObservableList<String> appLanguagesNames = model.getNames(APP_LANGUAGES_NAMES);
		SingleSelectionModel<String> defaultAppLanguageSel = defaultAppLanguageComboBox.getSelectionModel();
		currentIds.put(APP_LANGUAGE_ID, currentPreferences.getAppLanguage().getAppLanguageId());

		defaultAppLanguageComboBox.setItems(appLanguagesNames);
		defaultAppLanguageSel.select(model.getAppLanguageName());

		ObservableList<String> unitsLanguagesNames = model.getNames(UNITS_LANGUAGES_NAMES);
		SingleSelectionModel<String> defaultUnitsLanguageSel = defaultUnitsLanguageComboBox.getSelectionModel();
		currentIds.put(UNITS_LANGUAGE_ID, currentPreferences.getUnitsLanguage().getUnitsLanguageId());

		defaultUnitsLanguageComboBox.setItems(unitsLanguagesNames);
		defaultUnitsLanguageSel.select(model.getUnitsLanguageName());

		ObservableList<String> appSkinNames = model.getNames(APP_SKINS_NAMES);
		SingleSelectionModel<String> defaultAppSkinSel = defaultAppSkinComboBox.getSelectionModel();
		currentIds.put(APP_SKIN_ID, currentPreferences.getAppSkin().getAppSkinId());

		defaultAppSkinComboBox.setItems(appSkinNames);
		defaultAppSkinSel.select(model.getAppSkinName());

		boolean checkForApplicationUpdatesOnStartup = currentPreferences.getCheckForApplicationUpdatesOnStartup();
		currentIds.put(CHECK_APP_UPDATE_ID, checkForApplicationUpdatesOnStartup ? 1 : 0);
		applicationUpdateCheckBox.setSelected(checkForApplicationUpdatesOnStartup);

		boolean updateExchangeRatesOnStartup = currentPreferences.getUpdateExchangeRatesOnStartup();
		currentIds.put(UPDATE_EXCHANGE_RATES_ID, updateExchangeRatesOnStartup ? 1 : 0);
		exchangeRatesUpdateCheckbox.setSelected(updateExchangeRatesOnStartup);

		boolean logHistory = currentPreferences.getLogHistory();
		currentIds.put(LOG_HISTORY_ID, logHistory ? 1 : 0);
		logHistoryCheckbox.setSelected(logHistory);
	}

	public void changeUnitSet(ActionEvent event)
	{
		defaultFirstUnitComboBox.getItems().clear();
		defaultSecondUnitComboBox.getItems().clear();

		SingleSelectionModel<String> defaultUnitTypeSel = defaultUnitTypeComboBox.getSelectionModel();
		int currentUnitTypeIndex = defaultUnitTypeSel.getSelectedIndex();

		model.setPreferencesUnitTypeIndex(currentUnitTypeIndex);
		model.changePreferencesSetOfUnits(currentUnitTypeIndex);

		ObservableList<String> unitNames = model.getNames(PREFERENCES_UNIT_NAMES);
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
						String errorTitle = resourceBundle.getString("errorTitle");
						String savingPreferencesErrorMessage = resourceBundle
								.getString("savingPreferencesErrorMessage");

						Message.showMessage(errorTitle, savingPreferencesErrorMessage, AlertType.ERROR);
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

		selectedIds.put(NUMBER_OF_DECIMAL_PLACES_ID,
				model.getNumbersOfDecimalPlaces(defaultNumberOfDecimalPlacesIndex).getNumberOfDecimalPlacesId());
		selectedIds.put(UNIT_TYPE_ID, model.getUnitType(defaultUnitTypeIndex).getUnitTypeId());
		selectedIds.put(FIRST_UNIT_ID, model.getPreferencesUnit(defaultFirstUnitIndex).getUnitId());
		selectedIds.put(SECOND_UNIT_ID, model.getPreferencesUnit(defaultSecondUnitIndex).getUnitId());
		selectedIds.put(APP_LANGUAGE_ID, model.getAppLanguages(defaultAppLanguageIndex).getAppLanguageId());
		selectedIds.put(UNITS_LANGUAGE_ID, model.getUnitsLanguages(defaultUnitsLanguageIndex).getUnitsLanguageId());
		selectedIds.put(APP_SKIN_ID, model.getAppSkins(defaultAppSkinIndex).getAppSkinId());
		selectedIds.put(CHECK_APP_UPDATE_ID, applicationUpdateCheckBox.isSelected() ? 1 : 0);
		selectedIds.put(UPDATE_EXCHANGE_RATES_ID, exchangeRatesUpdateCheckbox.isSelected() ? 1 : 0);
		selectedIds.put(LOG_HISTORY_ID, logHistoryCheckbox.isSelected() ? 1 : 0);

		return !Arrays.equals(currentIds.values().toArray(), selectedIds.values().toArray());
	}

	private Preferences getNewValues()
	{
		return new Preferences(currentPreferences.getPreferencesId(), selectedIds.get(NUMBER_OF_DECIMAL_PLACES_ID),
				selectedIds.get(UNIT_TYPE_ID), selectedIds.get(FIRST_UNIT_ID), selectedIds.get(SECOND_UNIT_ID),
				selectedIds.get(APP_LANGUAGE_ID), selectedIds.get(UNITS_LANGUAGE_ID), selectedIds.get(APP_SKIN_ID),
				selectedIds.get(UPDATE_EXCHANGE_RATES_ID) == 1 ? true : false,
				selectedIds.get(CHECK_APP_UPDATE_ID) == 1 ? true : false,
				selectedIds.get(LOG_HISTORY_ID) == 1 ? true : false);
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
			model.setUnit(defaultFirstUnitIndex, DEFAULT_FIRST_UNIT);
		}

		if (defaultSecondUnitWasChanged())
		{
			model.setUnit(defaultSecondUnitIndex, DEFAULT_SECOND_UNIT);
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
		return currentIds.get(UNIT_TYPE_ID) != selectedIds.get(UNIT_TYPE_ID);
	}

	private boolean defaultFirstUnitWasChanged()
	{
		return currentIds.get(FIRST_UNIT_ID) != selectedIds.get(FIRST_UNIT_ID);
	}

	private boolean defaultSecondUnitWasChanged()
	{
		return currentIds.get(SECOND_UNIT_ID) != selectedIds.get(SECOND_UNIT_ID);
	}

	private Boolean numberOfDecimalPlacesWasChanged()
	{
		return currentIds.get(NUMBER_OF_DECIMAL_PLACES_ID) != selectedIds.get(NUMBER_OF_DECIMAL_PLACES_ID);
	}

	private Boolean defaultAppLanguageWasChanged()
	{
		return currentIds.get(APP_LANGUAGE_ID) != selectedIds.get(APP_LANGUAGE_ID);
	}

	private Boolean defaultUnitsLanguageWasChanged()
	{
		return currentIds.get(UNITS_LANGUAGE_ID) != selectedIds.get(UNITS_LANGUAGE_ID);
	}

	private Boolean defaultAppSkinWasChanged()
	{
		return currentIds.get(APP_SKIN_ID) != selectedIds.get(APP_SKIN_ID);
	}
}