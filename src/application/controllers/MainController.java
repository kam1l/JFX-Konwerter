package application.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import application.service.Message;
import application.service.Model;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController implements Initializable
{
	private Model model = new Model();
	private Message message = new Message();
	private static Stage stage;
	private HostServices hostServices;

	private String userInput;
	public static BooleanProperty numberOfDecimalPlacesWasChanged = new SimpleBooleanProperty();
	public static BooleanProperty defaultSkinNameWasChanged = new SimpleBooleanProperty();

	@FXML
	private TextField valueTextField, resultTextField;

	@FXML
	private AnchorPane appInfoAnchorPane, updateInfoAnchorPane;

	@FXML
	private ComboBox<String> unitTypeComboBox, firstUnitComboBox, secondUnitComboBox;

	private EventHandler<ActionEvent> firstUnitComboBoxHandler, secondUnitComboBoxHandler;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		model.initializeRamDataStructures();
		setAppSkin();

		ObservableList<String> unitTypeNames = model.getNames("allUnitTypeNames");
		String currentUnitTypeName = model.getUnitTypeName("currentUnitType");
		SingleSelectionModel<String> unitTypeSel = unitTypeComboBox.getSelectionModel();

		unitTypeComboBox.setItems(unitTypeNames);
		unitTypeSel.select(currentUnitTypeName);

		ObservableList<String> unitNames = model.getNames("mainWindowUnitNames");
		SingleSelectionModel<String> firstUnitSel = firstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();
		String currentFirstUnitName = model.getUnitDisplayName("currentFirstUnit");
		String currentSecondUnitName = model.getUnitDisplayName("currentSecondUnit");

		firstUnitComboBox.setItems(unitNames);
		secondUnitComboBox.setItems(unitNames);
		firstUnitSel.select(currentFirstUnitName);
		secondUnitSel.select(currentSecondUnitName);
		addListenerToValueTextField();

		valueTextField.setText("0");
		setResult();

		addEventHandlersToComboBoxes();
		addListenersToBooleanProperties();
	}

	private void addListenerToValueTextField()
	{
		valueTextField.textProperty()
				.addListener((ObservableValue<? extends String> observable, String oldText, String newText) ->
				{
					userInput = valueTextField.getText();
				});
	}

	private void addListenersToBooleanProperties()
	{
		numberOfDecimalPlacesWasChanged
				.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
				{
					if (newValue == true)
					{
						setResult();
						numberOfDecimalPlacesWasChanged.set(false);
					}
				});

		defaultSkinNameWasChanged
				.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
				{
					if (newValue == true)
					{
						setAppSkin();
						defaultSkinNameWasChanged.set(false);
					}
				});
	}

	private void addEventHandlersToComboBoxes()
	{
		firstUnitComboBoxHandler = (ActionEvent event) ->
		{
			SingleSelectionModel<String> firstUnitSel = firstUnitComboBox.getSelectionModel();
			int firstUnitSelectedIndex = firstUnitSel.getSelectedIndex();

			model.setUnit(firstUnitSelectedIndex, "currentFirstUnit");

			setResult();
		};

		secondUnitComboBoxHandler = (ActionEvent event) ->
		{
			SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();
			int secondUnitSelectedIndex = secondUnitSel.getSelectedIndex();

			model.setUnit(secondUnitSelectedIndex, "currentSecondUnit");

			setResult();
		};

		firstUnitComboBox.addEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.addEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);
	}

	public void processDigitAndSetResult(ActionEvent event)
	{
		Object eventSource = event.getSource();
		Button eventSourceButton = (Button) eventSource;
		String clickedButtonValue = eventSourceButton.getText();

		if (currentUserInputIsEqualZero())
		{
			valueTextField.setText(clickedButtonValue);
		}
		else
		{
			valueTextField.setText(userInput + clickedButtonValue);
		}

		setResult();
	}

	private boolean currentUserInputIsEqualZero()
	{
		return userInput.equals("0");
	}

	public void processDecimalMark(ActionEvent event)
	{
		if (currentUserInputDoesNotContainDecimalMark() && currentUserInputIsNotEmpty())
		{
			valueTextField.setText(userInput + ".");
		}
	}

	private boolean currentUserInputDoesNotContainDecimalMark()
	{
		return !userInput.contains(".");
	}

	public void processSignAndSetResult(ActionEvent event)
	{
		if (currentUserInputIsNotEmpty())
		{
			if (currentUserInputHasSign())
			{
				valueTextField.setText(userInput.substring(1));
			}
			else if (currentUserInputHasValidNegativeAdditiveInverse())
			{
				valueTextField.setText("-" + userInput);
			}

			setResult();
		}
	}

	private boolean currentUserInputIsNotEmpty()
	{
		return userInput.length() > 0;
	}

	private boolean currentUserInputHasSign()
	{
		return userInput.charAt(0) == '-';
	}

	private boolean currentUserInputHasValidNegativeAdditiveInverse()
	{
		return userInput.length() > 1 || userInput.charAt(0) != '0';
	}

	public void processDeletionKeyAndSetResult(ActionEvent event)
	{
		Object eventSource = event.getSource();
		Button eventSourceButton = (Button) eventSource;
		String clickedButtonValue = eventSourceButton.getText();

		if (clickedButtonValue.equals("C"))
		{
			valueTextField.setText("0");
		}
		else
		{
			if (currentUserInputCanBeShortened())
			{
				valueTextField.setText(userInput.substring(0, userInput.length() - 1));
			}
			else
			{
				valueTextField.setText("0");
			}
		}

		setResult();
	}

	private boolean currentUserInputCanBeShortened()
	{
		return !userInput.matches("-?[0-9]?|(-0\\.)");
	}

	public void swapUnits(ActionEvent event)
	{
		firstUnitComboBox.removeEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.removeEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		SingleSelectionModel<String> firstUnitSel = firstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();
		int firstUnitIndex = firstUnitSel.getSelectedIndex();
		int secondUnitIndex = secondUnitSel.getSelectedIndex();

		firstUnitSel.select(secondUnitIndex);
		secondUnitSel.select(firstUnitIndex);

		model.setUnit(secondUnitIndex, "currentFirstUnit");
		model.setUnit(firstUnitIndex, "currentSecondUnit");

		firstUnitComboBox.addEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.addEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		setResult();
	}

	public void runUpdateThread(ActionEvent event)
	{
		updateInfoAnchorPane.setVisible(true);

		new Thread(() ->
		{
			boolean taskSucceeded = model.updateExchangeRates();
			Platform.runLater(() ->
			{
				if (taskSucceeded)
				{
					model.updateExchangeRatesInRam();
					message.showMessage(Message.INFORMATION_TITLE, Message.UPDATE_SUCCESS_MESSAGE);

					setResult();
				}
				else
				{
					message.showMessage(Message.ERROR_TITLE, Message.UPDATE_ERROR_MESSAGE);
				}

				updateInfoAnchorPane.setVisible(false);
			});
		}).start();
	}

	public void showPreferences(ActionEvent event) throws IOException
	{
		stage = new Stage();
		stage.setMaxHeight(368);
		stage.setResizable(false);
		Parent root = FXMLLoader.load(getClass().getResource("/application/resources/view/Preferences.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets()
				.add(getClass().getResource("/application/resources/css/application.css").toExternalForm());
		stage.getIcons()
				.add(new Image(MainController.class.getResourceAsStream("/application/resources/images/icon.png")));
		stage.setScene(scene);
		stage.setTitle("Preferencje");
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(appInfoAnchorPane.getScene().getWindow());
		stage.show();
	}

	public void changeUnitSet(ActionEvent event)
	{
		firstUnitComboBox.removeEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.removeEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		firstUnitComboBox.getItems().clear();
		secondUnitComboBox.getItems().clear();

		SingleSelectionModel<String> unitTypeSel = unitTypeComboBox.getSelectionModel();
		int unitTypeIndex = unitTypeSel.getSelectedIndex();

		model.changeMainWindowSetOfUnits(unitTypeIndex);

		firstUnitComboBox.removeEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.removeEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		ObservableList<String> unitNames = model.getNames("mainWindowUnitNames");
		SingleSelectionModel<String> firstUnitSel = firstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();

		firstUnitComboBox.setItems(unitNames);
		firstUnitSel.select(0);
		model.setUnit(0, "currentFirstUnit");

		secondUnitComboBox.setItems(unitNames);
		secondUnitSel.select(0);
		model.setUnit(0, "currentSecondUnit");

		firstUnitComboBox.addEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.addEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		setResult();
	}

	public void setResult()
	{
		resultTextField.setText(model.convertValue(userInput));
	}

	public void closeApp(ActionEvent event)
	{
		Platform.exit();
		System.exit(0);
	}

	public void showAppInfo(ActionEvent event)
	{
		appInfoAnchorPane.setVisible(true);
	}

	public void closeAppInfo(ActionEvent event)
	{
		appInfoAnchorPane.setVisible(false);
	}

	public void openEmailClient(ActionEvent event)
	{
		hostServices.showDocument("mailto:kamiloleksik@gmail.com");
	}

	public void openWebBrowser(ActionEvent event)
	{
		hostServices.showDocument("https://github.com/kam1l/JFX-Konwerter");
	}

	public void setAppSkin()
	{
		String skinName = model.getPreferences().getDefaultSkinName();

		if (skinName.equals("Modena"))
		{
			Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
		}
		else
		{
			Application.setUserAgentStylesheet("/application/resources/css/caspian.css");
		}
	}

	public static Stage getStage()
	{
		return stage;
	}

	public void setHostServices(HostServices hostServices)
	{
		this.hostServices = hostServices;
	}
}