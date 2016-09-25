package application.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import application.dao.Preferences;
import application.service.Message;
import application.service.Model;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;

public class PreferencesController implements Initializable
{
	private Model model = new Model();
	private Message message = new Message();

	private boolean numberOfDecimalPlacesWasChanged, defaultSkinNameWasChanged;

	@FXML
	private ComboBox<String> defaultNumberOfDecimalPlacesComboBox, defaultUnitTypeComboBox, defaultFirstUnitComboBox,
			defaultSecondUnitComboBox, defaultSkinNameComboBox;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		int numberOfDecimalPlaces = Model.getNumberOfDecimalPlaces();
		SingleSelectionModel<String> defaultNumberOfDecimalPlacesSel = defaultNumberOfDecimalPlacesComboBox
				.getSelectionModel();

		defaultNumberOfDecimalPlacesSel.select(String.valueOf(numberOfDecimalPlaces));

		ObservableList<String> unitTypeNames = model.getAllUnitTypeNames();
		SingleSelectionModel<String> defaultUnitTypeSel = defaultUnitTypeComboBox.getSelectionModel();
		String defaultUnitTypeName = model.getDefaultUnitTypeName();

		defaultUnitTypeComboBox.setItems(unitTypeNames);
		defaultUnitTypeSel.select(defaultUnitTypeName);

		ObservableList<String> unitNames = model.getCurrentPreferencesSetOfUnitNames();
		SingleSelectionModel<String> defaultFirstUnitSel = defaultFirstUnitComboBox.getSelectionModel();
		String defaultFirstUnitName = Model.getDefaultFirstUnitDisplayName();

		defaultFirstUnitComboBox.setItems(unitNames);
		defaultFirstUnitSel.select(defaultFirstUnitName);

		SingleSelectionModel<String> defaultSecondUnitSel = defaultSecondUnitComboBox.getSelectionModel();
		String defaultSecondUnitName = Model.getDefaultSecondUnitDisplayName();

		defaultSecondUnitComboBox.setItems(unitNames);
		defaultSecondUnitSel.select(defaultSecondUnitName);

		SingleSelectionModel<String> defaultSkinNameSel = defaultSkinNameComboBox.getSelectionModel();
		String defaultSkinName = Model.getPreferences().getDefaultSkinName();

		defaultSkinNameSel.select(defaultSkinName);
	}

	public void changeUnitSet(ActionEvent event)
	{
		defaultFirstUnitComboBox.getItems().clear();
		defaultSecondUnitComboBox.getItems().clear();

		SingleSelectionModel<String> defaultUnitTypeSel = defaultUnitTypeComboBox.getSelectionModel();
		int currentUnitTypeIndex = defaultUnitTypeSel.getSelectedIndex();

		model.changeCurrentPreferencesSetOfUnits(currentUnitTypeIndex);

		ObservableList<String> unitNames = model.getCurrentPreferencesSetOfUnitNames();
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
			new Thread(() ->
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
			}).start();
		}

		MainController.getStage().close();
	}

	private boolean changesWereMade()
	{
		SingleSelectionModel<String> defaultNumberOfDecimalPlacesSel = defaultNumberOfDecimalPlacesComboBox
				.getSelectionModel();
		String currentNumberOfDecPlaces = String.valueOf(Model.getNumberOfDecimalPlaces());
		String selectedNumberOfDecPlaces = defaultNumberOfDecimalPlacesSel.getSelectedItem().toString();

		SingleSelectionModel<String> defaultFirstUnitSel = defaultFirstUnitComboBox.getSelectionModel();
		String currentDefaultFirstUnitName = Model.getDefaultFirstUnitDisplayName();
		String selectedDefaultFirstUnitName = defaultFirstUnitSel.getSelectedItem().toString();

		SingleSelectionModel<String> defaultSecondUnitSel = defaultSecondUnitComboBox.getSelectionModel();
		String currentDefaultSecondUnitName = Model.getDefaultFirstUnitDisplayName();
		String selectedDefaultSecondUnitName = defaultSecondUnitSel.getSelectedItem().toString();

		SingleSelectionModel<String> defaultSkinNameSel = defaultSkinNameComboBox.getSelectionModel();
		String currentDefaultSkinName = Model.getPreferences().getDefaultSkinName();
		String selectedDefaultSkinName = defaultSkinNameSel.getSelectedItem().toString();

		numberOfDecimalPlacesWasChanged = !selectedNumberOfDecPlaces.equals(currentNumberOfDecPlaces);
		defaultSkinNameWasChanged = !selectedDefaultSkinName.equals(currentDefaultSkinName);

		return !(numberOfDecimalPlacesWasChanged && selectedDefaultFirstUnitName.equals(currentDefaultFirstUnitName)
				&& selectedDefaultSecondUnitName.equals(currentDefaultSecondUnitName) && defaultSkinNameWasChanged);
	}

	private Preferences getNewValues()
	{
		SingleSelectionModel<String> defaultNumberOfDecimalPlacesSel = defaultNumberOfDecimalPlacesComboBox
				.getSelectionModel();
		SingleSelectionModel<String> defaultUnitTypeSel = defaultUnitTypeComboBox.getSelectionModel();
		SingleSelectionModel<String> defaultFirstUnitSel = defaultFirstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> defaultSecondUnitSel = defaultSecondUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> defaultSkinNameSel = defaultSkinNameComboBox.getSelectionModel();

		int defaultNumberOfDecimalPlaces = Integer
				.valueOf(defaultNumberOfDecimalPlacesSel.getSelectedItem().toString());
		int defaultUnitTypeIndex = defaultUnitTypeSel.getSelectedIndex();
		int defaultFirstUnitIndex = defaultFirstUnitSel.getSelectedIndex();
		int defaultSecondUnitIndex = defaultSecondUnitSel.getSelectedIndex();
		String defaultSkinName = defaultSkinNameSel.getSelectedItem().toString();

		int preferencesId = Model.getPreferencesId();
		int defaultUnitTypeId = Model.getUnitType(defaultUnitTypeIndex).getUnitTypeId();
		int defaultFirstUnitId = Model.getPreferencesUnit(defaultFirstUnitIndex).getUnitId();
		int defaultSecondUnitId = Model.getPreferencesUnit(defaultSecondUnitIndex).getUnitId();

		Preferences prefs = new Preferences(preferencesId, defaultNumberOfDecimalPlaces, defaultUnitTypeId,
				defaultFirstUnitId, defaultSecondUnitId, defaultSkinName);

		return prefs;
	}

	private void updatePreferencesRamDataStructures(Preferences prefs)
	{
		Model.setPreferences(prefs);

		SingleSelectionModel<String> defaultUnitTypeSel = defaultUnitTypeComboBox.getSelectionModel();
		SingleSelectionModel<String> defaultFirstUnitSel = defaultFirstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> defaultSecondUnitSel = defaultSecondUnitComboBox.getSelectionModel();

		int defaultUnitTypeIndex = defaultUnitTypeSel.getSelectedIndex();
		int defaultFirstUnitIndex = defaultFirstUnitSel.getSelectedIndex();
		int defaultSecondUnitIndex = defaultSecondUnitSel.getSelectedIndex();

		model.setDefaultUnitType(defaultUnitTypeIndex);
		model.setDefaultFirstUnit(defaultFirstUnitIndex);
		model.setDefaultSecondUnit(defaultSecondUnitIndex);

		MainController.numberOfDecimalPlacesWasChanged.setValue(numberOfDecimalPlacesWasChanged);
		MainController.defaultSkinNameWasChanged.setValue(defaultSkinNameWasChanged);
	}
}