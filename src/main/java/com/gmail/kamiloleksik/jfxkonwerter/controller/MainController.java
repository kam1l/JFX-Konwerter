package com.gmail.kamiloleksik.jfxkonwerter.controller;

import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.NamesKey.*;
import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.UnitKey.*;
import static com.gmail.kamiloleksik.jfxkonwerter.util.keys.UnitTypeKey.*;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gmail.kamiloleksik.jfxkonwerter.Main;
import com.gmail.kamiloleksik.jfxkonwerter.model.Model;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberBaseException;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberFormatException;
import com.gmail.kamiloleksik.jfxkonwerter.model.entity.Preferences;
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
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController implements Initializable
{
	public static final Pattern NUMBER_WITH_TWO_DIGIT_EXPONENT = Pattern.compile("^.+(e|E)(-|\\+)?[0-9]{2}$");
	public static final Pattern NUMBER_WITH_ONE_DIGIT = Pattern.compile("-?[0-9]?|(-0\\.)");
	public static final DefaultArtifactVersion APP_VERSION = new DefaultArtifactVersion("1.1.0");

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
	private MenuItem resultFormattingMenuItem, menuItemClose, menuItemSwapUnits, menuItemPreferences, menuItemUpdate,
			menuItemAbout, menuItemCheckUpdate, menuItemImportPreferences, menuItemExportPreferences, menuItemQuickCopy,
			menuItemQuickPaste, menuItemIncreaseNumOfDecPlaces, menuItemDecreaseNumOfDecPlaces;

	@FXML
	private Menu menuEdit, menuFile;

	@FXML
	private Label labelType, labelFirstUnit, labelInputValue, labelSecondUnit, labelResult, labelApplicationVersion,
			labelOngoingUpdate, labelSourceCode, labelContact, labelCopyrightInfo;

	@FXML
	private Button buttonCloseInfo;

	private EventHandler<ActionEvent> firstUnitComboBoxHandler, secondUnitComboBoxHandler, unitTypeComboBoxHandler;

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
		labelCopyrightInfo.setText("Copyright © " + new SimpleDateFormat("yyyy").format(new Date()) + " Kamil Oleksik");

		if (model.getPreferences().getCheckForApplicationUpdatesOnStartup() == true)
		{
			checkUpdatesInTheBackground();
		}

		if (model.getPreferences().getUpdateExchangeRatesOnStartup() == true)
		{
			updateExchangeRatesInTheBackground();
		}
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
		String errorTitle = resourceBundle.getString("errorTitle");
		String criticalErrorMessage = resourceBundle.getString("criticalErrorMessage");

		Message.showMessage(errorTitle, criticalErrorMessage, AlertType.ERROR);
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
							valueTextFieldTooltip.setText(resourceBundle.getString("emptyField"));
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
		String errorTitle = resourceBundle.getString("errorTitle");
		String numberTooLongErrorMessage = resourceBundle.getString("numberTooLongErrorMessage");

		Message.showMessage(errorTitle, numberTooLongErrorMessage + userInputLength + ").", AlertType.ERROR);
	}

	private void addListenersToBooleanProperties()
	{
		addListenerToNumOfDecPlacesBoolProperty();
		addListenerToSkinNameBoolProperty();
		addListenerToAppLangBoolProperty();
		addListenerToUnitsLangBoolProperty();
	}

	private void addListenerToUnitsLangBoolProperty()
	{
		defaultUnitsLanguageWasChanged
				.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
				{
					if (newValue == true)
					{
						unitTypeComboBox.removeEventHandler(ActionEvent.ACTION, unitTypeComboBoxHandler);
						firstUnitComboBox.removeEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
						secondUnitComboBox.removeEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);

						model.updateUnitsLanguage();
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

						firstUnitComboBox.setItems(unitNames);
						secondUnitComboBox.setItems(unitNames);
						firstUnitSel.select(currentFirstUnitName);
						secondUnitSel.select(currentSecondUnitName);

						unitTypeComboBox.addEventHandler(ActionEvent.ACTION, unitTypeComboBoxHandler);
						firstUnitComboBox.addEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
						secondUnitComboBox.addEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);
						defaultUnitsLanguageWasChanged.set(false);
					}
				});
	}

	private void addListenerToAppLangBoolProperty()
	{
		defaultAppLanguageWasChanged
				.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
				{
					if (newValue == true)
					{
						resourceBundle = Main.getBundle(model);
						menuFile.setText(resourceBundle.getString("menuFile"));
						menuItemImportPreferences.setText(resourceBundle.getString("menuItemImportPreferences"));
						menuItemExportPreferences.setText(resourceBundle.getString("menuItemExportPreferences"));
						menuEdit.setText(resourceBundle.getString("menuEdit"));
						menuItemClose.setText(resourceBundle.getString("menuItemClose"));
						menuItemSwapUnits.setText(resourceBundle.getString("menuItemSwapUnits"));
						menuItemQuickCopy.setText(resourceBundle.getString("menuItemQuickCopy"));
						menuItemQuickPaste.setText(resourceBundle.getString("menuItemQuickPaste"));
						menuItemIncreaseNumOfDecPlaces
								.setText(resourceBundle.getString("menuItemIncreaseNumOfDecPlaces"));
						menuItemDecreaseNumOfDecPlaces
								.setText(resourceBundle.getString("menuItemDecreaseNumOfDecPlaces"));
						menuItemPreferences.setText(resourceBundle.getString("menuItemPreferences"));
						menuItemUpdate.setText(resourceBundle.getString("menuItemUpdate"));
						menuItemCheckUpdate.setText(resourceBundle.getString("menuItemCheckUpdate"));
						menuItemAbout.setText(resourceBundle.getString("menuItemAbout"));
						labelType.setText(resourceBundle.getString("labelType"));
						labelFirstUnit.setText(resourceBundle.getString("labelFirstUnit"));
						labelInputValue.setText(resourceBundle.getString("labelInputValue"));
						labelSecondUnit.setText(resourceBundle.getString("labelSecondUnit"));
						labelResult.setText(resourceBundle.getString("labelResult"));
						labelApplicationVersion.setText(resourceBundle.getString("labelApplicationVersion"));
						labelContact.setText(resourceBundle.getString("labelContact"));
						labelSourceCode.setText(resourceBundle.getString("labelSourceCode"));
						labelOngoingUpdate.setText(resourceBundle.getString("labelOngoingUpdate"));
						buttonCloseInfo.setText(resourceBundle.getString("buttonCloseInfo"));

						if (resultInFixedNotation == null)
						{
							resultFormattingMenuItem.setText(resourceBundle.getString("resultFormattingMenuItem"));
						}
						else
						{
							resultFormattingMenuItem
									.setText(resourceBundle.getString("resultFormattingMenuItemFixedNotation"));
						}

						defaultAppLanguageWasChanged.set(false);
					}
				});
	}

	private void addListenerToSkinNameBoolProperty()
	{
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

	private void addListenerToNumOfDecPlacesBoolProperty()
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
	}

	private void addEventHandlersToComboBoxes()
	{
		createFirstUnitComboBoxHandler();
		createSecondUnitComboBoxHandler();
		createUnitTypeComboBoxHandler();

		firstUnitComboBox.addEventHandler(ActionEvent.ACTION, firstUnitComboBoxHandler);
		secondUnitComboBox.addEventHandler(ActionEvent.ACTION, secondUnitComboBoxHandler);
		unitTypeComboBox.addEventHandler(ActionEvent.ACTION, unitTypeComboBoxHandler);
	}

	private void createUnitTypeComboBoxHandler()
	{
		unitTypeComboBoxHandler = (ActionEvent event) ->
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
		};
	}

	private void createSecondUnitComboBoxHandler()
	{
		secondUnitComboBoxHandler = (ActionEvent event) ->
		{
			SingleSelectionModel<String> secondUnitSel = secondUnitComboBox.getSelectionModel();
			int secondUnitSelectedIndex = secondUnitSel.getSelectedIndex();

			model.setUnit(secondUnitSelectedIndex, CURRENT_SECOND_UNIT);

			getAndSetResult();
		};
	}

	private void createFirstUnitComboBoxHandler()
	{
		firstUnitComboBoxHandler = (ActionEvent event) ->
		{
			SingleSelectionModel<String> firstUnitSel = firstUnitComboBox.getSelectionModel();
			int firstUnitSelectedIndex = firstUnitSel.getSelectedIndex();

			model.setUnit(firstUnitSelectedIndex, CURRENT_FIRST_UNIT);

			getAndSetResult();
		};
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
					String informationTitle = resourceBundle.getString("informationTitle");
					String updateSuccessMessage = resourceBundle.getString("updateSuccessMessage");

					model.updateExchangeRatesInRam();
					Message.showMessage(informationTitle, updateSuccessMessage, AlertType.INFORMATION);

					if (model.getExchangeRatesAbbreviations()
							.contains(model.getUnit(CURRENT_FIRST_UNIT).getUnitAbbreviation()))
					{
						getAndSetResult();
					}
				}
				else
				{
					String errorTitle = resourceBundle.getString("errorTitle");
					String updateErrorMessage = resourceBundle.getString("updateErrorMessage");

					Message.showMessage(errorTitle, updateErrorMessage, AlertType.ERROR);
				}

				updateInfoAnchorPane.setVisible(false);
			});
		});
	}

	private void updateExchangeRatesInTheBackground()
	{
		executor.execute(() ->
		{
			boolean taskSucceeded = model.updateExchangeRates();
			Platform.runLater(() ->
			{
				if (taskSucceeded)
				{
					model.updateExchangeRatesInRam();

					if (model.getExchangeRatesAbbreviations()
							.contains(model.getUnit(CURRENT_FIRST_UNIT).getUnitAbbreviation()))
					{
						getAndSetResult();
					}
				}
				else
				{
					String errorTitle = resourceBundle.getString("errorTitle");
					String updateErrorMessage = resourceBundle.getString("updateErrorMessage");

					Message.showMessage(errorTitle, updateErrorMessage, AlertType.ERROR);
				}
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
		stage.setTitle(resourceBundle.getString("preferencesTitle"));
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

	public void getAndSetResult()
	{
		String result = getResult();

		resultTextField.setText(result);
		resultTextFieldTooltip.setText(result);
		resultFormattingMenuItem.setText(resourceBundle.getString("resultFormattingMenuItem"));
		resultInFixedNotation = null;

		if (model.currentUnitsAreNumberBases())
		{
			resultFormattingMenuItem.setDisable(true);
		}
		else
		{
			resultFormattingMenuItem.setDisable(false);
		}

		if (model.getPreferences().getLogHistory() == true)
		{
			try (BufferedWriter bw = new BufferedWriter(new FileWriter("history.log", true)))
			{
				bw.write(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + ", "
						+ model.getUnitTypeName(CURRENT_UNIT_TYPE) + ", " + model.getUnitDisplayName(CURRENT_FIRST_UNIT)
						+ " -> " + model.getUnitDisplayName(CURRENT_SECOND_UNIT) + ", " + valueTextField.getText()
						+ " -> " + result);
				bw.newLine();
			}
			catch (IOException e)
			{
				String errorTitle = resourceBundle.getString("errorTitle");
				String writingFileErrorMessage = resourceBundle.getString("writingFileErrorMessage");

				Message.showMessage(errorTitle, writingFileErrorMessage, AlertType.ERROR);
			}
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
			result = resourceBundle.getString("invalidNumberFormatMessage");
		}
		catch (InvalidNumberBaseException e)
		{
			int numberBase = e.getInvalidNumberBase();

			if (numberBase == 0)
			{
				result = resourceBundle.getString("numberBaseConversionErrorMessage");
			}
			else
			{
				result = resourceBundle.getString("invalidNumberBaseMessage") + numberBase + ".";
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
			resultFormattingMenuItem.setText(resourceBundle.getString("resultFormattingMenuItem"));
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
			resultFormattingMenuItem.setText(resourceBundle.getString("resultFormattingMenuItemFixedNotation"));
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

	public void importPreferences(ActionEvent event)
	{
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("Preferences Files", "*.dat"));
		File file = fc.showOpenDialog(null);

		if (file != null)
		{
			Preferences prefs = model.getPreferences();

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

				validateDataFromFile(prefs, preferencesId, numberOfDecimalPlacesId, unitTypeId, firstUnitId,
						secondUnitId, appSkinId, appLanguageId, unitsLanguageId);

				if (preferencesAreDifferent(prefs, numberOfDecimalPlacesId, unitTypeId, firstUnitId, secondUnitId,
						appSkinId, appLanguageId, unitsLanguageId, updateExchangeRatesOnStartup,
						checkForApplicationUpdatesOnStartup, logHistory))
				{
					executor.execute(() ->
					{
						Preferences newPrefs = new Preferences(preferencesId, numberOfDecimalPlacesId, unitTypeId,
								firstUnitId, secondUnitId, appLanguageId, unitsLanguageId, appSkinId,
								updateExchangeRatesOnStartup, checkForApplicationUpdatesOnStartup, logHistory);
						boolean taskSucceeded = model.updatePreferencesInDB(newPrefs);

						Platform.runLater(() ->
						{
							if (taskSucceeded)
							{
								updateModel(prefs, numberOfDecimalPlacesId, unitTypeId, firstUnitId, secondUnitId,
										appSkinId, appLanguageId, unitsLanguageId, newPrefs);
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
			}
			catch (IOException e)
			{
				String errorTitle = resourceBundle.getString("errorTitle");
				String readingFileErrorMessage = resourceBundle.getString("readingFileErrorMessage");

				Message.showMessage(errorTitle, readingFileErrorMessage, AlertType.ERROR);
			}
		}
	}

	private void updateModel(Preferences prefs, int numberOfDecimalPlacesId, int unitTypeId, int firstUnitId,
			int secondUnitId, int appSkinId, int appLanguageId, int unitsLanguageId, Preferences newPrefs)
	{
		model.setPreferences(newPrefs);

		if (unitTypeId != prefs.getUnitType().getUnitTypeId())
		{
			int unitTypeIndex = model.getUnitTypeIndex(unitTypeId);
			model.setPreferencesUnitTypeIndex(unitTypeIndex);
			model.changePreferencesSetOfUnits(unitTypeIndex);
			model.setDefaultUnitTypeIndex(unitTypeIndex);
			model.setDefaultUnitType(unitTypeIndex);
		}

		if (firstUnitId != prefs.getFirstUnit().getUnitId())
		{
			model.setUnit(model.getPreferencesUnitIndex(firstUnitId), DEFAULT_FIRST_UNIT);
		}

		if (secondUnitId != prefs.getSecondUnit().getUnitId())
		{
			model.setUnit(model.getPreferencesUnitIndex(secondUnitId), DEFAULT_SECOND_UNIT);
		}

		if (numberOfDecimalPlacesId != prefs.getNumberOfDecimalPlaces().getNumberOfDecimalPlacesId())
		{
			model.setNumberOfDecimalPlaces(model.getNumberOfDecimalPlacesIndex(numberOfDecimalPlacesId));
			MainController.numberOfDecimalPlacesWasChanged.setValue(true);
		}

		if (appLanguageId != prefs.getAppLanguage().getAppLanguageId())
		{
			model.setAppLanguage(model.getAppLanguageIndex(appLanguageId));
			MainController.defaultAppLanguageWasChanged.setValue(true);
		}

		if (unitsLanguageId != prefs.getUnitsLanguage().getUnitsLanguageId())
		{
			model.setUnitsLanguage(model.getUnitsLanguageIndex(unitsLanguageId));
			MainController.defaultUnitsLanguageWasChanged.setValue(true);
		}

		if (appSkinId != prefs.getAppSkin().getAppSkinId())
		{
			model.setAppSkin(model.getAppSkinIndex(appSkinId));
			MainController.defaultSkinNameWasChanged.setValue(true);
		}
	}

	private void validateDataFromFile(Preferences prefs, int preferencesId, int numberOfDecimalPlacesId, int unitTypeId,
			int firstUnitId, int secondUnitId, int appSkinId, int appLanguageId, int unitsLanguageId) throws IOException
	{
		if (prefs.getPreferencesId() != preferencesId || !model.numberOfDecimalPlacesExistsInDB(numberOfDecimalPlacesId)
				|| !model.unitTypeExistsInDB(unitTypeId) || !model.unitExistsInDB(firstUnitId)
				|| !model.unitExistsInDB(secondUnitId) || !model.appSkinExistsInDB(appSkinId)
				|| !model.appLanguageExistsInDB(appLanguageId) || !model.appUnitsLanguageExistsInDB(unitsLanguageId))
		{
			throw new IOException();
		}
	}

	private boolean preferencesAreDifferent(Preferences prefs, int numberOfDecimalPlacesId, int unitTypeId,
			int firstUnitId, int secondUnitId, int appSkinId, int appLanguageId, int unitsLanguageId,
			boolean updateExchangeRatesOnStartup, boolean checkForApplicationUpdatesOnStartup, boolean logHistory)
	{
		return numberOfDecimalPlacesId != prefs.getNumberOfDecimalPlaces().getNumberOfDecimalPlacesId()
				|| unitTypeId != prefs.getUnitType().getUnitTypeId() || firstUnitId != prefs.getFirstUnit().getUnitId()
				|| secondUnitId != prefs.getSecondUnit().getUnitId() || appSkinId != prefs.getAppSkin().getAppSkinId()
				|| appLanguageId != prefs.getAppLanguage().getAppLanguageId()
				|| unitsLanguageId != prefs.getUnitsLanguage().getUnitsLanguageId()
				|| updateExchangeRatesOnStartup != prefs.getUpdateExchangeRatesOnStartup()
				|| checkForApplicationUpdatesOnStartup != prefs.getCheckForApplicationUpdatesOnStartup()
				|| logHistory != prefs.getLogHistory();
	}

	public void exportPreferences(ActionEvent event)
	{
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("Preferences Files", "*.dat"));
		File file = fc.showSaveDialog(null);

		if (file != null)
		{
			try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
			{
				Preferences preferences = model.getPreferences();
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
			catch (IOException e)
			{
				String errorTitle = resourceBundle.getString("errorTitle");
				String writingFileErrorMessage = resourceBundle.getString("writingFileErrorMessage");

				Message.showMessage(errorTitle, writingFileErrorMessage, AlertType.ERROR);
			}
		}
	}

	public void quickCopy(ActionEvent event)
	{
		StringSelection selection = new StringSelection(resultTextField.getText());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}

	public void quickPaste(ActionEvent event)
	{
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		boolean hasText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);

		if (hasText)
		{
			String clipboardText = "";

			try
			{
				clipboardText = (String) contents.getTransferData(DataFlavor.stringFlavor);
			}
			catch (UnsupportedFlavorException | IOException e)
			{
				e.printStackTrace();
				return;
			}

			valueTextField.setStart(0);
			valueTextField.setEnd(valueTextField.getText().length());
			valueTextField.setTypedText(clipboardText);

			if (valueTextField.validate())
			{
				valueTextField.setText(clipboardText);
			}
		}
	}

	public void increaseNumberOfDecimalPlaces(ActionEvent event)
	{
		if (!model.getNumberOfDecimalPlaces().equals("100"))
		{
			int numberOfDecimalPlaces = Integer.valueOf(model.getNumberOfDecimalPlaces());
			changeDefaultNumberOfDecimalPlaces("" + ++numberOfDecimalPlaces);
		}
	}

	public void decreaseNumberOfDecimalPlaces(ActionEvent event)
	{
		if (!model.getNumberOfDecimalPlaces().equals("2"))
		{
			int numberOfDecimalPlaces = Integer.valueOf(model.getNumberOfDecimalPlaces());
			changeDefaultNumberOfDecimalPlaces("" + --numberOfDecimalPlaces);
		}
	}

	private void changeDefaultNumberOfDecimalPlaces(String numOfDecPlacesString)
	{
		try
		{
			model.changeDefaultNumberOfDecimalPlaces(numOfDecPlacesString);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		numberOfDecimalPlacesWasChanged.setValue(true);
	}

	public void checkApplicationUpdate(ActionEvent event)
	{
		if (updateIsPerforming() || appInfoIsShowing())
		{
			return;
		}

		updateInfoAnchorPane.setVisible(true);
		labelOngoingUpdate.setText(resourceBundle.getString("labelCheckingForUpdate"));

		executor.execute(() ->
		{
			Document doc = null;

			try
			{
				doc = Jsoup.connect("https://github.com/kam1l/JFX-Konwerter/releases").get();
			}
			catch (IOException e)
			{
				Platform.runLater(() ->
				{
					String errorTitle = resourceBundle.getString("errorTitle");
					String checkingUpdateErrorMessage = resourceBundle.getString("checkingUpdateErrorMessage");

					Message.showMessage(errorTitle, checkingUpdateErrorMessage, AlertType.ERROR);

					updateInfoAnchorPane.setVisible(false);
					labelOngoingUpdate.setText(resourceBundle.getString("labelOngoingUpdate"));
				});

				return;
			}

			Elements elementsByClass = doc.getElementsByClass("release-title");

			for (Element element : elementsByClass)
			{
				String text = element.text();
				DefaultArtifactVersion availableVersion = new DefaultArtifactVersion(text);

				if (availableVersion.compareTo(APP_VERSION) > 0)
				{
					Platform.runLater(() ->
					{
						String informationTitle = resourceBundle.getString("informationTitle");
						String newVersionAvailableMessage = resourceBundle.getString("newVersionAvailableMessage");

						boolean confirmed = Message.showConfirmationMessage(informationTitle,
								newVersionAvailableMessage, AlertType.CONFIRMATION);

						if (confirmed)
						{
							hostServices.showDocument(resourceBundle.getString("applicationDownloadPage"));
						}

						updateInfoAnchorPane.setVisible(false);
						labelOngoingUpdate.setText(resourceBundle.getString("labelOngoingUpdate"));
					});

					return;
				}
			}

			Platform.runLater(() ->
			{
				String informationTitle = resourceBundle.getString("informationTitle");
				String noUpdatesAvailableMessage = resourceBundle.getString("noUpdatesAvailableMessage");

				Message.showMessage(informationTitle, noUpdatesAvailableMessage, AlertType.INFORMATION);

				updateInfoAnchorPane.setVisible(false);
				labelOngoingUpdate.setText(resourceBundle.getString("labelOngoingUpdate"));
			});
		});
	}

	private void checkUpdatesInTheBackground()
	{
		executor.execute(() ->
		{
			Document doc = null;

			try
			{
				doc = Jsoup.connect("https://github.com/kam1l/JFX-Konwerter/releases").get();
			}
			catch (IOException e)
			{
				return;
			}

			Elements elementsByClass = doc.getElementsByClass("release-title");

			for (Element element : elementsByClass)
			{
				String text = element.text();
				DefaultArtifactVersion availableVersion = new DefaultArtifactVersion(text);

				if (availableVersion.compareTo(APP_VERSION) > 0)
				{
					Platform.runLater(() ->
					{
						String informationTitle = resourceBundle.getString("informationTitle");
						String newVersionAvailableMessage = resourceBundle.getString("newVersionAvailableMessage");

						boolean confirmed = Message.showConfirmationMessage(informationTitle,
								newVersionAvailableMessage, AlertType.CONFIRMATION);

						if (confirmed)
						{
							hostServices.showDocument(resourceBundle.getString("applicationDownloadPage"));
						}
					});

					return;
				}
			}
		});
	}

	public void closeAppInfo(ActionEvent event)
	{
		appInfoAnchorPane.setVisible(false);
	}

	public void openEmailClient(ActionEvent event)
	{
		hostServices.showDocument(resourceBundle.getString("authorEmailAddress"));
	}

	public void openWebBrowser(ActionEvent event)
	{
		hostServices.showDocument(resourceBundle.getString("applicationWebsite"));
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