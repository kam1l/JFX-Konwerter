package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Alert.AlertType;

public class PreferencesController implements Initializable
{
	private Model model = new Model();
	private Preferences newPreferences = new Preferences();

	@FXML
	private ComboBox<String> defaultNumberOfDecimalPlacesComboBox, defaultUnitTypeComboBox, defaultFirstUnitComboBox,
			defaultSecondUnitComboBox;

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
		if (whereChangesMade() == true)
		{
			Runnable updater = new Runnable()
			{
				@Override
				public void run()
				{

					getNewValues();
					boolean taskSucceeded = model.updatePreferencesInDB(newPreferences);

					Platform.runLater(() ->
					{
						if (taskSucceeded == false)
						{
							showSavingPreferencesErrorMessage();
						}
						else
						{
							updatePreferencesRamDataStructures();
						}
					});
				}
			};

			new Thread(updater).start();
		}

		MainController.getStage().close();
	}

	private boolean whereChangesMade()
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

		return !(selectedNumberOfDecPlaces.equals(currentNumberOfDecPlaces)
				&& selectedDefaultFirstUnitName.equals(currentDefaultFirstUnitName)
				&& selectedDefaultSecondUnitName.equals(currentDefaultSecondUnitName));
	}

	private void getNewValues()
	{
		SingleSelectionModel<String> defaultNumberOfDecimalPlacesSel = defaultNumberOfDecimalPlacesComboBox
				.getSelectionModel();
		SingleSelectionModel<String> defaultUnitTypeSel = defaultUnitTypeComboBox.getSelectionModel();
		SingleSelectionModel<String> defaultFirstUnitSel = defaultFirstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> defaultSecondUnitSel = defaultSecondUnitComboBox.getSelectionModel();

		int defaultNumberOfDecimalPlaces = Integer
				.valueOf(defaultNumberOfDecimalPlacesSel.getSelectedItem().toString());
		int defaultUnitTypeIndex = defaultUnitTypeSel.getSelectedIndex();
		int defaultFirstUnitIndex = defaultFirstUnitSel.getSelectedIndex();
		int defaultSecondUnitIndex = defaultSecondUnitSel.getSelectedIndex();

		newPreferences.setPreferencesId(Model.getPreferencesId());
		newPreferences.setNumberOfDecimalPlaces(defaultNumberOfDecimalPlaces);
		newPreferences.setDefaultUnitTypeId(Model.getUnitType(defaultUnitTypeIndex).getUnitTypeId());
		newPreferences.setDefaultFirstUnitId(Model.getPreferencesUnit(defaultFirstUnitIndex).getUnitId());
		newPreferences.setDefaultSecondUnitId(Model.getPreferencesUnit(defaultSecondUnitIndex).getUnitId());
	}

	private void updatePreferencesRamDataStructures()
	{
		SingleSelectionModel<String> defaultUnitTypeSel = defaultUnitTypeComboBox.getSelectionModel();
		SingleSelectionModel<String> defaultFirstUnitSel = defaultFirstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> defaultSecondUnitSel = defaultSecondUnitComboBox.getSelectionModel();

		int defaultUnitTypeIndex = defaultUnitTypeSel.getSelectedIndex();
		int defaultFirstUnitIndex = defaultFirstUnitSel.getSelectedIndex();
		int defaultSecondUnitIndex = defaultSecondUnitSel.getSelectedIndex();

		model.setDefaultUnitType(defaultUnitTypeIndex);
		model.setDefaultFirstUnit(defaultFirstUnitIndex);
		model.setDefaultSecondUnit(defaultSecondUnitIndex);
		model.setNumberOfDecimalPlaces(newPreferences.getNumberOfDecimalPlaces());

		MainController.preferencesSaved.setValue(!MainController.preferencesSaved.getValue());
	}

	private void showSavingPreferencesErrorMessage()
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(Message.ERROR_TITLE);
		alert.setHeaderText(null);
		alert.setContentText(Message.SAVING_PREFERENCES_ERROR_MESSAGE);

		alert.showAndWait();
	}
}