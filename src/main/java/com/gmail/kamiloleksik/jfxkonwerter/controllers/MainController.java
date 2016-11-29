package com.gmail.kamiloleksik.jfxkonwerter.controllers;

import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.NamesKey.*;
import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.UnitKey.*;
import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.UnitTypeKey.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import com.gmail.kamiloleksik.jfxkonwerter.model.Model;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberBaseException;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberFormatException;
import com.gmail.kamiloleksik.jfxkonwerter.util.Message;
import com.gmail.kamiloleksik.jfxkonwerter.util.NumberTextField;

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
import javafx.scene.control.MenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController implements Initializable
{
	public static final Pattern NUMBER_WITH_TWO_DIGIT_EXPONENT = Pattern.compile("^.+(e|E)(-|\\+)?[0-9]{2}$");
	public static final Pattern NUMBER_WITH_ONE_DIGIT = Pattern.compile("-?[0-9]?|(-0\\.)");

	private static Stage stage;
	private Model model;
	private HostServices hostServices;
	private ResourceBundle resourceBundle;
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private String userInput;
	private String resultInFixedNotation;

	public static BooleanProperty numberOfDecimalPlacesWasChanged = new SimpleBooleanProperty();
	public static BooleanProperty defaultAppLanguageWasChanged = new SimpleBooleanProperty();
	public static BooleanProperty defaultUnitsLanguageWasChanged = new SimpleBooleanProperty();
	public static BooleanProperty defaultSkinNameWasChanged = new SimpleBooleanProperty();

	@FXML
	private TextField resultTextField;

	@FXML
	private NumberTextField valueTextField;

	@FXML
	private AnchorPane appInfoAnchorPane, updateInfoAnchorPane;

	@FXML
	private ComboBox<String> unitTypeComboBox, firstUnitComboBox, secondUnitComboBox;

	@FXML
	private MenuItem resultFormattingMenuItem;

	private EventHandler<ActionEvent> firstUnitComboBoxHandler, secondUnitComboBoxHandler;

	private final Tooltip valueTextFieldTooltip = new Tooltip();
	private final Tooltip resultTextFieldTooltip = new Tooltip();

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.resourceBundle = resourceBundle;
		initializeModel(resourceBundle.getObject("model"));

		ObservableList<String> unitTypeNames = model.getNames(ALL_UNIT_TYPE_NAMES);
		String currentUnitTypeName = model.getUnitTypeName(CURRENT_UNIT_TYPE);
		SingleSelectionModel<String> unitTypeSel = unitTypeComboBox.getSelectionModel();

		unitTypeComboBox.setItems(unitTypeNames);
		unitTypeSel.select(currentUnitTypeName);

		ObservableList<String> unitNames = model.getNames(MAIN_WINDOW_UNIT_NAMES);
		SingleSelectionModel<String> firstUnitSel = firstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();
		String currentFirstUnitName = model.getUnitDisplayName(CURRENT_FIRST_UNIT);
		String currentSecondUnitName = model.getUnitDisplayName(CURRENT_SECOND_UNIT);
		boolean lettersAreAllowed = model.currentUnitsAreNumberBases();

		valueTextField.setLettersAreAllowed(lettersAreAllowed);
		firstUnitComboBox.setItems(unitNames);
		secondUnitComboBox.setItems(unitNames);
		firstUnitSel.select(currentFirstUnitName);
		secondUnitSel.select(currentSecondUnitName);
		addListenerToValueTextField();

		valueTextField.setText("0");
		valueTextFieldTooltip.setText("0");
		valueTextField.setTooltip(valueTextFieldTooltip);
		resultTextField.setTooltip(resultTextFieldTooltip);

		addEventHandlersToComboBoxes();
		addListenersToBooleanProperties();

		setAppSkin();
	}

	private void initializeModel(Object obj)
	{
		try
		{
			model = (Model) obj;
			model.initializeRamDataStructures();
		}
		catch (Exception e)
		{
			showCriticalErrorMessageAndExitApp();
		}
	}

	private void showCriticalErrorMessageAndExitApp()
	{
		Message.showMessage(Message.ERROR_TITLE, Message.CRITICAL_ERROR_MESSAGE);

		Platform.exit();
		System.exit(-1);
	}

	private void addListenerToValueTextField()
	{
		valueTextField.textProperty()
				.addListener((ObservableValue<? extends String> observable, String oldText, String newText) ->
				{
					userInput = valueTextField.getText();
					int userInputLength = userInput.length();

					if (userInputLength > 1000)
					{
						showNumberIsTooLongErrorMessage(userInputLength);
					}
					else
					{
						if (userInputLength == 0)
						{
							valueTextFieldTooltip.setText("pusty");
						}
						else
						{
							valueTextFieldTooltip.setText(userInput);
						}

						getAndSetResult();
					}
				});
	}

	private void showNumberIsTooLongErrorMessage(int userInputLength)
	{
		Message.showMessage(Message.ERROR_TITLE, Message.NUMBER_TOO_LONG_ERROR_MESSAGE + userInputLength + ").");
	}

	private void addListenersToBooleanProperties()
	{
		numberOfDecimalPlacesWasChanged
				.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
				{
					if (newValue == true)
					{
						getAndSetResult();
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

			model.setUnit(firstUnitSelectedIndex, CURRENT_FIRST_UNIT);

			getAndSetResult();
		};

		secondUnitComboBoxHandler = (ActionEvent event) ->
		{
			SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();
			int secondUnitSelectedIndex = secondUnitSel.getSelectedIndex();

			model.setUnit(secondUnitSelectedIndex, CURRENT_SECOND_UNIT);

			getAndSetResult();
		};

		firstUnitComboBox.addEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.addEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);
	}

	public void processDigit(ActionEvent event)
	{
		Object eventSource = event.getSource();
		Button eventSourceButton = (Button) eventSource;
		String clickedButtonValue = eventSourceButton.getText();

		if (currentUserInputIsEqualZero())
		{
			valueTextField.setText(clickedButtonValue);
		}
		else if (userInputCanBeLonger())
		{
			valueTextField.setText(userInput + clickedButtonValue);
		}
	}

	private boolean userInputCanBeLonger()
	{
		return !NUMBER_WITH_TWO_DIGIT_EXPONENT.matcher(userInput).matches();
	}

	private boolean currentUserInputIsEqualZero()
	{
		return userInput.equals("0");
	}

	public void processDecimalMark(ActionEvent event)
	{
		if (currentUserInputDoesNotContainE() && currentUserInputDoesNotContainDecimalMark()
				&& currentUserInputIsNotEmpty())
		{
			valueTextField.setText(userInput + ".");
		}
	}

	private boolean currentUserInputDoesNotContainE()
	{
		return !userInput.contains("e") && !userInput.contains("E");
	}

	private boolean currentUserInputDoesNotContainDecimalMark()
	{
		return !userInput.contains(".");
	}

	public void processSign(ActionEvent event)
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

	public void processDeletionKey(ActionEvent event)
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
	}

	private boolean currentUserInputCanBeShortened()
	{
		return !NUMBER_WITH_ONE_DIGIT.matcher(userInput).matches();
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

		model.setUnit(secondUnitIndex, CURRENT_FIRST_UNIT);
		model.setUnit(firstUnitIndex, CURRENT_SECOND_UNIT);

		firstUnitComboBox.addEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.addEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		getAndSetResult();
	}

	public void runUpdateThread(ActionEvent event)
	{
		if (updateIsPerforming() || appInfoIsShowing())
		{
			return;
		}

		updateInfoAnchorPane.setVisible(true);

		executor.execute(() ->
		{
			boolean taskSucceeded = model.updateExchangeRates();
			Platform.runLater(() ->
			{
				if (taskSucceeded)
				{
					model.updateExchangeRatesInRam();
					Message.showMessage(Message.INFORMATION_TITLE, Message.UPDATE_SUCCESS_MESSAGE);

					getAndSetResult();
				}
				else
				{
					Message.showMessage(Message.ERROR_TITLE, Message.UPDATE_ERROR_MESSAGE);
				}

				updateInfoAnchorPane.setVisible(false);
			});
		});
	}

	public void showPreferences(ActionEvent event) throws IOException
	{
		if (updateIsPerforming() || appInfoIsShowing())
		{
			return;
		}

		stage = new Stage();
		stage.setResizable(false);
		stage.sizeToScene();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/Preferences.fxml"), resourceBundle);
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
		stage.getIcons().add(new Image(MainController.class.getResourceAsStream("/images/icon.png")));
		stage.setScene(scene);
		stage.setTitle("Preferencje");
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(appInfoAnchorPane.getScene().getWindow());
		stage.setOnCloseRequest(e ->
		{
			int defaultUnitTypeIndex = model.getDefaultUnitTypeIndex();

			if (defaultUnitTypeIndex != model.getPreferencesUnitTypeIndex())
			{
				model.setPreferencesUnitTypeIndex(defaultUnitTypeIndex);
				model.changePreferencesSetOfUnits(defaultUnitTypeIndex);
			}
		});
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

		ObservableList<String> unitNames = model.getNames(MAIN_WINDOW_UNIT_NAMES);
		SingleSelectionModel<String> firstUnitSel = firstUnitComboBox.getSelectionModel();
		SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();

		firstUnitComboBox.setItems(unitNames);
		firstUnitSel.select(0);
		model.setUnit(0, CURRENT_FIRST_UNIT);

		secondUnitComboBox.setItems(unitNames);
		secondUnitSel.select(0);
		model.setUnit(0, CURRENT_SECOND_UNIT);

		firstUnitComboBox.addEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.addEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

		boolean lettersAreAllowed = model.currentUnitsAreNumberBases();

		valueTextField.setLettersAreAllowed(lettersAreAllowed);
		getAndSetResult();
	}

	public void getAndSetResult()
	{
		String result = getResult();

		resultTextField.setText(result);
		resultTextFieldTooltip.setText(result);
		resultFormattingMenuItem.setText("Poka¿ wynik w notacji naukowej");
		resultInFixedNotation = null;

		if (model.currentUnitsAreNumberBases())
		{
			resultFormattingMenuItem.setDisable(true);
		}
		else
		{
			resultFormattingMenuItem.setDisable(false);
		}
	}

	private String getResult()
	{
		String result;

		try
		{
			result = model.convertValue(userInput);
		}
		catch (InvalidNumberFormatException e)
		{
			result = Message.INVALID_NUMBER_FORMAT_MESSAGE;
		}
		catch (InvalidNumberBaseException e)
		{
			int numberBase = e.getInvalidNumberBase();

			if (numberBase == 0)
			{
				result = Message.NUMBER_BASE_CONVERSION_ERROR_MESSAGE;
			}
			else
			{
				result = Message.INVALID_NUMBER_BASE_MESSAGE + numberBase + ".";
			}
		}

		return result;
	}

	public void changeResultFormatting(ActionEvent event)
	{
		if (model.currentUnitsAreNumberBases())
		{
			return;
		}

		if (resultIsInScientificNotation())
		{
			resultTextFieldTooltip.setText(resultInFixedNotation);
			resultTextField.setText(resultInFixedNotation);
			resultFormattingMenuItem.setText("Poka¿ wynik w notacji naukowej");

			resultInFixedNotation = null;
		}
		else
		{
			String result = resultTextField.getText();
			resultInFixedNotation = result;
			BigDecimal bdResult = getResultInBigDecimal(result);
			if (bdResult == null)
			{
				return;
			}
			String resultInScientificNotation = getInScientificNotation(bdResult);

			resultTextFieldTooltip.setText(resultInScientificNotation);
			resultTextField.setText(resultInScientificNotation);
			resultFormattingMenuItem.setText("Wróæ do notacji sta³opozycyjnej");
		}
	}

	private boolean resultIsInScientificNotation()
	{
		return resultInFixedNotation != null;
	}

	private BigDecimal getResultInBigDecimal(String result)
	{
		BigDecimal bdResult = null;

		try
		{
			bdResult = new BigDecimal(result);
		}
		catch (NumberFormatException e)
		{
		}

		return bdResult;
	}

	private static String getInScientificNotation(BigDecimal value)
	{
		NumberFormat nF = NumberFormat.getNumberInstance(Locale.ROOT);
		DecimalFormat formatter = (DecimalFormat) nF;

		formatter.applyPattern("0.0E0");
		formatter.setRoundingMode(RoundingMode.HALF_UP);
		formatter.setMinimumFractionDigits((value.scale() > 0) ? value.precision() : value.scale());

		return formatter.format(value);
	}

	public void closeApp(ActionEvent event)
	{
		if (canBeShutdown())
		{
			shutdownExecutor();
			Platform.exit();
		}
		else
		{
			event.consume();
		}
	}

	public void showAppInfo(ActionEvent event)
	{
		if (updateIsPerforming())
		{
			return;
		}

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
		String appSkinPath = model.getAppSkinPath();

		Application.setUserAgentStylesheet(appSkinPath);
	}

	private boolean updateIsPerforming()
	{
		return updateInfoAnchorPane.isVisible();
	}

	private boolean appInfoIsShowing()
	{
		return appInfoAnchorPane.isVisible();
	}

	public boolean canBeShutdown()
	{
		return !updateIsPerforming();
	}

	public void shutdownExecutor()
	{
		executor.shutdown();
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